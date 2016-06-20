package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveAllVmCinderDisksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmIconDao;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveVmTemplateCommand<T extends VmTemplateParametersBase> extends VmTemplateCommand<T>
        implements QuotaStorageDependent {

    private List<DiskImage> imageTemplates;
    private final Map<Guid, List<DiskImage>> storageToDisksMap = new HashMap<>();
    /** used only while removing base-template */
    private EngineLock baseTemplateSuccessorLock;
    /** used only while removing base-template */
    private VmTemplate baseTemplateSuccessor;

    @Inject
    private VmIconDao vmIconDao;

    public RemoveVmTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        super.setVmTemplateId(parameters.getVmTemplateId());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));
    }

    public RemoveVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    private void initStoragePoolInfo() {
        if (getVmTemplate() != null) {
            setStoragePoolId(getVmTemplate().getStoragePoolId());
        }
    }

    @Override
    public void init() {
        initStoragePoolInfo();
        getParameters().setUseCinderCommandCallback(
                !ImagesHandler.filterDisksBasedOnCinder(getImageTemplates()).isEmpty());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    protected boolean validate() {
        Guid vmTemplateId = getVmTemplateId();
        VmTemplate template = getVmTemplate();

        if (!super.validate()) {
            return false;
        }

        boolean isInstanceType = getVmTemplate().getTemplateType() == VmEntityType.INSTANCE_TYPE;

        if (getCluster() == null && !isInstanceType) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            return false;
        }

        // check template exists
        if (!validate(templateExists())) {
            return false;
        }
        // check not blank template
        if (VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(vmTemplateId)) {
            return failValidation(EngineMessage.VMT_CANNOT_REMOVE_BLANK_TEMPLATE);
        }

        // check storage pool valid
        if (!isInstanceType && !validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        // check if delete protected
        if (template.isDeleteProtected()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DELETE_PROTECTION_ENABLED);
        }

        if (!isInstanceType) {
            getImageTemplates();
        }

        // populate all the domains of the template
        Set<Guid> allDomainsList = getStorageDomainsByDisks(imageTemplates, true);
        getParameters().setStorageDomainsList(new ArrayList<>(allDomainsList));

        // check template images for selected domains
        ArrayList<String> validationMessages = getReturnValue().getValidationMessages();
        for (Guid domainId : getParameters().getStorageDomainsList()) {
            if (!isVmTemplateImagesReady(getVmTemplate(),
                    domainId,
                    validationMessages,
                    getParameters().getCheckDisksExists(),
                    true,
                    false,
                    true,
                    storageToDisksMap.get(domainId))) {
                return false;
            }
        }

        // check no vms from this template on selected domains
        List<VM> vms = getVmDao().getAllWithTemplate(vmTemplateId);
        List<String> problematicVmNames = new ArrayList<>();
        for (VM vm : vms) {
            problematicVmNames.add(vm.getName());
        }

        if (!problematicVmNames.isEmpty()) {
            return failValidation(EngineMessage.VMT_CANNOT_REMOVE_DETECTED_DERIVED_VM,
                    String.format("$vmsList %1$s", StringUtils.join(problematicVmNames, ",")));
        }

        if (template.isBaseTemplate() && !tryLockSubVersionIfExists()) {
            return false;
        }

        if (!isInstanceType && !validate(checkNoDisksBasedOnTemplateDisks())) {
            return false;
        }

        return true;
    }

    /**
     * It locks direct sub-template of deleted template if it exits.
     * @return true if locking was successful or there is no direct sub-template, false otherwise
     */
    private boolean tryLockSubVersionIfExists() {
        final List<VmTemplate> templateSubVersions =
                getVmTemplateDao().getTemplateVersionsForBaseTemplate(getVmTemplateId());
        if (templateSubVersions.isEmpty()) {
            return true;
        }
        baseTemplateSuccessor = templateSubVersions
                .stream()
                .min(Comparator.comparing(VmTemplate::getTemplateVersionNumber))
                .get();
        if (!acquireBaseTemplateSuccessorLock()) {
            return false;
        }
        if (getVmTemplateDao().get(baseTemplateSuccessor.getId()) == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SUBVERSION_BEING_CONCURRENTLY_REMOVED,
                    String.format("$subVersionId %s", baseTemplateSuccessor.getId().toString()));
        }
        return true;
    }

    /**
     * To prevent concurrent deletion.
     * @return first: true ~ successfully locked, false otherwise; second: fail reasons in form suitable for
     *         validationMessages
     */
    private boolean acquireBaseTemplateSuccessorLock() {
        final Map<String, Pair<String, String>> lockSharedMap = Collections.singletonMap(
                baseTemplateSuccessor.getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE,
                        createSubTemplateLockMessage(baseTemplateSuccessor)));
        baseTemplateSuccessorLock = new EngineLock(null, lockSharedMap);
        final Pair<Boolean, Set<String>> isLockedAndFailReason = getLockManager().acquireLock(baseTemplateSuccessorLock);
        if (isLockedAndFailReason.getFirst()) {
            return true;
        }
        baseTemplateSuccessorLock = null;
        getReturnValue().getValidationMessages().addAll(extractVariableDeclarations(isLockedAndFailReason.getSecond()));
        return false;
    }

    private String createSubTemplateLockMessage(VmTemplate template) {
        return String.format("%s$templateName %s$templateId %s",
                EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_SET_AS_BASE_TEMPLATE,
                template.getName(),
                template.getId());
    }

    private ValidationResult checkNoDisksBasedOnTemplateDisks() {
        return new DiskImagesValidator(imageTemplates).diskImagesHaveNoDerivedDisks(null);
    }

    private List<DiskImage> getImageTemplates() {
        if (imageTemplates == null) {
            List<Disk> allImages = DbFacade.getInstance().getDiskDao().getAllForVm(getVmTemplateId());
            imageTemplates = ImagesHandler.filterImageDisks(allImages, false, false, true);
            imageTemplates.addAll(ImagesHandler.filterDisksBasedOnCinder(allImages, true));
        }
        return imageTemplates;
    }

    /**
     * Get a list of all domains id that the template is on
     */
    private Set<Guid> getStorageDomainsByDisks(List<DiskImage> disks, boolean isFillStorageTodDiskMap) {
        Set<Guid> domainsList = new HashSet<>();
        if (disks != null) {
            for (DiskImage disk : disks) {
                domainsList.addAll(disk.getStorageIds());
                if (isFillStorageTodDiskMap) {
                    for (Guid storageDomainId : disk.getStorageIds()) {
                        MultiValueMapUtils.addToMap(storageDomainId, disk, storageToDisksMap);
                    }
                }
            }
        }
        return domainsList;
    }

    @Override
    protected void executeCommand() {
        if (getVmTemplate().isBaseTemplate()) {
            shiftBaseTemplateToSuccessor();
        }
        List<Disk> templateImages = DbFacade.getInstance().getDiskDao().getAllForVm(getVmTemplateId());
        final List<CinderDisk> cinderDisks = ImagesHandler.filterDisksBasedOnCinder(templateImages);
        final List<DiskImage> diskImages = ImagesHandler.filterImageDisks(templateImages, false, false, true);
        // Set VM to lock status immediately, for reducing race condition.
        VmTemplateHandler.lockVmTemplateInTransaction(getVmTemplateId(), getCompensationContext());

        if (!diskImages.isEmpty() || !cinderDisks.isEmpty()) {
            TransactionSupport.executeInNewTransaction(() -> {
                if (!diskImages.isEmpty() && removeVmTemplateImages()) {
                    VmHandler.removeVmInitFromDB(getVmTemplate());
                    setSucceeded(true);
                }
                if (!cinderDisks.isEmpty()) {
                    removeCinderDisks(cinderDisks);
                    setSucceeded(true);
                }
                return null;
            });
        }
        // if for some reason template doesn't have images, remove it now and not in end action
        if (noAsyncOperations()) {
            handleEndAction();
        }
    }

    private void shiftBaseTemplateToSuccessor() {
        try {
            getVmTemplateDao().shiftBaseTemplate(getVmTemplateId());
        } finally {
            freeSubTemplateLock();
        }
    }

    private void freeSubTemplateLock() {
        if (baseTemplateSuccessorLock != null) {
            getLockManager().releaseLock(baseTemplateSuccessorLock);
            baseTemplateSuccessorLock = null;
        }
    }

    @Override
    protected void freeCustomLocks() {
        super.freeCustomLocks();
        freeSubTemplateLock();
    }

    /**
     * The following method performs a removing of all cinder disks from vm. These is only DB operation
     */
    private void removeCinderDisks(List<CinderDisk> cinderDisks) {
        RemoveAllVmCinderDisksParameters removeParam = new RemoveAllVmCinderDisksParameters(getVmTemplateId(), cinderDisks);
        Future<VdcReturnValueBase> future =
                CommandCoordinatorUtil.executeAsyncCommand(VdcActionType.RemoveAllVmCinderDisks,
                        withRootCommandInfo(removeParam),
                        cloneContextAndDetachFromParent());
        try {
            future.get().getActionReturnValue();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception", e);
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getVmTemplate() != null) {
            return Collections.singletonMap(getVmTemplateId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, getTemplateExclusiveLockMessage()));
        }
        return null;
    }

    private String getTemplateExclusiveLockMessage() {
        return new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_REMOVED.name())
        .append(String.format("$templateName %s$templateId %s", getVmTemplate().getName(), getVmTemplate().getId()))
        .toString();
    }

    private void removeTemplateFromDb() {
        removeNetwork();
        DbFacade.getInstance().getVmTemplateDao().remove(getVmTemplate().getId());
        vmIconDao.removeIfUnused(getVmTemplate().getSmallIconId());
        vmIconDao.removeIfUnused(getVmTemplate().getLargeIconId());
    }

    protected boolean removeVmTemplateImages() {
        getParameters().setEntityInfo(getParameters().getEntityInfo());
        getParameters().setParentCommand(getActionType());
        getParameters().setParentParameters(getParameters());
        VdcReturnValueBase vdcReturnValue = runInternalActionWithTasksContext(
                VdcActionType.RemoveAllVmTemplateImageTemplates,
                getParameters());

        if (!vdcReturnValue.getSucceeded()) {
            setSucceeded(false);
            getReturnValue().setFault(vdcReturnValue.getFault());
            return false;
        }

        getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_REMOVE_VM_TEMPLATE : AuditLogType.USER_FAILED_REMOVE_VM_TEMPLATE;
        case END_FAILURE:
        case END_SUCCESS:
        default:
            return AuditLogType.USER_REMOVE_VM_TEMPLATE_FINISHED;
        }
    }

    @Override
    protected void endSuccessfully() {
        handleEndAction();
    }

    @Override
    protected void endWithFailure() {
        handleEndAction();
    }

    private void handleEndAction() {
        try {
            removeTemplateFromDb();
            setSucceeded(true);
        } catch (RuntimeException e) {
            // Set the try again of task to false, to prevent log spam and audit log spam.
            getReturnValue().setEndActionTryAgain(false);
            log.error("Encountered a problem removing template from DB, setting the action not to retry.");
        }
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        getImageTemplates();
        if (imageTemplates != null) {
            for (DiskImage disk : imageTemplates) {
                if (disk.getQuotaId() != null && !Guid.Empty.equals(disk.getQuotaId())) {
                    for (Guid storageId : disk.getStorageIds()) {
                        list.add(new QuotaStorageConsumptionParameter(
                                disk.getQuotaId(),
                                null,
                                QuotaStorageConsumptionParameter.QuotaAction.RELEASE,
                                storageId,
                                (double) disk.getSizeInGigabytes()));
                    }
                }
            }
        }
        return list;
    }

    @Override
    public CommandCallback getCallback() {
        return getParameters().isUseCinderCommandCallback() ? new ConcurrentChildCommandsExecutionCallback() : null;
    }
}

package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_PLUGGED;

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

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveAllManagedBlockStorageDisksParameters;
import org.ovirt.engine.core.common.action.RemoveAllVmCinderDisksParameters;
import org.ovirt.engine.core.common.action.VmTemplateManagementParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmIconDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockingResult;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveVmTemplateCommand<T extends VmTemplateManagementParameters> extends VmTemplateManagementCommand<T>
        implements QuotaStorageDependent {

    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private VmIconDao vmIconDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    private List<DiskImage> imageTemplates;
    private final Map<Guid, List<DiskImage>> storageToDisksMap = new HashMap<>();
    /** used only while removing base-template */
    private EngineLock baseTemplateSuccessorLock;
    /** used only while removing base-template */
    private VmTemplate baseTemplateSuccessor;

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
        getParameters().setUseCinderCommandCallback(!DisksFilter.filterCinderDisks(getImageTemplates()).isEmpty());
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
        if (!isInstanceType && !validate(new StoragePoolValidator(getStoragePool()).existsAndUp())) {
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
        Set<Guid> allDomainsList = getStorageDomainsByDisks(imageTemplates);
        getParameters().setStorageDomainsList(new ArrayList<>(allDomainsList));

        // check template images for selected domains
        for (Guid domainId : getParameters().getStorageDomainsList()) {
            if (!validate(vmTemplateHandler.isVmTemplateImagesReady(getVmTemplate(),
                    domainId,
                    getParameters().isCheckDisksExists(),
                    true,
                    false,
                    true,
                    storageToDisksMap.get(domainId)))) {
                return false;
            }
        }

        // check no vms from this template on selected domains
        List<VM> vms = vmDao.getAllWithTemplate(vmTemplateId);
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
                vmTemplateDao.getTemplateVersionsForBaseTemplate(getVmTemplateId());
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
        if (vmTemplateDao.get(baseTemplateSuccessor.getId()) == null) {
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
        LockingResult lockingResult = lockManager.acquireLock(baseTemplateSuccessorLock);
        if (lockingResult.isAcquired()) {
            return true;
        }
        baseTemplateSuccessorLock = null;
        getReturnValue().getValidationMessages().addAll(extractVariableDeclarations(lockingResult.getMessages()));
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
            List<Disk> allImages = diskDao.getAllForVm(getVmTemplateId());
            imageTemplates = DisksFilter.filterImageDisks(allImages, ONLY_ACTIVE);
            imageTemplates.addAll(DisksFilter.filterCinderDisks(allImages, ONLY_PLUGGED));
            imageTemplates.addAll(DisksFilter.filterManagedBlockStorageDisks(allImages, ONLY_PLUGGED));
        }
        return imageTemplates;
    }

    /**
     * Get a list of all domains id that the template is on
     */
    private Set<Guid> getStorageDomainsByDisks(List<DiskImage> disks) {
        Set<Guid> domainsList = new HashSet<>();
        if (disks != null) {
            for (DiskImage disk : disks) {
                domainsList.addAll(disk.getStorageIds());
                for (Guid storageDomainId : disk.getStorageIds()) {
                    storageToDisksMap.computeIfAbsent(storageDomainId, k -> new ArrayList<>()).add(disk);
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
        List<Disk> templateImages = diskDao.getAllForVm(getVmTemplateId());
        final List<CinderDisk> cinderDisks = DisksFilter.filterCinderDisks(templateImages);
        final List<ManagedBlockStorageDisk> managedBlockDisks = DisksFilter.filterManagedBlockStorageDisks(templateImages);
        final List<DiskImage> diskImages = DisksFilter.filterImageDisks(templateImages, ONLY_ACTIVE);
        // Set VM to lock status immediately, for reducing race condition.
        vmTemplateHandler.lockVmTemplateInTransaction(getVmTemplateId(), getCompensationContext());

        if (!diskImages.isEmpty() || !cinderDisks.isEmpty() || !managedBlockDisks.isEmpty()) {
            TransactionSupport.executeInNewTransaction(() -> {
                if (!diskImages.isEmpty() && removeVmTemplateImages()) {
                    vmHandler.removeVmInitFromDB(getVmTemplate());
                    setSucceeded(true);
                }
                if (!cinderDisks.isEmpty()) {
                    removeCinderDisks(cinderDisks);
                    setSucceeded(true);
                }
                if (!managedBlockDisks.isEmpty()) {
                    removeManagedBlockDisks(managedBlockDisks);
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
            vmTemplateDao.shiftBaseTemplate(getVmTemplateId());
        } finally {
            freeSubTemplateLock();
        }
    }

    private void freeSubTemplateLock() {
        if (baseTemplateSuccessorLock != null) {
            lockManager.releaseLock(baseTemplateSuccessorLock);
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
        Future<ActionReturnValue> future =
                commandCoordinatorUtil.executeAsyncCommand(ActionType.RemoveAllVmCinderDisks,
                        withRootCommandInfo(removeParam),
                        cloneContextAndDetachFromParent());
        try {
            future.get().getActionReturnValue();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception", e);
        }
    }

    /**
     * The following method performs a removing of all managed block disks from vm.
     */
    private void removeManagedBlockDisks(List<ManagedBlockStorageDisk> managedBlockDisks) {
        RemoveAllManagedBlockStorageDisksParameters removeParam =
                new RemoveAllManagedBlockStorageDisksParameters(getVmTemplateId(), managedBlockDisks);
        Future<ActionReturnValue> future =
                commandCoordinatorUtil.executeAsyncCommand(ActionType.RemoveAllManagedBlockStorageDisks,
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
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE,
                            new LockMessage(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_REMOVED)
                                    .with("templateName", getVmTemplate().getName())
                                    .with("templateId", getVmTemplate().getId().toString())));
        }
        return null;
    }

    private void removeTemplateFromDb() {
        removeNetwork();
        vmTemplateDao.remove(getVmTemplate().getId());
        vmIconDao.removeIfUnused(getVmTemplate().getSmallIconId());
        vmIconDao.removeIfUnused(getVmTemplate().getLargeIconId());
    }

    protected boolean removeVmTemplateImages() {
        getParameters().setEntityInfo(getParameters().getEntityInfo());
        getParameters().setParentCommand(getActionType());
        getParameters().setParentParameters(getParameters());
        ActionReturnValue actionReturnValue = runInternalActionWithTasksContext(
                ActionType.RemoveAllVmTemplateImageTemplates,
                getParameters());

        if (!actionReturnValue.getSucceeded()) {
            setSucceeded(false);
            getReturnValue().setFault(actionReturnValue.getFault());
            return false;
        }

        getReturnValue().getVdsmTaskIdList().addAll(actionReturnValue.getInternalVdsmTaskIdList());
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
        return getParameters().isUseCinderCommandCallback() ? callbackProvider.get() : null;
    }
}

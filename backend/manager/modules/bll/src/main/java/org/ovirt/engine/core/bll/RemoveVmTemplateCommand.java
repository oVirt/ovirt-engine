package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveVmTemplateCommand<T extends VmTemplateParametersBase> extends VmTemplateCommand<T>
        implements QuotaStorageDependent {

    private List<DiskImage> imageTemplates;
    private final Map<Guid, List<DiskImage>> storageToDisksMap = new HashMap<Guid, List<DiskImage>>();

    public RemoveVmTemplateCommand(T parameters) {
        super(parameters);
        super.setVmTemplateId(parameters.getVmTemplateId());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));
        if (getVmTemplate() != null) {
            setStoragePoolId(getVmTemplate().getStoragePoolId());
        }
    }

    public RemoveVmTemplateCommand(Guid vmTemplateId) {
        super.setVmTemplateId(vmTemplateId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    protected boolean canDoAction() {
        Guid vmTemplateId = getVmTemplateId();
        VmTemplate template = getVmTemplate();

        if (!super.canDoAction()) {
            return false;
        }

        boolean isInstanceType = getVmTemplate().getTemplateType() == VmEntityType.INSTANCE_TYPE;

        if (getVdsGroup() == null && !isInstanceType) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            return false;
        }

        // check template exists
        if (!validate(templateExists())) {
            return false;
        }
        // check not blank template
        if (VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(vmTemplateId)) {
            return failCanDoAction(VdcBllMessages.VMT_CANNOT_REMOVE_BLANK_TEMPLATE);
        }

        // check storage pool valid
        if (!isInstanceType && !validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        // check if delete protected
        if (template.isDeleteProtected()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DELETE_PROTECTION_ENABLED);
        }

        if (!isInstanceType) {
            fetchImageTemplates();
        }

        // populate all the domains of the template
        Set<Guid> allDomainsList = getStorageDomainsByDisks(imageTemplates, true);
        getParameters().setStorageDomainsList(new ArrayList<>(allDomainsList));

        // check template images for selected domains
        ArrayList<String> canDoActionMessages = getReturnValue().getCanDoActionMessages();
        for (Guid domainId : getParameters().getStorageDomainsList()) {
            if (!isVmTemplateImagesReady(getVmTemplate(),
                    domainId,
                    canDoActionMessages,
                    getParameters().getCheckDisksExists(),
                    true,
                    false,
                    true,
                    storageToDisksMap.get(domainId))) {
                return false;
            }
        }

        // check no vms from this template on selected domains
        List<VM> vms = getVmDAO().getAllWithTemplate(vmTemplateId);
        List<String> problematicVmNames = new ArrayList<String>();
        for (VM vm : vms) {
            problematicVmNames.add(vm.getName());
        }

        if (!problematicVmNames.isEmpty()) {
            return failCanDoAction(VdcBllMessages.VMT_CANNOT_REMOVE_DETECTED_DERIVED_VM,
                    String.format("$vmsList %1$s", StringUtils.join(problematicVmNames, ",")));
        }

        // for base templates, make sure it has no versions that need to be removed first
        if (vmTemplateId.equals(template.getBaseTemplateId())) {
            List<VmTemplate> templateVersions = getVmTemplateDAO().getTemplateVersionsForBaseTemplate(vmTemplateId);
            if (!templateVersions.isEmpty()) {
                List<String> templateVersionsNames = new ArrayList<>();
                for (VmTemplate version : templateVersions) {
                    templateVersionsNames.add(version.getName());
                }

                return failCanDoAction(VdcBllMessages.VMT_CANNOT_REMOVE_BASE_WITH_VERSIONS,
                        String.format("$versionsList %1$s", StringUtils.join(templateVersionsNames, ",")));
            }
        }

        if (!isInstanceType && !validate(checkNoDisksBasedOnTemplateDisks())) {
            return false;
        }

        return true;
    }

    private ValidationResult checkNoDisksBasedOnTemplateDisks() {
        return new DiskImagesValidator(imageTemplates).diskImagesHaveNoDerivedDisks(null);
    }

    private void fetchImageTemplates() {
        if (imageTemplates == null) {
            imageTemplates =
                    ImagesHandler.filterImageDisks(DbFacade.getInstance().getDiskDao().getAllForVm(getVmTemplateId()),
                            false,
                            false,
                            true);
        }
    }

    /**
     * Get a list of all domains id that the template is on
     */
    private Set<Guid> getStorageDomainsByDisks(List<DiskImage> disks, boolean isFillStorageTodDiskMap) {
        Set<Guid> domainsList = new HashSet<Guid>();
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
        // Set VM to lock status immediately, for reducing race condition.
        VmTemplateHandler.lockVmTemplateInTransaction(getVmTemplateId(), getCompensationContext());

        if (!imageTemplates.isEmpty()) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    if (removeVmTemplateImages()) {
                        VmHandler.removeVmInitFromDB(getVmTemplate());
                        setSucceeded(true);
                    }
                    return null;
                }
            });
        } else {
            // if for some reason template doesn't have images, remove it now and not in end action
            HandleEndAction();
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
        return new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_REMOVED.name())
        .append(String.format("$TemplateName %1$s", getVmTemplate().getName()))
        .toString();
    }

    private void removeTemplateFromDb() {
        removeNetwork();
        DbFacade.getInstance().getVmTemplateDao().remove(getVmTemplate().getId());
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
        HandleEndAction();
    }

    @Override
    protected void endWithFailure() {
        HandleEndAction();
    }

    private void HandleEndAction() {
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
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        fetchImageTemplates();
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
}

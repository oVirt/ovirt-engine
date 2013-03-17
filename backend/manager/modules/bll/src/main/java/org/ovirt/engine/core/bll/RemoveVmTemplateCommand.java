package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.MultiValueMapUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
@LockIdNameAttribute(isReleaseAtEndOfExecute = false)
public class RemoveVmTemplateCommand<T extends VmTemplateParametersBase> extends VmTemplateCommand<T>
        implements QuotaStorageDependent {

    private List<DiskImage> imageTemplates;
    private final Map<Guid, List<DiskImage>> storageToDisksMap = new HashMap<Guid, List<DiskImage>>();

    public RemoveVmTemplateCommand(T parameters) {
        super(parameters);
        super.setVmTemplateId(parameters.getVmTemplateId());
        parameters.setEntityId(getVmTemplateId());
        if (getVmTemplate() != null) {
            setStoragePoolId(getVmTemplate().getstorage_pool_id());
        }
    }

    public RemoveVmTemplateCommand(Guid vmTemplateId) {
        super.setVmTemplateId(vmTemplateId);
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

        // check template exists
        if (!validate(templateExists())) {
            return false;
        }
        // check not blank template
        if (VmTemplateHandler.BlankVmTemplateId.equals(vmTemplateId)) {
            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_REMOVE_BLANK_TEMPLATE);
            return false;
        }

        // check storage pool valid
        if (getStoragePool() == null || getStoragePool().getstatus() != StoragePoolStatus.Up) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
            return false;
        }

        // check if delete protected
        if (template.isDeleteProtected()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DELETE_PROTECTION_ENABLED);
            return false;
        }

        fetchImageTemplates();
        List<Guid> storageDomainsList = getParameters().getStorageDomainsList();
        Set<Guid> allDomainsList = getStorageDoaminsByDisks(imageTemplates, true);

        // if null or empty list sent, get all template domains for deletion
        if (storageDomainsList == null || storageDomainsList.isEmpty()) {
            // populate all the domains of the template
            getParameters().setStorageDomainsList(new ArrayList<Guid>(allDomainsList));
            getParameters().setRemoveTemplateFromDb(true);
        } else {
            // if some domains sent, check that the sent domains are part of all domains
            List<String> problematicDomains = new ArrayList<String>();
            for (Guid domainId : storageDomainsList) {
                if (!allDomainsList.contains(domainId)) {
                    StorageDomainStatic domain = DbFacade.getInstance().getStorageDomainStaticDao().get(domainId);
                    if (domain == null) {
                        problematicDomains.add(domainId.toString());
                    } else {
                        problematicDomains.add(domain.getStorageName());
                    }
                }
            }
            if (!problematicDomains.isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_REMOVE_DOMAINS_LIST_MISMATCH);
                addCanDoActionMessage(String.format("$domainsList %1$s", StringUtils.join(problematicDomains, ",")));
                return false;
            }
            getParameters().setRemoveTemplateFromDb(allDomainsList.size() == storageDomainsList.size());
        }

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
        List<VM> vms = DbFacade.getInstance().getVmDao().getAllWithTemplate(vmTemplateId);
        List<String> problematicVmNames = new ArrayList<String>();
        for (VM vm : vms) {
            if (getParameters().isRemoveTemplateFromDb()) {
                problematicVmNames.add(vm.getName());
            } else {
                List<DiskImage> vmDIsks =
                        ImagesHandler.filterImageDisks(DbFacade.getInstance().getDiskDao().getAllForVm(vm.getId()),
                                false,
                                false);
                Set<Guid> domainsIds = getStorageDoaminsByDisks(vmDIsks, false);
                for (Guid domainId : domainsIds) {
                    if (!getParameters().getStorageDomainsList().contains(domainId)) {
                        problematicVmNames.add(vm.getName());
                    }
                }
            }
        }

        if (!problematicVmNames.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_REMOVE_DETECTED_DERIVED_VM);
            addCanDoActionMessage(String.format("$vmsList %1$s", StringUtils.join(problematicVmNames, ",")));
            return false;
        }

        return true;
    }

    private void fetchImageTemplates() {
        if (imageTemplates == null) {
            imageTemplates =
                    ImagesHandler.filterImageDisks(DbFacade.getInstance().getDiskDao().getAllForVm(getVmTemplateId()),
                            false,
                            false);
        }
    }

    /**
     * Get a list of all domains id that the template is on
     */
    private Set<Guid> getStorageDoaminsByDisks(List<DiskImage> disks, boolean isFillStorageTodDiskMap) {
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
        // if for some reason template doesn't have images, remove it now and not in end action
        final boolean hasImages = imageTemplates.size() > 0;
        if (hasImages) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    if (RemoveVmTemplateImages()) {
                        setSucceeded(true);
                    }
                    return null;
                }
            });
        } else {
            HandleEndAction();
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, getTemplateExclusiveLockMessage()));
    }

    private String getTemplateExclusiveLockMessage() {
        return new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_REMOVED.name())
        .append(String.format("$TemplateName %1$s", getVmTemplate().getName()))
        .toString();
    }

    private void RemoveTemplateFromDb() {
        RemoveNetwork();
        DbFacade.getInstance().getVmTemplateDao().remove(getVmTemplate().getId());
    }

    protected boolean RemoveVmTemplateImages() {
        getParameters().setEntityId(getParameters().getEntityId());
        getParameters().setParentCommand(getActionType());
        getParameters().setParentParameters(getParameters());
        VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                VdcActionType.RemoveAllVmTemplateImageTemplates,
                getParameters(),
                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

        if (!vdcReturnValue.getSucceeded()) {
            setSucceeded(false);
            getReturnValue().setFault(vdcReturnValue.getFault());
            return false;
        }

        getReturnValue().getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
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
            if (getParameters().isRemoveTemplateFromDb()) {
                RemoveTemplateFromDb();
            } else {
                // unlock template
                VmTemplateHandler.UnLockVmTemplate(getVmTemplateId());
            }
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
                    list.add(new QuotaStorageConsumptionParameter(
                            disk.getQuotaId(),
                            null,
                            QuotaStorageConsumptionParameter.QuotaAction.RELEASE,
                            disk.getStorageIds().get(0),
                            (double) disk.getSizeInGigabytes()));
                }
            }
        }
        return list;
    }
}

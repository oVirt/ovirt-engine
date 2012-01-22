package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.image_group_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveVmTemplateCommand<T extends VmTemplateParametersBase> extends VmTemplateCommand<T> {
    public RemoveVmTemplateCommand(T parameters) {
        super(parameters);
        super.setVmTemplateId(parameters.getVmTemplateId());
        parameters.setEntityId(getVmTemplateId());
    }

    @Override
    protected boolean canDoAction() {
        Guid vmTemplateId = getVmTemplateId();
        VmTemplate template = getVmTemplate();

        // add command specific can do action variables
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);

        // check template exists
        if (template == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            return false;
        }
        // check not blank template
        if (VmTemplateHandler.BlankVmTemplateId.equals(vmTemplateId)) {
            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_REMOVE_BLANK_TEMPLATE);
            return false;
        }

        // check storage pool valid
        if (template.getstorage_pool_id() != null
                && !((Boolean) Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.IsValid,
                                new IrsBaseVDSCommandParameters(template.getstorage_pool_id().getValue()))
                        .getReturnValue()).booleanValue()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
            return false;
        }

        List<Guid> storageDomainsList = getParameters().getStorageDomainsList();
        List<Guid> allDomainsList = getAllTemplateDoamins();

        // if null or empty list sent, get all template domains for deletion
        if (storageDomainsList == null || storageDomainsList.isEmpty()) {
            // populate all the domains of the template
            getParameters().setStorageDomainsList(allDomainsList);
            getParameters().setRemoveTemplateFromDb(true);
        } else {
            // if some domains sent, check that the sent domains are part of all domains
            ArrayList<String> problematicDomains = new ArrayList<String>();
            for (Guid domainId : storageDomainsList) {
                if (!allDomainsList.contains(domainId)) {
                    storage_domain_static domain = DbFacade.getInstance().getStorageDomainStaticDAO().get(domainId);
                    if (domain == null) {
                        problematicDomains.add(domainId.toString());
                    } else {
                        problematicDomains.add(domain.getstorage_name());
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
            if (!isVmTemplateImagesReady(vmTemplateId, domainId,
                        canDoActionMessages, getParameters().getCheckDisksExists(), true, false, true)) {
                return false;
            }
        }

        // check no vms from this template on selected domains
        List<VM> vms = DbFacade.getInstance().getVmDAO().getAllWithTemplate(vmTemplateId);
        ArrayList<String> problematicVmNames = new ArrayList<String>();
        for (VM vm : vms) {
            if (getParameters().isRemoveTemplateFromDb()) {
                problematicVmNames.add(vm.getvm_name());
            } else {
                List<DiskImage> vmDIsks = DbFacade.getInstance().getDiskImageDAO().getAllForVm(vm.getvm_guid());
                if (vmDIsks != null && !vmDIsks.isEmpty()
                        && storageDomainsList.contains(vmDIsks.get(0).getstorage_id())) {
                    problematicVmNames.add(vm.getvm_name());
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

    /**
     * Get a list of all domains id that the template is on
     */
    private List<Guid> getAllTemplateDoamins() {
        ArrayList<Guid> domainsList = new ArrayList<Guid>();
        List<DiskImageTemplate> imageTemplates =
                DbFacade.getInstance().getDiskImageTemplateDAO().getAllByVmTemplate(getVmTemplateId());
        if (imageTemplates != null && !imageTemplates.isEmpty()) {
            DiskImage disk =
                    DbFacade.getInstance()
                            .getDiskImageDAO()
                            .getSnapshotById(imageTemplates.get(0).getId());

            // populate storages list
            domainsList.add(disk.getstorage_id().getValue());
            for (image_group_storage_domain_map map : DbFacade.getInstance()
                    .getStorageDomainDAO()
                    .getAllImageGroupStorageDomainMapsForImage(disk.getimage_group_id().getValue())) {
                domainsList.add(map.getstorage_domain_id());
            }
        }
        return domainsList;
    }

    public RemoveVmTemplateCommand(Guid vmTemplateId) {
        super.setVmTemplateId(vmTemplateId);
    }

    @Override
    protected void executeCommand() {
        if (VmTemplateHandler.isTemplateStatusIsNotLocked(getVmTemplateId())) {
            // Set VM to lock status immediately, for reducing race condition.
            VmTemplateHandler.lockVmTemplateInTransaction(getVmTemplateId(), getCompensationContext());
            // if for some reason template doesn't have images, remove it now and not in end action
            final boolean hasImanges =
                    DbFacade.getInstance().getDiskImageTemplateDAO().getAllByVmTemplate(getVmTemplateId()).size() > 0;
            if (RemoveTemplateInSpm(getVmTemplate().getstorage_pool_id().getValue(), getVmTemplateId())) {
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                    @Override
                    public Void runInTransaction() {
                        if (RemoveVmTemplateImages()) {
                            if (!hasImanges) {
                                RemoveTemplateFromDb();
                            }
                            setSucceeded(true);
                        }
                        return null;
                    }
                });
            }
        }
    }

    private void RemoveTemplateFromDb() {
        RemoveNetwork();
        DbFacade.getInstance().getVmTemplateDAO().remove(getVmTemplate().getId());
    }

    protected boolean RemoveVmTemplateImages() {
        getParameters().setEntityId(getParameters().getEntityId());
        VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                        VdcActionType.RemoveAllVmTemplateImageTemplates,
                        getParameters(),
                        ExecutionHandler.createDefaultContexForTasks(executionContext));

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
    protected void EndSuccessfully() {
        HandleEndAction();
    }

    @Override
    protected void EndWithFailure() {
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
            log.errorFormat("Encounter a problem removing template from DB, Setting the action, not to try again.");
        }
    }

    private static Log log = LogFactory.getLog(RemoveVmTemplateCommand.class);
}

package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class ExportVmTemplateCommand<T extends MoveOrCopyParameters> extends MoveOrCopyTemplateCommand<T> {
    public ExportVmTemplateCommand(T parameters) {
        super(parameters);
        setVmTemplateId(parameters.getContainerId());
        setStoragePoolId(getVmTemplate().getstorage_pool_id());
    }

    protected ExportVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void MoveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (DiskImage disk : disks) {
                    // we force export template image to COW+Sparse but we don't update
                    // the ovf so the import
                    // will set the original format
                    MoveOrCopyImageGroupParameters tempVar = new MoveOrCopyImageGroupParameters(containerID, disk
                            .getimage_group_id().getValue(), disk.getId(), getParameters().getStorageDomainId(),
                            getMoveOrCopyImageOperation());
                    tempVar.setParentCommand(getActionType());
                    tempVar.setParentParemeters(getParameters());
                    tempVar.setEntityId(getParameters().getEntityId());
                    tempVar.setUseCopyCollapse(true);
                    tempVar.setCopyVolumeType(CopyVolumeType.SharedVol);
                    tempVar.setVolumeFormat(disk.getvolume_format());
                    tempVar.setVolumeType(disk.getvolume_type());
                    tempVar.setPostZero(disk.getwipe_after_delete());
                    tempVar.setForceOverride(getParameters().getForceOverride());
                    MoveOrCopyImageGroupParameters p = tempVar;
                    if (getSourceDomain() != null) {
                        p.setSourceDomainId(getSourceDomain().getId());
                    }
                    VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(
                                    VdcActionType.MoveOrCopyImageGroup,
                                    p,
                                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
                    getParameters().getImagesParameters().add(p);

                    getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
                }
                return null;
            }
        });
    }

    @Override
    protected boolean canDoAction() {
        if (getVmTemplate() != null) {
            setDescription(getVmTemplateName());
        }
        boolean retVal = ImportExportCommon.CheckStorageDomain(getParameters().getStorageDomainId(), getReturnValue()
                .getCanDoActionMessages());

        if (retVal) {
            // export must be to export domain
            if (getStorageDomain().getstorage_domain_type() != StorageDomainType.ImportExport) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_SPECIFY_DOMAIN_IS_NOT_EXPORT_DOMAIN);
                retVal = false;
            }
        }

        if (retVal) {
            retVal = super.canDoAction();
        }

        // check destination storage
        if (retVal) {
            retVal = IsDomainActive(getStorageDomain().getId(), getVmTemplate().getstorage_pool_id().getValue());
        }

        if (retVal) {
            // check that we have at least one disk
            if (getParameters().getDiskInfoList() != null) {
                if (getParameters().getDiskInfoList().size() > 0) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                    retVal = false;
                }
            }
        }

        // check if template (with no override option)
        if (retVal && !getParameters().getForceOverride()) {
            retVal = !ExportVmCommand.CheckTemplateInStorageDomain(getVmTemplate().getstorage_pool_id().getValue(),
                    getParameters().getStorageDomainId(), getVmTemplateId());
            if (!retVal) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NAME_ALREADY_EXISTS);
            }
        }

        // set source storage
        if (retVal) {
            DiskImage image = getTemplateDisks().get(0);
            if (image == null) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_NO_DISKS);
                retVal = false;
            }
            if (retVal) {
                if (getSourceDomain() == null) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
                    retVal = false;
                }
            }
        }
        // check that source domain is leagal
        if (retVal) {
            retVal = IsDomainActive(getSourceDomain().getId(), getVmTemplate().getstorage_pool_id());
        }
        // check that source domain is not ISO or Export domain
        if (retVal) {
            if (getSourceDomain().getstorage_domain_type() == StorageDomainType.ISO
                    || getSourceDomain().getstorage_domain_type() == StorageDomainType.ImportExport) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                retVal = false;
            }
        }
        if (!retVal) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EXPORT);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        }
        return retVal;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_STARTING_EXPORT_TEMPLATE
                    : AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_FAILED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE
                    : AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_FAILED;
        }
        return super.getAuditLogTypeValue();
    }

    @Override
    protected void UpdateTemplateInSpm() {
        // update the target (export) domain
        VmTemplateCommand.UpdateTemplateInSpm(getVmTemplate().getstorage_pool_id().getValue(),
                new java.util.ArrayList<VmTemplate>(java.util.Arrays.asList(new VmTemplate[] { getVmTemplate() })),
                getParameters().getStorageDomainId(), null);
    }
}

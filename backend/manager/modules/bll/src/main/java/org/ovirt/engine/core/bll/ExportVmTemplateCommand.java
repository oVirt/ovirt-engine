package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@LockIdNameAttribute
public class ExportVmTemplateCommand<T extends MoveOrCopyParameters> extends MoveOrCopyTemplateCommand<T> {

    public ExportVmTemplateCommand(T parameters) {
        super(parameters);
        setStoragePoolId(getVmTemplate().getstorage_pool_id());
    }

    protected ExportVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void moveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (DiskImage disk : disks) {
                    // we force export template image to COW+Sparse but we don't update
                    // the ovf so the import
                    // will set the original format
                    MoveOrCopyImageGroupParameters p = new MoveOrCopyImageGroupParameters(containerID, disk
                            .getId(), disk.getImageId(), getParameters().getStorageDomainId(),
                            getMoveOrCopyImageOperation());
                    p.setParentCommand(getActionType());
                    p.setParentParameters(getParameters());
                    p.setEntityId(getParameters().getEntityId());
                    p.setUseCopyCollapse(true);
                    p.setCopyVolumeType(CopyVolumeType.SharedVol);
                    p.setVolumeFormat(disk.getVolumeFormat());
                    p.setVolumeType(disk.getVolumeType());
                    p.setForceOverride(getParameters().getForceOverride());
                    p.setSourceDomainId(imageFromSourceDomainMap.get(disk.getId()).getStorageIds().get(0));
                    VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(
                                    VdcActionType.MoveOrCopyImageGroup,
                                    p,
                                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
                    getParameters().getImagesParameters().add(p);

                    if (!vdcRetValue.getSucceeded()) {
                        throw new VdcBLLException(vdcRetValue.getFault().getError(), vdcRetValue.getFault()
                                .getMessage());
                    }

                    getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
                }
                return null;
            }
        });
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmTemplateId().toString(), LockMessagesMatchUtil.TEMPLATE);
    }

    @Override
    protected boolean canDoAction() {
        if (getVmTemplate() != null) {
            setDescription(getVmTemplateName());
        }
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(getStorageDomain());
        boolean retVal = validate(storageDomainValidator.isDomainExistAndActive());

        if (retVal) {
            // export must be to export domain
            if (getStorageDomain().getStorageDomainType() != StorageDomainType.ImportExport) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_SPECIFY_DOMAIN_IS_NOT_EXPORT_DOMAIN);
                retVal = false;
            }
        }

        retVal = retVal && super.canDoAction();

        // check if template (with no override option)
        if (retVal && !getParameters().getForceOverride()) {
            retVal = !ExportVmCommand.CheckTemplateInStorageDomain(getVmTemplate().getstorage_pool_id().getValue(),
                    getParameters().getStorageDomainId(), getVmTemplateId());
            if (!retVal) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NAME_ALREADY_EXISTS);
            }
        }
        return retVal;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EXPORT);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
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
    protected void incrementDbGeneration() {
        Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary =
                new HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>();
        OvfDataUpdater.getInstance().loadTemplateData(getVmTemplate());
        VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
        // update the target (export) domain
        OvfDataUpdater.getInstance().buildMetadataDictionaryForTemplate(getVmTemplate(), metaDictionary);
        OvfDataUpdater.getInstance().executeUpdateVmInSpmCommand(getVmTemplate().getstorage_pool_id().getValue(),
                metaDictionary,
                getParameters().getStorageDomainId());
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(),
                    (getVmTemplateName() == null) ? "" : getVmTemplateName());
        }
        return jobProperties;
    }
}

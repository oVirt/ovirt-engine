package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.action.AddVmFromTemplateParameters;
import org.ovirt.engine.core.common.action.CreateCloneOfTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class AddVmFromTemplateCommand<T extends AddVmFromTemplateParameters> extends AddVmCommand<T> {

    public AddVmFromTemplateCommand(T parameters) {
        super(parameters);
        parameters.setDontCheckTemplateImages(true);
    }

    protected AddVmFromTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void ExecuteVmCommand() {
        super.ExecuteVmCommand();
        // override template id to blank
        getParameters().OriginalTemplate = getVm().getvmt_guid();
        VmTemplateHandler.lockVmTemplateInTransaction(getParameters().OriginalTemplate, getCompensationContext());
        getVm().setvmt_guid(VmTemplateHandler.BlankVmTemplateId);
        getVm().getStaticData().setQuotaId(getParameters().getQuotaId());
        DbFacade.getInstance().getVmStaticDAO().update(getVm().getStaticData());
    }

    @Override
    protected boolean AddVmImages() {
        if (getVmTemplate().getDiskMap().size() > 0) {
            if (getVm().getstatus() != VMStatus.Down) {
                log.error("Cannot add images. VM is not Down");
                throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
            }
            VmHandler.LockVm(getVm().getDynamicData(), getCompensationContext());
            for (DiskImage dit : getVmTemplate().getDiskMap().values()) {
                DiskImageBase diskInfo = null;
                diskInfo = getParameters().getDiskInfoList().get(dit.getinternal_drive_mapping());
                CreateCloneOfTemplateParameters p = new CreateCloneOfTemplateParameters(dit.getId(),
                        getParameters().getVmStaticData().getId(), diskInfo);
                p.setStorageDomainId(dit.getstorage_ids().get(0));
                p.setDestStorageDomainId(imageToDestinationDomainMap.get(dit.getId()));
                p.setVmSnapshotId(getVmSnapshotId());
                p.setParentCommand(VdcActionType.AddVmFromTemplate);
                p.setEntityId(getParameters().getEntityId());
                VdcReturnValueBase result = Backend.getInstance().runInternalAction(
                                VdcActionType.CreateCloneOfTemplate,
                                p,
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
                getParameters().getImagesParameters().add(p);

                /**
                 * if couldnt create snapshot then stop the transaction and the command
                 */
                if (!result.getSucceeded()) {
                    throw new VdcBLLException(VdcBllErrors.VolumeCreationError);
                } else {
                    getTaskIdList().addAll(result.getInternalTaskIdList());
                }
            }
        }
        return true;
    }

    @Override
    protected boolean buildAndCheckDestStorageDomains() {
        if (imageToDestinationDomainMap.isEmpty()) {
            List<storage_domains> domains =
                    DbFacade.getInstance()
                            .getStorageDomainDAO()
                            .getAllForStoragePool(getVmTemplate().getstorage_pool_id().getValue());
            Map<Guid, storage_domains> storageDomainsMap = new HashMap<Guid, storage_domains>();
            for (storage_domains storageDomain : domains) {
                StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
                ArrayList<String> messages = new ArrayList<String>();
                if (validator.isDomainExistAndActive(messages) && validator.domainIsValidDestination(messages)) {
                    storageDomainsMap.put(storageDomain.getId(), storageDomain);
                }
            }
            for (DiskImage image : getVmTemplate().getDiskMap().values()) {
                for (Guid storageId : image.getstorage_ids()) {
                    if (storageDomainsMap.containsKey(storageId)) {
                        imageToDestinationDomainMap.put(image.getId(), storageId);
                        break;
                    }
                }
            }
            if (getVmTemplate().getDiskMap().values().size() != imageToDestinationDomainMap.size()) {
                log.errorFormat("Can not found any default active domain for one of the disks of template with id : {0}",
                        getVmTemplateId());
                return false;
            }
            for (Guid storageDomainId : new HashSet<Guid>(imageToDestinationDomainMap.values())) {
                destStorages.put(storageDomainId, storageDomainsMap.get(storageDomainId));
            }
            return true;
        }
        return super.buildAndCheckDestStorageDomains();
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = false;
        if (super.canDoAction()) {
            for (DiskImage dit : getVmTemplate().getDiskMap().values()) {
                retValue =
                        ImagesHandler.CheckImageConfiguration(destStorages.get(imageToDestinationDomainMap.get(dit.getId()))
                                .getStorageStaticData(),
                                getParameters().getDiskInfoList().get(dit.getinternal_drive_mapping()),
                                getReturnValue().getCanDoActionMessages());
                if (!retValue) {
                    break;
                }
            }
            retValue = true;
        }
        return retValue;
    }

    @Override
    protected int getNeededDiskSize(Guid storageId) {
        double actualSize = 0;
        List<DiskImage> disks = storageToDisksMap.get(storageId);
        for (DiskImage disk : disks) {
            actualSize += disk.getActualSize();
        }
        return (int) actualSize;
    }

    @Override
    protected void EndSuccessfully() {
        super.EndSuccessfully();
        VmTemplateHandler.UnLockVmTemplate(getParameters().OriginalTemplate);
    }

    @Override
    protected void EndWithFailure() {
        super.EndWithFailure();
        VmTemplateHandler.UnLockVmTemplate(getParameters().OriginalTemplate);
    }
}

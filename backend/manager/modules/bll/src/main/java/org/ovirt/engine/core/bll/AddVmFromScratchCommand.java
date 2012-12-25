package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@DisableInPrepareMode
@LockIdNameAttribute
public class AddVmFromScratchCommand<T extends AddVmFromScratchParameters> extends AddVmCommand<T> {
    public AddVmFromScratchCommand(T parameters) {
        super(parameters);
        getParameters().setDontCheckTemplateImages(true);
    }

    protected AddVmFromScratchCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public NGuid getStorageDomainId() {
        NGuid storageDomainId = super.getStorageDomainId();
        if (Guid.Empty.equals(storageDomainId) || storageDomainId == null) {
            List<storage_domains> storagesInPool =
                    LinqUtils.filter(DbFacade.getInstance()
                            .getStorageDomainDao().getAllForStoragePool(getStoragePoolId().getValue()),
                            new Predicate<storage_domains>() {
                                @Override
                                public boolean eval(storage_domains a) {
                                    return (a.getstorage_domain_type() != StorageDomainType.ISO && a.getstorage_domain_type() != StorageDomainType.ImportExport)
                                            && (a.getstatus() != null) && (a.getstatus() == StorageDomainStatus.Active);
                                }
                            });
            storageDomainId = (storagesInPool.size() > 0) ? storagesInPool.get(0).getId() : Guid.Empty;

            getParameters()
                    .setStorageDomainId(storageDomainId.getValue());
            setStorageDomainId(storageDomainId);
        }
        return storageDomainId;
    }

    @Override
    protected boolean addVmImages() {
        List<Disk> disks = DbFacade.getInstance().getDiskDao().getAllForVm(
                getParameters().getVmStaticData().getvmt_guid());
        if (disks.isEmpty() && !getParameters().getVmStaticData().getvmt_guid().equals(Guid.Empty)) {
            throw new VdcBLLException(VdcBllErrors.VM_TEMPLATE_CANT_LOCATE_DISKS_IN_DB);
        }

        Disk defBootDisk = null;
        for(Disk disk : getVmDisks()) {
            if(disk.isBoot()) {
                defBootDisk = disk;
                break;
            }
        }

        if (defBootDisk != null) {
            for (Disk disk : getVmDisks()) {
                if (!disk.equals(defBootDisk))
                    disk.setBoot(false);
            }
        }
        return (!disks.isEmpty()) ? ConcreteAddVmImages(((DiskImage)disks.get(0)).getImageId()) : true;
    }

    protected boolean ConcreteAddVmImages(Guid itGuid) {
        boolean ret = true;

        if (getVmDisks().size() > 0) {
            for (Disk diskInfo : getVmDisks()) {
                VdcReturnValueBase tmpRetValue = null;
                AddImageFromScratchParameters tempVar = new AddImageFromScratchParameters(itGuid, getParameters()
                            .getVmStaticData().getId(), (DiskImage) diskInfo);
                tempVar.setStorageDomainId(this.getStorageDomainId().getValue());
                tempVar.setVmSnapshotId(getVmSnapshotId());
                tempVar.setParentCommand(VdcActionType.AddVmFromScratch);
                tempVar.setEntityId(getParameters().getEntityId());
                tempVar.setParentParameters(getParameters());
                tmpRetValue = Backend.getInstance().runInternalAction(
                                VdcActionType.AddImageFromScratch,
                                tempVar,
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
                if (!tmpRetValue.getSucceeded()) {
                    log.error("AddVmFromScratchCommand::ConcreteAddVmImages: AddImageFromScratch Command failed.");
                    ret = false;
                }

                else // the AddImageFromScratch task created ended successfully:
                {
                    getReturnValue().getTaskIdList().addAll(tmpRetValue.getInternalTaskIdList());
                }
            }

            VmHandler.LockVm(getParameters().getVmStaticData().getId());
        } else {
            // if no disks send update vm here
            getVmStaticDao().incrementDbGeneration(getVm().getId());
        }

        return ret;
    }

    @Override
    protected boolean canDoAction() {
        boolean result = (getVdsGroup() != null || !Guid.Empty.equals(super.getStorageDomainId()));
        if (!result) {
            addCanDoActionMessage(VdcBllMessages.VM_CLUSTER_IS_NOT_VALID);
        } else {
            result = ImagesHandler.CheckImagesConfiguration(getStorageDomainId().getValue(),
                                                            getParameters()
                                                                           .getDiskInfoList(),
                                                            getReturnValue().getCanDoActionMessages());
        }

        if (!result) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        } else {
            result = super.canDoAction();
        }
        return result;
    }

    @Override
    protected List<? extends Disk> getVmDisks() {
        if (_vmDisks == null) {
            _vmDisks = ((getParameters().getDiskInfoList()) != null) ? getParameters().getDiskInfoList()
                    : new ArrayList<Disk>();
        }
        return _vmDisks;
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.AddImageFromScratch;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getVdsGroupId(),
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
        addPermissionSubjectForCustomProperties(permissionList);
        return permissionList;
    }
}

package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

/**
 * This class adds a thinly provisioned VM based on disks list.
 */
@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class AddVmFromScratchCommand<T extends AddVmParameters> extends AddVmCommand<T> {
    public AddVmFromScratchCommand(T parameters) {
        super(parameters, null);
    }

    public AddVmFromScratchCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    protected AddVmFromScratchCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return null;
    }

    @Override
    public Guid getStorageDomainId() {
        Guid storageDomainId = super.getStorageDomainId();
        if (Guid.Empty.equals(storageDomainId) || storageDomainId == null) {
            List<StorageDomain> storagesInPool =
                    LinqUtils.filter(DbFacade.getInstance()
                            .getStorageDomainDao().getAllForStoragePool(getStoragePoolId()),
                            new Predicate<StorageDomain>() {
                                @Override
                                public boolean eval(StorageDomain a) {
                                    return (!a.getStorageDomainType().isIsoOrImportExportDomain())
                                            && (a.getStatus() != null) && (a.getStatus() == StorageDomainStatus.Active);
                                }
                            });
            storageDomainId = (storagesInPool.size() > 0) ? storagesInPool.get(0).getId() : Guid.Empty;

            getParameters().setStorageDomainId(storageDomainId);
            setStorageDomainId(storageDomainId);
        }
        return storageDomainId;
    }

    @Override
    protected boolean checkTemplateImages(List<String> reasons) {
        return true;
    }

    @Override
    protected boolean addVmImages() {
        List<Disk> disks = DbFacade.getInstance().getDiskDao().getAllForVm(
                getParameters().getVmStaticData().getVmtGuid());
        if (disks.isEmpty() && !getParameters().getVmStaticData().getVmtGuid().equals(Guid.Empty)) {
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
        return (!disks.isEmpty()) ? concreteAddVmImages(((DiskImage) disks.get(0)).getImageId()) : true;
    }

    protected boolean concreteAddVmImages(Guid itGuid) {
        boolean ret = true;

        if (getVmDisks().size() > 0) {
            for (Disk diskInfo : getVmDisks()) {
                VdcReturnValueBase tmpRetValue = null;
                AddImageFromScratchParameters tempVar = new AddImageFromScratchParameters(itGuid, getParameters()
                            .getVmStaticData().getId(), (DiskImage) diskInfo);
                tempVar.setStorageDomainId(this.getStorageDomainId());
                tempVar.setVmSnapshotId(getVmSnapshotId());
                tempVar.setParentCommand(VdcActionType.AddVmFromScratch);
                tempVar.setEntityInfo(getParameters().getEntityInfo());
                tempVar.setParentParameters(getParameters());
                tmpRetValue = runInternalActionWithTasksContext(VdcActionType.AddImageFromScratch, tempVar);
                if (!tmpRetValue.getSucceeded()) {
                    log.error("concreteAddVmImages: AddImageFromScratch Command failed.");
                    ret = false;
                }

                else // the AddImageFromScratch task created ended successfully:
                {
                    getReturnValue().getVdsmTaskIdList().addAll(tmpRetValue.getInternalVdsmTaskIdList());
                }
            }

            VmHandler.lockVm(getParameters().getVmStaticData().getId());
        } else {
            // if no disks send update vm here
            getVmStaticDao().incrementDbGeneration(getVm().getId());
        }

        return ret;
    }

    @Override
    protected boolean canDoAction() {
        if (getVdsGroup() == null && Guid.Empty.equals(super.getStorageDomainId())) {
            return failCanDoAction(VdcBllMessages.VM_CLUSTER_IS_NOT_VALID);
        }

        if (!ImagesHandler.checkImagesConfiguration(getStorageDomainId(),
                getParameters().getDiskInfoList(), getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        return super.canDoAction();
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
        addPermissionSubjectForAdminLevelProperties(permissionList);
        return permissionList;
    }
}

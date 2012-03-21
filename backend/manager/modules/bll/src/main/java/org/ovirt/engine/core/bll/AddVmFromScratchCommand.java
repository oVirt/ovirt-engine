package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

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
                            .getStorageDomainDAO().getAllForStoragePool(getStoragePoolId().getValue()),
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
    protected boolean AddVmImages() {
        List<DiskImage> disks = DbFacade.getInstance().getDiskImageDAO().getAllForVm(
                getParameters().getVmStaticData().getvmt_guid());
        if (disks.isEmpty() && !getParameters().getVmStaticData().getvmt_guid().equals(Guid.Empty)) {
            throw new VdcBLLException(VdcBllErrors.VM_TEMPLATE_CANT_LOCATE_DISKS_IN_DB);
        }
        // only one (first) disk can be boot disk, make all other disks not boot
        // LINQ 29456
        // DiskImageBase defBootDisk = VmDisks.FirstOrDefault(a => a.boot);
        // if (defBootDisk != null)
        // {
        // VmDisks.ForEach(disk =>
        // {
        // if (disk != defBootDisk)
        // {
        // disk.boot = false;
        // }
        // });
        // }

        DiskImageBase defBootDisk = LinqUtils.firstOrNull(getVmDisks(), new Predicate<DiskImageBase>() {
            @Override
            public boolean eval(DiskImageBase diskImageBase) {
                return diskImageBase.getboot();
            }
        });

        if (defBootDisk != null) {
            for (DiskImageBase diskImageBase : getVmDisks()) {
                if (diskImageBase != defBootDisk)
                    diskImageBase.setboot(false);
            }
        }
        return (!disks.isEmpty()) ? ConcreteAddVmImages(disks.get(0).getId()) : true;
    }

    protected boolean ConcreteAddVmImages(Guid itGuid) {
        boolean ret = true;

        if (getVmDisks().size() > 0) {
            int drivesCount = 1;
            for (DiskImageBase diskInfo : getVmDisks()) {
                diskInfo.setinternal_drive_mapping((Integer.toString(drivesCount)));

                AddImageFromScratchParameters tempVar = new AddImageFromScratchParameters(itGuid, getParameters()
                        .getVmStaticData().getId(), diskInfo);
                tempVar.setStorageDomainId(this.getStorageDomainId().getValue());
                tempVar.setVmSnapshotId(getVmSnapshotId());
                tempVar.setParentCommand(VdcActionType.AddVmFromScratch);
                tempVar.setEntityId(getParameters().getEntityId());
                tempVar.setParentParemeters(getParameters());
                VdcReturnValueBase tmpRetValue = Backend.getInstance().runInternalAction(
                                VdcActionType.AddImageFromScratch,
                                tempVar,
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

                drivesCount++;
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
            UpdateVmInSpm(getVm().getstorage_pool_id(),
                    new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { getVm() })));
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

    // protected override List<Interface> VmInterfaces
    // {
    // get
    // {
    // if (_vmInterfaces == null)
    // {
    // _vmInterfaces = AddVmFromScratchParametersData.Interfaces ?? new
    // List<Interface>();
    // }
    // return _vmInterfaces;
    // }
    // }

    @Override
    protected List<DiskImageBase> getVmDisks() {
        if (_vmDisks == null) {
            _vmDisks = ((getParameters().getDiskInfoList()) != null) ? getParameters().getDiskInfoList()
                    : new java.util.ArrayList<DiskImageBase>();
        }
        return _vmDisks;
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.AddImageFromScratch;
    }

    private static Log log = LogFactory.getLog(AddVmFromScratchCommand.class);

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getVdsGroupId(),
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
    }
}

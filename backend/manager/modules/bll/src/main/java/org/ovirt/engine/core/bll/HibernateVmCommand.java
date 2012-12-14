package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.HibernateVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HibernateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class HibernateVmCommand<T extends HibernateVmParameters> extends VmOperationCommandBase<T> {
    private boolean isHibernateVdsProblematic = false;
    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected HibernateVmCommand(Guid commandId) {
        super(commandId);
    }

    public HibernateVmCommand(T parameters) {
        super(parameters);
        super.setStoragePoolId(getVm().getStoragePoolId());
        parameters.setEntityId(getVm().getId());
    }

    private Guid _storageDomainId = Guid.Empty;

    @Override
    public NGuid getStorageDomainId() {
        if (_storageDomainId.equals(Guid.Empty) && getVm() != null) {
            VmHandler.updateDisksFromDb(getVm());
            List<StorageDomainStatic> domainsInPool = DbFacade.getInstance()
                        .getStorageDomainStaticDao().getAllForStoragePool(getVm().getStoragePoolId());
            if (domainsInPool.size() > 0) {
                for (StorageDomainStatic currDomain : domainsInPool) {
                    if (currDomain.getstorage_domain_type().equals(StorageDomainType.Master)
                                || currDomain.getstorage_domain_type().equals(StorageDomainType.Data)) {
                        _storageDomainId = currDomain.getId();
                        break;
                    }
                }
            }
        }
        return _storageDomainId;
    }

    @Override
    protected void Perform() {
        // Set the VM to null, to fetch it again from the DB ,instead from the cache.
        // We want to get the VM state from the DB, to avoid multi requests for VM hibernation.
        setVm(null);
        if (VM.isStatusUp(getVm().getStatus())) {

            TransactionSupport.executeInNewTransaction(
                    new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            getCompensationContext().snapshotEntityStatus(getVm().getDynamicData(), getVm().getStatus());

                            // Set the VM to SavingState to lock the VM,to avoid situation of multi VM hibernation.
                            getVm().setStatus(VMStatus.SavingState);

                            Backend.getInstance()
                                    .getResourceManager()
                                    .RunVdsCommand(VDSCommandType.UpdateVmDynamicData,
                                            new UpdateVmDynamicDataVDSCommandParameters(getVdsId(),
                                                    getVm().getDynamicData()));
                            getCompensationContext().stateChanged();
                            return null;
                        }
                    });

            Guid image1GroupId = Guid.NewGuid();
            // this is temp code until SPM will implement the new verb that does
            // it for us:

            Guid hiberVol1 = Guid.NewGuid();
            VDSReturnValue ret1 =
                    Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.CreateImage,
                                    new CreateImageVDSCommandParameters(
                                            getVm().getStoragePoolId(),
                                            getStorageDomainId().getValue(),
                                            image1GroupId,
                                            getImageSizeInBytes(),
                                            getVolumeType(),
                                            VolumeFormat.RAW,
                                            hiberVol1,
                                            "",
                                            getStoragePool().getcompatibility_version().toString()));

            if (!ret1.getSucceeded()) {
                return;
            }
            Guid guid1 =
                    createTask(ret1.getCreationInfo(),
                            VdcActionType.HibernateVm,
                            VdcObjectType.Storage,
                            getStorageDomainId().getValue());
            getReturnValue().getTaskIdList().add(guid1);

            // second vol should be 10kb
            Guid image2GroupId = Guid.NewGuid();

            Guid hiberVol2 = Guid.NewGuid();
            VDSReturnValue ret2 =
                    Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.CreateImage,
                                    new CreateImageVDSCommandParameters(getVm().getStoragePoolId(),
                                            getStorageDomainId()
                                                    .getValue(),
                                            image2GroupId,
                                            getMetaDataSizeInBytes(),
                                            VolumeType.Sparse,
                                            VolumeFormat.COW,
                                            hiberVol2,
                                            "",
                                            getStoragePool().getcompatibility_version()
                                                    .toString()));

            if (!ret2.getSucceeded()) {
                return;
            }
            Guid guid2 = createTask(ret2.getCreationInfo(), VdcActionType.HibernateVm);
            getReturnValue().getTaskIdList().add(guid2);

            // this is the new param that should be passed to the hibernate
            // command
            getVm().setHibernationVolHandle(
                    String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s", getStorageDomainId().toString(), getVm()
                            .getStoragePoolId().toString(), image1GroupId.toString(), hiberVol1.toString(),
                            image2GroupId.toString(), hiberVol2.toString()));
            // end of temp code

            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.UpdateVmDynamicData,
                            new UpdateVmDynamicDataVDSCommandParameters(getVdsId(),
                                    getVm().getDynamicData()));

            getParameters().setTaskIds(new java.util.ArrayList<Guid>());
            getParameters().getTaskIds().add(guid1);
            getParameters().getTaskIds().add(guid2);

            setSucceeded(true);
        }
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.createVolume;
    }

    protected HibernateVmParameters getHibernateVmParams() {
        VdcActionParametersBase tempVar = getParameters();
        return (HibernateVmParameters) ((tempVar instanceof HibernateVmParameters) ? tempVar : null);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getHibernateVmParams().getAutomaticSuspend() ? getSucceeded() ? AuditLogType.AUTO_SUSPEND_VM
                    : AuditLogType.AUTO_FAILED_SUSPEND_VM : getSucceeded() ? AuditLogType.USER_SUSPEND_VM
                    : AuditLogType.USER_FAILED_SUSPEND_VM;

        case END_SUCCESS:
            return getHibernateVmParams().getAutomaticSuspend() ? getSucceeded() ? AuditLogType.AUTO_SUSPEND_VM_FINISH_SUCCESS
                    : AuditLogType.AUTO_SUSPEND_VM_FINISH_FAILURE
                    : getSucceeded() ? AuditLogType.USER_SUSPEND_VM_FINISH_SUCCESS
                            : isHibernateVdsProblematic ? AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE_WILL_TRY_AGAIN : AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE;

        default:
            return getHibernateVmParams().getAutomaticSuspend() ? AuditLogType.AUTO_SUSPEND_VM_FINISH_FAILURE
                    : isHibernateVdsProblematic ? AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE_WILL_TRY_AGAIN : AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE;
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVm() == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        // else if (IrsClusterMonitor.Instance.DiskFreePercent <
        // Config.FreeSpaceLow)
        // {
        // retValue = false;
        // ReturnValue.CanDoActionMessages.Add(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString());
        // }
        else if (getStorageDomainId().equals(Guid.Empty)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
            retValue = false;
        } else {
            if (getVm().getStatus() == VMStatus.WaitForLaunch || getVm().getStatus() == VMStatus.NotResponding) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
            } else if (getVm().getStatus() != VMStatus.Up) {
                retValue = false;
                getReturnValue().getCanDoActionMessages()
                        .add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_UP.toString());
            } else {
                if (AsyncTaskManager.getInstance().EntityHasTasks(getVmId())) {
                    retValue = false;
                    addCanDoActionMessage(VdcBllMessages.VM_CANNOT_SUSPENDE_HAS_RUNNING_TASKS);
                }
                if (retValue) {
                    // check if vm has stateless images in db in case vm was run once as stateless
                    // (then is_stateless is false)
                    if (getVm().isStateless() ||
                            DbFacade.getInstance().getSnapshotDao().exists(getVmId(), SnapshotType.STATELESS)) {
                        retValue = false;
                        addCanDoActionMessage(VdcBllMessages.VM_CANNOT_SUSPEND_STATELESS_VM);
                    } else if (DbFacade.getInstance().getVmPoolDao().getVmPoolMapByVmGuid(getVmId()) != null) {
                        retValue = false;
                        addCanDoActionMessage(VdcBllMessages.VM_CANNOT_SUSPEND_VM_FROM_POOL);
                    }

                    if (retValue) {
                        Collection<DiskImage> disksImages =
                                ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), false, false);
                        if (disksImages.isEmpty()) {
                            retValue = false;
                            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_SUSPEND_VM_WITHOUT_IMAGE_DISKS);
                        }
                    }

                    // Check storage before trying to create Images for hibernation.
                    storage_domains domain =
                            DbFacade.getInstance().getStorageDomainDao().get(getStorageDomainId().getValue());
                    if (retValue
                            && !StorageDomainSpaceChecker.hasSpaceForRequest(domain, (getImageSizeInBytes()
                                    + getMetaDataSizeInBytes())/BYTES_IN_GB)) {
                        retValue = false;
                        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
                    }
                }
            }
        }

        if (!retValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__HIBERNATE);
        }
        return retValue;
    }

    @Override
    protected void endSuccessfully() {
        if (getVm() != null) {
            if (getVm().getStatus() != VMStatus.SavingState && getVm().getStatus() != VMStatus.Up) {
                // If the Vm is not in SavingState/Up status, we shouldn't
                // perform Hibernate on it,
                // since if the Vm is in another status, something might have
                // happend to it
                // that might prevent it from being hibernated.

                // NOTE: We don't remove the 2 volumes because we don't want to
                // start here
                // another tasks.

                log.warnFormat(
                        "HibernateVmCommand::EndSuccessfully: Vm '{0}' is not in 'SavingState'/'Up' status, but in '{1}' status - not performing Hibernate.",
                        getVm().getVmName(),
                        getVm().getStatus());
                getReturnValue().setEndActionTryAgain(false);
            }

            else if (getVm().getRunOnVds() == null) {
                log.warnFormat(
                        "HibernateVmCommand::EndSuccessfully: Vm '{0}' doesn't have 'run_on_vds' value - cannot Hibernate.",
                        getVm().getVmName());
                getReturnValue().setEndActionTryAgain(false);
            }

            else {
                String hiberVol = getVm().getHibernationVolHandle();
                if (hiberVol != null) {
                    try {
                        Backend.getInstance()
                                .getResourceManager()
                                .RunVdsCommand(
                                        VDSCommandType.Hibernate,
                                        new HibernateVDSCommandParameters(new Guid(getVm().getRunOnVds().toString()),
                                                getVmId(), getVm().getHibernationVolHandle()));
                    } catch (VdcBLLException e) {
                        isHibernateVdsProblematic = true;
                        throw e;
                    }
                    setSucceeded(true);
                } else {
                    log.errorFormat("hibernation volume of VM '{0}', is not initialized.", getVm().getVmName());
                    endWithFailure();
                }
            }
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("HibernateVmCommand::EndSuccessfully: Vm is null - not performing full EndAction.");
            setSucceeded(true);
        }
    }

    @Override
    protected void endWithFailure() {
        if (getVm() != null) {
            revertTasks();
            if (getVm().getRunOnVds() != null) {
                getVm().setHibernationVolHandle(null);
                getVm().setStatus(VMStatus.Up);

                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.UpdateVmDynamicData,
                                new UpdateVmDynamicDataVDSCommandParameters(
                                        new Guid(getVm().getRunOnVds().toString()), getVm().getDynamicData()));

                setSucceeded(true);
            }

            else {
                log.warnFormat(
                        "HibernateVmCommand::endWithFailure: Vm '{0}' doesn't have 'run_on_vds' value - not clearing 'hibernation_vol_handle' info.",
                        getVm().getVmName());

                getReturnValue().setEndActionTryAgain(false);
            }
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("HibernateVmCommand::endWithFailure: Vm is null - not performing full EndAction.");
            setSucceeded(true);
        }
    }

    /**
     * Returns whether to use Sparse or Preallocation. If the storage type is file system devices ,it would be more
     * efficient to use Sparse allocation. Otherwise for block devices we should use Preallocated for faster allocation.
     *
     * @return - VolumeType of allocation type to use.
     */
    private VolumeType getVolumeType() {
        return (getStoragePool().getstorage_pool_type().isFileDomain()) ? VolumeType.Sparse
                : VolumeType.Preallocated;
    }

    /**
     * Returns the memory size should be allocated in the storage.
     *
     * @return - Memory size for allocation in bytes.
     */
    private long getImageSizeInBytes() {
        return (long) (getVm().getVmMemSizeMb() + 200 + (64 * getVm().getNumOfMonitors())) * 1024 * 1024;
    }

    /**
     * Returns the meta data that should be allocated when saving state of image.
     *
     * @return - Meta data size for allocation in bytes.
     */
    private long getMetaDataSizeInBytes() {
        return (long) 10 * 1024;
    }

    private static Log log = LogFactory.getLog(HibernateVmCommand.class);
}

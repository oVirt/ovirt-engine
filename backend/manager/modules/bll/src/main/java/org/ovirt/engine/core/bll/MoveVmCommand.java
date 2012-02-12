package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.MoveMultipleImageGroupsParameters;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.IsVmDuringInitiatingVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.Helper;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class MoveVmCommand<T extends MoveVmParameters> extends MoveOrCopyTemplateCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected MoveVmCommand(Guid commandId) {
        super(commandId);
    }

    public MoveVmCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getContainerId());
        parameters.setEntityId(getVmId());
        setStoragePoolId(getVm().getstorage_pool_id());
    }

    @Override
    protected ImageOperation getMoveOrCopyImageOperation() {
        return ImageOperation.Move;
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVm() == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        } else {
            setDescription(getVmName());
        }
        // check that vm is down and images are ok
        // not checking storage domain, there is a check in
        // CheckTemplateInStorageDomain later
        retValue = retValue
                && ImagesHandler.PerformImagesChecks(getVmId(), getReturnValue().getCanDoActionMessages(), getVm()
                        .getstorage_pool_id(), Guid.Empty, false, true, true, true, true, true, false);
        setStoragePoolId(getVm().getstorage_pool_id());

        VmHandler.updateDisksFromDb(getVm());

        retValue = retValue && CheckTemplateInStorageDomain();

        if (retValue
                && DbFacade.getInstance()
                        .getStoragePoolIsoMapDAO()
                        .get(new StoragePoolIsoMapId(getStorageDomain().getid(),
                                getVm().getstorage_pool_id())) == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
        }

        if (retValue && getVm().getDiskMap().size() <= 0) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_NO_DISKS);
            retValue = false;
        }

        // update vm snapshots for storage free space check
        for (DiskImage diskImage : getVm().getDiskMap().values()) {
            diskImage.getSnapshots().addAll(
                    ImagesHandler.getAllImageSnapshots(diskImage.getId(), diskImage.getit_guid()));
        }
        retValue = retValue && destinationHasSpace();

        if (!retValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MOVE);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        }
        return retValue;
    }

    private boolean destinationHasSpace() {
        if (!StorageDomainSpaceChecker.hasSpaceForRequest(getStorageDomain(),
                (int) getVm().getActualDiskWithSnapshotsSize())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
            return false;
        }
        return true;
    }

    protected boolean CheckTemplateInStorageDomain() {
        boolean retValue = CheckStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active)
                // LINQ 32934 && CheckIfDisksExist(Vm.DiskMap.Values.ToList());
                && CheckIfDisksExist(getVm().getDiskMap().values());
        if (retValue && !VmTemplateHandler.BlankVmTemplateId.equals(getVm().getvmt_guid())) {
            List<storage_domains> domains = (List) Backend
                    .getInstance()
                    .runInternalQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                            new GetStorageDomainsByVmTemplateIdQueryParameters(getVm().getvmt_guid())).getReturnValue();
            // LINQ 32934 if (!domains.Select(a =>
            // a.id).Contains(MoveParameters.StorageDomainId))
            List<Guid> list = LinqUtils.foreach(domains, new Function<storage_domains, Guid>() {
                @Override
                public Guid eval(storage_domains a) {
                    return a.getid();
                }
            });
            if (!list.contains(getParameters().getStorageDomainId())) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }
        return retValue;
    }

    @Override
    protected void executeCommand() {
        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDAO().get(getVm().getvm_guid());
        if (vmDynamic.getstatus() != VMStatus.Down) {
            throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
        }
        VM vm = getVm();
        // Check if vm is initializing to run or already running - if it is in
        // such state,
        // we cannot move the vm
        boolean isVmDuringInit = ((Boolean) Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.IsVmDuringInitiating,
                        new IsVmDuringInitiatingVDSCommandParameters(vm.getvm_guid())).getReturnValue()).booleanValue();

        if (isVmDuringInit) {
            log.errorFormat("VM {0} must be down for Move VM to be successfuly executed", vm.getvm_name());
            setActionReturnValue(vm.getstatus());
            setSucceeded(false);
            return;
        }

        VmHandler.LockVm(vmDynamic, getCompensationContext());
        MoveOrCopyAllImageGroups();

        setSucceeded(true);

    }

    protected boolean UpdateVmImSpm() {
        return VmCommand.UpdateVmInSpm(getVm().getstorage_pool_id(),
                new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { getVm() })));
    }

    @Override
    protected void MoveOrCopyAllImageGroups() {
        MoveMultipleImageGroupsParameters tempVar = new MoveMultipleImageGroupsParameters(getVm().getvm_guid(),
                Helper.ToList(getVm().getDiskMap().values()), getParameters().getStorageDomainId());
        tempVar.setParentCommand(getActionType());
        tempVar.setEntityId(getParameters().getEntityId());
        MoveMultipleImageGroupsParameters p = tempVar;
        VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(VdcActionType.MoveMultipleImageGroups,
                p);

        getParameters().getImagesParameters().add(p);
        getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_MOVED_VM : AuditLogType.USER_FAILED_MOVE_VM;
        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_MOVED_VM_FINISHED_SUCCESS
                    : AuditLogType.USER_MOVED_VM_FINISHED_FAILURE;
        default:
            return AuditLogType.USER_MOVED_VM_FINISHED_FAILURE;
        }
    }

    protected void EndMoveVmCommand() {
        EndActionOnAllImageGroups();

        if (getVm() != null) {
            VmHandler.UnLockVm(getVm().getvm_guid());

            VmHandler.updateDisksFromDb(getVm());
            UpdateVmImSpm();
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("MoveVmCommand::EndMoveVmCommand: Vm is null - not performing full EndAction");
        }

        setSucceeded(true);
    }

    @Override
    protected void EndSuccessfully() {
        EndMoveVmCommand();
    }

    @Override
    protected void EndWithFailure() {
        EndMoveVmCommand();
    }

    @Override
    protected VdcActionType getImagesActionType() {
        return VdcActionType.MoveMultipleImageGroups;
    }

    private static Log log = LogFactory.getLog(MoveVmCommand.class);
}

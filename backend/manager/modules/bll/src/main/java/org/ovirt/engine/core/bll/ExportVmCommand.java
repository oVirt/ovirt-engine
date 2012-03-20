package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ExportVmCommand<T extends MoveVmParameters> extends MoveOrCopyTemplateCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected ExportVmCommand(Guid commandId) {
        super(commandId);
    }

    public ExportVmCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getContainerId());
        parameters.setEntityId(getVmId());
        setStoragePoolId(getVm().getstorage_pool_id());
    }

    @Override
    protected boolean canDoAction() {
        boolean retVal = true;

        if (getVm() == null) {
            retVal = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        } else {
            setDescription(getVmName());
        }

        // check that target domain exists
        StorageDomainValidator targetstorageDomainValidator = new StorageDomainValidator(getStorageDomain());
        retVal = retVal && targetstorageDomainValidator.isDomainExistAndActive(getReturnValue()
                    .getCanDoActionMessages());

        // load the disks of vm from database
        VmHandler.updateDisksFromDb(getVm());

        // update vm snapshots for storage free space check
        for (DiskImage diskImage : getVm().getDiskMap().values()) {
            diskImage.getSnapshots().addAll(
                                ImagesHandler.getAllImageSnapshots(diskImage.getId(),
                                        diskImage.getit_guid()));
        }

        setStoragePoolId(getVm().getstorage_pool_id());

        // check that the target and source domain are in the same storage_pool
        if (DbFacade.getInstance()
                .getStoragePoolIsoMapDAO()
                .get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                        getVm().getstorage_pool_id())) == null) {
            retVal = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
        }

        // check if template exists only if asked for
        if (retVal && getParameters().getTemplateMustExists()) {
            retVal = CheckTemplateInStorageDomain(getVm().getstorage_pool_id(), getParameters().getStorageDomainId(),
                    getVm().getvmt_guid());
            if (!retVal) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_EXPORT_DOMAIN);
                getReturnValue().getCanDoActionMessages().add(
                        String.format("$TemplateName %1$s", getVm().getvmt_name()));
            }
        }

        // check if Vm has disks
        if (retVal && getVm().getDiskMap().size() == 0) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_NO_DISKS);
            retVal = false;
        }

        if (retVal) {
            Map<String, ? extends DiskImageBase> images = getParameters().getDiskInfoList();
            if (images == null) {
                images = getVm().getDiskMap();
            }
            // check that the images requested format are valid (COW+Sparse)
            retVal = ImagesHandler.CheckImagesConfiguration(getParameters().getStorageDomainId(),
                    new ArrayList<DiskImageBase>(images.values()),
                    getReturnValue().getCanDoActionMessages());

            if (retVal && getParameters().getCopyCollapse()) {
                for (DiskImage img : getVm().getDiskMap().values()) {
                    if (images.containsKey(img.getinternal_drive_mapping())) {
                        // check that no RAW format exists (we are in collapse
                        // mode)
                        if (images.get(img.getinternal_drive_mapping()).getvolume_format() == VolumeFormat.RAW
                                && img.getvolume_format() != VolumeFormat.RAW) {
                            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_EXPORT_RAW_FORMAT);
                            retVal = false;
                        }
                    }
                }
            }
        }

        // check destination storage is Export domain
        if (retVal && getStorageDomain().getstorage_domain_type() != StorageDomainType.ImportExport) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_SPECIFY_DOMAIN_IS_NOT_EXPORT_DOMAIN);
            retVal = false;
        }
        // check destination storage have free space
        if (retVal) {
            int sizeInGB = (int) getVm().getActualDiskWithSnapshotsSize();
            retVal = StorageDomainSpaceChecker.hasSpaceForRequest(getStorageDomain(), sizeInGB);
            if (!retVal)
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
        }

        if (retVal) {
            DiskImage image = LinqUtils.first(getVm().getDiskMap().values());
            if (image == null) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_NO_DISKS);
                retVal = false;
            }
        }

        retVal =
                CheckVmInStorageDomain() && retVal && validate(new SnapshotsValidator().vmNotDuringSnapshot(getVmId()))
                        && ImagesHandler.PerformImagesChecks(getVm(),
                                    getReturnValue().getCanDoActionMessages(),
                                    getVm().getstorage_pool_id(),
                                    Guid.Empty,
                                    false,
                                    true,
                                    false,
                                    false,
                                    true,
                                    true,
                                    true,
                                    true,
                                    new ArrayList<DiskImage>(getVm().getDiskMap().values()));

        if (!retVal) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EXPORT);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        }
        return retVal;
    }

    @Override
    protected void executeCommand() {
        VmHandler.checkStatusAndLockVm(getVm().getId(), getCompensationContext());

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                MoveOrCopyAllImageGroups();
                return null;
            }
        });

        if (!getReturnValue().getTaskIdList().isEmpty()) {
            setSucceeded(true);
        }
    }

    public boolean UpdateCopyVmInSpm(Guid storagePoolId, List<VM> vmsList, Guid storageDomainId) {
        HashMap<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndMetaDictionary =
                new HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>(
                        vmsList.size());
        OvfManager ovfManager = new OvfManager();
        for (VM vm : vmsList) {
            ArrayList<DiskImage> AllVmImages = new ArrayList<DiskImage>();
            VmHandler.updateDisksFromDb(vm);
            List<VmNetworkInterface> interfaces = vm.getInterfaces();
            if (interfaces != null) {
                // TODO remove this when the API changes
                interfaces.clear();
                interfaces.addAll(DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForVm(vm.getId()));
            }
            for (DiskImage disk : vm.getDiskMap().values()) {
                disk.setParentId(VmTemplateHandler.BlankVmTemplateId);
                disk.setit_guid(VmTemplateHandler.BlankVmTemplateId);
                disk.setstorage_ids(new ArrayList<Guid>(Arrays.asList(storageDomainId)));
                DiskImage diskForVolumeInfo = getDiskForVolumeInfo(disk);
                disk.setvolume_format(diskForVolumeInfo.getvolume_format());
                disk.setvolume_type(diskForVolumeInfo.getvolume_type());
                VDSReturnValue vdsReturnValue = Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.GetImageInfo,
                                new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, disk
                                        .getimage_group_id().getValue(), disk.getId()));
                if (vdsReturnValue != null && vdsReturnValue.getSucceeded()) {
                    DiskImage fromVdsm = (DiskImage) vdsReturnValue.getReturnValue();
                    disk.setactual_size(fromVdsm.getactual_size());
                }
                AllVmImages.add(disk);
            }
            if (StringHelper.isNullOrEmpty(vm.getvmt_name())) {
                VmTemplate t = DbFacade.getInstance().getVmTemplateDAO()
                        .get(vm.getvmt_guid());
                vm.setvmt_name(t.getname());
            }
            getVm().setvmt_guid(VmTemplateHandler.BlankVmTemplateId);
            String vmMeta = null;
            RefObject<String> tempRefObject = new RefObject<String>(vmMeta);
            ovfManager.ExportVm(tempRefObject, vm, AllVmImages);
            vmMeta = tempRefObject.argvalue;
            List<Guid> imageGroupIds = LinqUtils.foreach(vm.getDiskMap().values(), new Function<DiskImage, Guid>() {
                @Override
                public Guid eval(DiskImage diskImage) {
                    return diskImage.getimage_group_id().getValue();
                }
            });
            vmsAndMetaDictionary
                    .put(vm.getId(), new KeyValuePairCompat<String, List<Guid>>(vmMeta, imageGroupIds));
        }
        UpdateVMVDSCommandParameters tempVar = new UpdateVMVDSCommandParameters(storagePoolId, vmsAndMetaDictionary);
        tempVar.setStorageDomainId(storageDomainId);
        return Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.UpdateVM, tempVar)
                .getSucceeded();
    }

    @Override
    protected void MoveOrCopyAllImageGroups() {
        MoveOrCopyAllImageGroups(getVm().getId(), getVm().getDiskMap().values());
    }

    @Override
    protected void MoveOrCopyAllImageGroups(Guid containerID, Iterable<DiskImage> disks) {
        for (DiskImage disk : disks) {
            MoveOrCopyImageGroupParameters tempVar = new MoveOrCopyImageGroupParameters(containerID, disk
                    .getimage_group_id().getValue(), disk.getId(), getParameters().getStorageDomainId(),
                    getMoveOrCopyImageOperation());
            tempVar.setParentCommand(getActionType());
            tempVar.setEntityId(getParameters().getEntityId());
            tempVar.setUseCopyCollapse(getParameters().getCopyCollapse());
            DiskImage diskForVolumeInfo = getDiskForVolumeInfo(disk);
            tempVar.setVolumeFormat(diskForVolumeInfo.getvolume_format());
            tempVar.setVolumeType(diskForVolumeInfo.getvolume_type());
            tempVar.setCopyVolumeType(CopyVolumeType.LeafVol);
            tempVar.setPostZero(disk.getwipe_after_delete());
            tempVar.setForceOverride(getParameters().getForceOverride());
            MoveOrCopyImageGroupParameters p = tempVar;
            p.setParentParemeters(getParameters());
            VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(
                            VdcActionType.MoveOrCopyImageGroup,
                            p,
                            ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
            if(!vdcRetValue.getSucceeded()) {
                throw new VdcBLLException(vdcRetValue.getFault().getError(), "Failed during ExportVmCommand");
            }
            getParameters().getImagesParameters().add(p);

            getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
        }
    }

    /**
     * Return the correct disk to get the volume info (type & allocation) from. For copy collapse it's the ancestral
     * disk of the given disk, and otherwise it's the disk itself.
     *
     * @param disk
     *            The disk for which to get the disk with the info.
     * @return The disk with the correct volume info.
     */
    private DiskImage getDiskForVolumeInfo(DiskImage disk) {
        if (getParameters().getCopyCollapse()) {
            DiskImage ancestor = DbFacade.getInstance().getDiskImageDAO().getAncestor(disk.getId());
            if (ancestor == null) {
                log.warnFormat("Can't find ancestor of Disk with ID {0}, using original disk for volume info.",
                        disk.getId());
                ancestor = disk;
            }

            return ancestor;
        } else {
            return disk;
        }
    }

    /**
     * Check that vm is in export domain
     * @return
     */
    protected boolean CheckVmInStorageDomain() {
        boolean retVal = true;
        GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(getVm()
                .getstorage_pool_id(), getParameters().getStorageDomainId());
        tempVar.setGetAll(true);
        VdcQueryReturnValue qretVal = Backend.getInstance().runInternalQuery(VdcQueryType.GetVmsFromExportDomain,
                tempVar);

        if (qretVal.getSucceeded()) {
            java.util.ArrayList<VM> vms = (java.util.ArrayList<VM>) qretVal.getReturnValue();
            for (VM vm : vms) {
                // check the same id when not overriding
                if (vm.getId().equals(getVm().getId()) && !getParameters().getForceOverride()) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_GUID_ALREADY_EXIST);
                    retVal = false;
                    break;
                // check the same name when not overriding
                } else if (vm.getvm_name().equals(getVm().getvm_name()) && !getParameters().getForceOverride()) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_ALREADY_EXIST);
                    retVal = false;
                    break;
                // check if we have vm with the same name and overriding
                } else if (!vm.getId().equals(getVm().getId()) &&
                        vm.getvm_name().equals(getVm().getvm_name())) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_ALREADY_EXIST);
                    retVal = false;
                    break;
                }
                // check if we have vm with the same id and overriding
                else if (vm.getId().equals(getVm().getId()) &&
                        !vm.getvm_name().equals(getVm().getvm_name())) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_GUID_ALREADY_EXIST);
                    retVal = false;
                    break;
                }
            }
        }
        return retVal;
    }

    public static boolean CheckTemplateInStorageDomain(Guid storagePoolId, Guid storageDomainId, final Guid tmplId) {
        boolean retVal = false;
        GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(storagePoolId,
                storageDomainId);
        tempVar.setGetAll(true);
        VdcQueryReturnValue qretVal = Backend.getInstance().runInternalQuery(VdcQueryType.GetTemplatesFromExportDomain,
                tempVar);

        if (qretVal.getSucceeded()) {
            if (!VmTemplateHandler.BlankVmTemplateId.equals(tmplId)) {
                Map<VmTemplate, DiskImageList> templates = (Map) qretVal.getReturnValue();
                // LINQ VAR var tmpl = templates.FirstOrDefault(t =>
                // t.Key.vmt_guid == tmplId);
                VmTemplate tmpl = LinqUtils.firstOrNull(templates.keySet(), new Predicate<VmTemplate>() {
                    @Override
                    public boolean eval(VmTemplate vmTemplate) {
                        return vmTemplate.getId().equals(tmplId);
                    }
                });

                // retVal = false; //LINQ VAR (tmpl.Key != null);
                retVal = tmpl != null;
            } else {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_STARTING_EXPORT_VM
                    : AuditLogType.IMPORTEXPORT_EXPORT_VM_FAILED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_EXPORT_VM : AuditLogType.IMPORTEXPORT_EXPORT_VM_FAILED;

        case END_FAILURE:
            return AuditLogType.IMPORTEXPORT_EXPORT_VM_FAILED;
        }
        return super.getAuditLogTypeValue();
    }

    protected boolean UpdateVmImSpm() {
        return VmCommand.UpdateVmInSpm(getVm().getstorage_pool_id(),
                Arrays.asList(getVm()), getParameters()
                        .getStorageDomainId());
    }

    @Override
    protected void EndSuccessfully() {
        EndActionOnAllImageGroups();

        if (getVm() != null) {
            VM vm = getVm();
            VmHandler.UnLockVm(vm.getId());

            VmHandler.updateDisksFromDb(vm);
            VmDeviceUtils.setVmDevices(vm.getStaticData());
            if (getParameters().getCopyCollapse()) {
                vm.setvmt_guid(VmTemplateHandler.BlankVmTemplateId);
                vm.setvmt_name(null);
                UpdateCopyVmInSpm(getVm().getstorage_pool_id(),
                        Arrays.asList(vm), getParameters()
                                .getStorageDomainId());
            } else {
                UpdateVmImSpm();
            }
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("ExportVmCommand::EndMoveVmCommand: Vm is null - not performing full EndAction");
        }

        setSucceeded(true);
    }

    @Override
    protected void EndWithFailure() {
        EndActionOnAllImageGroups();

        if (getVm() != null) {
            VmHandler.UnLockVm(getVm().getId());
            VmHandler.updateDisksFromDb(getVm());
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("ExportVmCommand::EndMoveVmCommand: Vm is null - not performing full EndAction");
        }

        setSucceeded(true);
    }

    private static Log log = LogFactory.getLog(ExportVmCommand.class);
}

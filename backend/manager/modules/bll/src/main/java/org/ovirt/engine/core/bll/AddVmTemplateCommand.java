package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.CreateImageTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmTemplateCommand<T extends AddVmTemplateParameters> extends VmTemplateCommand<T> {
    private final java.util.ArrayList<DiskImage> mImages = new java.util.ArrayList<DiskImage>();

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmTemplateCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getMasterVm().getId());
        super.setVmTemplateName(parameters.getName());
        setVdsGroupId(parameters.getMasterVm().getvds_group_id());
        if (getVm() != null) {
            VmHandler.updateDisksFromDb(getVm());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE : AuditLogType.USER_FAILED_ADD_VM_TEMPLATE;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_SUCCESS
                    : AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_FAILURE;
        }
    }

    @Override
    protected void executeCommand() {
        // get vm status from db to check its really down before locking
        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDAO().get(getVmId());
        if (vmDynamic.getstatus() != VMStatus.Down) {
            throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
        }

        VmHandler.LockVm(vmDynamic, getCompensationContext());
        setActionReturnValue(Guid.Empty);
        setVmTemplateId(Guid.NewGuid());
        getParameters().setVmTemplateID(getVmTemplateId());
        getParameters().setEntityId(getVmTemplateId());

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                AddVmTemplateToDb();
                getCompensationContext().stateChanged();
                return null;
            }
        });
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                addPermission();
                AddVmTemplateImages();
                AddVmInterfaces();
                setSucceeded(true);
                return null;
            }
        });

    }

    @Override
    protected boolean canDoAction() {
        if (getVdsGroup() == null || !getVm().getstorage_pool_id().equals(getVdsGroup().getstorage_pool_id())) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }
        for (DiskImage diskImage : getVm().getDiskList()) {
            mImages.add(diskImage);
        }
        if (mImages.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_NO_DISKS);
            return false;
        }
        if (!VmHandler.isMemorySizeLegal(getParameters().getMasterVm().getos(),
                getParameters().getMasterVm().getmem_size_mb(),
                getReturnValue().getCanDoActionMessages(), getVdsGroup().getcompatibility_version().toString())) {
            return false;
        }
        if (!IsVmPriorityValueLegal(getParameters().getMasterVm().getpriority(), getReturnValue()
                    .getCanDoActionMessages())) {
            return false;
        }

        Guid srcStorageDomainId = mImages.get(0).getstorage_id().getValue();
        // get storage from parameters
        // or populate storage domain id from the vm domain (of the first disk)
        if (getParameters().getDestinationStorageDomainId() != null) {
            setStorageDomainId(getParameters().getDestinationStorageDomainId());
        } else {
            setStorageDomainId(srcStorageDomainId);
        }

        if (!ImagesHandler.PerformImagesChecks(getParameters().getMasterVm().getId(),
                getReturnValue().getCanDoActionMessages(),
                getVm().getstorage_pool_id(),
                srcStorageDomainId,
                true,
                true,
                true,
                true,
                true,
                false,
                true)) {
            return false;
        }
        VM vm = DbFacade.getInstance().getVmDAO().getById(getParameters().getMasterVm().getId());

        if (vm.getstatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM.toString());
            return false;
        }

        if (isVmTemlateWithSameNameExist(getVmTemplateName())) {
            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_CREATE_DUPLICATE_NAME);
            return false;
        }

        if (getStorageDomainId() != null) {
            storage_domains storage = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(
                            getStorageDomainId().getValue(), getVm().getstorage_pool_id());
            // if source and destination domains are different we need to check destination domain also
            if (!srcStorageDomainId.equals(getStorageDomainId().getValue())) {
                if (storage == null) {
                    // if storage is null then we need to check if it doesn't exist or
                    // domain is not in the same storage pool as the vm
                    if (DbFacade.getInstance().getStorageDomainStaticDAO().get(getStorageDomainId().getValue()) == null) {
                        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST.toString());
                    } else {
                        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_IN_STORAGE_POOL);
                    }
                    return false;
                }
                if (storage.getstatus() == null || storage.getstatus() != StorageDomainStatus.Active) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL.toString());
                    return false;
                }
            }

            if (storage.getstorage_domain_type() == StorageDomainType.ImportExport
                            || storage.getstorage_domain_type() == StorageDomainType.ISO) {

                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                return false;
            }
            // update vm snapshots for storage free space check
            for (DiskImage diskImage : getVm().getDiskMap().values()) {
                diskImage.getSnapshots().addAll(
                                    ImagesHandler.getAllImageSnapshots(diskImage.getId(),
                                            diskImage.getit_guid()));
            }
            if (!StorageDomainSpaceChecker.hasSpaceForRequest(storage, (int) getVm().getActualDiskWithSnapshotsSize())) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
                return false;
            }
        }
        if (!AddVmCommand.CheckCpuSockets(getParameters().getMasterVm().getnum_of_sockets(),
                    getParameters().getMasterVm().getcpu_per_socket(), getVdsGroup()
                            .getcompatibility_version().toString(), getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        return true;
    }

    protected void AddVmTemplateToDb() {
        // TODO: add timezone handling
        setVmTemplate(new VmTemplate(0, getNow(), getParameters().getDescription(),
                getParameters().getMasterVm().getmem_size_mb(), getVmTemplateName(),
                getParameters().getMasterVm().getnum_of_sockets(), getParameters().getMasterVm()
                        .getcpu_per_socket(), getParameters().getMasterVm().getos(),
                getParameters().getMasterVm().getvds_group_id(), getVmTemplateId(),
                getParameters().getMasterVm().getdomain(), getParameters().getMasterVm()
                        .getnum_of_monitors(), (VmTemplateStatus.Locked.getValue()), (getParameters().getMasterVm()
                        .getusb_policy().getValue()), getParameters().getMasterVm().gettime_zone(),
                getParameters().getMasterVm().getis_auto_suspend(), getParameters().getMasterVm()
                        .getnice_level(), getParameters().getMasterVm().getfail_back(),
                getParameters().getMasterVm().getdefault_boot_sequence(), getParameters()
                        .getMasterVm().getvm_type(), getParameters().getMasterVm().gethypervisor_type(),
                getParameters().getMasterVm().getoperation_mode()));
        getVmTemplate().setauto_startup(getParameters().getMasterVm().getauto_startup());
        getVmTemplate().setpriority(getParameters().getMasterVm().getpriority());
        getVmTemplate().setdefault_display_type(getParameters().getMasterVm().getdefault_display_type());
        getVmTemplate().setinitrd_url(getParameters().getMasterVm().getinitrd_url());
        getVmTemplate().setkernel_url(getParameters().getMasterVm().getkernel_url());
        getVmTemplate().setkernel_params(getParameters().getMasterVm().getkernel_params());
        getVmTemplate().setis_stateless(getParameters().getMasterVm().getis_stateless());
        DbFacade.getInstance().getVmTemplateDAO().save(getVmTemplate());
        getCompensationContext().snapshotNewEntity(getVmTemplate());
        setActionReturnValue(getVmTemplate().getId());
    }

    protected void AddVmInterfaces() {
        List<VmNetworkInterface> interfaces = DbFacade
                .getInstance()
                .getVmNetworkInterfaceDAO()
                .getAllForVm(
                        getParameters().getMasterVm().getId());
        for (VmNetworkInterface iface : interfaces) {
            VmNetworkInterface iDynamic = new VmNetworkInterface();
            // \\interface_statistics iStat = new interface_statistics();
            iDynamic.setId(Guid.NewGuid());
            iDynamic.setVmTemplateId(getVmTemplateId());
            // TODO why is a VM interface getting VDS details?
            // iDynamic.setAddress(iface.getInterfaceDynamic().getAddress());
            // iDynamic.setBondName(iface.getInterfaceDynamic().getBondName());
            // iDynamic.setBondType(iface.getInterfaceDynamic().getBondType());
            // iDynamic.setGateway(iface.getInterfaceDynamic().getGateway());
            iDynamic.setName(iface.getName());
            iDynamic.setNetworkName(iface.getNetworkName());
            iDynamic.setSpeed(VmInterfaceType.forValue(iface.getType()).getSpeed());
            // iDynamic.setSubnet(iface.getInterfaceDynamic().getSubnet());
            iDynamic.setType(iface.getType());

            DbFacade.getInstance().getVmNetworkInterfaceDAO().save(iDynamic);
            // \\DbFacade.Instance.addInterfaceStatistics(iStat);
        }
    }

    protected void AddVmTemplateImages() {
        Guid srcStorageDomain = mImages.get(0).getstorage_id().getValue();
        Guid vmSnapshotId = Guid.NewGuid();

        for (DiskImage diskImage : mImages) {
            CreateImageTemplateParameters createParams = new CreateImageTemplateParameters(diskImage.getId(),
                        getVmTemplateId(), getVmTemplateName(), getVmId());
            if(!diskImage.getstorage_id().equals(Guid.Empty)) {
                createParams.setStorageDomainId(diskImage.getstorage_id().getValue());
            } else {
                createParams.setStorageDomainId(srcStorageDomain);
            }
            createParams.setVmSnapshotId(vmSnapshotId);
            createParams.setEntityId(getParameters().getEntityId());
            createParams.setDestinationStorageDomainId(getStorageDomainId().getValue());
            createParams.setParentParemeters(getParameters());
            getParameters().getImagesParameters().add(createParams);
            // The return value of this action is the 'copyImage' task GUID:
            VdcReturnValueBase retValue = Backend.getInstance().runInternalAction(
                        VdcActionType.CreateImageTemplate, createParams);

            getReturnValue().getTaskIdList().addAll(retValue.getInternalTaskIdList());
        }
    }

    @Override
    protected void EndSuccessfully() {
        setVmTemplateId(getParameters().getVmTemplateId());
        // set it to null to reload from db
        setVmTemplate(null);

        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            Backend.getInstance().EndAction(VdcActionType.CreateImageTemplate, p);
        }

        if (getVmTemplate() != null) {
            UpdateTemplateInSpm(getVmTemplate().getstorage_pool_id().getValue(), new java.util.ArrayList<VmTemplate>(
                    java.util.Arrays.asList(new VmTemplate[] { getVmTemplate() })));
            if (getVm() != null) {
                VmHandler.UnLockVm(getVm().getvm_guid());
            } else {
                log.warn("AddVmTemplateCommand::EndSuccessfully: Vm is null, cannot unlock Vm");
            }
            VmTemplateHandler.UnLockVmTemplate(getVmTemplateId());
        } else {
            setCommandShouldBeLogged(false);
            log.warn("AddVmTemplateCommand::EndSuccessfully: VmTemplate is null - not performing full EndAction");
        }

        setSucceeded(true);
    }

    @Override
    protected void EndWithFailure() {
        // We evaluate 'VmTemplate' so it won't be null in the last 'if'
        // statement.
        // (a template without images doesn't exist in the 'vm_template_view').
        setVmTemplateId(getParameters().getVmTemplateId());
        VmTemplate template = getVmTemplate();

        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            Backend.getInstance().EndAction(VdcActionType.CreateImageTemplate, p);
        }

        // if template exist in db remove it
        if (getVmTemplate() != null) {
            RemoveTemplateInSpm(getVmTemplate().getstorage_pool_id().getValue(), getVmTemplateId());
            DbFacade.getInstance().getVmTemplateDAO().remove(getVmTemplateId());
            RemoveNetwork();
        }

        if (!getVmId().equals(Guid.Empty) && getVm() != null) {
            VmHandler.UnLockVm(getVmId());
        }

        setSucceeded(true);
    }

    private static LogCompat log = LogFactoryCompat.getLog(AddVmTemplateCommand.class);

    /**
     * in case of non-existing cluster the backend query will return a null
     */
    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        VDSGroup vdsGroup = getVdsGroup();
        return Collections.singletonMap(vdsGroup == null || vdsGroup.getstorage_pool_id() == null ? null : vdsGroup
                .getstorage_pool_id().getValue(), VdcObjectType.StoragePool);
    }

    private void addPermission() {
        // if the template is for public use, set EVERYONE as a TEMPLATE_USER.
        if (getParameters().isPublicUse()) {
            permissions perms = new permissions();
            perms.setad_element_id(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID);
            perms.setObjectType(VdcObjectType.VmTemplate);
            perms.setObjectId(getParameters().getVmTemplateId());
            perms.setrole_id(PredefinedRoles.TEMPLATE_USER.getId());
            MultiLevelAdministrationHandler.addPermission(perms);
        }
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
    }
}

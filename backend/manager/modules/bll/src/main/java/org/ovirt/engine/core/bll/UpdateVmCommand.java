package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.quota.Quotable;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.VMCustomProperties;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.ValidationError;


@LockIdNameAttribute
public class UpdateVmCommand<T extends VmManagementParametersBase> extends VmManagementCommandBase<T>
        implements Quotable {
    private static final long serialVersionUID = -2444359305003244168L;

    public UpdateVmCommand(T parameters) {
        super(parameters);
        if (getVdsGroup() != null) {
            setStoragePoolId(getVdsGroup().getstorage_pool_id());
        }

        if (isVmExist()) {
            setCustomDefinedProperties(parameters.getVmStaticData());
            setCustomDefinedProperties(getVm().getStaticData());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM : AuditLogType.USER_FAILED_UPDATE_VM;
    }

    @Override
    protected void executeVmCommand() {
        VM oldVm = getVm();
        VmStatic newVmStatic = getParameters().getVmStaticData();
        newVmStatic.setcreation_date(oldVm.getStaticData().getcreation_date());
        if (newVmStatic.getcreation_date().equals(DateTime.getMinValue())) {
            newVmStatic.setcreation_date(new Date());
        }
        UpdateVmNetworks();
        getVmStaticDAO().update(newVmStatic);
        updateVmPayload();
        VmDeviceUtils.updateVmDevices(getParameters(), oldVm);
        if (((Boolean) runVdsCommand(VDSCommandType.IsValid,
                new IrsBaseVDSCommandParameters(getVm().getstorage_pool_id())).getReturnValue())) {

            // Set the VM to null, to fetch it again from the DB ,instead from the cache.
            // We want to get the VM current data that was updated to the DB.
            setVm(null);
            updateVmInSpm(getVm().getstorage_pool_id(),
                    new ArrayList<VM>(Arrays.asList(new VM[] { getVm() })));
        }
        setSucceeded(true);
    }

    protected void updateVmPayload() {
        VmDeviceDAO dao = getVmDeviceDao();
        VmPayload payload = getParameters().getVmPayload();

        if (payload != null) {
            List<VmDevice> disks = dao.getVmDeviceByVmIdAndType(getVmId(), VmDeviceType.DISK.getName());
            VmDevice oldPayload = null;
            for (VmDevice disk : disks) {
                if (VmPayload.isPayload(disk.getSpecParams())) {
                    oldPayload = disk;
                    break;
                }
            }

            if (oldPayload != null) {
                List<VmDeviceId> devs = new ArrayList<VmDeviceId>();
                devs.add(oldPayload.getId());
                dao.removeAll(devs);
            }

            VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.NewGuid(), getVmId()),
                    VmDeviceType.DISK,
                    payload.getType(),
                    payload.getSpecParams(),
                    true,
                    true);
        }
    }

    private void UpdateVmNetworks() {
        // check if the cluster has changed
        if (!getVm().getvds_group_id().equals(getParameters().getVmStaticData().getvds_group_id())) {
            List<Network> networks = DbFacade
                    .getInstance()
                    .getNetworkDao()
                    .getAllForCluster(
                            getParameters().getVmStaticData().getvds_group_id());
            List<VmNetworkInterface> interfaces = DbFacade.getInstance()
                    .getVmNetworkInterfaceDao()
                    .getAllForVm(getParameters().getVmStaticData().getId());
            for (final VmNetworkInterface iface : interfaces) {
                Network net = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                    @Override
                    public boolean eval(Network n) {
                        return iface.getNetworkName().equals(n.getname());
                    }
                });
                // if network not exists in cluster we remove the network to
                // interface connection
                if (net == null) {
                    iface.setNetworkName(null);
                    DbFacade.getInstance().getVmNetworkInterfaceDao().update(iface);
                }

            }
        }
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected boolean canDoAction() {
        VM vmFromDB = getVm();
        VM vmFromParams = getParameters().getVm();

        if (!isVmExist()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        }

        if (StringUtils.isEmpty(vmFromParams.getvm_name())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
            return false;
        }

        // check that VM name is not too long
        boolean vmNameValidLength = isVmNameValidLength(vmFromParams);
        if (!vmNameValidLength) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
            return false;
        }

        // Checking if a desktop with same name already exists
        if (!StringUtils.equals(vmFromDB.getvm_name(), vmFromParams.getvm_name())) {
            boolean exists = (Boolean) getBackend()
                    .runInternalQuery(VdcQueryType.IsVmWithSameNameExist,
                            new IsVmWithSameNameExistParameters(vmFromParams.getvm_name()))
                    .getReturnValue();
            if (exists) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_ALREADY_EXIST);
                return false;
            }
        }

        List<ValidationError> validationErrors = validateCustomProperties(vmFromParams.getStaticData());
        if (!validationErrors.isEmpty()) {
            VmHandler.handleCustomPropertiesError(validationErrors, getReturnValue().getCanDoActionMessages());
            return false;
        }

        if (vmFromParams.getauto_startup()
                && vmFromParams.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_PINNED_TO_HOST);
            return false;
        }

        if (!VmHandler.isMemorySizeLegal(vmFromParams.getos(),
                vmFromParams.getmem_size_mb(), getReturnValue().getCanDoActionMessages(),
                getVdsGroup().getcompatibility_version().toString())) {
            return false;
        }

        if (!areUpdatedFieldsLegal()) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_UPDATE_ILLEGAL_FIELD);
            return false;
        }

        if (!vmFromDB.getvds_group_id().equals(vmFromParams.getvds_group_id())) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_UPDATE_CLUSTER);
            return false;
        }

        if (!isDedicatedVdsOnSameCluster(vmFromParams.getStaticData())) {
            return false;
        }

        // Check if number of monitors passed is legal
        if (!VmHandler.isNumOfMonitorsLegal(vmFromParams.getdefault_display_type(),
                vmFromParams.getnum_of_monitors(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        // if number of monitors has increased, check PCI and IDE limits are ok
        if (vmFromDB.getnum_of_monitors() < vmFromParams.getnum_of_monitors()) {
            List<Disk> allDisks = DbFacade.getInstance().getDiskDao().getAllForVm(getVmId());
            List<VmNetworkInterface> interfaces = getVmNetworkInterfaceDao().getAllForVm(getVmId());
            if (!checkPciAndIdeLimit(vmFromParams.getnum_of_monitors(),
                    interfaces,
                    allDisks,
                    getReturnValue().getCanDoActionMessages())) {
                return false;
            }
        }

        if (!VmTemplateCommand.IsVmPriorityValueLegal(vmFromParams.getpriority(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        if (vmFromDB.getVmPoolId() != null && vmFromParams.getis_stateless()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_FROM_POOL_CANNOT_BE_STATELESS);
            return false;
        }

        if (!AddVmCommand.CheckCpuSockets(vmFromParams.getnum_of_sockets(),
                vmFromParams.getcpu_per_socket(), getVdsGroup().getcompatibility_version()
                        .toString(), getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        // check for Vm Payload
        if (getParameters().getVmPayload() != null) {
            if (!checkPayload(getParameters().getVmPayload(), vmFromParams.getiso_path())) {
                return false;
            }
            // we save the content in base64 string
            getParameters().getVmPayload().setContent(Base64.encodeBase64String(
                    getParameters().getVmPayload().getContent().getBytes()));
        }

        // Check that the USB policy is legal
        if (!VmHandler.isUsbPolicyLegal(vmFromParams.getusb_policy(),
                vmFromParams.getos(),
                getVdsGroup(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        // check cpuPinning
        if (!isCpuPinningValid(vmFromParams.getCpuPinning())) {
            addCanDoActionMessage(VdcBllMessages.VM_PINNING_FORMAT_INVALID);
            return false;
        }

        if (!isPinningAndMigrationValid(getReturnValue().getCanDoActionMessages(),
                getParameters().getVm().getStaticData(), getParameters().getVm().getCpuPinning())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_CANNOT_BE_PINNED_TO_CPU_AND_MIGRATABLE);
            return false;
        }

        return true;
    }

    private boolean isVmExist() {
        return getParameters().getVmStaticData() != null && getVm() != null;
    }

    protected boolean areUpdatedFieldsLegal() {
        return VmHandler.mUpdateVmsStatic.IsUpdateValid(getVm().getStaticData(),
                getParameters().getVmStaticData(),
                getVm().getstatus());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        // user need specific permission to change custom properties
        if (isVmExist()
                &&
                (!StringUtils.equals(getVm().getPredefinedProperties(), getParameters().getVmStaticData()
                        .getPredefinedProperties())
                || !StringUtils.equals(getVm().getUserDefinedProperties(), getParameters().getVmStaticData()
                        .getUserDefinedProperties()))) {
            permissionList.add(new PermissionSubject(getParameters().getVmId(),
                    VdcObjectType.VM,
                    ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
        }
        return permissionList;
    }

    private void setCustomDefinedProperties(VmStatic vmStaticDataFromParams) {
        VMCustomProperties properties =
                VmPropertiesUtils.getInstance().parseProperties(getVdsGroup()
                        .getcompatibility_version(),
                        vmStaticDataFromParams.getCustomProperties());

        vmStaticDataFromParams.setPredefinedProperties(properties.getPredefinedProperties());
        vmStaticDataFromParams.setUserDefinedProperties(properties.getUseDefinedProperties());
    }

    @Override
    public Guid getVmId() {
        if (super.getVmId().equals(Guid.Empty)) {
            super.setVmId(getParameters().getVmStaticData().getId());
        }
        return super.getVmId();
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        if (!StringUtils.isBlank(getParameters().getVm().getvm_name())) {
            return Collections.singletonMap(getParameters().getVm().getvm_name(), LockingGroup.VM_NAME.name());
        }
        return null;
    }

    @Override
    public boolean validateAndSetQuota() {
        return getQuotaManager().validateQuotaForStoragePool(getStoragePool(),
                getVdsGroupId(),
                getQuotaId(),
                getReturnValue().getCanDoActionMessages());
    }

    @Override
    public void rollbackQuota() {
        // In update vm, we don't omit resources from quota, just
        // assign the vm to the quota, so there's nothing to rollback.
    }

    @Override
    public Guid getQuotaId() {
        return getParameters().getVmStaticData().getQuotaId();
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        if (getStoragePool() != null &&
                getQuotaId() != null &&
                !getStoragePool().getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED)) {
            VM vm = getVm();
            if (vm != null && !getQuotaId().equals(vm.getQuotaId())) {
                quotaPermissionList.add(new PermissionSubject(getQuotaId(),
                        VdcObjectType.Quota,
                        ActionGroup.CONSUME_QUOTA));
            }
        }
    }

}

package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmInterfaceCommand<T extends AddVmInterfaceParameters> extends AbstractVmInterfaceCommand<T> {

    private MacPool macPool;

    public AddVmInterfaceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        addCustomValue("InterfaceType",
                VmInterfaceType.forValue(getInterface().getType()).getDescription().toString());
        this.setVmName(getVmStaticDao().get(getParameters().getVmId()).getName());

        boolean succeeded = false;
        boolean macAddedToPool = false;

        try {
            if (StringUtils.isEmpty(getMacAddress())) {
                getInterface().setMacAddress(macPool.allocateNewMac());
                macAddedToPool = true;
            } else {
                macAddedToPool = addMacToPool(getMacAddress());
            }

            getInterface().setSpeed(VmInterfaceType.forValue(getInterface().getType()).getSpeed());
            getInterface().setId(Guid.newGuid());
            getInterface().setVmId(getParameters().getVmId());

            TransactionSupport.executeInNewTransaction(() -> {
                bumpVmVersion();
                addInterfaceToDb(getInterface());
                addInterfaceDeviceToDb();
                getCompensationContext().stateChanged();
                return null;
            });

            if (getInterface().isPlugged()) {
                succeeded = activateOrDeactivateNewNic(getInterface(), PlugAction.PLUG);
            } else {
                succeeded = true;
            }
        } finally {
            setSucceeded(succeeded);
            if (macAddedToPool && !succeeded) {
                macPool.freeMac(getMacAddress());
            }
        }
    }

    private void addInterfaceDeviceToDb() {
        VmDevice vmDevice = VmDeviceUtils.addInterface(
                getParameters().getVmId(),
                getInterface().getId(),
                getInterface().isPlugged(),
                getInterface().isPassthrough());
        getCompensationContext().snapshotNewEntity(vmDevice);
    }

    private void addInterfaceToDb(VmNic vmNetworkInterface) {
        getVmNicDao().save(vmNetworkInterface);
        getCompensationContext().snapshotNewEntity(vmNetworkInterface);

        getDbFacade().getVmNetworkStatisticsDao().save(vmNetworkInterface.getStatistics());
        getCompensationContext().snapshotNewEntity(vmNetworkInterface.getStatistics());
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        macPool = getMacPool();

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (getVm().isHostedEngine() && !getVm().isManagedHostedEngine()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_UNMANAGED_HOSTED_ENGINE);
        }

        if (!updateVnicForBackwardCompatibility()) {
            return false;
        }

        if (!validate(vmStatusLegal(getVmDynamicDao().get(getParameters().getVmId()).getStatus()))) {
            return false;
        }

        List<VmNic> interfaces = getVmNicDao().getAllForVm(getParameters().getVmId());

        if (!uniqueInterfaceName(interfaces)) {
            return false;
        }

        if (!validate(vmTemplateEmpty())) {
            return false;
        }

        // check that not exceeded PCI and IDE limit
        List<VmNic> allInterfaces = new ArrayList<>(interfaces);
        allInterfaces.add(getInterface());

        if (!pciAndIdeWithinLimit(getVm(), allInterfaces)) {
            return false;
        }

        Version compatibilityVersion = getVm().getClusterCompatibilityVersion();
        VmNicValidator nicValidator = new VmNicValidator(getInterface(), compatibilityVersion, getVm().getOs());
        if (!validate(nicValidator.isCompatibleWithOs())
                || !validate(nicValidator.profileValid(getVm().getClusterId()))
                || !validate(nicValidator.typeMatchesProfile())
                || !validate(nicValidator.passthroughIsLinked())
                || !validate(nicValidator.validateProfileNotEmptyForHostedEngineVm(getVm()))) {
            return false;
        }

        if (StringUtils.isNotEmpty(getMacAddress())) {
            if (!validate(macAvailable())) {
                return false;
            }
        } else if (macPool.getAvailableMacsCount() <= 0) {
            addValidationMessage(EngineMessage.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES);
            return false;
        }

        return true;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    /**
     * Set the parameters for bll messages, such as type and action,
     */
    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ADD_VM_INTERFACE : AuditLogType.NETWORK_ADD_VM_INTERFACE_FAILED;
    }

    /**
     * The permissions list contains the vm and the vnic profile id used by the vnic.<br>
     */
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        if (getInterface() != null && getInterface().getVnicProfileId() != null && getVm() != null) {
            permissionList.add(new PermissionSubject(getInterface().getVnicProfileId(),
                    VdcObjectType.VnicProfile,
                    getActionType().getActionGroup()));
        }

        return permissionList;
    }
}

package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VmInterfacesModifyParameters;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

import com.google.common.base.Functions;

@InternalCommandAttribute
public class VmInterfacesModifyCommand<T extends VmInterfacesModifyParameters> extends CommandBase<T> {

    @Inject
    private OsRepository osRepository;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    private VmInterfaceType defaultType;
    private Collection<VmInterfaceType> supportedInterfaceTypes;

    protected VmInterfacesModifyCommand(Guid commandId) {
        super(commandId);
    }

    public VmInterfacesModifyCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        Guid vmId = getParameters().getVmId();
        List<VmNetworkInterface> existingVnics = getNics();

        Map<String, VmNetworkInterface> existingVnicForName = existingVnics.stream()
                .collect(Collectors.toMap(VmNetworkInterface::getName, Functions.identity()));

        List<ActionParametersBase> createVnicParameters = new ArrayList<>();
        List<ActionParametersBase> updateVnicParameters = new ArrayList<>();
        Set<String> vnicsEncountered = new HashSet<>();

        // iterate over edited VNICs, see if any need to be added or have been assigned a different profile
        for (var vnicWithProfile : getParameters().getVnicsWithProfiles()) {
            VmNetworkInterface editedVnic = vnicWithProfile.getNetworkInterface();
            String vnicName = editedVnic.getName();
            VmNetworkInterface existingVnic = existingVnicForName.get(vnicName);

            updateVnicType(vnicWithProfile.getProfile(), existingVnic, editedVnic);

            if (existingVnic == null) {
                createVnicParameters.add(new AddVmInterfaceParameters(vmId, editedVnic));
            } else {
                vnicsEncountered.add(vnicName);
                Guid existingProfileId = existingVnic.getVnicProfileId();
                Guid editedProfileId = editedVnic.getVnicProfileId();

                if ((editedProfileId == null && existingProfileId != null)
                        || (editedProfileId != null && !editedProfileId.equals(existingProfileId))) {
                    existingVnic.setVnicProfileId(editedProfileId);
                    existingVnic.setNetworkName(editedVnic.getNetworkName());
                    updateVnicParameters.add(new AddVmInterfaceParameters(vmId, existingVnic));
                }
            }
        }

        // iterate over existing VNICs, see if any have not been encountered and thus removed in editing
        List<ActionParametersBase> removeVnicParameters = existingVnics.stream()
                .filter(vnic -> !vnicsEncountered.contains(vnic.getName()))
                .map(vnic -> new RemoveVmInterfaceParameters(vmId, vnic.getId()))
                .collect(Collectors.toList());

        List<ActionReturnValue> results = new ArrayList<>();
        results.addAll(runInternalMultipleActions(ActionType.AddVmInterface, createVnicParameters));
        results.addAll(runInternalMultipleActions(ActionType.UpdateVmInterface, updateVnicParameters));
        results.addAll(runInternalMultipleActions(ActionType.RemoveVmInterface, removeVnicParameters));
        if (results.stream().anyMatch(r -> !r.getSucceeded())) {
            log.error("Modifying VM NICs failed");
            setSucceeded(false);
            return;
        }
        if (getParameters().isAddingNewVm()) {
            VmOperationParameterBase reorderParams = new VmOperationParameterBase(vmId);
            ActionReturnValue result = runInternalAction(ActionType.ReorderVmNics, reorderParams);
            if (!result.getSucceeded()) {
                log.error("Reordering VM NICs failed");
                setSucceeded(false);
                return;
            }
        }

        setSucceeded(true);
    }

    private void updateVnicType(VnicProfileView profile, VmNetworkInterface existingVnic, VmNetworkInterface editedVnic) {
        boolean shouldBePciPassthroughType = profile != null && profile.isPassthrough()
                && supportedInterfaceTypes != null && supportedInterfaceTypes.contains(VmInterfaceType.pciPassthrough);
        if (existingVnic == null) {
            if (shouldBePciPassthroughType) {
                editedVnic.setType(VmInterfaceType.pciPassthrough.getValue());
            } else {
                editedVnic.setType(defaultType == null ? null : defaultType.getValue());
            }
        } else {
            VmInterfaceType existingInterfaceType = VmInterfaceType.forValue(existingVnic.getType());
            boolean shouldRestoreToDefault =
                    profile != null && !profile.isPassthrough()
                            && VmInterfaceType.pciPassthrough.equals(existingInterfaceType);

            if (shouldBePciPassthroughType) {
                existingVnic.setType(VmInterfaceType.pciPassthrough.getValue());
            } else if (shouldRestoreToDefault
                    || supportedInterfaceTypes == null
                    || !supportedInterfaceTypes.contains(existingInterfaceType)) {
                existingVnic.setType(defaultType == null ? null : defaultType.getValue());
            }
        }
    }

    private List<VmNetworkInterface> getNics() {
        List<String> devices = osRepository.getNetworkDevices(getParameters().getOsId(),
                getParameters().getCompatibilityVersion());
        supportedInterfaceTypes = new ArrayList<>();
        for (String deviceType : devices) {
            try {
                supportedInterfaceTypes.add(VmInterfaceType.valueOf(deviceType));
            } catch (IllegalArgumentException e) {
                // ignore if we can't find the enum value.
            }
        }
        defaultType = getDefaultNicType(supportedInterfaceTypes);
        var nics = vmNetworkInterfaceDao.getAllForVm(getParameters().getVmId(), getCurrentUser().getId(), false);
        return nics != null ? nics : new ArrayList<>();
    }

    private VmInterfaceType getDefaultNicType(Collection<VmInterfaceType> items) {
        if (items == null || items.isEmpty()) {
            return null;
        } else if (items.contains(VmInterfaceType.pv)) {
            return VmInterfaceType.pv;
        } else {
            return items.iterator().next();
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));
        return permissionList;
    }

}

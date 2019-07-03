package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;

public abstract class BaseInterfaceCreatingManager {

    protected VmInterfaceType defaultType;
    protected Collection<VmInterfaceType> supportedInterfaceTypes;
    private PostVnicCreatedCallback callback;

    public BaseInterfaceCreatingManager(PostVnicCreatedCallback callback) {
        this.callback = callback;
    }

    public PostVnicCreatedCallback getCallback() {
        return callback;
    }

    public static interface PostVnicCreatedCallback {
        void vnicCreated(Guid vmId, UnitVmModel unitVmModel);

        void queryFailed();
    }

    public void updateVnics(final Guid vmId,
            final Iterable<VnicInstanceType> vnicsWithProfiles,
            final UnitVmModel unitVmModel) {
        getNics(new AsyncQuery<>(existingVnics -> {
            if (existingVnics == null) {
                existingVnics = new ArrayList<>();
            }

            Map<String, VmNetworkInterface> existingVnicForName = new HashMap<>();
            for (VmNetworkInterface vnic : existingVnics) {
                existingVnicForName.put(vnic.getName(), vnic);
            }

            final ArrayList<ActionParametersBase> createVnicParameters = new ArrayList<>();
            final ArrayList<ActionParametersBase> updateVnicParameters = new ArrayList<>();
            final ArrayList<ActionParametersBase> removeVnicParameters = new ArrayList<>();
            final Set<String> vnicsEncountered = new HashSet<>();

            // iterate over edited VNICs, see if any need to be added or have been assigned a different profile
            for (VnicInstanceType vnicWithProfile : vnicsWithProfiles) {
                VmNetworkInterface editedVnic = vnicWithProfile.getNetworkInterface();
                String vnicName = editedVnic.getName();
                VmNetworkInterface existingVnic = existingVnicForName.get(vnicName);

                VnicProfileView profile = vnicWithProfile.getSelectedItem();

                updateVnicType(profile, existingVnic, editedVnic);

                if (existingVnic == null) {
                    createVnicParameters.add(createAddInterfaceParameter(vmId, editedVnic));
                } else {
                    vnicsEncountered.add(vnicName);
                    Guid existingProfileId = existingVnic.getVnicProfileId();
                    Guid editedProfileId = editedVnic.getVnicProfileId();

                    if ((editedProfileId == null && existingProfileId != null)
                            || (editedProfileId != null && !editedProfileId.equals(existingProfileId))) {
                        existingVnic.setVnicProfileId(editedProfileId);
                        existingVnic.setNetworkName(editedVnic.getNetworkName());
                        updateVnicParameters.add(createAddInterfaceParameter(vmId, existingVnic));
                    }
                }
            }

            // iterate over existing VNICs, see if any have not been encountered and thus removed in editing
            for (VmNetworkInterface existingVnic : existingVnics) {
                if (!vnicsEncountered.contains(existingVnic.getName())) {
                    removeVnicParameters.add(createRemoveInterfaceParameter(vmId, existingVnic.getId()));
                }
            }

            doNicManipulation(
                    createVnicParameters,
                    updateVnicParameters,
                    removeVnicParameters,
                    unitVmModel.getIsNew() || unitVmModel.getIsClone(),
                    vmId,
                    unitVmModel);
        }), vmId, unitVmModel);
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

    protected abstract ActionParametersBase createAddInterfaceParameter(Guid id, VmNetworkInterface editedVnic);

    protected abstract ActionParametersBase createRemoveInterfaceParameter(Guid id, Guid nicId);

    protected abstract void getNics(AsyncQuery<List<VmNetworkInterface>> getNicsQuery, Guid id, UnitVmModel unitVmModel);

    protected abstract void doNicManipulation(
            ArrayList<ActionParametersBase> createVnicParameters,
            final ArrayList<ActionParametersBase> updateVnicParameters,
            final ArrayList<ActionParametersBase> removeVnicParameters,
            final boolean isAddingNewVm,
            final Guid id,
            final UnitVmModel unitVmModel);
}

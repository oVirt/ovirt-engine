package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class VmInterfaceCreatingManager {

    private PostVnicCreatedCallback callback;

    public VmInterfaceCreatingManager(PostVnicCreatedCallback callback) {
        this.callback = callback;
    }

    private void updateVnicsFromParams(final Guid vmId, ArrayList<VdcActionParametersBase> parameters) {
        if (parameters.size() == 0) {
            callback.vnicCreated(vmId);
            return;
        }

        Frontend.RunMultipleAction(
                VdcActionType.UpdateVmInterface,
                parameters,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        callback.vnicCreated(vmId);
                    }
                },
                this
                );
    }

    /**
     * Used mainly when the VM is created from the template. If the Vm has been created but without NICs, new
     * ones are created according to the vnicInstanceTypes. If the VM is created with nics - e.g. by copying from template update their profiles
     * as edited by the user (again, according to the vnicInstanceTypes).
     *
     * @param vmId The ID of the VM
     * @param vnicInstanceTypes list of nics as edited in the window
     */
    public void updateOrCreateIfNothingToUpdate(final Guid vmId,
            final List<VnicInstanceType> vnicInstanceTypes) {
        AsyncQuery getVmNicsQuery = new AsyncQuery();
        getVmNicsQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                List<VmNetworkInterface> createdNics = (List<VmNetworkInterface>) result;

                if (createdNics == null || createdNics.size() == 0) {
                    // there are no vnics created - create according to the setup
                    createVnics(vmId, vnicInstanceTypes);
                } else {
                    // there are some vnics created - update according to the setup in the window
                    ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

                    for (VmNetworkInterface created : createdNics) {
                        for (VnicInstanceType edited : vnicInstanceTypes) {
                            // can not use getId() because they have different IDs - one is already created, one is not
                            // yet
                            boolean sameNic = edited.getNetworkInterface().getName().equals(created.getName());

                            boolean bothProfilesNull =
                                    created.getVnicProfileId() == null
                                            && edited.getNetworkInterface().getVnicProfileId() == null;

                            boolean sameProfiles =
                                    created.getVnicProfileId() != null
                                            && created.getVnicProfileId().equals(edited.getNetworkInterface()
                                                    .getVnicProfileId());

                            boolean assignedProfileChanged = !(bothProfilesNull || sameProfiles);

                            if (sameNic && assignedProfileChanged) {
                                created.setVnicProfileId(edited.getNetworkInterface().getVnicProfileId());
                                parameters.add(new AddVmInterfaceParameters(vmId, created));
                                break;
                            }
                        }

                    }

                    updateVnicsFromParams(vmId, parameters);
                }
            }
        };
        AsyncDataProvider.getVmNicList(getVmNicsQuery, vmId);
    }

    /**
     * Update the nic->profile assignment on the VM which already has the NICs created. Used when editing a VM.
     *
     * @param vmId The ID of the VM
     * @param vnicInstanceTypes list of nics as edited in the window
     */
    public void updateVnics(final Guid vmId, final List<VnicInstanceType> vnicInstanceTypes) {
        updateVnicsFromParams(vmId, createAddVmInterfaceParams(vmId, vnicInstanceTypes));
    }

    /**
     * Create new NICs with profiles assignment according to the config in the window.
     *
     * @param vmId The ID of the VM
     * @param vnicInstanceTypes list of nics as edited in the window
     */
    public void createVnics(final Guid vmId,
            final List<VnicInstanceType> vnicInstanceTypes) {
        ArrayList<VdcActionParametersBase> parameters = createAddVmInterfaceParams(vmId, vnicInstanceTypes);

        if (parameters.size() == 0) {
            callback.vnicCreated(vmId);
            return;
        }

        Frontend.RunMultipleAction(
                VdcActionType.AddVmInterface,
                parameters,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        callback.vnicCreated(vmId);
                    }

                },
                this
                );

    }

    private ArrayList<VdcActionParametersBase> createAddVmInterfaceParams(final Guid vmId,
            final List<VnicInstanceType> vnicInstanceTypes) {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

        for (VnicInstanceType vnicInstanceType : vnicInstanceTypes) {
            parameters.add(new AddVmInterfaceParameters(vmId, vnicInstanceType.getNetworkInterface()));
        }
        return parameters;
    }

    public PostVnicCreatedCallback getCallback() {
        return callback;
    }

    public static interface PostVnicCreatedCallback {
        void vnicCreated(Guid vmId);

        void queryFailed();
    }
}

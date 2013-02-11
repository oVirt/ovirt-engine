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

public class VmNetworkCreatingManager {

    private PostNetworkCreatedCallback callback;

    public VmNetworkCreatingManager(PostNetworkCreatedCallback callback) {
        this.callback = callback;
    }

    private void updateNetworksFromParams(final Guid vmId, ArrayList<VdcActionParametersBase> parameters) {
        if (parameters.size() == 0) {
            callback.networkCreated(vmId);
            return;
        }

        Frontend.RunMultipleAction(
                VdcActionType.UpdateVmInterface,
                parameters,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        callback.networkCreated(vmId);
                    }
                },
                this
        );
    }

    /**
     * Used mainly when the VM is created from the template. If the Vm has been created but without NICs, new
     * ones are created according to the nicsWithLogicalNetworks. If the VM is created with nics - e.g. by copying from template update they're virtual
     * networks as edited by the user (again, according to the nicsWithLogicalNetworks).
     *
     * @param vmId The ID of the VM
     * @param nicsWithLogicalNetworks list of nics as edited in the window
     */
    public void updateOrCreateIfNothingToUpdate(final Guid vmId,
                                                final List<NicWithLogicalNetworks> nicsWithLogicalNetworks) {
        AsyncQuery getVmNicsQuery = new AsyncQuery();
        getVmNicsQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                List<VmNetworkInterface> createdNics = (List<VmNetworkInterface>) result;

                if (createdNics == null || createdNics.size() == 0) {
                    // there are no networks created - create according to the setup
                    createNetworks(vmId, nicsWithLogicalNetworks);
                } else {
                    // there are some networks created - update according to the setup in the window
                    ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

                    for (VmNetworkInterface created : createdNics) {
                        for (NicWithLogicalNetworks edited : nicsWithLogicalNetworks) {
                            // can not use getId() because they have different IDs - one is already created, one is not
                            // yet
                            boolean sameNic = edited.getNetworkInterface().getName().equals(created.getName());

                            boolean bothNetworksNull = created.getNetworkName() == null && edited.getNetworkInterface().getNetworkName() == null;

                            boolean sameNetworkNames = created.getNetworkName() != null && created.getNetworkName().equals(edited.getNetworkInterface().getNetworkName());

                            boolean assignedNetworkChanged = !(bothNetworksNull || sameNetworkNames);

                            if (sameNic && assignedNetworkChanged) {
                                created.setNetworkName(edited.getNetworkInterface().getNetworkName());
                                parameters.add(new AddVmInterfaceParameters(vmId, created));
                                break;
                            }
                        }

                    }

                    updateNetworksFromParams(vmId, parameters);
                }
            }
        };
        AsyncDataProvider.getVmNicList(getVmNicsQuery, vmId);
    }

    /**
     * Update the nic->network assignment on the VM which already has the NICs created. Used when editing a VM.
     *
     * @param vmId The ID of the VM
     * @param nicsWithLogicalNetworks list of nics as edited in the window
     */
    public void updateNetworks(final Guid vmId, final List<NicWithLogicalNetworks> nicsWithLogicalNetworks) {
        updateNetworksFromParams(vmId, createAddVmInterfaceParams(vmId, nicsWithLogicalNetworks));
    }

    /**
     * Create new NICs with network assignment according to the config in the window.
     *
     * @param vmId The ID of the VM
     * @param nicsWithLogicalNetworks list of nics as edited in the window
     */
    public void createNetworks(final Guid vmId,
            final List<NicWithLogicalNetworks> nicsWithLogicalNetworks) {
        ArrayList<VdcActionParametersBase> parameters = createAddVmInterfaceParams(vmId, nicsWithLogicalNetworks);

        if (parameters.size() == 0) {
            callback.networkCreated(vmId);
            return;
        }

        Frontend.RunMultipleAction(
                VdcActionType.AddVmInterface,
                parameters,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        callback.networkCreated(vmId);
                    }

                },
                this
                );

    }

    private ArrayList<VdcActionParametersBase> createAddVmInterfaceParams(final Guid vmId,
            final List<NicWithLogicalNetworks> nicsWithLogicalNetworks) {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

        for (NicWithLogicalNetworks nicWithNetworks : nicsWithLogicalNetworks) {
            parameters.add(new AddVmInterfaceParameters(vmId, nicWithNetworks.getNetworkInterface()));
        }
        return parameters;
    }

    public PostNetworkCreatedCallback getCallback() {
        return callback;
    }

    public static interface PostNetworkCreatedCallback {
        void networkCreated(Guid vmId);

        void queryFailed();
    }
}

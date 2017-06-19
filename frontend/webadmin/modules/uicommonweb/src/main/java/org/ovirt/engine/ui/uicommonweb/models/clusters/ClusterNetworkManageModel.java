package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class ClusterNetworkManageModel extends ListModel<ClusterNetworkModel> {

    private final SearchableListModel<?, ?> sourceListModel;
    private final UICommand okCommand;
    private final UICommand cancelCommand;
    private ClusterNetworkModel managementNetwork;
    private ClusterNetworkModel glusterNetwork;
    private ClusterNetworkModel defaultRouteNetwork;
    private boolean needsAnyChange;

    public ClusterNetworkManageModel(SearchableListModel<?, ?> sourceListModel) {
        this.sourceListModel = sourceListModel;

        cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        getCommands().add(cancelCommand);

        okCommand = UICommand.createDefaultOkUiCommand("OnManage", this); //$NON-NLS-1$
        getCommands().add(0, okCommand);

        getItemsChangedEvent().addListener((ev, sender, args) -> {
            for (ClusterNetworkModel model : getItems()) {
                if (model.isManagement()) {
                    managementNetwork = model;
                }
                if (model.isGlusterNetwork()) {
                    glusterNetwork = model;
                }
                if (model.isDefaultRouteNetwork()) {
                    defaultRouteNetwork = model;
                }
            }
        });
    }

    public boolean isMultiCluster() {
        return false;
    }

    private ClusterNetworkModel getDisplayNetwork() {
        if (!isMultiCluster()){
            for (ClusterNetworkModel clusterNetworkManageModel : getItems()) {
                if (clusterNetworkManageModel.isDisplayNetwork()) {
                   return clusterNetworkManageModel;
                }
            }
        }
        return null;
    }

    public void setDisplayNetwork(ClusterNetworkModel model, boolean value) {
        if (!isMultiCluster()) {
            if (value) {
                // Reset the old display
                if (getDisplayNetwork() != null) {
                    getDisplayNetwork().setDisplayNetwork(false);
                }
            } else {
                // Set the management network as display
                getManagementNetwork().setDisplayNetwork(true);
            }
        }
        model.setDisplayNetwork(value);
    }

    private ClusterNetworkModel getMigrationNetwork() {
        if (!isMultiCluster()) {
            for (ClusterNetworkModel clusterNetworkManageModel : getItems()) {
                if (clusterNetworkManageModel.isMigrationNetwork()) {
                    return clusterNetworkManageModel;
                }
            }
        }
        return null;
    }

    public void setMigrationNetwork(ClusterNetworkModel model, boolean value) {
        if (!isMultiCluster()) {
            if (value) {
                // Reset the old migration
                if (getMigrationNetwork() != null) {
                    getMigrationNetwork().setMigrationNetwork(false);
                }
            } else {
                // Set the management network as migration
                getManagementNetwork().setMigrationNetwork(true);
            }
        }
        model.setMigrationNetwork(value);
    }

    private ClusterNetworkModel getManagementNetwork() {
        return managementNetwork;
    }

    public void setManagementNetwork(ClusterNetworkModel model, boolean value) {
        if (!isMultiCluster()) {
            if (value) {
                // Unset the old management network
                if (getManagementNetwork() != null) {
                    getManagementNetwork().setManagement(false);
                }
                managementNetwork = model;
            }
        }
        model.setManagement(value);
   }

   private ClusterNetworkModel getGlusterNetwork() {
        return glusterNetwork;
    }

    public void setGlusterNetwork(ClusterNetworkModel model, boolean value) {
        if (!isMultiCluster()) {
            if (value) {
                // Reset the old gluster network
                if (getGlusterNetwork() != null) {
                    getGlusterNetwork().setGlusterNetwork(false);
                }
                glusterNetwork = model;
            }
        }
        model.setGlusterNetwork(value);
    }

    public ClusterNetworkModel getDefaultRouteNetwork() {
        return defaultRouteNetwork;
    }

    public void setDefaultRouteNetwork(ClusterNetworkModel model, boolean value) {
        if (!isMultiCluster()) {
            if (value) {
                // Reset the old default route network
                ClusterNetworkModel defaultRouteNetwork = getDefaultRouteNetwork();
                if (defaultRouteNetwork != null) {
                    defaultRouteNetwork.setDefaultRouteNetwork(false);
                }
                this.defaultRouteNetwork = model;
            }
        }
        model.setDefaultRouteNetwork(value);
    }

    private void onManage() {
        Iterable<ClusterNetworkModel> manageList = getItems();
        final List<NetworkCluster> toAttach = new ArrayList<>();
        final List<NetworkCluster> toDetach = new ArrayList<>();
        final List<NetworkCluster> toUpdate = new ArrayList<>();

        for (ClusterNetworkModel manageModel : manageList) {
            NetworkCluster networkCluster = manageModel.getOriginalNetworkCluster();

            boolean wasAttached = networkCluster != null;
            boolean needsAttach = manageModel.isAttached() && !wasAttached;
            boolean needsDetach = !manageModel.isAttached() && wasAttached;
            boolean needsUpdate = false;

            // Attachment wasn't changed- check if needs update
            if (wasAttached && !needsDetach) {
                if ((manageModel.isRequired() != networkCluster.isRequired())
                        || manageModel.isDisplayNetwork() != networkCluster.isDisplay()
                        || manageModel.isMigrationNetwork() != networkCluster.isMigration()
                        || manageModel.isManagement() != networkCluster.isManagement()
                        || manageModel.isDefaultRouteNetwork() != networkCluster.isDefaultRoute()
                        || manageModel.isGlusterNetwork() != networkCluster.isGluster()) {
                    needsUpdate = true;
                    copyRoles(manageModel, networkCluster);
                }
            }

            if (needsAttach) {
                toAttach.add(createNetworkCluster(manageModel));
            }

            if (needsDetach) {
                toDetach.add(networkCluster);
            }

            if (needsUpdate) {
                toUpdate.add(networkCluster);
            }
        }

        startProgress();
        needsAnyChange = !(toAttach.isEmpty() && toDetach.isEmpty() && toUpdate.isEmpty());

        if (needsAnyChange) {
            Frontend.getInstance()
                    .runAction(ActionType.ManageNetworkClusters,
                            new ManageNetworkClustersParameters(toAttach, toDetach, toUpdate),
                            result -> {
                                needsAnyChange = false;
                                doFinish();
                            });
        }
        doFinish();
    }

    private void copyRoles(ClusterNetworkModel manageModel, NetworkCluster networkCluster) {
        networkCluster.setRequired(manageModel.isRequired());
        networkCluster.setDisplay(manageModel.isDisplayNetwork());
        networkCluster.setMigration(manageModel.isMigrationNetwork());
        networkCluster.setManagement(manageModel.isManagement());
        networkCluster.setDefaultRoute(manageModel.isDefaultRouteNetwork());
        networkCluster.setGluster(manageModel.isGlusterNetwork());
    }

    private NetworkCluster createNetworkCluster(ClusterNetworkModel manageModel) {
        final NetworkCluster networkCluster = new NetworkCluster();

        networkCluster.setClusterId(manageModel.getCluster().getId());
        networkCluster.setNetworkId(manageModel.getEntity().getId());
        copyRoles(manageModel, networkCluster);

        return networkCluster;
    }

    private void doFinish() {
        if (needsAnyChange) {
            return;
        }

        stopProgress();
        cancel();
        sourceListModel.forceRefresh();
    }

    private void cancel() {
        sourceListModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == okCommand) {
            onManage();
        } else if (command == cancelCommand) {
            cancel();
        }
    }

}

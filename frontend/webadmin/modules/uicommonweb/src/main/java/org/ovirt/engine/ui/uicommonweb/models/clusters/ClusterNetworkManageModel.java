package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class ClusterNetworkManageModel extends ListModel<ClusterNetworkModel> {

    private final SearchableListModel<?> sourceListModel;
    private final UICommand okCommand;
    private final UICommand cancelCommand;
    private boolean needsAttach;
    private boolean needsDetach;
    private boolean needsUpdate;
    private ClusterNetworkModel managementNetwork;
    private ClusterNetworkModel glusterNetwork;
    private boolean needsAnyChange;

    public ClusterNetworkManageModel(SearchableListModel<?> sourceListModel) {
        this.sourceListModel = sourceListModel;

        cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        getCommands().add(cancelCommand);

        okCommand = new UICommand("OnManage", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        getCommands().add(0, okCommand);

        getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                for (ClusterNetworkModel model : getItems()) {
                    if (model.isManagement()) {
                        managementNetwork = model;
                    }
                    if (model.isGlusterNetwork()) {
                        glusterNetwork = model;
                    }
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
                managementNetwork.setDisplayNetwork(true);
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
                managementNetwork.setMigrationNetwork(true);
            }
        }
        model.setMigrationNetwork(value);
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

    private void onManage() {
        Iterable<ClusterNetworkModel> manageList = getItems();
        final ArrayList<VdcActionParametersBase> toAttach = new ArrayList<VdcActionParametersBase>();
        final ArrayList<VdcActionParametersBase> toDetach = new ArrayList<VdcActionParametersBase>();
        final ArrayList<VdcActionParametersBase> toUpdate = new ArrayList<VdcActionParametersBase>();

        for (ClusterNetworkModel manageModel : manageList) {
            NetworkCluster networkCluster = manageModel.getOriginalNetworkCluster();

            boolean wasAttached = (networkCluster != null);
            boolean needsAttach = manageModel.isAttached() && !wasAttached;
            boolean needsDetach = !manageModel.isAttached() && wasAttached;
            boolean needsUpdate = false;

            // Attachment wasn't changed- check if needs update
            if (wasAttached && !needsDetach) {
                if ((manageModel.isRequired() != networkCluster.isRequired())
                        || (manageModel.isDisplayNetwork() != networkCluster.isDisplay())
                        || (manageModel.isMigrationNetwork() != networkCluster.isMigration())
                        || manageModel.isGlusterNetwork() != networkCluster.isGluster()) {
                    needsUpdate = true;
                    networkCluster.setRequired(manageModel.isRequired());
                    networkCluster.setDisplay(manageModel.isDisplayNetwork());
                    networkCluster.setMigration(manageModel.isMigrationNetwork());
                    networkCluster.setGluster(manageModel.isGlusterNetwork());
                }
            }

            if (needsAttach) {
                toAttach.add(new AttachNetworkToVdsGroupParameter(manageModel.getCluster(), manageModel.getEntity()));
            }

            if (needsDetach) {
                toDetach.add(new AttachNetworkToVdsGroupParameter(manageModel.getCluster(), manageModel.getEntity()));
            }

            if (needsUpdate) {
                toUpdate.add(new NetworkClusterParameters(networkCluster));
            }
        }

        startProgress(null);
        needsAttach = !toAttach.isEmpty();
        needsDetach = !toDetach.isEmpty();
        needsUpdate = !toUpdate.isEmpty();
        if (needsAttach) {
            Frontend.getInstance().runMultipleAction(VdcActionType.AttachNetworkToVdsGroup,
                    toAttach,
                    new IFrontendMultipleActionAsyncCallback() {

                @Override
                public void executed(FrontendMultipleActionAsyncResult result) {
                    needsAttach = false;
                    doFinish();
                }
            });
        }
        if (needsDetach) {
            Frontend.getInstance().runMultipleAction(VdcActionType.DetachNetworkToVdsGroup,
                    toDetach,
                    new IFrontendMultipleActionAsyncCallback() {

                @Override
                public void executed(FrontendMultipleActionAsyncResult result) {
                    needsDetach = false;
                    doFinish();
                }
            });
        }
        if (needsUpdate) {
            Frontend.getInstance().runMultipleAction(VdcActionType.UpdateNetworkOnCluster,
                    toUpdate,
                    new IFrontendMultipleActionAsyncCallback() {

                @Override
                public void executed(FrontendMultipleActionAsyncResult result) {
                    needsUpdate = false;
                    doFinish();
                }
            });
        }
        doFinish();
    }

    private void doFinish() {
        if (needsAttach || needsDetach || needsUpdate) {
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

package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class ReplaceBrickModel extends Model {

    ListModel<VDS> servers;
    EntityModel<String> brickDirectory;
    ListModel<String> bricksFromServer;
    EntityModel<Boolean> showBricksList;

    public ReplaceBrickModel() {
        setServers(new ListModel<VDS>());
        setBrickDirectory(new EntityModel<String>());
        setBricksFromServer(new ListModel<String>());
        setShowBricksList(new EntityModel<Boolean>());
        init();
    }

    private void init() {
        getShowBricksList().setEntity(true);
        getBrickDirectory().setIsAvailable(false);
        getShowBricksList().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getShowBricksList().getEntity()) {
                // Show the brick list and hide the text box for entering brick dir
                getBricksFromServer().setIsAvailable(true);
                getBrickDirectory().setIsAvailable(false);
                updateBricksFromHost();
            } else {
                // Hide the brick list and show the text box for entering brick dir
                getBricksFromServer().setIsAvailable(false);
                getBrickDirectory().setIsAvailable(true);
            }
        });

        getServers().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            if (getShowBricksList().getEntity()) {
                updateBricksFromHost();
            }
        });

    }

    private void updateBricksFromHost() {
        VDS selectedServer = getServers().getSelectedItem();
        if (selectedServer != null) {
            AsyncDataProvider.getInstance().getUnusedBricksFromServer(new AsyncQuery<>(bricks -> {
                List<String> lvNames = new ArrayList<>();
                for (StorageDevice brick : bricks) {
                    String mountPoint = brick.getMountPoint();
                    if (mountPoint != null && !mountPoint.isEmpty()) {
                        // Gluster requires a directory under the mount point, not the mount point itself as
                        // a brick directory. So adding a directory with name of the brick under the mount
                        // point.
                        lvNames.add(mountPoint + mountPoint.substring(mountPoint.lastIndexOf("/"))); //$NON-NLS-1$
                    }
                }
                getBricksFromServer().setItems(lvNames);
            }), selectedServer.getId());
        }

    }

    public void setIsBrickProvisioningSupported(boolean isBrickProvisioningSupported) {
        getShowBricksList().setIsAvailable(isBrickProvisioningSupported);
        getShowBricksList().setEntity(isBrickProvisioningSupported);
    }

    public ListModel<VDS> getServers() {
        return servers;
    }

    public void setServers(ListModel<VDS> servers) {
        this.servers = servers;
    }

    public EntityModel<String> getBrickDirectory() {
        return brickDirectory;
    }

    public void setBrickDirectory(EntityModel<String> brickDirectory) {
        this.brickDirectory = brickDirectory;
    }

    public ListModel<String> getBricksFromServer() {
        return bricksFromServer;
    }

    public void setBricksFromServer(ListModel<String> bricksFromServer) {
        this.bricksFromServer = bricksFromServer;
    }

    public EntityModel<Boolean> getShowBricksList() {
        return showBricksList;
    }

    public void setShowBricksList(EntityModel<Boolean> showBricksList) {
        this.showBricksList = showBricksList;
    }

    public boolean validate() {

        if (getShowBricksList().getEntity()) {
            getBrickDirectory().setEntity(getBricksFromServer().getSelectedItem());
        }
        getBrickDirectory().validateEntity(new IValidation[] { new NotEmptyValidation() });

        return getServers().getIsValid() && getBrickDirectory().getIsValid();
    }

}

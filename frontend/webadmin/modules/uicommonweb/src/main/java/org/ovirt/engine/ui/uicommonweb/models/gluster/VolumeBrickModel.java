package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VolumeBrickModel extends Model {

    EntityModel<GlusterVolumeType> volumeType;

    EntityModel<Integer> replicaCount;
    EntityModel<Integer> stripeCount;

    ListModel<VDS> servers;
    EntityModel<String> brickDirectory;
    ListModel<String> bricksFromServer;
    EntityModel<Boolean> showBricksList;

    ListModel<EntityModel<GlusterBrickEntity>> bricks;

    EntityModel<Boolean> force;

    private UICommand addBrickCommand;
    private UICommand removeBricksCommand;
    private UICommand removeAllBricksCommand;

    private UICommand moveBricksUpCommand;
    private UICommand moveBricksDownCommand;

    public VolumeBrickModel() {
        setVolumeType(new EntityModel<GlusterVolumeType>());

        setReplicaCount(new EntityModel<Integer>());
        getReplicaCount().setEntity(VolumeListModel.REPLICATE_COUNT_DEFAULT);
        getReplicaCount().setIsChangeable(false);

        setStripeCount(new EntityModel<Integer>());
        getStripeCount().setEntity(VolumeListModel.STRIPE_COUNT_DEFAULT);
        getStripeCount().setIsChangeable(false);

        setServers(new ListModel<VDS>());
        setBrickDirectory(new EntityModel<String>());
        setBricksFromServer(new ListModel<String>());
        setShowBricksList(new EntityModel<Boolean>());

        setBricks(new ListModel<EntityModel<GlusterBrickEntity>>());

        setForce(new EntityModel<Boolean>());
        getForce().setEntity(false);
        setAddBrickCommand(new UICommand("AddBrick", this)); //$NON-NLS-1$
        setRemoveBricksCommand(new UICommand("RemoveBricks", this)); //$NON-NLS-1$
        setRemoveAllBricksCommand(new UICommand("RemoveAllBricks", this)); //$NON-NLS-1$
        getAddBrickCommand().setTitle(ConstantsManager.getInstance().getConstants().addBricksButtonLabel());
        getRemoveBricksCommand().setTitle(ConstantsManager.getInstance().getConstants().removeBricksButtonLabel());
        getRemoveAllBricksCommand().setTitle(ConstantsManager.getInstance().getConstants().removeAllBricksButtonLabel());
        getRemoveBricksCommand().setIsExecutionAllowed(false);
        getRemoveAllBricksCommand().setIsExecutionAllowed(false);

        setMoveBricksUpCommand(new UICommand("MoveBricksUp", this)); //$NON-NLS-1$
        setMoveBricksDownCommand(new UICommand("MoveBricksDown", this)); //$NON-NLS-1$
        getMoveBricksUpCommand().setTitle(ConstantsManager.getInstance().getConstants().moveBricksUpButtonLabel());
        getMoveBricksDownCommand().setTitle(ConstantsManager.getInstance().getConstants().moveBricksDownButtonLabel());
        getMoveBricksUpCommand().setIsExecutionAllowed(false);
        getMoveBricksDownCommand().setIsExecutionAllowed(false);

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
        getBricks().getSelectedItemsChangedEvent().addListener((ev, sender, args) -> updateSelectedBricksActions());


        getBricks().getItemsChangedEvent().addListener((ev, sender, args) -> {
            if (bricks.getItems() != null && bricks.getItems().iterator().hasNext()) {
                getRemoveAllBricksCommand().setIsExecutionAllowed(true);
            } else {
                getRemoveAllBricksCommand().setIsExecutionAllowed(false);
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
                List<String> brickDirectories = new ArrayList<>();
                for (StorageDevice brick : bricks) {
                    String mountPoint = brick.getMountPoint();
                    if (mountPoint != null && !mountPoint.isEmpty()) {
                        // Gluster requires a directory under the mount point, not the mount point itself as a brick
                        // directory. So adding a directory with name of the brick under the mount point.
                        brickDirectories.add(mountPoint + mountPoint.substring(mountPoint.lastIndexOf("/"))); //$NON-NLS-1$
                    }
                }
                getBricksFromServer().setItems(brickDirectories);
            }), selectedServer.getId());
        }

    }

    public void setIsBrickProvisioningSupported() {
        getShowBricksList().setIsAvailable(true);
        getShowBricksList().setEntity(true);
    }

    private void updateSelectedBricksActions() {
        if (bricks.getSelectedItems() == null || bricks.getSelectedItems().size() == 0) {
            getRemoveBricksCommand().setIsExecutionAllowed(false);
        } else {
            getRemoveBricksCommand().setIsExecutionAllowed(true);
        }

        if (bricks.getItems() == null || bricks.getSelectedItems() == null
                || bricks.getSelectedItems().size() != 1) {
            getMoveBricksUpCommand().setIsExecutionAllowed(false);
            getMoveBricksDownCommand().setIsExecutionAllowed(false);
        } else {
            EntityModel<GlusterBrickEntity> selectedItem = bricks.getSelectedItems().get(0);
            List<EntityModel<GlusterBrickEntity>> items = (List<EntityModel<GlusterBrickEntity>>) bricks.getItems();
            int position = items.indexOf(selectedItem);
            if (position == 0) {
                getMoveBricksUpCommand().setIsExecutionAllowed(false);
            } else {
                getMoveBricksUpCommand().setIsExecutionAllowed(true);
            }

            if (position == (items.size() - 1)) {
                getMoveBricksDownCommand().setIsExecutionAllowed(false);
            } else {
                getMoveBricksDownCommand().setIsExecutionAllowed(true);
            }
        }
    }

    public UICommand getAddBrickCommand() {
        return addBrickCommand;
    }

    private void setAddBrickCommand(UICommand value) {
        addBrickCommand = value;
    }

    public UICommand getRemoveBricksCommand() {
        return removeBricksCommand;
    }

    private void setRemoveBricksCommand(UICommand value) {
        removeBricksCommand = value;
    }

    public UICommand getRemoveAllBricksCommand() {
        return removeAllBricksCommand;
    }

    private void setRemoveAllBricksCommand(UICommand value) {
        removeAllBricksCommand = value;
    }

    public UICommand getMoveBricksUpCommand() {
        return moveBricksUpCommand;
    }

    private void setMoveBricksUpCommand(UICommand value) {
        moveBricksUpCommand = value;
    }

    public UICommand getMoveBricksDownCommand() {
        return moveBricksDownCommand;
    }

    private void setMoveBricksDownCommand(UICommand value) {
        moveBricksDownCommand = value;
    }

    public EntityModel<GlusterVolumeType> getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(EntityModel<GlusterVolumeType> volumeType) {
        this.volumeType = volumeType;
    }

    public EntityModel<Integer> getReplicaCount() {
        return replicaCount;
    }

    public Integer getReplicaCountValue() {
        return replicaCount.getEntity();
    }

    public void setReplicaCount(EntityModel<Integer> replicaCount) {
        this.replicaCount = replicaCount;
    }

    public EntityModel<Integer> getStripeCount() {
        return stripeCount;
    }

    public Integer getStripeCountValue() {
        return stripeCount.getEntity();
    }

    public void setStripeCount(EntityModel<Integer> stripeCount) {
        this.stripeCount = stripeCount;
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

    public ListModel<EntityModel<GlusterBrickEntity>> getBricks() {
        return bricks;
    }

    public void setBricks(ListModel<EntityModel<GlusterBrickEntity>> selectedBricks) {
        this.bricks = selectedBricks;
    }

    public EntityModel<Boolean> getForce() {
        return force;
    }

    public void setForce(EntityModel<Boolean> force) {
        this.force = force;
    }

    public boolean validateAddBricks(GlusterVolumeType selectedVolumeType) {
        boolean valid = true;
        valid = getBricks().getItems() != null && getBricks().getItems().iterator().hasNext();
        return valid;
    }

    private void addBrick() {
        VDS server = servers.getSelectedItem();
        String brickDir = null;
        if (getShowBricksList().getEntity()) {
            brickDir = bricksFromServer.getSelectedItem();
        } else {
            brickDir = getBrickDirectory().getEntity();
        }

        if (server == null) {
            setMessage(ConstantsManager.getInstance().getConstants().emptyServerBrickMsg());
            return;
        }

        if (brickDir == null || brickDir.trim().length() == 0) {
            setMessage(ConstantsManager.getInstance().getConstants().emptyBrickDirectoryMsg());
            return;
        }

        brickDir = brickDir.trim();
        if (!validateBrickDirectory(brickDir)) {
            return;
        }

        GlusterBrickEntity brickEntity = new GlusterBrickEntity();
        brickEntity.setServerId(server.getId());
        brickEntity.setServerName(server.getHostName());
        brickEntity.setBrickDirectory(brickDir);

        EntityModel<GlusterBrickEntity> entityModel = new EntityModel<>(brickEntity);
        List<EntityModel<GlusterBrickEntity>> items = (List<EntityModel<GlusterBrickEntity>>) bricks.getItems();
        if (items == null) {
            items = new ArrayList<>();
        }

        for (EntityModel<GlusterBrickEntity> model : items) {
            GlusterBrickEntity existingBrick = model.getEntity();

            if (existingBrick.getServerId().equals(brickEntity.getServerId())
                    && existingBrick.getBrickDirectory().equals(brickEntity.getBrickDirectory())) {
                setMessage(ConstantsManager.getInstance().getConstants().duplicateBrickMsg());
                return;
            }
        }

        items.add(entityModel);

        bricks.setItems(null);
        bricks.setItems(items);

        clearBrickDetails();
    }

    private boolean validateBrickDirectory(String brickDir) {

        if (brickDir.length() < 2) {
            setMessage(ConstantsManager.getInstance().getConstants().invalidBrickDirectoryAtleastTwoCharacterseMsg());
            return false;
        }

        if(!brickDir.startsWith("/")) { //$NON-NLS-1$
            setMessage(ConstantsManager.getInstance().getConstants().invalidBrickDirectoryStartWithSlashMsg());
            return false;
        }

        if (brickDir.contains(" ")) { //$NON-NLS-1$
            setMessage(ConstantsManager.getInstance().getConstants().invalidBrickDirectoryContainsSpaceMsg());
            return false;
        }

        if (brickDir.charAt(1) == '/'
                || (brickDir.charAt(1) == '.' && brickDir.length() == 2)
                || brickDir.charAt(1) == '*'
                || (brickDir.length() == 3 && brickDir.charAt(1) == '.' && brickDir.charAt(2) == '.')) {
            setMessage(ConstantsManager.getInstance().getConstants().invalidBrickDirectoryMsg());
            return false;
        }

        return true;
    }

    private void clearBrickDetails() {
        getBrickDirectory().setEntity(null);
        setMessage(null);
    }

    private void removeBricks() {
        List<EntityModel<GlusterBrickEntity>> items = (List<EntityModel<GlusterBrickEntity>>) bricks.getItems();
        List<EntityModel<GlusterBrickEntity>> selectedItems = bricks.getSelectedItems();
        if (items == null || selectedItems == null) {
            return;
        }

        items.removeAll(selectedItems);
        bricks.setItems(null);
        bricks.setItems(items);
    }

    private void removeAllBricks() {
        List<EntityModel<GlusterBrickEntity>> items = (List<EntityModel<GlusterBrickEntity>>) bricks.getItems();
        if (items == null) {
            return;
        }

        items.clear();
        bricks.setItems(null);
        bricks.setItems(items);
    }

    @SuppressWarnings("unchecked")
    private void moveItemsUpDown(boolean isUp) {

        List<EntityModel<GlusterBrickEntity>> selectedItems = bricks.getSelectedItems();
        ArrayList<EntityModel<GlusterBrickEntity>> items = new ArrayList<>(bricks.getItems());
        for (EntityModel<GlusterBrickEntity> selectedItem : selectedItems) {
            int position = items.indexOf(selectedItem);

            if (position == -1) {
                continue;
            }

            if (isUp) {
                if (position > 0) {
                    items.remove(position);
                    items.add(position - 1, selectedItem);
                }
            } else {
                if (position < items.size() - 1) {
                    items.remove(position);
                    items.add(position + 1, selectedItem);
                }
            }
        }
        bricks.setItems(items);
        bricks.setSelectedItems(selectedItems);
    }

    public boolean validateBrickCount(GlusterVolumeType selectedVolumeType, boolean isCreateVolume) {

        int brickCount = 0;

        if (bricks.getItems() != null) {
            brickCount = getBricks().getItems().size();
        }

        int replicaCount = getReplicaCountValue();
        int stripeCount = getStripeCountValue();

        return validateBrickCount(selectedVolumeType, brickCount, replicaCount, stripeCount, isCreateVolume);

    }

    public static boolean validateBrickCount(GlusterVolumeType selectedVolumeType,
            ListModel bricks,
            int replicaCount,
            int stripeCount, boolean isCreateVolume) {
        int brickCount = 0;

        if (bricks.getItems() != null) {
            brickCount = ((List<?>) bricks.getItems()).size();
        }
        return validateBrickCount(selectedVolumeType, brickCount, replicaCount, stripeCount, isCreateVolume);
    }

    public static boolean validateBrickCount(GlusterVolumeType selectedVolumeType,
            int brickCount,
            int replicaCount,
            int stripeCount, boolean isCreateVolume) {

        if (brickCount < 1) {
            return false;
        }

        boolean valid = true;

        // At the time extending a volume, stripe volume can be converted to a distributed stripe volume
        // and a replicate volume can be converted to a distributed replicate volume
        // so the validation will be performed for the corresponding distributed types
        if (!isCreateVolume) {
            if (selectedVolumeType == GlusterVolumeType.REPLICATE) {
                selectedVolumeType = GlusterVolumeType.DISTRIBUTED_REPLICATE;
            } else if (selectedVolumeType == GlusterVolumeType.STRIPE) {
                selectedVolumeType = GlusterVolumeType.DISTRIBUTED_STRIPE;
            }
        }

        switch (selectedVolumeType) {

        case REPLICATE:
            if (brickCount != replicaCount) {
                valid = false;
            }
            break;

        case STRIPE:
            if (brickCount != stripeCount) {
                valid = false;
            }
            break;

        case DISTRIBUTED_REPLICATE:
            if ((brickCount % replicaCount) != 0) {
                valid = false;
            }
            break;

        case DISTRIBUTED_STRIPE:
            if ((brickCount % stripeCount) != 0) {
                valid = false;
            }
            break;

        case STRIPED_REPLICATE:
            if (brickCount != stripeCount * replicaCount) {
                valid = false;
            }
            break;

        case DISTRIBUTED_STRIPED_REPLICATE:
            if (brickCount <= stripeCount * replicaCount || (brickCount % (stripeCount * replicaCount)) != 0) {
                valid = false;
            }
            break;
        }

        return valid;
    }

    public static String getValidationFailedMsg(GlusterVolumeType selectedVolumeType, boolean isCreateVolume) {
        String validationMsg = null;

        if (!isCreateVolume) {
            if (selectedVolumeType == GlusterVolumeType.REPLICATE) {
                selectedVolumeType = GlusterVolumeType.DISTRIBUTED_REPLICATE;
            } else if (selectedVolumeType == GlusterVolumeType.STRIPE) {
                selectedVolumeType = GlusterVolumeType.DISTRIBUTED_STRIPE;
            }
        }

        switch (selectedVolumeType) {
        case DISTRIBUTE:
            validationMsg = ConstantsManager.getInstance().getConstants().distriputedVolumeAddBricksMsg();
            break;

        case REPLICATE:
            validationMsg = ConstantsManager.getInstance().getConstants().replicateVolumeAddBricksMsg();
            break;

        case DISTRIBUTED_REPLICATE:
            validationMsg = ConstantsManager.getInstance().getConstants().distriputedReplicateVolumeAddBricksMsg();
            break;

        case STRIPE:
            validationMsg = ConstantsManager.getInstance().getConstants().stripeVolumeAddBricksMsg();
            break;

        case DISTRIBUTED_STRIPE:
            validationMsg = ConstantsManager.getInstance().getConstants().distriputedStripeVolumeAddBricksMsg();
            break;

        case STRIPED_REPLICATE:
            validationMsg = ConstantsManager.getInstance().getConstants().stripedReplicateVolumeAddBricksMsg();
            break;

        case DISTRIBUTED_STRIPED_REPLICATE:
            validationMsg = ConstantsManager.getInstance().getConstants().distriputedStripedReplicateVolumeAddBricksMsg();
            break;

        }

        return validationMsg;
    }

    public boolean validate() {
        getReplicaCount().setIsValid(true);
        getStripeCount().setIsValid(true);

        if (getReplicaCount().getIsAvailable()) {
            IntegerValidation replicaCountValidation = new IntegerValidation();
            replicaCountValidation.setMinimum(2);
            replicaCountValidation.setMaximum(16);
            getReplicaCount().validateEntity(new IValidation[] { new NotEmptyValidation(), replicaCountValidation });
        }

        if (getStripeCount().getIsAvailable()) {
            IntegerValidation stripeCountValidation = new IntegerValidation();
            stripeCountValidation.setMinimum(4);
            stripeCountValidation.setMaximum(16);
            getStripeCount().validateEntity(new IValidation[] { new NotEmptyValidation(), stripeCountValidation });
        }

        return getReplicaCount().getIsValid() && getStripeCount().getIsValid();
    }

    public boolean validateReplicateBricks() {
        return validateReplicateBricks(getReplicaCountValue(), null);
    }

    public boolean validateReplicateBricks(int oldReplicaCount, List<GlusterBrickEntity> existingBricks) {

        int replicaCount = getReplicaCountValue();
        Set<String> servers = new HashSet<>();

        if(replicaCount > oldReplicaCount) {
            int count = 0;
            for (GlusterBrickEntity brick : existingBricks) {
                servers.add(brick.getServerName());
                count++;
            }

            for (Object model : bricks.getItems()) {
                if (count > replicaCount) {
                    break;
                }
                GlusterBrickEntity brick = (GlusterBrickEntity) ((EntityModel) model).getEntity();
                if (servers.contains(brick.getServerName())) {
                    return false;
                } else {
                    servers.add(brick.getServerName());
                }
                count++;
            }
        } else {

            int count = 0;
            for (Object model : bricks.getItems()) {
                count++;
                GlusterBrickEntity brick = (GlusterBrickEntity) ((EntityModel) model).getEntity();
                if (servers.contains(brick.getServerName())) {
                    return false;
                } else {
                    servers.add(brick.getServerName());
                }
                if (count % replicaCount == 0) {
                    servers.clear();
                }
            }
        }

        return true;
    }

    public void setHostList(List<VDS> hosts) {
        Collections.sort(hosts, Comparator.comparing(VDS::getHostName, new LexoNumericComparator()));
        getServers().setItems(hosts);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getAddBrickCommand()) {
            addBrick();
        } else if (command == getRemoveBricksCommand()) {
            removeBricks();
        } else if (command == getRemoveAllBricksCommand()) {
            removeAllBricks();
        } else if (command == getMoveBricksUpCommand()) {
            moveItemsUpDown(true);
        } else if (command == getMoveBricksDownCommand()) {
            moveItemsUpDown(false);
        }
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



}

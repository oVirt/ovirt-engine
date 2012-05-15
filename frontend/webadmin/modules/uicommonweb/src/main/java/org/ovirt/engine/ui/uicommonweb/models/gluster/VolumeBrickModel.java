package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VolumeBrickModel extends Model {

    EntityModel volumeType;

    EntityModel replicaCount;
    EntityModel stripeCount;

    ListModel servers;
    EntityModel brickDirectory;

    ListModel bricks;

    private UICommand addBrickCommand;
    private UICommand clearBrickDetailsCommand;
    private UICommand removeBricksCommand;
    private UICommand removeAllBricksCommand;

    private UICommand moveBricksUpCommand;
    private UICommand moveBricksDownCommand;

    public VolumeBrickModel()
    {
        setVolumeType(new EntityModel());

        setReplicaCount(new EntityModel());
        getReplicaCount().setEntity(VolumeListModel.REPLICATE_COUNT_DEFAULT);
        getReplicaCount().setIsChangable(false);

        setStripeCount(new EntityModel());
        getStripeCount().setEntity(VolumeListModel.STRIPE_COUNT_DEFAULT);
        getStripeCount().setIsChangable(false);

        setServers(new ListModel());
        setBrickDirectory(new EntityModel());

        setBricks(new ListModel());

        setAddBrickCommand(new UICommand("AddBrick", this)); //$NON-NLS-1$
        setClearBrickDetailsCommand(new UICommand("ClearBrickDetails", this)); //$NON-NLS-1$
        setRemoveBricksCommand(new UICommand("RemoveBricks", this)); //$NON-NLS-1$
        setRemoveAllBricksCommand(new UICommand("RemoveAllBricks", this)); //$NON-NLS-1$
        // getAddBrickCommand().setIsExecutionAllowed(false);
        // getClearBrickDetailsCommand().setIsExecutionAllowed(false);
        getRemoveBricksCommand().setIsExecutionAllowed(false);
        getRemoveAllBricksCommand().setIsExecutionAllowed(false);

        setMoveBricksUpCommand(new UICommand("MoveBricksUp", this)); //$NON-NLS-1$
        setMoveBricksDownCommand(new UICommand("MoveBricksDown", this)); //$NON-NLS-1$
        getMoveBricksUpCommand().setIsExecutionAllowed(false);
        getMoveBricksDownCommand().setIsExecutionAllowed(false);

        getBricks().getSelectedItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateSelectedBricksActions();
            }
        });


        getBricks().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (bricks.getItems() != null && bricks.getItems().iterator().hasNext())
                {
                    getRemoveAllBricksCommand().setIsExecutionAllowed(true);
                }
                else
                {
                    getRemoveAllBricksCommand().setIsExecutionAllowed(false);
                }
            }
        });
    }

    private void updateSelectedBricksActions()
    {
        if (bricks.getSelectedItems() == null || bricks.getSelectedItems().size() == 0)
        {
            getRemoveBricksCommand().setIsExecutionAllowed(false);
        }
        else
        {
            getRemoveBricksCommand().setIsExecutionAllowed(true);
        }

        if (bricks.getItems() == null || bricks.getSelectedItems() == null
                || bricks.getSelectedItems().size() != 1)
        {
            getMoveBricksUpCommand().setIsExecutionAllowed(false);
            getMoveBricksDownCommand().setIsExecutionAllowed(false);
        }
        else
        {
            EntityModel selectedItem = (EntityModel) bricks.getSelectedItems().get(0);
            List<EntityModel> items = (List<EntityModel>) bricks.getItems();
            int position = items.indexOf(selectedItem);
            if (position == 0)
            {
                getMoveBricksUpCommand().setIsExecutionAllowed(false);
            }
            else
            {
                getMoveBricksUpCommand().setIsExecutionAllowed(true);
            }

            if (position == (items.size() - 1))
            {
                getMoveBricksDownCommand().setIsExecutionAllowed(false);
            }
            else
            {
                getMoveBricksDownCommand().setIsExecutionAllowed(true);
            }
        }
    }

    public UICommand getAddBrickCommand()
    {
        return addBrickCommand;
    }

    private void setAddBrickCommand(UICommand value)
    {
        addBrickCommand = value;
    }

    public UICommand getRemoveBricksCommand()
    {
        return removeBricksCommand;
    }

    private void setRemoveBricksCommand(UICommand value)
    {
        removeBricksCommand = value;
    }

    public UICommand getClearBrickDetailsCommand()
    {
        return clearBrickDetailsCommand;
    }

    private void setClearBrickDetailsCommand(UICommand value)
    {
        clearBrickDetailsCommand = value;
    }

    public UICommand getRemoveAllBricksCommand()
    {
        return removeAllBricksCommand;
    }

    private void setRemoveAllBricksCommand(UICommand value)
    {
        removeAllBricksCommand = value;
    }

    public UICommand getMoveBricksUpCommand()
    {
        return moveBricksUpCommand;
    }

    private void setMoveBricksUpCommand(UICommand value)
    {
        moveBricksUpCommand = value;
    }

    public UICommand getMoveBricksDownCommand()
    {
        return moveBricksDownCommand;
    }

    private void setMoveBricksDownCommand(UICommand value)
    {
        moveBricksDownCommand = value;
    }

    public EntityModel getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(EntityModel volumeType) {
        this.volumeType = volumeType;
    }

    public EntityModel getReplicaCount() {
        return replicaCount;
    }

    public Integer getReplicaCountValue() {
        if (replicaCount.getEntity() instanceof String)
        {
            return Integer.parseInt((String) replicaCount.getEntity());
        }
        else
        {
            return (Integer) replicaCount.getEntity();
        }
    }

    public void setReplicaCount(EntityModel replicaCount) {
        this.replicaCount = replicaCount;
    }

    public EntityModel getStripeCount() {
        return stripeCount;
    }

    public Integer getStripeCountValue() {
        if (stripeCount.getEntity() instanceof String)
        {
            return Integer.parseInt((String) stripeCount.getEntity());
        }
        else
        {
            return (Integer) stripeCount.getEntity();
        }
    }

    public void setStripeCount(EntityModel stripeCount) {
        this.stripeCount = stripeCount;
    }

    public ListModel getServers() {
        return servers;
    }

    public void setServers(ListModel servers) {
        this.servers = servers;
    }

    public EntityModel getBrickDirectory() {
        return brickDirectory;
    }

    public void setBrickDirectory(EntityModel brickDirectory) {
        this.brickDirectory = brickDirectory;
    }

    public ListModel getBricks() {
        return bricks;
    }

    public void setBricks(ListModel selectedBricks) {
        this.bricks = selectedBricks;
    }

    public boolean validateAddBricks(GlusterVolumeType selectedVolumeType)
    {
        boolean valid = true;
        valid = getBricks().getItems() != null && getBricks().getItems().iterator().hasNext();
        return valid;
    }

    private void addBrick()
    {
        VDS server = (VDS) servers.getSelectedItem();

        if(server == null || brickDirectory.getEntity() == null || ((String)brickDirectory.getEntity()).trim().length() == 0)
        {
            return;
        }

        GlusterBrickEntity brickEntity = new GlusterBrickEntity();
        brickEntity.setServerId(server.getId());
        brickEntity.setServerName(server.getvds_name());
        brickEntity.setBrickDirectory((String) brickDirectory.getEntity());

        EntityModel entityModel = new EntityModel(brickEntity);
        List<EntityModel> items = (List<EntityModel>) bricks.getItems();
        if (items == null)
        {
            items = new ArrayList<EntityModel>();
        }

        for (EntityModel model : items)
        {
            GlusterBrickEntity existingBrick = (GlusterBrickEntity) model.getEntity();

            if (existingBrick.getServerId().equals(brickEntity.getServerId())
                    && existingBrick.getBrickDirectory().equals(brickEntity.getBrickDirectory()))
            {
                setMessage(ConstantsManager.getInstance().getConstants().duplicateBrickMsg());
                return;
            }
        }

        items.add(entityModel);

        bricks.setItems(null);
        bricks.setItems(items);

        clearBrickDetails();
    }

    private void clearBrickDetails()
    {
        getBrickDirectory().setEntity(null);
        setMessage(null);
    }

    private void removeBricks()
    {
        List<EntityModel> items = (List<EntityModel>) bricks.getItems();
        List<EntityModel> selectedItems = bricks.getSelectedItems();
        if (items == null || selectedItems == null)
        {
            return;
        }

        items.removeAll(selectedItems);
        bricks.setItems(null);
        bricks.setItems(items);
    }

    private void removeAllBricks()
    {
        List<EntityModel> items = (List<EntityModel>) bricks.getItems();
        if (items == null)
        {
            return;
        }

        items.clear();
        bricks.setItems(null);
        bricks.setItems(items);
    }

    @SuppressWarnings("unchecked")
    private void moveItemsUpDown(boolean isUp)
    {

        List<EntityModel> selectedItems = bricks.getSelectedItems();
        ArrayList<EntityModel> items = new ArrayList<EntityModel>((List<EntityModel>) bricks.getItems());
        for (EntityModel selectedItem : selectedItems)
        {
            int position = items.indexOf(selectedItem);

            if (position == -1)
            {
                continue;
            }

            if (isUp)
            {
                if (position > 0)
                {
                    items.remove(position);
                    items.add(position - 1, selectedItem);
                }
            }
            else
            {
                if (position < items.size() - 1)
                {
                    items.remove(position);
                    items.add(position + 1, selectedItem);
                }
            }
        }
        bricks.setItems(items);
        bricks.setSelectedItems(selectedItems);
    }

    public boolean validateBrickCount(GlusterVolumeType selectedVolumeType) {

        int brickCount = 0;

        if (bricks.getItems() != null)
        {
            brickCount = ((List<?>) getBricks().getItems()).size();
        }

        int replicaCount = getReplicaCountValue();
        int stripeCount = getStripeCountValue();

        return validateBrickCount(selectedVolumeType, brickCount, replicaCount, stripeCount);

    }

    public static boolean validateBrickCount(GlusterVolumeType selectedVolumeType,
            ListModel bricks,
            int replicaCount,
            int stripeCount) {
        int brickCount = 0;

        if (bricks.getItems() != null)
        {
            brickCount = ((List<?>) bricks.getItems()).size();
        }
        return validateBrickCount(selectedVolumeType, brickCount, replicaCount, stripeCount);
    }

    public static boolean validateBrickCount(GlusterVolumeType selectedVolumeType,
            int brickCount,
            int replicaCount,
            int stripeCount) {

        if (brickCount < 1)
        {
            return false;
        }

        boolean valid = true;

        switch (selectedVolumeType)
        {

        case DISTRIBUTE:
            if (brickCount < 1)
            {
                valid = false;
            }
            break;

        case REPLICATE:
            if (brickCount != replicaCount)
            {
                valid = false;
            }
            break;

        case STRIPE:
            if (brickCount != stripeCount)
            {
                valid = false;
            }
            break;

        case DISTRIBUTED_REPLICATE:
            if ((brickCount % replicaCount) != 0)
            {
                valid = false;
            }
            break;

        case DISTRIBUTED_STRIPE:
            if ((brickCount % stripeCount) != 0)
            {
                valid = false;
            }
            break;
        }

        return valid;
    }

    public static String getValidationFailedMsg(GlusterVolumeType selectedVolumeType)
    {
        String validationMsg = null;

        switch (selectedVolumeType)
        {
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
        }

        return validationMsg;
    }

    public boolean validate()
    {
        getReplicaCount().setIsValid(true);
        getStripeCount().setIsValid(true);

        if (getReplicaCount().getIsAvailable())
        {
            IntegerValidation replicaCountValidation = new IntegerValidation();
            replicaCountValidation.setMinimum(2);
            replicaCountValidation.setMaximum(16);
            getReplicaCount().ValidateEntity(new IValidation[] { new NotEmptyValidation(), replicaCountValidation });
        }

        if (getStripeCount().getIsAvailable())
        {
            IntegerValidation stripeCountValidation = new IntegerValidation();
            stripeCountValidation.setMinimum(4);
            stripeCountValidation.setMaximum(16);
            getReplicaCount().ValidateEntity(new IValidation[] { new NotEmptyValidation(), stripeCountValidation });
        }

        return getReplicaCount().getIsValid() && getStripeCount().getIsValid();
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);

        if (command == getAddBrickCommand())
        {
            addBrick();
        }
        else if (command == getClearBrickDetailsCommand())
        {
            clearBrickDetails();
        }
        else if (command == getRemoveBricksCommand())
        {
            removeBricks();
        }
        else if (command == getRemoveAllBricksCommand())
        {
            removeAllBricks();
        }
        else if (command == getMoveBricksUpCommand())
        {
            moveItemsUpDown(true);
        }
        else if (command == getMoveBricksDownCommand())
        {
            moveItemsUpDown(false);
        }
    }

}

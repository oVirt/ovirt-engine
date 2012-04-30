package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;

public class VolumeBrickModel extends Model {

    EntityModel replicaCount;
    EntityModel stripeCount;
    ListModel availableBricks;
    ListModel selectedBricks;

    private UICommand addBricksCommand;
    private UICommand removeBricksCommand;
    private UICommand addAllBricksCommand;
    private UICommand removeAllBricksCommand;

    private UICommand moveBricksUpCommand;
    private UICommand moveBricksDownCommand;

    public VolumeBrickModel()
    {
        setReplicaCount(new EntityModel());
        getReplicaCount().setEntity(VolumeListModel.REPLICATE_COUNT_DEFAULT);
        getReplicaCount().setIsChangable(false);

        setStripeCount(new EntityModel());
        getStripeCount().setEntity(VolumeListModel.STRIPE_COUNT_DEFAULT);
        getStripeCount().setIsChangable(false);

        setAvailableBricks(new ListModel());
        setSelectedBricks(new ListModel());

        setAddBricksCommand(new UICommand("AddBricks", this)); //$NON-NLS-1$
        setRemoveBricksCommand(new UICommand("RemoveBricks", this)); //$NON-NLS-1$
        setAddAllBricksCommand(new UICommand("AddAllBricks", this)); //$NON-NLS-1$
        setRemoveAllBricksCommand(new UICommand("RemoveAllBricks", this)); //$NON-NLS-1$
        getAddBricksCommand().setIsExecutionAllowed(false);
        getRemoveBricksCommand().setIsExecutionAllowed(false);
        getAddAllBricksCommand().setIsExecutionAllowed(false);
        getRemoveAllBricksCommand().setIsExecutionAllowed(false);

        setMoveBricksUpCommand(new UICommand("MoveBricksUp", this)); //$NON-NLS-1$
        setMoveBricksDownCommand(new UICommand("MoveBricksDown", this)); //$NON-NLS-1$
        getMoveBricksUpCommand().setIsExecutionAllowed(false);
        getMoveBricksDownCommand().setIsExecutionAllowed(false);

        getAvailableBricks().getSelectedItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

                if (availableBricks.getSelectedItems() == null || availableBricks.getSelectedItems().size() == 0)
                {
                    getAddBricksCommand().setIsExecutionAllowed(false);
                }
                else
                {
                    getAddBricksCommand().setIsExecutionAllowed(true);
                }
            }
        });

        getSelectedBricks().getSelectedItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateSelectedBricksActions();
            }
        });

        getAvailableBricks().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (availableBricks.getItems() != null && availableBricks.getItems().iterator().hasNext())
                {
                    getAddAllBricksCommand().setIsExecutionAllowed(true);
                }
                else
                {
                    getAddAllBricksCommand().setIsExecutionAllowed(false);
                }
            }
        });

        getSelectedBricks().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (selectedBricks.getItems() != null && selectedBricks.getItems().iterator().hasNext())
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
        if (selectedBricks.getSelectedItems() == null || selectedBricks.getSelectedItems().size() == 0)
        {
            getRemoveBricksCommand().setIsExecutionAllowed(false);
        }
        else
        {
            getRemoveBricksCommand().setIsExecutionAllowed(true);
        }

        if (selectedBricks.getItems() == null || selectedBricks.getSelectedItems() == null
                || selectedBricks.getSelectedItems().size() != 1)
        {
            getMoveBricksUpCommand().setIsExecutionAllowed(false);
            getMoveBricksDownCommand().setIsExecutionAllowed(false);
        }
        else
        {
            EntityModel selectedItem = (EntityModel) selectedBricks.getSelectedItems().get(0);
            List<EntityModel> items = (List<EntityModel>) selectedBricks.getItems();
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

    public UICommand getAddBricksCommand()
    {
        return addBricksCommand;
    }

    private void setAddBricksCommand(UICommand value)
    {
        addBricksCommand = value;
    }

    public UICommand getRemoveBricksCommand()
    {
        return removeBricksCommand;
    }

    private void setRemoveBricksCommand(UICommand value)
    {
        removeBricksCommand = value;
    }

    public UICommand getAddAllBricksCommand()
    {
        return addAllBricksCommand;
    }

    private void setAddAllBricksCommand(UICommand value)
    {
        addAllBricksCommand = value;
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

    public EntityModel getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(EntityModel replicaCount) {
        this.replicaCount = replicaCount;
    }

    public EntityModel getStripeCount() {
        return stripeCount;
    }

    public void setStripeCount(EntityModel stripeCount) {
        this.stripeCount = stripeCount;
    }

    public ListModel getAvailableBricks() {
        return availableBricks;
    }

    public void setAvailableBricks(ListModel availableBricks) {
        this.availableBricks = availableBricks;
    }

    public ListModel getSelectedBricks() {
        return selectedBricks;
    }

    public void setSelectedBricks(ListModel selectedBricks) {
        this.selectedBricks = selectedBricks;
    }

    public boolean validateAddBricks(GlusterVolumeType selectedVolumeType)
    {
        boolean valid = true;
        valid = getSelectedBricks().getItems() != null && getSelectedBricks().getItems().iterator().hasNext();
        return valid;
    }

    private void addBricks()
    {
        moveSelectedItems(availableBricks, selectedBricks);
    }

    private void removeBricks()
    {
        moveSelectedItems(selectedBricks, availableBricks);
    }

    private void addAllBricks()
    {
        moveAllItems(availableBricks, selectedBricks);
    }

    private void removeAllBricks()
    {
        moveAllItems(selectedBricks, availableBricks);
    }

    @SuppressWarnings("unchecked")
    private void moveSelectedItems(ListModel source, ListModel target)
    {
        if (source.getSelectedItems() == null || source.getSelectedItems().size() == 0)
        {
            return;
        }

        List<EntityModel> sourceItems = (List<EntityModel>) source.getItems();
        List<EntityModel> sourceSelectedItems = source.getSelectedItems();
        source.setItems(null);
        sourceItems.removeAll(sourceSelectedItems);
        source.setItems(sourceItems);


        List<EntityModel> targetItems = new ArrayList<EntityModel>();
        if (target.getItems() != null)
        {
            targetItems.addAll((List<EntityModel>) target.getItems());
        }
        targetItems.addAll(sourceSelectedItems);
        target.setItems(targetItems);
    }

    @SuppressWarnings("unchecked")
    private void moveAllItems(ListModel source, ListModel target)
    {
        if (source.getItems() == null)
        {
            return;
        }

        List<EntityModel> sourceItems = (List<EntityModel>) source.getItems();
        source.setItems(null);

        List<EntityModel> targetItems = new ArrayList<EntityModel>();
        if (target.getItems() != null)
        {
            targetItems.addAll((List<EntityModel>) target.getItems());
        }
        targetItems.addAll(sourceItems);
        target.setItems(targetItems);
        target.setSelectedItems(null);
    }

    @SuppressWarnings("unchecked")
    private void moveItemsUpDown(boolean isUp)
    {

        List<EntityModel> selectedItems = selectedBricks.getSelectedItems();
        ArrayList<EntityModel> items = new ArrayList<EntityModel>((List<EntityModel>) selectedBricks.getItems());
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
        selectedBricks.setItems(items);
        selectedBricks.setSelectedItems(selectedItems);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);

        if (command == getAddBricksCommand())
        {
            addBricks();
        }
        else if (command == getRemoveBricksCommand())
        {
            removeBricks();
        }
        else if (command == getAddAllBricksCommand())
        {
            addAllBricks();
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

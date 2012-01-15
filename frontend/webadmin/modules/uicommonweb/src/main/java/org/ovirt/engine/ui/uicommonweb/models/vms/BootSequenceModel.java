package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

@SuppressWarnings("unused")
public class BootSequenceModel extends ListModel
{

    private UICommand privateMoveItemUpCommand;

    public UICommand getMoveItemUpCommand()
    {
        return privateMoveItemUpCommand;
    }

    private void setMoveItemUpCommand(UICommand value)
    {
        privateMoveItemUpCommand = value;
    }

    private UICommand privateMoveItemDownCommand;

    public UICommand getMoveItemDownCommand()
    {
        return privateMoveItemDownCommand;
    }

    private void setMoveItemDownCommand(UICommand value)
    {
        privateMoveItemDownCommand = value;
    }

    @Override
    public ObservableCollection<EntityModel> getItems()
    {
        return (ObservableCollection<EntityModel>) (super.getItems());
    }

    public void setItems(ObservableCollection<EntityModel> value)
    {
        super.setItems(value);
    }

    public EntityModel getNetworkOption()
    {
        for (EntityModel a : getItems())
        {
            if ((BootSequence) a.getEntity() == BootSequence.N)
            {
                return a;
            }
        }

        throw new IndexOutOfBoundsException();
    }

    public EntityModel getCdromOption()
    {
        for (EntityModel a : getItems())
        {
            if ((BootSequence) a.getEntity() == BootSequence.D)
            {
                return a;
            }
        }

        throw new IndexOutOfBoundsException();
    }

    public BootSequence getSequence()
    {
        // string str = Items.Where(a => a.IsChangable)
        // .Select(a => (BootSequence)a.Entity)
        // .Aggregate(String.Empty, (a, b) => a += b.ToString());

        String str = "";
        for (EntityModel a : getItems())
        {
            if (a.getIsChangable())
            {
                BootSequence bs = (BootSequence) a.getEntity();
                str += bs.toString();
            }
        }

        return BootSequence.valueOf(str);
    }

    public BootSequenceModel()
    {
        setMoveItemUpCommand(new UICommand("MoveItemUp", this));
        setMoveItemDownCommand(new UICommand("MoveItemDown", this));

        InitializeItems();

        UpdateActionAvailability();
    }

    public int getSelectedItemIndex()
    {
        return getSelectedItem() != null ? getItems().indexOf(getSelectedItem()) : -1;
    }

    public void MoveItemDown()
    {
        if (getSelectedItemIndex() < getItems().size() - 1)
        {
            getItems().Move(getSelectedItemIndex(), getSelectedItemIndex() + 1);
        }
    }

    public void MoveItemUp()
    {
        if (getSelectedItemIndex() > 0)
        {
            getItems().Move(getSelectedItemIndex(), getSelectedItemIndex() - 1);
        }
    }

    private void InitializeItems()
    {
        ObservableCollection<EntityModel> items = new ObservableCollection<EntityModel>();
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle("Hard Disk");
        tempVar.setEntity(BootSequence.C);
        items.add(tempVar);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle("CD-ROM");
        tempVar2.setEntity(BootSequence.D);
        items.add(tempVar2);
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setTitle("Network (PXE)");
        tempVar3.setEntity(BootSequence.N);
        items.add(tempVar3);

        setItems(items);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getMoveItemUpCommand())
        {
            MoveItemUp();
        }
        else if (command == getMoveItemDownCommand())
        {
            MoveItemDown();
        }
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
    {
        getMoveItemUpCommand().setIsExecutionAllowed(getSelectedItem() != null);
        getMoveItemDownCommand().setIsExecutionAllowed(getSelectedItem() != null);
    }
}

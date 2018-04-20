package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.ObservableCollection;

@SuppressWarnings("unused")
public class BootSequenceModel extends ListModel<EntityModel<BootSequence>> {

    private UICommand privateMoveItemUpCommand;

    public UICommand getMoveItemUpCommand() {
        return privateMoveItemUpCommand;
    }

    private void setMoveItemUpCommand(UICommand value) {
        privateMoveItemUpCommand = value;
    }

    private UICommand privateMoveItemDownCommand;

    public UICommand getMoveItemDownCommand() {
        return privateMoveItemDownCommand;
    }

    private void setMoveItemDownCommand(UICommand value) {
        privateMoveItemDownCommand = value;
    }

    @Override
    public ObservableCollection<EntityModel<BootSequence>> getItems() {
        return (ObservableCollection<EntityModel<BootSequence>>) (super.getItems());
    }

    public void setItems(ObservableCollection<EntityModel<BootSequence>> value) {
        super.setItems(value);
    }

    public EntityModel getHardDiskOption() {
        return getBootSequenceOption(BootSequence.C);
    }

    public EntityModel getNetworkOption() {
        return getBootSequenceOption(BootSequence.N);
    }

    public EntityModel getCdromOption() {
        return getBootSequenceOption(BootSequence.D);
    }

    private EntityModel getBootSequenceOption(BootSequence bootSequenceOption) {
        for (EntityModel a : getItems()) {
            if (a.getEntity() == bootSequenceOption) {
                return a;
            }
        }

        throw new IndexOutOfBoundsException();
    }

    public BootSequence getSequence() {
        StringBuilder str = new StringBuilder();
        for (EntityModel a : getItems()) {
            if (a.getIsChangable()) {
                BootSequence bs = (BootSequence) a.getEntity();
                str.append(bs.toString());
            }
        }

        return !str.toString().equals("") ? BootSequence.valueOf(str.toString()) : null;  //$NON-NLS-1$
    }

    public BootSequenceModel() {
        setMoveItemUpCommand(new UICommand("MoveItemUp", this)); //$NON-NLS-1$
        setMoveItemDownCommand(new UICommand("MoveItemDown", this)); //$NON-NLS-1$

        initializeItems();

        updateActionAvailability();
    }

    public int getSelectedItemIndex() {
        return getSelectedItem() != null ? getItems().indexOf(getSelectedItem()) : -1;
    }

    public void moveItemDown() {
        if (getSelectedItemIndex() < getItems().size() - 1) {
            getItems().move(getSelectedItemIndex(), getSelectedItemIndex() + 1);
        }
    }

    public void moveItemUp() {
        if (getSelectedItemIndex() > 0) {
            getItems().move(getSelectedItemIndex(), getSelectedItemIndex() - 1);
        }
    }

    private void initializeItems() {
        ObservableCollection<EntityModel<BootSequence>> items = new ObservableCollection<>();
        EntityModel<BootSequence> tempVar = new EntityModel<>();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().hardDiskTitle());
        tempVar.setEntity(BootSequence.C);
        items.add(tempVar);
        EntityModel<BootSequence> tempVar2 = new EntityModel<>();
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cdromTitle());
        tempVar2.setEntity(BootSequence.D);
        items.add(tempVar2);
        EntityModel<BootSequence> tempVar3 = new EntityModel<>();
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().networkPXETitle());
        tempVar3.setEntity(BootSequence.N);
        items.add(tempVar3);

        setItems(items);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getMoveItemUpCommand()) {
            moveItemUp();
        } else if (command == getMoveItemDownCommand()) {
            moveItemDown();
        }
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        getMoveItemUpCommand().setIsExecutionAllowed(getSelectedItem() != null);
        getMoveItemDownCommand().setIsExecutionAllowed(getSelectedItem() != null);
    }
}

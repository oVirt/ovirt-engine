package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.EditVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NewVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveVnicProfileModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class NetworkProfileListModel extends SearchableListModel
{
    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    public NetworkProfileListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().profilesTitle());
        setHashName("profiles"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    public UICommand getNewCommand() {
        return newCommand;
    }

    private void setNewCommand(UICommand value) {
        newCommand = value;
    }

    public UICommand getEditCommand() {
        return editCommand;
    }

    private void setEditCommand(UICommand value) {
        editCommand = value;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    public void newProfile() {
        if (getWindow() != null) {
            return;
        }

        NewVnicProfileModel model = new NewVnicProfileModel(this, getEntity().getCompatibilityVersion());
        setWindow(model);

        initProfileNetwork(model);
    }

    public void edit() {
        if (getWindow() != null) {
            return;
        }

        EditVnicProfileModel model =
                new EditVnicProfileModel(this, getEntity().getCompatibilityVersion(), (VnicProfile) getSelectedItem());
        setWindow(model);

        initProfileNetwork(model);
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        RemoveVnicProfileModel model = new RemoveVnicProfileModel(this, getSelectedItems(), false);
        setWindow(model);
    }

    private void initProfileNetwork(VnicProfileModel model) {
        model.getNetwork().setItems(Arrays.asList(getEntity()));
        model.getNetwork().setSelectedItem(getEntity());
        model.getNetwork().setIsChangable(false);
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    public NetworkView getEntity() {
        return (NetworkView) ((super.getEntity() instanceof NetworkView) ? super.getEntity() : null);
    }

    public void setEntity(NetworkView value) {
        super.setEntity(value);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }

        updateActionAvailability();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                NetworkProfileListModel.this.setItems((List<VnicProfile>) returnValue);
            }
        };
        AsyncDataProvider.getVnicProfilesByNetworkId(asyncQuery, getEntity().getId());
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    private void updateActionAvailability() {
        NetworkView profile = getEntity();

        getEditCommand().setIsExecutionAllowed((getSelectedItems() != null && getSelectedItems().size() == 1));
        getRemoveCommand().setIsExecutionAllowed((getSelectedItems() != null && getSelectedItems().size() > 0));
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newProfile();
        }
        else if (command == getEditCommand()) {
            edit();
        }
        else if (command == getRemoveCommand()) {
            remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "NetworkProfileListModel"; //$NON-NLS-1$
    }

}

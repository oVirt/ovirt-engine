package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.EditVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NewVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveVnicProfileModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class NetworkProfileListModel extends SearchableListModel<NetworkView, VnicProfileView> {
    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    public NetworkProfileListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().vnicProfilesTitle());
        setHelpTag(HelpTag.profiles);
        setHashName("profiles"); //$NON-NLS-1$

        setAvailableInModes(ApplicationMode.VirtOnly);
        setComparator(Linq.VnicProfileViewComparator);

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

    @Override
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

        NewVnicProfileModel model = new NewVnicProfileModel(this,
                getEntity().getDataCenterId());
        setWindow(model);
        VnicProfile profile = new VnicProfile();
        profile.setNetworkId(getEntity().getId());
        model.setProfile(profile);
    }

    public void edit() {
        if (getWindow() != null) {
            return;
        }

        EditVnicProfileModel model =
                new EditVnicProfileModel(this,
                        getSelectedItem(),
                        getEntity().getDataCenterId());
        setWindow(model);
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        RemoveVnicProfileModel model = new RemoveVnicProfileModel(this, getSelectedItems(), false);
        setWindow(model);
    }

    public void cancel() {
        setWindow(null);
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

        AsyncDataProvider.getInstance().getVnicProfilesByNetworkId(new SetRawItemsAsyncQuery(), getEntity().getId());
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    private void updateActionAvailability() {
        NetworkView network = getEntity();

        getNewCommand().setIsExecutionAllowed(network != null && network.isVmNetwork());
        getEditCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
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
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "NetworkProfileListModel"; //$NON-NLS-1$
    }

}

package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.NewExternalSubnetModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveExternalSubnetModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class NetworkExternalSubnetListModel extends SearchableListModel<NetworkView, ExternalSubnet>
{

    private UICommand newCommand;
    private UICommand removeCommand;

    public NetworkExternalSubnetListModel() {
        setHelpTag(HelpTag.external_subnets);
        setHashName("external_subnets"); //$NON-NLS-1$
        setComparator(new NameableComparator());

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    public UICommand getNewCommand() {
        return newCommand;
    }

    private void setNewCommand(UICommand newCommand) {
        this.newCommand = newCommand;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    public void newSubnet() {
        if (getWindow() != null) {
            return;
        }

        NewExternalSubnetModel model = new NewExternalSubnetModel(getEntity(), this);
        setWindow(model);
    }

    @SuppressWarnings("unchecked")
    public void remove() {
        if (getWindow() != null) {
            return;
        }

        RemoveExternalSubnetModel model = new RemoveExternalSubnetModel(this, getSelectedItems());
        setWindow(model);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        super.syncSearch(VdcQueryType.GetExternalSubnetsOnProviderByNetwork, new IdQueryParameters(getEntity().getId()));
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    private void updateActionAvailability() {
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
            newSubnet();
        } else if (command == getRemoveCommand()) {
            remove();
        }
    }

    @Override
    protected String getListName() {
        return "NetworkExternalSubnetListModel"; //$NON-NLS-1$
    }

}

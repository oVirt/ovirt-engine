package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.NewExternalSubnetModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveExternalSubnetModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class NetworkExternalSubnetListModel extends SearchableListModel<NetworkView, ExternalSubnet> {

    private UICommand newCommand;
    private UICommand removeCommand;
    private boolean isExecutionAllowed;

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
            adjustActionButtonsForNetworkReadOnlyProperty();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }
        super.syncSearch(QueryType.GetExternalSubnetsOnProviderByNetwork, new IdQueryParameters(getEntity().getId()));
    }

    private void adjustActionButtonsForNetworkReadOnlyProperty(){
        NetworkView networkView = getEntity();

        if (!networkView.isExternal()) {
            setCommandExecutionAllowed(true);
            return;
        }
        Guid providerGuid = networkView.getProvidedBy().getProviderId();
        Frontend.getInstance().runQuery(QueryType.GetProviderById, new IdQueryParameters(providerGuid),
                createProviderReadOnlyCallback());
    }

    private AsyncQuery<QueryReturnValue> createProviderReadOnlyCallback(){
        return new AsyncQuery<>(returnValue -> setCommandExecutionAllowedForProvider((Provider) (returnValue.getReturnValue())));
    }

    private void setCommandExecutionAllowedForProvider(Provider provider){
        OpenstackNetworkProviderProperties properties = (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
        if (properties != null && properties.getReadOnly()){
            setCommandExecutionAllowed(false);
            return;
        }
        setCommandExecutionAllowed(true);
    }

    private void setCommandExecutionAllowed(boolean isAllowed){
        isExecutionAllowed = isAllowed;
        updateActionAvailability();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    private void updateActionAvailability() {
        newCommand.setIsExecutionAllowed(isExecutionAllowed);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && isExecutionAllowed);
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

package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.queries.GetAllProvidersParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveNetworksModel extends ConfirmationModel {

    private final SearchableListModel<?, ? extends Network> sourceListModel;

    @SuppressWarnings("unchecked")
    public RemoveNetworksModel(SearchableListModel<?, ? extends Network> sourceListModel) {
        this.sourceListModel = sourceListModel;

        setTitle(ConstantsManager.getInstance().getConstants().removeLogicalNetworkTitle());
        setHelpTag(HelpTag.remove_logical_network);
        setHashName("remove_logical_network"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        // A set of the external providers to which selected networks belong
        Set<Guid> externalProviderIds = new HashSet();
        for (Network network : (Iterable<Network>) sourceListModel.getSelectedItems()) {
            if (network instanceof NetworkView) {
                NetworkView netView = (NetworkView) network;
                if (netView.getDescription() == null
                        || netView.getDescription().trim().equals("")) { //$NON-NLS-1$
                    list.add(ConstantsManager.getInstance()
                            .getMessages()
                            .networkDc(netView.getName(), netView.getDataCenterName()));
                } else {
                    list.add(ConstantsManager.getInstance()
                            .getMessages()
                            .networkDcDescription(netView.getName(),
                                    netView.getDataCenterName(),
                                    netView.getDescription()));
                }

            } else {
                if (network.getDescription() == null || "".equals(network.getDescription().trim())) { //$NON-NLS-1$
                    list.add(network.getName());
                } else {
                    list.add(StringFormat.format("%1$s (%2$s)", network.getName(), network.getDescription())); //$NON-NLS-1$
                }
            }
            if (network.isExternal()) {
                externalProviderIds.add(network.getProvidedBy().getProviderId());
            }
        }
        setItems(list);

        adjustRemoveExternalOptions(externalProviderIds);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("onRemove", this); //$NON-NLS-1$
        getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("cancel", this); //$NON-NLS-1$
        getCommands().add(tempVar2);
    }

    /**
     * Check if the 'remove on external provider' checkbox should be visible and changeable.
     * The checkbox is visible if any of the removed networks is an external network.
     * The checkbox is visible but not changeable if any of the external networks belongs
     * to a read-only provider.
     * @param externalProviderIds Set of all external provider id's to which the removed networks belong
     */
    private void adjustRemoveExternalOptions(Set<Guid> externalProviderIds) {
        // If there are no external providers, exit without any changes to the checkbox (it will not be visible)
        if (externalProviderIds.isEmpty()) {
            return;
        }
        // We do not have details about whether the provider is read-only, so a query to the backend must be made
        Frontend.getInstance().runQuery(QueryType.GetAllProviders, new GetAllProvidersParameters(ProviderType.EXTERNAL_NETWORK, ProviderType.OPENSTACK_NETWORK),
                createProviderReadOnlyCallback(externalProviderIds));
    }

    /**
     * Create an asynchronous callback for the backend query. Upon retrieving the information the state of the
     * 'remove on external provider' will be changed (if needed)
     * @param externalProviderIds Set of all external provider id's to which the removed networks belong
     * @return callback object for backend query
     */
    private AsyncQuery<QueryReturnValue> createProviderReadOnlyCallback(final Set<Guid> externalProviderIds) {
        return new AsyncQuery<>(returnValue -> {
            List<Provider> providers = returnValue.getReturnValue();
            boolean isReadOnly = checkForRemoveExternalNetworkAvailability(providers, externalProviderIds);
            makeRemoveExternalNetworkCheckboxAvailable(isReadOnly);
        });
    }

    /**
     * Check if any of the providers in externalProviderIds is read only
     * @param providers providers retrieved from the backend
     * @param externalProviderIds Set of all external provider id's to which the removed networks belong
     * @return true if any of the providers is read only
     */

    protected boolean checkForRemoveExternalNetworkAvailability(
            List<Provider> providers, Set<Guid> externalProviderIds) {

        for (Provider provider : providers) {
            if (externalProviderIds.contains(provider.getId())) {
                OpenstackNetworkProviderProperties properties =
                        (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
                if (properties.getReadOnly()) {
                    // If any of the providers is read only, abort
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set the 'remove on external provider' checkbox visible.
     * If any of the providers is read only, make this checkbox unchangeable, and set the state to not selected.
     * The state in which the checkbox is visible but not changeable is desired (not a bug).
     * @param isReadOnly true if any of the providers is read only
     */
    private void makeRemoveExternalNetworkCheckboxAvailable(boolean isReadOnly) {
        getForce().setIsAvailable(true);
        getForce().setEntity(true);
        setForceLabel(ConstantsManager.getInstance().getConstants().removeNetworkFromProvider());
        getForce().setIsChangeable(!isReadOnly);
        getForce().setEntity(!isReadOnly);
    }

    public void onRemove() {
        ArrayList<ActionParametersBase> pb = new ArrayList<>();

        for (Object a : sourceListModel.getSelectedItems()) {
            Network network = (Network) a;
            if (network.isExternal()) {
                pb.add(new RemoveNetworkParameters(network.getId(), getForce().getEntity()));
            } else {
                pb.add(new RemoveNetworkParameters(network.getId()));
            }
        }

        Frontend.getInstance().runMultipleAction(ActionType.RemoveNetwork, pb);

        sourceListModel.setConfirmWindow(null);
    }

    private void cancel() {
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if ("onRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }
}

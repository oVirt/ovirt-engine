package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddExternalSubnetParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewExternalSubnetModel extends Model {

    private EntityModel<NetworkView> network;

    private ExternalSubnetModel subnetModel;

    private final SearchableListModel sourceModel;

    public NewExternalSubnetModel(NetworkView network, SearchableListModel sourceModel) {
        this.sourceModel = sourceModel;

        setNetwork(new EntityModel<NetworkView>());
        getNetwork().setEntity(network);
        setSubnetModel(new ExternalSubnetModel());
        getSubnetModel().setExternalNetwork(network.getProvidedBy());

        setTitle(ConstantsManager.getInstance().getConstants().newExternalSubnetTitle());
        setHelpTag(HelpTag.new_external_subnet);
        setHashName("new_external_subnet"); //$NON-NLS-1$

        initCommands();
    }

    protected void initCommands() {
        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        getCommands().add(cancelCommand);
    }

    public EntityModel<NetworkView> getNetwork() {
        return network;
    }

    private void setNetwork(EntityModel<NetworkView> network) {
        this.network = network;
    }

    public ExternalSubnetModel getSubnetModel() {
        return subnetModel;
    }

    private void setSubnetModel(ExternalSubnetModel subnetModel) {
        this.subnetModel = subnetModel;
    }

    private void onSave() {

        if (!validate()) {
            return;
        }

        // Save changes.
        flush();

        startProgress();

        ProviderNetwork providedBy = getNetwork().getEntity().getProvidedBy();

        Frontend.getInstance().runAction(ActionType.AddSubnetToProvider,
                new AddExternalSubnetParameters(getSubnetModel().getSubnet(),
                        providedBy.getProviderId(), providedBy.getExternalId()),
                result -> {
                    ActionReturnValue returnValue = result.getReturnValue();
                    stopProgress();

                    if (returnValue != null && returnValue.getSucceeded()) {
                        cancel();
                    }
                },
                this,
                true);
    }

    public void flush() {
        getSubnetModel().flush();
    }

    private void cancel() {
        sourceModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    public boolean validate() {
        return getSubnetModel().validate();
    }
}

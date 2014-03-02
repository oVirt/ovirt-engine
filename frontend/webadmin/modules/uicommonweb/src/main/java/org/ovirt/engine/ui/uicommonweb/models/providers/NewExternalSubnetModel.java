package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.action.AddExternalSubnetParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewExternalSubnetModel extends Model {

    private EntityModel<NetworkView> network;

    private ExternalSubnetModel subnetModel;

    private final SearchableListModel sourceModel;

    public NewExternalSubnetModel(NetworkView network, SearchableListModel sourceModel) {
        this.sourceModel = sourceModel;

        setNetwork(new ListModel<NetworkView>());
        getNetwork().setEntity(network);
        setSubnetModel(new ExternalSubnetModel());
        getSubnetModel().setExternalNetwork(network.getProvidedBy());

        setTitle(ConstantsManager.getInstance().getConstants().newExternalSubnetTitle());
        setHelpTag(HelpTag.new_external_subnet);
        setHashName("new_external_subnet"); //$NON-NLS-1$

        initCommands();
    }

    protected void initCommands() {
        UICommand okCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        getCommands().add(cancelCommand);
    }

    public EntityModel<NetworkView> getNetwork() {
        return network;
    }

    private void setNetwork(ListModel<NetworkView> network) {
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

        startProgress(null);

        Frontend.getInstance().runAction(VdcActionType.AddSubnetToProvider,
                new AddExternalSubnetParameters(getSubnetModel().getSubnet(),
                        getNetwork().getEntity().getId()),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        VdcReturnValueBase returnValue = result.getReturnValue();
                        stopProgress();

                        if (returnValue != null && returnValue.getSucceeded()) {
                            cancel();
                        }
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

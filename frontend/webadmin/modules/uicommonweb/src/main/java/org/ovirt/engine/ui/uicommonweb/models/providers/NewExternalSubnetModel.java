package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.action.AddExternalSubnetParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet.IpVersion;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.CidrValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewExternalSubnetModel extends Model {

    private EntityModel<String> name;
    private EntityModel<String> cidr;
    private EntityModel<NetworkView> network;
    private ListModel<IpVersion> ipVersion;
    private final SearchableListModel sourceModel;
    private ExternalSubnet subnet;

    public NewExternalSubnetModel(NetworkView network, SearchableListModel sourceModel) {
        this.sourceModel = sourceModel;

        setName(new EntityModel<String>());
        setCidr(new EntityModel<String>());
        setNetwork(new ListModel<NetworkView>());
        setIpVersion(new ListModel<IpVersion>());
        getIpVersion().setItems(AsyncDataProvider.getExternalSubnetIpVerionList());
        getNetwork().setEntity(network);

        setTitle(ConstantsManager.getInstance().getConstants().newExternalSubnetTitle());
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

    public EntityModel<String> getName() {
        return name;
    }

    private void setName(EntityModel<String> name) {
        this.name = name;
    }

    public EntityModel<String> getCidr() {
        return cidr;
    }

    private void setCidr(EntityModel<String> cidr) {
        this.cidr = cidr;
    }

    public EntityModel<NetworkView> getNetwork() {
        return network;
    }

    private void setNetwork(ListModel<NetworkView> network) {
        this.network = network;
    }

    public ListModel<IpVersion> getIpVersion() {
        return ipVersion;
    }

    private void setIpVersion(ListModel<IpVersion> ipVersion) {
        this.ipVersion = ipVersion;
    }

    private void onSave() {

        if (!validate()) {
            return;
        }

        // Save changes.
        flush();

        startProgress(null);

        Frontend.getInstance().runAction(VdcActionType.AddSubnetToProvider,
                new AddExternalSubnetParameters(subnet, getNetwork().getEntity().getId()),
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
        subnet = new ExternalSubnet();
        subnet.setName(getName().getEntity());
        Network network = getNetwork().getEntity();
        subnet.setExternalNetwork(network.getProvidedBy());
        subnet.setCidr(getCidr().getEntity());
        subnet.setIpVersion(getIpVersion().getSelectedItem());
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
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });
        getCidr().validateEntity(new IValidation[] { getIpVersion().getSelectedItem() == IpVersion.IPV4
                ? new CidrValidation()
                : new NotEmptyValidation() });
        getIpVersion().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getName().getIsValid() && getCidr().getIsValid() && getIpVersion().getIsValid();
    }
}

package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.validation.BondNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IpAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SubnetMaskValidation;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class HostBondInterfaceModel extends Model
{

    private SortedListModel<String> privateBond;

    public SortedListModel<String> getBond()
    {
        return privateBond;
    }

    protected void setBond(SortedListModel<String> value)
    {
        privateBond = value;
    }

    private EntityModel<String> privateAddress;

    public EntityModel<String> getAddress()
    {
        return privateAddress;
    }

    private void setAddress(EntityModel<String> value)
    {
        privateAddress = value;
    }

    private EntityModel<String> privateSubnet;

    public EntityModel<String> getSubnet()
    {
        return privateSubnet;
    }

    private void setSubnet(EntityModel<String> value)
    {
        privateSubnet = value;
    }

    private EntityModel<String> privateGateway;

    public EntityModel<String> getGateway()
    {
        return privateGateway;
    }

    private void setGateway(EntityModel<String> value)
    {
        privateGateway = value;
    }

    private ListModel<Network> privateNetwork;

    public ListModel<Network> getNetwork()
    {
        return privateNetwork;
    }

    private void setNetwork(ListModel<Network> value)
    {
        privateNetwork = value;
    }

    private ListModel<Map.Entry<String, EntityModel<String>>> privateBondingOptions;

    public ListModel<Map.Entry<String, EntityModel<String>>> getBondingOptions()
    {
        return privateBondingOptions;
    }

    private void setBondingOptions(ListModel<Map.Entry<String, EntityModel<String>>> value)
    {
        privateBondingOptions = value;
    }

    private EntityModel<Boolean> privateCheckConnectivity;

    public EntityModel<Boolean> getCheckConnectivity()
    {
        return privateCheckConnectivity;
    }

    private void setCheckConnectivity(EntityModel<Boolean> value)
    {
        privateCheckConnectivity = value;
    }

    private NetworkBootProtocol bootProtocol = NetworkBootProtocol.values()[0];

    public NetworkBootProtocol getBootProtocol()
    {
        return bootProtocol;
    }

    public void setBootProtocol(NetworkBootProtocol value)
    {
        if (bootProtocol != value)
        {
            bootProtocol = value;
            bootProtocolChanged();
            onPropertyChanged(new PropertyChangedEventArgs("BootProtocol")); //$NON-NLS-1$
        }
    }

    private EntityModel<Boolean> privateCommitChanges;

    public EntityModel<Boolean> getCommitChanges()
    {
        return privateCommitChanges;
    }

    public void setCommitChanges(EntityModel<Boolean> value)
    {
        privateCommitChanges = value;
    }

    private boolean noneBootProtocolAvailable = true;

    public boolean getNoneBootProtocolAvailable()
    {
        return noneBootProtocolAvailable;
    }

    public void setNoneBootProtocolAvailable(boolean value)
    {
        if (noneBootProtocolAvailable != value)
        {
            noneBootProtocolAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("NoneBootProtocolAvailable")); //$NON-NLS-1$
        }
    }

    private boolean bootProtocolAvailable = true;

    public boolean getBootProtocolAvailable()
    {
        return bootProtocolAvailable;
    }

    public void setBootProtocolAvailable(boolean value)
    {
        if (bootProtocolAvailable != value)
        {
            bootProtocolAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("BootProtocolAvailable")); //$NON-NLS-1$
        }
    }

    public boolean getIsStaticAddress()
    {
        return getBootProtocol() == NetworkBootProtocol.STATIC_IP;
    }

    private NicLabelModel labelsModel;

    public NicLabelModel getLabelsModel() {
        return labelsModel;
    }

    protected void setLabelsModel(NicLabelModel labelsModel) {
        this.labelsModel = labelsModel;
    }

    public HostBondInterfaceModel() {
        setAddress(new EntityModel<String>());
        setSubnet(new EntityModel<String>());
        setGateway(new EntityModel<String>());
        setBond(new SortedListModel<String>(new LexoNumericComparator()));
        setNetwork(new ListModel<Network>());
        setBootProtocolAvailable(true);
        setBondingOptions(new ListModel<Map.Entry<String, EntityModel<String>>>());
        Map.Entry<String, EntityModel<String>> defaultItem = null;
        RefObject<Map.Entry<String, EntityModel<String>>> tempRef_defaultItem =
                new RefObject<Map.Entry<String, EntityModel<String>>>(defaultItem);
        ArrayList<Map.Entry<String, EntityModel<String>>> list =
                AsyncDataProvider.getInstance().getBondingOptionList(tempRef_defaultItem);
        defaultItem = tempRef_defaultItem.argvalue;
        getBondingOptions().setItems(list);
        getBondingOptions().setSelectedItem(defaultItem);
        setCheckConnectivity(new EntityModel<Boolean>());
        getCheckConnectivity().setEntity(false);
        EntityModel<Boolean> tempVar = new EntityModel<Boolean>();
        tempVar.setEntity(false);
        setCommitChanges(tempVar);

        getNetwork().getSelectedItemChangedEvent().addListener(this);

        // call the Network_ValueChanged method to set all
        // properties according to default value of Network:
        network_SelectedItemChanged(null);
    }

    private void network_SelectedItemChanged(EventArgs e)
    {
        updateCanSpecify();

        // ** TODO: When BootProtocol will be added to 'network', and when
        // ** BootProtocol, Address, Subnet, and Gateway will be added to
        // ** the Network Add/Edit dialog, the next lines will be uncommented.
        // ** DO NOT DELETE NEXT COMMENTED LINES!
        // var network = (network)Network;
        // BootProtocol = network == null ? null : network.bootProtocol;
        // Address.Value = network == null ? null : network.addr;
        // Subnet.Value = network == null ? null : network.subnet;
        // Gateway.Value = network == null ? null : network.gateway;
    }

    private void bootProtocolChanged()
    {
        updateCanSpecify();

        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);
        getGateway().setIsValid(true);
    }

    private void updateCanSpecify()
    {
        Network network = getNetwork().getSelectedItem();
        boolean isChangeble = getIsStaticAddress() && network != null && !network.getId().equals(Guid.Empty);
        getAddress().setIsChangable(isChangeble);
        getSubnet().setIsChangable(isChangeble);
        getGateway().setIsChangable(isChangeble);
    }

    public boolean validate()
    {
        getNetwork().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getBond().validateSelectedItem(new IValidation[] { new NotEmptyValidation(),
                new LengthValidation(BusinessEntitiesDefinitions.HOST_NIC_NAME_LENGTH), new BondNameValidation() });

        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);
        getGateway().setIsValid(true);

        if (getIsStaticAddress())
        {
            getAddress().validateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
            getSubnet().validateEntity(new IValidation[] { new NotEmptyValidation(), new SubnetMaskValidation() });
            getGateway().validateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
        }

        return getBond().getIsValid() && getNetwork().getIsValid() && getAddress().getIsValid()
                && getSubnet().getIsValid() && getGateway().getIsValid();
    }
}

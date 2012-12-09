package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NetworkGeneralModel extends EntityModel
{
    private final String ENGINE_NETWORK_NAME =
            (String) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    private String name;
    private String role;
    private Integer vlan;
    private Integer mtu;
    private String description;
    private Guid id;

    public NetworkGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void OnEntityChanged() {
        super.OnEntityChanged();

        if (super.getEntity() != null)
        {
            UpdateProperties();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.EntityPropertyChanged(sender, e);

        UpdateProperties();
    }

    private void UpdateProperties() {
        Network extendedNetwork = (Network) getEntity();

        setName(extendedNetwork.getName());
        setId(extendedNetwork.getId());
        setDescription(extendedNetwork.getdescription());

        String role = ""; //$NON-NLS-1$

        if (ENGINE_NETWORK_NAME.equals(extendedNetwork.getName())) {
            role = role.concat(ConstantsManager.getInstance().getConstants().mgmgtNetworkRole());
        }

        if (extendedNetwork.isVmNetwork()) {
            if (!role.equals("")) //$NON-NLS-1$
            {
                role = role.concat(" ,"); //$NON-NLS-1$
            }
            role = role.concat(ConstantsManager.getInstance().getConstants().vmNetworkRole());
        }
        setRole(role);
        setVlan(extendedNetwork.getvlan_id());

        if (extendedNetwork.getMtu() == 0) {
            setMtu(null);
        } else {
            setMtu(extendedNetwork.getMtu());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!StringHelper.stringsEqual(name, value))
        {
            name = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    public String getRole() {
        return role;
    }

    public void setRole(String value) {
        if (!StringHelper.stringsEqual(role, value))
        {
            role = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Role")); //$NON-NLS-1$
        }
    }

    public Integer getVlan() {
        return vlan;
    }

    public void setVlan(Integer value) {
        if (vlan == null && value == null)
        {
            return;
        }
        if (vlan == null || !vlan.equals(value))
        {
            vlan = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Vlan")); //$NON-NLS-1$
        }
    }

    public Integer getMtu() {
        return mtu;
    }

    public void setMtu(Integer value) {
        if (mtu == null && value == null)
        {
            return;
        }
        if (mtu == null || !mtu.equals(value))
        {
            mtu = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Mtu")); //$NON-NLS-1$
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value)
    {
        if (!StringHelper.stringsEqual(description, value))
        {
            description = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid value) {
        if ((id == null && value != null) || (id != null && !id.equals(value)))
        {
            id = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Id")); //$NON-NLS-1$
        }
    }
}

package org.ovirt.engine.ui.uicommonweb.models.networks;


import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NetworkGeneralModel extends EntityModel
{
    private final String ENGINE_NETWORK_NAME = (String) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    private String privateName;

    public String getName()
    {
        return privateName;
    }

    public void setName(String value)
    {
        if (!StringHelper.stringsEqual(privateName, value))
        {
            privateName = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    private String privateRole;

    public String getRole()
    {
        return privateRole;
    }

    public void setRole(String value)
    {
        if (!StringHelper.stringsEqual(privateRole, value))
        {
            privateRole = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Role")); //$NON-NLS-1$
        }
    }

    private Integer privateVlan;

    public Integer getVlan()
    {
        return privateVlan;
    }

    public void setVlan(Integer value)
    {
        if (privateVlan == null && value == null)
        {
            return;
        }
        if (privateVlan == null || !privateVlan.equals(value))
        {
            privateVlan = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Vlan")); //$NON-NLS-1$
        }
    }

    private Integer privateMtu;

    public Integer getMtu()
    {
        return privateMtu;
    }

    public void setMtu(Integer value)
    {
        if (privateMtu != value)
        {
            privateMtu = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Mtu")); //$NON-NLS-1$
        }
    }

    private String privateDescription;

    public String getDescription()
    {
        return privateDescription;
    }

    public void setDescription(String value)
    {
        if (!StringHelper.stringsEqual(privateDescription, value))
        {
            privateDescription = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }
    }

    private Guid privateId;

    public Guid getId()
    {
        return privateId;
    }

    public void setId(Guid value)
    {
        if ((privateId == null && value != null) || (privateId != null && !privateId.equals(value)))
        {
            privateId = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Id")); //$NON-NLS-1$
        }
    }

    public NetworkGeneralModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (super.getEntity() != null)
        {
            UpdateProperties();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        UpdateProperties();
    }

    private void UpdateProperties()
    {
        NetworkView extendedNetwork = (NetworkView) getEntity();


        setName(extendedNetwork.getNetwork().getName());
        setId(extendedNetwork.getNetwork().getId());
        setDescription(extendedNetwork.getNetwork().getdescription());

        String role = "";  //$NON-NLS-1$

        if (ENGINE_NETWORK_NAME.equals(extendedNetwork.getNetwork().getName())){
            role = role.concat(ConstantsManager.getInstance().getConstants().mgmgtNetworkRole());
        }

        if (extendedNetwork.getNetwork().isVmNetwork()){
            if (!role.equals("")) //$NON-NLS-1$
            {
                role = role.concat(" ,"); //$NON-NLS-1$
            }
            role = role.concat(ConstantsManager.getInstance().getConstants().vmNetworkRole());
        }
        setRole(role);
        setVlan(extendedNetwork.getNetwork().getvlan_id());

        if (extendedNetwork.getNetwork().getMtu() == 0){
            setMtu(null);
        }else{
            setMtu(extendedNetwork.getNetwork().getMtu());
        }
    }
}

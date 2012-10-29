package org.ovirt.engine.ui.uicommonweb.models.networks;


import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NetworkGeneralModel extends EntityModel
{
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

    private Boolean privateVm;

    public Boolean getVm()
    {
        return privateVm;
    }

    public void setVm(Boolean value)
    {
        if (privateVm !=  value)
        {
            privateVm = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Vm")); //$NON-NLS-1$
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
        Network network = (Network) getEntity();


        setName(network.getName());
        setDescription(network.getdescription());
        setVm(network.isVmNetwork());
        setVlan(network.getvlan_id());
        setMtu(network.getMtu());
    }
}

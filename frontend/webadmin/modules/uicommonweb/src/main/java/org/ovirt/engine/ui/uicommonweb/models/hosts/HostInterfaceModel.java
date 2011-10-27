package org.ovirt.engine.ui.uicommonweb.models.hosts;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class HostInterfaceModel extends EntityModel
{

	private EntityModel privateAddress;
	public EntityModel getAddress()
	{
		return privateAddress;
	}
	private void setAddress(EntityModel value)
	{
		privateAddress = value;
	}
	private EntityModel privateSubnet;
	public EntityModel getSubnet()
	{
		return privateSubnet;
	}
	private void setSubnet(EntityModel value)
	{
		privateSubnet = value;
	}
	private ListModel privateNetwork;
	public ListModel getNetwork()
	{
		return privateNetwork;
	}
	private void setNetwork(ListModel value)
	{
		privateNetwork = value;
	}
	private EntityModel privateCheckConnectivity;
	public EntityModel getCheckConnectivity()
	{
		return privateCheckConnectivity;
	}
	private void setCheckConnectivity(EntityModel value)
	{
		privateCheckConnectivity = value;
	}
	private ListModel privateBondingOptions;
	public ListModel getBondingOptions()
	{
		return privateBondingOptions;
	}
	private void setBondingOptions(ListModel value)
	{
		privateBondingOptions = value;
	}
	private java.util.ArrayList<VdsNetworkInterface> privateNetworks;
	public java.util.ArrayList<VdsNetworkInterface> getNetworks()
	{
		return privateNetworks;
	}
	public void setNetworks(java.util.ArrayList<VdsNetworkInterface> value)
	{
		privateNetworks = value;
	}

	private EntityModel privateName;
	public EntityModel getName()
	{
		return privateName;
	}
	public void setName(EntityModel value)
	{
		privateName = value;
	}
	private EntityModel privateCommitChanges;
	public EntityModel getCommitChanges()
	{
		return privateCommitChanges;
	}
	public void setCommitChanges(EntityModel value)
	{
		privateCommitChanges = value;
	}

	private EntityModel privateNetworkBootProtocol_None;
	public EntityModel getNetworkBootProtocol_None()
	{
		return privateNetworkBootProtocol_None;
	}
	public void setNetworkBootProtocol_None(EntityModel value)
	{
		privateNetworkBootProtocol_None = value;
	}
	private EntityModel privateNetworkBootProtocol_Dhcp;
	public EntityModel getNetworkBootProtocol_Dhcp()
	{
		return privateNetworkBootProtocol_Dhcp;
	}
	public void setNetworkBootProtocol_Dhcp(EntityModel value)
	{
		privateNetworkBootProtocol_Dhcp = value;
	}
	private EntityModel privateNetworkBootProtocol_StaticIp;
	public EntityModel getNetworkBootProtocol_StaticIp()
	{
		return privateNetworkBootProtocol_StaticIp;
	}
	public void setNetworkBootProtocol_StaticIp(EntityModel value)
	{
		privateNetworkBootProtocol_StaticIp = value;
	}

	//private NetworkBootProtocol bootProtocol;
	//public NetworkBootProtocol BootProtocol
	//{
	//    get
	//    {
	//        return bootProtocol;
	//    }

	//    set
	//    {
	//        if (bootProtocol != value)
	//        {
	//            bootProtocol = value;
	//            BootProtocolChanged();
	//            OnPropertyChanged(new PropertyChangedEventArgs("BootProtocol"));
	//        }
	//    }
	//}

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
			OnPropertyChanged(new PropertyChangedEventArgs("NoneBootProtocolAvailable"));
		}
	}


	private boolean bootProtocolsAvailable;
	public boolean getBootProtocolsAvailable()
	{
		return bootProtocolsAvailable;
	}

	public void setBootProtocolsAvailable(boolean value)
	{
		if (bootProtocolsAvailable != value)
		{
			bootProtocolsAvailable = value;
			OnPropertyChanged(new PropertyChangedEventArgs("BootProtocolsAvailable"));
		}
	}

	public boolean getIsStaticAddress()
	{
		return (Boolean)getNetworkBootProtocol_StaticIp().getEntity() == true;
	}



	private boolean privatebondingOptionsOverrideNotification;
	private boolean getbondingOptionsOverrideNotification()
	{
		return privatebondingOptionsOverrideNotification;
	}
	private void setbondingOptionsOverrideNotification(boolean value)
	{
		privatebondingOptionsOverrideNotification = value;
	}
	public boolean getBondingOptionsOverrideNotification()
	{
		return getbondingOptionsOverrideNotification();
	}
	public void setBondingOptionsOverrideNotification(boolean value)
	{
		setbondingOptionsOverrideNotification(value);
		OnPropertyChanged(new PropertyChangedEventArgs("BondingOptionsOverrideNotification"));
	}


	public HostInterfaceModel()
	{
		setAddress(new EntityModel());
		setSubnet(new EntityModel());
		setNetwork(new ListModel());
		getNetwork().getSelectedItemChangedEvent().addListener(this);
		setName(new EntityModel());
		EntityModel tempVar = new EntityModel();
		tempVar.setEntity(false);
		setCommitChanges(tempVar);

		EntityModel tempVar2 = new EntityModel();
		tempVar2.setEntity(true);
		setNetworkBootProtocol_None(tempVar2);
		getNetworkBootProtocol_None().getEntityChangedEvent().addListener(this);
		EntityModel tempVar3 = new EntityModel();
		tempVar3.setEntity(false);
		setNetworkBootProtocol_Dhcp(tempVar3);
		getNetworkBootProtocol_Dhcp().getEntityChangedEvent().addListener(this);
		EntityModel tempVar4 = new EntityModel();
		tempVar4.setEntity(false);
		setNetworkBootProtocol_StaticIp(tempVar4);
		getNetworkBootProtocol_StaticIp().getEntityChangedEvent().addListener(this);

		setCheckConnectivity(new EntityModel());
		getCheckConnectivity().setEntity(false);
		setBondingOptions(new ListModel());
		// call the Network_ValueChanged method to set all
		// properties according to default value of Network:
		Network_SelectedItemChanged(null);
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getNetwork())
		{
			Network_SelectedItemChanged(null);
		}
		else if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender instanceof EntityModel)
		{
			EntityModel senderEntityModel = (EntityModel)sender;
			if ((Boolean)senderEntityModel.getEntity())
			{
				if (senderEntityModel.equals(getNetworkBootProtocol_None()))
				{
					getNetworkBootProtocol_Dhcp().setEntity(false);
					getNetworkBootProtocol_StaticIp().setEntity(false);
					BootProtocolChanged();
				}
				else if (senderEntityModel.equals(getNetworkBootProtocol_Dhcp()))
				{
					getNetworkBootProtocol_None().setEntity(false);
					getNetworkBootProtocol_StaticIp().setEntity(false);
					BootProtocolChanged();
				}
				else if (senderEntityModel.equals(getNetworkBootProtocol_StaticIp()))
				{
					getNetworkBootProtocol_None().setEntity(false);
					getNetworkBootProtocol_Dhcp().setEntity(false);
					BootProtocolChanged();
				}
			}
		}
	}

	private void Network_SelectedItemChanged(EventArgs e)
	{
		UpdateCanSpecify();

		network network = (network)getNetwork().getSelectedItem();
		setBootProtocolsAvailable((network != null && StringHelper.stringsEqual(network.getname(), "None")) ? false : true);

		if (getNetworks() != null)
		{
			for (VdsNetworkInterface item : getNetworks())
			{
				if (StringHelper.stringsEqual(item.getNetworkName(), network.getname()))
				{
					getAddress().setEntity(StringHelper.isNullOrEmpty(item.getAddress()) ? null : item.getAddress());
					getSubnet().setEntity(StringHelper.isNullOrEmpty(item.getSubnet()) ? null : item.getSubnet());
					NetworkBootProtocol tempBootProtocol = !getNoneBootProtocolAvailable() && item.getBootProtocol() == NetworkBootProtocol.None ? NetworkBootProtocol.Dhcp : item.getBootProtocol();
					switch (tempBootProtocol)
					{
						case None:
							getNetworkBootProtocol_None().setEntity(true);
							break;
						case Dhcp:
							getNetworkBootProtocol_Dhcp().setEntity(true);
							break;
						case StaticIp:
							getNetworkBootProtocol_StaticIp().setEntity(true);
							break;
						default:
							break;
					}
					break;
				}
			}
		}

	}

	private void BootProtocolChanged()
	{
		UpdateCanSpecify();

		getAddress().setIsValid(true);
		getSubnet().setIsValid(true);
	}

	private void UpdateCanSpecify()
	{
		network network = (network)getNetwork().getSelectedItem();
		boolean isChangable = getIsStaticAddress() && network != null && !network.getId().equals(Guid.Empty);
		getAddress().setIsChangable(isChangable);
		getSubnet().setIsChangable(isChangable);
	}

	public boolean Validate()
	{
		getNetwork().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		getAddress().setIsValid(true);
		getSubnet().setIsValid(true);

		network net = (network)getNetwork().getSelectedItem();
		if (getIsStaticAddress() && getNetwork().getSelectedItem() != null && !net.getId().equals(Guid.Empty))
		{
			getAddress().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
			getSubnet().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
		}

		return getNetwork().getIsValid() && getAddress().getIsValid() && getSubnet().getIsValid();
	}
}
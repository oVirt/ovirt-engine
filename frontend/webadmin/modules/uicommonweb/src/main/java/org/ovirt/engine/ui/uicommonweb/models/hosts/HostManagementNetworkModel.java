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
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class HostManagementNetworkModel extends EntityModel
{

	public network getEntity()
	{
		return (network)super.getEntity();
	}
	public void setEntity(network value)
	{
		super.setEntity(value);
	}

	private ListModel privateInterface;
	public ListModel getInterface()
	{
		return privateInterface;
	}
	private void setInterface(ListModel value)
	{
		privateInterface = value;
	}
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
	private EntityModel privateGateway;
	public EntityModel getGateway()
	{
		return privateGateway;
	}
	private void setGateway(EntityModel value)
	{
		privateGateway = value;
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
	private EntityModel privateCommitChanges;
	public EntityModel getCommitChanges()
	{
		return privateCommitChanges;
	}
	public void setCommitChanges(EntityModel value)
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
			OnPropertyChanged(new PropertyChangedEventArgs("NoneBootProtocolAvailable"));
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


	public HostManagementNetworkModel()
	{
		setInterface(new ListModel());
		setAddress(new EntityModel());
		setSubnet(new EntityModel());
		setGateway(new EntityModel());

		setCheckConnectivity(new EntityModel());
		getCheckConnectivity().setEntity(false);
		setBondingOptions(new ListModel());

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

		UpdateFieldsByEntity();
	}

	private void UpdateFieldsByEntity()
	{
		UpdateCanSpecify();

		// ** TODO: When BootProtocol will be added to 'network', and when
		// ** BootProtocol, Address, Subnet, and Gateway will be added to
		// ** the Network Add/Edit dialog, the next lines will be uncommented.
		// ** DO NOT DELETE NEXT COMMENTED LINES!
		//var network = (network)Network;
		//BootProtocol = network == null ? null : network.bootProtocol;
		//Address.Value = network == null ? null : network.addr;
		//Subnet.Value = network == null ? null : network.subnet;
		//Gateway.Value = network == null ? null : network.gateway;
	}

	private void BootProtocolChanged()
	{
		UpdateCanSpecify();

		getAddress().setIsValid(true);
		getSubnet().setIsValid(true);
		getGateway().setIsValid(true);
	}

	private void UpdateCanSpecify()
	{
		getAddress().setIsChangable(getIsStaticAddress());
		getSubnet().setIsChangable(getIsStaticAddress());
		getGateway().setIsChangable(getIsStaticAddress());
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);
		if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender instanceof EntityModel)
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

	public boolean Validate()
	{
		getInterface().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		getAddress().setIsValid(true);
		getSubnet().setIsValid(true);

		if (getIsStaticAddress())
		{
			getAddress().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
			getSubnet().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
		}

		return getInterface().getIsValid() && getAddress().getIsValid() && getSubnet().getIsValid();
	}
}
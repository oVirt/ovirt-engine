package org.ovirt.engine.ui.uicommon.models.hosts;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

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
			BootProtocolChanged();
			OnPropertyChanged(new PropertyChangedEventArgs("BootProtocol"));
		}
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
		return getBootProtocol() == NetworkBootProtocol.StaticIp;
	}

	private boolean commitChanges;
	public boolean getCommitChanges()
	{
		return commitChanges;
	}
	public void setCommitChanges(boolean value)
	{
		if (commitChanges != value)
		{
			commitChanges = value;
			OnPropertyChanged(new PropertyChangedEventArgs("CommitChanges"));
		}
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
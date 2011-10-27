package org.ovirt.engine.ui.uicommon.models.datacenters;
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
public class NetworkModel extends Model
{

	private boolean privateIsNew;
	public boolean getIsNew()
	{
		return privateIsNew;
	}
	public void setIsNew(boolean value)
	{
		privateIsNew = value;
	}

	private EntityModel privateName;
	public EntityModel getName()
	{
		return privateName;
	}
	private void setName(EntityModel value)
	{
		privateName = value;
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
	private EntityModel privateDescription;
	public EntityModel getDescription()
	{
		return privateDescription;
	}
	private void setDescription(EntityModel value)
	{
		privateDescription = value;
	}
	private EntityModel privateVLanTag;
	public EntityModel getVLanTag()
	{
		return privateVLanTag;
	}
	private void setVLanTag(EntityModel value)
	{
		privateVLanTag = value;
	}

	private boolean isStpEnabled;
	public boolean getIsStpEnabled()
	{
		return isStpEnabled;
	}
	public void setIsStpEnabled(boolean value)
	{
		if (isStpEnabled != value)
		{
			isStpEnabled = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsStpEnabled"));
		}
	}

	private boolean hasVLanTag;
	public boolean getHasVLanTag()
	{
		return hasVLanTag;
	}
	public void setHasVLanTag(boolean value)
	{
		if (hasVLanTag != value)
		{
			hasVLanTag = value;
			HasVLanTagChanged();
			OnPropertyChanged(new PropertyChangedEventArgs("HasVLanTag"));
		}
	}


	public NetworkModel()
	{
		setName(new EntityModel());
		setAddress(new EntityModel());
		setSubnet(new EntityModel());
		setGateway(new EntityModel());
		setDescription(new EntityModel());
		setVLanTag(new EntityModel());
	}

	private void HasVLanTagChanged()
	{
		if (!getHasVLanTag())
		{
			getVLanTag().setIsValid(true);
		}
	}

	public boolean Validate()
	{
		RegexValidation tempVar = new RegexValidation();
		tempVar.setExpression("^[A-Za-z0-9_]{1,15}$");
		tempVar.setMessage("Name must contain alphanumeric characters or '_' (maximum length 15 characters).");
		RegexValidation tempVar2 = new RegexValidation();
		tempVar2.setIsNegate(true);
		tempVar2.setExpression("^(bond)");
		tempVar2.setMessage("Network name shouldn't start with 'bond'.");
		getName().ValidateEntity(new IValidation[]{ new NotEmptyValidation(), tempVar, tempVar2 });

		getAddress().ValidateEntity(new IValidation[] { new IpAddressValidation() });

		getSubnet().ValidateEntity(new IValidation[] { new IpAddressValidation() });

		getGateway().ValidateEntity(new IValidation[] { new IpAddressValidation() });

		LengthValidation tempVar3 = new LengthValidation();
		tempVar3.setMaxLength(40);
		getDescription().ValidateEntity(new IValidation[] { tempVar3 });

		getVLanTag().setIsValid(true);
		if (getHasVLanTag())
		{
			IntegerValidation tempVar4 = new IntegerValidation();
			tempVar4.setMinimum(0);
			tempVar4.setMaximum(4095);
			getVLanTag().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar4 });
		}

		return getName().getIsValid() && getAddress().getIsValid() && getSubnet().getIsValid() && getGateway().getIsValid() && getVLanTag().getIsValid() && getDescription().getIsValid();
	}
}
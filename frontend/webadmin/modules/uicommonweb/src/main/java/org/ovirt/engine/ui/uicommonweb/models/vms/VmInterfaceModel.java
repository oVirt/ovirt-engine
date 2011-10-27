package org.ovirt.engine.ui.uicommonweb.models.vms;
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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class VmInterfaceModel extends Model
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
	private ListModel privateNetwork;
	public ListModel getNetwork()
	{
		return privateNetwork;
	}
	private void setNetwork(ListModel value)
	{
		privateNetwork = value;
	}
	private ListModel privateNicType;
	public ListModel getNicType()
	{
		return privateNicType;
	}
	private void setNicType(ListModel value)
	{
		privateNicType = value;
	}
	private EntityModel privateMAC;
	public EntityModel getMAC()
	{
		return privateMAC;
	}
	private void setMAC(EntityModel value)
	{
		privateMAC = value;
	}


	public VmInterfaceModel()
	{
		setName(new EntityModel());
		setNetwork(new ListModel());
		setNicType(new ListModel());
		setMAC(new EntityModel());
		getMAC().getPropertyChangedEvent().addListener(this);
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (sender == getMAC())
		{
			MAC_PropertyChanged((PropertyChangedEventArgs)args);
		}
	}

	private void MAC_PropertyChanged(PropertyChangedEventArgs e)
	{
		if (e.PropertyName.equals("IsChangeAllowed") && !getMAC().getIsChangable())
		{
			getMAC().setIsValid(true);
		}
	}

	public boolean Validate()
	{
		RegexValidation tempVar = new RegexValidation();
		tempVar.setExpression("^\\w+$");
		tempVar.setMessage("Name must contain alphanumeric characters only.");
		getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

		getNetwork().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		getNicType().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		getMAC().setIsValid(true);
		if (getMAC().getIsChangable())
		{
			getMAC().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new MacAddressValidation() });
		}

		return getName().getIsValid() && getNetwork().getIsValid() && getNicType().getIsValid() && getMAC().getIsValid();
	}
}
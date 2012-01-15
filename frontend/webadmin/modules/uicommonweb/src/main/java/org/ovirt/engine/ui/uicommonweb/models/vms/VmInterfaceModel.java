package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.MacAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

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
            MAC_PropertyChanged((PropertyChangedEventArgs) args);
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

        return getName().getIsValid() && getNetwork().getIsValid() && getNicType().getIsValid()
                && getMAC().getIsValid();
    }
}

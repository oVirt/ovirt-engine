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
import org.ovirt.engine.ui.uicompat.ConstantsManager;

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

    private EntityModel privatePortMirroring;

    public EntityModel getPortMirroring()
    {
        return privatePortMirroring;
    }

    public void setPortMirroring(EntityModel value)
    {
        privatePortMirroring = value;
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

    private EntityModel plugged;

    public EntityModel getPlugged()
    {
        return plugged;
    }

    private void setPlugged(EntityModel value)
    {
        plugged = value;
    }

    private EntityModel plugged_IsSelected;

    public EntityModel getPlugged_IsSelected()
    {
        return plugged_IsSelected;
    }

    public void setPlugged_IsSelected(EntityModel value)
    {
        plugged_IsSelected = value;
    }

    private EntityModel unplugged_IsSelected;

    public EntityModel getUnplugged_IsSelected()
    {
        return unplugged_IsSelected;
    }

    public void setUnplugged_IsSelected(EntityModel value)
    {
        unplugged_IsSelected = value;
    }

    public VmInterfaceModel()
    {
        setName(new EntityModel());
        setNetwork(new ListModel());
        setNicType(new ListModel());
        setMAC(new EntityModel());
        setPortMirroring(new EntityModel());
        getMAC().getPropertyChangedEvent().addListener(this);

        setPlugged(new EntityModel());
        getPlugged().getEntityChangedEvent().addListener(this);

        setPlugged_IsSelected(new EntityModel());
        getPlugged_IsSelected().getEntityChangedEvent().addListener(this);

        setUnplugged_IsSelected(new EntityModel());
        getUnplugged_IsSelected().getEntityChangedEvent().addListener(this);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (sender == getMAC())
        {
            MAC_PropertyChanged((PropertyChangedEventArgs) args);
        }

        else if (sender == getPlugged())
        {
            boolean plugged = (Boolean) getPlugged().getEntity();
            getPlugged_IsSelected().setEntity(plugged);
            getUnplugged_IsSelected().setEntity(!plugged);
        }
        else if (sender == getPlugged_IsSelected())
        {
            if ((Boolean) getPlugged_IsSelected().getEntity()) {
                getPlugged().setEntity(true);
            }
        }
        else if (sender == getUnplugged_IsSelected())
        {
            if ((Boolean) getUnplugged_IsSelected().getEntity()) {
                getPlugged().setEntity(false);
            }
        }
    }

    private void MAC_PropertyChanged(PropertyChangedEventArgs e)
    {
        if (e.PropertyName.equals("IsChangeAllowed") && !getMAC().getIsChangable()) //$NON-NLS-1$
        {
            getMAC().setIsValid(true);
        }
    }

    public boolean Validate()
    {
        RegexValidation tempVar = new RegexValidation();
        tempVar.setExpression("^\\w+$"); //$NON-NLS-1$
        tempVar.setMessage(ConstantsManager.getInstance().getConstants().nameMustContainAlphanumericCharactersOnlyMsg());
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

package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public abstract class ConsoleModel extends EntityModel
{
    public static final String EjectLabel = "[Eject]"; //$NON-NLS-1$

    public static EventDefinition ErrorEventDefinition;
    private Event privateErrorEvent;

    /**
     * The user have selected this model in the edit console dialog
     */
    private boolean userSelected;

    public boolean isUserSelected() {
        return userSelected;
    }

    private ConsoleSelectionContext selectionContext;

    public ConsoleSelectionContext getSelectionContext() {
        return selectionContext;
    }

    public void setSelectionContext(ConsoleSelectionContext selectionContext) {
        this.selectionContext = selectionContext;
    }

    public void setUserSelected(boolean userSelected) {
        this.userSelected = userSelected;
    }

    public Event getErrorEvent()
    {
        return privateErrorEvent;
    }

    private void setErrorEvent(Event value)
    {
        privateErrorEvent = value;
    }

    private UICommand privateConnectCommand;

    public UICommand getConnectCommand()
    {
        return privateConnectCommand;
    }

    private void setConnectCommand(UICommand value)
    {
        privateConnectCommand = value;
    }

    private boolean isConnected;

    public boolean getIsConnected()
    {
        return isConnected;
    }

    public void setIsConnected(boolean value)
    {
        if (isConnected != value)
        {
            isConnected = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsConnected")); //$NON-NLS-1$
        }
    }

    private boolean forceVmStatusUp;

    public boolean getForceVmStatusUp()
    {
        return forceVmStatusUp;
    }

    public void setForceVmStatusUp(boolean value)
    {
        if (forceVmStatusUp != value)
        {
            forceVmStatusUp = value;
            OnPropertyChanged(new PropertyChangedEventArgs("ForceVmStatusUp")); //$NON-NLS-1$
        }
    }

    @Override
    public VM getEntity()
    {
        return (VM) super.getEntity();
    }

    public void setEntity(VM value)
    {
        super.setEntity(value);
    }

    static
    {
        ErrorEventDefinition = new EventDefinition("Error", ConsoleModel.class); //$NON-NLS-1$
    }

    protected ConsoleModel()
    {
        setErrorEvent(new Event(ErrorEventDefinition));

        setConnectCommand(new UICommand("Connect", this)); //$NON-NLS-1$
    }

    protected abstract void Connect();

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        UpdateActionAvailability();
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();
        }
    }

    protected void UpdateActionAvailability()
    {
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getConnectCommand())
        {
            Connect();
        }
    }

    public boolean IsVmConnectReady()
    {
        if (getForceVmStatusUp())
        {
            return getEntity().getStatus() == VMStatus.Up;
        }

        return IsVmUp();
    }

    public boolean IsVmUp()
    {
        switch (getEntity().getStatus())
        {
        case PoweringUp:
        case Up:
        case RebootInProgress:
        case PoweringDown:
        case Paused:
            return true;

        default:
            return false;
        }
    }
}

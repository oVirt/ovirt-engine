package org.ovirt.engine.ui.uicommonweb.models.configure;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;

@SuppressWarnings("unused")
public class ChangeCDModel extends Model
{

    // public event EventHandler Executed = delegate { };

    public static EventDefinition ExecutedEventDefinition;
    private Event privateExecutedEvent;

    public Event getExecutedEvent()
    {
        return privateExecutedEvent;
    }

    private void setExecutedEvent(Event value)
    {
        privateExecutedEvent = value;
    }

    private UICommand privateDoCommand;

    public UICommand getDoCommand()
    {
        return privateDoCommand;
    }

    private void setDoCommand(UICommand value)
    {
        privateDoCommand = value;
    }

    static
    {
        ExecutedEventDefinition = new EventDefinition("Executed", ChangeCDModel.class); //$NON-NLS-1$
    }

    public ChangeCDModel()
    {
        setExecutedEvent(new Event(ExecutedEventDefinition));

        setDoCommand(new UICommand("Do", this)); //$NON-NLS-1$
    }

    private void doAction()
    {
        getExecutedEvent().raise(this, EventArgs.Empty);
        // Executed(this, EventArgs.Empty);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getDoCommand())
        {
            doAction();
        }
    }
}

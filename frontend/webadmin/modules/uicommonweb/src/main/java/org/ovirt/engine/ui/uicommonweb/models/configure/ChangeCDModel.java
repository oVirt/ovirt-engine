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

    public static final EventDefinition executedEventDefinition;
    private Event<EventArgs> privateExecutedEvent;

    public Event<EventArgs> getExecutedEvent()
    {
        return privateExecutedEvent;
    }

    private void setExecutedEvent(Event<EventArgs> value)
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
        executedEventDefinition = new EventDefinition("Executed", ChangeCDModel.class); //$NON-NLS-1$
    }

    public ChangeCDModel()
    {
        setExecutedEvent(new Event<EventArgs>(executedEventDefinition));

        setDoCommand(new UICommand("Do", this)); //$NON-NLS-1$
    }

    private void doAction()
    {
        getExecutedEvent().raise(this, EventArgs.EMPTY);
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

package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class SanTargetModel extends EntityModel<StorageServerConnections> {

    public static final EventDefinition loggedInEventDefinition;
    private Event<EventArgs> loggedInEvent;

    public Event<EventArgs> getLoggedInEvent() {
        return loggedInEvent;
    }

    private void setLoggedInEvent(Event<EventArgs> value) {
        loggedInEvent = value;
    }

    private UICommand loginCommand;

    public UICommand getLoginCommand() {
        return loginCommand;
    }

    public void setLoginCommand(UICommand value) {
        loginCommand = value;
    }

    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        if (!Objects.equals(address, value)) {
            address = value;
            onPropertyChanged(new PropertyChangedEventArgs("Address")); //$NON-NLS-1$
        }
    }

    private String port;

    public String getPort() {
        return port;
    }

    public void setPort(String value) {
        if (!Objects.equals(port, value)) {
            port = value;
            onPropertyChanged(new PropertyChangedEventArgs("Port")); //$NON-NLS-1$
        }
    }

    private String portal;

    public String getPortal() {
        return portal;
    }

    public void setPortal(String value) {
        if (!Objects.equals(portal, value)) {
            portal = value;
            onPropertyChanged(new PropertyChangedEventArgs("Portal")); //$NON-NLS-1$
        }
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!Objects.equals(name, value)) {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    private boolean isLoggedIn;

    public boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean value) {
        if (isLoggedIn != value) {
            isLoggedIn = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsLoggedIn")); //$NON-NLS-1$
        }
    }

    private List<LunModel> luns;

    public List<LunModel> getLuns() {
        return luns;
    }

    public void setLuns(List<LunModel> value) {
        if (luns != value) {
            luns = value;
            onPropertyChanged(new PropertyChangedEventArgs("Luns")); //$NON-NLS-1$
            getLunsList().setItems(luns);
        }
    }

    private ListModel<LunModel> lunsList;

    public ListModel<LunModel> getLunsList() {
        return lunsList;
    }

    public void setLunsList(ListModel<LunModel> value) {
        if (lunsList != value) {
            lunsList = value;
            onPropertyChanged(new PropertyChangedEventArgs("LunsList")); //$NON-NLS-1$
        }
    }

    static {
        loggedInEventDefinition = new EventDefinition("LoggedIn", SanTargetModel.class); //$NON-NLS-1$
    }

    public SanTargetModel() {
        setLoggedInEvent(new Event<>(loggedInEventDefinition));
        setLoginCommand(new UICommand("", this)); //$NON-NLS-1$
        setLunsList(new ListModel<LunModel>());
    }

    private void login() {
        getLoggedInEvent().raise(this, EventArgs.EMPTY);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getLoginCommand()) {
            login();
        }
    }
}

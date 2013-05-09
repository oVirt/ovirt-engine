package org.ovirt.engine.ui.userportal.utils;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.ConsoleManager;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.models.userportal.IUserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.userportal.section.login.presenter.ConnectAutomaticallyProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Listens on the getItemsChangedEvent of the model and opens the console when: <li>model.getCanConnectAutomatically()
 * returns true <li>the connect automatically has been checked on the logon screen <li>the console has not yet been
 * opened
 * <p>
 * If the console has been opened and closed, the console will not be opened again
 * </p>
 */
public class ConnectAutomaticallyManager {

    private final ConnectAutomaticallyProvider connectAutomatically;

    private boolean alreadyOpened = false;

    private List<EventChangeListener> listeners;

    private final ConsoleManager consoleManager;

    @Inject
    public ConnectAutomaticallyManager(ConnectAutomaticallyProvider connectAutomatically,
            ConsoleUtils consoleUtils,
            ConsoleManager consoleManager,
            EventBus eventBus) {
        this.connectAutomatically = connectAutomatically;
        this.consoleManager = consoleManager;
    }

    public void resetAlreadyOpened() {
        this.alreadyOpened = false;
    }

    public void unregisterModels() {
        if (listeners == null) {
            return;
        }

        for (EventChangeListener listener : listeners) {
            listener.unregister();
        }

        listeners.clear();
    }

    public void registerModel(final IUserPortalListModel model) {

        if (alreadyOpened || !connectAutomatically.readConnectAutomatically()) {
            return;
        }

        EventChangeListener listener = new EventChangeListener(model);
        if (listeners == null) {
            listeners = new ArrayList<EventChangeListener>();
        }
        listeners.add(listener);
        listener.register();
    }

    class EventChangeListener implements IEventListener {

        private final IUserPortalListModel model;

        public EventChangeListener(IUserPortalListModel model) {
            this.model = model;
        }

        public void register() {
            model.getItemsChangedEvent().addListener(this);
        }

        public void unregister() {
            model.getItemsChangedEvent().removeListener(this);
        }

        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {

            if (connectAutomatically.readConnectAutomatically() && model.getCanConnectAutomatically() && !alreadyOpened) {
                UserPortalItemModel userPortalItemModel = model.getUpVms(model.getItems()).get(0);
                if (userPortalItemModel != null) {
                    consoleManager.connectToConsole(userPortalItemModel);

                    alreadyOpened = true;
                }
            }

            unregisterModels();

        }
    }
}

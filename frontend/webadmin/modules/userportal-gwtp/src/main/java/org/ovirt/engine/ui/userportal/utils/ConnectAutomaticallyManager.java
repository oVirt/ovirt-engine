package org.ovirt.engine.ui.userportal.utils;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicommonweb.models.userportal.AbstractUserPortalListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.userportal.section.login.presenter.ConnectAutomaticallyProvider;

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

    private final ErrorPopupManager errorPopupManager;

    private final ConnectAutomaticallyProvider connectAutomatically;

    private boolean alreadyOpened = false;

    private List<EventChangeListener> listeners;

    @Inject
    public ConnectAutomaticallyManager(ConnectAutomaticallyProvider connectAutomatically,
                                       ErrorPopupManager errorPopupManager) {
        this.connectAutomatically = connectAutomatically;
        this.errorPopupManager = errorPopupManager;
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

    public void registerModel(final AbstractUserPortalListModel model) {
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

    class EventChangeListener implements IEventListener<EventArgs> {

        private final AbstractUserPortalListModel model;

        public EventChangeListener(AbstractUserPortalListModel model) {
            this.model = model;
        }

        public void register() {
            model.getItemsChangedEvent().addListener(this);
        }

        public void unregister() {
            model.getItemsChangedEvent().removeListener(this);
        }

        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            if (connectAutomatically.readConnectAutomatically() && model.getCanConnectAutomatically() && !alreadyOpened) {
                try {
                    model.getAutoConnectableConsoles().get(0).connect();
                    alreadyOpened = true;
                } catch (VmConsoles.ConsoleConnectException e) {
                    errorPopupManager.show(e.getLocalizedErrorMessage());
                }
            }

            unregisterModels();
        }
    }
}

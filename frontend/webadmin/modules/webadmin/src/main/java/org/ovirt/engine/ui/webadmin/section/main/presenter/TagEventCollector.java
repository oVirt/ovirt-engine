package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.webadmin.uicommon.model.TagActivationChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Collects all tag activation changes until both VM and Hosts presenter are active.
 */
public class TagEventCollector {
    List<TagActivationChangeEvent> eventList = new ArrayList<>();

    HandlerRegistration handler;

    // We only care about VMs and Hosts since those are the entities with tag events. So once both presenters
    // are active we can disable collecting tag events in the TagEventCollector.
    boolean vmsActive = false;
    boolean hostsActive = false;

    @Inject
    public TagEventCollector(EventBus eventBus) {
        handler = eventBus.addHandler(TagActivationChangeEvent.getType(), event -> eventList.add(event));
    }

    public List<TagActivationChangeEvent> getActivationEvents() {
        return eventList;
    }

    public void activateVms() {
        vmsActive = true;
        deactivateHandler();
    }

    private void deactivateHandler() {
        if (vmsActive && hostsActive) {
            handler.removeHandler();
            eventList.clear();
        }
    }

    public void activateHosts() {
        hostsActive = true;
        deactivateHandler();
    }

}

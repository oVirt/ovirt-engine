package org.ovirt.engine.core.bll.gluster.events;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is called by the webhook servlet whenever an
 * event is received.
 *
 */
@Singleton
public class GlusterEventsProcessor {

    private static final Logger log = LoggerFactory.getLogger(GlusterEventsProcessor.class);

    private ConcurrentMap<String, GlusterEventSubscriber> subscribers = new ConcurrentHashMap<>();

    public GlusterEventsProcessor(){

    }

    public void processEvent(GlusterEvent event) {
        if (subscribers.containsKey(event.getEvent())) {
            GlusterEventSubscriber clz = subscribers.get(event.getEvent());
            clz.processEvent(event);
        }

    }

    public void addSubscriber(String eventType, GlusterEventSubscriber classType) {
        if (subscribers.containsKey(eventType)) {
            log.debug("Replacing associated handler for '{}'", eventType);
        }
        subscribers.put(eventType, classType);
    }

    public void removeSubscriber(String eventType, GlusterEventSubscriber classType) {
        subscribers.remove(eventType, classType);
    }

}

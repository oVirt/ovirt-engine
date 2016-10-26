package org.ovirt.engine.core.bll.gluster.events;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterEvent;
import org.ovirt.engine.core.di.Injector;
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
        subscribers.put("EVENT_GEOREP_.*", Injector.injectMembers(new GeorepEventSubscriber()));
        subscribers.put("BRICK.*", Injector.injectMembers(new GlusterBrickEventSubscriber()));
    }

    public void processEvent(GlusterEvent event) {
        log.debug(event.toString());
        String subKey = subscribers.keySet()
                .stream()
                .filter(key -> Pattern.compile(key).matcher(event.getEvent()).matches())
                .findFirst()
                .orElse(null);
        if (subKey != null) {
            GlusterEventSubscriber clz = subscribers.get(subKey);
            clz.processEvent(event);
        }
    }

    public void addSubscriber(String eventPattern, GlusterEventSubscriber classType) {
        if (subscribers.containsKey(eventPattern)) {
            log.debug("Replacing associated handler for '{}'", eventPattern);
        }
        subscribers.put(eventPattern, classType);
    }

    public void removeSubscriber(String eventType, GlusterEventSubscriber classType) {
        subscribers.remove(eventType, classType);
    }

}

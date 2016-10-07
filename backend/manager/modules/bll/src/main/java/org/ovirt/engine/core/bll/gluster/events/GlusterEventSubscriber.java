package org.ovirt.engine.core.bll.gluster.events;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterEvent;


/**
 * Interface that needs to be implemented to handle gluster events received
 * via webhook.
 *
 */
public interface GlusterEventSubscriber {

    public void processEvent(GlusterEvent event);

}

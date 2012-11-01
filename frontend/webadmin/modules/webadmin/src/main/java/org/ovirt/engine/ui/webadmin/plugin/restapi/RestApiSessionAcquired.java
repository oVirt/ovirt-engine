package org.ovirt.engine.ui.webadmin.plugin.restapi;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when {@link RestApiSessionManager} acquires new Engine REST API session.
 */
@GenEvent
public class RestApiSessionAcquired {

    String sessionId;

}

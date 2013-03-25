package org.ovirt.engine.ui.webadmin.system;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when the application window receives HTML5 {@code message} event.
 */
@GenEvent
public class MessageReceived {

    MessageEventData data;

}

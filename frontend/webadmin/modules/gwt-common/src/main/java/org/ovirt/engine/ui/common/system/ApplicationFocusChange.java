package org.ovirt.engine.ui.common.system;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when the application window gains or looses its focus.
 */
@GenEvent
public class ApplicationFocusChange {

    boolean inFocus;

}

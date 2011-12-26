package org.ovirt.engine.ui.webadmin.section.main.view;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when the Application gains or looses its focus
 */
@GenEvent
public class ApplicationFocusChange {

    boolean inFocus;

}

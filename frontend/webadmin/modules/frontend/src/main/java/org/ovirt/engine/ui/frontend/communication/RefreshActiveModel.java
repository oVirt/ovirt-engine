package org.ovirt.engine.ui.frontend.communication;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when a VdcOperation completes. The typical use case is when a {@code VdcAction} completes
 * and needs to inform models to refresh themselves.
 */
@GenEvent
public class RefreshActiveModel {
    boolean doFastForward;
}

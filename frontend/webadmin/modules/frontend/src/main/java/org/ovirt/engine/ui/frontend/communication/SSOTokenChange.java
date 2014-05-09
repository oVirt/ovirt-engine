package org.ovirt.engine.ui.frontend.communication;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when SSO token is acquired as part of successful authentication.
 */
@GenEvent
public class SSOTokenChange {
    String token;
}

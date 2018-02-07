package org.ovirt.engine.core.common.flow;

/**
 * Possible outcomes of executing a {@link Handler}
 */
public enum HandlerOutcome {
    SUCCESS,
    NEUTRAL,
    FAILURE,
    EXCEPTION
}

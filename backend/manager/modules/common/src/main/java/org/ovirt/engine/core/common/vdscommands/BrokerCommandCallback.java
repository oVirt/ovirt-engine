package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

/**
 * This interface provides a way to pass a logic which is run
 * after the specific command was invoked. The logic is invoked
 * by running {@link #onResponse} when command's response arrives
 * or there was an issue during invocation so {@link #onFailure} is
 * run.
 */
public interface BrokerCommandCallback {

    /**
     * When a command response arrive this method is invoked and it
     * should contain continuation logic for the command.
     *
     * @param response - Map containing response where we optionally
     *                   provide status key. It is provided when an
     *                   issue occurred on the server side.
     */
    void onResponse(Map<String, Object> response);

    /**
     * Whenever any issue occurs during command invocation it
     * will be passed here as an instance of {@link Throwable}.
     *
     * @param t - Instance of {@link Throwable} containing information
     *            about the failure.
     */
    void onFailure(Throwable t);
}

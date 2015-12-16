package org.ovirt.engine.core.common.eventqueue;

import java.util.concurrent.Callable;

public interface EventQueue {

    /**
     * The following method should allow to submit an asynchronous event
     * The Event will be submitted to queue and will be executed when after that
     * @param event - description of event
     * @param callable - a code which should be run
     */
    void submitEventAsync(Event event, Callable<EventResult> callable);

    /**
     * The following method is used in order to submit a synchronous event
     * The Event will submitted and a thread will be stuck until event will be
     * executed or aborted
     * @param event - description of event
     * @param callable - a code which should be run
     */
    EventResult submitEventSync(Event event, Callable<EventResult> callable);

}

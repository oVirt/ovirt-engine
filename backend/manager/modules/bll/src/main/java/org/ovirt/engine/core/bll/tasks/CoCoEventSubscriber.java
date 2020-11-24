package org.ovirt.engine.core.bll.tasks;

import java.util.Map;
import java.util.concurrent.Flow;

import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.vdsm.jsonrpc.client.events.EventSubscriber;

public class CoCoEventSubscriber extends EventSubscriber {
    private Flow.Subscription subscription;
    private CommandEntity commandEntity;
    private CommandsRepository commandsRepository;

    public CoCoEventSubscriber(String subscriptionId,
                               CommandEntity commandEntity,
                               CommandsRepository commandsRepository) {
        super(subscriptionId);
        this.commandEntity = commandEntity;
        this.commandsRepository = commandsRepository;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Map<String, Object> eventData) {
        // Invokes command's callback for processing the event
        processEvent(eventData);
    }

    @Override
    public void onError(Throwable throwable) {
        // communication issue is delivered as a message so we need to request for more
        subscription.request(1);
    }

    @Override
    public void onComplete() {
    }

    public void cancel() {
        subscription.cancel();
    }

    /**
     * Processes the event of the current subscription by invoking the {@code CommandCallback.onEvent} of the associated
     * command. After the event is processed, the callback can be terminated from the {@code CommandCallback.onEvent} or
     * continue to the rest of command's callback execution.
     *
     * @param eventData
     *            the requested data for the event processing
     */
    private void processEvent(Map<String, Object> eventData) {
        Guid cmdId = commandEntity.getId();
        CallbackTiming callbackTiming = commandsRepository.getCallbackTiming(cmdId);
        if (callbackTiming == null) {
            return;
        }

        try {
            // TODO there is a race condition between processing the callback here and by the polling, in case of an
            // event which has arrived during its timeout treatment by CommandCallbacksPoller.
            // Once Bug 1334926 is implemented, this code will be changed to mark the callback to be processed, and
            // the actual processing will be done by the CommandCallbacksPoller which will be the responsible to invoke
            // the CommandCallback.onEvent() on thread from a dedicated thread pool
            callbackTiming.getCallback().onEvent(cmdId, commandsRepository.getChildCommandIds(cmdId), eventData);
        } finally {
            subscription.cancel();
            commandsRepository.removeEventSubscription(cmdId);
            CommandEntity commandEntityFromCache = commandsRepository.getCommandEntity(cmdId);
            if (commandEntityFromCache != null) {
                commandEntityFromCache.setWaitingForEvent(false);
            }
        }
    }
}

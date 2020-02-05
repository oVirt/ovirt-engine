package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.vdsm.jsonrpc.client.events.EventSubscriber;
import org.reactivestreams.Subscription;

public class HostConnectionRefresher implements HostConnectionRefresherInterface {

    private SubscriberRefreshingHostOnHostConnectionChangeEvent subscriber;
    private ResourceManager resourceManager;
    protected VdsManager vdsManager;

    public HostConnectionRefresher(VdsManager vdsManager, ResourceManager resourceManager) {
        this.vdsManager = vdsManager;
        this.resourceManager = resourceManager;
    }

    @Override
    public void start() {
        subscriber = new SubscriberRefreshingHostOnHostConnectionChangeEvent();
        resourceManager.subscribe(subscriber);
    }

    @Override
    public void stop() {
        if (subscriber != null) {
            subscriber.unsubscribe();
        }
    }

    private class SubscriberRefreshingHostOnHostConnectionChangeEvent extends EventSubscriber {

        private Subscription subscription;

        public SubscriberRefreshingHostOnHostConnectionChangeEvent() {
            super(HostConnectionRefresher.this.vdsManager.getVdsHostname() + "|net|host_conn|*");
        }

        @Override
        public void onSubscribe(Subscription sub) {
            subscription = sub;
            subscription.request(1);
        }

        @Override
        public void onNext(Map<String, Object> map) {
            try {
                resourceManager.getEventListener().refreshHostCapabilities(vdsManager.getVdsId());
            } finally {
                subscription.request(1);
            }
        }

        @Override
        public void onError(Throwable t) {
            // communication issue is delivered as a message so we need to request for more
            subscription.request(1);
        }

        @Override
        public void onComplete() {
        }

        public void unsubscribe() {
            subscription.cancel();
        }
    }
}

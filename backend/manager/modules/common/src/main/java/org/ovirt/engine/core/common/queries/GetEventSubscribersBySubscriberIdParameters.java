package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetEventSubscribersBySubscriberIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5217613461609639801L;

    public GetEventSubscribersBySubscriberIdParameters(Guid subscriberId) {
        setSubscriberId(subscriberId);
    }

    private Guid privateSubscriberId = new Guid();

    public Guid getSubscriberId() {
        return privateSubscriberId;
    }

    private void setSubscriberId(Guid value) {
        privateSubscriberId = value;
    }

    public GetEventSubscribersBySubscriberIdParameters() {
    }
}

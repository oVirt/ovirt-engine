package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetEventSubscribersBySubscriberIdParameters")
public class GetEventSubscribersBySubscriberIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5217613461609639801L;

    public GetEventSubscribersBySubscriberIdParameters(Guid subscriberId) {
        setSubscriberId(subscriberId);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "SubscriberId")
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

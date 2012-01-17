package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmsMessagesParameters")
public class GetVmsMessagesParameters extends GetMessagesByIdParametersBase {
    private static final long serialVersionUID = -4224784218887876903L;

    public GetVmsMessagesParameters(Guid vmId) {
        super(vmId);
    }

    public GetVmsMessagesParameters() {
    }
}

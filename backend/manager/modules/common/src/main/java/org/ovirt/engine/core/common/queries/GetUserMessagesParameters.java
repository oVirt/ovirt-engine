package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetUserMessagesParameters")
public class GetUserMessagesParameters extends GetMessagesByIdParametersBase {
    private static final long serialVersionUID = 3931907771424026679L;

    public GetUserMessagesParameters(Guid vmId) {
        super(vmId);
    }

    public GetUserMessagesParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetEventMessagesParameters")
public class GetEventMessagesParameters extends GetMessagesByIdParametersBase {
    private static final long serialVersionUID = 921754923722982628L;

    public GetEventMessagesParameters(Guid vmId) {
        super(vmId);
    }

    public GetEventMessagesParameters() {
    }
}

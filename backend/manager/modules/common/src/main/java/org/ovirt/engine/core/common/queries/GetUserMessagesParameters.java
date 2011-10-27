package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
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

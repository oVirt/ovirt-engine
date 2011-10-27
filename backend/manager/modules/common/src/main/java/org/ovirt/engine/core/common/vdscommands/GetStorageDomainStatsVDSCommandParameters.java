package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetStorageDomainStatsVDSCommandParameters")
public class GetStorageDomainStatsVDSCommandParameters extends ValidateStorageDomainVDSCommandParameters {
    public GetStorageDomainStatsVDSCommandParameters(Guid vdsId, Guid storageDomainId) {
        super(vdsId, storageDomainId);
    }

    public GetStorageDomainStatsVDSCommandParameters() {
    }
}

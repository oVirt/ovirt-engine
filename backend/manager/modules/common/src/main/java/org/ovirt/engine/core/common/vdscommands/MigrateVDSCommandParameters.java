package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.MigrationMethod;
import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MigrateVDSCommandParameters")
public class MigrateVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String _srcHost;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private Guid _dstVdsId;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String _dstHost;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private MigrationMethod _migrationMethod = MigrationMethod.forValue(0);

    public MigrateVDSCommandParameters(Guid vdsId, Guid vmId, String srcHost, Guid dstVdsId, String dstHost,
            MigrationMethod migrationMethod) {
        super(vdsId, vmId);
        _srcHost = srcHost;
        _dstVdsId = dstVdsId;
        _dstHost = dstHost;
        _migrationMethod = migrationMethod;
    }

    public String getSrcHost() {
        return _srcHost;
    }

    public Guid getDstVdsId() {
        return _dstVdsId;
    }

    public String getDstHost() {
        return _dstHost;
    }

    public MigrationMethod getMigrationMethod() {
        return _migrationMethod;
    }

    public MigrateVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, srcHost=%s, dstVdsId=%s, dstHost=%s, migrationMethod=%s",
                super.toString(),
                getSrcHost(),
                getDstVdsId(),
                getDstHost(),
                getMigrationMethod());
    }
}

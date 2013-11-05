package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.MigrationMethod;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class MigrateVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String _srcHost;
    private Guid _dstVdsId;
    private String _dstHost;
    private MigrationMethod _migrationMethod;
    private boolean tunnelMigration;
    private String dstQemu;
    private Version clusterVersion;

    public MigrateVDSCommandParameters(Guid vdsId, Guid vmId, String srcHost, Guid dstVdsId, String dstHost,
            MigrationMethod migrationMethod, boolean tunnelMigration, String dstQemu, Version clusterVersion) {
        super(vdsId, vmId);
        _srcHost = srcHost;
        _dstVdsId = dstVdsId;
        _dstHost = dstHost;
        _migrationMethod = migrationMethod;
        this.tunnelMigration = tunnelMigration;
        this.dstQemu = dstQemu;
        this.clusterVersion = clusterVersion;
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

    public boolean isTunnelMigration() {
        return tunnelMigration;
    }

    public String getDstQemu() {
        return dstQemu;
    }

    public MigrateVDSCommandParameters() {
        _migrationMethod = MigrationMethod.OFFLINE;
    }

    @Override
    public String toString() {
        return String.format("%s, srcHost=%s, dstVdsId=%s, dstHost=%s, migrationMethod=%s, tunnelMigration=%s",
                super.toString(),
                getSrcHost(),
                getDstVdsId(),
                getDstHost(),
                getMigrationMethod(),
                isTunnelMigration());
    }

    public void setClusterVersion(Version clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

    public Version getClusterVersion() {
        return clusterVersion;
    }
}

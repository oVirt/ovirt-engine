package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.MigrationMethod;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class MigrateVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String srcHost;
    private Guid dstVdsId;
    private String dstHost;
    private MigrationMethod migrationMethod;
    private boolean tunnelMigration;
    private String dstQemu;
    private Version clusterVersion;
    private Integer migrationDowntime;

    public MigrateVDSCommandParameters(Guid vdsId, Guid vmId, String srcHost, Guid dstVdsId,
            String dstHost, MigrationMethod migrationMethod, boolean tunnelMigration,
            String dstQemu, Version clusterVersion, int migrationDowntime) {
        super(vdsId, vmId);
        this.srcHost = srcHost;
        this.dstVdsId = dstVdsId;
        this.dstHost = dstHost;
        this.migrationMethod = migrationMethod;
        this.tunnelMigration = tunnelMigration;
        this.dstQemu = dstQemu;
        this.clusterVersion = clusterVersion;
        this.migrationDowntime = migrationDowntime;
    }

    public String getSrcHost() {
        return srcHost;
    }

    public Guid getDstVdsId() {
        return dstVdsId;
    }

    public String getDstHost() {
        return dstHost;
    }

    public MigrationMethod getMigrationMethod() {
        return migrationMethod;
    }

    public boolean isTunnelMigration() {
        return tunnelMigration;
    }

    public String getDstQemu() {
        return dstQemu;
    }

    public int getMigrationDowntime() {
        return migrationDowntime;
    }

    public MigrateVDSCommandParameters() {
        migrationMethod = MigrationMethod.OFFLINE;
    }

    @Override
    public String toString() {
        return String.format("%s, srcHost=%s, dstVdsId=%s, dstHost=%s, migrationMethod=%s, tunnelMigration=%s, migrationDowntime=%s",
                super.toString(),
                getSrcHost(),
                getDstVdsId(),
                getDstHost(),
                getMigrationMethod(),
                isTunnelMigration(),
                getMigrationDowntime());
    }

    public void setClusterVersion(Version clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

    public Version getClusterVersion() {
        return clusterVersion;
    }
}

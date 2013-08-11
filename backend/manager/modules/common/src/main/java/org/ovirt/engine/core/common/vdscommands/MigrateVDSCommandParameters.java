package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.MigrationMethod;
import org.ovirt.engine.core.compat.Guid;

public class MigrateVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String _srcHost;
    private Guid _dstVdsId;
    private String _dstHost;
    private MigrationMethod _migrationMethod;
    private Boolean tunnelMigration;
    private String dstQemu;

    public MigrateVDSCommandParameters(Guid vdsId, Guid vmId, String srcHost, Guid dstVdsId, String dstHost,
            MigrationMethod migrationMethod, Boolean tunnelMigration, String dstQemu) {
        super(vdsId, vmId);
        _srcHost = srcHost;
        _dstVdsId = dstVdsId;
        _dstHost = dstHost;
        _migrationMethod = migrationMethod;
        this.tunnelMigration = tunnelMigration;
        this.dstQemu = dstQemu;
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

    public Boolean getTunnelMigration() {
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
                getTunnelMigration());
    }
}

package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;

public class MigrateBrokerVDSCommand<P extends MigrateVDSCommandParameters> extends VdsBrokerCommand<P> {

    public MigrateBrokerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().migrate(createMigrationInfo());
        proceedProxyReturnValue();
    }

    private Map<String, String> createMigrationInfo() {
        Map<String, String> migrationInfo = new HashMap<>();

        P parameters = getParameters();
        migrationInfo.put(VdsProperties.vm_guid, parameters.getVmId().toString());
        migrationInfo.put(VdsProperties.src, String.format("%1$s", parameters.getSrcHost()));
        migrationInfo.put(VdsProperties.dst, String.format("%1$s", parameters.getDstHost()));
        migrationInfo.put(VdsProperties.method,
                VdsProperties.migrationMethodtoString(parameters.getMigrationMethod()));

        if (FeatureSupported.tunnelMigration(parameters.getClusterVersion())) {
            migrationInfo.put(VdsProperties.TUNNELED, Boolean.toString(parameters.isTunnelMigration()));
        }

        if (StringUtils.isNotBlank(parameters.getDstQemu())) {
            migrationInfo.put(VdsProperties.DST_QEMU, parameters.getDstQemu());
        }
        if (Config.<Boolean> getValue(ConfigValues.AbortMigrationOnError, parameters.getClusterVersion().getValue())) {
            migrationInfo.put("abortOnError", Boolean.TRUE.toString());
        }

        if (parameters.getMigrationDowntime() != 0) {
            migrationInfo.put(VdsProperties.MIGRATION_DOWNTIME, Integer.toString(parameters.getMigrationDowntime()));
        }

        if (parameters.getAutoConverge() != null) {
            migrationInfo.put(VdsProperties.AUTO_CONVERGE, parameters.getAutoConverge().toString());
        }

        if (parameters.getMigrateCompressed() != null) {
            migrationInfo.put(VdsProperties.MIGRATE_COMPRESSED, parameters.getMigrateCompressed().toString());
        }

        return migrationInfo;
    }
}

package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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

    private Map<String, Object> createMigrationInfo() {
        Map<String, Object> migrationInfo = new HashMap<>();

        P parameters = getParameters();
        migrationInfo.put(VdsProperties.vm_guid, parameters.getVmId().toString());
        migrationInfo.put(VdsProperties.src, String.format("%1$s", parameters.getSrcHost()));
        migrationInfo.put(VdsProperties.dst, String.format("%1$s", parameters.getDstHost()));
        migrationInfo.put(VdsProperties.method, parameters.getMigrationMethod().name().toLowerCase());
        migrationInfo.put(VdsProperties.TUNNELED, Boolean.toString(parameters.isTunnelMigration()));
        migrationInfo.put("abortOnError", Boolean.TRUE.toString());

        if (StringUtils.isNotBlank(parameters.getDstQemu())) {
            migrationInfo.put(VdsProperties.DST_QEMU, parameters.getDstQemu());
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

        if (parameters.getMigrateEncrypted() != null) {
            migrationInfo.put(VdsProperties.MIGRATE_ENCRYPTED, parameters.getMigrateEncrypted());
        }

        if (parameters.getConsoleAddress() != null) {
            migrationInfo.put(VdsProperties.CONSOLE_ADDRESS, parameters.getConsoleAddress());
        }

        if (parameters.getMaxBandwidth() != null) {
            migrationInfo.put(VdsProperties.MIGRATION_BANDWIDTH, parameters.getMaxBandwidth());
        }

        if (parameters.getConvergenceSchedule() != null) {
            migrationInfo.put(VdsProperties.MIGRATION_CONVERGENCE_SCHEDULE, parameters.getConvergenceSchedule());
        }

        if (parameters.isEnableGuestEvents() != null) {
            migrationInfo.put(VdsProperties.ENABLE_GUEST_EVENTS, parameters.isEnableGuestEvents());
        }

        if (parameters.getMaxIncomingMigrations() != null) {
            migrationInfo.put(VdsProperties.MIGRATION_INCOMING_LIMIT, parameters.getMaxIncomingMigrations());
        }

        if (parameters.getMaxOutgoingMigrations() != null) {
            migrationInfo.put(VdsProperties.MIGRATION_OUTGOING_LIMIT, parameters.getMaxOutgoingMigrations());
        }

        return migrationInfo;
    }
}

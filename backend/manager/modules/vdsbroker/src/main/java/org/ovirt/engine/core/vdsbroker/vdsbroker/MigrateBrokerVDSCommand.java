package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;
import java.util.Map;
import java.util.HashMap;

public class MigrateBrokerVDSCommand<P extends MigrateVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Map<String, String> migrationInfo;

    public MigrateBrokerVDSCommand(P parameters) {
        super(parameters);
        String migMethod = VdsProperties.MigrationMethostoString(parameters.getMigrationMethod());
        log.infoFormat("VdsBroker::migrate::Entered (vm_guid='{0}', srcHost='{1}', dstHost='{2}',  method='{3}'",
                parameters.getVmId().toString(), parameters.getSrcHost(), parameters.getDstHost(), migMethod);
        migrationInfo = new HashMap<String, String>();
        migrationInfo.put(VdsProperties.vm_guid, parameters.getVmId().toString());
        migrationInfo.put(VdsProperties.src, String.format("%1$s", parameters.getSrcHost()));
        migrationInfo.put(VdsProperties.dst, String.format("%1$s", parameters.getDstHost()));
        migrationInfo.put(VdsProperties.method, migMethod);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().migrate(migrationInfo);
        ProceedProxyReturnValue();
    }

    private static LogCompat log = LogFactoryCompat.getLog(MigrateBrokerVDSCommand.class);
}

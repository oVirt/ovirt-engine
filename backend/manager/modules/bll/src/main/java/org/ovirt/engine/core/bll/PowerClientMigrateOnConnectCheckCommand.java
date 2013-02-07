package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.PowerClientMigrateOnConnectCheckParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetPowerClientByClientInfoParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class PowerClientMigrateOnConnectCheckCommand<T extends PowerClientMigrateOnConnectCheckParameters> extends
        MigrateVmCommand<T> {
    public PowerClientMigrateOnConnectCheckCommand(T parameters) {
        super(parameters);
        super.setVdsId(parameters.getVdsId());
        if (getPowerClient() != null) {
            getVdsSelector().setDestinationVdsId(getPowerClient().getId());
            getVdsSelector().setCheckDestinationFirst(true);
        }
    }

    private VDS _powerClient;

    private VDS getPowerClient() {
        if (_powerClient == null) {
            VdcQueryReturnValue ret = Backend.getInstance().runInternalQuery(VdcQueryType.GetPowerClient,
                    new GetPowerClientByClientInfoParameters(getParameters().getClientIp()));
            if (ret != null && ret.getSucceeded()) {
                Object retvalue = ret.getReturnValue();
                if (retvalue != null && retvalue instanceof VDS) {
                    _powerClient = (VDS) retvalue;
                }
            }
        }
        return _powerClient;
    }

    @Override
    protected void executeCommand() {
        PowerClientMigrateOnConnectCheck();
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        final boolean powerClientAutoMigrateToPowerClientOnConnect = Config.<Boolean>
        GetValue(ConfigValues.PowerClientAutoMigrateToPowerClientOnConnect);
        final boolean powerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient =
            Config.<Boolean> GetValue(ConfigValues
                    .PowerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient);

        // check if any chance we need to do something (reduce code and prevent
        // from getting exceptions on optional code...)
        if (!powerClientAutoMigrateToPowerClientOnConnect
                && !powerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient) {
            addCanDoActionMessage(VdcBllMessages.AUTO_MIGRATE_DISABLED);
            returnValue = false;
        } else if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.AUTO_MIGRATE_VDS_NOT_FOUND);
            returnValue = false;
        } else if (powerClientAutoMigrateToPowerClientOnConnect
                && !powerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient
                && getPowerClient() == null) {
            addCanDoActionMessage(VdcBllMessages.AUTO_MIGRATE_POWERCLIENT_NOT_FOUND);
            returnValue = false;
        } else if (getPowerClient() != null
                && powerClientAutoMigrateToPowerClientOnConnect
                && getPowerClient().getId().equals(getVdsId())) {
            getReturnValue().getCanDoActionMessages()
                    .add(VdcBllMessages.AUTO_MIGRATE_ALREADY_ON_POWERCLIENT.toString());
            returnValue = false;
        } else if (powerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient) {
            log.infoFormat("VdcBll.PowerClientMigrateOnConnectCheck - checking PowerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient");
            if (getVds().getVdsType() != VDSType.PowerClient) {
                addCanDoActionMessage(VdcBllMessages.AUTO_MIGRATE_ALREADY_RUNNING_ON_VDS);
                returnValue = false;
            } else if (getPowerClient() != null && getVdsId().equals(getPowerClient().getId())) {
                // check if not a case that already running locally, but
                // PowerClientAutoMigrateToPowerClientOnConnect was false, so we
                // still got here
                addCanDoActionMessage(VdcBllMessages.AUTO_MIGRATE_UNSUCCESSFUL);
                returnValue = false;
            }
        }

        return returnValue ? super.canDoAction() : false;
    }

    private void PowerClientMigrateOnConnectCheck() {
        // logic to migrate to current client, if possible
        if (getPowerClient() != null
                && Config.<Boolean> GetValue(ConfigValues.PowerClientAutoMigrateToPowerClientOnConnect)) {
            // check if we can run (migrate) the vm to the power client.

            Guid checkVds_id = getVdsSelector().getVdsToRunOn(true);
            if (!(checkVds_id).equals(getPowerClient().getId())) {
                log.infoFormat("VdcBll.PowerClientMigrateOnConnectCheck - Can't migrate to power client, since getVdsToRunOn rejected the run");
                // return; // no return, so we can continue and migrate from a
                // power client to a VDS if needed in next section
            } else {
                log.infoFormat("VdcBll.PowerClientMigrateOnConnectCheck - Migrating the VM to the power client");
                setVdsDestinationId(getPowerClient().getId());
                _destinationVds = getPowerClient();
                super.executeVmCommand();
                return;
            }
        }
        // not a power client, or could not migrate to local power client to
        // migrate from a power client to a VDS, if applicable

        if (Config
                .<Boolean> GetValue(ConfigValues.PowerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient)) {
            log.infoFormat("VdcBll.PowerClientMigrateOnConnectCheck - Client is not a power client, so we got here to migrate from the power client the VM is currently running on");
            log.infoFormat("VdcBll.PowerClientMigrateOnConnectCheck - Migrating the VM to a VDS");
            super.executeVmCommand();
        }

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.VM_MIGRATION_ON_CONNECT_CHECK_SUCCEEDED
                : AuditLogType.VM_MIGRATION_ON_CONNECT_CHECK_FAILED;
    }

    private static Log log = LogFactory.getLog(PowerClientMigrateOnConnectCheckCommand.class);
}

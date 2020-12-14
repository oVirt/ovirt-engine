package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.FipsMode;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.ClusterDao;

@NonTransactiveCommandAttribute
public class HandleVdsFipsCommand <T extends VdsActionParameters> extends VdsCommand<T>{

    @Inject
    private ClusterDao clusterDao;

    public HandleVdsFipsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getVds() == null) {
            return failValidation(EngineMessage.VDS_INVALID_SERVER_ID);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        if (!FeatureSupported.isFipsModeSupported(getCluster().getCompatibilityVersion())) {
            setSucceeded(true);
            return;
        }
        if (getCluster().getFipsMode() == FipsMode.UNDEFINED) {
            // cluster is not configured yet, initiate by the first VDS
            getCluster().setFipsMode(getVds().isFipsEnabled() ? FipsMode.ENABLED : FipsMode.DISABLED);
            log.info("Updating FIPS mode configuration of the cluster {} to {}",
                     getCluster().getName(), getVds().isFipsEnabled());

            clusterDao.update(getCluster());
        } else if (getCluster().getFipsMode() != map(getVds().isFipsEnabled())) {
            addCustomValue("VdsFips", String.valueOf(getVds().isFipsEnabled()));
            addCustomValue("ClusterFips", getCluster().getFipsMode().name());

            SetNonOperationalVdsParameters params = new SetNonOperationalVdsParameters(getVdsId(),
                    NonOperationalReason.FIPS_INCOMPATIBLE_WITH_CLUSTER);

            runInternalAction(ActionType.SetNonOperationalVds,
                    params,
                    ExecutionHandler.createInternalJobContext(getContext()));
        }
        setSucceeded(true);
    }

    private FipsMode map(boolean hostFipsEnabled) {
        if(hostFipsEnabled) {
            return FipsMode.ENABLED;
        } else {
            return FipsMode.DISABLED;
        }
    }
}

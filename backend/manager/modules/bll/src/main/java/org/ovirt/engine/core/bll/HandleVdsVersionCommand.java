package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
public class HandleVdsVersionCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    public HandleVdsVersionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
            result = false;
        } else if (getVds().getstatus() == VDSStatus.Problematic || getVds().getstatus() == VDSStatus.NonResponsive) {
            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_CHECK_VERSION_HOST_NON_RESPONSIVE);
            result = false;
        }
        return result;
    }

    @Override
    protected void executeCommand() {
        VDSGroup cluster = DbFacade.getInstance().getVdsGroupDAO().get(getVds().getvds_group_id());

        // get only major and minor of vdc version
        Version partialVdcVersion = new Version(
                new Version(Config.<String> GetValue(ConfigValues.VdcVersion)).toString(2));
        // check that vdc support vds OR vds support vdc
        boolean vdsmVdcVersionSupported = Config.<java.util.HashSet<Version>> GetValue(
                ConfigValues.SupportedVDSMVersions).contains(getVds().getVersion().getPartialVersion())
                || (!StringHelper.isNullOrEmpty(getVds().getsupported_engines()) && getVds()
                        .getSupportedENGINESVersionsSet().contains(partialVdcVersion));

        // move to non operational if vds-vdc version not supported OR cluster
        // version is not supported
        if (!vdsmVdcVersionSupported || !VersionSupport.checkClusterVersionSupported(cluster.getcompatibility_version(), getVds())) {
            SetNonOperationalVdsParameters tempVar = new SetNonOperationalVdsParameters(getVdsId(),
                    NonOperationalReason.VERSION_INCOMPATIBLE_WITH_CLUSTER);
            tempVar.setSaveToDb(true);
            Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar);
        }
        setSucceeded(true);
    }
}

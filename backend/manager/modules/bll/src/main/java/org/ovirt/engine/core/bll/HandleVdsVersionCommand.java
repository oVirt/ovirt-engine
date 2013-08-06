package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
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
        } else if (getVds().getStatus() == VDSStatus.Connecting || getVds().getStatus() == VDSStatus.NonResponsive) {
            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_CHECK_VERSION_HOST_NON_RESPONSIVE);
            result = false;
        }
        return result;
    }

    @Override
    protected void executeCommand() {
        VDS vds = getVds();
        VDSGroup cluster = getVdsGroup();

        // get only major and minor of vdc version
        Version partialVdcVersion = new Version(
                new Version(Config.<String> GetValue(ConfigValues.VdcVersion)).toString(2));
        // check that vdc support vds OR vds support vdc
        RpmVersion vdsVersion = vds.getVersion();
        Version vdsmVersion = new Version(vdsVersion.getMajor(),vdsVersion.getMinor());

        // move to non operational if vds-vdc version not supported OR cluster
        // version is not supported
        if (!Config.<HashSet<Version>> GetValue(ConfigValues.SupportedVDSMVersions).contains(vdsmVersion)) {
            reportNonOperationReason(NonOperationalReason.VERSION_INCOMPATIBLE_WITH_CLUSTER,
                                     Config.<HashSet<Version>> GetValue(ConfigValues.SupportedVDSMVersions).toString(),
                                     vdsmVersion.toString());
        }
        else if (!VersionSupport.checkClusterVersionSupported(cluster.getcompatibility_version(), vds)) {
            reportNonOperationReason(NonOperationalReason.CLUSTER_VERSION_INCOMPATIBLE_WITH_CLUSTER,
                                     cluster.getcompatibility_version().toString(),
                                     vds.getSupportedClusterLevels().toString());
        }
        setSucceeded(true);
    }

    private void reportNonOperationReason(NonOperationalReason reason, String compatibleVersions,
                                          String vdsSupportedVersions) {
        Map<String, String> customLogValues = new HashMap<>();
        customLogValues.put("CompatibilityVersion", compatibleVersions);
        customLogValues.put("VdsSupportedVersions", vdsSupportedVersions);
        SetNonOperationalVdsParameters tempVar = new SetNonOperationalVdsParameters(getVdsId(),
                reason,
                customLogValues);
        tempVar.setSaveToDb(true);
        Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar,  ExecutionHandler.createInternalJobContext());
    }
}

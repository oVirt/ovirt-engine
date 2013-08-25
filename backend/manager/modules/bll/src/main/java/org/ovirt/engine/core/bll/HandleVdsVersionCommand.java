package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.businessentities.NonOperationalReason.VERSION_INCOMPATIBLE_WITH_CLUSTER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
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

        // check that vdc support vds OR vds support vdc
        RpmVersion vdsVersion = vds.getVersion();
        Version vdsmVersion = new Version(vdsVersion.getMajor(),vdsVersion.getMinor());
        boolean vdsmVersionSupported =
                Config.<HashSet<Version>> GetValue(ConfigValues.SupportedVDSMVersions).contains(vdsmVersion);
        if (!vdsmVersionSupported && !StringUtils.isEmpty(vds.getSupportedEngines())) {
            try {
                vdsmVersionSupported = vds.getSupportedENGINESVersionsSet().contains(partialVdcVersion);
            } catch (RuntimeException e) {
                log.error(e.getMessage());
            }
        }

        // move to non operational if vds-vdc version not supported OR cluster
        // version is not supported
        if (!vdsmVersionSupported
                || !VersionSupport.checkClusterVersionSupported(cluster.getcompatibility_version(), vds)) {
            Map<String, String> customLogValues = new HashMap<String, String>();
            customLogValues.put("CompatibilityVersion", cluster.getcompatibility_version().toString());
            customLogValues.put("VdsSupportedVersions", vds.getSupportedClusterLevels());
            SetNonOperationalVdsParameters tempVar = new SetNonOperationalVdsParameters(getVdsId(),
                    VERSION_INCOMPATIBLE_WITH_CLUSTER,
                    customLogValues);
            tempVar.setSaveToDb(true);
            Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar,  ExecutionHandler.createInternalJobContext());
        }
        setSucceeded(true);
    }
}

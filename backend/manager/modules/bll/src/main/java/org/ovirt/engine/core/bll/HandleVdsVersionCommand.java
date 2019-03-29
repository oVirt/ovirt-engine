package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.SupportedHostFeatureDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class HandleVdsVersionCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    @Inject
    private ClusterFeatureDao clusterFeatureDao;

    @Inject
    private SupportedHostFeatureDao hostFeatureDao;

    public HandleVdsVersionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getVds() == null) {
            return failValidation(EngineMessage.VDS_INVALID_SERVER_ID);
        }
        if (getVds().getStatus() == VDSStatus.Connecting || getVds().getStatus() == VDSStatus.NonResponsive) {
            return failValidation(EngineMessage.VDS_CANNOT_CHECK_VERSION_HOST_NON_RESPONSIVE);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        VDS vds = getVds();
        Cluster cluster = getCluster();
        boolean isEngineSupportedByVdsm = false;

        // partialVdcVersion will hold the engine's version (minor and major parts),
        // this will be compared to vdsm supported engines to see if vdsm can be added
        // to cluster
        Version partialVdcVersion = new Version(new Version(Config.getValue(ConfigValues.VdcVersion)).toString(2));
        RpmVersion vdsVersion = vds.getVersion();
        Version vdsmVersion = new Version(vdsVersion.getMajor(), vdsVersion.getMinor());
        if (!StringUtils.isEmpty(vds.getSupportedEngines())) {
            isEngineSupportedByVdsm = vds.getSupportedENGINESVersionsSet().contains(partialVdcVersion);
        }

        // If vdsm doesn't support the engine's version (engine's version is not included
        // vdsm supprtedEngineVersions list) we move on and check if engine
        // and cluster supports the specific vdsm version. which is sufficient
        if (!isEngineSupportedByVdsm &&
                !Config.<Set<Version>> getValue(ConfigValues.SupportedVDSMVersions).contains(vdsmVersion)) {
            reportNonOperationReason(NonOperationalReason.VERSION_INCOMPATIBLE_WITH_CLUSTER,
                    Config.<Set<Version>> getValue(ConfigValues.SupportedVDSMVersions).toString(),
                                     vdsmVersion.toString());
        } else if (!VersionSupport.checkClusterVersionSupported(cluster.getCompatibilityVersion(), vds)) {
            reportNonOperationReason(NonOperationalReason.CLUSTER_VERSION_INCOMPATIBLE_WITH_CLUSTER,
                                     cluster.getCompatibilityVersion().toString(),
                                     vds.getSupportedClusterLevels());
        } else {
            checkClusterAdditionalFeaturesSupported(cluster, vds);
        }
        setSucceeded(true);
    }

    private void checkClusterAdditionalFeaturesSupported(Cluster cluster, VDS vds) {
        Set<SupportedAdditionalClusterFeature> clusterSupportedFeatures =
                clusterFeatureDao.getAllByClusterId(cluster.getId());
        Set<String> hostSupportedFeatures =
                hostFeatureDao.getSupportedHostFeaturesByHostId(vds.getId());
        for (SupportedAdditionalClusterFeature feature : clusterSupportedFeatures) {
            if (feature.isEnabled() && !hostSupportedFeatures.contains(feature.getFeature().getName())) {
                Map<String, String> customLogValues = new HashMap<>();
                customLogValues.put("UnSupportedFeature", feature.getFeature().getName());
                reportNonOperationReason(NonOperationalReason.HOST_FEATURES_INCOMPATIBILE_WITH_CLUSTER, customLogValues);
                return;
            }
        }
    }

    private void reportNonOperationReason(NonOperationalReason reason, String compatibleVersions,
                                          String vdsSupportedVersions) {
        Map<String, String> customLogValues = new HashMap<>();
        customLogValues.put("CompatibilityVersion", compatibleVersions);
        customLogValues.put("VdsSupportedVersions", vdsSupportedVersions);
        reportNonOperationReason(reason, customLogValues);
    }

    private void reportNonOperationReason(NonOperationalReason reason, Map<String, String> customLogValues) {
        SetNonOperationalVdsParameters tempVar = new SetNonOperationalVdsParameters(getVdsId(),
                reason,
                customLogValues);
        runInternalAction(ActionType.SetNonOperationalVds, tempVar,  ExecutionHandler.createInternalJobContext(getContext()));
    }
}

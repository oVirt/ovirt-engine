package org.ovirt.engine.core.bll;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.FencingPolicyHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;

public class FenceProxyLocator {

    private static final Logger log = LoggerFactory.getLogger(FenceProxyLocator.class);

    private final VDS _vds;
    private FencingPolicy fencingPolicy;

    public FenceProxyLocator(VDS _vds) {
        super();
        this._vds = _vds;
    }

    public FenceProxyLocator(VDS vds, FencingPolicy fencingPolicy) {
        this(vds);
        this.fencingPolicy = fencingPolicy;
    }

    public boolean isProxyHostAvailable() {
        return findProxyHost(false) != null;
    }

    public VDS findProxyHost() {
        return findProxyHost(false, null);
    }

    public VDS findProxyHost(boolean withRetries) {
        return findProxyHost(withRetries, null);
    }

    public VDS findProxyHost(boolean withRetries, Guid excludedHostId) {
        // make sure that loop is executed at least once , no matter what is the
        // value in config
        int retries = Math.max(Config.<Integer> getValue(ConfigValues.FindFenceProxyRetries), 1);
        int delayInMs = 1000 * Config.<Integer> getValue(ConfigValues.FindFenceProxyDelayBetweenRetriesInSec);
        VDS proxyHost = null;
        // get PM Proxy preferences or use defaults if not defined
        for (PMProxyOptions proxyOption : getPmProxyPreferences()) {
            proxyHost = chooseBestProxy(proxyOption, excludedHostId);
            int count = 0;
            // If can not find a proxy host retry and delay between retries as configured.
            while (proxyHost == null && withRetries && count < retries) {
                log.warn("Attempt {} to find fence proxy host failed...", ++count);
                try {
                    Thread.sleep(delayInMs);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                proxyHost = chooseBestProxy(proxyOption, excludedHostId);
            }
            if (proxyHost != null) {
                break;
            }
        }
        if (proxyHost == null) {
            log.error("Failed to run Power Management command on Host {}, no running proxy Host was found.",
                    _vds.getName());
            return null;
        }
        return proxyHost;
    }


    private PMProxyOptions[] getPmProxyPreferences() {
        String pmProxyPreferences = (StringUtils.isEmpty(_vds.getPmProxyPreferences()))
                ?
                Config.<String> getValue(ConfigValues.FenceProxyDefaultPreferences)
                : _vds.getPmProxyPreferences();
        String[] pmProxyOptions = pmProxyPreferences.split(",");
        PMProxyOptions[] proxyOptions = new PMProxyOptions[pmProxyOptions.length];
        for (int i = 0; i < pmProxyOptions.length; i++) {
            proxyOptions[i] = getProxyOption(pmProxyOptions[i]);
        }
        return proxyOptions;
    }

    private VDS chooseBestProxy(PMProxyOptions proxyOption, Guid excludedHostId) {
        List<VDS> hosts = DbFacade.getInstance().getVdsDao().getAll();
        Version minSupportedVersion = null;
        if (fencingPolicy != null) {
            minSupportedVersion = FencingPolicyHelper.getMinimalSupportedVersion(fencingPolicy);
        }
        Iterator<VDS> iterator = hosts.iterator();
        while (iterator.hasNext()) {
            VDS host = iterator.next();
            if (host.getId().equals(_vds.getId())
                    || host.getId().equals(excludedHostId)
                    || !matchesOption(host, proxyOption)
                    || !areAgentsVersionCompatible(host)
                    || (fencingPolicy != null && !isFencingPolicySupported(_vds, minSupportedVersion))
                    || isHostNetworkUnreacable(host)) {
                iterator.remove();
            }
        }
        for (VDS host : hosts) {
            if (host.getStatus() == VDSStatus.Up) {
                return host;
            }
        }
        return hosts.size() == 0 ? null : hosts.get(0);
    }

    private boolean matchesOption(VDS host, PMProxyOptions proxyOption) {
        if (proxyOption == PMProxyOptions.CLUSTER) {
            return host.getVdsGroupId().equals(_vds.getVdsGroupId());
        }
        if (proxyOption == PMProxyOptions.DC) {
            return host.getStoragePoolId().equals(_vds.getStoragePoolId());
        }
        if (proxyOption == PMProxyOptions.OTHER_DC) {
            return !host.getStoragePoolId().equals(_vds.getStoragePoolId());
        }
        return false;
    }

    private boolean areAgentsVersionCompatible(VDS vds) {
        VdsFenceOptions options = new VdsFenceOptions(vds.getVdsGroupCompatibilityVersion().getValue());
        boolean supported = true;
        for (FenceAgent agent : vds.getFenceAgents()) {
            supported = supported && options.isAgentSupported(agent.getType());
        }
        return supported;
    }

    private boolean isFencingPolicySupported(VDS vds, Version minimalSupportedVersion) {
        return vds.getSupportedClusterVersionsSet().contains(minimalSupportedVersion);
    }

    private boolean isHostNetworkUnreacable(VDS vds) {
        VdsDynamic vdsDynamic = vds.getDynamicData();
        return (vdsDynamic.getStatus() == VDSStatus.Down
                || vdsDynamic.getStatus() == VDSStatus.Reboot
                || vdsDynamic.getStatus() == VDSStatus.Kdumping
                || vdsDynamic.getStatus() == VDSStatus.NonResponsive
                || (vdsDynamic.getStatus() == VDSStatus.NonOperational
        && vdsDynamic.getNonOperationalReason() == NonOperationalReason.NETWORK_UNREACHABLE));
    }

    private PMProxyOptions getProxyOption(String pmProxyOption) {
        if (pmProxyOption.equalsIgnoreCase(PMProxyOptions.CLUSTER.name())) {
            return PMProxyOptions.CLUSTER;
        }
        else if (pmProxyOption.equalsIgnoreCase(PMProxyOptions.DC.name())) {
            return PMProxyOptions.DC;
        }
        else if (pmProxyOption.equalsIgnoreCase(PMProxyOptions.OTHER_DC.name())) {
            return PMProxyOptions.OTHER_DC;
        } else {
            log.error("Illegal value in PM Proxy Preferences string {}, skipped.", pmProxyOption);
            return null;
        }
    }

    private enum PMProxyOptions {
        CLUSTER,
        DC,
        OTHER_DC;
    };
}

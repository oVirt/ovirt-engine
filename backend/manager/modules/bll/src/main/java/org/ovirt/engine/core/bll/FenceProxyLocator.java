package org.ovirt.engine.core.bll;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.FencingPolicyHelper;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        for (FenceProxySourceType fenceProxySource : getFenceProxySources()) {
            proxyHost = chooseBestProxy(fenceProxySource, excludedHostId);
            int count = 0;
            // If can not find a proxy host retry and delay between retries as configured.
            while (proxyHost == null && withRetries && count < retries) {
                log.warn("Attempt {} to find fence proxy host failed...", ++count);
                try {
                    Thread.sleep(delayInMs);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                proxyHost = chooseBestProxy(fenceProxySource, excludedHostId);
            }
            if (proxyHost != null) {
                break;
            }
        }
        if (proxyHost == null) {
            log.error("Can not run Power Management command on Host {}, no suitable proxy Host was found.",
                    _vds.getName());
            return null;
        }
        return proxyHost;
    }

    private List<FenceProxySourceType> getFenceProxySources() {
        List<FenceProxySourceType> fenceProxySources = _vds.getFenceProxySources();
        if (CollectionUtils.isEmpty(fenceProxySources)) {
            fenceProxySources = FenceProxySourceTypeHelper.parseFromString(
                    Config.<String> getValue(ConfigValues.FenceProxyDefaultPreferences));
        }
        return fenceProxySources;
    }

    private VDS chooseBestProxy(FenceProxySourceType fenceProxySource, Guid excludedHostId) {
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
                    || !matchesOption(host, fenceProxySource)
                    || !areAgentsVersionCompatible(host)
                    || (fencingPolicy != null && !isFencingPolicySupported(host, minSupportedVersion))
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

    private boolean matchesOption(VDS host, FenceProxySourceType fenceProxySource) {
        boolean matches = false;
        switch (fenceProxySource) {
            case CLUSTER:
                matches = host.getVdsGroupId().equals(_vds.getVdsGroupId());
                break;

            case DC:
                matches = host.getStoragePoolId().equals(_vds.getStoragePoolId());
                break;

            case OTHER_DC:
                matches = !host.getStoragePoolId().equals(_vds.getStoragePoolId());
        }
        return matches;
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
                || vdsDynamic.getStatus() == VDSStatus.PendingApproval
                || (vdsDynamic.getStatus() == VDSStatus.NonOperational
        && vdsDynamic.getNonOperationalReason() == NonOperationalReason.NETWORK_UNREACHABLE));
    }

    public FencingPolicy getFencingPolicy() {
        return fencingPolicy;
    }

    public void setFencingPolicy(FencingPolicy fencingPolicy) {
        this.fencingPolicy = fencingPolicy;
    }
}

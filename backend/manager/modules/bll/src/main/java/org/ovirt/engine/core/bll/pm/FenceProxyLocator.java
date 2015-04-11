package org.ovirt.engine.core.bll.pm;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.FencingPolicyHelper;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It manages selection of fence proxy for specified host and fencing policy
 */
public class FenceProxyLocator {
    private static final Logger log = LoggerFactory.getLogger(FenceProxyLocator.class);

    private final VDS fencedHost;
    private FencingPolicy fencingPolicy;

    public FenceProxyLocator(VDS fencedHost) {
        this(fencedHost, null);
    }

    public FenceProxyLocator(VDS fencedHost, FencingPolicy fencingPolicy) {
        this.fencedHost = fencedHost;
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
        int retries = getFindFenceProxyRetries();
        long delayInMs = getDelayBetweenRetries();
        VDS proxyHost = null;
        // get PM Proxy preferences or use defaults if not defined
        for (FenceProxySourceType fenceProxySource : getFenceProxySources()) {
            proxyHost = selectBestProxy(fenceProxySource, excludedHostId);
            int count = 0;
            // If can not find a proxy host retry and delay between retries as configured.
            while (proxyHost == null && withRetries && count < retries) {
                log.warn("Attempt {} to find fence proxy for host '{}' failed...",
                        ++count,
                        fencedHost.getHostName());
                ThreadUtils.sleep((int) delayInMs);
                proxyHost = selectBestProxy(fenceProxySource, excludedHostId);
            }
            if (proxyHost != null) {
                break;
            }
        }
        if (proxyHost == null) {
            log.error("Can not run fence action on host '{}', no suitable proxy host was found.",
                    fencedHost.getName());
            return null;
        }
        return proxyHost;
    }

    protected List<FenceProxySourceType> getFenceProxySources() {
        List<FenceProxySourceType> fenceProxySources = fencedHost.getFenceProxySources();
        if (CollectionUtils.isEmpty(fenceProxySources)) {
            fenceProxySources = getDefaultFenceProxySources();
        }
        return fenceProxySources;
    }

    protected VDS selectBestProxy(FenceProxySourceType fenceProxySource, Guid excludedHostId) {
        Version minSupportedVersion = getMinSupportedVersionForFencingPolicy();
        List<VDS> proxyCandidates = getDbFacade().getVdsDao().getAll();
        Iterator<VDS> iterator = proxyCandidates.iterator();
        while (iterator.hasNext()) {
            VDS proxyCandidate = iterator.next();
            log.debug("Evaluating host '{}'", proxyCandidate.getHostName());
            if (proxyCandidate.getId().equals(fencedHost.getId())
                    || isHostExcluded(proxyCandidate, excludedHostId)
                    || !isHostFromSelectedSource(proxyCandidate, fenceProxySource)
                    || !areAgentsVersionCompatible(proxyCandidate)
                    || !isFencingPolicySupported(proxyCandidate, minSupportedVersion)
                    || isHostNetworkUnreachable(proxyCandidate)) {
                iterator.remove();
            }
        }
        for (VDS proxyCandidate : proxyCandidates) {
            if (proxyCandidate.getStatus() == VDSStatus.Up) {
                return proxyCandidate;
            }
        }
        return proxyCandidates.size() == 0 ? null : proxyCandidates.get(0);
    }

    protected boolean isHostExcluded(VDS proxyCandidate, Guid excludedHostId) {
        boolean excluded = proxyCandidate.getId().equals(excludedHostId);

        log.debug("Proxy candidate '{}' was excluded intentionally: {}",
                proxyCandidate.getHostName(),
                excluded);
        return excluded;
    }

    private boolean isHostFromSelectedSource(VDS proxyCandidate, FenceProxySourceType fenceProxySource) {
        boolean fromSelectedSource = false;
        switch (fenceProxySource) {
            case CLUSTER:
                fromSelectedSource = proxyCandidate.getVdsGroupId().equals(fencedHost.getVdsGroupId());
                break;

            case DC:
                fromSelectedSource = proxyCandidate.getStoragePoolId().equals(fencedHost.getStoragePoolId());
                break;

            case OTHER_DC:
                fromSelectedSource = !proxyCandidate.getStoragePoolId().equals(fencedHost.getStoragePoolId());
                break;
        }

        log.debug("Proxy candidate '{}' matches proxy source '{}': {}",
                proxyCandidate.getHostName(),
                fenceProxySource,
                fromSelectedSource);
        return fromSelectedSource;
    }

    protected boolean areAgentsVersionCompatible(VDS proxyCandidate) {
        VdsFenceOptions options = createVdsFenceOptions(proxyCandidate.getVdsGroupCompatibilityVersion().getValue());
        boolean compatible = true;
        for (FenceAgent agent : fencedHost.getFenceAgents()) {
            if (!options.isAgentSupported(agent.getType())) {
                compatible = false;
                break;
            }
        }

        log.debug("Proxy candidate '{}' has compatible fence agents: {}",
                proxyCandidate.getHostName(),
                compatible);
        return compatible;
    }

    protected boolean isFencingPolicySupported(VDS proxyCandidate, Version minimalSupportedVersion) {
        boolean supported = fencingPolicy == null
                || proxyCandidate.getSupportedClusterVersionsSet().contains(minimalSupportedVersion);

        log.debug("Proxy candidate '{}' supports fencing policy '{}': {}",
                proxyCandidate.getHostName(),
                fencingPolicy,
                supported);
        return supported;
    }

    protected boolean isHostNetworkUnreachable(VDS proxyCandidate) {
        boolean unreachable = proxyCandidate.getStatus() == VDSStatus.Down
                || proxyCandidate.getStatus() == VDSStatus.Reboot
                || proxyCandidate.getStatus() == VDSStatus.Kdumping
                || proxyCandidate.getStatus() == VDSStatus.NonResponsive
                || proxyCandidate.getStatus() == VDSStatus.PendingApproval
                || (proxyCandidate.getStatus() == VDSStatus.NonOperational
                        && proxyCandidate.getNonOperationalReason() == NonOperationalReason.NETWORK_UNREACHABLE);

        log.debug("Proxy candidate '{}' with status '{}' is unreachable: {}",
                proxyCandidate.getHostName(),
                proxyCandidate.getStatus(),
                unreachable);
        return unreachable;
    }

    protected List<FenceProxySourceType> getDefaultFenceProxySources() {
        return FenceProxySourceTypeHelper.parseFromString(
                Config.<String>getValue(ConfigValues.FenceProxyDefaultPreferences));
    }

    protected int getFindFenceProxyRetries() {
        // make sure that loop is executed at least once , no matter what is the value in config
        return Math.max(Config.<Integer>getValue(ConfigValues.FindFenceProxyRetries), 1);
    }

    protected long getDelayBetweenRetries() {
        return TimeUnit.SECONDS.toMillis(
                Config.<Integer>getValue(ConfigValues.FindFenceProxyDelayBetweenRetriesInSec));
    }

    protected Version getMinSupportedVersionForFencingPolicy() {
        return fencingPolicy == null ? null : FencingPolicyHelper.getMinimalSupportedVersion(fencingPolicy);
    }

    protected VdsFenceOptions createVdsFenceOptions(String version) {
        return new VdsFenceOptions(version);
    }

    // TODO Investigate if injection is possible
    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }
}

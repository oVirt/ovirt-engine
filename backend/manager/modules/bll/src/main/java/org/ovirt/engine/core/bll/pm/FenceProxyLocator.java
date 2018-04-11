package org.ovirt.engine.core.bll.pm;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.di.Injector;
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
                ThreadUtils.sleep(delayInMs);
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
        return Injector.get(VdsDao.class).getAll().stream()
                .peek(vds -> log.debug("Evaluating host '{}'", vds.getHostName()))
                .filter(vds -> !Objects.equals(vds.getId(), fencedHost.getId()))
                .filter(vds -> !isHostExcluded(vds, excludedHostId))
                .filter(vds -> isHostFromSelectedSource(vds, fenceProxySource))
                .filter(this::areAgentsVersionCompatible)
                .filter(vds -> isFencingPolicySupported(vds, getMinSupportedVersionForFencingPolicy()))
                .filter(vds -> !isHostNetworkUnreachable(vds))
                .sorted(Comparator.comparingInt(vds -> vds.getStatus() == VDSStatus.Up ? -1 : 1))
                .findFirst()
                .orElse(null);
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
                fromSelectedSource = proxyCandidate.getClusterId().equals(fencedHost.getClusterId());
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
        VdsFenceOptions options = createVdsFenceOptions(proxyCandidate.getClusterCompatibilityVersion().getValue());
        boolean compatible = true;
        for (FenceAgent agent : Injector.get(FenceAgentDao.class).getFenceAgentsForHost(fencedHost.getId())) {
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
        boolean supported = minimalSupportedVersion == null;
        if (!supported) {
            for (Version version : proxyCandidate.getSupportedClusterVersionsSet()) {
                if (version.compareTo(minimalSupportedVersion) >= 0) {
                    supported = true;
                    break;
                }
            }
        }
        log.debug("Proxy candidate '{}' supports fencing policy '{}': {}",
                proxyCandidate.getHostName(),
                fencingPolicy,
                supported);
        return supported;
    }

    protected boolean isHostNetworkUnreachable(VDS proxyCandidate) {
        boolean unreachable = proxyCandidate.getStatus() != VDSStatus.Up
                && (proxyCandidate.getStatus() != VDSStatus.NonOperational
                        || proxyCandidate.getNonOperationalReason() == NonOperationalReason.NETWORK_UNREACHABLE);

        log.debug("Proxy candidate '{}' with status '{}' is unreachable: {}",
                proxyCandidate.getHostName(),
                proxyCandidate.getStatus(),
                unreachable);
        return unreachable;
    }

    protected List<FenceProxySourceType> getDefaultFenceProxySources() {
        return FenceProxySourceTypeHelper.parseFromString(
                Config.getValue(ConfigValues.FenceProxyDefaultPreferences));
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
}

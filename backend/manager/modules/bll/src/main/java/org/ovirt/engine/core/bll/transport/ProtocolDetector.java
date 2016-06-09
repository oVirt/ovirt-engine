package org.ovirt.engine.core.bll.transport;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We need to detect whether vdsm supports jsonrpc or only xmlrpc. It is confusing to users
 * when they have cluster 3.5+ and connect to vdsm <3.5 which supports only xmlrpc.
 * In order to present version information in such situation we need fallback to xmlrpc.
 * In cluster 3.5 we support both xmlrpc and jsonrpc. Since cluster 3.6 we support only jsonrpc.
 * Therefore if engine fails to communicate with host >= 3.5 in cluster >= 3.6, we'll move host
 * to non-operational, due to incompatibility protocol level.
 */
public class ProtocolDetector implements AutoCloseable {

    private static Logger log = LoggerFactory.getLogger(ProtocolDetector.class);
    private Integer connectionTimeout = null;
    private Integer retryAttempts = null;
    private final ResourceManager resourceManager;
    private final VdsStaticDao vdsStaticDao;
    private final AuditLogDirector auditLogDirector;
    private boolean fallbackTriggered;
    private VDS vds;

    public ProtocolDetector(VDS vds,
                            ResourceManager resourceManager,
                            VdsStaticDao vdsStaticDao,
                            AuditLogDirector auditLogDirector) {
        this.vds = vds;
        this.retryAttempts = Config.<Integer> getValue(ConfigValues.ProtocolFallbackRetries);
        this.connectionTimeout = Config.<Integer> getValue(ConfigValues.ProtocolFallbackTimeoutInMilliSeconds);
        this.resourceManager = resourceManager;
        this.vdsStaticDao = vdsStaticDao;
        this.auditLogDirector = auditLogDirector;
    }

    /**
     * Attempts to connect to vdsm using a proxy from {@code VdsManager} for a host.
     * There are 3 attempts to connect.
     *
     * @return <code>true</code> if connected or <code>false</code> if connection failed.
     */
    public boolean attemptConnection() {
        boolean connected = false;
        long timeout = Config.<Integer> getValue(ConfigValues.SetupNetworksPollingTimeout);
        for (int i = 0; i < this.retryAttempts; i++) {
            try {
                if (i != 0) {
                    Thread.sleep(this.connectionTimeout);
                }
                FutureVDSCall<VDSReturnValue> task =
                        resourceManager.runFutureVdsCommand(FutureVDSCommandType.TimeBoundPoll,
                                new TimeBoundPollVDSCommandParameters(vds.getId(), timeout, TimeUnit.SECONDS));
                VDSReturnValue returnValue = task.get(timeout, TimeUnit.SECONDS);
                connected = returnValue.getSucceeded();
                if (connected) {
                    break;
                }
            } catch (TimeoutException | InterruptedException | XmlRpcRunTimeException ignored) {
            } catch (Exception e) {
                log.warn("Failed to connect to host", e.getMessage());
                log.debug("Exception", e);
            }
        }
        return connected;
    }

    /**
     * Stops {@code VdsManager} for a host.
     */
    public void stopConnection() {
        resourceManager.removeVds(this.vds.getId());
    }

    /**
     * Fall back the protocol and attempts the connection {@link ProtocolDetector#attemptConnection()}.
     *
     * @return <code>true</code> if connected or <code>false</code> if connection failed.
     */
    public boolean attemptFallbackProtocol() {
        vds.setProtocol(VdsProtocol.XML);
        resourceManager.addVds(vds, false);
        return attemptConnection();
    }

    /**
     * Updates DB with fall back protocol (xmlrpc).
     */
    public void setFallbackProtocol() {
        log.info("Fallback for XML-RPC protocol for host '{}' ({})", vds.getName(), vds.getId());
        setFallbackProtocol(VdsProtocol.XML);
        fallbackTriggered = true;
    }

    private void setFallbackProtocol(VdsProtocol protocol) {
        final VdsStatic vdsStatic = this.vds.getStaticData();
        vdsStatic.setProtocol(protocol);
        TransactionSupport.executeInNewTransaction(() -> {
            vdsStaticDao.update(vdsStatic);
            return null;
        });
    }

    /**
     * @return {@code true} if fallback should be invoked for new host's installation, where the host capabilities are
     *         not known yet to the engine
     */
    public boolean shouldCheckProtocolTofallback() {
        return vds.getHostOs() == null && VdsProtocol.STOMP == vds.getProtocol();
    }

    @Override
    public void close() throws Exception {
        if (fallbackTriggered) {
            setFallbackProtocol(VdsProtocol.STOMP);

            // warn if host supports jsonrpc in cluster which supports only jsonrpc, and fallback was triggered
            if (vds.getClusterCompatibilityVersion().greaterOrEquals(Version.v3_6)) {
                // Report an error for protocol incompatibility
                AuditLogableBase event = new AuditLogableBase();
                event.setVds(vds);
                auditLogDirector.log(event, AuditLogType.HOST_PROTOCOL_INCOMPATIBLE_WITH_CLUSTER);
            }
        }
    }
}

package org.ovirt.engine.core.bll.transport;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

/**
 * We need to detect whether vdsm supports jsonrpc or only xmlrpc. It is confusing to users
 * when they have cluster 3.5+ and connect to vdsm <3.5 which supports only xmlrpc.
 * In order to present version information in such situation we need fallback to xmlrpc.
 *
 */
public class ProtocolDetector {

    private Integer connectionTimeout = null;
    private Integer retryAttempts = null;

    private VDS vds;

    public ProtocolDetector(VDS vds) {
        this.vds = vds;
        this.retryAttempts = Config.<Integer> getValue(ConfigValues.ProtocolFallbackRetries);
        this.connectionTimeout = Config.<Integer> getValue(ConfigValues.ProtocolFallbackTimeoutInMilliSeconds);
    }

    /**
     * Attempts to connect to vdsm using a proxy from {@code VdsManager} for a host.
     * There are 3 attempts to connect.
     *
     * @return <code>true</code> if connected or <code>false</code> if connection failed.
     */
    public boolean attemptConnection() {
        boolean connected = false;
        try {
            for (int i = 0; i < this.retryAttempts; i++) {
                long timeout = Config.<Integer> getValue(ConfigValues.SetupNetworksPollingTimeout);
                FutureVDSCall<VDSReturnValue> task =
                        Backend.getInstance().getResourceManager().runFutureVdsCommand(FutureVDSCommandType.TimeBoundPoll,
                                new TimeBoundPollVDSCommandParameters(vds.getId(), timeout, TimeUnit.SECONDS));
                VDSReturnValue returnValue =
                        task.get(timeout, TimeUnit.SECONDS);
                connected = returnValue.getSucceeded();
                if (connected) {
                    break;
                }
                Thread.sleep(this.connectionTimeout);
            }
        } catch (TimeoutException | InterruptedException ignored) {
        }
        return connected;
    }

    /**
     * Stops {@code VdsManager} for a host.
     */
    public void stopConnection() {
        ResourceManager.getInstance().RemoveVds(this.vds.getId());
    }

    /**
     * Fall back the protocol and attempts the connection {@link ProtocolDetector#attemptConnection()}.
     *
     * @return <code>true</code> if connected or <code>false</code> if connection failed.
     */
    public boolean attemptFallbackProtocol() {
        vds.setProtocol(VdsProtocol.XML);
        ResourceManager.getInstance().AddVds(vds, false);
        return attemptConnection();
    }

    /**
     * Updates DB with fall back protocol (xmlrpc).
     */
    public void setFallbackProtocol() {
        final VdsStatic vdsStatic = this.vds.getStaticData();
        vdsStatic.setProtocol(VdsProtocol.XML);
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                DbFacade.getInstance().getVdsStaticDao().update(vdsStatic);
                return null;
            }
        });
    }
}

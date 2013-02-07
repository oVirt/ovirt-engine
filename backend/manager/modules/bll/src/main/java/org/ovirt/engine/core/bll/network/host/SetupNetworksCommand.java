package org.ovirt.engine.core.bll.network.host;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.SetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@SuppressWarnings("serial")
@NonTransactiveCommandAttribute
public class SetupNetworksCommand<T extends SetupNetworksParameters> extends VdsCommand<T> {

    /** Time between polling attempts, to prevent flooding the host/network. */
    private static final long POLLING_BREAK = 500;
    private static final List<VDSStatus> SUPPORTED_HOST_STATUSES =
            Arrays.asList(VDSStatus.Maintenance, VDSStatus.Up, VDSStatus.NonOperational);
    private static Log log = LogFactory.getLog(SetupNetworksCommand.class);
    private SetupNetworksHelper helper;

    public SetupNetworksCommand(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__SETUP);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORKS);
    }

    @Override
    protected boolean canDoAction() {
        VDS vds = getVds();

        if (vds == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
            return false;
        }

        if (!SUPPORTED_HOST_STATUSES.contains(vds.getStatus())) {
            addCanDoActionMessage(VdcBllMessages.VAR__HOST_STATUS__UP_MAINTENANCE_OR_NON_OPERATIONAL);
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
            return false;
        }

        helper = new SetupNetworksHelper(getParameters(), vds.getVdsGroupId());
        List<String> validationMesseges = helper.validate();

        if (!validationMesseges.isEmpty()) {
            for (String msg : validationMesseges) {
                addCanDoActionMessage(msg);
            }
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        if (noChangesDetected()) {
            setSucceeded(true);
            return;
        }

        T bckndCmdParams = getParameters();
        final SetupNetworksVdsCommandParameters vdsCmdParams = new SetupNetworksVdsCommandParameters(
                getVdsId(),
                getNetworks(),
                getRemovedNetworks(),
                getBonds(),
                getRemovedBonds(),
                getInterfaces());
        vdsCmdParams.setForce(bckndCmdParams.isForce());
        vdsCmdParams.setCheckConnectivity(bckndCmdParams.isCheckConnectivity());

        int timeout =
                bckndCmdParams.getConectivityTimeout() != null ? bckndCmdParams.getConectivityTimeout()
                        : Config.<Integer> GetValue(ConfigValues.NetworkConnectivityCheckTimeoutInSeconds);
        vdsCmdParams.setConectivityTimeout(timeout);

        FutureVDSCall<VDSReturnValue> setupNetworksTask = createFutureTask(vdsCmdParams);

        if (bckndCmdParams.isCheckConnectivity()) {
            pollInterruptively(setupNetworksTask);
        }

        try {
            VDSReturnValue retVal = setupNetworksTask.get(timeout, TimeUnit.SECONDS);
            if (retVal != null) {
                VdsHandler.handleVdsResult(retVal);

                if (retVal.getSucceeded()) {
                    setSucceeded(TransactionSupport.executeInNewTransaction(updateVdsNetworksInTx(bckndCmdParams)));
                }
            }
        } catch (TimeoutException e) {
            log.debugFormat("Setup networks command timed out for {0} seconds", timeout);
        }
    }

    private boolean noChangesDetected() {
        return getNetworks().isEmpty() && getRemovedNetworks().isEmpty()
                && getBonds().isEmpty() && getRemovedBonds().isEmpty();
    }

    private List<VdsNetworkInterface> getInterfaces() {
        return getParameters().getInterfaces();
    }

    private Set<String> getRemovedBonds() {
        return helper.getRemovedBonds();
    }

    private List<VdsNetworkInterface> getBonds() {
        return helper.getBonds();
    }

    private List<String> getRemovedNetworks() {
        return helper.getRemoveNetworks();
    }

    private List<Network> getNetworks() {
        return helper.getNetworks();
    }

    /**
     * use FutureTask to poll the vdsm (with getCapabilities) while setupNetworks task is not done. during the poll task
     * try to fetch the setupNetwork task answer with a timeout equal to getConnectitivtyTimeout defined in the command
     * parameters and stop both tasks when its done
     *
     * @param setupNetworksTask
     * @param timeout
     */
    private void pollInterruptively(final FutureVDSCall<VDSReturnValue> setupNetworksTask) {
        while (!setupNetworksTask.isDone()) {
            pollVds();
        }
    }

    /**
     * update the new VDSM networks to DB in new transaction.
     *
     * @param bckndCmdParams
     * @return
     */
    private TransactionMethod<Boolean> updateVdsNetworksInTx(final T bckndCmdParams) {
        return new TransactionMethod<Boolean>() {

            @Override
            public Boolean runInTransaction() {
                // save the new network topology to DB
                Backend.getInstance().getResourceManager()
                        .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                                new VdsIdAndVdsVDSCommandParametersBase(bckndCmdParams.getVdsId()));

                // Update cluster networks (i.e. check if need to activate each new network)
                for (Network net : getNetworks()) {
                    NetworkClusterHelper.setStatus(getVdsGroupId(), net);
                }
                return Boolean.TRUE;
            }
        };
    }

    private void pollVds() {
        long timeBeforePoll = System.currentTimeMillis();
        FutureVDSCall<VDSReturnValue> task =
                Backend.getInstance().getResourceManager().runFutureVdsCommand(FutureVDSCommandType.Poll,
                        new VdsIdVDSCommandParametersBase(getVds().getId()));
        try {
            task.get(Config.<Integer> GetValue(ConfigValues.SetupNetworksPollingTimeout), TimeUnit.SECONDS);

            if (System.currentTimeMillis() - timeBeforePoll < POLLING_BREAK) {
                Thread.sleep(POLLING_BREAK);
            }
        } catch (Exception e) {
            // ignore failure. network can go down due to VDSM changing the network
        }
    }

    private FutureVDSCall<VDSReturnValue> createFutureTask(final SetupNetworksVdsCommandParameters vdsCmdParams) {
        return Backend.getInstance()
                .getResourceManager()
                .runFutureVdsCommand(FutureVDSCommandType.SetupNetworks, vdsCmdParams);
    }

}

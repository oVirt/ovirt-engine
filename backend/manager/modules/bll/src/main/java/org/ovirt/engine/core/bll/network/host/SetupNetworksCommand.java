package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.SetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class SetupNetworksCommand<T extends SetupNetworksParameters> extends VdsCommand<T> {

    public static enum SETUP_NETWORKS_RESOLUTION {
        NO_CHANGES_DETECTED;
    };

    /** Time between polling attempts, to prevent flooding the host/network. */
    private static final long POLLING_BREAK = 500;
    private static final List<VDSStatus> SUPPORTED_HOST_STATUSES =
            Arrays.asList(VDSStatus.Maintenance, VDSStatus.Up, VDSStatus.NonOperational);
    private static final Logger log = LoggerFactory.getLogger(SetupNetworksCommand.class);
    private SetupNetworksHelper helper;

    public SetupNetworksCommand(T parameters) {
        this(parameters, null);
    }

    public SetupNetworksCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
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

        if (!(SUPPORTED_HOST_STATUSES.contains(vds.getStatus()) || (vds.getStatus() == VDSStatus.Installing && isInternalExecution()))) {
            addCanDoActionMessage(VdcBllMessages.VAR__HOST_STATUS__UP_MAINTENANCE_OR_NON_OPERATIONAL);
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
            return false;
        }

        helper = new SetupNetworksHelper(getParameters(), vds);
        List<String> validationMessages = helper.validate();

        if (!validationMessages.isEmpty()) {
            for (String msg : validationMessages) {
                addCanDoActionMessage(msg);
            }
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        if (noChangesDetected()) {
            if (!getModifiedInterfaces().isEmpty()) {
                updateModifiedInterfaces();
            }

            log.info("No changes were detected in setup networks for host '{}' (ID '{}')",
                    getVdsName(),
                    getVdsId());

            if (isInternalExecution()) {
                setActionReturnValue(SETUP_NETWORKS_RESOLUTION.NO_CHANGES_DETECTED);
            }

            setSucceeded(true);
            return;
        }

        T bckndCmdParams = getParameters();
        final SetupNetworksVdsCommandParameters vdsCmdParams = new SetupNetworksVdsCommandParameters(
                getVdsId(),
                getNetworks(),
                getRemovedNetworks(),
                getBonds(),
                getRemovedBonds().keySet(),
                getInterfaces());
        vdsCmdParams.setForce(bckndCmdParams.isForce());
        vdsCmdParams.setCheckConnectivity(bckndCmdParams.isCheckConnectivity());

        int timeout =
                bckndCmdParams.getConectivityTimeout() != null ? bckndCmdParams.getConectivityTimeout()
                        : Config.<Integer> getValue(ConfigValues.NetworkConnectivityCheckTimeoutInSeconds);
        vdsCmdParams.setConectivityTimeout(timeout);

        FutureVDSCall<VDSReturnValue> setupNetworksTask = createFutureTask(vdsCmdParams);

        if (bckndCmdParams.isCheckConnectivity()) {
            pollInterruptively(setupNetworksTask);
        }

        try {
            VDSReturnValue retVal = setupNetworksTask.get(timeout, TimeUnit.SECONDS);
            if (retVal != null) {
                if (!retVal.getSucceeded() && retVal.getVdsError() == null && getParameters().isCheckConnectivity()) {
                    throw new VdcBLLException(VdcBllErrors.SETUP_NETWORKS_ROLLBACK, retVal.getExceptionString());
                }

                VdsHandler.handleVdsResult(retVal);

                if (retVal.getSucceeded()) {
                    setSucceeded(TransactionSupport.executeInNewTransaction(updateVdsNetworksInTx()));
                }
            }
        } catch (TimeoutException e) {
            log.debug("Setup networks command timed out for {} seconds", timeout);
        }
    }

    private void updateModifiedInterfaces() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<T>() {

            @Override
            public T runInTransaction() {
                getDbFacade().getInterfaceDao().massUpdateInterfacesForVds(getModifiedInterfaces());
                return null;
            }
        });
    }

    private boolean noChangesDetected() {
        return getNetworks().isEmpty() && getRemovedNetworks().isEmpty()
                && getBonds().isEmpty() && getRemovedBonds().isEmpty();
    }

    private List<VdsNetworkInterface> getInterfaces() {
        return getParameters().getInterfaces();
    }

    private Map<String, VdsNetworkInterface> getRemovedBonds() {
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

    private List<VdsNetworkInterface> getModifiedInterfaces() {
        return helper.getModifiedInterfaces();
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
     * @return
     */
    private TransactionMethod<Boolean> updateVdsNetworksInTx() {
        return new TransactionMethod<Boolean>() {

            @Override
            public Boolean runInTransaction() {
                // save the new network topology to DB
                List<VdsNetworkInterface> ifaces = new ArrayList<>(getInterfaces());
                ifaces.addAll(getRemovedBonds().values());
                runVdsCommand(VDSCommandType.CollectVdsNetworkData,
                                new CollectHostNetworkDataVdsCommandParameters(getVds(), ifaces));

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
            task.get(Config.<Integer> getValue(ConfigValues.SetupNetworksPollingTimeout), TimeUnit.SECONDS);

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

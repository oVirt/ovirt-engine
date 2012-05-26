package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.businessentities.VDSStatus.Maintenance;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
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

@NonTransactiveCommandAttribute
public class SetupNetworksCommand<T extends SetupNetworksParameters> extends VdsCommand<T> {

    private static Log log = LogFactory.getLog(SetupNetworksCommand.class);
    private SetupNetworksHelper helper;

    public SetupNetworksCommand(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected boolean canDoAction() {
        VDS vds = getVds();

        if (vds == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
            return false;
        }

        if (vds.getstatus() != Maintenance) {
            addCanDoActionMessage(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE);
            return false;
        }

        helper = new SetupNetworksHelper(getParameters(), vds.getvds_group_id());
        List<VdcBllMessages> validationMesseges = helper.validate();

        if (!validationMesseges.isEmpty()) {
            for (VdcBllMessages msg : validationMesseges) {
                addCanDoActionMessage(msg);
            }
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
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
        vdsCmdParams.setConectivityTimeout(bckndCmdParams.getConectivityTimeout());

        FutureVDSCall<VDSReturnValue> setupNetworksTask = createFutureTask(vdsCmdParams);

        if (bckndCmdParams.isCheckConnectivity()) {
            pollInterruptively(setupNetworksTask);
        }

        long timeout =
                bckndCmdParams.getConectivityTimeout() > 0 ? bckndCmdParams.getConectivityTimeout()
                        : Config.<Integer> GetValue(ConfigValues.vdsTimeout);
        try {
            VDSReturnValue retVal = setupNetworksTask.get(timeout, TimeUnit.SECONDS);
            if (retVal != null && retVal.getSucceeded()) {
                setSucceeded(TransactionSupport.executeInNewTransaction(updateVdsNetworksInTx(bckndCmdParams)));
            }
        } catch (TimeoutException e) {
            log.debugFormat("Setup networks command timed out for {0} seconds", timeout);
        }
    }

    private List<VdsNetworkInterface> getInterfaces() {
        return getParameters().getInterfaces();
    }

    private List<VdsNetworkInterface> getRemovedBonds() {
        return helper.getRemovedBonds();
    }

    private List<VdsNetworkInterface> getBonds() {
        return helper.getBonds();
    }

    private List<String> getRemovedNetworks() {
        return helper.getRemoveNetworks();
    }

    private List<network> getNetworks() {
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
                for (network net : getNetworks()) {
                    AttachNetworkToVdsGroupCommand.SetNetworkStatus(getVdsGroupId(), net);
                }
                return Boolean.TRUE;
            }
        };
    }

    private void pollVds() {
        FutureVDSCall<VDSReturnValue> task =
                Backend.getInstance().getResourceManager().runFutureVdsCommand(FutureVDSCommandType.Poll,
                        new VdsIdVDSCommandParametersBase(getVds().getId()));
        try {
            log.debugFormat("polling host {0}", getVdsName());
            task.get(Config.<Integer> GetValue(ConfigValues.SetupNetworksPollingTimeout), TimeUnit.SECONDS);
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

package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.common.action.SetupNetworksParameters;
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
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
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
        boolean retVal = true;

        if (getParameters().getConectivityTimeout() < 0) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CONNECTIVITY_TIMEOUT_NEGATIVE);
            retVal = false;
        }

        if (retVal && networksOrInterfacesEmpty()) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NO_NETWORKS_OR_INTERFACES);
            retVal = false;
        }

        if (retVal) {
            retVal = checkNetworksValidity();
        }

        if (retVal) {
            retVal = checkBondsValidity();
        }

        return retVal;
    }


    private boolean networksOrInterfacesEmpty() {
        T params = getParameters();
        return params.getNetworks().isEmpty() &&
                params.getRemovedNetworks().isEmpty() &&
                params.getBonds().isEmpty() &&
                params.getRemovedBonds().isEmpty();
    }

    private boolean checkBondsValidity() {
        boolean retVal = true;

        // Check that the same bond not exists in the removedBonds list
        T params = getParameters();
        if (params.getRemovedBonds().size() > 0) {
            for (VdsNetworkInterface bond : params.getBonds()) {
                for (VdsNetworkInterface removedBond : params.getRemovedBonds()) {
                    if (bond.getName().equals(removedBond.getName())) {
                        addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_EXISTS_IN_ADD_AND_REMOVE);
                        retVal = false;
                        break;
                    }
                }
            }
        }

        if (retVal) {
            boolean ifaceExists = false;
            // Check that bond have at least one VdsNetworkInterface
            for (VdsNetworkInterface bond : params.getBonds()) {
                for (VdsNetworkInterface i : params.getInterfaces()) {
                    if (bond.getName().equals(i.getBondName())) {
                        ifaceExists = true;
                        break;
                    }
                }
                if (!ifaceExists) {
                    addCanDoActionMessage(VdcBllMessages.NETWORK_NO_INTERFACE_FOR_BOND);
                    addCanDoActionMessage(String.format("$BondName %1$s", bond.getName()));
                    retVal = false;
                    break;
                }
            }
        }

        return retVal;
    }

    private boolean checkNetworksValidity() {
        boolean retVal = true;

        // Check that the same network not exists in the removedNetworks list
        T params = getParameters();
        if (params.getRemovedNetworks().size() > 0) {
            for (network net : params.getNetworks()) {
                for (network removedNet : params.getRemovedNetworks()) {
                    if (net.getname().equals(removedNet.getname())) {
                        addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_EXISTS_IN_ADD_AND_REMOVE);
                        retVal = false;
                        break;
                    }
                }
            }
        }

        if (retVal) {
            // Check that networks have at least one VdsNetworkInterface or bond
            for (network net : params.getNetworks()) {
                boolean ifaceExists = false;
                for (VdsNetworkInterface i : params.getInterfaces()) {
                    if (net.getname().equals(i.getNetworkName())) {
                        ifaceExists = true;
                        break;
                    }
                }
                if (ifaceExists) {
                    continue;
                }
                for (VdsNetworkInterface bond : params.getBonds()) {
                    if (net.getname().equals(bond.getNetworkName())) {
                        ifaceExists = true;
                        break;
                    }
                }
                if (!ifaceExists) {
                    addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_HAVE_NO_INERFACES);
                    addCanDoActionMessage(String.format("$NetworkName %1$s", net.getname()));
                    retVal = false;
                    break;
                }
            }
        }

        return retVal;
    }

    @Override
    protected void executeCommand() {
        final T bckndCmdParams = getParameters();
        final SetupNetworksVdsCommandParameters vdsCmdParams = new SetupNetworksVdsCommandParameters(
                getVdsId(),
                bckndCmdParams.getNetworks(),
                bckndCmdParams.getRemovedNetworks(),
                bckndCmdParams.getBonds(),
                bckndCmdParams.getRemovedBonds(),
                bckndCmdParams.getInterfaces());
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
                        new VdsIdAndVdsVDSCommandParametersBase(getVds()));
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

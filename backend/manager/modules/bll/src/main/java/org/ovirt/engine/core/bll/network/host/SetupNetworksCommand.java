package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidatorResolver;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.SetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UserConfiguredNetworkData;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostNetworkTopologyPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaced by {@link org.ovirt.engine.core.bll.network.host.HostSetupNetworksCommand}
 */
@Deprecated
@NonTransactiveCommandAttribute
public class SetupNetworksCommand<T extends SetupNetworksParameters> extends VdsCommand<T> {

    @Inject
    private HostNetworkTopologyPersister hostNetworkTopologyPersister;

    @Inject
    private NetworkExclusivenessValidatorResolver networkExclusivenessValidatorResolver;

    public static enum SetupNetworksResolution {
        NO_CHANGES_DETECTED;
    };

    private static final List<VDSStatus> SUPPORTED_HOST_STATUSES =
            Arrays.asList(VDSStatus.Maintenance, VDSStatus.Up, VDSStatus.NonOperational);
    private static final Logger log = LoggerFactory.getLogger(SetupNetworksCommand.class);

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

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
        addValidationMessage(EngineMessage.VAR__ACTION__SETUP);
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORKS);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.HOST_NETWORK,
                        EngineMessage.ACTION_TYPE_FAILED_SETUP_NETWORKS_IN_PROGRESS));
    }

    @Override
    protected boolean validate() {
        VDS vds = getVds();

        if (vds == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
            return false;
        }

        if (!(SUPPORTED_HOST_STATUSES.contains(vds.getStatus()) || (vds.getStatus() == VDSStatus.Installing && isInternalExecution()))) {
            addValidationMessage(EngineMessage.VAR__HOST_STATUS__UP_MAINTENANCE_OR_NON_OPERATIONAL);
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
            return false;
        }

        helper = new SetupNetworksHelper(
                getParameters(),
                vds,
                managementNetworkUtil,
                networkExclusivenessValidatorResolver);

        List<String> validationMessages = helper.validate();

        if (!validationMessages.isEmpty()) {
            for (String msg : validationMessages) {
                addValidationMessage(msg);
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
                setActionReturnValue(SetupNetworksResolution.NO_CHANGES_DETECTED);
            }

            setSucceeded(true);
            return;
        }

        T bckndCmdParams = getParameters();
        final SetupNetworksVdsCommandParameters vdsCmdParams = new SetupNetworksVdsCommandParameters(
                getVds(),
                getNetworks(),
                getRemovedNetworks(),
                getBonds(),
                getRemovedBonds().keySet(),
                getInterfaces(),
                getParameters().getCustomProperties(),
                isManagementNetworkChanged());
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
                    throw new EngineException(EngineError.SETUP_NETWORKS_ROLLBACK, retVal.getExceptionString());
                }

                VdsHandler.handleVdsResult(retVal);

                if (retVal.getSucceeded()) {
                    try (EngineLock monitoringLock = acquireMonitorLock()) {
                        VDSReturnValue returnValue =
                                runVdsCommand(VDSCommandType.GetCapabilities,
                                        new VdsIdAndVdsVDSCommandParametersBase(getVds()));
                        VDS updatedHost = (VDS) returnValue.getReturnValue();
                        persistNetworkChanges(updatedHost);
                    }

                    setSucceeded(true);
                }
            }
        } catch (TimeoutException e) {
            log.debug("Setup networks command timed out for {} seconds", timeout);
        }
    }

    private void updateModifiedInterfaces() {
        TransactionSupport.executeInNewTransaction(() -> {
            getDbFacade().getInterfaceDao().massUpdateInterfacesForVds(getModifiedInterfaces());
            return null;
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
     */
    private void pollInterruptively(final FutureVDSCall<VDSReturnValue> setupNetworksTask) {
        HostSetupNetworkPoller poller = new HostSetupNetworkPoller();
        while (!setupNetworksTask.isDone()) {
            poller.poll(getVdsId());
        }
    }

    private void persistNetworkChanges(final VDS updatedHost) {
        TransactionSupport.executeInNewTransaction(() -> {

            List<VdsNetworkInterface> ifaces = new ArrayList<>(getInterfaces());
            ifaces.addAll(getRemovedBonds().values());
            UserConfiguredNetworkData userConfiguredNetworkData =
                    new UserConfiguredNetworkData(Collections.<NetworkAttachment> emptyList(), ifaces,
                        getParameters().getCustomProperties());

            // save the new network topology to DB
            hostNetworkTopologyPersister.persistAndEnforceNetworkCompliance(updatedHost,
                    false,
                    userConfiguredNetworkData);

            getVdsDynamicDao().updateNetConfigDirty(getVds().getId(), getVds().getNetConfigDirty());
            for (Network net : getNetworks()) {
                NetworkClusterHelper.setStatus(getVdsGroupId(), net);
            }

            return null;
        });
    }

    private FutureVDSCall<VDSReturnValue> createFutureTask(final SetupNetworksVdsCommandParameters vdsCmdParams) {
        return getVdsBroker().runFutureVdsCommand(FutureVDSCommandType.SetupNetworks, vdsCmdParams);
    }

    private boolean isManagementNetworkChanged(){
        String mgmtNetworkName = managementNetworkUtil.getManagementNetwork(getVds().getVdsGroupId()).getName();
        for (Network netowrk : getNetworks()) {
            if (mgmtNetworkName.equals(netowrk.getName())){
                return true;
            }
        }
        for (VdsNetworkInterface bond : getBonds()) {
            if (mgmtNetworkName.equals(bond.getNetworkName())){
                return true;
            }
        }
        return false;
    }
}

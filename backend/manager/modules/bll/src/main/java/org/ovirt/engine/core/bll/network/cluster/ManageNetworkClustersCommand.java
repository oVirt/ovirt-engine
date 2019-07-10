package org.ovirt.engine.core.bll.network.cluster;

import static org.ovirt.engine.core.common.action.ActionType.AttachNetworkToCluster;
import static org.ovirt.engine.core.common.action.ActionType.DetachNetworkToCluster;
import static org.ovirt.engine.core.common.action.ActionType.UpdateNetworkOnCluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public final class ManageNetworkClustersCommand extends CommandBase<ManageNetworkClustersParameters> {

    @Inject
    @Named
    private Predicate<NetworkCluster> becomingManagementNetworkPredicate;

    @Inject
    @Named
    private Function<NetworkCluster, AttachNetworkToClusterParameter> networkClusterToAttachNetworkToClusterParameterTransformer;

    @Inject
    @Named
    private Function<NetworkCluster, NetworkClusterParameters> networkClusterParameterTransformer;

    @Inject
    private AttachNetworkClusterPermissionsChecker attachPermissionChecker;

    @Inject
    private DetachNetworkClusterPermissionFinder detachPermissionFinder;

    @Inject
    private UpdateNetworkClusterPermissionsChecker updatePermissionChecker;

    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public ManageNetworkClustersCommand(
            ManageNetworkClustersParameters parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        final Boolean dbUpdateResult = TransactionSupport.executeInNewTransaction(() -> {
            final Collection<NetworkCluster> attachments = getParameters().getAttachments();
            final List<NetworkCluster> managementNetworkAttachments =
                    attachments.stream().filter(becomingManagementNetworkPredicate).collect(Collectors.toList());
            final List<NetworkCluster> nonManagementNetworkAttachments =
                    attachments.stream().filter(becomingManagementNetworkPredicate.negate()).collect(Collectors.toList());
            final Collection<NetworkCluster> updates = getParameters().getUpdates();
            final List<NetworkCluster> managementNetworkUpdates =
                    updates.stream().filter(becomingManagementNetworkPredicate).collect(Collectors.toList());
            final List<NetworkCluster> nonManagementNetworkUpdates =
                    updates.stream().filter(becomingManagementNetworkPredicate.negate()).collect(Collectors.toList());

            boolean resultStatus = attachNetworks(managementNetworkAttachments);
            resultStatus = resultStatus && updateNetworkAttachments(managementNetworkUpdates);
            resultStatus = resultStatus && attachNetworks(nonManagementNetworkAttachments);
            resultStatus = resultStatus && updateNetworkAttachments(nonManagementNetworkUpdates);
            resultStatus = resultStatus && detachNetworks(getParameters().getDetachments());

            return resultStatus;
        });

        setSucceeded(dbUpdateResult);

        if (dbUpdateResult) {
            propagateNetworksChanges();
        }
    }

    private boolean attachNetworks(Collection<NetworkCluster> attachments) {
        return runNetworkClusterCommands(
                attachments,
                ActionType.AttachNetworkToClusterInternal,
                networkClusterToAttachNetworkToClusterParameterTransformer);
    }

    private boolean detachNetworks(Collection<NetworkCluster> detachments) {
        return runNetworkClusterCommands(
                detachments,
                ActionType.DetachNetworkFromClusterInternal,
                networkClusterToAttachNetworkToClusterParameterTransformer);
    }

    private boolean updateNetworkAttachments(Collection<NetworkCluster> updates) {
        return runNetworkClusterCommands(
                updates,
                ActionType.UpdateNetworkOnCluster,
                networkClusterParameterTransformer);
    }

    private void propagateNetworksChanges() {
        withRootCommandInfo(getParameters());
        runInternalAction(ActionType.PropagateNetworksToClusterHosts, getParameters());
    }

    private boolean runNetworkClusterCommands(
            Collection<NetworkCluster> networkClusters,
            ActionType actionType,
            Function<NetworkCluster, ? extends ActionParametersBase> networkClusterToParameterTransformer) {
        final List<? extends ActionParametersBase> parameters =
                networkClusters.stream().map(networkClusterToParameterTransformer).collect(Collectors.toList());
        return runMultipleInternalCommandsSynchronously(actionType, parameters);
    }

    /**
     * Runs multiple commands sequentially on the current thread. The execution stops on first failure.
     *
     * @param actionType command type
     * @param parameters commad parameters
     * @return the execution status. <code>true</code> on success and <code>false</code> on faliure.
     */
    private boolean runMultipleInternalCommandsSynchronously(
            ActionType actionType,
            List<? extends ActionParametersBase> parameters) {

        for (ActionParametersBase param : parameters) {
            final ActionReturnValue executionResult = runInternalAction(actionType, param);
            if (!executionResult.getSucceeded()) {
                TransactionSupport.setRollbackOnly();
                propagateFailure(executionResult);
                return false;
            }
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> result = new ArrayList<>();

        for (NetworkCluster attachment : getParameters().getAttachments()) {
            result.addAll(
                    attachPermissionChecker.findPermissionCheckSubjects(
                            attachment,
                            ActionType.AttachNetworkToCluster));
        }
        for (NetworkCluster detachment : getParameters().getDetachments()) {
            result.addAll(
                    detachPermissionFinder.findPermissionCheckSubjects(
                            detachment.getNetworkId(),
                            ActionType.DetachNetworkToCluster));
        }
        for (NetworkCluster update : getParameters().getUpdates()) {
            result.addAll(
                    updatePermissionChecker.findPermissionCheckSubjects(
                            update.getNetworkId(),
                            update.getClusterId(),
                            ActionType.UpdateNetworkOnCluster));
        }
        return result;
    }

    @Override
    protected boolean checkPermissions(final List<PermissionSubject> permSubjects) {
        return checkAttachmentPermissions() && checkDetachmentsPermissions() && checkUpdatesPermissions();
    }

    private boolean checkAttachmentPermissions() {
        for (NetworkCluster attachment : getParameters().getAttachments()) {
            final boolean isUserAllowed = attachPermissionChecker.checkPermissions(
                    this,
                    attachment,
                    AttachNetworkToCluster);
            if (!isUserAllowed) {
                return false;
            }
        }
        return true;
    }

    private boolean checkDetachmentsPermissions() {
        for (NetworkCluster detachment : getParameters().getDetachments()) {
            final List<PermissionSubject> permissionCheckSubjects =
                    detachPermissionFinder.findPermissionCheckSubjects(
                            detachment.getNetworkId(),
                            DetachNetworkToCluster);
            for (PermissionSubject permissionSubject : permissionCheckSubjects) {
                final ArrayList<String> messages = new ArrayList<>();
                final boolean isUserAllowed = checkSinglePermission(permissionSubject, messages);
                if (!isUserAllowed) {
                    getReturnValue().getValidationMessages().addAll(messages);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkUpdatesPermissions() {
        for (NetworkCluster update : getParameters().getUpdates()) {
            final boolean isUserAllowed = updatePermissionChecker.checkPermissions(
                    this,
                    update.getNetworkId(),
                    update.getClusterId(),
                    UpdateNetworkOnCluster);
            if (!isUserAllowed) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean validate() {
        return super.validate() && validate(validateInputForDuplication());
    }

    private ValidationResult validateInputForDuplication() {
        final Set<NetworkClusterId> networkClusterIds = new HashSet<>();
        final Iterable<NetworkCluster> inputNetworkClusters = Stream.of(
                getParameters().getAttachments(),
                getParameters().getDetachments(),
                getParameters().getUpdates()).flatMap(Collection::stream).collect(Collectors.toList());
        for (NetworkCluster networkCluster : inputNetworkClusters) {
            if (networkClusterIds.contains(networkCluster.getId())) {
                final String networkClusterReplacement = String.format("${NetworkCluster} %s", networkCluster.getId());
                return new ValidationResult(
                        EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_NETWORK_CLUSTER_INPUT, networkClusterReplacement);
            } else {
                networkClusterIds.add(networkCluster.getId());
            }
        }
        return ValidationResult.VALID;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}

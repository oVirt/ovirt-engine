package org.ovirt.engine.core.bll.network.cluster;

import static org.ovirt.engine.core.utils.CollectionUtils.nullToEmptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.HostSetupNetworksParametersBuilder;
import org.ovirt.engine.core.bll.network.cluster.transformer.NetworkClustersToSetupNetworksParametersTransformer;
import org.ovirt.engine.core.bll.network.cluster.transformer.NetworkClustersToSetupNetworksParametersTransformerFactory;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class PropagateNetworksToClusterHostsCommand extends CommandBase<ManageNetworkClustersParameters> {
    @Inject
    private NetworkClustersToSetupNetworksParametersTransformerFactory
            networkClustersToSetupNetworksParametersTransformerFactory;

    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public PropagateNetworksToClusterHostsCommand(ManageNetworkClustersParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {

        final Map<Guid, ManageNetworkClustersParameters> paramsByClusterId = mapParametersByClusterId();

        for (ManageNetworkClustersParameters param : paramsByClusterId.values()) {
            processSingleClusterChanges(param);
        }

        setSucceeded(true);
    }

    private void processSingleClusterChanges(ManageNetworkClustersParameters param) {
        final List<ActionParametersBase> setupNetworksParams = new ArrayList<>(
                createNetworkClustersToSetupNetworksParametersTransformer().transform(
                        param.getAttachments(),
                        param.getDetachments(),
                        param.getUpdates())
        );

        HostSetupNetworksParametersBuilder.updateParametersSequencing(setupNetworksParams);
        setupNetworksParams.forEach(this::withRootCommandInfo);
        runInternalMultipleActions(ActionType.PersistentHostSetupNetworks, setupNetworksParams);
    }

    private NetworkClustersToSetupNetworksParametersTransformer createNetworkClustersToSetupNetworksParametersTransformer() {
        return networkClustersToSetupNetworksParametersTransformerFactory.
                createNetworkClustersToSetupNetworksParametersTransformer(getContext());
    }

    private Map<Guid, ManageNetworkClustersParameters> mapParametersByClusterId() {
        ManageNetworkClustersParameters parameters = getParameters();
        Map<Guid, List<NetworkCluster>> attachmentByClusterId = groupByClusterId(parameters.getAttachments());
        Map<Guid, List<NetworkCluster>> detachmentByClusterId = groupByClusterId(parameters.getDetachments());
        Map<Guid, List<NetworkCluster>> updatesByClusterId = groupByClusterId(parameters.getUpdates());

        Set<Guid> clusterIds = Stream.of(attachmentByClusterId, detachmentByClusterId, updatesByClusterId)
                .flatMap(e -> e.keySet().stream())
                .collect(Collectors.toSet());

        return clusterIds
                .stream()
                .collect(Collectors.toMap(Function.identity(), clusterId -> new ManageNetworkClustersParameters(
                        nullToEmptyList(attachmentByClusterId.get(clusterId)),
                        nullToEmptyList(detachmentByClusterId.get(clusterId)),
                        nullToEmptyList(updatesByClusterId.get(clusterId)))));
    }

    private Map<Guid, List<NetworkCluster>> groupByClusterId(Collection<NetworkCluster> attachments) {
        return attachments.stream().collect(Collectors.groupingBy(NetworkCluster::getClusterId));
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}

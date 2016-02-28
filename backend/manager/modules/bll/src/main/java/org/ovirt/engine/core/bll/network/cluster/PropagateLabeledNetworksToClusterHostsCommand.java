package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.HostSetupNetworksParametersBuilder;
import org.ovirt.engine.core.bll.network.cluster.transformer.NetworkClustersToSetupNetworksParametersTransformer;
import org.ovirt.engine.core.bll.network.cluster.transformer.NetworkClustersToSetupNetworksParametersTransformerFactory;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class PropagateLabeledNetworksToClusterHostsCommand extends CommandBase<ManageNetworkClustersParameters> {
    @Inject
    private NetworkClustersToSetupNetworksParametersTransformerFactory
            networkClustersToSetupNetworksParametersTransformerFactory;

    public PropagateLabeledNetworksToClusterHostsCommand(ManageNetworkClustersParameters parameters, CommandContext cmdContext) {
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
        final NetworkClustersToSetupNetworksParametersTransformer
                networkClustersToSetupNetworksParametersTransformer =
                networkClustersToSetupNetworksParametersTransformerFactory.
                        createNetworkClustersToSetupNetworksParametersTransformer(getContext());
        final ArrayList<VdcActionParametersBase> setupNetworksParams = new ArrayList<>();
        setupNetworksParams.addAll(networkClustersToSetupNetworksParametersTransformer.transform(
                param.getAttachments(),
                param.getDetachments()));

        HostSetupNetworksParametersBuilder.updateParametersSequencing(setupNetworksParams);
        runInternalMultipleActions(VdcActionType.PersistentHostSetupNetworks, setupNetworksParams);
    }

    private Map<Guid, ManageNetworkClustersParameters> mapParametersByClusterId() {
        final Map<Guid, ManageNetworkClustersParameters> paramsByClusterId = new HashMap<>();
        final Map<Guid, List<NetworkCluster>> attachmentByClusterId =
                getParameters().getAttachments().stream().collect(Collectors.groupingBy(NetworkCluster::getClusterId));
        final Map<Guid, List<NetworkCluster>> detachmentByClusterId =
                getParameters().getDetachments().stream().collect(Collectors.groupingBy(NetworkCluster::getClusterId));
        for (Entry<Guid, List<NetworkCluster>> singleClusterAttachments: attachmentByClusterId.entrySet()) {
            final Guid clusterId = singleClusterAttachments.getKey();
            final List<NetworkCluster> networkAttachments = singleClusterAttachments.getValue();
            final List<NetworkCluster> networkDetachments;
            if (detachmentByClusterId.containsKey(clusterId)) {
                networkDetachments = detachmentByClusterId.get(clusterId);
            } else {
                networkDetachments = Collections.emptyList();
            }
            paramsByClusterId.put(clusterId, new ManageNetworkClustersParameters(
                    networkAttachments,
                    networkDetachments,
                    Collections.<NetworkCluster>emptyList()));
        }

        for (Entry<Guid, List<NetworkCluster>> singleClusterAttachments: detachmentByClusterId.entrySet()) {
            final Guid clusterId = singleClusterAttachments.getKey();
            final List<NetworkCluster> networkDetachments = singleClusterAttachments.getValue();
            if (!attachmentByClusterId.containsKey(clusterId)) {
                paramsByClusterId.put(
                        clusterId,
                        new ManageNetworkClustersParameters(
                                Collections.<NetworkCluster>emptyList(),
                                networkDetachments,
                                Collections.<NetworkCluster>emptyList()));
            }
        }

        return paramsByClusterId;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}

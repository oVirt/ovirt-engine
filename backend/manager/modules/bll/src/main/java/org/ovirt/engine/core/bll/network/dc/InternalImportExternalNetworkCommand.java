package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.InternalImportExternalNetworkParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class InternalImportExternalNetworkCommand<P extends InternalImportExternalNetworkParameters> extends CommandBase<P> {

    @Inject
    private NetworkHelper networkHelper;

    public InternalImportExternalNetworkCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getDataCenterId());
    }

    protected Network getNetwork() {
        return getParameters().getExternalNetwork();
    }

    @Override
    protected void executeCommand() {
        final Guid dataCenterId = getStoragePoolId();
        final Network network = getNetwork();
        network.setDataCenterId(dataCenterId);

        ActionReturnValue addNetworkReturnValue = addNetwork(dataCenterId, network,
                getParameters().isAttachToAllClusters());
        if (!addNetworkReturnValue.getSucceeded()) {
            propagateFailure(addNetworkReturnValue);
            return;
        }

        network.setId(addNetworkReturnValue.getActionReturnValue());

        getReturnValue().setActionReturnValue(network.getId());
        setSucceeded(true);
    }

    private ActionReturnValue addNetwork(Guid dataCenterId, Network network, boolean attachToAllClusters) {
        AddNetworkStoragePoolParameters params =
                new AddNetworkStoragePoolParameters(dataCenterId, network);

        params.setVnicProfilePublicUse(getParameters().isPublicUse());
        if (attachToAllClusters) {
            params.setNetworkClusterList(networkHelper.createNetworkClusters(
                    getAllClusterIdsInDataCenter(dataCenterId)));
        }

        return runInternalAction(ActionType.AddNetwork, params);
    }

    private List<Guid> getAllClusterIdsInDataCenter(Guid dataCenterId) {
        QueryReturnValue queryReturnValue = runInternalQuery(QueryType.GetClustersByStoragePoolId,
                new IdQueryParameters(dataCenterId));

        List<Cluster> clusters = queryReturnValue.getReturnValue();
        return clusters.stream().map(Cluster::getId).collect(Collectors.toList());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__IMPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORK);
    }
}

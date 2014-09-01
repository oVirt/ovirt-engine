package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.NetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.RemoveNetworksByLabelParametersBuilder;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.ClusterNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class DetachNetworksFromClusterCommand<T extends ClusterNetworksParameters> extends VdsGroupCommandBase<T> {

    public DetachNetworksFromClusterCommand(T parameters) {
        super(parameters);
    }

    public DetachNetworksFromClusterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (AttachNetworkToVdsGroupParameter param : getParameters().getClusterNetworksParameters()) {
                    NetworkClusterHelper helper = new NetworkClusterHelper(param.getNetworkCluster());
                    helper.removeNetworkAndReassignRoles();
                }

                return null;
            }
        });

        if (NetworkHelper.setupNetworkSupported(getVdsGroup().getcompatibility_version())) {
            Set<Network> networks = new HashSet<>();
            Map<Guid, List<Network>> networksByHost = new HashMap<>();
            Map<Guid, List<VdsNetworkInterface>> nicsByHost = new HashMap<>();

            for (AttachNetworkToVdsGroupParameter param : getParameters().getClusterNetworksParameters()) {
                networks.add(getNetworkDAO().get(param.getNetworkCluster().getNetworkId()));
            }

            for (Network network : networks) {
                List<VdsNetworkInterface> nics =
                        getDbFacade().getInterfaceDao().getAllInterfacesByLabelForCluster(getVdsGroupId(),
                                network.getLabel());

                for (VdsNetworkInterface nic : nics) {
                    if (!networksByHost.containsKey(nic.getVdsId())) {
                        networksByHost.put(nic.getVdsId(), new ArrayList<Network>());
                        nicsByHost.put(nic.getVdsId(), new ArrayList<VdsNetworkInterface>());
                    }

                    nicsByHost.get(nic.getVdsId()).add(nic);
                    networksByHost.get(nic.getVdsId()).add(network);
                }
            }

            if (!networksByHost.isEmpty()) {
                removeNetworksFromHosts(networksByHost, nicsByHost);
            }
        }

        setSucceeded(true);
    }

    private void removeNetworksFromHosts(Map<Guid, List<Network>> networksByHost,
            Map<Guid, List<VdsNetworkInterface>> nicsByHost) {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        for (Entry<Guid, List<Network>> entry : networksByHost.entrySet()) {
            RemoveNetworksByLabelParametersBuilder builder = new RemoveNetworksByLabelParametersBuilder(getContext());
            parameters.add(builder.buildParameters(entry.getKey(),
                    entry.getValue(),
                    nicsByHost.get(entry.getKey())));
        }

        NetworkParametersBuilder.updateParametersSequencing(parameters);
        runInternalMultipleActions(VdcActionType.PersistentSetupNetworks, parameters);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}

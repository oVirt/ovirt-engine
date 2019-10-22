/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworkResource;
import org.ovirt.engine.api.resource.openstack.OpenstackSubnetsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackNetworkResource
        extends AbstractBackendActionableResource<OpenStackNetwork, Network>
        implements OpenstackNetworkResource {
    private String providerId;

    protected BackendOpenStackNetworkResource(String providerId, String id) {
        super(id, OpenStackNetwork.class, Network.class);
        this.providerId = providerId;
    }

    @Override
    public OpenStackNetwork get() {
        Network network = getNetwork();
        return addLinks(populate(map(network), network));
    }

    @Override
    protected OpenStackNetwork addParents(OpenStackNetwork network) {
        OpenStackNetworkProvider provider = new OpenStackNetworkProvider();
        provider.setId(providerId);
        network.setOpenstackNetworkProvider(provider);
        return super.addParents(network);
    }

    @Override
    public OpenstackSubnetsResource getSubnetsResource() {
        return inject(new BackendOpenStackSubnetsResource(providerId, id));
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier of an OpenStack image isn't a UUID.
        return null;
    }

    @Override
    public Response doImport(Action action) {
        validateParameters(action, "dataCenter.id|name");

        Guid dataCenterId = getDataCenterId(action);

        ProviderNetwork providerNetwork = new ProviderNetwork(new Guid(providerId), id);

        Network network = new Network();
        network.setProvidedBy(providerNetwork);
        network.setDataCenterId(dataCenterId);
        network.setName(getNetwork().getName());

        AddNetworkStoragePoolParameters parameters = new AddNetworkStoragePoolParameters(dataCenterId, network);
        return doAction(ActionType.AddNetwork, parameters, action);
    }

    private Network getNetwork() {
        // The backend doesn't provide a mechanism to get a specific network from a provider, so we have to get all and
        // then iterate to find the requested one:
        Map<Network, Set<Guid>> networks = getAllNetworks();
        return findCurrentNetwork(networks);
    }

    private Network findCurrentNetwork(Map<Network, Set<Guid>> networks) {
        final Network result;
        if (networks == null) {
            result = null;
        } else {
            result = networks.keySet()
                    .stream()
                    .filter(network -> Objects.equals(network.getProvidedBy().getExternalId(), id))
                    .findFirst()
                    .orElse(null);
        }

        if (result == null) {
            // This will never return but always throw a WebApplicationException
            notFound();
        }
        return result;
    }

    private Map<Network, Set<Guid>> getAllNetworks() {
        IdQueryParameters parameters = new IdQueryParameters(asGuid(providerId));
        QueryReturnValue result = runQuery(QueryType.GetAllExternalNetworksOnProvider, parameters);

        checkResultAndThrowExceptionIfFailed(result);

        Map<Network, Set<Guid>> networks = result.getReturnValue();
        return networks;
    }

    private Guid getDataCenterId(Action action) {
        String dcIdFromAction = action.getDataCenter().getId();
        if (dcIdFromAction != null) {
            return new Guid(dcIdFromAction);
        }
        return getDataCenterIdByName(action.getDataCenter().getName());
    }

    private Guid getDataCenterIdByName(String dataCenterName) {
        NameQueryParameters parameters = new NameQueryParameters(dataCenterName);
        QueryReturnValue result = runQuery(QueryType.GetStoragePoolByDatacenterName, parameters);

        checkResultAndThrowExceptionIfFailed(result);

        List<StoragePool> dataCenters = result.getReturnValue();
        if (dataCenters.isEmpty()){
            // This will always throw a WebApplicationException
            notFound();
        }
        return dataCenters.get(0).getId();
    }

    private void checkResultAndThrowExceptionIfFailed(QueryReturnValue result){
        if (!result.getSucceeded()) {
            try {
                backendFailure(result.getExceptionString());
            } catch (BackendFailureException e){
                handleError(e, false);
            }
        }
    }
}

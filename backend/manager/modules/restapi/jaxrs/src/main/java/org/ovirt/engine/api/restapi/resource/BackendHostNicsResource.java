package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.common.util.LinkHelper;
import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.HostNics;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Slaves;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.api.resource.HostNicsResource;
import org.ovirt.engine.core.common.action.AddBondParameters;
import org.ovirt.engine.core.common.action.RemoveBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetAllNetworkQueryParamenters;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicsResource
    extends AbstractBackendCollectionResource<HostNIC, VdsNetworkInterface>
    implements HostNicsResource {

    static final String SUB_COLLECTIONS = "statistics";

    private String hostId;

    public BackendHostNicsResource(String hostId) {
        super(HostNIC.class, VdsNetworkInterface.class, SUB_COLLECTIONS);
        this.hostId = hostId;
    }

    public String getHostId() {
        return hostId;
    }

    public HostNics list() {
        HostNics ret = new HostNics();
        List<VdsNetworkInterface> ifaces = getCollection();
        List<network> clusterNetworks = getClusterNetworks();
        Map<String, String> networkIds = new HashMap<String, String>();
        for(network nwk : clusterNetworks) {
            networkIds.put(nwk.getname(), nwk.getId().toString());
        }
        for (VdsNetworkInterface iface : ifaces) {
            HostNIC hostNic = populate(map(iface, ifaces), iface);
            if (networkIds.containsKey(iface.getNetworkName())) {
                hostNic.getNetwork().setId(networkIds.get(iface.getNetworkName()));
                hostNic.getNetwork().setName(null);
            }
            ret.getHostNics().add(addLinks(hostNic));
        }
        return ret;
    }

    @SuppressWarnings("serial")
    @Override
    public Response add(final HostNIC nic) {
        validateParameters(nic, "name", "network.id|name", "bonding.slaves.id|name");
        return performCreation(VdcActionType.AddBond,
                               new AddBondParameters(asGuid(hostId),
                                                     nic.getName(),
                                                     lookupNetwork(nic.getNetwork()),
                                                     lookupSlaves(nic)){{setBondingOptions(map(nic, null).getBondOptions());}},
                               new HostNicResolver(nic.getName()));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveBond,
                             new RemoveBondParameters(asGuid(hostId),
                                                      lookupInterface(id).getName()));
    }

    @Override
    @SingleEntityResource
    public HostNicResource getHostNicSubResource(String id) {
        return inject(new BackendHostNicResource(id, this));
    }

    public HostNIC lookupNic(String id) {
        List<VdsNetworkInterface> ifaces = getCollection();
        for (VdsNetworkInterface iface : ifaces) {
            if (iface.getId().toString().equals(id)) {
                HostNIC hostNic = populate(map(iface, ifaces), iface);
                for(network nwk : getClusterNetworks()){
                    if(nwk.getname().equals(iface.getNetworkName())) {
                        hostNic.getNetwork().setId(nwk.getId().toString());
                        hostNic.getNetwork().setName(null);
                        break;
                    }
                }
                return addLinks(hostNic);
            }
        }
        return notFound();
    }

    public VdsNetworkInterface lookupInterface(String id) {
        for (VdsNetworkInterface iface : getCollection()) {
            if (iface.getId().toString().equals(id)) {
                return iface;
            }
        }
        return entityNotFound();
    }

    protected VdsNetworkInterface lookupInterfaceByName(String name) {
        for (VdsNetworkInterface iface : getCollection()) {
            if (iface.getName().equals(name)) {
                return iface;
            }
        }
        return null;
    }

    protected List<VdsNetworkInterface> getCollection() {
        return getBackendCollection(VdcQueryType.GetVdsInterfacesByVdsId, new GetVdsByVdsIdParameters(asGuid(hostId)));
    }

    protected List<VdsNetworkInterface> getCollection(List<VdsNetworkInterface> collection) {
        if (collection != null) {
            return collection;
        } else {
            return getCollection();
        }
    }

    @Override
    public HostNIC addParents(HostNIC nic) {
        nic.setHost(new Host());
        nic.getHost().setId(hostId);
        return nic;
    }

    protected HostNIC map(VdsNetworkInterface iface, List<VdsNetworkInterface> ifaces) {
        return map(iface, null, ifaces);
    }

    protected HostNIC map(VdsNetworkInterface iface, HostNIC template, List<VdsNetworkInterface> ifaces) {
        HostNIC nic = super.map(iface, template);
        if (iface.getBonded() != null && iface.getBonded()) {
            nic = addSlaveLinks(nic, getCollection(ifaces));
        } else if (iface.getBondName() != null) {
            nic = addMasterLink(nic, iface.getBondName(), getCollection(ifaces));
        }
        return nic;
    }

    @Override
    protected HostNIC map(VdsNetworkInterface entity, HostNIC template) {
        return map(entity, template, null);
    }

    @Override
    protected VdsNetworkInterface map(HostNIC entity, VdsNetworkInterface template) {
        return super.map(entity, template);
    }

    protected HostNIC addSlaveLinks(HostNIC nic, List<VdsNetworkInterface> ifaces) {
        if(nic.getBonding() == null) nic.setBonding(new Bonding());
        nic.getBonding().setSlaves(new Slaves());
        for (VdsNetworkInterface i : ifaces) {
            if (isSlave(i, nic.getName())) {
                nic.getBonding().getSlaves().getSlaves().add(slave(i.getId().toString()));
            }
        }
        return nic;
    }

    protected boolean isSlave(VdsNetworkInterface iface, String masterName) {
        return iface.getBondName() != null && iface.getBondName().equals(masterName);
    }

    protected HostNIC slave(String id) {
        HostNIC slave = new HostNIC();
        slave.setId(id);

        slave.setHost(new Host());
        slave.getHost().setId(hostId);
        slave = LinkHelper.addLinks(getUriInfo(), slave);
        slave.setHost(null);

        return slave;
    }

    protected HostNIC addMasterLink(HostNIC nic, String bondName, List<VdsNetworkInterface> ifaces) {
        for (VdsNetworkInterface i : ifaces) {
            if (i.getName().equals(bondName)) {
                nic.getLinks().add(masterLink(i.getId().toString()));
                break;
            }
        }
        return nic;
    }

    protected Link masterLink(String id) {
        Link master = new Link();
        master.setRel("master");
        master.setHref(idToHref(id));
        return master;
    }

    protected String idToHref(String id) {
        HostNIC master = new HostNIC();
        master.setId(id);
        master.setHost(new Host());
        master.getHost().setId(hostId);
        return LinkHelper.addLinks(getUriInfo(), master).getHref();
    }

    protected network lookupNetwork(Network network) {
        String id = network.getId();
        String name = network.getName();

        for (network entity : getBackendCollection(network.class,
                                                   VdcQueryType.GetAllNetworks,
                                                   new GetAllNetworkQueryParamenters(Guid.Empty))) {
            if ((id != null && id.equals(entity.getId().toString())) ||
                (name != null && name.equals(entity.getname()))) {
                return entity;
            }
        }

        return handleError(new EntityNotFoundException(id != null ? id : name), false);
    }

    protected String[] lookupSlaves(HostNIC nic) {
        List<String> slaves = new ArrayList<String>();

        for (HostNIC slave : nic.getBonding().getSlaves().getSlaves()) {
            if (slave.isSetId()) {
                for (VdsNetworkInterface iface : getCollection()) {
                    if (iface.getId().toString().equals(slave.getId())) {
                        slaves.add(iface.getName());
                    }
                }
            } else {
                slaves.add(slave.getName());
            }
        }

        return slaves.toArray(new String[slaves.size()]);
    }

    @Override
    protected HostNIC populate(HostNIC model, VdsNetworkInterface entity) {
        return addStatistics(model, entity, uriInfo, httpHeaders);
    }

    HostNIC addStatistics(HostNIC model, VdsNetworkInterface entity, UriInfo ui, HttpHeaders httpHeaders) {
        if (DetailHelper.include(httpHeaders, "statistics")) {
            model.setStatistics(new Statistics());
            HostNicStatisticalQuery query = new HostNicStatisticalQuery(newModel(model.getId()));
            List<Statistic> statistics = query.getStatistics(entity);
            for (Statistic statistic : statistics) {
                LinkHelper.addLinks(ui, statistic, query.getParentType());
            }
            model.getStatistics().getStatistics().addAll(statistics);
        }
        return model;
    }

    protected class HostNicResolver extends EntityIdResolver {

        private String name;

        HostNicResolver(String name) {
            this.name = name;
        }

        @Override
        public VdsNetworkInterface lookupEntity(Guid id) {
            assert(id == null); // AddBond returns nothing, lookup name instead
            return lookupInterfaceByName(name);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<network> getClusterNetworks(){
        VDS vds = getEntity(VDS.class, VdcQueryType.GetVdsByVdsId, new GetVdsByVdsIdParameters(Guid.createGuidFromString(getHostId())), "Host");
        return getEntity(List.class, VdcQueryType.GetAllNetworksByClusterId, new VdsGroupQueryParamenters(vds.getvds_group_id()), "Networks");
    }

    public network lookupClusterNetwork(Network net) {
        List<network> networks = getClusterNetworks();
        if(net.isSetId()){
            for(network nwk : networks){
                if (nwk.getId().toString().equals(net.getId()))
                    return nwk;
            }
        }else{
            String networkName = net.getName();
            for(network nwk : networks){
                if(nwk.getname().equals(networkName)) return nwk;
            }
        }
        return notFound(network.class);
    }
}

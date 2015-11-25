package org.ovirt.engine.api.restapi.resource;

import static java.util.stream.Collectors.toMap;
import static org.ovirt.engine.core.common.action.VdcActionType.SetupNetworks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.HostNicVirtualFunctionsConfiguration;
import org.ovirt.engine.api.model.HostNics;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.api.resource.HostNicsResource;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.utils.ArrayUtils;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.common.action.AddBondParameters;
import org.ovirt.engine.core.common.action.CustomPropertiesForVdsNetworkInterface;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicsResource
    extends AbstractBackendCollectionResource<HostNic, VdsNetworkInterface>
        implements HostNicsResource {

    static final String[] PF_SUB_COLLECTIONS = { "vfallowedlabels", "vfallowednetworks" };
    static final String[] SUB_COLLECTIONS = ArrayUtils.concat(
           new String[] { "statistics", "labels", "networkattachments"}, PF_SUB_COLLECTIONS);
    private static final String UPDATE_VFS_CONFIG_ACTION = "updatevfsconfig";

    private String hostId;

    public BackendHostNicsResource(String hostId) {
        super(HostNic.class, VdsNetworkInterface.class, SUB_COLLECTIONS);
        this.hostId = hostId;
    }

    public String getHostId() {
        return hostId;
    }

    @Override
    public HostNics list() {
        HostNics ret = new HostNics();
        List<VdsNetworkInterface> ifaces = getCollection();
        List<org.ovirt.engine.core.common.businessentities.network.Network> clusterNetworks = getClusterNetworks();
        Map<String, String> networkIds = new HashMap<>();
        for(org.ovirt.engine.core.common.businessentities.network.Network nwk : clusterNetworks) {
            networkIds.put(nwk.getName(), nwk.getId().toString());
        }
        for (VdsNetworkInterface iface : ifaces) {
            HostNic hostNic = populate(map(iface, ifaces), iface);
            if (networkIds.containsKey(iface.getNetworkName())) {
                hostNic.getNetwork().setId(networkIds.get(iface.getNetworkName()));
                hostNic.getNetwork().setName(null);
            }
            ret.getHostNics().add(addLinks(hostNic));
        }
        return addActions(ret);
    }

    @Override
    protected HostNic addLinks(HostNic hostNic, String... subCollectionsToExclude) {
        if (hostNic.isSetVirtualFunctionsConfiguration()) {
            return super.addLinks(hostNic, subCollectionsToExclude);
        }
        else {
            final HostNic resultHostNic = super.addLinks(hostNic,
                    ArrayUtils.concat(PF_SUB_COLLECTIONS, subCollectionsToExclude));
            final Iterator<Link> linkIterator = resultHostNic.getActions().getLinks().iterator();
            while (linkIterator.hasNext()) {
                final Link link = linkIterator.next();
                if (link.getRel().equals(UPDATE_VFS_CONFIG_ACTION)) {
                    linkIterator.remove();
                }
            }
            return resultHostNic;
        }
    }

    @SuppressWarnings("serial")
    @Override
    public Response add(final HostNic nic) {
        validateParameters(nic, "name", "network.id|name", "bonding.slaves.id|name");
        validateEnums(HostNic.class, nic);
        return performCreate(VdcActionType.AddBond,
                               new AddBondParameters(asGuid(hostId),
                                                     nic.getName(),
                                                     lookupNetwork(nic.getNetwork()),
                                                     lookupSlaves(nic)){{setBondingOptions(map(nic, null).getBondOptions());}},
                               new HostNicResolver(nic.getName()));
    }

    @Override
    public HostNicResource getNicResource(String id) {
        return inject(new BackendHostNicResource(id, this));
    }

    public Map<Guid, HostNicVfsConfig> findHostNicVfsConfigs() {
        final List<HostNicVfsConfig> hostNicVfsConfigs = getBackendCollection(HostNicVfsConfig.class,
                VdcQueryType.GetAllVfsConfigByHostId,
                new IdQueryParameters(asGuid(hostId)));
        return hostNicVfsConfigs.stream().collect(toMap(HostNicVfsConfig::getNicId, x -> x));
    }

    public Map<Guid, Guid> retriveVfMap() {
        final VdcQueryReturnValue returnValue =
                runQuery(VdcQueryType.GetVfToPfMapByHostId, new IdQueryParameters(asGuid(hostId)));
        return returnValue.getReturnValue();
    }

    @Override
    protected HostNic doPopulate(HostNic model, VdsNetworkInterface entity) {
        final HostNic hostNic = super.doPopulate(model, entity);
        final Guid nicId = entity.getId();
        final HostNicVfsConfig hostNicVfsConfig = findVfsConfig(nicId);
        if (hostNicVfsConfig == null) {
            final Map<Guid, Guid> vfMap = retriveVfMap();
            final Guid physicalFunctionNicId = vfMap.get(nicId);
            if (physicalFunctionNicId != null) {
                final HostNic physicalFunction = new HostNic();
                physicalFunction.setId(physicalFunctionNicId.toString());
                hostNic.setPhysicalFunction(physicalFunction);
            }
        } else {
            final Mapper<HostNicVfsConfig, HostNicVirtualFunctionsConfiguration> mapper =
                    getMapper(HostNicVfsConfig.class,
                            HostNicVirtualFunctionsConfiguration.class);
            final HostNicVirtualFunctionsConfiguration vfsConfigModel =
                    mapper.map(hostNicVfsConfig, new HostNicVirtualFunctionsConfiguration());
            hostNic.setVirtualFunctionsConfiguration(vfsConfigModel);
        }

        return hostNic;
    }

    HostNicVfsConfig findVfsConfig(Guid nicId) {
        final Map<Guid, HostNicVfsConfig> hostNicVfsConfigs = findHostNicVfsConfigs();
        return hostNicVfsConfigs.get(nicId);
    }

    public HostNic lookupNic(String id, boolean forcePopulate) {
        List<VdsNetworkInterface> ifaces = getCollection();
        for (VdsNetworkInterface iface : ifaces) {
            if (iface.getId().toString().equals(id)) {
                HostNic hostNic = map(iface, ifaces);
                if (forcePopulate) {
                    deprecatedPopulate(hostNic, iface);
                    doPopulate(hostNic, iface);
                } else {
                    populate(hostNic, iface);
                }
                for (org.ovirt.engine.core.common.businessentities.network.Network nwk : getClusterNetworks()) {
                    if (nwk.getName().equals(iface.getNetworkName())) {
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

    /**
     * Look for the interface by ID, and raise an error if the interface was not found.
     *
     * @param id
     *            The ID of the interface to look for.
     * @return The interface.
     */
    public VdsNetworkInterface lookupInterface(String id) {
        VdsNetworkInterface iface = lookupInterfaceById(id);

        return iface == null ? entityNotFound() : iface;
    }

    private VdsNetworkInterface lookupInterfaceById(String id) {
        for (VdsNetworkInterface iface : getCollection()) {
            if (iface.getId().toString().equals(id)) {
                return iface;
            }
        }
        return null;
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
        return getBackendCollection(VdcQueryType.GetVdsInterfacesByVdsId, new IdQueryParameters(asGuid(hostId)));
    }

    protected List<VdsNetworkInterface> getCollection(List<VdsNetworkInterface> collection) {
        if (collection != null) {
            return collection;
        } else {
            return getCollection();
        }
    }

    @Override
    public HostNic addParents(HostNic hostNic) {
        final HostNic nic = super.addParents(hostNic);
        final Host host = new Host();
        host.setId(hostId);
        nic.setHost(host);
        if (nic.getPhysicalFunction() != null) {
            nic.getPhysicalFunction().setHost(host);
        }
        return nic;
    }

    protected HostNic map(VdsNetworkInterface iface, List<VdsNetworkInterface> ifaces) {
        return map(iface, null, ifaces);
    }

    protected HostNic map(VdsNetworkInterface iface, HostNic template, List<VdsNetworkInterface> ifaces) {
        HostNic nic = super.map(iface, template);
        if (iface.getBonded() != null && iface.getBonded()) {
            nic = addSlaveLinks(nic, getCollection(ifaces));
        } else if (iface.getBondName() != null) {
            nic = addMasterLink(nic, iface.getBondName(), getCollection(ifaces));
        }
        return nic;
    }

    @Override
    protected HostNic map(VdsNetworkInterface entity, HostNic template) {
        return map(entity, template, null);
    }

    @Override
    protected VdsNetworkInterface map(HostNic entity, VdsNetworkInterface template) {
        VdsNetworkInterface iface = super.map(entity, template);
        if (entity.isSetNetwork()) {
            if (entity.getNetwork().isSetId() || entity.getNetwork().isSetName()) {
                org.ovirt.engine.core.common.businessentities.network.Network net = lookupNetwork(entity.getNetwork());
                iface.setNetworkName(net.getName());
            } else {
                iface.setNetworkName(null);
            }
        }
        return iface;
    }

    protected HostNic addSlaveLinks(HostNic nic, List<VdsNetworkInterface> ifaces) {
        if(nic.getBonding() == null) nic.setBonding(new Bonding());
        nic.getBonding().setSlaves(new HostNics());
        for (VdsNetworkInterface i : ifaces) {
            if (i.isPartOfBond(nic.getName())) {
                nic.getBonding().getSlaves().getHostNics().add(slave(i.getId().toString()));
            }
        }
        return nic;
    }

    protected HostNic slave(String id) {
        HostNic slave = new HostNic();
        slave.setId(id);

        slave.setHost(new Host());
        slave.getHost().setId(hostId);
        slave = LinkHelper.addLinks(getUriInfo(), slave);
        slave.setHost(null);

        return slave;
    }

    protected HostNic addMasterLink(HostNic nic, String bondName, List<VdsNetworkInterface> ifaces) {
        for (VdsNetworkInterface i : ifaces) {
            if (i.getName().equals(bondName)) {
                nic.getLinks().add(masterLink(i.getId().toString()));
                break;
            }
        }
        return nic;
    }

    private Link masterLink(String id) {
        Link master = new Link();
        master.setRel("master");
        master.setHref(idToHref(id));
        return master;
    }

    private String idToHref(String id) {
        HostNic master = new HostNic();
        master.setId(id);
        master.setHost(new Host());
        master.getHost().setId(hostId);
        return LinkHelper.addLinks(getUriInfo(), master).getHref();
    }

    protected org.ovirt.engine.core.common.businessentities.network.Network lookupNetwork(Network network) {
        String id = network.getId();
        String name = network.getName();

        for (org.ovirt.engine.core.common.businessentities.network.Network entity : getBackendCollection(org.ovirt.engine.core.common.businessentities.network.Network.class,
                                                   VdcQueryType.GetAllNetworks,
                                                   new IdQueryParameters(Guid.Empty))) {
            if ((id != null && id.equals(entity.getId().toString())) ||
                (name != null && name.equals(entity.getName()))) {
                return entity;
            }
        }

        return handleError(new EntityNotFoundException(id != null ? id : name), false);
    }

    protected String[] lookupSlaves(HostNic nic) {
        List<String> slaves = new ArrayList<>();

        for (HostNic slave : nic.getBonding().getSlaves().getHostNics()) {
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
    protected HostNic deprecatedPopulate(HostNic model, VdsNetworkInterface entity) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        if (details.contains("statistics")) {
            addStatistics(model, entity);
        }
        return model;
    }

    private void addStatistics(HostNic model, VdsNetworkInterface entity) {
        model.setStatistics(new Statistics());
        HostNicStatisticalQuery query = new HostNicStatisticalQuery(newModel(model.getId()));
        List<Statistic> statistics = query.getStatistics(entity);
        for (Statistic statistic : statistics) {
            LinkHelper.addLinks(uriInfo, statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }

    protected class HostNicResolver extends EntityIdResolver<Guid> {

        private String name;

        HostNicResolver(String name) {
            this.name = name;
        }

        @Override
        public VdsNetworkInterface lookupEntity(Guid id) {
            assert (id == null); // AddBond returns nothing, lookup name instead
            return lookupInterfaceByName(name);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<org.ovirt.engine.core.common.businessentities.network.Network> getClusterNetworks(){
        VDS vds = getEntity(VDS.class, VdcQueryType.GetVdsByVdsId, new IdQueryParameters(Guid.createGuidFromStringDefaultEmpty(getHostId())), "Host");
        return getEntity(List.class, VdcQueryType.GetAllNetworksByClusterId, new IdQueryParameters(vds.getVdsGroupId()), "Networks");
    }

    public org.ovirt.engine.core.common.businessentities.network.Network lookupClusterNetwork(Network net) {
        List<org.ovirt.engine.core.common.businessentities.network.Network> networks = getClusterNetworks();
        if(net.isSetId()){
            for(org.ovirt.engine.core.common.businessentities.network.Network nwk : networks){
                if (nwk.getId().toString().equals(net.getId()))
                    return nwk;
            }
        }else{
            String networkName = net.getName();
            for(org.ovirt.engine.core.common.businessentities.network.Network nwk : networks){
                if(nwk.getName().equals(networkName)) return nwk;
            }
        }
        return notFound(org.ovirt.engine.core.common.businessentities.network.Network.class);
    }

    @Override
    public Response setupNetworks(Action action) {
        validateParameters(action, "hostNics");
        SetupNetworksParameters parameters = toParameters(action);
        return performAction(SetupNetworks, parameters, action);
    }

    private SetupNetworksParameters toParameters(Action action) {
        List<HostNic> hostNics = action.getHostNics().getHostNics();
        List<VdsNetworkInterface> existingNics = getCollection();
        BusinessEntityMap<VdsNetworkInterface> existingNicsMapping = new BusinessEntityMap<>(existingNics);

        SetupNetworksParameters parameters = new SetupNetworksParameters();
        parameters.setInterfaces(nicsToInterfaces(hostNics, existingNicsMapping));
        parameters.setCustomProperties(nicsToCustomProperties(hostNics, existingNicsMapping));
        parameters.setVdsId(Guid.createGuidFromStringDefaultEmpty(getHostId()));
        parameters.setForce(action.isSetForce() ? action.isForce() : false);
        parameters.setCheckConnectivity(action.isSetCheckConnectivity() ? action.isCheckConnectivity() : false);
        if (action.isSetConnectivityTimeout()) {
            parameters.setConectivityTimeout(action.getConnectivityTimeout());
        }
        parameters.setNetworksToSync(nicsToNetworksToSync(hostNics));
        return parameters;
    }

    private CustomPropertiesForVdsNetworkInterface nicsToCustomProperties(List<HostNic> hostNics,
        BusinessEntityMap<VdsNetworkInterface> existingNicsMapping) {
        CustomPropertiesForVdsNetworkInterface result = new CustomPropertiesForVdsNetworkInterface();
        for (HostNic hostNic : hostNics) {
            if (hostNic.isSetProperties()) {
                String hostNicName = hostNic.getName();
                String nicName = StringUtils.isEmpty(hostNicName)
                    ? existingNicsMapping.get(hostNic.getId()).getName()
                    : hostNicName;

                result.add(nicName, CustomPropertiesParser.toMap(hostNic.getProperties()));
            }
        }
        return result;
    }

    private List<VdsNetworkInterface> nicsToInterfaces(List<HostNic> hostNics,
        BusinessEntityMap<VdsNetworkInterface> existingNicsMapping) {
        List<VdsNetworkInterface> ifaces = new ArrayList<>(hostNics.size());

        for (HostNic nic : hostNics) {
            VdsNetworkInterface iface = map(nic, null);
            ifaces.add(iface);
            if (nic.isSetBonding() && nic.getBonding().isSetSlaves()) {
                for (HostNic slave : nic.getBonding().getSlaves().getHostNics()) {
                    VdsNetworkInterface slaveIface = map(slave, slave.getId() == null
                            ? lookupInterfaceByName(slave.getName()) : lookupInterfaceById(slave.getId()));
                    slaveIface.setBondName(nic.getName());
                    ifaces.add(slaveIface);
                }
            }

            if (nic.isSetName() && existingNicsMapping.containsKey(nic.getName())) {
                iface.setLabels(existingNicsMapping.get(nic.getName()).getLabels());
            } else if (nic.isSetId()) {
                Guid nicId = asGuid(nic.getId());
                if (existingNicsMapping.containsKey(nicId)) {
                    iface.setLabels(existingNicsMapping.get(nicId).getLabels());
                }
            }

        }
        return ifaces;
    }

    private List<String> nicsToNetworksToSync(List<HostNic> hostNics) {
        List<String> networks = new ArrayList<>();
        for (HostNic nic : hostNics) {
            if (nic.isSetOverrideConfiguration() && nic.isOverrideConfiguration() && nic.isSetNetwork()) {
                org.ovirt.engine.core.common.businessentities.network.Network net = lookupNetwork(nic.getNetwork());
                networks.add(net.getName());
            }
        }
        return networks;
    }

    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }
}

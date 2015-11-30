package org.ovirt.engine.api.restapi.resource;

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
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.HostNicVirtualFunctionsConfiguration;
import org.ovirt.engine.api.model.HostNics;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Slaves;
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
import org.ovirt.engine.core.utils.linq.LinqUtils;

public class BackendHostNicsResource
    extends AbstractBackendCollectionResource<HostNIC, VdsNetworkInterface>
        implements HostNicsResource {

    static final String VIRTUAL_FUNCTION_ALLOWED_LABELS = "virtualfunctionallowedlabels";
    static final String LABELS = "labels";
    static final String[] PF_SUB_COLLECTIONS = { VIRTUAL_FUNCTION_ALLOWED_LABELS, "virtualfunctionallowednetworks" };
    static final String[] SUB_COLLECTIONS = ArrayUtils.concat(
            new String[] { "statistics", LABELS, "networkattachments" }, PF_SUB_COLLECTIONS);
    private static final String UPDATE_VFS_CONFIG_ACTION = "updatevirtualfunctionsconfiguration";

    private String hostId;

    public BackendHostNicsResource(String hostId) {
        super(HostNIC.class, VdsNetworkInterface.class, SUB_COLLECTIONS);
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
            HostNIC hostNic = populate(map(iface, ifaces), iface);
            if (networkIds.containsKey(iface.getNetworkName())) {
                hostNic.getNetwork().setId(networkIds.get(iface.getNetworkName()));
                hostNic.getNetwork().setName(null);
            }
            ret.getHostNics().add(addLinks(hostNic));
        }
        return addActions(ret);
    }

    @Override
    protected HostNIC addLinks(HostNIC hostNic, String... subCollectionsToExclude) {
        if (hostNic.isSetVirtualFunctionsConfiguration()) {
            return super.addLinks(hostNic, subCollectionsToExclude);
        }
        else {
            final HostNIC resultHostNic = super.addLinks(hostNic,
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
    public Response add(final HostNIC nic) {
        validateParameters(nic, "name", "network.id|name", "bonding.slaves.id|name");
        validateEnums(HostNIC.class, nic);
        return performCreate(VdcActionType.AddBond,
                               new AddBondParameters(asGuid(hostId),
                                                     nic.getName(),
                                                     lookupNetwork(nic.getNetwork()),
                                                     lookupSlaves(nic)){{setBondingOptions(map(nic, null).getBondOptions());}},
                               new HostNicResolver(nic.getName()));
    }

    @Override
    public HostNicResource getHostNicSubResource(String id) {
        return inject(new BackendHostNicResource(id, this));
    }

    public Map<Guid, HostNicVfsConfig> findHostNicVfsConfigs() {
        final List<HostNicVfsConfig> hostNicVfsConfigs = getBackendCollection(HostNicVfsConfig.class,
                VdcQueryType.GetAllVfsConfigByHostId,
                new IdQueryParameters(asGuid(hostId)));
        return LinqUtils.toMap(hostNicVfsConfigs,
                new org.ovirt.engine.core.utils.linq.Mapper<HostNicVfsConfig, Guid, HostNicVfsConfig>() {
                    @Override
                    public Guid createKey(HostNicVfsConfig hostNicVfsConfig) {
                        return hostNicVfsConfig.getNicId();
                    }

                    @Override
                    public HostNicVfsConfig createValue(HostNicVfsConfig hostNicVfsConfig) {
                        return hostNicVfsConfig;
                    }
                });
    }

    public Map<Guid, Guid> retriveVfMap() {
        final VdcQueryReturnValue returnValue =
                runQuery(VdcQueryType.GetVfToPfMapByHostId, new IdQueryParameters(asGuid(hostId)));
        return returnValue.getReturnValue();
    }

    @Override
    protected HostNIC doPopulate(HostNIC model, VdsNetworkInterface entity) {
        final HostNIC hostNic = super.doPopulate(model, entity);
        final Guid nicId = entity.getId();
        final HostNicVfsConfig hostNicVfsConfig = findVfsConfig(nicId);
        if (hostNicVfsConfig == null) {
            final Map<Guid, Guid> vfMap = retriveVfMap();
            final Guid physicalFunctionNicId = vfMap.get(nicId);
            if (physicalFunctionNicId != null) {
                final HostNIC physicalFunction = new HostNIC();
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

    public HostNIC lookupNic(String id, boolean forcePopulate) {
        List<VdsNetworkInterface> ifaces = getCollection();
        for (VdsNetworkInterface iface : ifaces) {
            if (iface.getId().toString().equals(id)) {
                HostNIC hostNic = map(iface, ifaces);
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
    public HostNIC addParents(HostNIC hostNic) {
        final HostNIC nic = super.addParents(hostNic);
        final Host host = new Host();
        host.setId(hostId);
        nic.setHost(host);
        if (nic.getPhysicalFunction() != null) {
            nic.getPhysicalFunction().setHost(host);
        }
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

    protected HostNIC addSlaveLinks(HostNIC nic, List<VdsNetworkInterface> ifaces) {
        if(nic.getBonding() == null) nic.setBonding(new Bonding());
        nic.getBonding().setSlaves(new Slaves());
        for (VdsNetworkInterface i : ifaces) {
            if (i.isPartOfBond(nic.getName())) {
                nic.getBonding().getSlaves().getSlaves().add(slave(i.getId().toString()));
            }
        }
        return nic;
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

    private Link masterLink(String id) {
        Link master = new Link();
        master.setRel("master");
        master.setHref(idToHref(id));
        return master;
    }

    private String idToHref(String id) {
        HostNIC master = new HostNIC();
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

    protected String[] lookupSlaves(HostNIC nic) {
        List<String> slaves = new ArrayList<>();

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
    protected HostNIC deprecatedPopulate(HostNIC model, VdsNetworkInterface entity) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        if (details.contains("statistics")) {
            addStatistics(model, entity);
        }
        return model;
    }

    private void addStatistics(HostNIC model, VdsNetworkInterface entity) {
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
        List<HostNIC> hostNics = action.getHostNics().getHostNics();
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

    private CustomPropertiesForVdsNetworkInterface nicsToCustomProperties(List<HostNIC> hostNics,
        BusinessEntityMap<VdsNetworkInterface> existingNicsMapping) {
        CustomPropertiesForVdsNetworkInterface result = new CustomPropertiesForVdsNetworkInterface();
        for (HostNIC hostNic : hostNics) {
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

    private List<VdsNetworkInterface> nicsToInterfaces(List<HostNIC> hostNics,
        BusinessEntityMap<VdsNetworkInterface> existingNicsMapping) {
        List<VdsNetworkInterface> ifaces = new ArrayList<>(hostNics.size());

        for (HostNIC nic : hostNics) {
            VdsNetworkInterface iface = map(nic, null);
            ifaces.add(iface);
            if (nic.isSetBonding() && nic.getBonding().isSetSlaves()) {
                for (HostNIC slave : nic.getBonding().getSlaves().getSlaves()) {
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

    private List<String> nicsToNetworksToSync(List<HostNIC> hostNics) {
        List<String> networks = new ArrayList<>();
        for (HostNIC nic : hostNics) {
            if (nic.isSetOverrideConfiguration() && nic.isOverrideConfiguration() && nic.isSetNetwork()) {
                org.ovirt.engine.core.common.businessentities.network.Network net = lookupNetwork(nic.getNetwork());
                networks.add(net.getName());
            }
        }
        return networks;
    }


    @Override
    public ActionResource getActionSubresource(String action) {
        return inject(new BackendActionResource(action, ""));
    }
}

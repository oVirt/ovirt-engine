package org.ovirt.engine.api.restapi.resource;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.HostNicVirtualFunctionsConfiguration;
import org.ovirt.engine.api.model.HostNics;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.api.resource.HostNicsResource;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.utils.ArrayUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicsResource
    extends AbstractBackendCollectionResource<HostNic, VdsNetworkInterface>
        implements HostNicsResource {

    static final String VIRTUAL_FUNCTION_ALLOWED_LABELS = "virtualfunctionallowedlabels";
    static final String LINK_LAYER_DISCOVERY_PROTOCOL_ELEMENTS = "linklayerdiscoveryprotocolelements";
    static final String[] PF_SUB_COLLECTIONS = { VIRTUAL_FUNCTION_ALLOWED_LABELS, "virtualfunctionallowednetworks" };
    private static final String UPDATE_VFS_CONFIG_ACTION = "updatevirtualfunctionsconfiguration";

    private String hostId;

    public BackendHostNicsResource(String hostId) {
        super(HostNic.class, VdsNetworkInterface.class);
        this.hostId = hostId;
    }

    public String getHostId() {
        return hostId;
    }

    @Override
    public HostNics list() {
        HostNics ret = new HostNics();
        List<VdsNetworkInterface> ifaces = getCollection();
        Map<String, Guid> networkNameToNetworkIdMap = mapNetworkNamesToNetworkIds();
        Map<Guid, NetworkAttachment> attachmentsByNetworkId = getAttachmentsByNetworkId();

        for (VdsNetworkInterface iface : ifaces) {
            HostNic hostNic = populate(map(iface, ifaces), iface);
            setCustomProperties(attachmentsByNetworkId, networkNameToNetworkIdMap, hostNic);

            String networkName = iface.getNetworkName();
            if (networkNameToNetworkIdMap.containsKey(networkName)) {
                Guid networkId = networkNameToNetworkIdMap.get(networkName);
                hostNic.getNetwork().setId(networkId.toString());
                hostNic.getNetwork().setName(null);
            }
            ret.getHostNics().add(addLinks(hostNic));
        }
        return addActions(ret);
    }

    private Map<String, Guid> mapNetworkNamesToNetworkIds() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> clusterNetworks = getClusterNetworks();
        Map<String, Guid> networkIdByName = new HashMap<>();
        for(org.ovirt.engine.core.common.businessentities.network.Network nwk : clusterNetworks) {
            networkIdByName.put(nwk.getName(), nwk.getId());
        }
        return networkIdByName;
    }

    private void setCustomProperties(Map<Guid, NetworkAttachment> attachmentsByNetworkId,
            Map<String, Guid> networkNameToNetworkIdMap,
            HostNic hostNic) {
        Network network = hostNic.getNetwork();
        if (network == null) {
            return;
        }

        String networkName = network.getName();
        NetworkAttachment networkAttachment = attachmentsByNetworkId.get(networkNameToNetworkIdMap.get(networkName));
        if (networkAttachment == null) {
            return;
        }

        Map<String, String> properties = networkAttachment.getProperties();
        if (properties != null) {
            hostNic.setProperties(CustomPropertiesParser.fromMap(properties));
        }
    }

    private Map<Guid, NetworkAttachment> getAttachmentsByNetworkId() {
        List<NetworkAttachment> attachments = getBackendCollection(NetworkAttachment.class,
                QueryType.GetNetworkAttachmentsByHostId,
                new IdQueryParameters(asGuid(hostId)));

        return new MapNetworkAttachments(attachments).byNetworkId();
    }

    @Override
    protected HostNic addLinks(HostNic hostNic, String... subCollectionsToExclude) {
        if (hostNic.isSetVirtualFunctionsConfiguration()) {
            return super.addLinks(hostNic, subCollectionsToExclude);
        } else {
            final HostNic resultHostNic = super.addLinks(hostNic,
                    ArrayUtils.concat(PF_SUB_COLLECTIONS, subCollectionsToExclude));
            final Iterator<Link> linkIterator = resultHostNic.getActions().getLinks().iterator();
            while (linkIterator.hasNext()) {
                final Link link = linkIterator.next();
                if (link.getRel().equals(UPDATE_VFS_CONFIG_ACTION)) {
                    linkIterator.remove();
                }
            }

            if (isBond(resultHostNic)) {
                removeLldpLink(resultHostNic);
            }

            return resultHostNic;
        }
    }

    private boolean isBond(HostNic hostNic) {
        return hostNic.getBonding() != null;
    }

    private void removeLldpLink(HostNic hostNic) {
        final Iterator<Link> linkIterator = hostNic.getLinks().iterator();
        while (linkIterator.hasNext()) {
            final Link link = linkIterator.next();
            if (link.getRel().equals(LINK_LAYER_DISCOVERY_PROTOCOL_ELEMENTS)) {
                linkIterator.remove();
                return;
            }
        }
    }

    @Override
    public HostNicResource getNicResource(String id) {
        return inject(new BackendHostNicResource(id, this));
    }

    public Map<Guid, HostNicVfsConfig> findHostNicVfsConfigs() {
        final List<HostNicVfsConfig> hostNicVfsConfigs = getBackendCollection(HostNicVfsConfig.class,
                QueryType.GetAllVfsConfigByHostId,
                new IdQueryParameters(asGuid(hostId)));
        return hostNicVfsConfigs.stream().collect(toMap(HostNicVfsConfig::getNicId, x -> x));
    }

    public Map<Guid, Guid> retriveVfMap() {
        final QueryReturnValue returnValue =
                runQuery(QueryType.GetVfToPfMapByHostId, new IdQueryParameters(asGuid(hostId)));
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

    public HostNic lookupNic(String id) {
        List<VdsNetworkInterface> ifaces = getCollection();
        for (VdsNetworkInterface iface : ifaces) {
            if (iface.getId().toString().equals(id)) {
                HostNic hostNic = map(iface, ifaces);
                populate(hostNic, iface);
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
        return getBackendCollection(QueryType.GetVdsInterfacesByVdsId, new IdQueryParameters(asGuid(hostId)));
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
            nic = addActiveSlaveLink(nic, iface, getCollection(ifaces));
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
        if(nic.getBonding() == null) {
            nic.setBonding(new Bonding());
        }
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
        slave = LinkHelper.addLinks(slave, null, false);
        slave.setHost(null);

        return slave;
    }

    protected HostNic addActiveSlaveLink(HostNic nic, VdsNetworkInterface iface, List<VdsNetworkInterface> ifaces) {
        if (iface instanceof Bond) {
            Bond bond = (Bond) iface;
            if(nic.getBonding() == null) {
                nic.setBonding(new Bonding());
            }
            for (VdsNetworkInterface i : ifaces) {
                if (i.getName().equals(bond.getActiveSlave())) {
                    nic.getBonding().setActiveSlave(slave(i.getId().toString()));
                    break;
                }
            }
        }
        return nic;
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
        return LinkHelper.addLinks(master).getHref();
    }

    protected org.ovirt.engine.core.common.businessentities.network.Network lookupNetwork(Network network) {
        String id = network.getId();
        String name = network.getName();

        for (org.ovirt.engine.core.common.businessentities.network.Network entity : getBackendCollection(org.ovirt.engine.core.common.businessentities.network.Network.class,
                                                   QueryType.GetAllNetworks,
                                                   new IdQueryParameters(Guid.Empty))) {
            if ((id != null && id.equals(entity.getId().toString())) ||
                (name != null && name.equals(entity.getName()))) {
                return entity;
            }
        }

        return handleError(new EntityNotFoundException(id != null ? id : name), false);
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
            LinkHelper.addLinks(statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }

    @SuppressWarnings("unchecked")
    protected List<org.ovirt.engine.core.common.businessentities.network.Network> getClusterNetworks(){
        VDS vds = getEntity(VDS.class, QueryType.GetVdsByVdsId, new IdQueryParameters(Guid.createGuidFromStringDefaultEmpty(getHostId())), "Host");
        return getEntity(List.class, QueryType.GetAllNetworksByClusterId, new IdQueryParameters(vds.getClusterId()), "Networks");
    }

    public org.ovirt.engine.core.common.businessentities.network.Network lookupClusterNetwork(Network net) {
        List<org.ovirt.engine.core.common.businessentities.network.Network> networks = getClusterNetworks();
        if(net.isSetId()){
            for(org.ovirt.engine.core.common.businessentities.network.Network nwk : networks){
                if (nwk.getId().toString().equals(net.getId())) {
                    return nwk;
                }
            }
        }else{
            String networkName = net.getName();
            for(org.ovirt.engine.core.common.businessentities.network.Network nwk : networks){
                if(nwk.getName().equals(networkName)) {
                    return nwk;
                }
            }
        }
        return notFound(org.ovirt.engine.core.common.businessentities.network.Network.class);
    }
}

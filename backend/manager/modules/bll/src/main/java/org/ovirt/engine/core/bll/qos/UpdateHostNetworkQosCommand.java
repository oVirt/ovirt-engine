package org.ovirt.engine.core.bll.qos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.HostSetupNetworksParametersBuilder;
import org.ovirt.engine.core.bll.validator.HostNetworkQosValidator;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

public class UpdateHostNetworkQosCommand extends UpdateQosCommandBase<HostNetworkQos, HostNetworkQosValidator> {

    @Inject
    private NetworkDao networkDao;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    public UpdateHostNetworkQosCommand(QosParametersBase<HostNetworkQos> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected HostNetworkQosDao getQosDao() {
        return getDbFacade().getHostNetworkQosDao();
    }

    @Override
    protected HostNetworkQosValidator getQosValidator(HostNetworkQos qos) {
        return new HostNetworkQosValidator(qos);
    }

    @Override
    protected boolean validate() {
        HostNetworkQosValidator validator = getQosValidator(getQos());
        return super.validate()
                && validate(validator.requiredValuesPresent())
                && validate(validator.valuesConsistent());
    }

    @Override
    protected void executeCommand() {
        Guid qosId = getQosId();
        HostNetworkQos oldQos = getQosDao().get(qosId);
        HostNetworkQos newQos = getQos();

        super.executeCommand();

        if (networkUpdateRequired(oldQos, newQos)) {
            applyNetworkChangesToHosts();
        }
    }

    /**
     * @param oldQos Qos entity before update
     * @param newQos Qos entity after update
     * @return true if Qos entity was update in such way, which needs to update related networks.
     */
    private boolean networkUpdateRequired(HostNetworkQos oldQos, HostNetworkQos newQos) {
        boolean noChange = Objects.equals(newQos.getOutAverageLinkshare(), oldQos.getOutAverageLinkshare())
        && Objects.equals(newQos.getOutAverageRealtime(), oldQos.getOutAverageRealtime())
        && Objects.equals(newQos.getOutAverageUpperlimit(), oldQos.getOutAverageUpperlimit());

        return !noChange;
    }

    private void applyNetworkChangesToHosts() {
        ArrayList<VdcActionParametersBase> parameters = createPersistentHostSetupNetworksParameters();

        if (!parameters.isEmpty()) {
            HostSetupNetworksParametersBuilder.updateParametersSequencing(parameters);
            runInternalMultipleActions(VdcActionType.PersistentHostSetupNetworks, parameters);
        }
    }

    /**
     * @return list of PersistentHostSetupNetworksParameters instances to update all hosts affected by qos change.
     */
    private ArrayList<VdcActionParametersBase> createPersistentHostSetupNetworksParameters() {
        List<Network> networksHavingAlteredQos = networkDao.getAllForQos(getQosId());
        Map<Guid, List<Network>> vdsIdToNetworksOfAlteredQos = getHostIdToNetworksMap(networksHavingAlteredQos);

        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        for (Map.Entry<Guid, List<Network>> entry : vdsIdToNetworksOfAlteredQos.entrySet()) {
            Guid hostId = entry.getKey();
            List<Network> networksOfAlteredQos = entry.getValue();

            PersistentHostSetupNetworksParameters setupNetworkParams =
                    createPersistentHostSetupNetworksParameters(hostId, networksOfAlteredQos);

            parameters.add(setupNetworkParams);
        }
        return parameters;
    }

    /**
     * @param hostId host to update
     * @param networks networks to be updated
     * @return PersistentHostSetupNetworksParameters to refresh all given networks.
     */
    private PersistentHostSetupNetworksParameters createPersistentHostSetupNetworksParameters(Guid hostId,
            List<Network> networks) {

        PersistentHostSetupNetworksParameters parameters = new PersistentHostSetupNetworksParameters(hostId);
        parameters.setRollbackOnFailure(true);
        parameters.setShouldBeLogged(false);

        parameters.setNetworkNames(getNetworkNames(networks));

        List<NetworkAttachment> networkAttachmentsToSync = getNetworkAttachmentsToSync(hostId, networks);
        parameters.getNetworkAttachments().addAll(networkAttachmentsToSync);

        return parameters;
    }

    /**
     * @param networks networks to be transformed to comma separated list of its names.
     * @return comma separated list of network names.
     */
    private String getNetworkNames(List<Network> networks) {
        return networks.stream().map(Network::getName).collect(Collectors.joining(", "));
    }

    /**
     * For given host and it's networks, return network attachments representing those networks on this host.
     * @param hostId host ID
     * @param networks networks to transform
     * @return network attachments representing given networks on this host.
     */
    private List<NetworkAttachment> getNetworkAttachmentsToSync(Guid hostId, List<Network> networks) {
        List<NetworkAttachment> allAttachmentsOfHost = networkAttachmentDao.getAllForHost(hostId);
        Map<Guid, NetworkAttachment> attachmentsByNetworkId = new MapNetworkAttachments(allAttachmentsOfHost).byNetworkId();

        List<NetworkAttachment> attachmentsToSync = new ArrayList<>();
        for (Network network : networks) {
            Guid networkId = network.getId();
            NetworkAttachment attachmentToSync = attachmentsByNetworkId.get(networkId);
            if (!attachmentToSync.isQosOverridden()) {
                attachmentToSync.setOverrideConfiguration(true);
                attachmentsToSync.add(attachmentToSync);
            }
        }

        return attachmentsToSync;
    }


    /**
     * method finds all VDS records having any of given networks and creates VdsID->NetworksIDs mapping for all such VDS
     * and Network records.
     * @param networks networks to search for
     *
     * @return mapping of VDS ID to list of network ids, where given network ids are only those of networks specified in
     * parameters.
     */
    private Map<Guid, List<Network>> getHostIdToNetworksMap(List<Network> networks) {
        Map<Guid, List<Network>> networksPerHostId = new HashMap<>();
        for (Network network : networks) {
            List<VDS> hostRecordsForNetwork = vdsDao.getAllForNetwork(network.getId());
            for (VDS host : hostRecordsForNetwork) {
                MultiValueMapUtils.addToMap(host.getId(), network, networksPerHostId);
            }
        }
        return networksPerHostId;
    }
}

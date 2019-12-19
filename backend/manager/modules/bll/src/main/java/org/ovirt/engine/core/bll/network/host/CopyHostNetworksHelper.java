package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

class CopyHostNetworksHelper {

    private Set<Guid> attachmentsToRemove = new HashSet<>();
    private Set<Guid> bondsToRemove = new HashSet<>();
    private List<NetworkAttachment> attachmentsToApply = new ArrayList<>();
    private List<CreateOrUpdateBond> bondsToApply = new ArrayList<>();
    private Map<String, List<String>> destinationBondSlavesByName = new HashMap<>();

    private InterfaceConfigurationMapper sourceMapper;
    private InterfaceConfigurationMapper destinationMapper;
    private Map<Nic, Nic> nicsMap;

    CopyHostNetworksHelper(List<VdsNetworkInterface> sourceInterfaces,
            List<NetworkAttachment> sourceAttachments,
            List<VdsNetworkInterface> destinationInterfaces,
            List<NetworkAttachment> destinationAttachments) {
        sourceMapper = new InterfaceConfigurationMapper(sourceInterfaces, sourceAttachments);
        destinationMapper = new InterfaceConfigurationMapper(destinationInterfaces, destinationAttachments);
    }

    void buildDestinationConfig() {
        nicsMap = createNicsMap();
        clearDestinationHost();
        prepareBondsAndCopyNicAttachments();
        createDestinationBondDefinitionsAndCopyAttachments();
        reuseAttachmentsOnDestination();
    }

    int getSourceNicsCount() {
        return sourceMapper.getNicsSortedByName().size();
    }

    int getDestinationNicsCount() {
        return destinationMapper.getNicsSortedByName().size();
    }

    Set<Guid> getAttachmentsToRemove() {
        return attachmentsToRemove;
    }

    Set<Guid> getBondsToRemove() {
        return bondsToRemove;
    }

    List<CreateOrUpdateBond> getBondsToApply() {
        return bondsToApply;
    }

    List<NetworkAttachment> getAttachmentsToApply() {
        return attachmentsToApply;
    }

    /**
     * Clear all destination bonds and attachments
     */
    private void clearDestinationHost() {
        nicsMap.values().forEach(nic -> {
            Guid idToRemove = nic.getId();
            if (nic.isPartOfBond()) {
                idToRemove = destinationMapper.calcInterfacesIdByName().get(nic.getBondName());
                bondsToRemove.add(idToRemove);
            }
            List<Guid> destAttachmentIds = calcDestAttachmentsToRemove(idToRemove);
            attachmentsToRemove.addAll(destAttachmentIds);
        });
    }

    /**
     * Prepare destination bonds and their slaves and copy nics attachments
     */
    private void prepareBondsAndCopyNicAttachments() {
        for (Map.Entry<Nic, Nic> nicEntry : nicsMap.entrySet()) {
            Nic sourceNic = nicEntry.getKey();
            Nic destinationNic = nicEntry.getValue();

            // Define bond map for destination slaves
            if (sourceNic.isPartOfBond()) {
                addBondSlaveToBonds(sourceNic, destinationNic.getName());
            }

            // Copy attachments
            attachmentsToApply.addAll(copyRelevantPartsFromNetworkAttachmentsAndModify(sourceNic.getId(),
                    destinationNic.getName()));
        }
    }

    /**
     * Create new bonds or reuse old bonds on destination
     */
    private void createDestinationBondDefinitionsAndCopyAttachments() {
        for (Map.Entry<String, List<String>> bondEntry : destinationBondSlavesByName.entrySet()) {
            String bondName = bondEntry.getKey();
            Bond sourceBond = sourceMapper.getBondByName(bondName);
            CreateOrUpdateBond createOrUpdateBond =
                    createBond(bondName, bondEntry.getValue(), sourceBond.getBondOptions());

            Bond destinationBond = destinationMapper.getBondByName(bondName);
            if (destinationBond != null) {
                Guid bondId = destinationBond.getId();
                bondsToRemove.remove(bondId);
                if (destinationBond.getIsManagement()) {
                    // If destination contains bond with same name that is already attached to ovirtmgmt
                    // we should use different name and do not touch the original bond.
                    List<String> bondNames = new ArrayList<>(destinationBondSlavesByName.keySet());
                    createOrUpdateBond.setName(findNextAvailableBondName(bondNames));
                } else {
                    createOrUpdateBond.setId(bondId);
                }

            }

            bondsToApply.add(createOrUpdateBond);

            // Copy attachments
            attachmentsToApply.addAll(copyRelevantPartsFromNetworkAttachmentsAndModify(sourceBond.getId(),
                    createOrUpdateBond.getName()));
        }
    }

    /**
     * Reuse destination network attachment if the same network was linked to it as the one on the source
     * because we need to preserve the attachment id for setup networks. Otherwise it will fail the validation
     * of the setup
     */
    private void reuseAttachmentsOnDestination() {
        Map<Guid, Guid> destinationAttachmentIdsByNetworkId = destinationMapper.calcAttachmentIdsByNetworkId();
        for (NetworkAttachment attachment : attachmentsToApply) {
            Guid networkId = attachment.getNetworkId();
            if (destinationAttachmentIdsByNetworkId.containsKey(networkId)) {
                Guid attachmentId = destinationAttachmentIdsByNetworkId.get(networkId);
                attachmentsToRemove.remove(attachmentId);
                attachment.setId(attachmentId);
            }
        }
    }

    private List<Guid> calcDestAttachmentsToRemove(Guid interfaceId) {
        Map<Guid, List<NetworkAttachment>> attachments = destinationMapper.calcAttachmentsByInterfaceId();
        if (attachments.containsKey(interfaceId)) {
            return attachments.get(interfaceId)
                    .stream()
                    .map(NetworkAttachment::getId)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Map<Nic, Nic> createNicsMap() {
        List<Nic> sourceNics = sourceMapper.getNicsSortedByName();
        List<Nic> destinationNics = destinationMapper.getNicsSortedByName();
        return IntStream.range(0, sourceNics.size())
                .boxed()
                .collect(Collectors.toMap(sourceNics::get, destinationNics::get));
    }

    private String findNextAvailableBondName(List<String> names) {
        names.sort(String::compareTo);
        String index = names
                .get(names.size() - 1)
                .substring(BusinessEntitiesDefinitions.BOND_NAME_PREFIX.length());
        return BusinessEntitiesDefinitions.BOND_NAME_PREFIX + (Integer.parseInt(index) + 1);
    }

    private void addBondSlaveToBonds(Nic sourceNic, String destinationNicName) {
        String sourceBondName = sourceNic.getBondName();
        if (!destinationBondSlavesByName.containsKey(sourceBondName)) {
            destinationBondSlavesByName.put(sourceBondName, new ArrayList<>());
        }
        destinationBondSlavesByName.get(sourceBondName).add(destinationNicName);
    }

    private CreateOrUpdateBond createBond(String bondName, List<String> bondSlaves, String bondOptions) {
        var bond = new CreateOrUpdateBond();
        bond.setName(bondName);
        bond.setBondOptions(bondOptions);
        bond.setSlaves(new HashSet<>(bondSlaves));
        return bond;
    }

    private List<NetworkAttachment> copyRelevantPartsFromNetworkAttachmentsAndModify(Guid sourceId,
            String destinationName) {
        Map<Guid, List<NetworkAttachment>> sourceAttachmentsByInterfaceId = sourceMapper.calcAttachmentsByInterfaceId();
        if (sourceAttachmentsByInterfaceId.containsKey(sourceId)) {
            return sourceAttachmentsByInterfaceId.get(sourceId)
                    .stream()
                    .map(sourceAttachment ->
                            copyRelevantPartFromNetworkAttachmentAndModify(sourceAttachment, destinationName))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private NetworkAttachment copyRelevantPartFromNetworkAttachmentAndModify(NetworkAttachment sourceAttachment,
            String destinationName) {
        var destinationAttachment = new NetworkAttachment();
        destinationAttachment.setNetworkId(sourceAttachment.getNetworkId());
        destinationAttachment.setNicName(destinationName);
        destinationAttachment.setIpConfiguration(
                copyRelevantPartFromIpConfigAndModify(sourceAttachment.getIpConfiguration())
        );
        return destinationAttachment;
    }

    private IpConfiguration copyRelevantPartFromIpConfigAndModify(IpConfiguration sourceIpConfig) {
        var destinationIpConfig = new IpConfiguration();
        if (sourceIpConfig.hasIpv4PrimaryAddressSet()) {
            var destinationIpv4Config = new IPv4Address();
            Ipv4BootProtocol ipv4ConfigBootProto = sourceIpConfig.getIpv4PrimaryAddress().getBootProtocol();
            if (ipv4ConfigBootProto == Ipv4BootProtocol.STATIC_IP) {
                destinationIpv4Config.setBootProtocol(Ipv4BootProtocol.NONE);
            } else {
                destinationIpv4Config.setBootProtocol(ipv4ConfigBootProto);
            }
            destinationIpConfig.setIPv4Addresses(Collections.singletonList(destinationIpv4Config));
        }
        if (sourceIpConfig.hasIpv6PrimaryAddressSet()) {
            var destinationIpv6Config = new IpV6Address();
            Ipv6BootProtocol ipv6BootProtocol = sourceIpConfig.getIpv6PrimaryAddress().getBootProtocol();
            if (ipv6BootProtocol == Ipv6BootProtocol.STATIC_IP) {
                destinationIpv6Config.setBootProtocol(Ipv6BootProtocol.NONE);
            } else {
                destinationIpv6Config.setBootProtocol(ipv6BootProtocol);
            }
            destinationIpConfig.setIpV6Addresses(Collections.singletonList(destinationIpv6Config));
        }
        return destinationIpConfig;
    }
}

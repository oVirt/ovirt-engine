package org.ovirt.engine.core.bll.network.host;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

class InterfaceConfigurationMapper {
    private List<VdsNetworkInterface> interfaces;
    private List<NetworkAttachment> attachments;
    private Map<Guid, List<NetworkAttachment>> attachmentsByInterfaceId;
    private Map<String, Bond> bondsByName;
    private List<Nic> nicsSortedByName;
    private Map<String, Guid> interfacesIdByName;
    private Map<Guid, Guid> attachmentIdsByNetworkId;

    InterfaceConfigurationMapper(List<VdsNetworkInterface> interfaces,
            List<NetworkAttachment> attachments) {
        this.interfaces = interfaces;
        this.attachments = attachments;
        this.calcBondsByName();
    }

    List<VdsNetworkInterface> getInterfaces() {
        return interfaces;
    }

    List<NetworkAttachment> getAttachments() {
        return attachments;
    }

    Map<Guid, List<NetworkAttachment>> calcAttachmentsByInterfaceId() {
        if (attachmentsByInterfaceId == null) {
            attachmentsByInterfaceId = getAttachments()
                    .stream()
                    .collect(Collectors.groupingBy(NetworkAttachment::getNicId));
        }
        return attachmentsByInterfaceId;
    }

    List<Nic> getNicsSortedByName() {
        if (nicsSortedByName == null) {
            nicsSortedByName = filterNicsWithoutManagement(getInterfaces());
            nicsSortedByName.sort(Comparator.comparing(VdsNetworkInterface::getName));
        }
        return nicsSortedByName;
    }

    Map<String, Guid> calcInterfacesIdByName() {
        if (interfacesIdByName == null) {
            interfacesIdByName = interfaces
                    .stream()
                    .collect(Collectors.toMap(VdsNetworkInterface::getName, NetworkInterface::getId));
        }
        return interfacesIdByName;
    }

    Map<Guid, Guid> calcAttachmentIdsByNetworkId() {
        if (attachmentIdsByNetworkId == null) {
            attachmentIdsByNetworkId = attachments
                    .stream()
                    .collect(Collectors.toMap(NetworkAttachment::getNetworkId, NetworkAttachment::getId));
        }
        return attachmentIdsByNetworkId;
    }

    Bond getBondByName(String name) {
        return bondsByName.get(name);
    }

    private void calcBondsByName() {
        if (bondsByName == null) {
            bondsByName = getInterfaces()
                    .stream()
                    .filter(VdsNetworkInterface::isBond)
                    .map(iface -> (Bond) iface)
                    .collect(Collectors.toMap(VdsNetworkInterface::getName, Function.identity()));
        }
    }

    private List<Nic> filterNicsWithoutManagement(List<VdsNetworkInterface> vdsNetworkInterfaces) {
        return  filterInterfacesWithoutManagment(vdsNetworkInterfaces)
                .stream()
                .filter(noBondAndVlanPredicate())
                .map(nic -> (Nic) nic)
                .collect(Collectors.toList());
    }

    private List<VdsNetworkInterface> filterInterfacesWithoutManagment(List<VdsNetworkInterface> vdsNetworkInterfaces) {
        Optional<VdsNetworkInterface> mgmtInterface = vdsNetworkInterfaces.stream()
                .filter(VdsNetworkInterface::getIsManagement)
                .findFirst();

        if (mgmtInterface.isEmpty()) {
            return vdsNetworkInterfaces;
        }

        Stream<VdsNetworkInterface> interfaces = vdsNetworkInterfaces.stream();
        if (mgmtInterface.get().isBond()) {
            interfaces = interfaces
                    .filter(iface -> !iface.isPartOfBond(mgmtInterface.get().getName()));
        }
        return interfaces
                .filter(iface -> !iface.getIsManagement())
                .collect(Collectors.toList());
    }

    private Predicate<VdsNetworkInterface> noBondAndVlanPredicate() {
        return iface -> !iface.isBond() && iface.getVlanId() == null;
    }
}

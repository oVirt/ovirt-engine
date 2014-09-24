package org.ovirt.engine.core.bll.network.host;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

/**
 * Network interface can be provided as a parameter and identified either by ID or by name.<br>
 * Class {@code NetworkAttachmentCompleter} populates the interface name if it was omitted from the
 * {@link NetworkAttachment} entity or from {@link Bond} entity.
 */
public class NetworkAttachmentCompleter {

    private List<VdsNetworkInterface> existingNics;

    public NetworkAttachmentCompleter(List<VdsNetworkInterface> existingNics) {
        this.existingNics = existingNics;
    }

    public void completeNicName(NetworkAttachment attachment) {
        completeNicNames(Collections.singletonList(attachment));
    }

    public void completeNicNames(List<NetworkAttachment> networkAttachments) {
        BusinessEntityMap<VdsNetworkInterface> nics = new BusinessEntityMap<VdsNetworkInterface>(existingNics);
        for (NetworkAttachment attachment : networkAttachments) {
            VdsNetworkInterface nic = nics.get(attachment.getNicId(), attachment.getNicName());
            if (nic != null) {
                attachment.setNicName(nic.getName());
            }
        }
    }

    public void completeBondNames(List<Bond> bonds) {
        Map<Guid, VdsNetworkInterface> nicsById = Entities.businessEntitiesById(existingNics);
        for (Bond bond : bonds) {
            if (bond.getName() == null && nicsById.containsKey(bond.getId())) {
                bond.setName(nicsById.get(bond.getId()).getName());
            }
        }
    }
}

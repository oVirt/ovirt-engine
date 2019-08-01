package org.ovirt.engine.core.bll.network.host;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

/**
 * Network interface can be provided as a parameter and identified either by ID or by name.<br>
 * Class {@code NetworkAttachmentCompleter} populates the interface name or id if it was omitted from the
 * {@link NetworkAttachment} entity or from {@link org.ovirt.engine.core.common.businessentities.network.Bond} entity. If both were set, their coherence is verified.
 */
public class NicNameNicIdCompleter {

    private BusinessEntityMap<VdsNetworkInterface> existingNicsMap;

    public NicNameNicIdCompleter(List<VdsNetworkInterface> existingNics) {
        existingNicsMap = new BusinessEntityMap<>(existingNics);
    }

    public void completeNetworkAttachments(List<NetworkAttachment> networkAttachments) {
        NicNameAndNicIdAccessors.FromNetworkAttachment accessors = new NicNameAndNicIdAccessors.FromNetworkAttachment();
        for (NetworkAttachment attachment : networkAttachments) {
            complete(accessors.setNetworkAttachment(attachment));
        }
    }

    public void completeBonds(List<CreateOrUpdateBond> bonds) {
        NicNameAndNicIdAccessors.FromCreateOrUpdateBondData accessors = new NicNameAndNicIdAccessors.FromCreateOrUpdateBondData();
        for (CreateOrUpdateBond bond : bonds) {
            complete(accessors.setCreateOrUpdateBond(bond));
        }
    }

    /**
     * Fills either missing {@link NetworkAttachment#nicId} or {@link NetworkAttachment#nicName}. Missing datum is get
     * from instance located using other datum. If both data are provided, their coherence is verified.
     *
     * @param attachment attachment to complete.
     * @throws IllegalArgumentException when both id and name are provided but are incoherent.
     */
    public void completeNetworkAttachment(NetworkAttachment attachment) {
        complete(new NicNameAndNicIdAccessors.FromNetworkAttachment(attachment));
    }

    /**
     * Fills either missing {@link org.ovirt.engine.core.common.businessentities.network.Bond#id} or
     * {@link org.ovirt.engine.core.common.businessentities.network.Bond#name}. Missing datum is get
     * from instance located using other datum. If both data are provided, their coherence is verified.
     *
     * @param bond bond to complete.
     *
     * @throws IllegalArgumentException when both id and name are provided but are incoherent.
     */
    public void completeCreateOrUpdateBondData(CreateOrUpdateBond bond) {
        complete(new NicNameAndNicIdAccessors.FromCreateOrUpdateBondData(bond));
    }

    public void completeLabels(Set<NicLabel> labels) {
        NicNameAndNicIdAccessors.FromNicLabel accessors = new NicNameAndNicIdAccessors.FromNicLabel();
        for (NicLabel label : labels) {
            complete(accessors.setNicLabel(label));
        }
    }

    void complete(NicNameAndNicIdAccessors accessors) {
        Guid targetId = accessors.getId();
        String targetName = accessors.getName();

        VdsNetworkInterface existingInterface = existingNicsMap.get(targetId, targetName);
        if (shouldUpdateIdAndName(targetId, targetName, existingInterface)) {
            accessors.setName(existingInterface.getName());
            accessors.setId(existingInterface.getId());
        }
    }

    private boolean shouldUpdateIdAndName(Guid targetNicId, String targetNicName, VdsNetworkInterface existingInterface) {
        boolean bothIdentificationSet = targetNicId != null && targetNicName != null;
        return !bothIdentificationSet && existingInterface != null;
    }

    interface NicNameAndNicIdAccessors {

        String getName();

        NicNameAndNicIdAccessors setName(String name);

        Guid getId();

        NicNameAndNicIdAccessors setId(Guid id);

        class FromCreateOrUpdateBondData implements NicNameAndNicIdAccessors {
            private CreateOrUpdateBond createOrUpdateBond;

            public FromCreateOrUpdateBondData() {
            }

            public FromCreateOrUpdateBondData(CreateOrUpdateBond createOrUpdateBond) {
                this.createOrUpdateBond = createOrUpdateBond;
            }

            @Override
            public String getName() {
                return createOrUpdateBond.getName();
            }

            @Override
            public NicNameAndNicIdAccessors setName(String name) {
                createOrUpdateBond.setName(name);
                return this;
            }

            @Override
            public Guid getId() {
                return createOrUpdateBond.getId();
            }

            @Override
            public NicNameAndNicIdAccessors setId(Guid id) {
                createOrUpdateBond.setId(id);
                return this;
            }

            public FromCreateOrUpdateBondData setCreateOrUpdateBond(CreateOrUpdateBond createOrUpdateBond) {
                this.createOrUpdateBond = createOrUpdateBond;
                return this;
            }
        }

        class FromNetworkAttachment implements NicNameAndNicIdAccessors {
            private NetworkAttachment networkAttachment;

            public FromNetworkAttachment() {
            }

            public FromNetworkAttachment(NetworkAttachment networkAttachment) {
                this.networkAttachment = networkAttachment;
            }

            @Override
            public String getName() {
                return networkAttachment.getNicName();
            }

            @Override
            public NicNameAndNicIdAccessors setName(String name) {
                networkAttachment.setNicName(name);
                return this;
            }

            @Override
            public Guid getId() {
                return networkAttachment.getNicId();
            }

            @Override
            public NicNameAndNicIdAccessors setId(Guid id) {
                networkAttachment.setNicId(id);
                return this;
            }

            public FromNetworkAttachment setNetworkAttachment(NetworkAttachment networkAttachment) {
                this.networkAttachment = networkAttachment;
                return this;
            }
        }

        class FromNicLabel implements NicNameAndNicIdAccessors {
            private NicLabel nicLabel;

            public FromNicLabel() {
            }

            public FromNicLabel(NicLabel nicLabel) {
                this.nicLabel = nicLabel;
            }

            @Override
            public String getName() {
                return nicLabel.getNicName();
            }

            @Override
            public NicNameAndNicIdAccessors setName(String name) {
                nicLabel.setNicName(name);
                return this;
            }

            @Override
            public Guid getId() {
                return nicLabel.getNicId();
            }

            @Override
            public NicNameAndNicIdAccessors setId(Guid id) {
                nicLabel.setNicId(id);
                return this;
            }

            public FromNicLabel setNicLabel(NicLabel nicLabel) {
                this.nicLabel = nicLabel;
                return this;
            }
        }
    }
}

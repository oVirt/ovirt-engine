package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

//TODO MM: I know this is wrong. Please advise me, how to pass these structures around, access them and properly maintain labels collections. This code is so convoluted, that I do not have courage to touch it.
public class DataFromHostSetupNetworksModel {
    public List<VdsNetworkInterface> allNics;
    public List<NetworkAttachment> existingNetworkAttachments;

    public List<LabelOnNic> addedLabels = new ArrayList<>();
    public List<LabelOnNic> removedLabels = new ArrayList<>();

    public List<NetworkAttachment> newOrModifiedNetworkAttachments = new ArrayList<>();
    public List<NetworkAttachment> removedNetworkAttachments = new ArrayList<>();
    public List<Bond> newOrModifiedBonds = new ArrayList<>();
    public List<Bond> removedBonds = new ArrayList<>();
    public Set<String> removedUnmanagedNetworks = new HashSet<>();

    public DataFromHostSetupNetworksModel() {
    }

    public DataFromHostSetupNetworksModel(List<VdsNetworkInterface> allNics,
            List<NetworkAttachment> existingNetworkAttachments) {
        this.allNics = allNics;
        this.existingNetworkAttachments = existingNetworkAttachments;
    }

    public static final class LabelOnNic {
        private Guid nicId;
        private String nicName;
        private String label;

        public LabelOnNic(Guid nicId, String nicName, String label) {
            this.nicId = nicId;
            this.nicName = nicName;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public Guid getNicId() {
            return nicId;
        }

        public String getNicName() {
            return nicName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof LabelOnNic))
                return false;
            LabelOnNic that = (LabelOnNic) o;
            return Objects.equals(getNicId(), that.getNicId()) &&
                Objects.equals(getNicName(), that.getNicName()) &&
                Objects.equals(getLabel(), that.getLabel());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getNicId(), getNicName(), getLabel());
        }

        public void setNicId(Guid nicId) {
            this.nicId = nicId;
        }
    }
}

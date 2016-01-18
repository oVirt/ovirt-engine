package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public interface InterfacePropertiesAccessor {
    String getAddress();

    String getNetmask();

    String getGateway();

    NetworkBootProtocol getBootProtocol();

    boolean isQosOverridden();

    HostNetworkQos getHostNetworkQos();

    Map<String, String> getCustomProperties();

    class FromNic implements InterfacePropertiesAccessor {
        VdsNetworkInterface nic;

        public FromNic(VdsNetworkInterface nic) {
            this.nic = nic;
        }

        @Override
        public String getAddress() {
            return nic.getIpv4Address();
        }

        @Override
        public String getNetmask() {
            return nic.getIpv4Subnet();
        }

        @Override
        public String getGateway() {
            return nic.getIpv4Gateway();
        }

        @Override
        public NetworkBootProtocol getBootProtocol() {
            return nic.getIpv4BootProtocol();
        }

        @Override
        public HostNetworkQos getHostNetworkQos() {
            return nic.getQos();
        }

        @Override
        public boolean isQosOverridden() {
            return false;
        }

        @Override
        public Map<String, String> getCustomProperties() {
            return null;
        }
    }

    class FromNetworkAttachment implements InterfacePropertiesAccessor {
        NetworkAttachment networkAttachment;
        IPv4Address iPv4Address;
        HostNetworkQos networkQos;

        public FromNetworkAttachment(NetworkAttachment networkAttachment, HostNetworkQos networkQos) {
            this.networkAttachment = networkAttachment;
            this.iPv4Address = networkAttachment.getIpConfiguration() == null ? null
                    : networkAttachment.getIpConfiguration().hasIpv4PrimaryAddressSet()
                            ? networkAttachment.getIpConfiguration().getIpv4PrimaryAddress() : null;
            this.networkQos = networkQos;
        }

        @Override
        public String getAddress() {
            return iPv4Address.getAddress();
        }

        @Override
        public String getNetmask() {
            return iPv4Address.getNetmask();
        }

        @Override
        public String getGateway() {
            return iPv4Address.getGateway();
        }

        @Override
        public NetworkBootProtocol getBootProtocol() {
            return iPv4Address.getBootProtocol();
        }

        @Override
        public HostNetworkQos getHostNetworkQos() {
            if (networkAttachment.isQosOverridden()) {
                return networkAttachment.getHostNetworkQos();
            } else {
                return networkQos;
            }
        }

        @Override
        public boolean isQosOverridden() {
            return networkAttachment.isQosOverridden();
        }

        @Override
        public Map<String, String> getCustomProperties() {
            return networkAttachment.getProperties();
        }
    }
}

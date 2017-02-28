package org.ovirt.engine.core.bll.network.host;

import java.util.Collection;
import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;

public class IpConfigurationCompleter {

    public void fillInUnsetIpConfigs(Collection<NetworkAttachment> networkAttachments) {
        networkAttachments
                .forEach(attachment -> {
                    if (attachment.getIpConfiguration() == null) {
                        attachment.setIpConfiguration(NetworkCommonUtils.createDefaultIpConfiguration());
                    } else {
                        completeDefaultIpConfiguration(attachment.getIpConfiguration());
                    }
                });
    }

    private void completeDefaultIpConfiguration(IpConfiguration ipConfiguration) {
        if (ipConfiguration.getIPv4Addresses().isEmpty()) {
            final IPv4Address ipv4Address = NetworkCommonUtils.createDefaultIpv4Address();
            ipConfiguration.setIPv4Addresses(Collections.singletonList(ipv4Address));
        }
        if (ipConfiguration.getIpV6Addresses().isEmpty()) {
            final IpV6Address ipv6Address = NetworkCommonUtils.createDefaultIpv6Address();
            ipConfiguration.setIpV6Addresses(Collections.singletonList(ipv6Address));
        }
    }
}

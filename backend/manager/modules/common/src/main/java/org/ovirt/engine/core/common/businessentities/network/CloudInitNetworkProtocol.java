package org.ovirt.engine.core.common.businessentities.network;

/**
 * <pre>
 * The protocol of the input of the network configuration to cloud-init.
 * Cloud-init supports several source protocols as described in
 * http://cloudinit.readthedocs.io/en/latest/topics/network-config.html#network-configuration-sources
 *
 * ENI has become a legacy protocol that cannot support IPv6.
 * The Openstack Metadata protocol is a successor. It is described in
 * https://specs.openstack.org/openstack/nova-specs/specs/liberty/implemented/metadata-service-network-info.html
 * </pre>
 */
public enum CloudInitNetworkProtocol {
    OPENSTACK_METADATA, ENI
}

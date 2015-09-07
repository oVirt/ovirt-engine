# Introduction

This document contains miscellaneous information about the oVirt REST
API.

## Backwards compatibility breaking changes in oVirt 4.0

This section enumerates the backwards compatibility breaking changes
that have been done to the RESTAPI in version 4.0 of the engine.

### Removed YAML support

The support for YAML has been completely removed.

### Remove the NIC `network` and `port_mirroring` properties

The NIC `network` and `port_mirroring` elements have been replaced by
the `vnic_profile` element, so when creating or updating a NIC instead
of specifying the network and port mirroring configuration, these are
previusly specified creating a VNIC profile:

    POST /vnicprofiles
    <vnic_profile>
      <name>myprofile</name>
      <network id="..."/>
      <port_mirroring>true</port_mirroring>
    </vnic_profile>

And then the NIC is created or referencing the existing VNIC profile:

    PUT /vms/{vm:id}/nics/{nic:id}
    <nic>
      <vnic_profile id="/vnicprofiles/...">
    </nic>

The old elements and their meaning were preserved for backwards
compatibility, but they have now been completely removed.

Note that the `network` element hasn't been removed from the XML schema
because it is still used by the `initialization` element, but it will be
completely ignored if provided when creating or updating a NIC.

### Remove the NIC `active` property

The NIC `active` property was replaced by `plugged` some time ago. It
has been completely removed now.

### Remove the disk `type` property

The `type` property of disks has been removed, but kept in the XML
schema and ignored. It has been completely removed now.

### Remove the disk `size` property

The disk `size` property has been replaced by `provisioned_size` long
ago. It has been completely removed now.

### Removed support for pinning a VM to a single host

Before version 3.6 the API had the possibility to pin a VM to a single
host, using the `placement_policy` element of the VM entity:

    PUT /vms/{vm:id}
    <vm>
      <placement_policy>
        <host id="{host:id}/">
      </placement_policy>
    <vm>

In version 3.6 this capability was enhanced to support multiple
hosts, and to do so a new `hosts` element was added:

    PUT /vms/{vm:id}
    <vm>
      <placement_policy>
        <hosts>
          <host id="{host:id}"/>
          <host id="{host:id}"/>
          ...
        </hosts>
      </placement_policy>
    <vm>

To preserve backwards compatibility the single `host` element was
preserved. In 4.0 this has been removed, so applications will need
to use the `hosts` element even if when pinning to a single host.

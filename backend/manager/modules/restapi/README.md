# Introduction

This document contains miscellaneous information about the oVirt REST
API.

## Backwards compatibility breaking changes in oVirt 4.0

This section enumerates the backwards compatibility breaking changes
that have been done to the RESTAPI in version 4.0 of the engine.

### Removed YAML support

The support for YAML has been completely removed.

### Renamed complex types

The following XML schema complex types have been renamed:

- `API` - `Api`
- `CPU` - `Cpu`
- `CPUs` - `Cpus`
- `DNS` - `Dns`
- `HostNICStates` - `HostNicStates`
- `HostNIC` - `HostNic`
- `IO` - `Io`
- `IP` - `Ip`
- `IPs` - `Ips`
- `KSM` - `Ksm`
- `MAC` - `Mac`
- `NIC` - `Nic`
- `PreviewVMs` - `PreviewVms`
- `QoS` - `Qos`
- `QoSs` - `Qoss`
- `RSDL` - `Rsdl`
- `SPM` - `Spm`
- `SSHPublicKey` - `SshPublicKey`
- `SSHPublicKeys` - `SshPublicKeys`
- `SSH` - `Ssh`
- `SupportedVersions` - `Versions`
- `VCpuPin` - `VcpuPin`
- `VLAN` - `Vlan`
- `VM` - `Vm`
- `VMs` - `Vms`
- `VirtIO_SCSI` - `VirtioScsi`

First column is the old name, and second column is the new name.

These renamings don't affect users of the RESTAPI, unless they are using
the XML schema, either directly or indirectly via the Python or Java
SDKs.

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

### Removed the `capabilities.permits` element

The list of permits is version specific, and it has been added to
the `version` element long ago, but it has been kept into the
`capabilities` element as well, just for backwards compatibility.

In 4.0 it has been removed completely from `capabilities`.

### Removed the `storage_manager` element

The `storage_manager` element was replaced by the `spm` element some
time ago. The old one was kept for backwards compatibility, but it has
been completely removed now.

### Removed the data center `storage_type` element

Data centers used to be associated to a specific storage type (NFS,
Fiber Channel, iSCSI, etc) but they have been changed some time so that
there are only two types: with local storage and with shared storage. A
new `local` element was introduced to indicate this, and the old
`storage_type` was element was preserved for backwards compatibility.
This old element has now been completely removed.

### Remove the `timezone` element

The VM resource used to contain a `timezone` element to represent the
time zone. This element only allowed a string:

    <vm>
       <timezone>Europe/Madrid</timezone>
    </vm>

This doesn't allow extension, and as a it was necessary to add the UTC
offset, it was replaced with a new structured `time_zone` element:

    <vm>
      <time_zone>
        <name>Europe/Madrid</name>
        <utc_offset>GMT+1</utc_offset>
      </time_zone>
    </vm>

The old `timezone` element was preserved, but it has been completely
removed now.

### Removed the `guest_info` element

The `guest_info` element was used to hold information gathered by the
guest agent, like the IP addresses and the fully qualified host name.
This information is also available in other places. For example, the IP
addresses are available within VM resource:

    GET /vms/{vm:id}
    <vm>
      <guest_info>
        <ips>
          <ip address="192.168.122.30"/>
        </ips>
        <fqdn>whatever.example.com</fqdn>
      </guest_info>
    </vm>

And also within the NIC resource, using the newer `reported_devices`
element:

    GET /vms/{vm:id}/nics/{nic:id}
    <nic>
      <reported_devices>
        <reported_device>
          <name>eth0</name>
          <mac address="00:1a:4a:b5:4c:94"/>
          <ips>
            <ip address="192.168.1.115" version="v4"/>
            <ip address="fe80::21a:4aff:feb5:4c94" version="v6"/>
            <ip address="::1:21a:4aff:feb5:4c94" version="v6"/>
          </ips>
        </reported_device>
      </reported_devices>
    </nic>

In addition this newer `reported_devices` element provides more complete
information, like multiple IP addresses, MAC addresses, etc.

To remove this duplication the `guest_info` element has been removed.

To support the fully qualified domain name a new `fqdn` element has been
added to the VM resource:

    GET /vms/{vm:id}
    <vm>
      <fqdn>whatever.example.com</fqdn>
    </vms>

This will contain the same information that `guest_info.fqdn` used to
contain.

### Replaced CPU `id` attribute with `type` element

The `cpu` element used to have an `id` attribute that indicates the type
of CPU:

    <cpu id="Intel Conroe Family">
      <architecture>X86_64</architecture>
      ...
    </cpu>

This is in contradiction with the rest of the elements of the RESTAPI
model, where the `id` attribute is used for opaque identifiers. This
`id` attribute has been replaced with a new `type` element:

    <cpu>
      <type>Intel Conroe Family</type>
      <architecture>X86_64</architecture>

### Use elements instead of attributes in CPU topology

In the past the CPU topology element used attributes for its properties:

    <cpu>
      <topology sockets="1" cores="1" threads="1"/>
      ...
    </cpu>

This is contrary to the common practice in the RESTAPI. They have been
replaced by inner elements:

    <cpu>
      <topology>
        <sockets>1<sockets>
        <cores>1<cores>
        <threads>1<threads>
      </topology>
      ...
    </cpu>

### Use elements instead of attributes in VCPU pin

In the past the VCPU pin element used attributes for its properties:

    <cpu_tune>
      <vcpu_pin vcpu="0" cpu_set="0"/>
    </cpu_tune>

This is contrary to the common practice in the RESTAPI. They have been
replaced by inner elements:

    <cpu_tune>
      <vcpu_pin>
        <vcpu>0</vcpu>
        <cpu_set>0</cpu_set>
      </vcpu_pin>
    </cpu_tune>

### Use elements instead of attributes in VCPU pin

In the past the `version` element used attributes for its properties:

    <version major="3" minor="5" ../>

This is contrary to the common practice in the RESTAPI. They have been
replaced by inner elements:

    <version>
      <major>3</minor>
      <minor>5</minor>
      ...
    </version>

### Use elements instead of attributes in memory overcommit

In the past the `overcommit` element used attributes for its properties:

    <memory_policy>
      <overcommit percent="100"/>
      ...
    </memory_policy>

This is contrary to the common practice in the RESTAPI. They have been
replaced by inner elements:

    <memory_policy>
      <overcommit>
        <percent>100</percent>
      </overcommit>
      ...
    </memory_policy>

### Use elements instead of attributes in `console`

In the past the `console` element used attributes for its properties:

    <console enabled="true"/>

This is contrary to the common practice in the RESTAPI. They have been
replaced by inner elements:

    <console>
      <enabled>true</enabled>
    </console>

### Use elements instead of attributes in VIRTIO SCSI

In the past the VIRTIO ISCSI element used attributes for its properties:

    <virtio_scsi enabled="true"/>

This is contrary to the common practice in the RESTAPI. They have been
replaced by inner elements:

    <virtio_scsi>
      <enabled>true</enabled>
    </virtio_scsi>

### Use element instead of attribute for power management agent `type`

The power management `type` property was represented as an attribute:

    <agent type="apc">
      <username>myuser</username>
      ...
    </agent>

This is contrary to the common practice in the RESTAPI. It has been
replaced with an inner element:

    <agent>
      <type>apc</type>
      <username>myuser</username>
      ...
    </agent>

### Use elements instead of attributes in power management agent options

In the past the power management agent options element used attributes
for its properties:

    <options>
      <option name="port" value="22"/>
      <option name="slot" value="5"/>
      ...
    </options>

This is contrary to the common practice in the RESTAPI. They have been
replaced with inner elements:

    <options>
      <option>
        <name>port</name>
        <value>22</value>
      </option>
      <option>
        <name>slot</name>
        <value>5</value>
      </option>
      ...
    </options>

### Use elements instead of attributes in IP address:

In the past the IP address element used attributes for its properties:

    <ip address="192.168.122.1" netmask="255.255.255.0"/>

This is contrary to the common practice in the RESTAPI. They have been
replaced with inner elements:

    <ip>
      <address>192.168.122.1</address>
      <netmask>255.255.255.0</netmask>
    </ip>

### Use elements instead of attributes in MAC address:

In the past the MAC address element used attributes for its properties:

    <mac address="66:f2:c5:5f:bb:8d"/>

This is contrary to the common practice in the RESTAPI. They have been
replaced by inner elements:

    <mac>
      <address>66:f2:c5:5f:bb:8d</address>
    </mac>

### Use elements instead of attributes in boot device:

In the past the boot device element used attributes for its properties:

    <boot dev="cdrom"/>

This is contrary to the common practice in the RESTAPI. They have been
replaced by inner elements:

    <boot>
      <dev>cdrom</dev>
    </boot>

### Use element instead of attribute for operating system `type`

The operating system `type` property was represented as an attribute:

    <os type="other">
      ...
    </os>

This is contrary to the common practice in the RESTAPI. It has been
replaced with an inner element:

    <os>
      <type>other</type>
      ...
    </os>

### Removed the `force` parameter from the request to retrieve a host

The request to retrieve a host used to support a `force` matrix
parameter to indicate that the data of the host should be refreshed
(calling VDSM to reload host capabilities and devices) before retrieving
it from the database:

    GET /hosts/{host:id};force

This `force` parameter has been superseded by the host `refresh` action,
but kept for backwards compatibility. It has been completely removed
now. Applications that require this functionality should perform two
requests, first one to refresh the host:

    POST /hosts/{host:id}/refresh
    <action/>

And then one to retrieve it, without the `force` parameter:

   GET /hosts/{host:id}

### Removed deprecated host power management configuration

The host power management configuration used to be part of the host
resource, using embedded configuration elements:

    <power_management type="apc">
      <enabled>true</enabled>
      <address>myaddress</address>
      <username>myaddress</username>
      <options>
        <option name="port" value="22/>
        </option name="slot" value="5/>
      </options>
      ...
    </power_management>

This has been changed some time ago, in order to support multiple power
management agents, introducing a new
`/hosts/{host:id}fenceagents`collection.

The old `type` attribute, the old `address`, `username` and `password`
elements, and the inner `agents` element directly inside
`power_management` were preserved for backwards compatibility. All these
elements have been completely removed, so the only way to query or
modify the power management agents is now the
`/hosts/{host:id}/fenceagents` sub-collection.

### Use multiple `boot.devices.device` instead of multiple `boot`

In the past the way to specify the boot sequence when starting a virtual
machine was to use multiple `boot` elements, each containing a `dev`
element. For example, to specify that the virtual machine should first
try to boot from CDROM and then from hard disk the following request was
used:

    POST /vms/{vm:id}/start
    <action>
      <vm>
        ...
        <boot>
          <dev>cdrom</dev>
        </boot>
        <boot>
          <dev>hd</dev>
        </boot>
      </vm>
    </action>

The common practice in other parts of the API is to represent arrays
with a wrapper element. In that case that wrapper element could be named
`boots`, but that doesn't make much sense, as what can have multiple
values here is the boot device, not the boot sequence. To fix this
inconsistence this has been replaced with a single `boot` element that
can contain multiple devices:

    POST /vms/{vm:id}/start
    <action>
      <vm>
        ...
        <boot>
          <devices>
            <device>cdrom</device>
            <device>hd</device>
          </devices>
        </boot>
      </vm>
    </action>

### Removed the `disks.clone` and `disks.detach_only` elements

These elements aren't really part of the representation of disks, but
parameters of the operations to add and remove virtual machines.

The `disks.clone` element was used to indicate that the disks of a new
virtual machine have to be cloned:

    POST /vms
    <vm>
      ...
      <disks>
        <clone>true</clone>
      </disks>
    <vm>

This has been now removed, and replaced by a new `clone` matrix
parameter:

    POST /vms;clone=true
    <vm>
      ...
    </vm>

The `disks.detach_only` element was used to indicate that when removing
a virtual machine the disks don't have to be removed, but just detached
from the virtual machine:

    DELETE /vms/{vm:id}
    <action>
      <vm>
        <disks>
          <detach_only>true</detach_only>
        </disks>
      </vm>
    </action>

This has been now removed, and replaced by a new `detach_only` matrix
parameter:

    DELETE /vms/{vm:id};detach_only=true

### Rename element `vmpool` to `vm_pool`

The names of the elements that represent pools of virtual machines used
to be `vmpool` and `vmpools`. They have been renamed to `vm_pool` and
`vm_pools` in order to have a consistent correspondence between names of
complex types (`VmPool` and `VmPools` in this case) and elements.

### Use `logical_units` instead of multiple `logical_unit`

The logical units that are part of a volume group used to be reported as
an unbounded number of `logical_unit` elements. For example, when
reporting the details of a storage domain:

    GET /storagedomains/{storagedomain:id}
    <storage_domain>
      ...
      <storage>
        ...
        <volume_group>
          <logical_unit>
            <!-- First LU -->
          </logical_unit>
          <logical_unit>
            <!-- Second LU -->
          </logical_unit>
          ...
        </volume_group>
      </storage>
    </storage_domain>

This is contrary to the usual practice in the API, as list of elements
are always wrapped with an element. This has been fixed now, so the list
of logical units will be wrapped with the `logical_units` element:

    GET /storagedomains/{storagedomain:id}
    <storage_domain>
      ...
      <storage>
        ...
        <volume_group>
          <logical_units>
            <logical_unit>
              <!-- First LU -->
            </logical_unit>
            <logical_unit>
              <!-- Second LU -->
            </logical_unit>
            ...
          </logical_units>
        </volume_group>
      </storage>
    </storage_domain>

### Removed the `snapshots.collapse_snapshots` element

This element isn't really part of the representation of snapshots, but
a parameter of the operation that imports a virtual machine from an
export storage domain:

    POST /storagedomains/{sd:id}/vms/{vm:id}/import
    <action>
      <vm>
        <snapshots>
          <collapse_snapshots>true</collapse_snapshots>
        </snapshots>
      </vm>
    </action>

This has been now removed, and replaced by a new `collapse_snapshots`
matrix parameter:

    POST /storagedomains/{sd:id}/vms/{vm:id}/import;collapse_snapshots
    <action/>

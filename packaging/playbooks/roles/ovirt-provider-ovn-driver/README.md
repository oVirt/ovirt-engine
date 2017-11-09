oVirt provider OVN driver
=============================

This role is used for install and configuration of the oVirt provider OVN driver on oVirt hypervisor.

Requirements
------------

 * Ansible version 2.3

Role Variables
--------------

| Name                        | Default value  | Description                                                       |
|-----------------------------|----------------|-------------------------------------------------------------------|
| ovn_engine_cluster_version<br/> alias: <i>host_deploy_cluster_version</i> | 4.2            | The version of the cluster where the host resides.  oVirt provider OVN driver is installed and configured on versions >= 4.2. |
| ovn_central<br/> alias: <i>host_deploy_ovn_central</i>                    | UNDEF          | The IP address of the OVN Central host with OVN Southbound DB. If not set to an valid IP address oVirt provider OVN driver is not installed and configured. |
| ovn_tunneling_interface<br/> alias: <i>host_deploy_ovn_tunneling_interface</i>        | ovirtmgmt      | The host's IP address or the name of the logical network in engine, which will be used to create the OVN tunnels. |

Dependencies
------------

No.

Example Playbook
----------------

```yaml
- name: Install and configure oVirt provider OVN driver
  hosts: hostname

  vars:
    ovn_central: 192.0.2.1

  roles:
    - ovirt-provider-ovn-driver
```

License
-------

Apache License 2.0

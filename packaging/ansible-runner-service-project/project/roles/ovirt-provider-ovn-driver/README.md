oVirt provider OVN driver
=============================

This role is used for install and configuration, or unconfiguration of the oVirt provider OVN driver on an oVirt hypervisor.

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
| ovn_host_fqdn <br/> alias: <i>ovirt_vds_hostname</i> | "" | The host FQDN that will used as OvS system-id on the host. This parameter is supported only on >=4.5 hosts.
| ovn_state | configured | To install and configure the oVirt provider OVN driver, the value of <i>ovn_state</i> should be 'configured'. To unconfigure the provider, the value of <i>ovn_state</i> should be 'unconfigured'.

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

# unconfigure the oVirt provider OVN driver, and stop the OVN controller
# for the controller to be unconfigured, the driver has to be installed
# in the host
- name: Unconfigure the OVN chassis
  hosts: hostname

  roles:
    - role: ovirt-provider-ovn-driver
      ovn_state: unconfigured
```

License
-------

Apache License 2.0

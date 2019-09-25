oVirt host deploy - firewalld
=============================

This role is used for configuration of firewalld on oVirt hypervisor.

Requirements
------------

 * Ansible version 2.0

Role Variables
--------------

| Name                        | Default value  | Description                                                       |
|-----------------------------|----------------|-------------------------------------------------------------------|
| host_deploy_cluster_version | 4.2            | The version of the cluster where the host resides. Based on this version the role will enable cluster version specific firewalld rules. |
| host_deploy_gluster_enabled | false          | If true special virt firewalld rules will be enabled on host.     |
| host_deploy_virt_enabled    | false          | If true special gluster firewalld rules will be enabled on host.  |
| host_deploy_vdsm_port       | 54321          | VDSM port. This port will be enabled by firewalld.                |

Dependencies
------------

No.

Example Playbook
----------------

```yaml
- name: oVirt host deploy - firewalld
  hosts: hostname

  vars:
    host_deploy_cluster_version: 4.2
    host_deploy_gluster_enabled: false
    host_deploy_virt_enabled: true
    host_deploy_vdsm_port: 54321

  roles:
    - ovirt-host-deploy-firewalld
```

License
-------

Apache License 2.0

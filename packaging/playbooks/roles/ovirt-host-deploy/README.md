oVirt host deploy
================

This role will prepare the oVirt hypervisor to be ready to use by oVirt engine.

Requirements
------------

 * Ansible version 2.0

Role Variables
--------------

No.

Dependencies
------------

 * ovirt-host-deploy-facts
 * ovirt-provider-ovn-driver
 * ovirt-host-deploy-libvirt-guests
 * ovirt-host-deploy-firewalld
 * ovirt-host-deploy-vnc-certificates

Example Playbook
----------------

```yaml
- name: oVirt host deploy
  hosts: hostname

  roles:
    - ovirt-host-deploy
```

License
-------

Apache License 2.0

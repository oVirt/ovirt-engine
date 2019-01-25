oVirt host setup - VNC SASL authentication
=============================

This role is used for configuration of SASL authentication for VNC on oVirt
hosts (with scram-sha-1 mechanism).

IMPORTANT: This role restarts libvirtd, and that's safe to perform only on
hosts which are in maintenance mode and not running any VMs.

Requirements
------------

 * Ansible version 2.0

Example Playbook
----------------

```yaml
- name: oVirt host setup - VNC SASL authentication
  hosts: hostname

  roles:
    - ovirt-host-setup-vnc-sasl
```

License
-------

Apache License 2.0

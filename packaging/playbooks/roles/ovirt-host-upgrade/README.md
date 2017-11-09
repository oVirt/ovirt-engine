oVirt Host Upgrade
==================

The `ovirt-host-upgrade` role updates all packages on the host and installs ovirt-host package if it isn't installed.

Requirements
------------

 * Ansible version 2.0

Role Variables
--------------

No.

Dependencies
------------

No.

Example Playbook
----------------

```yaml
---
- name: oVirt Host Upgrade
  hosts: myhost1
  gather_facts: false

  roles:
    - ovirt-host-upgrade
```

License
-------

Apache License 2.0

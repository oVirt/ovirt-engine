oVirt host setup - add migration client certificate
=============================

This role is used to add client certificate links for encrypted
migrations.  Encrypted migrations to RHEL 8.4 or newer hosts do not
work without those links.  Normally, the links are added when
upgrading host or re-enrolling host certificates from the Web UI.
But both those actions require putting the host to maintenance.
If it is not possible then this playbook can be used to add the
required links without the need of bringing the host to maintenance.

Example Playbook
----------------

```yaml
- name: oVirt host setup - add migration client certificate
  hosts: hostname
  roles:
    - ovirt-host-fix-encrypted-migrations
```

License
-------

Apache License 2.0

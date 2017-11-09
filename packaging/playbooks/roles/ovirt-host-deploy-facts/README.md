oVirt host deploy - facts
=============================

This role is used to fetch specific information from oVirt hosts and create a facts.

Requirements
------------

 * Ansible version 2.0

Fatcs created
-------------

| Name                        | Description                                                       |
|-----------------------------|-------------------------------------------------------------------|
| host_deploy_vdsm_version    | Represent a result of running command "rpm -q --qf '%{version}' vdsm" on the host. |

Dependencies
------------

No.

Example Playbook
----------------

```yaml
- name: oVirt host deploy - facts
  hosts: hostname

  roles:
    - ovirt-host-deploy-facts
```

License
-------

Apache License 2.0

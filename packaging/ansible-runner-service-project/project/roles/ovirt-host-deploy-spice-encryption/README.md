oVirt - Configure SPICE encryption
==================================

This role is used for creating a configuration file for SPICE console
with a specified cipher string and protocol.

Requirements
------------

* Ansible version 2.0

Role variables
--------------

| Name                            | Default                                | Description                               |
|---------------------------------|----------------------------------------|-------------------------------------------|
| host_deploy_spice_cipher_string | "TLSv1.2+FIPS:kRSA+FIPS:!eNULL:!aNULL" | Cipher string to pass to crypto libraries |
| host_deploy_spice_protocol      | "ALL,-SSLv2,-SSLv3,-TLSv1,-TLSv1.1"    | Protocol version to pass to crypto libs   |

The default cipher string is the secure option, compatible with FIPS requirements. Customize when
older SPICE clients need to be used.

Dependencies
------------

None.

Example playbook
----------------

```yaml
- name: oVirt - setup weaker SPICE encription for old clients
  hosts: hostname
  vars:
    host_deploy_spice_cipher_string: 'DEFAULT:-RC4:-3DES:-DES'
    host_deploy_spice_protocol: 'ALL,-SSLv2,-SSLv3,-TLSv1,-TLSv1.1'
  roles:
    - ovirt-host-deploy-spice-encryption
```

License
-------

Apache License 2.0

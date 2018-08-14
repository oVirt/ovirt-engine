oVirt host deploy - Encrypted VNC certificates
=============================

This role is used for configuration of TLS certificates for VNC on oVirt hosts.
VNC should use the same certificates as SPICE console.

Requirements
------------

 * Ansible version 2.0

Role Variables
--------------

| Name                                 | Default                       |  Description                                       |
|--------------------------------------|-------------------------------|----------------------------------------------------|
| host_deploy_vnc_tls_x509_cert_dir    | "/etc/pki/vdsm/libvirt-vnc"   | Directory where VNC certificates are to be stored  |
| host_deploy_spice_tls_x509_cert_dir  | "/etc/pki/vdsm/libvirt-spice" | Directory where SPICE certificates are stored      |

The directories should not be usually changed.

Dependencies
------------

`host-deploy` script(s) have been run.

Example Playbook
----------------

```yaml
- name: oVirt host deploy - VNC certificates
  hosts: hostname

  roles:
    - ovirt-host-deploy-vnc-certificates
```

License
-------

Apache License 2.0

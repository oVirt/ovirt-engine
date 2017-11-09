oVirt host deploy - libvirt-guests
=============================

This role is used for configuration of libvirt-guests on oVirt hypervisor.
To make the libvirt-guests play nicely with the VDSM, these options are set:
  - libvirt-guests service is started and enables
  - optionaly set shutdown timeout
  - optionaly enable parallel shutdown

Requirements
------------

 * Ansible version 2.0

Role Variables
--------------

| Name                          | Default |  Description                                       |
|-------------------------------|---------|----------------------------------------------------|
| host_deploy_shutdown_timeout  |     600 | Timeout before systemd starts killing processes    |
| host_deploy_shutdown_parallel |       0 | If set VMs are shutdown in parallel with count set |

libvirt_guests_config is a list used to generate the configuration
file, however it is highly discouraged to change the defaults,
it may brake the functionality.
The default of the list is set to

```yaml
libvirt_guests_config:
  - { option: URIS, value: "qemu+tls://{{ ansible_fqdn }}/system" }
  - { option: ON_BOOT, value: ignore }
  - { option: ON_SHUTDOWN, value: shutdown }
  - { option: PARALLEL_SHUTDOWN, value: "{{ host_deploy_shutdown_parallel }}" }
  - { option: SHUTDOWN_TIMEOUT, value: "{{ host_deploy_shutdown_timeout }}" }
```

Dependencies
------------

No.

Example Playbook
----------------

```yaml
- name: oVirt host deploy - libvirt-guests
  hosts: hostname

  vars:
    host_deploy_shutdown_timeout: 9001
    host_deploy_shutdown_parallel: 0

  roles:
    - ovirt-host-deploy-libvirt-guests
```

License
-------

Apache License 2.0

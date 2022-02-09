package org.ovirt.engine.ui.uicommonweb.help;

public enum HelpTag {

    /*
     *              ******************************************************************************************
     *
     *              NOTE!!!  this file is parsed by a python script, so keep each enum value on the same line,
     *                       one per line
     *
     *              ******************************************************************************************
     *
     */

    SanStorageModelBase("SanStorageModelBase", HelpTagType.UNKNOWN), //$NON-NLS-1$

    add_bricks("add_bricks", HelpTagType.WEBADMIN, "Volumes Tab > Bricks Sub-Tab > Add Brick"), //$NON-NLS-1$ //$NON-NLS-2$

    add_bricks_confirmation("add_bricks_confirmation", HelpTagType.WEBADMIN, "[gluster] In 'Add Bricks' context: this is a confirmation dialog with the following message: 'Multiple bricks of a Replicate volume are present on the same server. This setup is not optimal. Do you still want to continue?'"), //$NON-NLS-1$ //$NON-NLS-2$

    add_event_notification("add_event_notification", HelpTagType.WEBADMIN, "Users Tab > Event Notifier > Add Events Notification"), //$NON-NLS-1$ //$NON-NLS-2$

    add_hosts("add_hosts", HelpTagType.WEBADMIN, "[gluster] In 'New Cluster' context: Allows to add/import Hosts to a newly-created gluster-cluster"), //$NON-NLS-1$ //$NON-NLS-2$

    add_permission_to_user("add_permission_to_user", HelpTagType.WEBADMIN, "Each Main Tab > Permissions Sub-Tab > Add Permission"), //$NON-NLS-1$ //$NON-NLS-2$

    add_provider("add_provider", HelpTagType.WEBADMIN, "[Neutron integration] 'Providers' main tab -> 'Add Provider' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    add_system_permission_to_user("add_system_permission_to_user", HelpTagType.WEBADMIN, "Configure > Add System Permission to User > Add System Permission"), //$NON-NLS-1$ //$NON-NLS-2$

    add_users_and_groups("add_users_and_groups", HelpTagType.WEBADMIN, "Users Tab > Add Users & Groups"), //$NON-NLS-1$ //$NON-NLS-2$

    affinity_groups("affinity_groups", HelpTagType.UNKNOWN), //$NON-NLS-1$

    affinity_labels("affinity_labels", HelpTagType.UNKNOWN), //$NON-NLS-1$

    applications("applications", HelpTagType.UNKNOWN), //$NON-NLS-1$

    guest_containers("guest_containers", HelpTagType.UNKNOWN), //$NON-NLS-1$

    vm_devices("vm_devices", HelpTagType.UNKNOWN), //$NON-NLS-1$

    assign_network("assign_network", HelpTagType.WEBADMIN, "Networks main tab -> Clusters sub-tab -> Manage Network"), //$NON-NLS-1$ //$NON-NLS-2$

    assign_networks("assign_networks", HelpTagType.WEBADMIN, "Cluster Tab > Logical Network Sub-Tab > Manage Networks"), //$NON-NLS-1$ //$NON-NLS-2$

    sync_all_host_networks("sync_all_host_networks", HelpTagType.WEBADMIN, "Host main tab > Networks Interfaces Sub-Tab > Sync All Networks"), //$NON-NLS-1$ //$NON-NLS-2$

    sync_all_cluster_networks("sync_all_cluster_networks", HelpTagType.WEBADMIN, "Cluster main tab > Networks Interfaces Sub-Tab > Sync All Networks"), //$NON-NLS-1$ //$NON-NLS-2$

    assign_tags_hosts("assign_tags_hosts", HelpTagType.WEBADMIN, "Host Tab > Assign Tags"), //$NON-NLS-1$ //$NON-NLS-2$

    assign_tags_users("assign_tags_users", HelpTagType.WEBADMIN, "Users Tab > Assign Tags"), //$NON-NLS-1$ //$NON-NLS-2$

    assign_tags_vms("assign_tags_vms", HelpTagType.WEBADMIN, "VMs Tab > Assign Tags"), //$NON-NLS-1$ //$NON-NLS-2$

    assign_users_and_groups_to_quota("assign_users_and_groups_to_quota", HelpTagType.WEBADMIN, "Quota Tab > Users Sub-Tab > Assign Users/Groups to Quota"), //$NON-NLS-1$ //$NON-NLS-2$

    update_ovfs("update_ovfs", HelpTagType.WEBADMIN, "Storage Tab > Update OVFs"), //$NON-NLS-1$ //$NON-NLS-2$

    bond_network_interfaces("bond_network_interfaces", HelpTagType.WEBADMIN, "Host Tab > Logical Network Sub-Tab > Bond Network Interfaces"), //$NON-NLS-1$ //$NON-NLS-2$

    brick_advanced("brick_advanced", HelpTagType.WEBADMIN, "[gluster] 'Brick Details' dialog (Volumes main tab -> Bricks sub tab)"), //$NON-NLS-1$ //$NON-NLS-2$

    brick_details_not_supported("brick_details_not_supported", HelpTagType.WEBADMIN, "[gluster] also in 'Brick Details' context - shows the following message: 'Brick Details not supported for this Cluster's compatibility version ([non-supported-compatibility-version]).'"), //$NON-NLS-1$ //$NON-NLS-2$

    bricks("bricks", HelpTagType.UNKNOWN), //$NON-NLS-1$

    geo_replication("geo_replication", HelpTagType.UNKNOWN), //$NON-NLS-1$

    geo_replication_status_detail("geo_replication_status_detail", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Geo-Replication sub tab -> View Details"), //$NON-NLS-1$//$NON-NLS-2$

    cannot_add_bricks("cannot_add_bricks", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Bricks sub tab (Add Bricks context), dialog shows the following message: 'Could not find any host in Up status in the cluster. Please try again later.'"), //$NON-NLS-1$ //$NON-NLS-2$

    change_cd("change_cd", HelpTagType.WEBADMIN, "VMs Tab > Change CD"), //$NON-NLS-1$ //$NON-NLS-2$

    change_cluster_compatibility_version("change_cluster_compatibility_version", HelpTagType.WEBADMIN, "Cluster Tab > Edit > Confirm Cluster Compatibility Version Change"), //$NON-NLS-1$ //$NON-NLS-2$

    change_cpu_level("change_cpu_level", HelpTagType.WEBADMIN, "Cluster main tab -> Edit Cluster -> confirmation on lowering Cluster's CPU level"), //$NON-NLS-1$ //$NON-NLS-2$

    change_data_center_compatibility_version("change_data_center_compatibility_version", HelpTagType.WEBADMIN, "Data Center Tab > Edit > Confirm Data Center Compatibility Version Change"), //$NON-NLS-1$ //$NON-NLS-2$

    change_data_center_quota_enforcement_mode("change_data_center_quota_enforcement_mode", HelpTagType.WEBADMIN, "Edit Data-Center: confirmation dialog that appears when changing the Data-Center's quota mode to 'enforce'."), //$NON-NLS-1$ //$NON-NLS-2$

    change_quota_disks("change_quota_disks", HelpTagType.WEBADMIN, "'Assign Disk Quota' dialog (available from 'Disks' main tab and 'Disks' sub-tab in VMs/Templates main tabs)"), //$NON-NLS-1$ //$NON-NLS-2$

    clone_vm_from_snapshot("clone_vm_from_snapshot", HelpTagType.WEBADMIN, "VMs Tab > Snapshots Sub-Tab > Clone VM From Snapshot"), //$NON-NLS-1$ //$NON-NLS-2$

    clone_template_from_snapshot("clone_template_from_snapshot", HelpTagType.WEBADMIN, "VMs Tab > Snapshots Sub-Tab > Clone Template From Snapshot"), //$NON-NLS-1$ //$NON-NLS-2$

    clusters("clusters", HelpTagType.UNKNOWN), //$NON-NLS-1$

    configure("configure", HelpTagType.WEBADMIN, "Main > Configure"), //$NON-NLS-1$ //$NON-NLS-2$

    configure_local_storage("configure_local_storage", HelpTagType.WEBADMIN, "Host Tab > Configure Local Storage"), //$NON-NLS-1$ //$NON-NLS-2$

    confirm_console_connect("confirm_console_connect", HelpTagType.WEBADMIN, "'Console Connect' confirmation dialog with the following message: 'There may be users connected to the console who will not be able to reconnect. Do you want to proceed?'"), //$NON-NLS-1$ //$NON-NLS-2$

    console_disconnected("console_disconnected", HelpTagType.WEBADMIN, "VMs Tab > Console Disconnected"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_public_key("edit_public_key", HelpTagType.WEBADMIN, "VMs Tab > Set Serial Console Key"), //$NON-NLS-1$ //$NON-NLS-2$

    copy_disk("copy_disk", HelpTagType.WEBADMIN, "Templates Tab > Virtual Disks Sub-Tab > Copy Template"), //$NON-NLS-1$ //$NON-NLS-2$

    copy_disks("copy_disks", HelpTagType.WEBADMIN, "Disks Tab > Copy Disk(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    create_snapshot("create_snapshot", HelpTagType.WEBADMIN, "VMs Tab > Snapshots Tabs > Create Snapshot"), //$NON-NLS-1$ //$NON-NLS-2$

    custom_preview_snapshot("custom_preview_snapshot", HelpTagType.WEBADMIN, "VMs Tab > Snapshots Sub Tab > Custom Preview Snapshot"), //$NON-NLS-1$ //$NON-NLS-2$

    data_center("data_center", HelpTagType.UNKNOWN), //$NON-NLS-1$

    data_center_re_initialize("data_center_re_initialize", HelpTagType.WEBADMIN), //$NON-NLS-1$

    delete_snapshot("delete_snapshot", HelpTagType.WEBADMIN, "VMs Tab > Snapshots Tabs > Delete Snapshot"), //$NON-NLS-1$ //$NON-NLS-2$

    commit_snapshot("commit_snapshot", HelpTagType.WEBADMIN, "VMs Tab > Snapshots Tabs > Commit Snapshot"), //$NON-NLS-1$ //$NON-NLS-2$

    destroy_storage_domain("destroy_storage_domain", HelpTagType.WEBADMIN, "Storage Tab > Destroy Storage Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    detach_gluster_hosts("detach_gluster_hosts", HelpTagType.WEBADMIN, "[gluster] 'Detach Gluster Host' dialog (Clusters main tab -> General sub-tab)"), //$NON-NLS-1$ //$NON-NLS-2$

    detach_network_interfaces("detach_network_interfaces", HelpTagType.WEBADMIN, "Host Tab > Logical Network Sub-Tab > Detach Network Interfaces"), //$NON-NLS-1$ //$NON-NLS-2$

    detach_storage("detach_storage", HelpTagType.WEBADMIN, "Storage Tab > Data Center Sub-Tab > Detach Storage"), //$NON-NLS-1$ //$NON-NLS-2$

    detach_virtual_machine("detach_virtual_machine", HelpTagType.WEBADMIN, "Pools Tab > Virtual Machine Sub-Tab > Detach Virtual Machine(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    directory_groups("directory_groups", HelpTagType.UNKNOWN), //$NON-NLS-1$

    cluster_edit_warnings("cluster_edit_warnings", HelpTagType.WEBADMIN, "'Edit Cluster' dialog context: confirmation dialog for edits that may result in non-operational hosts."), //$NON-NLS-1$ //$NON-NLS-2$

    disable_hooks("disable_hooks", HelpTagType.WEBADMIN, "[gluster] Clusters main tab -> Gluster Hooks sub tab: confirmation dialog for disabling selected gluster hooks."), //$NON-NLS-1$ //$NON-NLS-2$

    discover_networks("discover_networks", HelpTagType.WEBADMIN, "[Neutron integration] 'Providers' main tab -> 'Networks' sub-tab -> 'Discover' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    disks("disks", HelpTagType.UNKNOWN), //$NON-NLS-1$

    leases("leases", HelpTagType.UNKNOWN), //$NON-NLS-1$

    editConsole("editConsole", HelpTagType.WEBADMIN), //$NON-NLS-1$

    edit_affinity_group("edit_affinity_group", HelpTagType.WEBADMIN, "Clusters main tab -> Affinity Groups sub-tab -> Edit"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_affinity_label("edit_affinity_label", HelpTagType.WEBADMIN, "Clusters main tab -> Affinity Labels sub-tab -> Edit"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_and_approve_host("edit_and_approve_host", HelpTagType.WEBADMIN, "Host Tab > Edit/Approve Host"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_bookmark("edit_bookmark", HelpTagType.WEBADMIN, "Main > Bookmark > Edit Bookmark"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_cluster("edit_cluster", HelpTagType.WEBADMIN, "Cluster Tab > Edit"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_data_center("edit_data_center", HelpTagType.WEBADMIN, "Data Center Tab > Edit Data Center"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_domain("edit_domain", HelpTagType.WEBADMIN, "Storage Tab > Manage Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_host("edit_host", HelpTagType.WEBADMIN, "Host Tab > Edit Host"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_iscsi_bundle("edit_iscsi_bundle", HelpTagType.WEBADMIN, "Data Center main tab -> iSCSI Bonds sub-tab -> Edit"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_logical_network("edit_logical_network", HelpTagType.WEBADMIN, "Data Center Tab > Logical Network Sub-Tab > Edit Logical Network"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_management_network("edit_management_network", HelpTagType.WEBADMIN, "Host Tab > Logical Network Sub-Tab > Edit Management Network"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_management_network_interface("edit_management_network_interface", HelpTagType.WEBADMIN, "Host Tab > Logical Network Sub-Tab > Edit Management Network Interface > Confirm Management Network Disconnect"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_network_interface_hosts("edit_network_interface_hosts", HelpTagType.WEBADMIN, "Host Tab > Logical Network Sub-Tab > Edit Network Interface"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_network_interface_tmps("edit_network_interface_tmps", HelpTagType.WEBADMIN, "Templates Tab > Logical Network Sub-Tab > Edit Network Interface"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_network_interface_vms("edit_network_interface_vms", HelpTagType.WEBADMIN, "VMs Tab > Logical Network Sub-Tab > Edit Network Interface"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_network_qos("edit_network_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Network QoS' sub-tab -> 'Edit Network QoS' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_storage_qos("edit_storage_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Storage QoS' sub-tab -> 'Edit Storage QoS' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_cpu_qos("edit_cpu_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Cpu QoS' sub-tab -> 'Edit Cpu QoS' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_host_network_qos("edit_host_network_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Host Network QoS' sub-tab -> 'Edit Host Network QoS' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_pool("edit_pool", HelpTagType.WEBADMIN, "Pools Tab > Edit Pool"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_provider("edit_provider", HelpTagType.WEBADMIN, "[Neutron integration] 'Providers' main tab -> 'Edit Provider' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_tag("edit_tag", HelpTagType.WEBADMIN, "Main > tags > Edit Tag"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_template("edit_template", HelpTagType.WEBADMIN, "Templates Tab > Edit Template"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_instance_type("edit_instance_type", HelpTagType.WEBADMIN, "Configure' dialog -> 'Instance Types' section -> 'Edit Instance Type' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_virtual_vm_disk("edit_virtual_vm_disk", HelpTagType.WEBADMIN, "VMs Tab > Virtual Disks Sub-Tab > Edit Virtual Disk"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_virtual_disk("edit_virtual_disk", HelpTagType.WEBADMIN, "Disks Tab > Edit Virtual Disk"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_vm("edit_vm", HelpTagType.WEBADMIN, "'VMs' main tab -> 'Edit VM' dialog [replaces the old 'Edit Desktop' (edit_desktop) and 'Edit Server' (edit_server)]"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_vnic_profile("edit_vnic_profile", HelpTagType.WEBADMIN, "'Networks' main tab -> 'Profiles' sub-tab -> '(Edit) VM (Network) Interface Profile' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    event_details("event_details", HelpTagType.WEBADMIN, "Events list -> Event Details dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    event_notifier("event_notifier", HelpTagType.UNKNOWN), //$NON-NLS-1$

    events("events", HelpTagType.UNKNOWN), //$NON-NLS-1$

    export_disks("export_disks", HelpTagType.WEBADMIN, "Disks main tab -> 'Export Image(s)' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    export_virtual_machine("export_virtual_machine", HelpTagType.WEBADMIN, "VMs Tab > Export Virtual Machine"), //$NON-NLS-1$ //$NON-NLS-2$

    export_template("export_template", HelpTagType.WEBADMIN, "Templates Tab > Export Template"), //$NON-NLS-1$ //$NON-NLS-2$

    external_subnets("external_subnets", HelpTagType.UNKNOWN), //$NON-NLS-1$

    force_lun_disk_creation("force_lun_disk_creation", HelpTagType.WEBADMIN, "'New Disk' context (probably of type 'Direct LUN') -> confirmation dialog regarding LUNs that are already in use in a storage domain."), //$NON-NLS-1$ //$NON-NLS-2$

    force_remove_data_center("force_remove_data_center", HelpTagType.WEBADMIN, "Data Center Tab > Force Remove Data Center"), //$NON-NLS-1$ //$NON-NLS-2$

    force_storage_domain_creation("force_storage_domain_creation", HelpTagType.WEBADMIN, "Storage Tab > New Domain > Confirm LUNs in Use"), //$NON-NLS-1$ //$NON-NLS-2$

    import_storage_domain_confirmation("import_storage_domain_confirmation", HelpTagType.WEBADMIN, "Storage Tab > Import Domain > Confirm import storage domain"), //$NON-NLS-1$ //$NON-NLS-2$

    attach_storage_domain_confirmation("attach_storage_domain_confirmation", HelpTagType.WEBADMIN, "Storage main-tab > Data Center sub-tab > Attach > Confirm attach storage domain"), //$NON-NLS-1$ //$NON-NLS-2$

    attach_storage_from_dc_confirmation("attach_storage_from_dc_confirmation", HelpTagType.WEBADMIN, "Data Center main-tab > Storage sub-tab > Attach > Confirm attach storage domain"), //$NON-NLS-1$ //$NON-NLS-2$

    general("general", HelpTagType.UNKNOWN), //$NON-NLS-1$

    new_storage_dr("new_storage_dr", HelpTagType.WEBADMIN, "[storage] Storage -> DR Setup -> New"), //$NON-NLS-1$ //$NON-NLS-2$

    gluster_bricks("gluster_bricks", HelpTagType.UNKNOWN), //$NON-NLS-1$

    gluster_hook_resolve_conflicts("gluster_hook_resolve_conflicts", HelpTagType.WEBADMIN, "[gluster] Clusters main tab -> Gluster Hooks sub-tab -> 'Resolve Conflicts' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    gluster_hooks("gluster_hooks", HelpTagType.UNKNOWN), //$NON-NLS-1$

    gluster_swift("gluster_swift", HelpTagType.UNKNOWN), //$NON-NLS-1$

    hardware("hardware", HelpTagType.UNKNOWN), //$NON-NLS-1$

    host_hooks("host_hooks", HelpTagType.UNKNOWN), //$NON-NLS-1$

    host_setup_networks("host_setup_networks", HelpTagType.WEBADMIN, "Host Tab > Logical Network Sub-Tab > Setup Host Networks"), //$NON-NLS-1$ //$NON-NLS-2$

    hosts("hosts", HelpTagType.UNKNOWN), //$NON-NLS-1$

    images("images", HelpTagType.UNKNOWN), //$NON-NLS-1$

    vm_register("register_vms", HelpTagType.WEBADMIN), //$NON-NLS-1$

    template_register("register_templates", HelpTagType.WEBADMIN), //$NON-NLS-1$

    import_conflict("import_conflict", HelpTagType.WEBADMIN, "during import of a VM/Template: A dialog that warns that there is a name conflict between the import candidate and an existing VM/Template in the system"), //$NON-NLS-1$ //$NON-NLS-2$

    import_images("import_images", HelpTagType.WEBADMIN, "[Glance integration] Storage main tab -> Images sub-tab (only for 'ISO' storage domain) -> 'Import Image(s)' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    import_disks("import_disks", HelpTagType.WEBADMIN, "Storage main tab -> Disk Import sub-tab -> 'Import Disk(s)' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    import_networks("import_networks", HelpTagType.WEBADMIN, "[Neutron integration?] 'Networks' main tab -> 'Import Networks' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    import_pre_configured_domain("import_pre_configured_domain", HelpTagType.WEBADMIN, "Storage Tab > Import Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    import_provider_certificate("import_provider_certificate", HelpTagType.WEBADMIN, "Provider main tab -> Add/Edit Provider dialog -> confirmation dialog on importing a provider certificate:"), //$NON-NLS-1$ //$NON-NLS-2$

    import_template("import_template", HelpTagType.WEBADMIN, "Storage Tab > Import Template > Import Template(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    import_virtual_machine("import_virtual_machine", HelpTagType.WEBADMIN, "Storage Tab > Import VM > Import Virtual Machine(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    register_template("register_template", HelpTagType.WEBADMIN, "Storage Tab > Import Template > Import Template(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    register_virtual_machine("register_virtual_machine", HelpTagType.WEBADMIN, "Storage Tab > Import VM > Import Virtual Machine(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    install_host("install_host", HelpTagType.WEBADMIN, "Host Tab > General Sub-Tab > Install Host"), //$NON-NLS-1$ //$NON-NLS-2$

    upgrade_host("upgrade_host", HelpTagType.WEBADMIN, "Host Tab > General Sub-Tab > Upgrade Host"), //$NON-NLS-1$ //$NON-NLS-2$

    iscsi_bundles("iscsi_bundles", HelpTagType.UNKNOWN), //$NON-NLS-1$

    logical_networks("logical_networks", HelpTagType.UNKNOWN), //$NON-NLS-1$

    maintenance_host("maintenance_host", HelpTagType.WEBADMIN, "Host Tab > Maintain Host(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    manage_gluster_swift("manage_gluster_swift", HelpTagType.WEBADMIN, "[gluster] 'Clusters' main tab -> 'General' sub tab -> 'Manage Gluster Swift' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    manage_policy_units("manage_policy_units", HelpTagType.WEBADMIN, "'Configure' dialog -> 'Scheduling Policy' section -> 'Manage Policy Units' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    manual_fence_are_you_sure("manual_fence_are_you_sure", HelpTagType.WEBADMIN, "Host Tab > Confirm Host has been Rebooted"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_next_run_configuration("edit_next_run_configuration", HelpTagType.WEBADMIN, "'VMs' main tab -> 'Edit VM' dialog [replaces the old 'Edit Desktop' (edit_desktop) and 'Edit Server' (edit_server)]"), //$NON-NLS-1$ //$NON-NLS-2$

    configuration_changes_for_high_performance_vm("configuration_changes_for_high_performance_vm", HelpTagType.WEBADMIN), //$NON-NLS-1$

    configuration_changes_for_high_performance_pool("configuration_changes_for_high_performance_pool", HelpTagType.WEBADMIN), //$NON-NLS-1$

    edit_unsupported_cpu("edit_unsupported_cpu", HelpTagType.WEBADMIN, "'VMs' main tab -> 'Edit VM' dialog -> 'System' sub tab -> 'Advanced Parameters' -> 'Custom CPU Type'"), //$NON-NLS-1$ //$NON-NLS-2$

    monitor("monitor", HelpTagType.UNKNOWN), //$NON-NLS-1$

    move_disk("move_disk", HelpTagType.WEBADMIN, "VMs Tab > Virtual Disks Sub-Tab > Move Virtual Disk"), //$NON-NLS-1$ //$NON-NLS-2$

    move_disks("move_disks", HelpTagType.WEBADMIN, "Disks Tab > Move Disk(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    create_secret("create_secret", HelpTagType.WEBADMIN, "Providers main-tab > Authentication Keys sub-tab > New"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_secret("create_secret", HelpTagType.WEBADMIN, "Providers main-tab > Authentication Keys sub-tab > Edit"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_secret("remove_secret", HelpTagType.WEBADMIN, "Providers main-tab > Authentication Keys sub-tab > Remove"), //$NON-NLS-1$ //$NON-NLS-2$

    multiple_archs_dialog("multiple_archs_dialog", HelpTagType.WEBADMIN, "Storage main tab -> VM Import sub-tab -> Import -> warning dialog for VMs with non-mathcing architectures"), //$NON-NLS-1$ //$NON-NLS-2$

    network_interfaces("network_interfaces", HelpTagType.UNKNOWN), //$NON-NLS-1$

    network_qos("network_qos", HelpTagType.UNKNOWN), //$NON-NLS-1$

    networks("networks", HelpTagType.UNKNOWN), //$NON-NLS-1$

    new_affinity_group("new_affinity_group", HelpTagType.WEBADMIN, "Clusters main tab -> Affinity Groups sub-tab -> New"), //$NON-NLS-1$ //$NON-NLS-2$

    new_affinity_label("new_affinity_label", HelpTagType.WEBADMIN, "Clusters main tab -> Affinity Labels sub-tab -> New"), //$NON-NLS-1$ //$NON-NLS-2$

    new_bookmark("new_bookmark", HelpTagType.WEBADMIN, "Main > Bookmark > New Bookmark"), //$NON-NLS-1$ //$NON-NLS-2$

    new_cluster("new_cluster", HelpTagType.WEBADMIN, "Cluster Tab > New"), //$NON-NLS-1$ //$NON-NLS-2$

    new_cluster___guide_me("new_cluster___guide_me", HelpTagType.WEBADMIN), //$NON-NLS-1$

    new_data_center("new_data_center", HelpTagType.WEBADMIN, "Data Center Tab > New Data Center"), //$NON-NLS-1$ //$NON-NLS-2$

    new_data_center___guide_me("new_data_center___guide_me", HelpTagType.WEBADMIN), //$NON-NLS-1$

    new_shared_mac_pool("new_shared_mac_pool", HelpTagType.WEBADMIN, "Configure > MAC Address Pools > New"), //$NON-NLS-1$ $NON-NLS-2$

    edit_shared_mac_pool("edit_shared_mac_pool", HelpTagType.WEBADMIN, "Configure > MAC Address Pools > Edit"), //$NON-NLS-1$ $NON-NLS-2$

    remove_shared_mac_pools("remove_shared_mac_pools", HelpTagType.WEBADMIN, "Configure > MAC Address Pools > Remove"), //$NON-NLS-1$ $NON-NLS-2$

    new_domain("new_domain", HelpTagType.WEBADMIN, "Storage Tab > New Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    new_external_subnet("new_external_subnet", HelpTagType.WEBADMIN, "Networks main tab -> External Subnet sub tab -> New"), //$NON-NLS-1$ //$NON-NLS-2$

    new_host("new_host", HelpTagType.WEBADMIN, "Host Tab > New Host"), //$NON-NLS-1$ //$NON-NLS-2$

    new_host_guide_me("new_host_guide_me", HelpTagType.WEBADMIN, "Data Center Tab > Data Center Guide Me > Add Host"), //$NON-NLS-1$ //$NON-NLS-2$

    new_iscsi_bundle("new_iscsi_bundle", HelpTagType.WEBADMIN, "Data Center main tab -> iSCSI Bonds sub-tab -> New"), //$NON-NLS-1$ //$NON-NLS-2$

    new_local_domain("new_local_domain", HelpTagType.WEBADMIN, "Storage Tab > New Local Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    new_logical_network("new_logical_network", HelpTagType.WEBADMIN, "Data Center Tab > Logical Network Sub-Tab > New Logical Network"), //$NON-NLS-1$ //$NON-NLS-2$

    new_network_interface_tmps("new_network_interface_tmps", HelpTagType.WEBADMIN, "Templates Tab > Logical Network Sub-Tab > New Network Interface"), //$NON-NLS-1$ //$NON-NLS-2$

    new_network_interface_vms("new_network_interface_vms", HelpTagType.WEBADMIN, "VMs Tab > Logical Network Sub-Tab > New Network Interface"), //$NON-NLS-1$ //$NON-NLS-2$

    new_network_qos("new_network_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Network QoS' sub-tab -> 'New Network QoS' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    new_host_network_qos("new_host_network_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Host Network QoS' sub-tab -> 'New Host Network QoS' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    new_storage_qos("new_storage_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Storage QoS' sub-tab -> 'New Storage QoS' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    new_cpu_qos("new_cpu_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Cpu QoS' sub-tab -> 'New Cpu QoS' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    new_pool("new_pool", HelpTagType.WEBADMIN, "Pools Tab > New Pool"), //$NON-NLS-1$ //$NON-NLS-2$

    new_quota("new_quota", HelpTagType.WEBADMIN, "Quota Tab > New Quota"), //$NON-NLS-1$ //$NON-NLS-2$

    new_tag("new_tag", HelpTagType.WEBADMIN, "Main > tags > New Tag"), //$NON-NLS-1$ //$NON-NLS-2$

    new_template("new_template", HelpTagType.WEBADMIN, "VMs Tab > Make Template"), //$NON-NLS-1$ //$NON-NLS-2$

    new_instance_type("new_instance_type", HelpTagType.WEBADMIN, "Configure' dialog -> 'Instance Types' section -> 'New Instance Type' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    new_virtual_disk("new_virtual_disk", HelpTagType.WEBADMIN, "VMs Tab > Virtual Disks Sub-Tab > New Virtual Disk"), //$NON-NLS-1$ //$NON-NLS-2$

    attach_virtual_disk("attach_virtual_disk", HelpTagType.WEBADMIN, "VMs Tab > Virtual Disks Sub-Tab > Attach Virtual Disk"), //$NON-NLS-1$ //$NON-NLS-2$

    new_virtual_machine___guide_me("new_virtual_machine___guide_me", HelpTagType.WEBADMIN), //$NON-NLS-1$

    new_vm("new_vm", HelpTagType.WEBADMIN, "'VMs' main tab -> 'New VM' dialog [replaces the old 'New Desktop' (new_desktop) and 'New Server' (new_server)]"), //$NON-NLS-1$ //$NON-NLS-2$

    clone_vm("clone_vm", HelpTagType.WEBADMIN, "'VMs tab > Clone VM"), //$NON-NLS-1$ //$NON-NLS-2$

    new_vnic_profile("new_vnic_profile", HelpTagType.WEBADMIN, "'Networks' main tab -> 'Profiles' sub-tab -> '(New) VM (Network) Interface Profile' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    new_volume("new_volume", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> New"), //$NON-NLS-1$ //$NON-NLS-2$

    configure_volume_snapshot("configure_volume_snapshot", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Snapshot -> Options"), //$NON-NLS-1$ //$NON-NLS-2$

    configure_volume_snapshot_confirmation("configure_volume_snapshot_confirmation", HelpTagType.WEBADMIN, "Configure volume snapshot: confirmation dialog that appears when changing the volume snapshot configuration parameters."), //$NON-NLS-1$ //$NON-NLS-2$

    remove_volume_snapshot_schedule_confirmation("remove_volume_snapshot_schedule_confirmation", HelpTagType.WEBADMIN, "Remove volume snapshot schedule: confirmation dialog that appears when None option selected while editing the gluster volume snapshot schedule."), //$NON-NLS-1$ //$NON-NLS-2$

    new_volume_snapshot("new_volume_snapshot", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Snapshot -> New"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_volume_snapshot_schedule("edit_volume_snapshot_schedule", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Snapshot -> Edit Schedule"), //$NON-NLS-1$ //$NON-NLS-2$

    parameters("parameters", HelpTagType.UNKNOWN), //$NON-NLS-1$

    permissions("permissions", HelpTagType.UNKNOWN), //$NON-NLS-1$

    power_management_configuration("power_management_configuration", HelpTagType.WEBADMIN, "Host Tab > Power Management Configuration"), //$NON-NLS-1$ //$NON-NLS-2$

    preview_snapshot("preview_snapshot", HelpTagType.WEBADMIN, "VMs main tab -> Snapshots sub-tab -> 'Preview Snapshot' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    preview_partial_snapshot("preview_partial_snapshot", HelpTagType.WEBADMIN, "VMs main tab -> Snapshots sub-tab -> 'Preview Partial Snapshot' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    profiles("profiles", HelpTagType.UNKNOWN), //$NON-NLS-1$

    providers("providers", HelpTagType.UNKNOWN), //$NON-NLS-1$

    quota("quota", HelpTagType.UNKNOWN), //$NON-NLS-1$

    remove_affinity_groups("remove_affinity_groups", HelpTagType.WEBADMIN, "Clusters main tab -> Affinity Groups sub-tab -> Remove"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_affinity_labels("remove_affinity_labels", HelpTagType.WEBADMIN, "Clusters main tab -> Affinity Labels sub-tab -> Remove"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_backed_up_template("remove_backed_up_template", HelpTagType.WEBADMIN, "Storage Tab > Import Template > Remove Backed up Template(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_backed_up_vm("remove_backed_up_vm", HelpTagType.WEBADMIN, "Storage Tab > Import VM > Remove Backed up VM(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_bookmark("remove_bookmark", HelpTagType.WEBADMIN, "Main > Bookmark > Remove Bookmark(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_cluster("remove_cluster", HelpTagType.WEBADMIN, "Cluster Tab > Remove"), //$NON-NLS-1$ //$NON-NLS-2$

    reset_emulated_machine_cluster("reset_emulated_machine_cluster", HelpTagType.WEBADMIN, "Cluster Tab > Reset Emulated Machine"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_cluster_policy("remove_cluster_policy", HelpTagType.WEBADMIN, "'Configure' dialog -> 'Scheduling Policy' section -> 'Remove Scheduling Policy' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_data_center("remove_data_center", HelpTagType.WEBADMIN, "Data Center Tab > Remove Data Center(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_disk("remove_disk", HelpTagType.WEBADMIN, "VMs Tab > Disks Sub-Tab > Remove Disk(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_unregistered_disk("remove_unregistered_disk", HelpTagType.WEBADMIN, "Storage Tab > Disk Import > Remove Disk(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    sparsify_disk("sparsify_disk", HelpTagType.WEBADMIN, "VMs Tab > Disks Sub-Tab > Sparsify Disk(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_external_subnet("remove_external_subnet", HelpTagType.WEBADMIN, "Networks main tab -> External Subnet sub tab -> Remove"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_host("remove_host", HelpTagType.WEBADMIN, "Host Tab > Remove Host(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_iscsi_bundle("remove_iscsi_bundle", HelpTagType.WEBADMIN, "Data Center main tab -> iSCSI Bonds sub-tab -> Remove"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_logical_network("remove_logical_network", HelpTagType.WEBADMIN, "Data Center Tab > Logical Network Sub-Tab > Remove Logical Network(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_network_interface_tmps("remove_network_interface_tmps", HelpTagType.WEBADMIN, "Templates Tab > Logical Network Sub-Tab > Remove Network Interface(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_network_interface_vms("remove_network_interface_vms", HelpTagType.WEBADMIN, "VMs Tab > Logical Network Sub-Tab > Remove Network Interface(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_network_qos("remove_network_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Network QoS' sub-tab -> 'Remove Network QoS' confirmation dialog."), //$NON-NLS-1$ //$NON-NLS-2$

    remove_storage_qos("remove_storage_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Storage QoS' sub-tab -> 'Remove Storage QoS' confirmation dialog."), //$NON-NLS-1$ //$NON-NLS-2$

    remove_cpu_qos("remove_cpu_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Cpu QoS' sub-tab -> 'Remove Cpu QoS' confirmation dialog."), //$NON-NLS-1$ //$NON-NLS-2$

    remove_host_network_qos("remove_host_network_qos", HelpTagType.WEBADMIN, "'Data Centers' main tab -> 'Host Network QoS' sub-tab -> 'Remove Host Network QoS' confirmation dialog."), //$NON-NLS-1$ //$NON-NLS-2$

    remove_permission("remove_permission", HelpTagType.WEBADMIN, "Each Main Tab > Permissions Sub-Tab > Remove Permission"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_pool("remove_pool", HelpTagType.WEBADMIN, "Pools Tab > Remove Pool(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_provider("remove_provider", HelpTagType.WEBADMIN, "[Neutron integration] 'Providers' main tab -> 'Remove Provider(s)' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    terminate_session("terminate_session", HelpTagType.WEBADMIN, "'Sessions' main tab -> 'Terminate Sessions(s)' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_quota("remove_quota", HelpTagType.WEBADMIN, "Quota Tab > Remove Quota"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_quota_assignment_from_user("remove_quota_assignment_from_user", HelpTagType.WEBADMIN, "Quota Tab > Users Sub-Tab > Detach Users/Groups from Quota"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_role("remove_role", HelpTagType.WEBADMIN, "Configure > Roles > Remove Role(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_storage("remove_storage", HelpTagType.WEBADMIN, "Storage Tab > Remove Storage(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_system_permission("remove_system_permission", HelpTagType.WEBADMIN, "Configure > Add System Permission to User > Remove System Permission"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_tag("remove_tag", HelpTagType.WEBADMIN, "Main > tags > Remove Tag(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_template("remove_template", HelpTagType.WEBADMIN, "Templates Tab > Remove Template(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_unregistered_template("remove_unregistered_template", HelpTagType.WEBADMIN, "Storage Tab > Import Template > Remove Template(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_instance_type("remove_instance_type", HelpTagType.WEBADMIN, "Configure' dialog -> 'Instance Types' section -> 'Remove Instance Type'"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_template_disks("remove_template_disks", HelpTagType.WEBADMIN, "Templates Tab > Storage Sub-Tab > Remove Disk Instance From Storage"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_user("remove_user", HelpTagType.WEBADMIN, "Users Tab > Remove User(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_virtual_machine("remove_virtual_machine", HelpTagType.WEBADMIN, "VMs Tab > Remove Desktop(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_unregistered_virtual_machine("remove_unregistered_virtual_machine", HelpTagType.WEBADMIN, "Storage Tab > Import VM > Remove VM(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_vnic_prfoile("remove_vnic_prfoile", HelpTagType.WEBADMIN, "'Networks' main tab -> 'Profiles' sub-tab -> 'Remove VM (Network) Interface Profile(s)' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_volume("remove_volume", HelpTagType.WEBADMIN, "Volumes Tab > Remove Volume"), //$NON-NLS-1$ //$NON-NLS-2$

    replace_brick("replace_brick", HelpTagType.WEBADMIN, "Volumes Tab > Bricks Sub-Tab > Replace Bricks"), //$NON-NLS-1$ //$NON-NLS-2$

    add_option("add_option", HelpTagType.WEBADMIN, "Volumes Tab > Parameters Sub-Tab > Add Option"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_option("edit_option", HelpTagType.WEBADMIN, "Volumes Tab > Parameters Sub-Tab > Edit Option"), //$NON-NLS-1$ //$NON-NLS-2$

    reset_all_options("reset_all_options", HelpTagType.WEBADMIN, "Volumes Tab > Parameters Sub-Tab > Reset All Options"), //$NON-NLS-1$ //$NON-NLS-2$

    reset_option("reset_option", HelpTagType.WEBADMIN, "Volumes Tab > Parameters Sub-Tab > Reset Option"), //$NON-NLS-1$ //$NON-NLS-2$

    restart_host("restart_host", HelpTagType.WEBADMIN, "Host Tab > Power Management Menu > Restart Host(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    run_once_virtual_machine("run_once_virtual_machine", HelpTagType.WEBADMIN, "VMs Tab > Run Once"), //$NON-NLS-1$ //$NON-NLS-2$

    save_network_configuration("save_network_configuration", HelpTagType.WEBADMIN, "'Hosts' main tab -> Network sub-tab -> 'Save Network Configuration' confirmation dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    select_host("select_host", HelpTagType.WEBADMIN, "Cluster Tab > Data Center Sub-Tab > Guide Me > Select Host"), //$NON-NLS-1$ //$NON-NLS-2$

    services("services", HelpTagType.UNKNOWN), //$NON-NLS-1$

    guest_info("guest_info", HelpTagType.UNKNOWN), //$NON-NLS-1$

    engine_sessions("engine_sessions", HelpTagType.UNKNOWN), //$NON-NLS-1$

    set_unlimited_specific_quota("set_unlimited_specific_quota", HelpTagType.WEBADMIN, "Quota main tab -> New Quota dialog -> confirmation dialog on setting an 'unlimited' quota on a certain resource."), //$NON-NLS-1$ //$NON-NLS-2$

    snapshots("snapshots", HelpTagType.UNKNOWN), //$NON-NLS-1$

    sso_did_not_succeeded("sso_did_not_succeeded", HelpTagType.WEBADMIN, "'Guest Agent is not responsive' dialog - appears when connecting to SPICE and guest agent is not responsive"), //$NON-NLS-1$ //$NON-NLS-2$

    stop_host("stop_host", HelpTagType.WEBADMIN, "Host Tab > Stop Host(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    storage("storage", HelpTagType.UNKNOWN), //$NON-NLS-1$

    template_import("template_import", HelpTagType.UNKNOWN), //$NON-NLS-1$

    template_not_found_on_export_domain("template_not_found_on_export_domain", HelpTagType.WEBADMIN, "VMs Tab > Export VM(s) > Confirm VM's Template not on Export Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    memory_hot_unplug("memory_hot_unplug", HelpTagType.UNKNOWN, "VMs Tab > VM Devices subtab > Confirm memory hot unplug"), //$NON-NLS-1$ //$NON-NLS-2$

    base_template_not_found_on_export_domain("base_template_not_found_on_export_domain", HelpTagType.WEBADMIN, "Templates Tab > Export Template Version(s) > Confirm Base Template not on Export Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    templates("templates", HelpTagType.UNKNOWN), //$NON-NLS-1$

    users("users", HelpTagType.UNKNOWN), //$NON-NLS-1$

    view_gluster_hook("view_gluster_hook", HelpTagType.WEBADMIN, "[gluster] Clusters main tab -> Gluster Hooks sub tab -> 'Hook Content' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    gluster_storage_devices("gluster_storage_devices", HelpTagType.WEBADMIN), //$NON-NLS-1$

    create_brick("create_brick", HelpTagType.WEBADMIN, "[gluster] Hosts main tab -> Storage Devices sub tab -> 'Create Brick' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    virtual_machines("virtual_machines", HelpTagType.UNKNOWN), //$NON-NLS-1$

    vm_import("vm_import", HelpTagType.UNKNOWN), //$NON-NLS-1$

    vms("vms", HelpTagType.UNKNOWN), //$NON-NLS-1$

    vnicProfiles("vnicProfiles", HelpTagType.UNKNOWN), //$NON-NLS-1$

    volume_rebalance_status("volume_rebalance_status", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Show Rebalance Status dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_profile_statistics("volume_profile_statistics", HelpTagType.WEBADMIN, "[gluster] Volumes Main tab -> Profiling -> Details"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_rebalance_stop("volume_rebalance_stop", HelpTagType.WEBADMIN, "Volumes main tab -> 'Stop Rebalance' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_remove_bricks("volume_remove_bricks", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Bricks sub-tab -> 'Remove Bricks' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_remove_bricks_commit("volume_remove_bricks_commit", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Bricks sub-tab -> 'Remove Bricks - Commit' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_remove_bricks_status("volume_remove_bricks_status", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Bricks sub-tab -> 'Remove Bricks - Status' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_remove_bricks_stop("volume_remove_bricks_stop", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Bricks sub-tab -> 'Remove Bricks - Stop' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_retain_brick("volume_retain_brick", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Bricks sub-tab -> 'Retain Bricks' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_stop("volume_stop", HelpTagType.WEBADMIN, "Volumes Tab > Stop Volume"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_start("volume_start", HelpTagType.WEBADMIN, "Volumes Tab > Start Volume"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_snapshots("volume_snapshots", HelpTagType.WEBADMIN), //$NON-NLS-1$

    volume_restore_snapshot_confirmation("volume_restore_snapshot_confirmation", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Snapshots sub-tab -> 'Restore Snapshot' dialog"), //$NON-NLS-1$//$NON-NLS-2$

    volume_delete_snapshot_confirmation("volume_delete_snapshot_confirmation", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Snapshots sub-tab -> 'Remove Snapshot' dialog"), //$NON-NLS-1$//$NON-NLS-2$

    volume_delete_all_snapshot_confirmation("volume_delete_all_snapshot_confirmation", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Snapshots sub-tab -> 'Remove All Snapshots' dialog"), //$NON-NLS-1$//$NON-NLS-2$

    volume_activate_snapshot_confirmation("volume_activate_snapshot_confirmation", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Snapshots sub-tab -> 'Activate Snapshot' dialog"), //$NON-NLS-1$//$NON-NLS-2$

    volume_deactivate_snapshot_confirmation("volume_deactivate_snapshot_confirmation", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Snapshots sub-tab -> 'Deactivate Snapshot' dialog"), //$NON-NLS-1$//$NON-NLS-2$

    volume_geo_rep_create("volume_geo_rep_create", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Geo-Replication sub-tab -> 'New' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_geo_rep_start_confirmation("volume_geo_rep_start_confirmation", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Geo-Replication sub-tab -> 'Start' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_geo_rep_stop_confirmation("volume_geo_rep_stop_confirmation", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Geo-Replication sub-tab -> 'Stop' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_geo_rep_pause_confirmation("volume_geo_rep_pause_confirmation", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Geo-Replication sub-tab -> 'Pause' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_geo_rep_resume_confirmation("volume_geo_rep_resume_confirmation", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Geo-Replication sub-tab -> 'Resume' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_geo_rep_remove_confirmation("volume_geo_rep_remove_confirmation", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Geo-Replication sub-tab -> 'Remove' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_geo_rep_configuration_display("volume_geo_rep_configuration_display", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Geo-Replication sub-tab -> 'Options' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_geo_rep_config_multiple_action_error_display("volume_geo_rep_config_multiple_action_error_display", HelpTagType.WEBADMIN, "[gluster] Volumes main tab -> Geo-Replication sub-tab -> Options -> 'Set/Reset' error dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    new_role("new_role", HelpTagType.WEBADMIN), //$NON-NLS-1$

    edit_role("edit_role", HelpTagType.WEBADMIN), //$NON-NLS-1$

    copy_role("copy_role", HelpTagType.WEBADMIN), //$NON-NLS-1$

    clone_quota("clone_quota", HelpTagType.WEBADMIN), //$NON-NLS-1$

    edit_quota("edit_quota", HelpTagType.WEBADMIN), //$NON-NLS-1$

    attach_export_domain("attach_export_domain", HelpTagType.WEBADMIN), //$NON-NLS-1$

    attach_iso_library("attach_iso_library", HelpTagType.WEBADMIN), //$NON-NLS-1$

    attach_storage("attach_storage", HelpTagType.WEBADMIN), //$NON-NLS-1$

    new_cluster_policy("new_cluster_policy", HelpTagType.WEBADMIN, "'Configure' dialog -> 'Scheduling Policy' section -> 'New Scheduling Policy' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_cluster_policy("edit_cluster_policy", HelpTagType.WEBADMIN, "'Configure' dialog -> 'Scheduling Policy' section -> 'Edit Scheduling Policy' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    copy_cluster_policy("copy_cluster_policy", HelpTagType.WEBADMIN, "'Configure' dialog -> 'Scheduling Policy' section -> 'Copy Scheduling Policy' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    shutdown_virtual_machine("shutdown_virtual_machine", HelpTagType.WEBADMIN, "VMs main tab -> Shutdown confirmation dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    stop_virtual_machine("stop_virtual_machine", HelpTagType.WEBADMIN, "VMs main tab -> Power Off confirmation dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    reboot_virtual_machine("reboot_virtual_machine", HelpTagType.WEBADMIN, "VMs main tab -> Reboot confirmation dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    reset_virtual_machine("reset_virtual_machine", HelpTagType.WEBADMIN, "VMs main tab -> Reset confirmation dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    suspend_virtual_machine("suspend_virtual_machine", HelpTagType.WEBADMIN, "VMs main tab -> Suspend confirmation dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_disk_snapshot("remove_disk_snapshot", HelpTagType.WEBADMIN, "Storage Tab > Snapshots Sub-Tab > Remove Disk Snapshot(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    storage_qos("storage_qos", HelpTagType.UNKNOWN), //$NON-NLS-1$

    cpu_qos("storage_qos", HelpTagType.UNKNOWN), //$NON-NLS-1$

    host_network_qos("host_network_qos", HelpTagType.UNKNOWN), //$NON-NLS-1$

    disk_profiles("disk profiles", HelpTagType.UNKNOWN), //$NON-NLS-1$

    new_disk_profile("new_disk_profile", HelpTagType.WEBADMIN, "Storage Tab > Disk Profiles sub-tab -> New Disk Profile dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_disk_profile("edit_disk_profile", HelpTagType.WEBADMIN, "Storage Tab > Disk Profiles sub-tab -> Edit Disk Profile dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_disk_profile("remove_disk_profile", HelpTagType.WEBADMIN, "Storage Tab > Disk Profiles sub-tab -> Remove Disk Profile dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    cpu_profiles("cpu profiles", HelpTagType.UNKNOWN), //$NON-NLS-1$

    new_cpu_profile("new_cpu_profile", HelpTagType.WEBADMIN, "Clusters Tab > Cpu Profiles sub-tab -> New Cpu Profile dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_cpu_profile("edit_cpu_profile", HelpTagType.WEBADMIN, "Clusters Tab > Cpu Profiles sub-tab -> Edit Cpu Profile dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_cpu_profile("remove_cpu_profile", HelpTagType.WEBADMIN, "Clusters Tab > Cpu Profiles sub-tab -> Remove Cpu Profile dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    numa_support("numa_support", HelpTagType.WEBADMIN, "Host Tab > NUMA Support"), //$NON-NLS-1$ //$NON-NLS-2$

    upload_disk_image("upload_disk_image", HelpTagType.WEBADMIN, "Disks Tab -> Upload Image dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    resume_upload_image("resume_upload_image", HelpTagType.WEBADMIN, "Disks Tab -> Resume Uploading Image dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    cancel_upload_image("cancel_upload_image", HelpTagType.WEBADMIN, "Disks Tab -> Cancel Upload Image dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    upload_disk_image_to_domain("upload_disk_image_to_domain", HelpTagType.WEBADMIN, "Storage Tab > Disks sub-tab -> Upload Image dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    resume_upload_image_to_domain("resume_upload_image_to_domain", HelpTagType.WEBADMIN, "Storage Tab > Disks sub-tab -> Resume Uploading Image dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    cancel_upload_image_to_domain("cancel_upload_image_to_domain", HelpTagType.WEBADMIN, "Storage Tab > Disks sub-tab -> Cancel Upload Image dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    host_devices("host_devices", HelpTagType.UNKNOWN), //$NON-NLS-1$

    vm_host_devices("vm_host_devices", HelpTagType.UNKNOWN), //$NON-NLS-1$

    add_host_device("add_host_device", HelpTagType.WEBADMIN, "VMs Tab > Host Devices Sub-Tab > Add Host Device(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_host_device("remove_host_device", HelpTagType.WEBADMIN, "VMs Tab > Host Devices Sub-Tab > Remove Host Device(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    repin_host("repin_host", HelpTagType.WEBADMIN, "VMs Tab > Host Devices Sub-Tab > Pin to another Host"), //$NON-NLS-1$ //$NON-NLS-2$

    errata("errata", HelpTagType.WEBADMIN, "System tree > Errata"), //$NON-NLS-1$ //$NON-NLS-2$

    reset_brick("reset_brick", HelpTagType.WEBADMIN, "Volumes Tab > Bricks Sub-Tab > Reset Bricks"), //$NON-NLS-1$ //$NON-NLS-2$

    create_iso_domain("create_iso_domain", HelpTagType.WEBADMIN, "Storage Tab > New Domain > Confirm ISO domain type"); //$NON-NLS-1$ //$NON-NLS-2$

    public final String name;

    HelpTag(String name, HelpTagType type) {
        this.name = name;
    }

    HelpTag(String name, HelpTagType type, String comment) {
        this.name = name;
    }


}

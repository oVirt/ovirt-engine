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

    SanStorageModelBase("SanStorageModelBase"), //$NON-NLS-1$

    add_bricks("add_bricks", "Volumes Tab > Bricks Sub-Tab > Add Brick"), //$NON-NLS-1$ //$NON-NLS-2$

    add_bricks_confirmation("add_bricks_confirmation", "[gluster] In 'Add Bricks' context: this is a confirmation dialog with the following message: 'Multiple bricks of a Replicate volume are present on the same server. This setup is not optimal. Do you still want to continue?'"), //$NON-NLS-1$ //$NON-NLS-2$

    add_event_notification("add_event_notification", "Users Tab > Event Notifier > Add Events Notification"), //$NON-NLS-1$ //$NON-NLS-2$

    add_hosts("add_hosts", "[gluster] In 'New Cluster' context: Allows to add/import Hosts to a newly-created gluster-cluster"), //$NON-NLS-1$ //$NON-NLS-2$

    add_permission_to_user("add_permission_to_user", "Each Main Tab > Permissions Sub-Tab > Add Permission"), //$NON-NLS-1$ //$NON-NLS-2$

    add_provider("add_provider", "[Neutron integration] 'Providers' main tab -> 'Add Provider' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    add_system_permission_to_user("add_system_permission_to_user", "Configure > Add System Permission to User > Add System Permission"), //$NON-NLS-1$ //$NON-NLS-2$

    add_users_and_groups("add_users_and_groups", "Users Tab > Add Users & Groups"), //$NON-NLS-1$ //$NON-NLS-2$

    affinity_groups("affinity_groups"), //$NON-NLS-1$

    applications("applications"), //$NON-NLS-1$

    assign_network("assign_network"), //$NON-NLS-1$

    assign_networks("assign_networks", "Cluster Tab > Logical Network Sub-Tab > Assign Networks"), //$NON-NLS-1$ //$NON-NLS-2$

    assign_tags_hosts("assign_tags_hosts", "Host Tab > Assign Tags"), //$NON-NLS-1$ //$NON-NLS-2$

    assign_tags_users("assign_tags_users", "Users Tab > Assign Tags"), //$NON-NLS-1$ //$NON-NLS-2$

    assign_tags_vms("assign_tags_vms", "VMs Tab > Assign Tags"), //$NON-NLS-1$ //$NON-NLS-2$

    assign_users_and_groups_to_quota("assign_users_and_groups_to_quota", "Quota Tab > Users Sub-Tab > Assign Users/Groups to Quota"), //$NON-NLS-1$ //$NON-NLS-2$

    bond_network_interfaces("bond_network_interfaces", "Host Tab > Logical Network Sub-Tab > Bond Network Interfaces"), //$NON-NLS-1$ //$NON-NLS-2$

    brick_advanced("brick_advanced", "[gluster] 'Brick Details' dialog (Volumes main tab -> Bricks sub tab)"), //$NON-NLS-1$ //$NON-NLS-2$

    brick_details_not_supported("brick_details_not_supported", "[gluster] also in 'Brick Details' context - shows the following message: 'Brick Details not supported for this Cluster's compatibility version ([non-supported-compatibility-version]).'"), //$NON-NLS-1$ //$NON-NLS-2$

    bricks("bricks"), //$NON-NLS-1$

    cannot_add_bricks("cannot_add_bricks", "[gluster] Volumes main tab -> Bricks sub tab (Add Bricks context), dialog shows the following message: 'Could not find any host in Up status in the cluster. Please try again later.'"), //$NON-NLS-1$ //$NON-NLS-2$

    change_cd("change_cd", "VMs Tab > Change CD"), //$NON-NLS-1$ //$NON-NLS-2$

    change_cluster_compatibility_version("change_cluster_compatibility_version", "Cluster Tab > Edit > Confirm Cluster Compatibility Version Change"), //$NON-NLS-1$ //$NON-NLS-2$

    change_cpu_level("change_cpu_level"), //$NON-NLS-1$

    change_data_center_compatibility_version("change_data_center_compatibility_version", "Data Center Tab > Edit > Confirm Data Center Compatibility Version Change"), //$NON-NLS-1$ //$NON-NLS-2$

    change_data_center_quota_enforcement_mode("change_data_center_quota_enforcement_mode", "Edit Data-Center: confirmation dialog that appears when changing the Data-Center's quota mode to 'enforce'."), //$NON-NLS-1$ //$NON-NLS-2$

    change_quota_disks("change_quota_disks", "'Assign Disk Quota' dialog (available from 'Disks' main tab and 'Disks' sub-tab in VMs/Templates main tabs)"), //$NON-NLS-1$ //$NON-NLS-2$

    clone_vm_from_snapshot("clone_vm_from_snapshot", "VMs Tab > Snapshots Sub-Tab > Clone VM From Snapshot"), //$NON-NLS-1$ //$NON-NLS-2$

    clusters("clusters"), //$NON-NLS-1$

    configure("configure", "Main > Configure"), //$NON-NLS-1$ //$NON-NLS-2$

    configure_local_storage("configure_local_storage", "Host Tab > Configure Local Storage"), //$NON-NLS-1$ //$NON-NLS-2$

    confirm_console_connect("confirm_console_connect", "'Console Connect' confirmation dialog with the following message: 'There may be users connected to the console who will not be able to reconnect. Do you want to proceed?'"), //$NON-NLS-1$ //$NON-NLS-2$

    console_disconnected("console_disconnected", "VMs Tab > Console Disconnected"), //$NON-NLS-1$ //$NON-NLS-2$

    copy_disk("copy_disk", "Templates Tab > Virtual Disks Sub-Tab > Copy Template"), //$NON-NLS-1$ //$NON-NLS-2$

    copy_disks("copy_disks", "Disks Tab > Copy Disk(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    create_snapshot("create_snapshot", "VMs Tab > Snapshots Tabs > Create Snapshot"), //$NON-NLS-1$ //$NON-NLS-2$

    custom_preview_snapshot("custom_preview_snapshot"), //$NON-NLS-1$

    data_center("data_center"), //$NON-NLS-1$

    data_center_re_initialize("data_center_re_initialize"), //$NON-NLS-1$

    delete_snapshot("delete_snapshot", "VMs Tab > Snapshots Tabs > Delete Snapshot"), //$NON-NLS-1$ //$NON-NLS-2$

    destroy_storage_domain("destroy_storage_domain", "Storage Tab > Destroy Storage Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    detach_gluster_hosts("detach_gluster_hosts", "[gluster] 'Detach Gluster Host' dialog (Clusters main tab -> General sub-tab)"), //$NON-NLS-1$ //$NON-NLS-2$

    detach_network_interfaces("detach_network_interfaces", "Host Tab > Logical Network Sub-Tab > Detach Network Interfaces"), //$NON-NLS-1$ //$NON-NLS-2$

    detach_storage("detach_storage", "Storage Tab > Data Center Sub-Tab > Detach Storage"), //$NON-NLS-1$ //$NON-NLS-2$

    detach_virtual_machine("detach_virtual_machine", "Pools Tab > Virtual Machine Sub-Tab > Detach Virtual Machine(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    directory_groups("directory_groups"), //$NON-NLS-1$

    disable_cpu_thread_support("disable_cpu_thread_support", "'Edit Cluster' dialog context: confirmation dialog for disabling the CPU Thread feature (related to CPU pinning):"), //$NON-NLS-1$ //$NON-NLS-2$

    disable_hooks("disable_hooks", "[gluster] Clusters main tab -> Gluster Hooks sub tab: confirmation dialog for disabling selected gluster hooks."), //$NON-NLS-1$ //$NON-NLS-2$

    discover_networks("discover_networks", "[Neutron integration] 'Providers' main tab -> 'Networks' sub-tab -> 'Discover' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    disks("disks"), //$NON-NLS-1$

    editConsole("editConsole"), //$NON-NLS-1$

    edit_affinity_group("edit_affinity_group"), //$NON-NLS-1$

    edit_and_approve_host("edit_and_approve_host", "Host Tab > Edit/Approve Host"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_bookmark("edit_bookmark", "Main > Bookmark > Edit Bookmark"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_cluster("edit_cluster", "Cluster Tab > Edit"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_data_center("edit_data_center", "Data Center Tab > Edit Data Center"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_domain("edit_domain", "Storage Tab > Edit Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_host("edit_host", "Host Tab > Edit Host"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_iscsi_bundle("edit_iscsi_bundle"), //$NON-NLS-1$

    edit_logical_network("edit_logical_network", "Data Center Tab > Logical Network Sub-Tab > Edit Logical Network"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_management_network("edit_management_network", "Host Tab > Logical Network Sub-Tab > Edit Management Network"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_management_network_interface("edit_management_network_interface", "Host Tab > Logical Network Sub-Tab > Edit Management Network Interface > Confirm Management Network Disconnect"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_network_interface_hosts("edit_network_interface_hosts", "Host Tab > Logical Network Sub-Tab > Edit Network Interface"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_network_interface_tmps("edit_network_interface_tmps", "Templates Tab > Logical Network Sub-Tab > Edit Network Interface"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_network_interface_vms("edit_network_interface_vms", "VMs Tab > Logical Network Sub-Tab > Edit Network Interface"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_network_qos("edit_network_qos", "'Data Centers' main tab -> 'Network QoS' sub-tab -> 'Edit Network QoS' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_pool("edit_pool", "Pools Tab > Edit Pool"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_provider("edit_provider", "[Neutron integration] 'Providers' main tab -> 'Edit Provider' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_tag("edit_tag", "Main > tags > Edit Tag"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_template("edit_template", "Templates Tab > Edit Template"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_virtual_disk("edit_virtual_disk", "VMs Tab > Virtual Disks Sub-Tab > Edit Virtual Disk"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_vm("edit_vm", "'VMs' main tab -> 'Edit VM' dialog [replaces the old 'Edit Desktop' (edit_desktop) and 'Edit Server' (edit_server)]"), //$NON-NLS-1$ //$NON-NLS-2$

    edit_vnic_profile("edit_vnic_profile", "'Networks' main tab -> 'Profiles' sub-tab -> '(Edit) VM (Network) Interface Profile' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    event_details("event_details", "Events list -> Event Details dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    event_notifier("event_notifier"), //$NON-NLS-1$

    events("events"), //$NON-NLS-1$

    export_disks("export_disks", "Disks main tab -> 'Export Image(s)' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    export_virtual_machine("export_virtual_machine", "VMs Tab > Export Virtual Machine"), //$NON-NLS-1$ //$NON-NLS-2$

    external_subnets("external_subnets"), //$NON-NLS-1$

    force_lun_disk_creation("force_lun_disk_creation", "'New Disk' context (probably of type 'Direct LUN') -> confirmation dialog regarding LUNs that are already in use in a storage domain."), //$NON-NLS-1$ //$NON-NLS-2$

    force_remove_data_center("force_remove_data_center", "Data Center Tab > Force Remove Data Center"), //$NON-NLS-1$ //$NON-NLS-2$

    force_storage_domain_creation("force_storage_domain_creation", "Storage Tab > New Domain > Confirm LUNs in Use"), //$NON-NLS-1$ //$NON-NLS-2$

    general("general"), //$NON-NLS-1$

    gluster_bricks("gluster_bricks"), //$NON-NLS-1$

    gluster_hook_resolve_conflicts("gluster_hook_resolve_conflicts", "[gluster] Clusters main tab -> Gluster Hooks sub-tab -> 'Resolve Conflicts' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    gluster_hooks("gluster_hooks"), //$NON-NLS-1$

    gluster_swift("gluster_swift"), //$NON-NLS-1$

    hardware("hardware"), //$NON-NLS-1$

    host_hooks("host_hooks"), //$NON-NLS-1$

    host_setup_networks("host_setup_networks", "Host Tab > Logical Network Sub-Tab > Setup Host Networks"), //$NON-NLS-1$ //$NON-NLS-2$

    hosts("hosts"), //$NON-NLS-1$

    images("images"), //$NON-NLS-1$

    import_conflict("import_conflict", "during import of a VM/Template: A dialog that warns that there is a name conflict between the import candidate and an existing VM/Template in the system"), //$NON-NLS-1$ //$NON-NLS-2$

    import_images("import_images", "[Glance integration] Storage main tab -> Images sub-tab (only for 'ISO' storage domain) -> 'Import Image(s)' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    import_networks("import_networks", "[Neutron integration?] 'Networks' main tab -> 'Import Networks' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    import_pre_configured_domain("import_pre_configured_domain"), //$NON-NLS-1$

    import_provider_certificates("import_provider_certificates", "Provider main tab -> Add/Edit Provider dialog -> confirmation dialog on importing a provider certificate chain:"), //$NON-NLS-1$ //$NON-NLS-2$

    import_template("import_template", "Storage Tab > Import Template > Import Template(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    import_virtual_machine("import_virtual_machine", "Storage Tab > Import VM > Import Virtual Machine(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    install_host("install_host", "Host Tab > General Sub-Tab > Install Host"), //$NON-NLS-1$ //$NON-NLS-2$

    iscsi_bundles("iscsi_bundles"), //$NON-NLS-1$

    logical_networks("logical_networks"), //$NON-NLS-1$

    maintenance_host("maintenance_host", "Host Tab > Maintain Host(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    manage_gluster_swift("manage_gluster_swift", "[gluster] 'Clusters' main tab -> 'General' sub tab -> 'Manage Gluster Swift' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    manage_policy_units("manage_policy_units", "'Configure' dialog -> 'Cluster Policy' section -> 'Manage Policy Units' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    manual_fence_are_you_sure("manual_fence_are_you_sure", "Host Tab > Confirm Host has been Rebooted"), //$NON-NLS-1$ //$NON-NLS-2$

    migrate_virtual_machine("migrate_virtual_machine", "VMs Tab > Migrate Virtual Machine(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    monitor("monitor"), //$NON-NLS-1$

    move_disk("move_disk", "VMs Tab > Virtual Disks Sub-Tab > Move Virtual Disk"), //$NON-NLS-1$ //$NON-NLS-2$

    move_disks("move_disks", "Disks Tab > Move Disk(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    multiple_archs_dialog("multiple_archs_dialog"), //$NON-NLS-1$

    network_interfaces("network_interfaces"), //$NON-NLS-1$

    network_qos("network_qos"), //$NON-NLS-1$

    networks("networks"), //$NON-NLS-1$

    new_affinity_group("new_affinity_group"), //$NON-NLS-1$

    new_bookmark("new_bookmark", "Main > Bookmark > New Bookmark"), //$NON-NLS-1$ //$NON-NLS-2$

    new_cluster("new_cluster", "Cluster Tab > New"), //$NON-NLS-1$ //$NON-NLS-2$

    new_cluster___guide_me("new_cluster___guide_me"), //$NON-NLS-1$

    new_data_center("new_data_center", "Data Center Tab > New Data Center"), //$NON-NLS-1$ //$NON-NLS-2$

    new_data_center___guide_me("new_data_center___guide_me"), //$NON-NLS-1$

    new_domain("new_domain", "Storage Tab > New Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    new_external_subnet("new_external_subnet"), //$NON-NLS-1$

    new_host("new_host", "Host Tab > New Host"), //$NON-NLS-1$ //$NON-NLS-2$

    new_host_guide_me("new_host_guide_me", "Data Center Tab > Data Center Guide Me > Add Host"), //$NON-NLS-1$ //$NON-NLS-2$

    new_iscsi_bundle("new_iscsi_bundle"), //$NON-NLS-1$

    new_local_domain("new_local_domain", "Storage Tab > New Local Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    new_logical_network("new_logical_network", "Data Center Tab > Logical Network Sub-Tab > New Logical Network"), //$NON-NLS-1$ //$NON-NLS-2$

    new_network_interface_tmps("new_network_interface_tmps", "Templates Tab > Logical Network Sub-Tab > New Network Interface"), //$NON-NLS-1$ //$NON-NLS-2$

    new_network_interface_vms("new_network_interface_vms", "VMs Tab > Logical Network Sub-Tab > New Network Interface"), //$NON-NLS-1$ //$NON-NLS-2$

    new_network_qos("new_network_qos", "'Data Centers' main tab -> 'Network QoS' sub-tab -> 'New Network QoS' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    new_pool("new_pool", "Pools Tab > New Pool"), //$NON-NLS-1$ //$NON-NLS-2$

    new_quota("new_quota", "Quota Tab > New Quota"), //$NON-NLS-1$ //$NON-NLS-2$

    new_tag("new_tag", "Main > tags > New Tag"), //$NON-NLS-1$ //$NON-NLS-2$

    new_template("new_template", "VMs Tab > Make Template"), //$NON-NLS-1$ //$NON-NLS-2$

    new_virtual_disk("new_virtual_disk", "VMs Tab > Virtual Disks Sub-Tab > New Virtual Disk"), //$NON-NLS-1$ //$NON-NLS-2$

    new_virtual_machine___guide_me("new_virtual_machine___guide_me"), //$NON-NLS-1$

    new_vm("new_vm", "'VMs' main tab -> 'New VM' dialog [replaces the old 'New Desktop' (new_desktop) and 'New Server' (new_server)]"), //$NON-NLS-1$ //$NON-NLS-2$

    new_vnic_profile("new_vnic_profile", "'Networks' main tab -> 'Profiles' sub-tab -> '(New) VM (Network) Interface Profile' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    new_volume("new_volume"), //$NON-NLS-1$

    parameters("parameters"), //$NON-NLS-1$

    permissions("permissions"), //$NON-NLS-1$

    power_management_configuration("power_management_configuration", "Host Tab > Power Management Configuration"), //$NON-NLS-1$ //$NON-NLS-2$

    preview_snapshot("preview_snapshot", "VMs main tab -> Snapshots sub-tab -> 'Preview Snapshot' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    profiles("profiles"), //$NON-NLS-1$

    providers("providers"), //$NON-NLS-1$

    quota("quota"), //$NON-NLS-1$

    remove_affinity_groups("remove_affinity_groups"), //$NON-NLS-1$

    remove_backed_up_template("remove_backed_up_template", "Storage Tab > Import Template > Remove Backed up Template(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_backed_up_vm("remove_backed_up_vm", "Storage Tab > Import VM > Remove Backed up VM(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_bookmark("remove_bookmark", "Main > Bookmark > Remove Bookmark(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_cluster("remove_cluster", "Cluster Tab > Remove"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_cluster_policy("remove_cluster_policy", "'Configure' dialog -> 'Cluster Policy' section -> 'Remove Cluster Policy' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_data_center("remove_data_center", "Data Center Tab > Remove Data Center(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_disk("remove_disk", "VMs Tab > Disks Sub-Tab > Remove Disk(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_external_subnet("remove_external_subnet"), //$NON-NLS-1$

    remove_host("remove_host", "Host Tab > Remove Host(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_iscsi_bundle("remove_iscsi_bundle"), //$NON-NLS-1$

    remove_logical_network("remove_logical_network", "Data Center Tab > Logical Network Sub-Tab > Remove Logical Network(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_network_interface_tmps("remove_network_interface_tmps", "Templates Tab > Logical Network Sub-Tab > Remove Network Interface(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_network_interface_vms("remove_network_interface_vms", "VMs Tab > Logical Network Sub-Tab > Remove Network Interface(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_network_qos("remove_network_qos", "'Data Centers' main tab -> 'Network QoS' sub-tab -> 'Remove Network QoS' confirmation dialog."), //$NON-NLS-1$ //$NON-NLS-2$

    remove_permission("remove_permission", "Each Main Tab > Permissions Sub-Tab > Remove Permission"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_pool("remove_pool", "Pools Tab > Remove Pool(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_provider("remove_provider", "[Neutron integration] 'Providers' main tab -> 'Remove Provider(s)' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_quota("remove_quota", "Quota Tab > Remove Quota"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_quota_assignment_from_user("remove_quota_assignment_from_user", "Quota Tab > Users Sub-Tab > Detach Users/Groups from Quota"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_role("remove_role", "Configure > Roles > Remove Role(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_storage("remove_storage", "Storage Tab > Remove Storage(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_system_permission("remove_system_permission", "Configure > Add System Permission to User > Remove System Permission"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_tag("remove_tag", "Main > tags > Remove Tag(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_template("remove_template", "Templates Tab > Remove Template(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_template_disks("remove_template_disks", "Templates Tab > Storage Sub-Tab > Remove Disk Instance From Storage"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_user("remove_user", "Users Tab > Remove User(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_virtual_machine("remove_virtual_machine", "VMs Tab > Remove Desktop(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_vnic_prfoile("remove_vnic_prfoile", "'Networks' main tab -> 'Profiles' sub-tab -> 'Remove VM (Network) Interface Profile(s)' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    remove_volume("remove_volume", "Volumes Tab > Remove Volume"), //$NON-NLS-1$ //$NON-NLS-2$

    replace_brick("replace_brick", "Volumes Tab > Bricks Sub-Tab > Replace Bricks"), //$NON-NLS-1$ //$NON-NLS-2$

    reset_all_options("reset_all_options", "Volumes Tab > Parameters Sub-Tab > Reset All Options"), //$NON-NLS-1$ //$NON-NLS-2$

    reset_option("reset_option", "Volumes Tab > Parameters Sub-Tab > Reset Option"), //$NON-NLS-1$ //$NON-NLS-2$

    restart_host("restart_host", "Host Tab > Power Management Menu > Restart Host(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    run_virtual_machine("run_virtual_machine", "VMs Tab > Run Virtual Machine(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    save_network_configuration("save_network_configuration", "'Hosts' main tab -> Network sub-tab -> 'Save Network Configuration' confirmation dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    select_host("select_host", "Cluster Tab > Data Center Sub-Tab > Guide Me > Select Host"), //$NON-NLS-1$ //$NON-NLS-2$

    services("services"), //$NON-NLS-1$

    sessions("sessions"), //$NON-NLS-1$

    set_unlimited_specific_quota("set_unlimited_specific_quota", "Quota main tab -> New Quota dialog -> confirmation dialog on setting an 'unlimited' quota on a certain resource."), //$NON-NLS-1$ //$NON-NLS-2$

    snapshots("snapshots"), //$NON-NLS-1$

    sso_did_not_succeeded("sso_did_not_succeeded", "'Guest Agent is not responsive' dialog - appears when connecting to SPICE and guest agent is not responsive"), //$NON-NLS-1$ //$NON-NLS-2$

    stop_host("stop_host", "Host Tab > Stop Host(s)"), //$NON-NLS-1$ //$NON-NLS-2$

    storage("storage"), //$NON-NLS-1$

    template_import("template_import"), //$NON-NLS-1$

    template_not_found_on_export_domain("template_not_found_on_export_domain", "VMs Tab > Export VM(s) > Confirm VM's Template not on Export Domain"), //$NON-NLS-1$ //$NON-NLS-2$

    templates("templates"), //$NON-NLS-1$

    users("users"), //$NON-NLS-1$

    view_gluster_hook("view_gluster_hook", "[gluster] Clusters main tab -> Gluster Hooks sub tab -> 'Hook Content' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    virtual_machines("virtual_machines"), //$NON-NLS-1$

    vm_import("vm_import"), //$NON-NLS-1$

    vms("vms"), //$NON-NLS-1$

    vnicProfiles("vnicProfiles"), //$NON-NLS-1$

    volume_rebalance_status("volume_rebalance_status"), //$NON-NLS-1$

    volume_rebalance_stop("volume_rebalance_stop", "Volumes main tab -> 'Stop Rebalance' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_remove_bricks("volume_remove_bricks", "[gluster] Volumes main tab -> Bricks sub-tab -> 'Remove Bricks' dialog"), //$NON-NLS-1$ //$NON-NLS-2$

    volume_remove_bricks_commit("volume_remove_bricks_commit"), //$NON-NLS-1$

    volume_remove_bricks_status("volume_remove_bricks_status"), //$NON-NLS-1$

    volume_remove_bricks_stop("volume_remove_bricks_stop"), //$NON-NLS-1$

    volume_retain_brick("volume_retain_brick"), //$NON-NLS-1$

    volume_stop("volume_stop", "Volumes Tab > Stop Volume"), //$NON-NLS-1$ //$NON-NLS-2$

    new_role("new_role"), //$NON-NLS-1$

    edit_role("edit_role"), //$NON-NLS-1$

    copy_role("copy_role"), //$NON-NLS-1$

    clone_quota("clone_quota"), //$NON-NLS-1$

    edit_quota("edit_quota"), //$NON-NLS-1$

    attach_export_domain("attach_export_domain"), //$NON-NLS-1$

    attach_iso_library("attach_iso_library"), //$NON-NLS-1$

    attach_storage("attach_storage"), //$NON-NLS-1$

    new_cluster_policy("new_cluster_policy"), //$NON-NLS-1$

    edit_cluster_policy("edit_cluster_policy"), //$NON-NLS-1$

    copy_cluster_policy("copy_cluster_policy"), //$NON-NLS-1$

    shutdown_virtual_machine("shutdown_virtual_machine"), //$NON-NLS-1$

    stop_virtual_machine("stop_virtual_machine"), //$NON-NLS-1$

    reboot_virtual_machine("reboot_virtual_machine"); //$NON-NLS-1$

    public final String name;

    HelpTag(String name) {
        this.name = name;
    }

    HelpTag(String name, String comment) {
        this.name = name;
    }
}

package org.ovirt.engine.ui.frontend;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface AppErrors extends ConstantsWithLookup {
    @DefaultStringValue("Cannot remove Directory Group. Detach Group from  VM-Pool first.")
    String DIRECTORY_GROUP_CANNOT_REMOVE_DIRECTORY_GROUP_ATTACHED_TO_VM_POOL();

    @DefaultStringValue("Image file already exists.")
    String IO_CD_IMAGE_FILE_ALREADY_EXIST();

    @DefaultStringValue("Image file does not exist.")
    String IO_CD_IMAGE_FILE_NOT_EXIST();

    @DefaultStringValue("Invalid CD image extension.")
    String IO_INVALID_CD_IMAGE_EXTENSION();

    @DefaultStringValue("ISO directory does not exist.")
    String IO_ISO_DIRECTORY_NOT_EXIST();

    @DefaultStringValue("Snapshot creation failed.")
    String IRS_FAILED_CREATING_SNAPSHOT();

    @DefaultStringValue("Internal error, Snapshot cannot be retrieved.")
    String IRS_FAILED_RETRIEVING_SNAPSHOT_INFO();

    @DefaultStringValue("Storage Manager Service not responding.")
    String IRS_NETWORK_ERROR();

    @DefaultStringValue("Storage Manager protocol error.")
    String IRS_PROTOCOL_ERROR();

    @DefaultStringValue("Storage Manager response error.")
    String IRS_RESPONSE_ERROR();

    @DefaultStringValue("Not enough MAC addresses left in MAC Address Pool.")
    String MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES();

    @DefaultStringValue("Cannot ${action} ${type}. ${type} cannot be removed, because some data center is still using it.")
    String ACTION_TYPE_FAILED_CANNOT_REMOVE_STILL_USED_MAC_POOL();

    @DefaultStringValue("Cannot ${action} ${type}. Default ${type} cannot be removed.")
    String ACTION_TYPE_FAILED_CANNOT_REMOVE_DEFAULT_MAC_POOL();

    @DefaultStringValue("Cannot ${action} ${type}. ${type} does not exist.")
    String ACTION_TYPE_FAILED_MAC_POOL_DOES_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. Changing default ${type} is not supported.")
    String ACTION_TYPE_FAILED_CHANGING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Setting default ${type} is not supported.")
    String ACTION_TYPE_FAILED_SETTING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. ${type} must contain at least one MAC address range.")
    String ACTION_TYPE_FAILED_MAC_POOL_MUST_HAVE_RANGE();

    @DefaultStringValue("Cannot delete Template. Template is being used by the following VMs: ${vmsList}.")
    String VMT_CANNOT_REMOVE_DETECTED_DERIVED_VM();

    @DefaultStringValue("Cannot delete Base Template that has Template Versions, please first remove all Template Versions for this Template: ${versionsList}.")
    String VMT_CANNOT_REMOVE_BASE_WITH_VERSIONS();

    @DefaultStringValue("Cannot ${action} ${type}. The following Disk(s) are based on it: \n ${disksInfo}.")
    String ACTION_TYPE_FAILED_DETECTED_DERIVED_DISKS();

    @DefaultStringValue("Cannot ${action} ${type}. The specified disk snapshots don't belong to the same Disk.")
    String ACTION_TYPE_FAILED_DISKS_SNAPSHOTS_DONT_BELONG_TO_SAME_DISK();

    @DefaultStringValue("Cannot delete Template. The Template does not exist on the following Storage Domains: ${domainsList}.\nEither verify that Template exists on all Storage Domains listed on the domains list,\nor do not send domains list in order to delete all instances of the Template from the system.")
    String VMT_CANNOT_REMOVE_DOMAINS_LIST_MISMATCH();

    @DefaultStringValue("Cannot ${action} ${type}. VM's Image does not exist.")
    String ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. VM's Snapshot does not exist.")
    String ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The Snapshot type is ${snapshotType} while the operation is supported for Snapshots of the following type(s): ${supportedSnapshotTypes}.")
    String ACTION_TYPE_FAILED_VM_SNAPSHOT_TYPE_NOT_ALLOWED();

    @DefaultStringValue("Cannot ${action} ${type}. The snapshot configuration is corrupted (snapshot ID is empty). Please contact the system administrator.")
    String ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID();

    @DefaultStringValue("Cannot ${action} ${type}. The snapshot ${SnapshotName} of VM ${VmName} has no configuration available. Please choose a snapshot with configuration available.")
    String ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION();

    @DefaultStringValue("Cannot ${action} ${type}. This VM is not managed by the engine.")
    String ACTION_TYPE_FAILED_CANNOT_RUN_ACTION_ON_NON_MANAGED_VM();

    @DefaultStringValue("Cannot ${action} ${type}. Physical Memory Guaranteed cannot exceed Memory Size.")
    String ACTION_TYPE_FAILED_MIN_MEMORY_CANNOT_EXCEED_MEMORY_SIZE();

    @DefaultStringValue("Storage Domain cannot be accessed.\nPossible reasons:\nNo operational Host in Data Center or Data Center state is not Up.")
    String IMAGE_REPOSITORY_NOT_FOUND();

    @DefaultStringValue("Template is currently locked (temporarily).")
    String VM_TEMPLATE_IS_LOCKED();

    @DefaultStringValue("Template's Image is locked (temporarily).")
    String VM_TEMPLATE_IMAGE_IS_LOCKED();

    @DefaultStringValue("Template's Image is invalid (temporarily).")
    String VM_TEMPLATE_IMAGE_IS_ILLEGAL();

    @DefaultStringValue("Cannot ${action} ${type}. The image is the only remaining copy of a Template disk.")
    String VM_TEMPLATE_IMAGE_LAST_DOMAIN();

    @DefaultStringValue("VM Name cannot be empty.")
    String VM_NAME_CANNOT_BE_EMPTY();

    @DefaultStringValue("There was an attempt to change VM values while the VM is not down. Please shut down the VM in order to modify these properties.")
    String VM_CANNOT_UPDATE_ILLEGAL_FIELD();

    @DefaultStringValue("VM is configured to be 'Network bootable', but no Network Interface is configured.\nAlternatives:\n-Select a different boot device (using the 'Run Once' command or 'Edit VM' command on Boot Option Sub-Tab).\n-Configure Network Interface and rerun the VM.")
    String VM_CANNOT_RUN_FROM_NETWORK_WITHOUT_NETWORK();

    @DefaultStringValue("Cannot ${action} ${type} with attached CD without an active ISO domain.\nAlternatives:\n-Attach an ISO Domain (to enable CD operations).\n-Change the boot sequence, using the Edit VM command (Boot Sub-Tab).\n-Select a different boot device (using the 'Run Once' command).")
    String VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO();

    @DefaultStringValue("Cannot ${action} ${type} without at least one bootable disk.\nAlternatives:\n-Create a disk for this VM, and rerun the VM.\n-Change the boot sequence using the Edit VM command (Boot Option Sub-Tab).\n-Use the Run-Once command to select a different boot option and rerun the VM.")
    String VM_CANNOT_RUN_FROM_DISK_WITHOUT_DISK();

    @DefaultStringValue("Cannot ${action} ${type}. Stateless flag on VM conflicts with running the VM in Preview mode. Either remove the Stateless flag from the VM or run the VM not in Preview mode.")
    String VM_CANNOT_RUN_STATELESS_WHILE_IN_PREVIEW();

    @DefaultStringValue("Cannot ${action} ${type}. Highly Available Virtual servers can not be run as stateless.")
    String VM_CANNOT_RUN_STATELESS_HA();

    @DefaultStringValue("Cannot ${action} ${type}. Delete protection is enabled. In order to delete, disable Delete protection first.")
    String ACTION_TYPE_FAILED_DELETE_PROTECTION_ENABLED();

    @DefaultStringValue("Failed to attach VM to User.")
    String RESOURCE_MANAGER_FAILED_ATTACHING_VM_TO_USERS();

    @DefaultStringValue("Migration failed, VM is not running.")
    String RESOURCE_MANAGER_MIGRATING_VM_IS_NOT_UP();

    @DefaultStringValue("VM not found.")
    String RESOURCE_MANAGER_VM_NOT_FOUND();

    @DefaultStringValue("Search error.")
    String SEARCH();

    @DefaultStringValue("Error in Search text.")
    String SEARCH_ERRORNOUS_SEARCH_TEXT();

    @DefaultStringValue("Invalid Search text.")
    String SEARCH_INVALID_SEARCH_TEXT();

    @DefaultStringValue("Search unsupported business object.")
    String SEARCH_UNSUPPORTED_BUSINESS_OBJECT();

    @DefaultStringValue("Cannot create new VM. The disk image is being copied. Please wait for the Copy operation to complete.")
    String TEMPLATE_IMAGE_LOCKED();

    @DefaultStringValue("Cannot remove User attached to VM-Pool.Remove User from VM-Pool first by:\n-Going to 'VM-Pool(s)' window.\n-Selecting the VM-Pool.\n-Clicking on 'Manage Pool Users' from the menu bar.")
    String USER_CANNOT_REMOVE_USER_ATTACHED_POOL();

    @DefaultStringValue("Cannot remove User, user is not attached to VM-Pool.")
    String USER_CANNOT_REMOVE_USER_NOT_ATTACHED_POOL();

    @DefaultStringValue("Cannot remove a User that is attached to a VM. Remove VM from VMs tab first.")
    String USER_CANNOT_REMOVE_USER_DETECTED_RUNNING_VM();

    @DefaultStringValue("Cannot ${action} ${type}. User is currently logged in.")
    String USER_CANNOT_REMOVE_HIMSELF();

    @DefaultStringValue("Failed to get User data from Directory Server.")
    String USER_FAILED_POPULATE_DATA();

    @DefaultStringValue("Failed set User Role, operation canceled")
    String USER_FAILED_SET_USER_ROLE();

    @DefaultStringValue("Cannot switch Host to Maintenance mode.\nHost still has running VMs on it and is in Non Responsive state.")
    String VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_RESPONDING_WITH_VMS();

    @DefaultStringValue("Cannot switch Host to Maintenance mode.\nHost is Storage Pool Manager and is in Non Responsive state.\n"
            +
            "- If power management is configured, engine will try to fence automatically.\n"
            +
            "- Otherwise, either bring the node back up, or release the SPM resource.\n"
            +
            "  To do so, verify that the node is really down by right clicking on the host and confirm that the node was shutdown manually.")
    String VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_RESPONDING_AND_IS_SPM();

    @DefaultStringValue("Cannot switch Host to Maintenance mode, Host is not operational.")
    String VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_OPERATIONAL();

    @DefaultStringValue("Cannot switch Host to Maintenance mode. Host is already in Maintenance mode.")
    String VDS_CANNOT_MAINTENANCE_VDS_IS_IN_MAINTENANCE();

    @DefaultStringValue("Cannot switch Host(s) to Maintenance mode.\nThe following Enforcing Affinity Group(s) have running VMs and can break the affinity rule.\n${AFFINITY_GROUPS_VMS}\nPlease manually migrate the VMs, or change Affinity Group's enforcing to false.")
    String VDS_CANNOT_MAINTENANCE_VDS_HAS_AFFINITY_VMS();

    @DefaultStringValue("Host CPU type is not supported in this cluster compatibility version or is not supported at all.")
    String CPU_TYPE_UNSUPPORTED_IN_THIS_CLUSTER_VERSION();

    @DefaultStringValue("Cannot ${action} ${type}. The VM and the destination cluster architectures do not match.")
    String ACTION_TYPE_FAILED_VM_CLUSTER_DIFFERENT_ARCHITECTURES();

    @DefaultStringValue("The host and destination cluster architectures do not match.")
    String ACTION_TYPE_FAILED_VDS_CLUSTER_DIFFERENT_ARCHITECTURES();

    @DefaultStringValue("Cannot switch Host to Maintenance mode. Host has asynchronous running tasks,\nwait for operation to complete and retry.")
    String VDS_CANNOT_MAINTENANCE_SPM_WITH_RUNNING_TASKS();

    @DefaultStringValue("Cannot switch Host to Maintenance mode. Host is contending for Storage Pool Manager,\nwait for operation to complete and retry.")
    String VDS_CANNOT_MAINTENANCE_SPM_CONTENDING();

    @DefaultStringValue("Cannot switch the following Hosts to Maintenance mode: ${HostsList}.\nOne or more running VMs are indicated as non-migratable. The non-migratable VMs are: ${VmsList}.")
    String VDS_CANNOT_MAINTENANCE_IT_INCLUDES_NON_MIGRATABLE_VM();

    @DefaultStringValue("Cannot switch the following Hosts to Maintenance mode: ${HostsList}.\nThe following VMs cannot be migrated because they have activated Disk Snapshot attached (VM/Disk Snapshots): \n \n ${disksInfo} \n \nplease deactivate/detach the Disk snapshots or turn off those VMs and try again.")
    String VDS_CANNOT_MAINTENANCE_VM_HAS_PLUGGED_DISK_SNAPSHOT();

    @DefaultStringValue("Cannot ${action} ${type}. The operation is currently not supported for disks used as OVF store.")
    String ACTION_TYPE_FAILED_OVF_DISK_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. There is no OVF for the specific entity.")
    String ACTION_TYPE_FAILED_UNSUPPORTED_OVF();

    @DefaultStringValue("Cannot ${action} ${type}. The operation can be performed only for OVF disks that are in ${status} status.")
    String ACTION_TYPE_FAILED_OVF_DISK_NOT_IN_APPLICABLE_STATUS();

    @DefaultStringValue("Cannot remove default Host Cluster.")
    String VDS_CANNOT_REMOVE_DEFAULT_VDS_GROUP();

    @DefaultStringValue("Cannot ${action} ${type}. One or more VMs are still running on this Host. ")
    String VDS_CANNOT_REMOVE_VDS_DETECTED_RUNNING_VM();

    @DefaultStringValue("Cannot ${action} ${type}. Host Cluster contains one or more Hosts.")
    String VDS_CANNOT_REMOVE_VDS_GROUP_VDS_DETECTED();

    @DefaultStringValue("Cannot ${action} ${type}. Host is operational. Please switch Host to Maintenance mode first.")
    String VDS_CANNOT_REMOVE_VDS_STATUS_ILLEGAL();

    @DefaultStringValue("Cannot ${action} ${type}. Internal Error: Host does not exists in DB.")
    String VDS_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The Host ${VdsName} is not active.")
    String CANNOT_FORCE_SELECT_SPM_VDS_NOT_UP();

    @DefaultStringValue("Cannot ${action} ${type}. The Host ${VdsName} is already SPM or contending.")
    String CANNOT_FORCE_SELECT_SPM_VDS_ALREADY_SPM();

    @DefaultStringValue("Cannot ${action} ${type}. The SPM priority of Host ${VdsName} is set to 'never'. This Host cannot be elected as SPM.")
    String CANNOT_FORCE_SELECT_SPM_VDS_MARKED_AS_NEVER_SPM();

    @DefaultStringValue("Cannot ${action} ${type}. The Storage Pool has running tasks.")
    String CANNOT_FORCE_SELECT_SPM_STORAGE_POOL_HAS_RUNNING_TASKS();

    @DefaultStringValue("Cannot ${action} ${type}. The Host is not a part of a Storage Pool.")
    String CANNOT_FORCE_SELECT_SPM_VDS_NOT_IN_POOL();

    @DefaultStringValue("Internal error: Host protocol error.")
    String VDS_PROTOCOL_ERROR();

    @DefaultStringValue("Internal error: Host response error")
    String VDS_RESPONSE_ERROR();

    @DefaultStringValue("Cannot perform Stop operation, Host has to be in Maintenance mode in order to be stopped.")
    String VDS_STATUS_NOT_VALID_FOR_STOP();

    @DefaultStringValue("Cannot perform Start operation, Host has to be in one of the following statuses: Down ,Non Responsive or Maintenance.")
    String VDS_STATUS_NOT_VALID_FOR_START();

    @DefaultStringValue("There is no other Host in the Data Center that can be used to test the Power Management settings.")
    String VDS_NO_VDS_PROXY_FOUND();

    @DefaultStringValue("Connection to Host via proxy failed. Please verify that power management is available, and that the provided connection parameters are correct.")
    String VDS_FAILED_FENCE_VIA_PROXY_CONNECTION();

    @DefaultStringValue("Cannot ${action} ${type}. Host parameters cannot be modified while Host is operational.\nPlease switch Host to Maintenance mode first.")
    String VDS_STATUS_NOT_VALID_FOR_UPDATE();

    @DefaultStringValue("Cannot ${action} ${type}. Host in Up status.")
    String VDS_ALREADY_UP();

    @DefaultStringValue("Cannot ${action} ${type}. Host has no unique id.")
    String VDS_NO_UUID();

    @DefaultStringValue("Cannot ${action} ${type}. Host is non responsive.")
    String VDS_NON_RESPONSIVE();

    @DefaultStringValue("Cannot ${action} ${type}. Host does not exist.")
    String VDS_DOES_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. Invalid Host Id.")
    String VDS_INVALID_SERVER_ID();

    @DefaultStringValue("Cannot ${action} ${type}. Empty host name or id.")
    String VDS_EMPTY_NAME_OR_ID();

    @DefaultStringValue("Cannot install Host with empty password.")
    String VDS_CANNOT_INSTALL_EMPTY_PASSWORD();

    @DefaultStringValue("Cannot install Host. Please move Host to Maintenance mode first.")
    String VDS_CANNOT_INSTALL_STATUS_ILLEGAL();

    @DefaultStringValue("Cannot upgrade Host. Host version is not compatible with selected ISO version. Please select an ISO with major version ${IsoVersion}.x.")
    String VDS_CANNOT_UPGRADE_BETWEEN_MAJOR_VERSION();

    @DefaultStringValue("Cannot install Host. Image file is missing.")
    String VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE();

    @DefaultStringValue("The Host Port number cannot be changed without reinstalling the Host.")
    String VDS_PORT_CHANGE_REQUIRE_INSTALL();

    @DefaultStringValue("Cannot add new Host using a secured connection, Certificate file could not be found.")
    String VDS_TRY_CREATE_SECURE_CERTIFICATE_NOT_FOUND();

    @DefaultStringValue("Cannot fence Host, Host fence is disabled.")
    String VDS_FENCE_DISABLED();

    @DefaultStringValue("Fence is disabled due to the Engine Service start up sequence.")
    String VDS_FENCE_DISABLED_AT_SYSTEM_STARTUP_INTERVAL();

    @DefaultStringValue(" Power Management operation ${operation} is still running, please retry in ${seconds} Sec.")
    String VDS_FENCE_DISABLED_AT_QUIET_TIME();

    @DefaultStringValue("Host ${VdsName} became Non Responsive and was not restarted due to disabled fencing in the Cluster Fencing Policy.")
    String VDS_FENCE_DISABLED_BY_CLUSTER_POLICY();

    @DefaultStringValue("Cannot ${action} ${type}. Template's image doesn't exist.")
    String TEMPLATE_IMAGE_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. One or more VMs are still assigned to the Cluster")
    String VM_CANNOT_REMOVE_VDS_GROUP_VMS_DETECTED();

    @DefaultStringValue("Cannot ${action} ${type}. One or more Template(s) are still associated with it")
    String VMT_CANNOT_REMOVE_VDS_GROUP_VMTS_DETECTED();

    @DefaultStringValue("Cannot ${action} ${type}.One or more VM-Pools are still associated with it")
    String VDS_GROUP_CANNOT_REMOVE_HAS_VM_POOLS();

    @DefaultStringValue("Cannot run VM. The disk image is being copied. Please wait for the copy operation to complete.")
    String VM_IMAGE_LOCKED();

    @DefaultStringValue("Cannot attach VM to VM-Pool. VM-Pool is already attached to a User.")
    String VM_POOL_CANNOT_ADD_VM_WITH_USERS_ATTACHED_TO_POOL();

    @DefaultStringValue("User is already attached to this VM-Pool.")
    String ACTION_TYPE_FAILED_USER_ATTACHED_TO_POOL();

    @DefaultStringValue("Cannot attach VM to pool. VM is already attached to another VM-Pool.")
    String VM_POOL_CANNOT_ADD_VM_ATTACHED_TO_POOL();

    @DefaultStringValue("Cannot detach VM from pool. VM is not attached to the VM-Pool.")
    String VM_POOL_CANNOT_DETACH_VM_NOT_ATTACHED_TO_POOL();

    @DefaultStringValue("Cannot attach VM to VM-Pool. VM resides on different Host Cluster than VM-Pool.")
    String VM_POOL_CANNOT_ADD_VM_DIFFERENT_CLUSTER();

    @DefaultStringValue("Cannot remove VM-Pool. Detach Directory Groups from VM-Pool first.")
    String VM_POOL_CANNOT_REMOVE_VM_POOL_WITH_ATTACHED_DIRECTORY_GROUPS();

    @DefaultStringValue("Cannot remove VM-Pool. Detach Users from VM-Pool first.")
    String VM_POOL_CANNOT_REMOVE_VM_POOL_WITH_ATTACHED_USERS();

    @DefaultStringValue("Cannot remove VM-Pool. Detach VMs from VM-Pool first.")
    String VM_POOL_CANNOT_REMOVE_VM_POOL_WITH_VMS();

    @DefaultStringValue("Cannot detach VM from VM-Pool. VM is running.")
    String VM_POOL_CANNOT_REMOVE_RUNNING_VM_FROM_POOL();

    @DefaultStringValue("Cannot attach VM to VM-Pool. VM is running.")
    String VM_POOL_CANNOT_ADD_RUNNING_VM_TO_POOL();

    @DefaultStringValue("Invalid Host Cluster id.")
    String VM_INVALID_SERVER_CLUSTER_ID();

    @DefaultStringValue("Cannot ${action} ${type}. VM is locked or still running, try again once VM is in the Down state.")
    String VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM();

    @DefaultStringValue("Cannot ${action} ${type}. Removing Blank Template is not allowed.")
    String VMT_CANNOT_REMOVE_BLANK_TEMPLATE();

    @DefaultStringValue("Cannot ${action} ${type}. Editing Blank Template is not allowed.")
    String VMT_CANNOT_EDIT_BLANK_TEMPLATE();

    @DefaultStringValue("Cannot export Blank Template.")
    String VMT_CANNOT_EXPORT_BLANK_TEMPLATE();

    @DefaultStringValue("Failed updating the properties of the VM template.")
    String VMT_CANNOT_UPDATE_ILLEGAL_FIELD();

    @DefaultStringValue("Cannot update the name of Sub-Templates, Only the Version name can be updated.")
    String VMT_CANNOT_UPDATE_VERSION_NAME();

    @DefaultStringValue("Cannot remove Directory Group. Detach Directory Group from VM first.")
    String DIRECTORY_GROUP_CANNOT_REMOVE_DIRECTORY_GROUP_ATTACHED_TO_VM();

    @DefaultStringValue("VM not found")
    String VM_NOT_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. VM is previewing a Snapshot.")
    String ACTION_TYPE_FAILED_VM_IN_PREVIEW();

    @DefaultStringValue("Cannot ${action} ${type}. The following VMs are previewing a snapshot: ${vms}.")
    String ACTION_TYPE_FAILED_STORAGE_DELETE_VMS_IN_PREVIEW();

    @DefaultStringValue("Cannot ${action} ${type}. The following VM's disks snapshots are attached to other VMs (Disk Alias/Snapshot Description/VM attached to):\n\n ${disksInfo} \n\nPlease detach them from those VMs and try again.")
    String ACTION_TYPE_FAILED_VM_DISK_SNAPSHOT_IS_ATTACHED_TO_ANOTHER_VM();

    @DefaultStringValue("Cannot ${action} ${type}. The following VM's disks snapshots are plugged to other VMs (Disk Alias/Snapshot Description/VM attached to):\n\n ${disksInfo} \n\nPlease deactivate/detach them from those VMs and try again.")
    String ACTION_TYPE_FAILED_VM_DISK_SNAPSHOT_IS_PLUGGED_TO_ANOTHER_VM();

    @DefaultStringValue("Cannot ${action} ${type}. The following VM's activated disks are disk snapshots (VM/Disk Snapshots): \n\n ${disksInfo}. \n\nPlease deactivate them and try again.")
    String ACTION_TYPE_FAILED_VM_HAS_PLUGGED_DISK_SNAPSHOT();

    @DefaultStringValue("Cannot ${action} ${type}. Shareable disks are not supported on Gluster domains.")
    String ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN();

    @DefaultStringValue("Cannot ${action} ${type}: The following disks are locked: ${diskAliases}. Please try again in a few minutes.")
    String ACTION_TYPE_FAILED_DISKS_LOCKED();

    @DefaultStringValue("Cannot ${action} ${type}: The disk interface is not supported by the VM OS: ${osName}.")
    String ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. The following attached disks are in ILLEGAL status: ${diskAliases} - please remove them and try again.")
    String ACTION_TYPE_FAILED_DISKS_ILLEGAL();

    @DefaultStringValue("Cannot ${action} ${type}. The following disks could not be moved: ${diskAliases}. Please make sure that all disks are active or inactive in the VM.")
    String ACTION_TYPE_FAILED_MOVE_DISKS_MIXED_PLUGGED_STATUS();

    @DefaultStringValue("Cannot ${action} ${type}. The following disks already exist: ${diskAliases}. Please import as a clone.")
    String ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The cluster has an empty processor name.")
    String ACTION_TYPE_FAILED_CLUSTER_EMPTY_PROCESSOR_NAME();

    @DefaultStringValue("Cannot ${action} ${type}. The cluster does not have a defined architecture.")
    String ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE();

    @DefaultStringValue("Cannot ${action} ${type}: VM is locked. Please try again in a few minutes.")
    String ACTION_TYPE_FAILED_VM_IS_LOCKED();

    @DefaultStringValue("Cannot ${action} ${type}: VM is being exported now. Please try again in a few minutes.")
    String ACTION_TYPE_FAILED_VM_DURING_EXPORT();

    @DefaultStringValue("Cannot ${action} ${type}. VM's Image might be corrupted.")
    String ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL();

    @DefaultStringValue("Cannot ${action} ${type}. VM has no disks.")
    String ACTION_TYPE_FAILED_VM_HAS_NO_DISKS();

    @DefaultStringValue("Cannot ${action} ${type}: Storage Domain cannot be accessed.\n-Please check that at least one Host is operational and Data Center state is up.")
    String ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. VM is running.")
    String ACTION_TYPE_FAILED_VM_IS_RUNNING();

    @DefaultStringValue("Cannot ${action} ${type}. VM is being cloned.")
    String ACTION_TYPE_FAILED_VM_IS_BEING_CLONED();

    @DefaultStringValue("Cannot ${action} ${type}. VM is being updated.")
    String ACTION_TYPE_FAILED_VM_IS_BEING_UPDATED();

    @DefaultStringValue("Cannot ${action} ${type}. VM is not running.")
    String ACTION_TYPE_FAILED_VM_IS_NOT_RUNNING();

    @DefaultStringValue("Cannot ${action} ${type}. VM is not up.")
    String ACTION_TYPE_FAILED_VM_IS_NOT_UP();

    @DefaultStringValue("Cannot ${action} ${type}. At least one of the VMs is not down.")
    String ACTION_TYPE_FAILED_VM_IS_NOT_DOWN();

    @DefaultStringValue("Cannot ${action} ${type}. VM ${VmName} must be in status Down, Up or Paused.")
    String ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP();

    @DefaultStringValue("Cannot ${action} ${type}. The host running VM ${VmName} is not capable of live merging snapshots.")
    String ACTION_TYPE_FAILED_VM_HOST_CANNOT_LIVE_MERGE();

    @DefaultStringValue("Cannot ${action} ${type}. VM is in saving/restoring state.\n-Please try again when the VM is either up or down.")
    String ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING();

    @DefaultStringValue("Cannot ${action} ${type}. The VM is performing an operation on a Snapshot. Please wait for the operation to finish, and try again.")
    String ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT();

    @DefaultStringValue("Cannot ${action} ${type} because the VM is in ${vmStatus} status.")
    String ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL();

    @DefaultStringValue("Cannot ${action} ${type}. The VM is running as Stateless. Please try again when VM is not running as Stateless.")
    String ACTION_TYPE_FAILED_VM_RUNNING_STATELESS();

    @DefaultStringValue("Cannot ${action} ${type}. The VM was running as Stateless and didn't clean up successfully. Please try to run the VM which should clean up the VM, and then try again when VM is not running.")
    String ACTION_TYPE_FAILED_VM_HAS_STATELESS_SNAPSHOT_LEFTOVER();

    @DefaultStringValue("Cannot ${action} ${type}. The VM is in use by other user.")
    String ACTION_TYPE_FAILED_VM_IN_USE_BY_OTHER_USER();

    @DefaultStringValue("Cannot ${action} ${type}. VM is not found.")
    String ACTION_TYPE_FAILED_VM_NOT_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. Cannot use 'Latest Version' when using clone from Template.")
    String ACTION_TYPE_FAILED_CANNOT_USE_LATEST_WITH_CLONE();

    @DefaultStringValue("Cannot ${action} ${type}. Vm is set to use a specific version, and not automatically update to the latest version.")
    String ACTION_TYPE_FAILED_VM_NOT_SET_FOR_LATEST();

    @DefaultStringValue("Cannot ${action} ${type}. Vm is already at the latest version.")
    String ACTION_TYPE_FAILED_VM_ALREADY_IN_LATEST_VERSION();

    @DefaultStringValue("Cannot ${action} ${type}. VM is non migratable.")
    String ACTION_TYPE_FAILED_VM_IS_NON_MIGRTABLE();

    @DefaultStringValue("Cannot ${action} ${type}. VM is non migratable and user did not specify the force-migration flag")
    String ACTION_TYPE_FAILED_VM_IS_NON_MIGRTABLE_AND_IS_NOT_FORCED_BY_USER_TO_MIGRATE();

    @DefaultStringValue("Cannot ${action} ${type}. VM is pinned to Host.")
    String ACTION_TYPE_FAILED_VM_IS_PINNED_TO_HOST();

    @DefaultStringValue("Note: The VM is pinned to Host '${VdsName}' but cannot run on it.")
    String VM_PINNED_TO_HOST_CANNOT_RUN_ON_THE_DEFAULT_VDS();

    @DefaultStringValue("$VdsName [N/A]")
    String HOST_NAME_NOT_AVAILABLE();

    @DefaultStringValue("Cannot ${action} ${type}. Migration is not supported in this CPU architecture.")
    String MIGRATION_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. This resilience policy is not supported in this CPU architecture.")
    String MIGRATION_ON_ERROR_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Migration option is not supported in this CPU architecture.")
    String VM_MIGRATION_IS_NOT_SUPPORTED();

    @DefaultStringValue("CPU pinning format invalid.")
    String VM_PINNING_FORMAT_INVALID();

    @DefaultStringValue("CPU pinning validation failed - virtual CPU does not exist in vm.")
    String VM_PINNING_VCPU_DOES_NOT_EXIST();

    @DefaultStringValue("CPU pinning validation failed - CPU does not exist in host.")
    String VM_PINNING_PCPU_DOES_NOT_EXIST();

    @DefaultStringValue("Cannot configure CPU pinning twice to the same vCPU.")
    String VM_PINNING_DUPLICATE_DEFINITION();

    @DefaultStringValue("Cannot pin a vCPU to no pCPU.")
    String VM_PINNING_PINNED_TO_NO_CPU();

    @DefaultStringValue("Cannot ${action} ${type}. A highly available VM cannot be pinned to a specific Host")
    String ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_PINNED_TO_HOST();

    @DefaultStringValue("Cannot ${action} ${type}. VM with the same identifier already exists.")
    String ACTION_TYPE_FAILED_VM_GUID_ALREADY_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. VM is attached to a VM Pool.")
    String ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL();

    @DefaultStringValue("Cannot ${action} ${type}. There are no available VMs in the VM-Pool.")
    String ACTION_TYPE_FAILED_NO_AVAILABLE_POOL_VMS();

    @DefaultStringValue("Cannot ${action} ${type}. VM from Pool cannot be stateless.")
    String ACTION_TYPE_FAILED_VM_FROM_POOL_CANNOT_BE_STATELESS();

    @DefaultStringValue("Cannot ${action} ${type}. Number of Prestarted VMs cannot exceed the number of VMs in the Pool.")
    String ACTION_TYPE_FAILED_PRESTARTED_VMS_CANNOT_EXCEED_VMS_COUNT();

    @DefaultStringValue("Cannot ${action} ${type}: VM was created from the Blank Template and does not contain an operating system. Please run the VM using 'Boot from CD' or 'Boot from Network' options from the 'Run Once' menu.")
    String ACTION_TYPE_FAILED_VM_WITH_BLANK_TEMPLATE();

    @DefaultStringValue("Cannot ${action} ${type}. Low disk space on relevant Storage Domain.")
    String ACTION_TYPE_FAILED_DISK_SPACE_LOW();

    @DefaultStringValue("Cannot ${action} ${type}. The operation is unsupported for ${volumeType} Disk(s), please try again with Disk(s) with one of the following type(s): ${supportedVolumeTypes}.")
    String ACTION_TYPE_FAILED_DISK_VOLUME_TYPE_UNSUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. The Data Center has no Storage Pool Manager.")
    String ACTION_TYPE_FAILED_NO_SPM();

    @DefaultStringValue("Cannot ${action} ${type}. The Data Center's Storage Pool Manager has changed.")
    String ACTION_TYPE_FAILED_SPM_CHANGED();

    @DefaultStringValue("Cannot ${action} ${type}. Low disk space on Storage Domain ${storageName}.")
    String ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN();

    @DefaultStringValue("Cannot ${action} ${type}. VM is pinned to a specific host. The host's cluster must be the same as the selected VM cluster.")
    String ACTION_TYPE_FAILED_DEDICATED_VDS_NOT_IN_SAME_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. Disk configuration (${volumeFormat} ${volumeType}) is incompatible with the storage domain type.")
    String ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. The OVF configuration could not be parsed.")
    String ACTION_TYPE_FAILED_OVF_CONFIGURATION_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. VM migration is in progress")
    String ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS();

    @DefaultStringValue("Cannot ${action} ${type}. The provided lun is used by another disk.")
    String ACTION_TYPE_FAILED_DISK_LUN_IS_ALREADY_IN_USE();

    @DefaultStringValue("Cannot ${action} ${type}. The provided lun has no valid lun type.")
    String ACTION_TYPE_FAILED_DISK_LUN_HAS_NO_VALID_TYPE();

    @DefaultStringValue("Cannot ${action} ${type}. The provided lun is missing at least one connection parameter (address/port/iqn).")
    String ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS();

    @DefaultStringValue("Cannot ${action} ${type}. The provided LUN is not visible by the specified host, please check storage server connectivity.")
    String ACTION_TYPE_FAILED_DISK_LUN_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. source and destination is the same.")
    String ACTION_TYPE_FAILED_MIGRATION_TO_SAME_HOST();

    @DefaultStringValue("Cannot ${action} ${type} if custom properties are in invalid format. Please check the input.")
    String ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_INVALID_SYNTAX();

    @DefaultStringValue("Cannot ${action} ${type} if some of the specified custom properties are not configured by the system. The keys are: ${MissingKeys}")
    String ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_INVALID_KEYS();

    @DefaultStringValue("Cannot ${action} ${type} if some of the specified custom properties have illegal values. The keys are: ${WrongValueKeys}")
    String ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_INVALID_VALUES();

    @DefaultStringValue("Cannot ${action} ${type}. Custom properties are not supported in version: ${NotSupportedInVersion}")
    String ACTION_TYPE_FAILED_CUSTOM_PROPERTIES_NOT_SUPPORTED_IN_VERSION();

    @DefaultStringValue("Cannot ${action} ${type}. Network custom properties are not supported in the cluster's compatibility version, but they were supplied for the following network(s): ${ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED_LIST}.")
    String ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Some network custom properties contained errors (bad syntax, non-existing keys or invalid values), please take a closer look at the following network(s): ${ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT_LIST}. Refer to the engine log for further details.")
    String ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT();

    @DefaultStringValue("Cannot ${action} ${type}. Custom properties are not supported for device type: ${InvalidDeviceType}")
    String ACTION_TYPE_FAILED_INVALID_DEVICE_TYPE_FOR_CUSTOM_PROPERTIES();

    @DefaultStringValue("Cannot ${action} ${type}. There are no available running Hosts in the Host Cluster.")
    String ACTION_TYPE_FAILED_VDS_VM_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. There are no available running Hosts with sufficient memory in VM's Cluster .")
    String ACTION_TYPE_FAILED_VDS_VM_MEMORY();

    @DefaultStringValue("Cannot ${action} ${type}. There are no available running Hosts with enough cores in VM's Cluster .")
    String ACTION_TYPE_FAILED_VDS_VM_CPUS();

    @DefaultStringValue("Cannot ${action} ${type}. The host has lower CPU level than the VM was run with.")
    String ACTION_TYPE_FAILED_VDS_VM_CPU_LEVEL();

    @DefaultStringValue("Cannot ${action} ${type}. There are no available running Hosts with all the networks used by the VM.")
    String ACTION_TYPE_FAILED_VDS_VM_NETWORKS();

    @DefaultStringValue("Cannot ${action} ${type}. There are no available running Hosts with the cluster's display network.")
    String ACTION_TYPE_FAILED_MISSING_DISPLAY_NETWORK();

    @DefaultStringValue("Cannot ${action} ${type}. There are no available HA hosts in the VM's Cluster.")
    String ACTION_TYPE_FAILED_NO_HA_VDS();

    @DefaultStringValue("The following Hosts have running VMs and cannot be switched to maintenance mode: ${HostsList}.\nPlease ensure that the following Clusters have at least one Host in UP state: ${ClustersList}.")
    String CANNOT_MAINTENANCE_VDS_RUN_VMS_NO_OTHER_RUNNING_VDS();

    @DefaultStringValue("Cannot ${action} ${type}. VM's tools version (${toolsVersion}) mismatch with the Host's (${serverVersion}) version.")
    String ACTION_TYPE_FAILED_VDS_VM_VERSION();

    @DefaultStringValue("Cannot ${action} ${type}. Host swap percentage is above the defined threshold.\n- Check your configuration parameters for Host Swap Percentage.")
    String ACTION_TYPE_FAILED_VDS_VM_SWAP();

    @DefaultStringValue("Cannot ${action} ${type}. There is no available operational Host (in UP state) in the relevant Cluster.")
    String ACTION_TYPE_FAILED_NO_VDS_AVAILABLE_IN_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. Removing the Template Snapshot is not allowed.")
    String ACTION_TYPE_FAILED_CANNOT_REMOVE_IMAGE_TEMPLATE();

    @DefaultStringValue("Cannot ${action} ${type}. Failed to get data for Import operation.\n- Check your Import Domain.")
    String ACTION_TYPE_FAILED_PROBLEM_WITH_CANDIDATE_INFO();

    @DefaultStringValue("Cannot ${action} ${type}. The relevant Template doesn't exist.")
    String ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The relevant Cluster doesn't exist.")
    String ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY();

    @DefaultStringValue("Cannot ${action} ${type}. Only the first template version can be selected as the base template.")
    String ACTION_TYPE_FAILED_TEMPLATE_VERSION_CANNOT_BE_BASE_TEMPLATE();

    @DefaultStringValue("Cannot ${action} ${type} with an empty disk alias.")
    String ACTION_TYPE_FAILED_TEMPLATE_CANNOT_BE_CREATED_WITH_EMPTY_DISK_ALIAS();

    @DefaultStringValue("Cannot ${action} ${type}. The relevant Instance Type doesn't exist.")
    String ACTION_TYPE_FAILED_INSTANCE_TYPE_DOES_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The relevant Image Type doesn't exist.")
    String ACTION_TYPE_FAILED_IMAGE_TYPE_DOES_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The Template is disabled, please try to enable the template first and try again.")
    String ACTION_TYPE_FAILED_TEMPLATE_IS_DISABLED();

    @DefaultStringValue("The specified Template doesn't exist in the current Data Center.")
    String ACTION_TYPE_FAILED_TEMPLATE_NOT_EXISTS_IN_CURRENT_DC();

    @DefaultStringValue("Cannot ${action} ${type}. One of the Template Images already exists.")
    String ACTION_TYPE_FAILED_IMAGE_ALREADY_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. A Template with the same identifier already exists.")
    String ACTION_TYPE_FAILED_TEMPLATE_GUID_ALREADY_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. The export candidate already exists in the specified path.\n- Use the 'Force Override' option to override the existing file.")
    String ACTION_TYPE_FAILED_CANDIDATE_ALREADY_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. The Role is Read-Only.")
    String ACTION_TYPE_FAILED_ROLE_IS_READ_ONLY();

    @DefaultStringValue("Cannot ${action} ${type}. The target Data Center does not contain the ${type}.")
    String ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH();

    @DefaultStringValue("Cannot ${action} ${type}. The target Data Center does not contain the Virtual Machines.")
    String ACTION_TYPE_FAILED_STORAGE_POOL_OF_VM_NOT_MATCH();

    @DefaultStringValue("Cannot ${action} ${type}. The Storage Domain already contains the target disk(s).")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_CONTAINS_DISK();

    @DefaultStringValue("Cannot ${action} ${type}. Storage Domain hasn't been specified.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_SPECIFIED();

    @DefaultStringValue("Cannot ${action} ${type}. The following VMs/Templates are delete protected: ${vms}.")
    String ACTION_TYPE_FAILED_STORAGE_DELETE_PROTECTED();

    @DefaultStringValue("Cannot ${action} ${type}. The following VMs/Templates are attached to pool: ${vms}.")
    String ACTION_TYPE_FAILED_STORAGE_VMS_IN_POOL();

    @DefaultStringValue("Cannot ${action} ${type}. The Storage Domain name is already in use.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The Storage Domain already exists.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The Data Center name is already in use.")
    String ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The selected Storage Domain does not contain the VM Template.")
    String ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN();

    @DefaultStringValue("Can not ${action} ${type}. The given name is invalid for pool name. Only lower-case and upper-case letters, numbers, '_', '-', '.', and one mask sequence are allowed.")
    String ACTION_TYPE_FAILED_INVALID_POOL_NAME();

    @DefaultStringValue("Cannot ${action} ${type}. There is no active Host in the Data Center.")
    String ACTION_TYPE_FAILED_NO_VDS_IN_POOL();

    @DefaultStringValue("$type Host")
    String VAR__TYPE__HOST();

    @DefaultStringValue("$entities hosts")
    String VAR__ENTITIES__HOSTS();

    @DefaultStringValue("$type Networks")
    String VAR__TYPE__NETWORKS();

    @DefaultStringValue("$type Network")
    String VAR__TYPE__NETWORK();

    @DefaultStringValue("$type Label")
    String VAR__TYPE__LABEL();

    @DefaultStringValue("$type VM network interface profile")
    String VAR__TYPE__VNIC_PROFILE();

    @DefaultStringValue("$type provider")
    String VAR__TYPE__PROVIDER();

    @DefaultStringValue("$type provider certificate chain")
    String VAR__TYPE__PROVIDER_CERTIFICATE_CHAIN();

    @DefaultStringValue("$type VM")
    String VAR__TYPE__VM();

    @DefaultStringValue("$entities virtual machines")
    String VAR__ENTITIES__VMS();

    @DefaultStringValue("$type Quota")
    String VAR__TYPE__QUOTA();

    @DefaultStringValue("$type Computer Account")
    String VAR__TYPE__COMPUTER_ACCOUNT();

    @DefaultStringValue("$type Template")
    String VAR__TYPE__VM_TEMPLATE();

    @DefaultStringValue("$entities templates")
    String VAR__ENTITIES__VM_TEMPLATES();

    @DefaultStringValue("$type Snapshot")
    String VAR__TYPE__SNAPSHOT();

    @DefaultStringValue("$type VM-Pool")
    String VAR__TYPE__DESKTOP_POOL();

    @DefaultStringValue("$type VM from VM-Pool")
    String VAR__TYPE__VM_FROM_VM_POOL();

    @DefaultStringValue("$type Cluster")
    String VAR__TYPE__CLUSTER();

    @DefaultStringValue("$type Role")
    String VAR__TYPE__ROLE();

    @DefaultStringValue("$type Interface")
    String VAR__TYPE__INTERFACE();

    @DefaultStringValue("$type Virtual Machine Disk")
    String VAR__TYPE__VM_DISK();

    @DefaultStringValue("$type Bookmark")
    String VAR__TYPE__BOOKMARK();

    @DefaultStringValue("$type Virtual Machine Ticket")
    String VAR__TYPE__VM_TICKET();

    @DefaultStringValue("$type Storage Connection")
    String VAR__TYPE__STORAGE__CONNECTION();

    @DefaultStringValue("$type Storage")
    String VAR__TYPE__STORAGE__DOMAIN();

    @DefaultStringValue("$type Data Center")
    String VAR__TYPE__STORAGE__POOL();

    @DefaultStringValue("$type MAC Pool")
    String VAR__TYPE__MAC__POOL();

    @DefaultStringValue("$type User to VM")
    String VAR__TYPE__USER_FROM_VM();

    @DefaultStringValue("$type User")
    String VAR__TYPE__USER();

    @DefaultStringValue("$type Permission")
    String VAR__TYPE__PERMISSION();

    @DefaultStringValue("$type Host capabilities")
    String VAR__TYPE__HOST_CAPABILITIES();

    @DefaultStringValue("$type Network QoS")
    String VAR__TYPE__NETWORK_QOS();

    @DefaultStringValue("$type QoS")
    String VAR__TYPE__QOS();

    @DefaultStringValue("$type SPM")
    String VAR__TYPE__SPM();

    @DefaultStringValue("$type Scheduling Policy")
    String VAR__TYPE__CLUSTER_POLICY();

    @DefaultStringValue("$type Policy Unit")
    String VAR__TYPE__POLICY_UNIT();

    @DefaultStringValue("$type subnet")
    String VAR__TYPE__SUBNET();

    @DefaultStringValue("$type Affinity Group")
    String VAR__TYPE__AFFINITY_GROUP();

    @DefaultStringValue("$type iSCSI Bond")
    String VAR__TYPE__ISCSI_BOND();

    @DefaultStringValue("$type Disk Snapshot")
    String VAR__TYPE__DISK__SNAPSHOT();

    @DefaultStringValue("$type Disk Profile")
    String VAR__TYPE__DISK_PROFILE();

    @DefaultStringValue("$type CPU Profile")
    String VAR__TYPE__CPU_PROFILE();

    @DefaultStringValue("$action run")
    String VAR__ACTION__RUN();

    @DefaultStringValue("$action remove")
    String VAR__ACTION__REMOVE();

    @DefaultStringValue("$action add")
    String VAR__ACTION__ADD();

    @DefaultStringValue("$action create")
    String VAR__ACTION__CREATE();

    @DefaultStringValue("$action suspend")
    String VAR__ACTION__PAUSE();

    @DefaultStringValue("$action hibernate")
    String VAR__ACTION__HIBERNATE();

    @DefaultStringValue("$action migrate")
    String VAR__ACTION__MIGRATE();

    @DefaultStringValue("$action cancel migration")
    String VAR__ACTION__CANCEL_MIGRATE();

    @DefaultStringValue("$action attach VM to")
    String VAR__ACTION__ATTACH_DESKTOP_TO();

    @DefaultStringValue("$action revert to")
    String VAR__ACTION__REVERT_TO();

    @DefaultStringValue("$action preview")
    String VAR__ACTION__PREVIEW();

    @DefaultStringValue("$action stop")
    String VAR__ACTION__STOP();

    @DefaultStringValue("$action start")
    String VAR__ACTION__START();

    @DefaultStringValue("$action restart")
    String VAR__ACTION__RESTART();

    @DefaultStringValue("$action shutdown")
    String VAR__ACTION__SHUTDOWN();

    @DefaultStringValue("$action export")
    String VAR__ACTION__EXPORT();

    @DefaultStringValue("$action extend")
    String VAR__ACTION__EXTEND();

    @DefaultStringValue("$action import")
    String VAR__ACTION__IMPORT();

    @DefaultStringValue("$action attach")
    String VAR__ACTION__ATTACH_ACTION_TO();

    @DefaultStringValue("$action detach")
    String VAR__ACTION__DETACH_ACTION_TO();

    @DefaultStringValue("$action move")
    String VAR__ACTION__MOVE();

    @DefaultStringValue("$action copy")
    String VAR__ACTION__COPY();

    @DefaultStringValue("$action Change CD")
    String VAR__ACTION__CHANGE_CD();

    @DefaultStringValue("$action Eject CD")
    String VAR__ACTION__EJECT_CD();

    @DefaultStringValue("$action allocate and run")
    String VAR__ACTION__ALLOCATE_AND_RUN();

    @DefaultStringValue("$action confirm 'Host has been rebooted'")
    String VAR__ACTION__MANUAL_FENCE();

    @DefaultStringValue("$action maintenance")
    String VAR__ACTION__MAINTENANCE();

    @DefaultStringValue("$action setup")
    String VAR__ACTION__SETUP();

    @DefaultStringValue("$action set")
    String VAR__ACTION__SET();

    @DefaultStringValue("$action reset")
    String VAR__ACTION__RESET();

    @DefaultStringValue("$action edit")
    String VAR__ACTION__UPDATE();

    @DefaultStringValue("$action attach")
    String VAR__ACTION__ATTACH();

    @DefaultStringValue("$action detach")
    String VAR__ACTION__DETACH();

    @DefaultStringValue("$action activate")
    String VAR__ACTION__ACTIVATE();

    @DefaultStringValue("$action deactivate")
    String VAR__ACTION__DEACTIVATE();

    @DefaultStringValue("$action reconstruct master")
    String VAR__ACTION__RECONSTRUCT_MASTER();

    @DefaultStringValue("$action recover Data Center")
    String VAR__ACTION__RECOVER_POOL();

    @DefaultStringValue("$action destroy")
    String VAR__ACTION__DESTROY_DOMAIN();

    @DefaultStringValue("$action hot plug")
    String VAR__ACTION__HOT_PLUG();

    @DefaultStringValue("$action hot unplug")
    String VAR__ACTION__HOT_UNPLUG();

    @DefaultStringValue("$action hot set cpus")
    String VAR__ACTION__HOT_SET_CPUS();

    @DefaultStringValue("$action log on")
    String VAR__ACTION__LOGON();

    @DefaultStringValue("$action log off")
    String VAR__ACTION__LOGOFF();

    @DefaultStringValue("$action rebalance")
    String VAR__ACTION__REBALANCE_START();

    @DefaultStringValue("$action start removing")
    String VAR__ACTION__REMOVE_BRICKS_START();

    @DefaultStringValue("$action stop rebalance")
    String VAR__ACTION__REBALANCE_STOP();

    @DefaultStringValue("$action stop removing")
    String VAR__ACTION__REMOVE_BRICKS_STOP();

    @DefaultStringValue("$action commit removing")
    String VAR__ACTION__REMOVE_BRICKS_COMMIT();

    @DefaultStringValue("$action start profiling")
    String VAR__ACTION__START_PROFILE();

    @DefaultStringValue("$action stop profiling")
    String VAR__ACTION__STOP_PROFILE();

    @DefaultStringValue("$action scan alignment")
    String VAR__ACTION__SCAN_ALIGNMENT();

    @DefaultStringValue("$action force select")
    String VAR__ACTION__FORCE_SELECT();

    @DefaultStringValue("$action assign")
    String VAR__ACTION__ASSIGN();

    @DefaultStringValue("$action refresh")
    String VAR__ACTION__REFRESH();

    @DefaultStringValue("$action enable")
    String VAR__ACTION__ENABLE();

    @DefaultStringValue("$action disable")
    String VAR__ACTION__DISABLE();

    @DefaultStringValue("$action update version for")
    String VAR__ACTION__UPDATE_VM_VERSION();

    @DefaultStringValue("$hostStatus Up")
    String VAR__HOST_STATUS__UP();

    @DefaultStringValue("$hostStatus Up, Maintenance or Non operational")
    String VAR__HOST_STATUS__UP_MAINTENANCE_OR_NON_OPERATIONAL();

    @DefaultStringValue("$vmStatus Up")
    String VAR__VM_STATUS__UP();

    @DefaultStringValue("$vmStatus Unassigned")
    String VAR__VM_STATUS__UNASSIGNED();

    @DefaultStringValue("$vmStatus Down")
    String VAR__VM_STATUS__DOWN();

    @DefaultStringValue("$vmStatus Powering Up")
    String VAR__VM_STATUS__POWERING_UP();

    @DefaultStringValue("$vmStatus Paused")
    String VAR__VM_STATUS__PAUSED();

    @DefaultStringValue("$vmStatus Migrating")
    String VAR__VM_STATUS__MIGRATING();

    @DefaultStringValue("$vmStatus Unknown")
    String VAR__VM_STATUS__UNKNOWN();

    @DefaultStringValue("$vmStatus Not Responding")
    String VAR__VM_STATUS__NOT_RESPONDING();

    @DefaultStringValue("$vmStatus Wait for Launch")
    String VAR__VM_STATUS__WAIT_FOR_LAUNCH();

    @DefaultStringValue("$vmStatus Reboot in Progress")
    String VAR__VM_STATUS__REBOOT_IN_PROGRESS();

    @DefaultStringValue("$vmStatus Saving State")
    String VAR__VM_STATUS__SAVING_STATE();

    @DefaultStringValue("$vmStatus Restoring State")
    String VAR__VM_STATUS__RESTORING_STATE();

    @DefaultStringValue("$vmStatus Suspended")
    String VAR__VM_STATUS__SUSPENDED();

    @DefaultStringValue("$vmStatus Image Locked")
    String VAR__VM_STATUS__IMAGE_LOCKED();

    @DefaultStringValue("$vmStatus Powering Down")
    String VAR__VM_STATUS__POWERING_DOWN();

    @DefaultStringValue("$vmStatus Image Illegal")
    String VAR__VM_STATUS__IMAGE_ILLEGAL();

    @DefaultStringValue("$vmStatus Preparing for Hibernate")
    String VAR__VM_STATUS__PREPARING_FOR_HIBERNATE();

    @DefaultStringValue("$type Gluster Volume")
    String VAR__TYPE__GLUSTER_VOLUME();

    @DefaultStringValue("$type Gluster Volume Option")
    String VAR__TYPE__GLUSTER_VOLUME_OPTION();

    @DefaultStringValue("$type Gluster Volume Rebalance")
    String VAR__TYPE__GLUSTER_VOLUME_REBALANCE();

    @DefaultStringValue("$type Brick(s)")
    String VAR__TYPE__GLUSTER_BRICK();

    @DefaultStringValue("$type Gluster Server")
    String VAR__TYPE__GLUSTER_SERVER();

    @DefaultStringValue("$type Gluster Hook")
    String VAR__TYPE__GLUSTER_HOOK();

    @DefaultStringValue("$type Service")
    String VAR__TYPE__GLUSTER_SERVICE();

    @DefaultStringValue("Cannot ${action} ${type}. The chosen disk drive letter is already in use, please select a free one.")
    String ACTION_TYPE_FAILED_DISK_LETTER_ALREADY_IN_USE();

    @DefaultStringValue("Cannot ${action} ${type}. The VM has exceeded maximum number of available disks.")
    String ACTION_TYPE_FAILED_DISK_LIMITATION_EXCEEDED();

    @DefaultStringValue("Cannot ${action} ${type}. The disk is already attached to VM.")
    String ACTION_TYPE_FAILED_DISK_ALREADY_ATTACHED();

    @DefaultStringValue("Cannot ${action} ${type}. Disk is Illegal. Illegal disks can only be deleted.")
    String ACTION_TYPE_FAILED_ILLEGAL_DISK_OPERATION();

    @DefaultStringValue("Cannot ${action} ${type}. Architecture does not match the expected value.")
    String ACTION_TYPE_FAILED_ILLEGAL_ARCHITECTURE_TYPE_INCOMPATIBLE();

    @DefaultStringValue("Cannot ${action} ${type}. The disk is already detached from VM.")
    String ACTION_TYPE_FAILED_DISK_ALREADY_DETACHED();

    @DefaultStringValue("Cannot ${action} ${type}. The disk is not shareable and is already attached to a VM.")
    String ACTION_TYPE_FAILED_NOT_SHAREABLE_DISK_ALREADY_ATTACHED();

    @DefaultStringValue("Cannot ${action} ${type}. Maximum value for concurrently running VMs exceeded.")
    String ACTION_TYPE_FAILED_VM_MAX_RESOURCE_EXEEDED();

    @DefaultStringValue("Cannot ${action} ${type}. One or more provided storage domains are in maintenance/non-operational status.")
    String ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS();

    @DefaultStringValue("Cannot ${action} ${type}. Provided wrong storage domain, which is not related to disk.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_IS_WRONG();

    @DefaultStringValue("Cannot ${action} ${type}. The chosen CPU is not supported.")
    String ACTION_TYPE_FAILED_CPU_NOT_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. Maximum PCI devices exceeded.")
    String ACTION_TYPE_FAILED_EXCEEDED_MAX_PCI_SLOTS();

    @DefaultStringValue("Cannot ${action} ${type}. Maximum IDE devices exceeded.")
    String ACTION_TYPE_FAILED_EXCEEDED_MAX_IDE_SLOTS();

    @DefaultStringValue("Cannot ${action} ${type}. Maximum VirtIO SCSI devices exceeded.")
    String ACTION_TYPE_FAILED_EXCEEDED_MAX_VIRTIO_SCSI_DISKS();

    @DefaultStringValue("Cannot ${action} ${type}. Maximum sPAPR VSCSI devices exceeded.")
    String ACTION_TYPE_FAILED_EXCEEDED_MAX_SPAPR_VSCSI_DISKS();

    @DefaultStringValue("User is already attached to VM")
    String USER_CANNOT_ATTACH_TO_VM_ALREADY_ATTACHED();

    @DefaultStringValue("The user is not attached to this VM.")
    String USER_CANNOT_ATTACH_TO_VM_NOT_ATTACHED();

    @DefaultStringValue("Login failed. Please verify your login information or contact the system administrator.")
    String USER_FAILED_TO_AUTHENTICATE();

    @DefaultStringValue("Login failed. One or more servers that are needed for completion of the login process is not available.")
    String USER_FAILED_TO_AUTHENTICATE_SERVER_IS_NOT_AVAILABLE();

    @DefaultStringValue("Login failed. A timeout has occurred to one or more of the servers that participate in the login process.")
    String USER_FAILED_TO_AUTHENTICATE_TIMED_OUT();

    @DefaultStringValue("Login failed. Client not found in kerberos database. Please verify your login information or contact the system administrator.")
    String USER_FAILED_TO_AUTHENTICATE_KERBEROS_ERROR();

    @DefaultStringValue("Login failed (Authentication Failed).\n- Please verify that the correct authentication method is used in your system.")
    String USER_FAILED_TO_AUTHENTICATION_WRONG_AUTHENTICATION_METHOD();

    @DefaultStringValue("Cannot ${action} ${type}. Cluster ID is not valid.")
    String VMT_CLUSTER_IS_NOT_VALID();

    @DefaultStringValue("Cannot Login. User Account is Disabled or Locked, Please contact your system administrator.")
    String USER_ACCOUNT_DISABLED();

    @DefaultStringValue("Cannot Login. User Account has expired, Please contact your system administrator.")
    String USER_ACCOUNT_EXPIRED();

    @DefaultStringValue("Permission denied. Engine Administrator permission is required.")
    String USER_PERMISSION_DENIED();

    @DefaultStringValue("Cannot Login. Session timeout.")
    String USER_CANNOT_LOGIN_SESSION_MISSING();

    @DefaultStringValue("Cannot remove internal admin user or its permissions.")
    String USER_CANNOT_REMOVE_ADMIN_USER();

    @DefaultStringValue("User must exist in Database.")
    String USER_MUST_EXIST_IN_DB();

    @DefaultStringValue("User must exist in Directory.")
    String USER_MUST_EXIST_IN_DIRECTORY();

    @DefaultStringValue("User is already logged in.")
    String USER_IS_ALREADY_LOGGED_IN();

    @DefaultStringValue("User is not logged in.")
    String USER_IS_NOT_LOGGED_IN();

    @DefaultStringValue("User does not have a valid e-mail address.")
    String USER_DOES_NOT_HAVE_A_VALID_EMAIL();

    @DefaultStringValue("Native USB support is only available on cluster level 3.1 or higher.")
    String USB_NATIVE_SUPPORT_ONLY_AVAILABLE_ON_CLUSTER_LEVEL();

    @DefaultStringValue("Legacy USB support is not available on Linux VMs.")
    String USB_LEGACY_NOT_SUPPORTED_ON_LINUX_VMS();

    @DefaultStringValue("VM-Pool must be based on  non-blank Template.")
    String VM_POOL_CANNOT_CREATE_FROM_BLANK_TEMPLATE();

    @DefaultStringValue("Cannot edit VM-Pool properties - VM-Pool not found.")
    String VM_POOL_CANNOT_UPDATE_POOL_NOT_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. There are already asynchronous running tasks, please retry later.")
    String ACTION_TYPE_FAILED_VM_TASKS_ARE_ALREADY_RUNNING();

    @DefaultStringValue("Cannot suspend VM. VM has asynchronous running tasks, please retry later.")
    String VM_CANNOT_SUSPENDE_HAS_RUNNING_TASKS();

    @DefaultStringValue("Cannot suspend VM, VM is stateless.")
    String VM_CANNOT_SUSPEND_STATELESS_VM();

    @DefaultStringValue("Cannot suspend VM that belongs to a  VM-Pool.")
    String VM_CANNOT_SUSPEND_VM_FROM_POOL();

    @DefaultStringValue("User is not authorized to perform this action.")
    String USER_NOT_AUTHORIZED_TO_PERFORM_ACTION();

    @DefaultStringValue("One or more Permissions is still associated with Role.\n- Please remove all Permissions first.")
    String ERROR_CANNOT_REMOVE_ROLE_ATTACHED_TO_PERMISSION();

    @DefaultStringValue("Cannot remove Role, invalid Role id.")
    String ERROR_CANNOT_REMOVE_ROLE_INVALID_ROLE_ID();

    @DefaultStringValue("Cannot ${action} Role. Changing Role ID is not allowed.")
    String ERROR_CANNOT_UPDATE_ROLE_ID();

    @DefaultStringValue("Cannot ${action} Role. Changing Role type is not allowed.")
    String ERROR_CANNOT_UPDATE_ROLE_TYPE();

    @DefaultStringValue("Cannot approve RHEV Hypervisor Host.\n-Host must be in \"Pending Approval\" or \"Install Failed\" status in order to be approved.")
    String VDS_APPROVE_VDS_IN_WRONG_STATUS();

    @DefaultStringValue("Cannot update Host properties. Mismatch between Cluster CPU and Host CPU.")
    String VDS_NEW_CLUSTER_ILLEGAL();

    @DefaultStringValue("Cannot ${action} ${type}. Cluster ID is not valid.")
    String VDS_CLUSTER_IS_NOT_VALID();

    @DefaultStringValue("Cannot change Cluster CPU type when there are Hosts attached to this Cluster.")
    String VDS_GROUP_CANNOT_UPDATE_CPU_ILLEGAL();

    @DefaultStringValue("Cannot change Cluster CPU architecture when there are Hosts or VMs attached to this Cluster.")
    String VDS_GROUP_CANNOT_UPDATE_CPU_ARCHITECTURE_ILLEGAL();

    @DefaultStringValue("Cannot change Cluster CPU to higher CPU type when there are active Hosts with lower CPU type.\n-Please move Hosts with lower CPU to maintenance first.")
    String VDS_GROUP_CANNOT_UPDATE_CPU_WITH_LOWER_HOSTS();

    @DefaultStringValue("Cannot change Cluster Compatibility Version to higher version when there are active Hosts with lower version.\n-Please move Hosts with lower version to maintenance first.")
    String VDS_GROUP_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_LOWER_HOSTS();

    @DefaultStringValue("Cannot change Cluster.Trying to connect Cluster to Data Center with Hosts that are up.")
    String VDS_GROUP_CANNOT_UPDATE_VDS_UP();

    @DefaultStringValue("Cannot add Cluster with Compatibility Version that is lower than the Data Center Compatibility Version.\n-Please upgrade your Cluster to a later Compatibility version first.")
    String VDS_GROUP_CANNOT_ADD_COMPATIBILITY_VERSION_WITH_LOWER_STORAGE_POOL();

    @DefaultStringValue("Cannot ${action} Cluster. Cluster name is already in use.")
    String VDS_GROUP_CANNOT_DO_ACTION_NAME_IN_USE();

    @DefaultStringValue("Attestation server should be configured correctly.")
    String VDS_GROUP_CANNOT_SET_TRUSTED_ATTESTATION_SERVER_NOT_CONFIGURED();

    @DefaultStringValue("Cannot Cannot add Cluster. CPU type must be specified.")
    String VDS_GROUP_CPU_TYPE_CANNOT_BE_NULL();

    @DefaultStringValue("Cannot ${action} ${type}. Network name already exists.")
    String NETWORK_NAME_ALREADY_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. The ${type} name is already in use, please choose a unique name and try again.")
    String ACTION_TYPE_FAILED_NAME_ALREADY_USED();

    @DefaultStringValue("Cannot ${action} ${type}. The URL is not valid, please enter a valid URL and try again.")
    String ACTION_TYPE_FAILED_URL_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. Storage connection doesn't exist.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. Storage connection is not attached to the specified domain.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_FOR_DOMAIN_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. No active data Storage Domain with enough storage was found in the Data Center.")
    String ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. Action is supported only for iSCSI storage domains.")
    String ACTION_TYPE_FAILED_ACTION_IS_SUPPORTED_ONLY_FOR_ISCSI_DOMAINS();

    @DefaultStringValue("Cannot ${action} ${type}. Storage connection id is empty.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_ID_EMPTY();

    @DefaultStringValue("Cannot ${action} ${type}. Storage connection already exists.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. Storage connection is already attached to the specified domain.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_FOR_DOMAIN_ALREADY_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. Storage connection parameters can be edited only for NFS, Posix, local or iSCSI data domains.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_STORAGE_TYPE();

    @DefaultStringValue("Cannot ${action} ${type}. The data domains ${domainNames} should be in maintenance or unattached.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_DOMAINS_STATUS();

    @DefaultStringValue("Cannot ${action} ${type}. Storage connection and domain type don't match.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_NOT_SAME_STORAGE_TYPE();

    @DefaultStringValue("Cannot ${action} ${type}. VMs ${vmNames} should be down.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_RUNNING_VMS();

    @DefaultStringValue("Cannot ${action} ${type}. The data domains ${domainNames} should be in maintenance or unattached, and VMs ${vmNames} should be down.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_RUNNING_VMS_AND_DOMAINS_STATUS();

    @DefaultStringValue("Cannot ${action} ${type}. Storage connection parameters are used by the following storage domains : ${domainNames}.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS();

    @DefaultStringValue("Cannot ${action} ${type}. Storage connection parameters are used by the following storage domains : ${domainNames} and disks: ${diskNames}.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS_AND_DISKS();

    @DefaultStringValue("Cannot ${action} ${type}. Storage connection parameters are used by the following disks : ${diskNames}.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_DISKS();

    @DefaultStringValue("Cannot ${action} ${type}. Connection parameters are invalid for this storage type.")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTION_WRONG_PARAMETERS_FOR_STORAGE_TYPE();

    @DefaultStringValue("Cannot ${action} ${type}. Storage Domain doesn't exist.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. Cannot change Storage Domain type.")
    String ACTION_TYPE_FAILED_CANNOT_CHANGE_STORAGE_DOMAIN_TYPE();

    @DefaultStringValue("Cannot ${action} ${type}. The relevant Storage Domain is inaccessible.\n-Please handle Storage Domain issues and retry the operation.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL();

    @DefaultStringValue("Cannot ${action} ${type}. The relevant Storage Domain's status is ${status}.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2();

    @DefaultStringValue("Cannot ${action} ${type}.\nThe ${action} action can be performed on a Data Center that has only one Storage Domain in Active/Unknown state.")
    String STORAGE_POOL_REINITIALIZE_WITH_MORE_THAN_ONE_DATA_DOMAIN();

    @DefaultStringValue(" Data Center doesn't exist.")
    String ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The selected Storage Domain is not part of the Data Center.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_IN_STORAGE_POOL();

    @DefaultStringValue("Cannot ${action} ${type}. The following LUNs are already part of existing storage domains: ${lunIds}.")
    String ACTION_TYPE_FAILED_LUNS_ALREADY_PART_OF_STORAGE_DOMAINS();

    @DefaultStringValue("Cannot ${action} ${type}. The following LUNs are already used by DirectLUN disks: ${lunIds}.")
    String ACTION_TYPE_FAILED_LUNS_ALREADY_USED_BY_DISKS();

    @DefaultStringValue("Cannot remove Data Center while there are more than one Storage Domains attached.")
    String ERROR_CANNOT_REMOVE_STORAGE_POOL_WITH_NONMASTER_DOMAINS();

    @DefaultStringValue("Cannot manage Storage Domain. The domain is defined externally (e.g. through a provider).")
    String ERROR_CANNOT_MANAGE_STORAGE_DOMAIN();

    @DefaultStringValue("Cannot ${action} ${type} while there are Hosts that are not in Maintenance mode.")
    String ERROR_CANNOT_FORCE_REMOVE_STORAGE_POOL_WITH_VDS_NOT_IN_MAINTENANCE();

    @DefaultStringValue("Cannot ${action} ${type}. Unknown Data Center status.")
    String ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL();

    @DefaultStringValue("Data Center version does not support mixed storage types.")
    String ACTION_TYPE_FAILED_MIXED_STORAGE_TYPES_NOT_ALLOWED();

    @DefaultStringValue("Bond name already exists.")
    String NETWORK_BOND_NAME_EXISTS();

    @DefaultStringValue("Bond name doesn't exist.")
    String NETWORK_BOND_NOT_EXISTS();

    @DefaultStringValue("Cannot check connectivity on non management network.")
    String NETWORK_CHECK_CONNECTIVITY();

    @DefaultStringValue("Invalid parameters.\n-Please check that bond name is formatted as <bondYYY>.\n-Bond consist of at least two network interfaces(NICs).")
    String NETWORK_BOND_PARAMETERS_INVALID();

    @DefaultStringValue("Network '${NetworkName}' is mandatory, it cannot be modified.")
    String NETWORK_DEFAULT_UPDATE_NAME_INVALID();

    @DefaultStringValue("Network Interface already belongs to the bond.")
    String NETWORK_INTERFACE_ALREADY_IN_BOND();

    @DefaultStringValue("Bond name must be formatted as <bondYYY>.")
    String NETWORK_INVALID_BOND_NAME();

    @DefaultStringValue("Network name doesn't exist.")
    String NETWORK_INTERFACE_NOT_EXISTS();

    @DefaultStringValue("Cannot attach more than one ISO Storage Domain to the same Data Center. If you want to use a newly created Domain, detach the existing attached Domain and attach the new one.")
    String ERROR_CANNOT_ATTACH_MORE_THAN_ONE_ISO_DOMAIN();

    @DefaultStringValue("Cannot attach more than one Import/Export Storage Domain to the same Data Center. If you want to use a newly created Domain, detach the existing attached Domain and attach the new one.")
    String ERROR_CANNOT_ATTACH_MORE_THAN_ONE_EXPORT_DOMAIN();

    @DefaultStringValue("Cannot attach storage to Data Center. Storage type doesn't match the Data Center type.")
    String ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_STORAGE_TYPE_NOT_MATCH();

    @DefaultStringValue("Cannot change Data Center type when Storage Domains are still attached to it.\n-Please detach all attached Storage Domains and retry.")
    String ERROR_CANNOT_CHANGE_STORAGE_POOL_TYPE_WITH_DOMAINS();

    @DefaultStringValue("The Network Interface is already attached to a Logical Network.")
    String NETWORK_INTERFACE_ALREADY_HAVE_NETWORK();

    @DefaultStringValue("Logical Network is already attached to a Network Interface.")
    String NETWORK_ALREAY_ATTACH_TO_INTERFACE();

    @DefaultStringValue("The specified Logical Network doesn't exist.")
    String NETWORK_NOT_EXISTS();

    @DefaultStringValue("The specified Network QoS doesn't exist.")
    String ACTION_TYPE_FAILED_NETWORK_QOS_NOT_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. The specified VM network interface profile doesn't exist.")
    String ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. The VM network interface profile's name is already used by an existing profile for the same network.\n-Please choose a different name.")
    String ACTION_TYPE_FAILED_VNIC_PROFILE_NAME_IN_USE();

    @DefaultStringValue("Cannot ${action} ${type}. Several ${entities} (${ENTITIES_USING_VNIC_PROFILE_COUNTER}) are using this VM network interface profile:\n${ENTITIES_USING_VNIC_PROFILE}\n - Please remove it from all ${entities} that are using it and try again.")
    String ACTION_TYPE_FAILED_VNIC_PROFILE_IN_USE();

    @DefaultStringValue("Cannot ${action} ${type}. VM network interface profile's network cannot be changed.")
    String ACTION_TYPE_FAILED_CANNOT_CHANGE_VNIC_PROFILE_NETWORK();

    @DefaultStringValue("Cannot ${action} ${type}. VM network interface profiles cannot be added to a non-VM network. Please make sure the network is a VM network.")
    String ACTION_TYPE_FAILED_CANNOT_ADD_VNIC_PROFILE_TO_NON_VM_NETWORK();

    @DefaultStringValue("Cannot ${action} ${type}. There is no VM network interface profile for the network the user can use.\n- Please use a VM network interface profile instead of a network name.")
    String ACTION_TYPE_FAILED_CANNOT_FIND_VNIC_PROFILE_FOR_NETWORK();

    @DefaultStringValue("The specified external network cannot be configured on the host's interface.")
    String EXTERNAL_NETWORK_CANNOT_BE_PROVISIONED();

    @DefaultStringValue("Network label must be formed only from: English letters, numbers, hyphen or underscore.")
    String NETWORK_LABEL_FORMAT_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. The specified network is already labeled.")
    String ACTION_TYPE_FAILED_NETWORK_ALREADY_LABELED();

    @DefaultStringValue("Cannot ${action} ${type}. The following networks cannot be removed from the network interface since they are managed by the label: ${ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC_LIST}. Please remove the label from the network interface in order to remove the network.")
    String ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC();

    @DefaultStringValue("Cannot ${action} ${type}. The display network ${DisplayNetwork} must have a DHCP or Static boot protocol when configured on a host.")
    String ACTION_TYPE_FAILED_DISPLAY_NETWORK_HAS_NO_BOOT_PROTOCOL();

    @DefaultStringValue("Cannot ${action} ${type}. The following networks are already attached to a different interface: ${AssignedNetworks}. Please remove the networks in order to label the interface.")
    String LABELED_NETWORK_ATTACHED_TO_WRONG_INTERFACE();

    @DefaultStringValue("Cannot ${action} ${type}. The label is already defined on other interface ${LabeledNic} on the host.")
    String OTHER_INTERFACE_ALREADY_LABELED();

    @DefaultStringValue("Cannot recover Data Center with active Data Storage Domain in Data Center.")
    String ERROR_CANNOT_RECOVERY_STORAGE_POOL_THERE_IS_ACTIVE_DATA_DOMAINS();

    @DefaultStringValue("Cannot remove the master Storage Domain from the Data Center without another active Storage Domain to take its place.\n-Either activate another Storage Domain in the Data Center, or remove the Data Center.")
    String ERROR_CANNOT_DETACH_LAST_STORAGE_DOMAIN();

    @DefaultStringValue("Cannot destroy the master Storage Domain from the Data Center without another active Storage Domain to take its place.\n-Either activate another Storage Domain in the Data Center, or remove the Data Center.\n-If you have problems with the master Data Domain, consider following the recovery process described in the documentation, or contact your system administrator.")
    String ERROR_CANNOT_DESTROY_LAST_STORAGE_DOMAIN();

    @DefaultStringValue("In order to complete the operation a new master Storage Domain needs to be elected, which requires at least one active Host in the Data Center.\n-Please make sure one of the Hosts is active in the Data Center first.")
    String ERROR_CANNOT_DESTROY_LAST_STORAGE_DOMAIN_HOST_NOT_ACTIVE();

    @DefaultStringValue("VLAN ID must be a number between 0 and 4094.")
    String NETWORK_VLAN_OUT_OF_RANGE();

    @DefaultStringValue("Cannot attach Storage Domain.\n-Please attach Data Domain to the Data Center first.")
    String ERROR_CANNOT_ADD_STORAGE_POOL_WITHOUT_DATA_DOMAIN();

    @DefaultStringValue("Cannot remove Data Center which contains active/locked Storage Domains.\n-Please deactivate all domains and wait for tasks to finish before removing the Data Center.")
    String ERROR_CANNOT_REMOVE_POOL_WITH_ACTIVE_DOMAINS();

    @DefaultStringValue("Storage Domain doesn't exist.")
    String STORAGE_DOMAIN_DOES_NOT_EXIST();

    @DefaultStringValue("Cannot change Data Center association when editing a Cluster.")
    String VDS_GROUP_CANNOT_CHANGE_STORAGE_POOL();

    @DefaultStringValue("Cannot ${action} ${type}. The name of the logical network '${NetworkName}' is already used by an existing logical network in the same data-center.\n-Please choose a different name.")
    String ACTION_TYPE_FAILED_NETWORK_NAME_IN_USE();

    @DefaultStringValue("Cannot ${action} ${type}. Several ${entities} (${ENTITIES_USING_NETWORK_COUNTER}) are using this logical network:\n${ENTITIES_USING_NETWORK}\n - Please remove it from all ${entities} that are using it and try again.")
    String ACTION_TYPE_FAILED_NETWORK_IN_USE();

    @DefaultStringValue("Volume Group (VGs) and Logical Volumes (LVs) are not specified.")
    String ERROR_CANNOT_CREATE_STORAGE_DOMAIN_WITHOUT_VG_LV();

    @DefaultStringValue("Cannot ${action} ${type}. VM Template ID must be empty.")
    String NETWORK_INTERFACE_TEMPLATE_CANNOT_BE_SET();

    @DefaultStringValue("Cannot ${action} ${type}. VM ID must be empty.")
    String NETWORK_INTERFACE_VM_CANNOT_BE_SET();

    @DefaultStringValue("Network interface is already in use.")
    String NETWORK_INTERFACE_NAME_ALREAY_IN_USE();

    @DefaultStringValue("The Notification method is unsupported.")
    String EN_UNKNOWN_NOTIFICATION_METHOD();

    @DefaultStringValue("The notification event ${eventName} is unsupported.")
    String EN_UNSUPPORTED_NOTIFICATION_EVENT();

    @DefaultStringValue("Alert Notification.")
    String EN_EVENT_UP_SUBJECT_TEXT();

    @DefaultStringValue("Issue Solved Notification.")
    String EN_EVENT_DOWN_SUBJECT_TEXT();

    @DefaultStringValue("User is already subscribed to this event with the same Notification method. ")
    String EN_ALREADY_SUBSCRIBED();

    @DefaultStringValue("Cannot ${action} ${type}.User is not subscribed to this event with the given Notification method.")
    String EN_NOT_SUBSCRIBED();

    @DefaultStringValue("Cannot ${action} ${type}. Storage Domain type not specified.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL();

    @DefaultStringValue("Cannot ${action} ${type}. Storage Domain format ${storageFormat} is illegal.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL();

    @DefaultStringValue("Cannot ${action} ${type}. Storage format ${storageFormat} is not supported on the selected host version.")
    String ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST();

    @DefaultStringValue("Cannot extend Storage Domain. Extend operation is supported only on Data Storage Domain.")
    String ERROR_CANNOT_EXTEND_NON_DATA_DOMAIN();

    @DefaultStringValue("Cannot extend Storage Domain. Storage device ${lun} is unreachable from ${hostName}.")
    String ERROR_CANNOT_EXTEND_CONNECTION_FAILED();

    @DefaultStringValue("Cannot ${action} ${type}. Only Storage Domain name, description and comment are updateable.")
    String ERROR_CANNOT_CHANGE_STORAGE_DOMAIN_FIELDS();

    @DefaultStringValue("Cannot update Data Center compatibility version to a value that is greater than its Cluster's version. The following clusters should be upgraded ${ClustersList}.")
    String ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS();

    @DefaultStringValue("Cannot import Storage Domain. Internal Error: The connection data is illegal.")
    String ERROR_CANNOT_ADD_EXISTING_STORAGE_DOMAIN_CONNECTION_DATA_ILLEGAL();

    @DefaultStringValue("Cannot import SAN Export Storage Domain as it is no longer supported.")
    String ERROR_CANNOT_ADD_DEPRECATED_EXISTING_SAN_EXPORT_STORAGE_DOMAIN();

    @DefaultStringValue("Cannot get Storage Domains list.")
    String ERROR_GET_STORAGE_DOMAIN_LIST();

    @DefaultStringValue("MAC Address is already in use.")
    String NETWORK_MAC_ADDRESS_IN_USE();

    @DefaultStringValue("Cannot ${action} ${type}. There is at least one running VM that uses this Network.")
    String NETWORK_INTERFACE_IN_USE_BY_VM();

    @DefaultStringValue("Cannot ${action} ${type}. Network is being used by at least one Cluster.")
    String NETWORK_CLUSTER_NETWORK_IN_USE();

    @DefaultStringValue("Cannot deactivate a Master Storage Domain while there are ISO/Export active domains in the Data Center.\n-Please deactivate all ISO/Export domains first.")
    String ERROR_CANNOT_DEACTIVATE_MASTER_WITH_NON_DATA_DOMAINS();

    @DefaultStringValue("Cannot deactivate Data Domain while there are running tasks in the Data Center.\n-Please wait until tasks will finish and try again.")
    String ERROR_CANNOT_DEACTIVATE_DOMAIN_WITH_TASKS();

    @DefaultStringValue("Cannot deactivate Master Data Domain while there are running tasks on its Data Center.\n-Please wait until tasks will finish and try again.")
    String ERROR_CANNOT_DEACTIVATE_MASTER_DOMAIN_WITH_TASKS_ON_POOL();

    @DefaultStringValue("Cannot ${action} ${type}. The system must have at least one Super User Role defined.")
    String ERROR_CANNOT_REMOVE_LAST_SUPER_USER_ROLE();

    @DefaultStringValue("Cannot remove VM that its state is not down.\n-Please stop the VM first.")
    String VM_CANNOT_REMOVE_VM_WHEN_STATUS_IS_NOT_DOWN();

    @DefaultStringValue("Cannot ${action} ${type} with detaching disks, snapshots exist.")
    String VM_CANNOT_REMOVE_WITH_DETACH_DISKS_SNAPSHOTS_EXIST();

    @DefaultStringValue("Cannot ${action} ${type} with detaching disks, VM is based from template.")
    String VM_CANNOT_REMOVE_WITH_DETACH_DISKS_BASED_ON_TEMPLATE();

    @DefaultStringValue("Failed to ${action} ${type} due to an error on the Data Center master Storage Domain.\n-Please activate the master Storage Domain first.")
    String ACTION_TYPE_FAILED_MASTER_STORAGE_DOMAIN_NOT_ACTIVE();

    @DefaultStringValue("The specified Tag does not exist.")
    String TAGS_SPECIFY_TAG_IS_NOT_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. The Network is in use by a Template.\nNetwork definition cannot be detached unless all references to it are cleared.")
    String NETWORK_CANNOT_REMOVE_NETWORK_IN_USE_BY_TEMPLATE();

    @DefaultStringValue("Cannot ${action} a Network Interface when VM is not Down, Up or Image-Locked.")
    String NETWORK_CANNOT_ADD_INTERFACE_WHEN_VM_STATUS_NOT_UP_DOWN_LOCKED();

    @DefaultStringValue("The specified VLAN ID (${vlanId}) is already in use.")
    String NETWORK_VLAN_IN_USE();

    @DefaultStringValue("Cluster has Networks that doesn't exist in the Data Center.\n-Please remove those Networks first.")
    String NETWORK_CLUSTER_HAVE_NOT_EXISTING_DATA_CENTER_NETWORK();

    @DefaultStringValue("The specified Logical Network doesn't exist in the current Cluster.")
    String NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. The following Networks' definitions on the Network Interfaces are different than those on the Logical Networks. Please synchronize the Network Interfaces before editing the networks: ${NETWORKS_NOT_IN_SYNC}.")
    String NETWORKS_NOT_IN_SYNC();

    @DefaultStringValue("Cannot ${action} ${type}. The following Network Interfaces were specified more than once: ${NETWORK_INTERFACES_ALREADY_SPECIFIED_LIST}.")
    String NETWORK_INTERFACES_ALREADY_SPECIFIED();

    @DefaultStringValue("Cannot ${action} ${type}. The following external networks cannot be configured on host via 'Setup Networks': ${ACTION_TYPE_FAILED_EXTERNAL_NETWORKS_CANNOT_BE_PROVISIONED_LIST}")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORKS_CANNOT_BE_PROVISIONED();

    @DefaultStringValue("Cannot ${action} ${type}. The following Logical Networks are attached to more than one Network Interface: ${NETWORKS_ALREADY_ATTACHED_TO_IFACES_LIST}.")
    String NETWORKS_ALREADY_ATTACHED_TO_IFACES();

    @DefaultStringValue("Cannot ${action} ${type}. The following Network Interfaces do not exist on the Host: ${NETWORK_INTERFACES_DONT_EXIST_LIST}.")
    String NETWORK_INTERFACES_DONT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The following Logical Networks do not exist in the Host's Cluster: ${NETWORKS_DONT_EXIST_IN_CLUSTER_LIST}.")
    String NETWORKS_DONT_EXIST_IN_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. The following Network Interfaces can have only a single VM Logical Network, or at most one non-VM Logical Network and/or several VLAN Logical Networks: ${NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK_LIST}.")
    String NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK();

    @DefaultStringValue("Cannot ${action} ${type}. The following bonds consist of less than two Network Interfaces: ${NETWORK_BONDS_INVALID_SLAVE_COUNT_LIST}.")
    String NETWORK_BONDS_INVALID_SLAVE_COUNT();

    @DefaultStringValue("Cannot ${action} ${type}. The following VMs are actively using the Logical Network: ${NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS_LIST}. Please stop the VMs and try again.")
    String NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS();

    @DefaultStringValue("Cannot ${action} ${type}. STP can only be enabled on VM Networks.")
    String NON_VM_NETWORK_CANNOT_SUPPORT_STP();

    @DefaultStringValue("Cannot ${action} ${type}. The following Logical Networks do not have the same MTU value: ${NETWORK_MTU_DIFFERENCES_LIST}.")
    String NETWORK_MTU_DIFFERENCES();

    @DefaultStringValue("Cannot ${action} ${type}. Overriding MTU is not supported for this Data Center's compatibility version.")
    String NETWORK_MTU_OVERRIDE_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Migration network is not supported for this cluster version.")
    String ACTION_TYPE_FAILED_MIGRATION_NETWORK_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. The provider does not exist in the system.")
    String ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The provider type should be 'OpenStack Networking'.")
    String ACTION_TYPE_FAILED_PROVIDER_TYPE_MISMATCH();

    @DefaultStringValue("Cannot ${action} ${type}. The network provider's broker host address must be configured on the provider.")
    String ACTION_TYPE_FAILED_MISSING_NETWORK_MAPPINGS();

    @DefaultStringValue("Cannot ${action} ${type}. The provider's broker address must be configured on the provider.")
    String ACTION_TYPE_FAILED_MISSING_MESSAGING_BROKER_PROPERTIES();

    @DefaultStringValue("Cannot ${action} ${type}. Several external networks (${NETWORK_NAMES_COUNTER}) are being used by virtual machines and/or templates:\n${NETWORK_NAMES}\n - Please resolve the external networks usage first and try again.")
    String ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED();

    @DefaultStringValue("Cannot ${action} ${type}. The external network already exists as '${NetworkName}' in the data center.")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORK_ALREADY_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. An external network cannot be a non-VM network.")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORK_MUST_BE_VM_NETWORK();

    @DefaultStringValue("Cannot ${action} ${type}. External network details (except name and description) cannot be changed.")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORK_DETAILS_CANNOT_BE_EDITED();

    @DefaultStringValue("Cannot ${action} ${type}. External networks are not supported for this cluster's compatibility version.")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORK_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. External network cannot be used as a display network.")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_DISPLAY();

    @DefaultStringValue("Cannot ${action} ${type}. External network cannot be set as required in the cluster.")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_REQUIRED();

    @DefaultStringValue("Cannot ${action} ${type}. External network with vlan must be labeled.")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORK_WITH_VLAN_MUST_BE_LABELED();

    @DefaultStringValue("Cannot ${action} ${type}. External network cannot be used when port mirroring is set.")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_PORT_MIRRORED();

    @DefaultStringValue("Cannot ${action} ${type}. External network cannot be changed while the virtual machine is running")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_REWIRED();

    @DefaultStringValue("Cannot ${action} ${type}. External network cannot have MTU set.")
    String ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_HAVE_MTU();

    @DefaultStringValue("Cannot ${action} ${type}. The management network '${NetworkName}' must be required, please change the network to be required and try again.")
    String ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_REQUIRED();

    @DefaultStringValue("Cannot ${action} ${type}. The address of the network '${ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED_LIST}' cannot be modified without reinstalling the host, since this address was used to create the host's certification.")
    String ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED();

    @DefaultStringValue("Cannot preview Active VM snapshot.")
    String CANNOT_PREVIEW_ACTIVE_SNAPSHOT();

    @DefaultStringValue("Illegal configuration entry.\n-Please check configuration entry name.")
    String CONFIG_UNKNOWN_KEY();

    @DefaultStringValue("Operation canceled, recursive Tag hierarchy cannot be defined.")
    String TAGS_SPECIFIED_TAG_CANNOT_BE_THE_PARENT_OF_ITSELF();

    @DefaultStringValue("VM can be moved only to a Cluster in the same Data Center.")
    String VM_CANNOT_MOVE_TO_CLUSTER_IN_OTHER_STORAGE_POOL();

    @DefaultStringValue("Cannot ${action} ${type}. Cluster ID is not valid.")
    String VM_CLUSTER_IS_NOT_VALID();

    @DefaultStringValue("The Management Network ('${NetworkName}') is mandatory and cannot be removed.")
    String NETWORK_CANNOT_REMOVE_MANAGEMENT_NETWORK();

    @DefaultStringValue("The Network ('${NetworkName}') could not be removed since several iSCSI bonds (${IscsiBonds_COUNTER}) are using this network:\n ${IscsiBonds}.\nPlease remove the network first from those iSCSI bonds, and try again.")
    String NETWORK_CANNOT_REMOVE_ISCSI_BOND_NETWORK();

    @DefaultStringValue("Previous network name is required.")
    String NETWORK_OLD_NETWORK_NOT_SPECIFIED();

    @DefaultStringValue("Cannot ${action} ${type}. Active VMs were detected.\n-Please ensure all VMs associated with this Storage Domain are stopped and in the Down state first.")
    String ACTION_TYPE_FAILED_DETECTED_ACTIVE_VMS();

    @DefaultStringValue("The provided Host does not exist.")
    String ACTION_TYPE_FAILED_HOST_NOT_EXIST();

    @DefaultStringValue("The specified Tag name already exists.")
    String TAGS_SPECIFY_TAG_IS_IN_USE();

    @DefaultStringValue("The specified Logical Network does not exist in Cluster.")
    String NETWORK_NET_EXISTS_IN_CLUSTER();

    @DefaultStringValue("Actions list cannot be empty.")
    String ACTION_LIST_CANNOT_BE_EMPTY();

    @DefaultStringValue("Cannot ${action} ${type}. Bookmark ID is not valid.")
    String ACTION_TYPE_FAILED_BOOKMARK_INVALID_ID();

    @DefaultStringValue("Cannot ${action} ${type}. Operation can be performed only when Host status is ${hostStatus}.")
    String ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL();

    @DefaultStringValue("Cannot ${action} ${type}. VM can be migrated only between Hosts in the same Cluster.\n-Please select target Host in the same Cluster to run the VM.")
    String ACTION_TYPE_FAILED_MIGRATE_BETWEEN_TWO_CLUSTERS();

    @DefaultStringValue("Cannot get Host version when Host is in Non Responsive status.")
    String VDS_CANNOT_CHECK_VERSION_HOST_NON_RESPONSIVE();

    @DefaultStringValue("Due to intermittent connectivity to this Host, fence operations are not allowed at this time. The system is trying to reconnect, please try again in 30 seconds.")
    String ACTION_TYPE_FAILED_VDS_INTERMITENT_CONNECTIVITY();

    @DefaultStringValue("Cannot ${action} ${type}. Power Management is enabled for Host but no Agent type selected.")
    String ACTION_TYPE_FAILED_PM_ENABLED_WITHOUT_AGENT();

    @DefaultStringValue("Cannot ${action} ${type}. Power Management is enabled for Host but Agent credentials are missing.")
    String ACTION_TYPE_FAILED_PM_ENABLED_WITHOUT_AGENT_CREDENTIALS();

    @DefaultStringValue("Cannot ${action} ${type}. Selected Power Management Agent is not supported.")
    String ACTION_TYPE_FAILED_AGENT_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. The selected cluster doesn't support VM's architecture")
    String ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_VM_ARCHITECTURE_NOT_SUPPORTED_BY_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. The selected cluster doesn't support Template's architecture")
    String ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_TEMPLATE_ARCHITECTURE_NOT_SUPPORTED_BY_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. The selected VM has undefined architecture")
    String ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_VM_WITH_NOT_SUPPORTED_ARCHITECTURE();

    @DefaultStringValue("Cannot ${action} ${type}. The selected Template has undefined architecture")
    String ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_TEMPLATE_WITH_NOT_SUPPORTED_ARCHITECTURE();

    @DefaultStringValue("Cannot ${action} ${type}. Setting hosted engine maintenance mode is only supported on cluster version 3.4 and above.")
    String ACTION_TYPE_FAILED_VDS_HA_MAINT_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Hosted Engine is not configured on this host.")
    String ACTION_TYPE_FAILED_VDS_HA_NOT_CONFIGURED();

    @DefaultStringValue("Cannot ${action} ${type}. The cluster's compatibility version doesn't support MoM Policy update.")
    String ACTION_TYPE_FAILED_MOM_UPDATE_VDS_VERSION();

    @DefaultStringValue("Bond is not attached to Network.")
    String NETWORK_BOND_NOT_ATTACH_TO_NETWORK();

    @DefaultStringValue("Network Interface is not attached to Logical Network.")
    String NETWORK_INTERFACE_NOT_ATTACH_TO_NETWORK();

    @DefaultStringValue("Bonding cannot be applied on an Interface where VLAN is defined.\n-Please remove VLAN from the interface.")
    String NETWORK_INTERFACE_IN_USE_BY_VLAN();

    @DefaultStringValue("Logical Network is already attached to Cluster.")
    String NETWORK_ALREADY_ATTACHED_TO_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. Host with the same address already exists.")
    String ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. Invalid SSH port was entered.")
    String ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_PORT();

    @DefaultStringValue("Cannot ${action} ${type}. Invalid SSH user name was entered.")
    String ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_USERNAME();

    @DefaultStringValue("Cannot ${action} ${type}. Host with the same UUID already exists.")
    String ACTION_TYPE_FAILED_VDS_WITH_SAME_UUID_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. Illegal number of monitors is provided, max allowed number of monitors is 1 for VNC and the max number in the ValidNumOfMonitors configuration variable for SPICE.")
    String ACTION_TYPE_FAILED_ILLEGAL_NUM_OF_MONITORS();

    @DefaultStringValue("Cannot ${action} ${type}. Cannot set single display device via VNC display.")
    String ACTION_TYPE_FAILED_ILLEGAL_SINGLE_DEVICE_DISPLAY_TYPE();

    @DefaultStringValue("Cannot ${action} ${type}. Selected operating system is not supported by the architecture.")
    String ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_IS_NOT_SUPPORTED_BY_ARCHITECTURE_TYPE();

    @DefaultStringValue("Cannot ${action} ${type}. Selected watchdog model is not supported by the operating system.")
    String ACTION_TYPE_FAILED_ILLEGAL_WATCHDOG_MODEL_IS_NOT_SUPPORTED_BY_OS();

    @DefaultStringValue("Cannot ${action} ${type}. Selected display type is not supported by the operating system.")
    String ACTION_TYPE_FAILED_ILLEGAL_VM_DISPLAY_TYPE_IS_NOT_SUPPORTED_BY_OS();

    @DefaultStringValue("Cannot ${action} ${type}. Selected operation system does not support VirtIO-SCSI. Please disable VirtIO-SCSI for the VM.")
    String ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_DOES_NOT_SUPPORT_VIRTIO_SCSI();

    @DefaultStringValue("Cannot ${action} ${type}. Selected VM has an architecture that does not support suspension.")
    String ACTION_TYPE_FAILED_SUSPEND_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Cannot set single display device to non Linux operating system.")
    String ACTION_TYPE_FAILED_ILLEGAL_SINGLE_DEVICE_OS_TYPE();

    @DefaultStringValue("Cannot ${action} ${type}. Cluster does not support Single Qxl Pci devices.")
    String ACTION_TYPE_FAILED_ILLEGAL_SINGLE_DEVICE_INCOMPATIBLE_VERSION();

    @DefaultStringValue("Cannot ${action} ${type}. Illegal Domain name: ${Domain}. Domain name has unsupported special character ${Char}.")
    String ACTION_TYPE_FAILED_ILLEGAL_DOMAIN_NAME();

    @DefaultStringValue("Cannot decrease data center compatibility version.")
    String ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION();

    @DefaultStringValue("Cannot decrease cluster compatibility version beneath data center compatibility version.")
    String ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION_UNDER_DC();

    @DefaultStringValue("Cannot ${action} ${type}. Selected Compatibility Version is not supported.")
    String ACTION_TYPE_FAILED_GIVEN_VERSION_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Selected data center compatibility version does not support live snapshot.")
    String ACTION_TYPE_FAILED_DATA_CENTER_VERSION_DOESNT_SUPPORT_LIVE_SNAPSHOT();

    @DefaultStringValue("Network address must be specified when using static ip")
    String NETWORK_ADDR_MANDATORY_IN_STATIC_IP();

    @DefaultStringValue("Cannot ${action} ${type}. Related operation is currently in progress. Please try again later.")
    String ACTION_TYPE_FAILED_OBJECT_LOCKED();

    @DefaultStringValue("Cannot ${action} ${type}. The network is currently in use. Please wait and try again later.")
    String ACTION_TYPE_FAILED_NETWORK_IS_USED();

    @DefaultStringValue("Cannot ${action} ${type}. This template is currently in use to create VM ${VmName}.")
    String ACTION_TYPE_FAILED_TEMPLATE_IS_USED_FOR_CREATE_VM();

    @DefaultStringValue("Cannot ${action} ${type}. Snapshot is currently being created for VM ${VmName}.")
    String ACTION_TYPE_FAILED_SNAPSHOT_IS_BEING_TAKEN_FOR_VM();

    @DefaultStringValue("Cannot ${action} ${type}. This disk is currently in use to create VM ${VmName}.")
    String ACTION_TYPE_FAILED_DISK_IS_USED_FOR_CREATE_VM();

    @DefaultStringValue("Cannot ${action} ${type}. Disk ${DiskName} is being removed.")
    String ACTION_TYPE_FAILED_DISK_IS_BEING_REMOVED();

    @DefaultStringValue("Cannot ${action} ${type}. Disk ${DiskName} OVF data is currently being updated.")
    String ACTION_TYPE_FAILED_OVF_DISK_IS_BEING_UPDATED();

    @DefaultStringValue("Cannot ${action} ${type}. Storage Domain OVF data is currently being updated.")
    String ACTION_TYPE_FAILED_DOMAIN_OVF_ON_UPDATE();

    @DefaultStringValue("Cannot ${action} ${type}. Disk is being moved or copied.")
    String ACTION_TYPE_FAILED_DISK_IS_BEING_MIGRATED();

    @DefaultStringValue("Cannot ${action} ${type}. Source and target domains must both be either file domains or block domains.")
    String ACTION_TYPE_FAILED_DESTINATION_AND_SOURCE_STORAGE_SUB_TYPES_DIFFERENT();

    @DefaultStringValue("Cannot ${action} ${type}. Template ${TemplateName} is being exported.")
    String ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_EXPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Can't find Domain(s) in ${applicableStatus} status for some of the Template disks.\n"
            +
            "Please make sure that there is at least one Storage Domain in applicable status for each one of the disks :\n"
            +
            "${disksInfo}")
    String ACTION_TYPE_FAILED_NO_VALID_DOMAINS_STATUS_FOR_TEMPLATE_DISKS();

    @DefaultStringValue("Cannot ${action} ${type}. VM ${VmName} is being imported.")
    String ACTION_TYPE_FAILED_VM_IS_BEING_IMPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. VM ${VmName} is being migrated.")
    String ACTION_TYPE_FAILED_VM_IS_BEING_MIGRATED();

    @DefaultStringValue("Cannot ${action} ${type}. Template ${TemplateName} is being removed.")
    String ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_REMOVED();

    @DefaultStringValue("Cannot ${action} ${type}. VM ${VmName} is being removed from export domain.")
    String ACTION_TYPE_FAILED_VM_IS_BEING_REMOVED_FROM_EXPORT_DOMAIN();

    @DefaultStringValue("Bond attached to vlan, remove bonds vlan first")
    String NETWORK_BOND_HAVE_ATTACHED_VLANS();

    @DefaultStringValue("Cannot attach non vlan network to vlan interface")
    String NETWORK_INTERFACE_CONNECT_TO_VLAN();

    @DefaultStringValue("Cannot remove network '${NetworkName}', it's in use by a VM")
    String NETWORK_CANNOT_REMOVE_NETWORK_IN_USE_BY_VM();

    @DefaultStringValue("Cannot create disk more than ${max}_disk_size GB")
    String ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED();

    @DefaultStringValue("Cannot edit Network while Host is Active, change the Host to Maintenance mode and try again.")
    String NETWORK_HOST_IS_BUSY();

    @DefaultStringValue("Cannot change Template images format")
    String VMT_CANNOT_CHANGE_IMAGES_TEMPLATE();

    @DefaultStringValue("Cannot change image format to raw when image have Snapshots.")
    String VMT_CANNOT_IMPORT_RAW_IMAGE_WITH_SNAPSHOTS();

    @DefaultStringValue("Cannot change image format to raw when VM has Template.")
    String VMT_CANNOT_IMPORT_RAW_IMAGE_WITH_TEMPLATE();

    @DefaultStringValue("Cannot change format to RAW on export VM.")
    String VM_CANNOT_EXPORT_RAW_FORMAT();

    @DefaultStringValue("Cannot export VM. Template ${TemplateName} does not exist on the export domain. if you want to export VM without its Template please use TemplateMustExists=false")
    String ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_EXPORT_DOMAIN();

    @DefaultStringValue("Cannot delete VM, VM not exists in export domain")
    String ACTION_TYPE_FAILED_VM_NOT_FOUND_ON_EXPORT_DOMAIN();

    @DefaultStringValue("The Action ${action} ${type} is not supported for this Cluster or Data Center compatibility version")
    String ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL();

    @DefaultStringValue("Cannot ${action} ${type}. Max number of cpu per socket exceeded")
    String ACTION_TYPE_FAILED_MAX_CPU_PER_SOCKET();

    @DefaultStringValue("Cannot ${action} ${type}. Max number of cpu exceeded")
    String ACTION_TYPE_FAILED_MAX_NUM_CPU();

    @DefaultStringValue("Cannot ${action} ${type}. Max number of sockets exceeded")
    String ACTION_TYPE_FAILED_MAX_NUM_SOCKETS();

    @DefaultStringValue("Import Template failed - Template Id already exist in the system. Please remove the Template (${TemplateName}) from the system first")
    String VMT_CANNOT_IMPORT_TEMPLATE_EXISTS();

    @DefaultStringValue("Cannot import Template Version, Base Template for this Version is missing, please first Import Base Template, or Import Version as Base Template using Clone.")
    String VMT_CANNOT_IMPORT_TEMPLATE_VERSION_MISSING_BASE();

    @DefaultStringValue("Import Template failed - Template Name already exist in the system. Please rename the Template in the system first")
    String VM_CANNOT_IMPORT_TEMPLATE_NAME_EXISTS();

    @DefaultStringValue("Import VM failed - VM Id already exist in the system. Please remove the VM (${VmName}) from the system first")
    String VM_CANNOT_IMPORT_VM_EXISTS();

    @DefaultStringValue("Import VM failed - VM Name already exist in the system. Please rename the VM in the system first")
    String VM_CANNOT_IMPORT_VM_NAME_EXISTS();

    @DefaultStringValue("Cannot edit readonly tag")
    String TAGS_CANNOT_EDIT_READONLY_TAG();

    @DefaultStringValue("Cannot force remove VM when there are running tasks.")
    String VM_CANNOT_REMOVE_HAS_RUNNING_TASKS();

    @DefaultStringValue("Cannot import and collapse VM, Template is missing in domain ${DomainName}")
    String ACTION_TYPE_FAILED_IMPORTED_TEMPLATE_IS_MISSING();

    @DefaultStringValue("Cannot create Pool with 0 VMs")
    String VM_POOL_CANNOT_CREATE_WITH_NO_VMS();

    @DefaultStringValue("Cannot ${action} ${type}. Disk ${DiskName} in VM ${VmName} already marked as boot.")
    String ACTION_TYPE_FAILED_DISK_BOOT_IN_USE();

    @DefaultStringValue("Cannot ${action} ${type}. The VMs ${VmsName} already have a disk marked as boot.")
    String ACTION_TYPE_FAILED_VMS_BOOT_IN_USE();

    @DefaultStringValue("Cannot change cpu level when VMs are running.")
    String VDS_GROUP_CANNOT_UPDATE_CPU_WHEN_RUNNING_VMS();

    @DefaultStringValue("VM priority value passed the permitted max value, value should be between 0 and ${MaxValue}.")
    String VM_OR_TEMPLATE_ILLEGAL_PRIORITY_VALUE();

    @DefaultStringValue("Minimum number of CPU per socket cannot be less than 1")
    String ACTION_TYPE_FAILED_MIN_CPU_PER_SOCKET();

    @DefaultStringValue("Minimum number of sockets cannot be less then 1")
    String ACTION_TYPE_FAILED_MIN_NUM_SOCKETS();

    @DefaultStringValue("Cannot ${action} ${type}. The specified domain '${storageDomainName}' is not an export domain.")
    String ACTION_TYPE_FAILED_SPECIFY_DOMAIN_IS_NOT_EXPORT_DOMAIN();

    @DefaultStringValue("Bad format of IP address")
    String NETWORK_ADDR_IN_STATIC_IP_BAD_FORMAT();

    @DefaultStringValue("Bad format of gateway address")
    String NETWORK_ADDR_IN_GATEWAY_BAD_FORMAT();

    @DefaultStringValue("Bad format of subnet mask")
    String NETWORK_ADDR_IN_SUBNET_BAD_FORMAT();

    @DefaultStringValue("Bad bond name, it must begin with the prefix 'bond' followed by a number.")
    String NETWORK_BOND_NAME_BAD_FORMAT();

    @DefaultStringValue("Bad network name, network cannot start with 'bond'")
    String NETWORK_CANNOT_CONTAIN_BOND_NAME();

    @DefaultStringValue("Local Storage folder on RHEV Hypervisor must be located under ${path}")
    String RHEVH_LOCALFS_WRONG_PATH_LOCATION();

    @DefaultStringValue("Data Center must be \"Local Storage\"")
    String ACTION_TYPE_FAILED_STORAGE_POOL_IS_NOT_LOCAL();

    @DefaultStringValue("Host is not attached to Data Center")
    String ACTION_TYPE_FAILED_VDS_IS_NOT_IN_STORAGE_POOL();

    @DefaultStringValue("Cannot add more than one Host to \"Local Storage\" Data Center")
    String VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE();

    @DefaultStringValue("Cannot remove Host, as it contains a local Storage Domain. Please activate the Host and remove the Data Center first.\n- If Host cannot be activated, use the Force-Remove option on the Data Center object (select the Data Center and right click on it with the mouse).\n- Please note that this action is destructive.")
    String VDS_CANNOT_REMOVE_HOST_WITH_LOCAL_STORAGE();

    @DefaultStringValue("Can not add local Storage Domain to non local storage Host")
    String VDS_CANNOT_ADD_LOCAL_STORAGE_TO_NON_LOCAL_HOST();

    @DefaultStringValue("Can not remove local Storage Domain from non local storage Host")
    String VDS_CANNOT_REMOVE_LOCAL_STORAGE_ON_NON_LOCAL_HOST();

    @DefaultStringValue("Cannot add more than one Cluster to \"Local Storage\" Data Center")
    String VDS_GROUP_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE();

    @DefaultStringValue("Selection algorithm must be set to \"None\" on \"Local Storage\" Data Center")
    String VDS_GROUP_SELECTION_ALGORITHM_MUST_BE_SET_TO_NONE_ON_LOCAL_STORAGE();

    @DefaultStringValue("\"Local Storage\" data domain cannot be detached from Data Center")
    String VDS_GROUP_CANNOT_DETACH_DATA_DOMAIN_FROM_LOCAL_STORAGE();

    @DefaultStringValue("\"File based storage\" is not supported with data center compatibility version.")
    String DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION();

    @DefaultStringValue("\"Gluster based storage\" is not supported with current data center compatibility version.")
    String DATA_CENTER_GLUSTER_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION();

    @DefaultStringValue("Updating Host's Cluster cannot be performed through update Host action, please use Change Host Cluster action instead.")
    String VDS_CANNOT_UPDATE_CLUSTER();

    @DefaultStringValue("Updating VM's Cluster cannot be performed through update VM action, please use Change VM Cluster action instead.")
    String VM_CANNOT_UPDATE_CLUSTER();

    @DefaultStringValue("Cannot update a VM in this status. Try stopping the VM first.")
    String VM_STATUS_NOT_VALID_FOR_UPDATE();

    @DefaultStringValue("Cannot add permission, no permission sent")
    String PERMISSION_ADD_FAILED_PERMISSION_NOT_SENT();

    @DefaultStringValue("Cannot add permission, invalid Role id")
    String PERMISSION_ADD_FAILED_INVALID_ROLE_ID();

    @DefaultStringValue("Cannot add permission, invalid object ID or type")
    String PERMISSION_ADD_FAILED_INVALID_OBJECT_ID();

    @DefaultStringValue("Cannot add permission, no user/group ID or ID's mismatch")
    String PERMISSION_ADD_FAILED_USER_ID_MISMATCH();

    @DefaultStringValue("Cannot add permission, only system super user can give permissions with admin Role")
    String PERMISSION_ADD_FAILED_ONLY_SYSTEM_SUPER_USER_CAN_GIVE_ADMIN_ROLES();

    @DefaultStringValue("Cannot remove permission, only system super user can remove permissions with admin Role")
    String PERMISSION_REMOVE_FAILED_ONLY_SYSTEM_SUPER_USER_CAN_REMOVE_ADMIN_ROLES();

    @DefaultStringValue("Cannot add permission, VM is part of VM-Pool, permission should be given on VM-Pool instead")
    String PERMISSION_ADD_FAILED_VM_IN_POOL();

    @DefaultStringValue("Role with this ID does not exit")
    String ROLE_WITH_ROLE_ID_DO_NOT_EXIST();

    @DefaultStringValue("CPU utilization threshold must be between 0 and 100.")
    String VDS_GROUP_CPU_UTILIZATION_MUST_BE_IN_VALID_RANGE();

    @DefaultStringValue("The lower CPU utilization threshold must be lower than the defined upper threshold.")
    String VDS_GROUP_CPU_LOW_UTILIZATION_PERCENTAGE_MUST_BE_LOWER_THAN_HIGH_PERCENTAGE();

    @DefaultStringValue("High CPU utilization threshold must be defined when using evenly distributed policy.")
    String VDS_GROUP_CPU_HIGH_UTILIZATION_PERCENTAGE_MUST_BE_DEFINED_WHEN_USING_EVENLY_DISTRIBUTED();

    @DefaultStringValue("Both low and high CPU utilization thresholds must be defined when using power saving policy.")
    String VDS_GROUP_BOTH_LOW_AND_HIGH_CPU_UTILIZATION_PERCENTAGE_MUST_BE_DEFINED_WHEN_USING_POWER_SAVING();

    @DefaultStringValue("The default gateway should be set only on the Management Network")
    String NETWORK_ATTACH_ILLEGAL_GATEWAY();

    @DefaultStringValue("A slave interface is not properly configured. Please verify slaves do not contain any of the following properties: network name, boot protocol, IP address, netmask, gateway or vlan-ID notation (as part of interface's name or explicitly).")
    String SLAVE_INTERFACE_IS_MISCONFIGURED();

    @DefaultStringValue("Cannot ${action} ${type}. An improper network interface is labeled. Please verify labels are provided only to interfaces or to bonds (not to slaves nor vlans).")
    String IMPROPER_INTERFACE_IS_LABELED();

    @DefaultStringValue("Cannot ${action} ${type}. An improper bond is labeled. Please verify labels are provided only to bonds with at least two slaves.")
    String IMPROPER_BOND_IS_LABELED();

    @DefaultStringValue("Cannot ${action} ${type}. The network interface is already labeled with the specified label.")
    String INTERFACE_ALREADY_LABELED();

    @DefaultStringValue("Cannot ${action} ${type}. The network interface is not labeled with the specified label.")
    String INTERFACE_NOT_LABELED();

    @DefaultStringValue("User/group ID cannot be empty.")
    String MISSING_DIRECTORY_ELEMENT_ID();

    @DefaultStringValue("Network name must be 1-15 long and can contain only 'A-Z', 'a-z', '0-9', '_' characters")
    String NETWORK_ILEGAL_NETWORK_NAME();

    @DefaultStringValue("Network interface name is already in use")
    String NETWORK_INTERFACE_NAME_ALREADY_IN_USE();

    @DefaultStringValue("Storage related operations can't be performed while the Storage Pool Manager is down.\nPlease make sure the Storage Pool Manager is up and running, and check network connectivity.")
    String STORAGE_OPERATION_FAILED_SPM_NETWORK_PROBLEMS();

    @DefaultStringValue("Invalid tag name. Only 'A-Z', 'a-z', '0-9', '_' and '-' characters are valid")
    String VALIDATION_TAGS_INVALID_TAG_NAME();

    @DefaultStringValue("Pool name is required")
    String VALIDATION_VM_POOLS_NAME_NOT_NULL();

    @DefaultStringValue("SPICE proxy address must be in form [protocol://]hostname or ip[:port]")
    String VALIDATION_VM_POOLS_SPICE_PROXY_HOSTNAME_OR_IP();

    @DefaultStringValue("Role name is required")
    String VALIDATION_ROLES_NAME_NOT_NULL();

    @DefaultStringValue("Role name must not exceed 126 characters")
    String VALIDATION_ROLES_NAME_MAX();

    @DefaultStringValue("Cluster name is required")
    String VALIDATION_VDS_GROUP_NAME_NOT_NULL();

    @DefaultStringValue("Cluster name must not exceed 40 characters")
    String VALIDATION_VDS_GROUP_NAME_MAX();

    @DefaultStringValue("Cluster migrate on error option is required")
    String VALIDATION_VDS_GROUP_MigrateOnError_NOT_NULL();

    @DefaultStringValue("SPICE proxy address must be in form [protocol://]hostname or ip[:port]")
    String VALIDATION_VDS_GROUP_SPICE_PROXY_HOSTNAME_OR_IP();

    @DefaultStringValue("Data Center ID is required")
    String VALIDATION_STORAGE_POOL_ID_NOT_NULL();

    @DefaultStringValue("Host address must be a FQDN or a valid IP address")
    String VALIDATION_VDS_HOSTNAME_HOSTNAME_OR_IP();

    @DefaultStringValue("Host power management address must be a FQDN or a valid IP address")
    String VALIDATION_VDS_POWER_MGMT_ADDRESS_HOSTNAME_OR_IP();

    @DefaultStringValue("VM exceeded the number of allowed monitors")
    String VALIDATION_VM_NUM_OF_MONITORS_EXCEEDED();

    @DefaultStringValue("VM Template name must not exceed 40 characters")
    String VALIDATION_VM_TEMPLATE_NAME_MAX();

    @DefaultStringValue("Interface is required")
    String VALIDATION_DISK_INTERFACE_NOT_NULL();

    @DefaultStringValue("Volume type is required")
    String VALIDATION_VOLUME_TYPE_NOT_NULL();

    @DefaultStringValue("Disk type is required")
    String VALIDATION_DISK_TYPE_NOT_NULL();

    @DefaultStringValue("Disk alias name must be formed of alpha-numeric characters or \"-_.\"")
    String VALIDATION_DISK_ALIAS_INVALID();

    @DefaultStringValue("Volume format is required")
    String VALIDATION_VOLUME_FORMAT_NOT_NULL();

    @DefaultStringValue("Snapshot description cannot be empty")
    String VALIDATION_DISK_IMAGE_DESCRIPTION_NOT_EMPTY();

    @DefaultStringValue("Snapshot description must not exceed 4000 characters")
    String VALIDATION_DISK_IMAGE_DESCRIPTION_MAX();

    @DefaultStringValue("MAC Address must be in the following format: XX:XX:XX:XX:XX:XX, (XX=0-9 or A-F)")
    String VALIDATION_VMINTERFACE_MACADDRESS();

    @DefaultStringValue("\"Data Center description must be formed of ASCII charis only\"")
    String VALIDATION_DATA_CENTER_DESCRIPTION_INVALID();

    @DefaultStringValue("Host name must be formed of alphanumeric characters, numbers or \"-_.\"")
    String VALIDATION_VDS_NAME_INVALID();

    @DefaultStringValue("Console address must be a FQDN or a valid IP address")
    String VALIDATION_VDS_CONSOLEADDRESSS_HOSTNAME_OR_IP();

    @DefaultStringValue("email format is not valid")
    String VALIDATION_EVENTS_EMAIL_FORMAT();

    @DefaultStringValue("The correlation ID must be formed of \"a-z0-9A-Z\" or \"-_\"")
    String VALIDATION_INVALID_CORRELATION_ID();

    @DefaultStringValue("Role name must be formed of \"a-z0-9A-Z\" or \"-_\"")
    String VALIDATION_ROLES_NAME_INVALID();

    @DefaultStringValue("Cluster name must be formed of \"a-z0-9A-Z\" or \"-_\"")
    String VALIDATION_VDS_GROUP_NAME_INVALID();

    @DefaultStringValue("Storage Domain name must be formed of \"a-z0-9A-Z\" or \"-_\"")
    String VALIDATION_STORAGE_DOMAIN_NAME_INVALID();

    @DefaultStringValue("Storage Domain description must be formed of \"a-z0-9A-Z\" or \"-_\"")
    String VALIDATION_STORAGE_DOMAIN_DESCRIPTION_INVALID();

    @DefaultStringValue("Storage Domain description must not exceed 4000 characters")
    String VALIDATION_STORAGE_DOMAIN_DESCRIPTION_MAX();

    @DefaultStringValue("Data Center name must be formed of \"a-z0-9A-Z\" or \"-_\"")
    String VALIDATION_DATA_CENTER_NAME_INVALID();

    @DefaultStringValue("ID is required.")
    String VALIDATION_ID_NULL();

    @DefaultStringValue("Name is required.")
    String VALIDATION_NAME_NULL();

    @DefaultStringValue("Name must be formed of alphanumeric characters, numbers or \"-_\".")
    String VALIDATION_NAME_INVALID();

    @DefaultStringValue("Name must be formed of alphanumeric characters, numbers or \"-_.\".")
    String VALIDATION_NAME_INVALID_WITH_DOT();

    @DefaultStringValue("URL is required.")
    String VALIDATION_URL_NULL();

    @DefaultStringValue("Provider type is required.")
    String VALIDATION_PROVIDER_TYPE_NULL();

    @DefaultStringValue("Quota name must be formed of \"a-z0-9A-Z\" and \"-_\"")
    String VALIDATION_QUOTA_NAME_INVALID();

    @DefaultStringValue("MAC address must be in format \"HH:HH:HH:HH:HH:HH\" where H is a hexadecimal character (either a digit or A-F, case is insignificant).")
    String VALIDATION_VM_NETWORK_MAC_ADDRESS_INVALID();

    @DefaultStringValue("Multi-cast MAC address is not allowed.")
    String VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST();

    @DefaultStringValue("MAC address is required.")
    String VALIDATION_VM_NETWORK_MAC_ADDRESS_NOT_NULL();

    @DefaultStringValue("Interface name is required.")
    String VALIDATION_VM_NETWORK_NAME_NOT_NULL();

    @DefaultStringValue("Connectivity timeout is not valid. Timeout must be between 1 and 120.")
    String VALIDATION_CONNECTIVITY_TIMEOUT_INVALID();

    @DefaultStringValue("Invalid list of interfaces, two or more network interfaces have the same IP.")
    String VALIDATION_REPETITIVE_IP_IN_VDS();

    @DefaultStringValue("The Port number must be between 1 and 65535.")
    String VALIDATION_VDS_PORT_RANGE();

    @DefaultStringValue("Invalid ISO image path")
    String ERROR_CANNOT_FIND_ISO_IMAGE_PATH();

    @DefaultStringValue("Invalid Floppy image path")
    String ERROR_CANNOT_FIND_FLOPPY_IMAGE_PATH();

    @DefaultStringValue("Cannot add storage server connection when Host status is not up")
    String VDS_ADD_STORAGE_SERVER_STATUS_MUST_BE_UP();

    @DefaultStringValue("The user name or password is incorrect.")
    String USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWORD();

    @DefaultStringValue("Cannot update Cluster and change CPU Cluster level if there are suspended VMs in the Cluster")
    String VDS_GROUP_CANNOT_UPDATE_CPU_WITH_SUSPENDED_VMS();

    @DefaultStringValue("Cannot update Cluster and change CPU Cluster name if there are hosts or virtual machines in the Cluster. This CPU name is incompatible with all other available CPUs")
    String VDS_GROUP_CPU_IS_NOT_UPDATABLE();

    @DefaultStringValue("Cannot update Cluster, custom properties are not supported under current Cluster version")
    String CUSTOM_PROPERTIES_INVALID_VALUES_NOT_ALLOWED_IN_CURRENT_CLUSTER();

    @DefaultStringValue("Authentication failed. The user is either locked or disabled")
    String USER_FAILED_TO_AUTHENTICATE_ACCOUNT_IS_LOCKED_OR_DISABLED();

    @DefaultStringValue("Authentication Failed. Error in DNS configuration. Please verify the Engine Host has a valid reverse DNS (PTR) record.")
    String USER_FAILED_TO_AUTHENTICATE_DNS_ERROR();

    @DefaultStringValue("Authentication Failed. Please verify the fully qualified domain name that is used for authentication is correct.")
    String USER_FAILED_TO_AUTHENTICATE_NO_KDCS_FOUND();

    @DefaultStringValue("Authentication Failed. The Engine clock is not synchronized with directory services (must be within 5 minutes difference). Please verify the clocks are synchronized and restart the service.")
    String USER_FAILED_TO_AUTHENTICATE_CLOCK_SKEW_TOO_GREAT();

    @DefaultStringValue("Authentication Failed. Connection to Directory server has timed out. Please contact your system administrator")
    String USER_FAILED_TO_AUTHENTICATE_CONNECTION_TIMED_OUT();

    @DefaultStringValue("Authentication Failed. Wrong domain name was provided for authentication")
    String USER_FAILED_TO_AUTHENTICATE_WRONG_REALM();

    @DefaultStringValue("Cannot ${action} ${type} if some of the specified custom properties appear more than once. The keys are: ${DuplicateKeys}")
    String ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_DUPLICATE_KEYS();

    @DefaultStringValue("Host Address can not be modified due to Security restrictions.  In order to change Host Address, Host has to be reinstalled")
    String ACTION_TYPE_FAILED_HOSTNAME_CANNOT_CHANGE();

    @DefaultStringValue("Action failed due to database connection failure. Please check connectivity to your Database server.")
    String CAN_DO_ACTION_DATABASE_CONNECTION_FAILURE();

    @DefaultStringValue("Cannot ${action} ${type}. The given description contains special characters.\nOnly alpha-numeric and some special characters that conform to the standard ASCII character set are allowed.")
    String ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS();

    @DefaultStringValue("Cannot ${action} ${type}. Linux boot parameters contain trimming whitespace characters.")
    String ACTION_TYPE_FAILED_LINUX_BOOT_PARAMS_MAY_NOT_CONTAIN_TRIMMING_WHITESPACES();

    @DefaultStringValue("Cannot ${action} ${type}. The given name is too long.")
    String ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG();

    @DefaultStringValue("Can not ${action} ${type}. The given name is empty.")
    String ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY();

    @DefaultStringValue("Can not ${action} ${type}. The selected template is not compatible with Cluster architecture.")
    String ACTION_TYPE_FAILED_TEMPLATE_IS_INCOMPATIBLE();

    @DefaultStringValue("Can not ${action} ${type}. The given name contains special characters. Only lower-case and upper-case letters, numbers, '_', '-', '.' are allowed.")
    String ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS();

    @DefaultStringValue("Can not ${action} ${type}. The given name contains special characters. Only lower-case and upper-case letters, numbers, '_', allowed.")
    String ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS_OR_DASH();

    @DefaultStringValue("Can not ${action} ${type}. The given Host name is invalid. Only Host names corresponding to RFC-952 and RFC-1123 are allowed.")
    String ACTION_TYPE_FAILED_INVALID_VDS_HOSTNAME();

    @DefaultStringValue("Missing UserName or Password.")
    String VM_CANNOT_RUN_ONCE_WITH_ILLEGAL_SYSPREP_PARAM();

    @DefaultStringValue("Cannot remove the built-in group \"Everyone\".")
    String ACTION_TYPE_FAILED_CANNOT_REMOVE_BUILTIN_GROUP_EVERYONE();

    @DefaultStringValue("Cannot ${action} ${type}. The operation is not supported for this Data Center version.")
    String ACTION_TYPE_FAILED_IMPORT_DATA_DOMAIN_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Valid Host statuses are \"Non operational\", \"Maintenance\" or \"Problematic\".")
    String ACTION_TYPE_FAILED_VDS_NOT_MATCH_VALID_STATUS();

    @DefaultStringValue("Can not Remove Storage Domain - the underlying Host ID is invalid.")
    String CANNOT_REMOVE_STORAGE_DOMAIN_INVALID_HOST_ID();

    @DefaultStringValue("You are trying to deactivate a Master storage domain while there are locked domains in the Data Center. Please wait for the operations on those domains to finish first.")
    String ERROR_CANNOT_DEACTIVATE_MASTER_WITH_LOCKED_DOMAINS();

    @DefaultStringValue("Cannot remove tag. Tag does not exist.")
    String TAGS_CANNOT_REMOVE_TAG_NOT_EXIST();

    @DefaultStringValue("Cannot remove audit log. Audit Log does not exist.")
    String AUDIT_LOG_CANNOT_REMOVE_AUDIT_LOG_NOT_EXIST();

    @DefaultStringValue("${type} VM Cluster.")
    String VAR__TYPE__VM__CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. Fence operation failed.")
    String VDS_FENCE_OPERATION_FAILED();

    @DefaultStringValue("Cannot ${action} without active ISO domain.")
    String VM_CANNOT_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO();

    @DefaultStringValue("Cannot ${action} ${type}. The ISO Storage Domain is being used by the following VMs: ${VmNames}.")
    String ERROR_CANNOT_DEACTIVATE_STORAGE_DOMAIN_WITH_ISO_ATTACHED();

    @DefaultStringValue("MAC Address is in use.")
    String MAC_ADDRESS_IS_IN_USE();

    @DefaultStringValue("General command validation failure.")
    String CAN_DO_ACTION_GENERAL_FAILURE();

    @DefaultStringValue("Cannot remove an active Data Center.")
    String ERROR_CANNOT_REMOVE_ACTIVE_STORAGE_POOL();

    @DefaultStringValue("Invalid Role Type.")
    String ROLE_TYPE_CANNOT_BE_EMPTY();

    @DefaultStringValue("Cannot add administrator's action group to a User Role.")
    String CANNOT_ADD_ACTION_GROUPS_TO_ROLE_TYPE();

    @DefaultStringValue("Storage Domain is already detached from the Data Center.")
    String STORAGE_DOMAIN_NOT_ATTACHED_TO_STORAGE_POOL();

    @DefaultStringValue("Cannot approve Host - Host does not exists.")
    String VDS_APPROVE_VDS_NOT_FOUND();

    // Quota messages.
    @DefaultStringValue("Cannot ${action} ${type}. Quota doesn't exist.")
    String ACTION_TYPE_FAILED_QUOTA_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. Quota is not valid.")
    String ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID();

    @DefaultStringValue("Cannot ${action} ${type}. No quota is defined for the selected domain. Assign a quota to the domain or select a different domain.")
    String ACTION_TYPE_FAILED_NO_QUOTA_SET_FOR_DOMAIN();

    @DefaultStringValue("Cannot ${action} ${type}. The quota associated with VM ${VmName} is no longer available. This may be a result of an import or snapshot restoring actions. Please reassign a quota to this VM.")
    String ACTION_TYPE_FAILED_QUOTA_IS_NO_LONGER_AVAILABLE_IN_SYSTEM();

    @DefaultStringValue("Cannot ${action} ${type}. Limitation can not be configured as specific and general in the same Quota. Please choose whether the limitation should be enforced on the Data Center or on specific storage.")
    String ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL();

    @DefaultStringValue("Cannot ${action} ${type}. Quota limitation must be configured specific or general.")
    String ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_NOT_SPECIFIC_OR_GENERAL();

    @DefaultStringValue("Cannot ${action} ${type}. The prefix DefaultQuota is reserved for the default quota.")
    String ACTION_TYPE_FAILED_QUOTA_NAME_RESERVED_FOR_DEFAULT();

    @DefaultStringValue("Cannot ${action} ${type}. Quota can not have default indication.")
    String ACTION_TYPE_FAILED_QUOTA_CAN_NOT_HAVE_DEFAULT_INDICATION();

    @DefaultStringValue("Cannot ${action} ${type}. Quota with default indication can not be removed while Data Center is at disable mode.")
    String ACTION_TYPE_FAILED_QUOTA_WITH_DEFAULT_INDICATION_CAN_NOT_BE_REMOVED();

    @Constants.DefaultStringValue("Cannot ${action} ${type}. Quota is still in use by a VM or a disk and Data Center's Quota enforcement is enabled.")
    String ACTION_TYPE_FAILED_QUOTA_IN_USE_BY_VM_OR_DISK();

    @DefaultStringValue("Cannot ${action} ${type}. Data Center must have at least one quota associated with it. Please try to add another Quota, or change the existing one.")
    String ACTION_TYPE_FAILED_DATA_CENTER_MUST_HAVE_AT_LEAST_ONE_QUOTA();

    @DefaultStringValue("Cannot ${action} ${type}. Quota has insufficient storage resources.")
    String ACTION_TYPE_FAILED_QUOTA_STORAGE_LIMIT_EXCEEDED();

    @DefaultStringValue("Cannot ${action} ${type}. Quota has insufficient cluster resources.")
    String ACTION_TYPE_FAILED_QUOTA_VDS_GROUP_LIMIT_EXCEEDED();

    @DefaultStringValue("Cannot ${action} ${type}. The user is not a consumer of the Quota assigned to the resource.")
    String USER_NOT_AUTHORIZED_TO_CONSUME_QUOTA();

    // Internal
    @DefaultStringValue("Permission denied. Query you try to run not public.")
    String USER_CANNOT_RUN_QUERY_NOT_PUBLIC();

    @DefaultStringValue("Cannot attach action group to Role. This action group is already attached to Role.")
    String ERROR_CANNOT_ATTACH_ACTION_GROUP_TO_ROLE_ATTACHED();

    @DefaultStringValue("Cannot attach action group to Role. The action group is invalid.")
    String ERROR_CANNOT_ATTACH_ACTION_GROUP_TO_ROLE_INVALID_ACTION();

    @DefaultStringValue("Cannot detach action group from Role. This action group is not attached to this Role.")
    String ERROR_CANNOT_DETACH_ACTION_GROUP_TO_ROLE_NOT_ATTACHED();

    // Not Used
    @DefaultStringValue("There is no valid license for oVirt Engine")
    String ERROR_LICENSE_NO_LICENSE();

    @DefaultStringValue("Login failed, if the Engine Service was restarted please wait few seconds and try again, if the problem persists please check your oVirt Engine license.")
    String ERROR_LICENSE_ILLEGAL();

    @DefaultStringValue("The Engine has detected that the system clock has been modified")
    String ERROR_LICENSE_SYSTEM_CLOCK_MODIFIED();

    @DefaultStringValue("oVirt Engine's evaluation period has expired")
    String ERROR_LICENSE_EXPIRED();

    @DefaultStringValue("oVirt Engine is in evaluation mode. Evaluation period will end in ${DaysLeft} days")
    String MESSAGE_LICENSE_EVALUATION__PARAM_DAYS_LEFT();

    @DefaultStringValue("Cannot register Host. No Hostname specified.")
    String VDS_REGISTER_NO_HOSTNAME_INPUT();

    @DefaultStringValue("Cannot register Host, Host with the same unique ID was found, but requested Host type does not match.")
    String VDS_REGISTER_UNIQUE_ID_DIFFERENT_TYPE();

    @DefaultStringValue("Cannot add Host, Host unique ID is ambiguous with hosts: ${HostNameList}.")
    String VDS_REGISTER_UNIQUE_ID_AMBIGUOUS();

    @DefaultStringValue("Cannot ${action} ${type}. Connecting to host via SSH has failed, verify that the host is reachable (IP address, routable address etc.) You may refer to the engine.log file for further details.")
    String VDS_CANNOT_CONNECT_TO_SERVER();

    @DefaultStringValue("Cannot ${action} ${type}. SSH authentication failed, verify authentication parameters are correct (Username/Password, public-key etc.) You may refer to the engine.log file for further details.")
    String VDS_CANNOT_AUTHENTICATE_TO_SERVER();

    @DefaultStringValue("Cannot ${action} ${type}. SSH connection failed, ${ErrorMsg}.")
    String VDS_SECURITY_CONNECTION_ERROR();

    @DefaultStringValue("Cannot migrate - check relevant configuration options.")
    String AUTO_MIGRATE_DISABLED();

    @DefaultStringValue("Cannot migrate - Host not found.")
    String AUTO_MIGRATE_VDS_NOT_FOUND();

    @DefaultStringValue("Cannot migrate - VM already running on Host.")
    String AUTO_MIGRATE_ALREADY_RUNNING_ON_VDS();

    @DefaultStringValue("Cannot migrate - Previous migration was unsuccessful.")
    String AUTO_MIGRATE_UNSUCCESSFUL();

    @DefaultStringValue("Cannot approve Host. Host is not found.")
    String APPROVE_VDS_VDS_NOT_FOUND();

    @DefaultStringValue("Cannot ${action} Cluster. Cluster ID is not valid.")
    String VDS_GROUP_CLUSTER_IS_NOT_VALID();

    @DefaultStringValue("Network file is already in use")
    String NETWORK_INTERFACE_NAME_ALREAY_EXISTS();

    @DefaultStringValue("Storage from this type not allowed to be added")
    String STORAGE_DOMAIN_TYPE_ILLEGAL_FOR_ADDING_EXISTING();

    @DefaultStringValue("The specified Interface is not a display Interface.")
    String NETWORK_INTERFACE_NOT_HAVE_DISPLAY_FLAG();

    @DefaultStringValue("Cannot create Data Center - There must be at least one Data storage.")
    String ERROR_CANNOT_ADD_STORAGE_POOL_WITHOUT_DATA_AND_ISO_DOMAINS();

    @DefaultStringValue("Cannot add data storages to pool. Storages should have same format.")
    String ERROR_CANNOT_ADD_STORAGE_POOL_WITH_DIFFERENT_STORAGE_FORMAT();

    @DefaultStringValue("Cannot ${action} ${type}. There are VMs in Data Center.")
    String ACTION_TYPE_FAILED_VMS_IN_STORAGE_POOL();

    @DefaultStringValue("Please move all servers to maintenance before updating the Data Center.")
    String ERROR_CANNOT_CHANGE_STORAGE_POOL_VDSS_UP();

    @DefaultStringValue("Data Center must be specified")
    String NETWORK_STORAGE_POOL_MUST_BE_SPECIFY();

    @DefaultStringValue("Unknown tag name.")
    String EN_UNKNOWN_TAG_NAME();

    @DefaultStringValue("Cannot attach Storage Domain, Shared Domains are not supported in this Data Center compatibility version.")
    String ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_SHARED_NOT_SUPPORTED_IN_THIS_POOL();

    @DefaultStringValue("Cannot attach Storage Domain, Shared Domains are not supported in other Data Center compatibility version.")
    String ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_SHARED_NOT_SUPPORTED_IN_OTHER_POOL();

    @DefaultStringValue("Cannot remove Data Center with networks, please remove all networks first.")
    String ERROR_CANNOT_REMOVE_POOL_WITH_NETWORKS();

    @DefaultStringValue("Cannot ${action} ${type}. Some Hosts in the Cluster are in Up state. Please move all Hosts to Maintenance mode first.")
    String ACTION_TYPE_FAILED_UP_VDSS_IN_CLUSTER();

    @DefaultStringValue("Directory Element tag is missing in requested user")
    String TAGS_DIRECTORY_ELEMENT_TAG_IS_MISSING();

    @DefaultStringValue("Directory Group is already attached to this VM.")
    String DIRECTORY_GROUP_CANNOT_ATTACH_TO_VM_ALREADY_ATTACHED();

    @DefaultStringValue("Directory Group is not attached to the specified VM.")
    String DIRECTORY_GROUP_NOT_ATTACH_TO_VM();

    @DefaultStringValue("Cannot remove the default Directory tag.")
    String ERROR_CANNOT_DEFAULT_DIRECTORY_ELEMENT_TAG();

    @DefaultStringValue("Cannot ${action} ${type}. No Host supplied.")
    String ACTION_TYPE_FAILED_NO_VDS_SUPPLIED();

    @DefaultStringValue("Cannot remove the default Network.")
    String NETWORK_CAN_NOT_REMOVE_DEFAULT_NETWORK();

    @DefaultStringValue("Cannot ${action} ${type}. Renaming a network label while configured on hosts interfaces is not supported. Please unlabel the network first and later specify the new one.")
    String ACTION_TYPE_FAILED_NETWORK_LABEL_RENAMING_NOT_SUPPORTED();

    @DefaultStringValue("Cannot update default network name.")
    String NETWORK_CAN_NOT_UPDATE_DEFAULT_NETWORK();

    @DefaultStringValue("The specified Tag cannot be assigned")
    String TAGS_CANNOT_ASSING_TAG();

    @DefaultStringValue("Cannot ${action} ${type}. Illegal memory defined for Windows XP.")
    String ACTION_TYPE_FAILED_XP_MEMORY_ERROR();

    @DefaultStringValue("Cannot update Network with VLAN, you need to set the Interface to \"None\" Network")
    String NETWORK_INTERFACE_CANNOT_UPDATE_INTERFACE_VLAN();

    @DefaultStringValue("Illegal user supplied")
    String ILLEAGAL_USER_PROVIDED();

    @DefaultStringValue("User cannot be added to VM")
    String USER_CANNOT_BE_ADDED_TO_VM();

    @DefaultStringValue("User cannot be added to VM-Pool")
    String USER_CANNOT_BE_ADDED_TO_VM_POOL();

    @DefaultStringValue("Cannot ${action} ${type}. The following VMs are set to run specifically only on this Host: ${VmNames}.\nIn order to ${action} ${type}, you need to remove the association between the VMs and the Host (Using Edit VM properties).")
    String ACTION_TYPE_FAILED_DETECTED_PINNED_VMS();

    @DefaultStringValue("Activate/Deactivate while VM is running, is only supported for Clusters of version 3.1 and above.")
    String HOT_PLUG_IS_NOT_SUPPORTED();

    @DefaultStringValue("Hot plugging a CPU is not supported for cluster version ${clusterVersion} and architecture ${architecture}.")
    String HOT_PLUG_CPU_IS_NOT_SUPPORTED();

    @DefaultStringValue("Hot un-plugging a CPU is not supported for cluster version ${clusterVersion} and architecture ${architecture}.")
    String HOT_UNPLUG_CPU_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Activation/Deactivation of Disk Snapshot is not supported for clusters of version ${clusterVersion}.")
    String HOT_PLUG_DISK_SNAPSHOT_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Link state is set to 'Down' on the virtual machine's interface, this is not supported for clusters of version ${clusterVersion}.")
    String UNLINKING_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. There is no network on the virtual machine's interface, this is not supported for clusters of version ${clusterVersion}.")
    String NULL_NETWORK_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. There is Network QoS on the profile, this is not supported for clusters of version ${clusterVersion}.")
    String ACTION_TYPE_FAILED_NETWORK_QOS_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Host Network QoS is not supported in the cluster's compatibility version, but QoS was configured on the following network(s): ${ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED_LIST}.")
    String ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Network labels are not supported in the cluster's compatibility version, but are configured on the host's interfaces.")
    String ACTION_TYPE_FAILED_HOST_NETWORK_LABELS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Updating the virtual machine interface while the virtual machine is running is not supported for clusters of version ${clusterVersion}.")
    String HOT_VM_INTERFACE_UPDATE_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. The network interface type is not compatible with the selected operating system.")
    String ACTION_TYPE_FAILED_VM_INTERFACE_TYPE_IS_NOT_SUPPORTED_BY_OS();

    @DefaultStringValue("Cannot ${action} ${type}. Updating some of the properties is not supported while the interface is plugged into a running virtual machine. Please un-plug the interface, update the properties, and then plug it back.")
    String CANNOT_PERFORM_HOT_UPDATE();

    @DefaultStringValue("Cannot ${action} ${type}. Update is not possible when 'Port Mirroring' is set on the interface of a running virtual machine.")
    String CANNOT_PERFORM_HOT_UPDATE_WITH_PORT_MIRRORING();

    @DefaultStringValue("Cannot ${action} ${type}. 'Port Mirroring' setting requires a network.")
    String PORT_MIRRORING_REQUIRES_NETWORK();

    @DefaultStringValue("Cannot ${action} ${type}. Guest OS version is not supported.")
    String ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED();

    @DefaultStringValue("Can plug only VirtIO disks.")
    String HOT_PLUG_DISK_IS_NOT_VIRTIO();

    @DefaultStringValue("Disk is already activated.")
    String HOT_PLUG_DISK_IS_NOT_UNPLUGGED();

    @DefaultStringValue("Cannot ${action} ${type}. A disk configured with the \"Activate\" setting cannot be created as a floating disk.")
    String CANNOT_ADD_FLOATING_DISK_WITH_PLUG_VM_SET();

    @DefaultStringValue("Cannot activate/deactivate interface due to VM status. The VM status must be Down or Up.")
    String ACTIVATE_DEACTIVATE_NIC_VM_STATUS_ILLEGAL();

    @DefaultStringValue("The Network does not exist on the host the VM is running on.\n Either add the Network to the Host or migrate the VM to a Host that has this Network.")
    String ACTIVATE_DEACTIVATE_NETWORK_NOT_IN_VDS();

    @DefaultStringValue("Disk is already deactivated.")
    String HOT_UNPLUG_DISK_IS_NOT_PLUGGED();

    @DefaultStringValue("Cannot ${action} ${type}. Disk cannot be shareable if it depends on a snapshot. In order to share it, remove the disk's snapshots.")
    String SHAREABLE_DISK_IS_NOT_SUPPORTED_FOR_DISK();

    @DefaultStringValue("Cannot ${action} ${type}. Disk's volume format is not supported for shareable disk.")
    String SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT();

    @DefaultStringValue("Cannot ${action} ${type}. The disk is already configured in a snapshot. In order to detach it, remove the disk's snapshots.")
    String ERROR_CANNOT_DETACH_DISK_WITH_SNAPSHOT();

    @DefaultStringValue("Cannot ${action} ${type}. Disk is already shared between VMs. Remove the disk from the VMs and try to update the disk again")
    String DISK_IS_ALREADY_SHARED_BETWEEN_VMS();

    @DefaultStringValue("Cannot ${action} ${type} without at least one active disk.\nPlease activate a disk and rerun the VM.")
    String VM_CANNOT_RUN_FROM_DISK_WITHOUT_PLUGGED_DISK();

    @DefaultStringValue("Cannot ${action} ${type}. SCSI Generic IO is not supported for image disk.")
    String SCSI_GENERIC_IO_IS_NOT_SUPPORTED_FOR_IMAGE_DISK();

    @DefaultStringValue("Cannot ${action} ${type}. SCSI device pass-throguh is not supported for a read-only disk.")
    String SCSI_PASSTHROUGH_IS_NOT_SUPPORTED_FOR_READ_ONLY_DISK();

    @DefaultStringValue("VirtIO-SCSI interface is only available on cluster level 3.3 or higher.")
    String VIRTIO_SCSI_INTERFACE_IS_NOT_AVAILABLE_FOR_CLUSTER_LEVEL();

    @DefaultStringValue("Cannot ${action} ${type}. VirtIO-SCSI is disabled for the VM")
    String CANNOT_PERFORM_ACTION_VIRTIO_SCSI_IS_DISABLED();

    @DefaultStringValue("Cannot disable VirtIO-SCSI when disks with a VirtIO-SCSI interface are plugged into the VM.")
    String CANNOT_DISABLE_VIRTIO_SCSI_PLUGGED_DISKS();

    @DefaultStringValue("Cannot Login. User Password has expired, Please change your password.")
    String USER_PASSWORD_EXPIRED();

    @DefaultStringValue("Cannot Login. User Password has expired. Use the following URL to change the password: ${URL}")
    String USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED();

    @DefaultStringValue("Cannot login. User Password has expired. Detailed message: ${MSG}")
    String USER_PASSWORD_EXPIRED_CHANGE_MSG_PROVIDED();

    @DefaultStringValue("Cannot Login. The Domain provided is not configured, please contact your system administrator.")
    String USER_CANNOT_LOGIN_DOMAIN_NOT_SUPPORTED();

    @DefaultStringValue("Cannot decrease VMs from VM-Pool.")
    String VM_POOL_CANNOT_DECREASE_VMS_FROM_POOL();

    @DefaultStringValue("User is already attached to maximum number of VMs from this VM-Pool.")
    String VM_POOL_CANNOT_ATTACH_TO_MORE_VMS_FROM_POOL();

    // bad names
    // NETWORK_INTERFACE_EXITED_MAX_INTERFACES
    @DefaultStringValue("Cannot ${action} ${type}. Selected Cluster is missing one or more networks ${networks} that is used by VM.")
    String MOVE_VM_CLUSTER_MISSING_NETWORK();

    @DefaultStringValue("Default Cluster cannot be moved to the Data Center that has local Storage.")
    String ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_VDS_GROUP_CANNOT_BE_LOCALFS();

    @DefaultStringValue("Data Center containing the default Cluster does not support local Storage.")
    String DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS();

    @DefaultStringValue("Cannot cancel migration for non migrating VM.")
    String VM_CANNOT_CANCEL_MIGRATION_WHEN_VM_IS_NOT_MIGRATING();

    @DefaultStringValue("Cannot ${action} ${type} to a Snapshot that is not being previewed. Please select the correct Snapshot to restore to: Either the one being previewed, or the one before the preview.")
    String ACTION_TYPE_FAILED_VM_SNAPSHOT_NOT_IN_PREVIEW();

    @DefaultStringValue("Cannot ${action} a shareable ${type} (${diskAliases}). This operation is not supported.")
    String ACTION_TYPE_FAILED_SHAREABLE_DISK_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. The specified disk does not exist.")
    String ACTION_TYPE_FAILED_DISK_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The following disk(s) ID(s) does not exist: ${diskIds}.")
    String ACTION_TYPE_FAILED_DISKS_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The following disk snapshot(s) ID(s) does not exist: ${diskSnapshotIds}.")
    String ACTION_TYPE_FAILED_DISK_SNAPSHOTS_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The following disk snapshot(s) is active: ${diskSnapshotIds}.")
    String ACTION_TYPE_FAILED_DISK_SNAPSHOTS_ACTIVE();

    @DefaultStringValue("Cannot ${action} ${type}. No disks have been specified.")
    String ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED();

    @DefaultStringValue("Cannot ${action} ${type}. The selected disk is not a template disk. Only template disks can be copied.")
    String ACTION_TYPE_FAILED_DISK_IS_NOT_TEMPLATE_DISK();

    @DefaultStringValue("Cannot ${action} ${type}. The source and target storage domains are the same.")
    String ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME();

    @DefaultStringValue("Cannot ${action} ${type}. Template disks cannot be moved.")
    String ACTION_TYPE_FAILED_CANNOT_MOVE_TEMPLATE_DISK();

    @DefaultStringValue("Cannot ${action} ${type}. Base Template does not exist for this Template Version.")
    String ACTION_TYPE_FAILED_BASE_TEMPLATE_DOES_NOT_EXIST();

    @DefaultStringValue("Failed ${action} ${type}. The following networks (${networks}) are not defined as VM networks.")
    String ACTION_TYPE_FAILED_NOT_A_VM_NETWORK();

    @DefaultStringValue("Failed ${action} ${type}. The following networks (${networks}) are not defined in the cluster.")
    String ACTION_TYPE_FAILED_NETWORK_NOT_IN_CLUSTER();

    @DefaultStringValue("Failed ${action} ${type}. One or more network interfaces have incomplete network configuration. Please configure these interfaces and try again.")
    String ACTION_TYPE_FAILED_INTERFACE_NETWORK_NOT_CONFIGURED();

    @DefaultStringValue("Non-VM networks are not supported in this Data-Center.")
    String NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL();

    @DefaultStringValue("Disk description must be formed only from alpha-numeric characters and special characters that conform to the standard ASCII character set.")
    String VALIDATION_DISK_DESCRIPTION_INVALID();

    @DefaultStringValue("Snapshot description must be formed only from alpha-numeric characters and special characters that conform to the standard ASCII character set.")
    String VALIDATION_DISK_IMAGE_DESCRIPTION_INVALID();

    @DefaultStringValue("VFS type cannot be empty")
    String VALIDATION_STORAGE_CONNECTION_EMPTY_VFSTYPE();

    @DefaultStringValue("Cannot ${action} ${type}. Custom mount options contain the following managed options: ${invalidOptions}.")
    String VALIDATION_STORAGE_CONNECTION_MOUNT_OPTIONS_CONTAINS_MANAGED_PROPERTY();

    @DefaultStringValue("Target details cannot be empty.")
    String VALIDATION_STORAGE_CONNECTION_EMPTY_IQN();

    @DefaultStringValue("${fieldName} field cannot be empty.")
    String VALIDATION_STORAGE_CONNECTION_EMPTY_CONNECTION();

    @DefaultStringValue("Mount path is illegal, please use [IP:/path or FQDN:/path] convention.")
    String VALIDATION_STORAGE_CONNECTION_INVALID();

    @DefaultStringValue("Invalid value for port, should be an integer greater than 0.")
    String VALIDATION_STORAGE_CONNECTION_INVALID_PORT();

    @DefaultStringValue("NFS Retransmissions should be between 0 and 32767")
    String VALIDATION_STORAGE_CONNECTION_NFS_RETRANS();

    @DefaultStringValue("NFS Timeout should be between 1 and 6000")
    String VALIDATION_STORAGE_CONNECTION_NFS_TIMEO();

    String VMPAYLOAD_INVALID_PAYLOAD_TYPE();

    String VMPAYLOAD_SIZE_EXCEEDED();

    String VMPAYLOAD_FLOPPY_EXCEEDED();

    @DefaultStringValue("Payload floppy deivce cannot be used with Sysprep via floppy device.")
    String VMPAYLOAD_FLOPPY_WITH_SYSPREP();

    @DefaultStringValue("Payload cdrom deivce cannot be used with Cloud-Init via cdrom device.")
    String VMPAYLOAD_CDROM_WITH_CLOUD_INIT();

    // Gluster Messages
    @DefaultStringValue("Cannot ${action} ${type}. Cluster ID is not valid.")
    String ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID();

    @DefaultStringValue("Cannot ${action} ${type}. Cluster does not support Gluster service.")
    String ACTION_TYPE_FAILED_CLUSTER_DOES_NOT_SUPPORT_GLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. Volume name ${volumeName} already exists.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_NAME_ALREADY_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster Volume is not valid.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. Bricks are required.")
    String ACTION_TYPE_FAILED_BRICKS_REQUIRED();

    @DefaultStringValue("Cannot ${action} ${type}. Adding bricks with 'Force' option is not supported.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_ADD_BRICK_FORCE_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Replica count must be > 2 for a REPLICATE volume.")
    String ACTION_TYPE_FAILED_REPLICA_COUNT_MIN_2();

    @DefaultStringValue("Cannot ${action} ${type}. Number of bricks must be a multiple of replica count for a DISTRIBUTED REPLICATE volume.")
    String ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_DISTRIBUTED_REPLICATE();

    @DefaultStringValue("Cannot ${action} ${type}. Number of bricks must be equal to the replica count for a REPLICATED volume.")
    String ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_REPLICATE();

    @DefaultStringValue("Cannot ${action} ${type}. Stripe count must be > 4 for a STRIPED volume.")
    String ACTION_TYPE_FAILED_STRIPE_COUNT_MIN_4();

    @DefaultStringValue("Cannot ${action} ${type}. Number of bricks must be equal to stripe count for a STRIPE volume.")
    String ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_STRIPE();

    @DefaultStringValue("Cannot ${action} ${type}. Number of bricks must be a multiple of stripe count for a DISTRIBUTED STRIPE volume.")
    String ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_DISTRIBUTED_STRIPE();

    @DefaultStringValue("Cannot ${action} ${type}. Number of bricks must be multiple of stripe count and replicate count for a STRIPED REPLICATE volume.")
    String ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_STRIPED_REPLICATE();

    @DefaultStringValue("Cannot ${action} ${type}. Number of bricks must be a non-trivial multiple of stripe count and replicate count for a DISTRIBUTED STRIPED REPLICATE volume.")
    String ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_DISTRIBUTED_STRIPED_REPLICATE();

    @DefaultStringValue("Cannot ${action} ${type}. Invalid host id in brick.")
    String ACTION_TYPE_FAILED_INVALID_BRICK_SERVER_ID();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster Volume ${volumeName} already started.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_ALREADY_STARTED();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster Volume ${volumeName} already stopped.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_ALREADY_STOPPED();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster Volume ${volumeName} is up.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_UP();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster Volume is down.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_DOWN();

    @DefaultStringValue("Cannot ${action} ${type}. Cannot remove all the bricks from a Volume.")
    String ACTION_TYPE_FAILED_CAN_NOT_REMOVE_ALL_BRICKS_FROM_VOLUME();

    @DefaultStringValue("Cannot ${action} ${type}. Remove brick not started.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_REMOVE_BRICKS_NOT_STARTED();

    @DefaultStringValue("Cannot ${action} ${type}. Invalid task type.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID_TASK_TYPE();

    @DefaultStringValue("Cannot ${action} ${type}. The list of bricks does not match with the bricks used while starting the action. Valid bricks: ${validBricks}")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_REMOVE_BRICKS_PARAMS_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. Remove brick not finished.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_REMOVE_BRICKS_NOT_FINISHED();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster Volume should be started.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_SHOULD_BE_STARTED();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster Volume is not distributed.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_NOT_DISTRIBUTED();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster Volume has a single brick.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_DISTRIBUTED_AND_HAS_SINGLE_BRICK();

    @DefaultStringValue("Cannot ${action} ${type}. Replacing brick is not a Gluster volume brick.")
    String ACTION_TYPE_FAILED_NOT_A_GLUSTER_VOLUME_BRICK();

    @DefaultStringValue("Cannot ${action} ${type}. Server having Gluster volume.")
    String VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME();

    @DefaultStringValue("Cannot ${action} ${type}. There is no available server in the cluster to probe the new server.")
    String ACTION_TYPE_FAILED_NO_GLUSTER_HOST_TO_PEER_PROBE();

    @DefaultStringValue("Cannot ${action} ${type}. Invalid gluster brick.")
    String ACTION_TYPE_FAILED_GLUSTER_BRICK_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. Brick ID is required.")
    String ACTION_TYPE_FAILED_BRICK_ID_REQUIRED();

    @DefaultStringValue("Cannot ${action} ${type}. One or more bricks are down.")
    String ACTION_TYPE_FAILED_ONE_OR_MORE_BRICKS_ARE_DOWN();

    @DefaultStringValue("Cannot ${action} ${type}. Replica count cannot be reduced by more than one.")
    String ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT_MORE_THAN_ONE();

    @DefaultStringValue("Cannot ${action} ${type}. Data migration is not needed while reducing the replica count.")
    String ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT_WITH_DATA_MIGRATION();

    @DefaultStringValue("Cannot ${action} ${type}. Replica count cannot be increased when removing bricks.")
    String ACTION_TYPE_FAILED_CAN_NOT_INCREASE_REPLICA_COUNT();

    @DefaultStringValue("Cannot ${action} ${type}. Replica count cannot be reduced when adding bricks.")
    String ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT();

    @DefaultStringValue("Cannot ${action} ${type}. Replica count cannot be increased by more than one.")
    String ACTION_TYPE_FAILED_CAN_NOT_INCREASE_REPLICA_COUNT_MORE_THAN_ONE();

    @DefaultStringValue("Cannot ${action} ${type}. Stripe count cannot be increased by more than one.")
    String ACTION_TYPE_FAILED_CAN_NOT_INCREASE_STRIPE_COUNT_MORE_THAN_ONE();

    @DefaultStringValue("Cannot ${action} ${type}. Stripe count can not be reduced.")
    String ACTION_TYPE_FAILED_CAN_NOT_REDUCE_STRIPE_COUNT();

    @DefaultStringValue("Cannot ${action} ${type}. Duplicate entries found for brick ${brick}.")
    String ACTION_TYPE_FAILED_DUPLICATE_BRICKS();

    @DefaultStringValue("Cannot ${action} ${type}. Brick ${brick} is already used by the volume ${volumeName}.")
    String ACTION_TYPE_FAILED_BRICK_ALREADY_EXISTS_IN_VOLUME();

    @DefaultStringValue("Cannot ${action} ${type}. No up server found in ${clusterName}.")
    String ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. A task is in progress on the volume ${volumeName} in cluster ${vdsGroup}.")
    String ACTION_TYPE_FAILED_VOLUME_OPERATION_IN_PROGRESS();

    @DefaultStringValue("Cannot ${action} ${type}. The image content could not be detected. Please try to re-import the image.")
    String ACTION_TYPE_FAILED_IMAGE_DOWNLOAD_ERROR();

    @DefaultStringValue("Cannot ${action} ${type}. Only bare RAW and QCOW2 image formats are supported.")
    String ACTION_TYPE_FAILED_IMAGE_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Either the image is missing, or its format is corrupted or unrecognizable.")
    String ACTION_TYPE_FAILED_IMAGE_UNRECOGNIZED();

    @DefaultStringValue("Cannot ${action} ${type}. Rebalance is not running on the volume ${volumeName} in cluster ${vdsGroup}.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_REBALANCE_NOT_STARTED();

    @DefaultStringValue("Cannot ${action} ${type}. Rebalance is running on the volume ${volumeName} in cluster ${vdsGroup}.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_CANNOT_STOP_REBALANCE_IN_PROGRESS();

    @DefaultStringValue("Cannot ${action} ${type}. Remove brick operation is running on the volume ${volumeName} in cluster ${vdsGroup}.")
    String ACTION_TYPE_FAILED_GLUSTER_VOLUME_CANNOT_STOP_REMOVE_BRICK_IN_PROGRESS();

    @DefaultStringValue("Cannot ${action} ${type}. The Network Interface ${IfaceName} has an invalid MAC address ${MacAddress}. MAC address must be in format \"HH:HH:HH:HH:HH:HH\" where H is a hexadecimal character (either a digit or A-F, case is insignificant).")
    String ACTION_TYPE_FAILED_NETWORK_INTERFACE_MAC_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. Tag ID is required.")
    String ACTION_TYPE_FAILED_TAG_ID_REQUIRED();

    @DefaultStringValue("Migrating a VM in paused status due to I/O error is not supported.")
    String MIGRATE_PAUSED_EIO_VM_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. The VM Network Interface does not exist.")
    String VM_INTERFACE_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The host network interface does not exist.")
    String HOST_NETWORK_INTERFACE_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. The VM Network Interface is plugged to a running VM.")
    String ACTION_TYPE_FAILED_CANNOT_REMOVE_ACTIVE_DEVICE();

    @DefaultStringValue("Cannot ${action} ${type}. Unregistered VM can not be collapsed.")
    String ACTION_TYPE_FAILED_IMPORT_UNREGISTERED_NOT_COLLAPSED();

    @DefaultStringValue("Cannot ${action} ${type}. Enabling both Virt and Gluster services is not allowed.")
    String VDS_GROUP_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED();

    @DefaultStringValue("Console connection denied. Another user has already accessed the console of this VM. The VM should be rebooted to allow another user to access it, or changed by an admin to not enforce reboot between users accessing its console.")
    String USER_CANNOT_FORCE_RECONNECT_TO_VM();

    @DefaultStringValue("Engine is running in Maintenance mode and is not accepting commands.")
    String ENGINE_IS_RUNNING_IN_MAINTENANCE_MODE();

    @DefaultStringValue("This action is not allowed when Engine is preparing for maintenance.")
    String ENGINE_IS_RUNNING_IN_PREPARE_MODE();

    @DefaultStringValue("$type External Event.")
    String VAR__TYPE__EXTERNAL_EVENT();

    @DefaultStringValue(".Cannot ${action} ${type}. Illegal Origin for External Event : oVirt")
    String ACTION_TYPE_FAILED_EXTERNAL_EVENT_ILLEGAL_ORIGIN();

    @DefaultStringValue(".Cannot ${action} ${type}.External Event does not exist.")
    String ACTION_TYPE_FAILED_EXTERNAL_EVENT_NOT_FOUND();

    @DefaultStringValue("$type External Job.")
    String VAR__TYPE__EXTERNAL_JOB();

    @DefaultStringValue("$type External Step.")
    String VAR__TYPE__EXTERNAL_STEP();

    @DefaultStringValue("$action End.")
    String VAR__ACTION__END();

    @DefaultStringValue("$action Clear.")
    String VAR__ACTION__CLEAR();

    @DefaultStringValue("Cannot ${action} ${type}. Description can not be empty.")
    String ACTION_TYPE_EMPTY_DESCRIPTION();

    @DefaultStringValue("Cannot ${action} ${type}. External steps can be added only to external jobs or steps.")
    String ACTION_TYPE_NOT_EXTERNAL();

    @DefaultStringValue("Cannot ${action} ${type}. Step should be a child of the Job or other Step.")
    String ACTION_TYPE_NO_PARENT();

    @DefaultStringValue("Cannot ${action} ${type}. Job not found.")
    String ACTION_TYPE_NO_JOB();

    @DefaultStringValue("Cannot ${action} ${type}. Step not found.")
    String ACTION_TYPE_NO_STEP();

    @DefaultStringValue("Cannot ${action} ${type}. Non monitored command.")
    String ACTION_TYPE_NON_MONITORED();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster hook id is required.")
    String ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster hook does not exist.")
    String ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST();

    @DefaultStringValue("One or more servers are already part of an existing cluster")
    String SERVER_ALREADY_EXISTS_IN_ANOTHER_CLUSTER();

    @DefaultStringValue("Server ${server} is already part of another cluster.")
    String SERVER_ALREADY_PART_OF_ANOTHER_CLUSTER();

    @DefaultStringValue("SSH Authentication failed. Please make sure password is correct.")
    String SSH_AUTHENTICATION_FAILED();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster service is not supported in compatibility version ${compatibilityVersion}.")
    String GLUSTER_NOT_SUPPORTED();

    @DefaultStringValue("Volume id is invalid.")
    String GLUSTER_VOLUME_ID_INVALID();

    @Constants.DefaultStringValue("Failed to run LDAP query, please check server logs for more info.")
    String FAILED_TO_RUN_LDAP_QUERY();

    @DefaultStringValue("Cannot ${action} ${type}. There are no conflicting servers to add or update hook.")
    String ACTION_TYPE_FAILED_GLUSTER_HOOK_NO_CONFLICT_SERVERS();

    @DefaultStringValue("Cannot ${action} ${type}. The server ${VdsName} is not UP.")
    String ACTION_TYPE_FAILED_SERVER_STATUS_NOT_UP();

    @DefaultStringValue("One or more servers in the cluster is down.")
    String CLUSTER_ALL_SERVERS_NOT_UP();

    @DefaultStringValue("Cannot ${action} ${type}. Gluster operation is in progress in cluster. Please try again.")
    String ACTION_TYPE_FAILED_GLUSTER_OPERATION_INPROGRESS();

    @DefaultStringValue("Cannot ${action} ${type}. All three values are needed in order to define QoS on each network directions.")
    String ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES();

    @DefaultStringValue("Cannot ${action} ${type}. Negative values are not allowed.")
    String ACTION_TYPE_FAILED_NETWORK_QOS_NEGATIVE_VALUES();

    @DefaultStringValue("Cannot ${action} ${type}. Values are out of range.")
    String ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES();

    @DefaultStringValue("Cannot ${action} ${type}. Duplicate QoS name in Data Center.")
    String ACTION_TYPE_FAILED_NETWORK_QOS_NAME_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. Invalid QoS.")
    String ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_DATA();

    @DefaultStringValue("Cannot ${action} ${type}. QoS entity not found.")
    String ACTION_TYPE_FAILED_NETWORK_QOS_NOT_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. Data Center does not contain the specific QoS entity.")
    String ACTION_TYPE_FAILED_NETWORK_QOS_INVALID_DC_ID();

    @DefaultStringValue("Cannot ${action} ${type}. Peak cannot be set lower than Average.")
    String ACTION_TYPE_FAILED_NETWORK_QOS_PEAK_LOWER_THAN_AVERAGE();

    @DefaultStringValue("Cannot ${action} ${type}. All three QoS parameters are required to configure QoS in a certain direction, but the following network(s) are missing some of them: ${ACTION_TYPE_FAILED_HOST_NETWORK_QOS_MISSING_VALUES_LIST}.")
    String ACTION_TYPE_FAILED_HOST_NETWORK_QOS_MISSING_VALUES();

    @DefaultStringValue("Cannot ${action} ${type}. QoS cannot be configured such that Peak is set lower than Average, but it was configured so on the following network(s): ${ACTION_TYPE_FAILED_HOST_NETWORK_QOS_PEAK_LOWER_THAN_AVERAGE_LIST}.")
    String ACTION_TYPE_FAILED_HOST_NETWORK_QOS_PEAK_LOWER_THAN_AVERAGE();

    @DefaultStringValue("Cannot ${action} ${type}. Values are out of range.")
    String ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES();

    @DefaultStringValue("Cannot ${action} ${type}. Invalid data center")
    String ACTION_TYPE_FAILED_QOS_STORAGE_POOL_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. Cannot change QoS data center.")
    String ACTION_TYPE_FAILED_QOS_STORAGE_POOL_NOT_CONSISTENT();

    @DefaultStringValue("QoS name cannot be empty.")
    String QOS_NAME_NOT_NULL();

    @DefaultStringValue("Invalid QoS name (name must be formed of \"a-z0-9A-Z\" or \"-_ \")")
    String QOS_NAME_INVALID();

    @DefaultStringValue("QoS name length must be under 50 characters.")
    String QOS_NAME_TOO_LONG();

    @DefaultStringValue("Cannot ${action} ${type}. QoS element has missing values.")
    String ACTION_TYPE_FAILED_QOS_MISSING_VALUES();

    @DefaultStringValue("Cannot ${action} ${type}. QoS element cannot have negative values.")
    String ACTION_TYPE_FAILED_QOS_NEGATIVE_VALUES();

    @DefaultStringValue("Cannot ${action} ${type}. QoS element name already exists.")
    String ACTION_TYPE_FAILED_QOS_NAME_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. QoS element not found.")
    String ACTION_TYPE_FAILED_QOS_NOT_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. Profile isn't provided.")
    String ACTION_TYPE_FAILED_PROFILE_MISSING();

    @DefaultStringValue("Cannot ${action} ${type}. Profile not exists.")
    String ACTION_TYPE_FAILED_PROFILE_NOT_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. Profile name is in use.")
    String ACTION_TYPE_FAILED_PROFILE_NAME_IN_USE();

    @DefaultStringValue("Cannot ${action} ${type}. Cannot change profile.")
    String ACTION_TYPE_FAILED_CANNOT_CHANGE_PROFILE();

    @DefaultStringValue("Cannot ${action} ${type}. Profile is in use.")
    String ACTION_TYPE_FAILED_PROFILE_IN_USE();

    @DefaultStringValue("Cannot ${action} ${type}. Disk Profile wasn't found.")
    String ACTION_TYPE_FAILED_DISK_PROFILE_NOT_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. Storage Domain wasn't provided.")
    String ACTION_TYPE_DISK_PROFILE_STORAGE_DOMAIN_NOT_PROVIDED();

    @DefaultStringValue("Cannot ${action} ${type}. Disk Profile doesn't match provided Storage Domain.")
    String ACTION_TYPE_DISK_PROFILE_NOT_MATCH_STORAGE_DOMAIN();

    @DefaultStringValue("Cannot ${action} ${type}. CPU Profile doesn't match provided Cluster.")
    String ACTION_TYPE_CPU_PROFILE_NOT_MATCH_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. Cannot remove last CPU profile in Cluster.")
    String ACTION_TYPE_CANNOT_REMOVE_LAST_CPU_PROFILE_IN_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. Cannot remove last Disk profile in Storage Domain.")
    String ACTION_TYPE_CANNOT_REMOVE_LAST_DISK_PROFILE_IN_STORAGE_DOMAIN();

    @DefaultStringValue("Cannot ${action}. New disk size cannot be smaller than the current.")
    String ACTION_TYPE_FAILED_REQUESTED_DISK_SIZE_IS_TOO_SMALL();

    @DefaultStringValue("Cannot ${action} ${type}. Read-only disk cannot be resized.")
    String ACTION_TYPE_FAILED_CANNOT_RESIZE_READ_ONLY_DISK();

    @DefaultStringValue("Cannot ${action} ${type}. Disk snapshot cannot be resized.")
    String ACTION_TYPE_FAILED_CANNOT_RESIZE_DISK_SNAPSHOT();

    @DefaultStringValue("Cannot ${action}. The selected disk format is not supported.")
    String ACTION_TYPE_FAILED_NOT_SUPPORTED_IMAGE_FORMAT();

    @DefaultStringValue("$action extend image size")
    String VAR__ACTION__EXTEND_IMAGE_SIZE();

    @DefaultStringValue("Cannot ${action} ${type}. Parameters are invalid.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_PARAMETERS_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. policy unit already exists in Scheduling Policy.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_DUPLICATE_POLICY_UNIT();

    @DefaultStringValue("Cannot ${action} ${type}. Name is in use.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_NAME_INUSE();

    @DefaultStringValue("Cannot ${action} ${type}. Scheduling Policy is locked, and cannot be editable.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_LOCKED();

    @DefaultStringValue("Cannot ${action} ${type}. Scheduling Policy is attached to cluster(s), please assign these cluster(s) to other policy.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_INUSE();

    @DefaultStringValue("Cannot ${action} ${type}. Policy unit is unknown.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_UNKNOWN_POLICY_UNIT();

    @DefaultStringValue("Cannot ${action} ${type}. Policy unit doesn't implement Filtering.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_FILTER_NOT_IMPLEMENTED();

    @DefaultStringValue("Cannot ${action} ${type}. Policy unit doesn't implement score function.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_FUNCTION_NOT_IMPLEMENTED();

    @DefaultStringValue("Cannot ${action} ${type}. Policy unit doesn't implement load balancing logic.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_BALANCE_NOT_IMPLEMENTED();

    @DefaultStringValue("Cannot ${action} ${type}. Only a single filter can be selected as first.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_ONLY_ONE_FILTER_CAN_BE_FIRST();

    @DefaultStringValue("Cannot ${action} ${type}. Only a single filter can be selected as last.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_ONLY_ONE_FILTER_CAN_BE_LAST();

    @DefaultStringValue("Cannot ${action} ${type}. Function factor cannot be negative.")
    String ACTION_TYPE_FAILED_CLUSTER_POLICY_FUNCTION_FACTOR_NEGATIVE();

    @DefaultStringValue("Cannot ${action} ${type}. Policy unit is attached to the following cluster policies: ${cpNames}.")
    String ACTION_TYPE_FAILED_CANNOT_REMOVE_POLICY_UNIT_ATTACHED_TO_CLUSTER_POLICY();

    @DefaultStringValue("Cannot ${action} ${type}. Cloud-Init is only supported on cluster compatibility version 3.3 and higher.")
    String ACTION_TYPE_FAILED_CLOUD_INIT_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Alignment scan of a disk attached to a running VM is only supported with RAW virtual disks.")
    String ERROR_CANNOT_RUN_ALIGNMENT_SCAN_VM_IS_RUNNING();

    @DefaultStringValue("Cannot ${action} ${type}. Alignment scan is only supported for disks located on block storage domains.")
    String ACTION_TYPE_FAILED_ALIGNMENT_SCAN_STORAGE_TYPE();

    @DefaultStringValue("Cannot ${action} ${type}. Invalid time zone for given OS type.")
    String ACTION_TYPE_FAILED_INVALID_TIMEZONE();

    @DefaultStringValue("Cannot ${action} ${type}. Action type cannot be empty.")
    String ACTION_TYPE_EMPTY_ACTION_TYPE();

    @DefaultStringValue("Cannot get list of images from Storage Domain '${sdName}'. Please try again later.")
    String ERROR_GET_IMAGE_LIST();

    @DefaultStringValue("Cannot ${action} ${type}. The following disk(s) are not attached to any VM: ${diskAliases}.")
    String ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK();

    @DefaultStringValue("Cannot ${action} ${type}. Disk ${DiskAlias} is being exported.")
    String ACTION_TYPE_FAILED_DISK_IS_BEING_EXPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Disk ${DiskAlias} alignment is currently being determined.")
    String ACTION_TYPE_FAILED_DISK_IS_USED_BY_GET_ALIGNMENT();

    @DefaultStringValue("$filterType external")
    String VAR__FILTERTYPE__INTERNAL();

    @DefaultStringValue("$filterType internal")
    String VAR__FILTERTYPE__EXTERNAL();

    @DefaultStringValue("The host ${hostName} did not satisfy ${filterType} filter ${filterName}.")
    String SCHEDULING_HOST_FILTERED_REASON();

    @DefaultStringValue("Cannot ${action} ${type}. There is no host that satisfies current scheduling constraints. See below for details:")
    String SCHEDULING_ALL_HOSTS_FILTERED_OUT();

    @DefaultStringValue("The host ${hostName} did not satisfy ${filterType} filter ${filterName} because ${detailMessage}.")
    String SCHEDULING_HOST_FILTERED_REASON_WITH_DETAIL();

    @DefaultStringValue("$detailMessage network(s) ${networkNames} are missing")
    String VAR__DETAIL__NETWORK_MISSING();

    @DefaultStringValue("$detailMessage its CPU level ${hostCPULevel} is lower than the VM requires ${vmCPULevel}")
    String VAR__DETAIL__LOW_CPU_LEVEL();

    @DefaultStringValue("$detailMessage it did not match positive affinity rules ${affinityRules}")
    String VAR__DETAIL__AFFINITY_FAILED_POSITIVE();

    @DefaultStringValue("$detailMessage it matched negative affinity rules ${affinityRules}")
    String VAR__DETAIL__AFFINITY_FAILED_NEGATIVE();

    @DefaultStringValue("$detailMessage its swap value was illegal")
    String VAR__DETAIL__SWAP_VALUE_ILLEGAL();

    @DefaultStringValue("$detailMessage it has insufficient free memory to run the VM")
    String VAR__DETAIL__NOT_ENOUGH_MEMORY();

    @DefaultStringValue("$detailMessage cannot accommodate memory of VM's pinned virtual NUMA nodes within host's physical NUMA nodes.")
    String VAR__DETAIL__NOT_MEMORY_PINNED_NUMA();

    @DefaultStringValue("$detailMessage it has insufficient CPU cores to run the VM")
    String VAR__DETAIL__NOT_ENOUGH_CORES();

    @DefaultStringValue("$detailMessage it has insufficient NUMA node free memory to run the VM")
    String VAR__DETAIL__NUMA_PINNING_FAILED();

    @DefaultStringValue("Preferred NUMA tune mode is allowed for a single pinned Virtual NUMA Node.")
    String VM_NUMA_NODE_PREFERRED_NOT_PINNED_TO_SINGLE_NODE();

    @DefaultStringValue("$detailMessage it is not a Hosted Engine host.")
    String VAR__DETAIL__NOT_HE_HOST();

    @DefaultStringValue("There are no hosts to use. Check that the cluster contains at least one host in Up state.")
    String SCHEDULING_NO_HOSTS();

    @DefaultStringValue("Cannot perform ${action}. Another power management action is already in progress.")
    String POWER_MANAGEMENT_ACTION_ON_ENTITY_ALREADY_IN_PROGRESS();

    @DefaultStringValue("A labeled network interface could not be found.")
    String LABELED_NETWORK_INTERFACE_NOT_FOUND();

    @DefaultStringValue("The networks represented by label cannot be configured on the same network interface.")
    String NETWORK_LABEL_CONFLICT();

    @DefaultStringValue("Cannot ${action} ${type}. Name field input is too long.")
    String AFFINITY_GROUP_NAME_TOO_LONG();

    @DefaultStringValue("Cannot ${action} ${type}. Name field is invalid.")
    String AFFINITY_GROUP_NAME_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. Description field is invalid.")
    String AFFINITY_GROUP_DESCRIPTION_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. Cluster is empty.")
    String ACTION_TYPE_FAILED_AFFINITY_GROUP_INVALID_CLUSTER_ID();

    @DefaultStringValue("Cannot ${action} ${type}. Provided Cluster is invalid.")
    String ACTION_TYPE_FAILED_INVALID_CLUSTER_FOR_AFFINITY_GROUP();

    @DefaultStringValue("Cannot ${action} ${type}. Provided VM is invalid (does not exist).")
    String ACTION_TYPE_FAILED_INVALID_VM_FOR_AFFINITY_GROUP();

    @DefaultStringValue("Cannot ${action} ${type}. Provided VM resides in another Cluster.")
    String ACTION_TYPE_FAILED_VM_NOT_IN_AFFINITY_GROUP_CLUSTER();

    @DefaultStringValue("Cannot ${action} ${type}. Duplicate VM.")
    String ACTION_TYPE_FAILED_DUPLICTE_VM_IN_AFFINITY_GROUP();

    @DefaultStringValue("Cannot ${action} ${type}. No matching Affinity Group.")
    String ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID();

    @DefaultStringValue("Cannot ${action} ${type}. Cannot change Cluster for Affinity Group.")
    String ACTION_TYPE_FAILED_CANNOT_CHANGE_CLUSTER_ID();

    @DefaultStringValue("Cannot ${action} ${type}. Affinity Group name already exists.")
    String ACTION_TYPE_FAILED_AFFINITY_GROUP_NAME_EXISTS();

    @DefaultStringValue("VM is associated with a positive Affinity Group (${affinityGroupName}) and require to run on the same Host (${hostName}) as the other group VMs")
    String ACTION_TYPE_FAILED_POSITIVE_AFFINITY_GROUP();

    @DefaultStringValue("VM is associated with a negative Affinity Group and require to run on separate Host, which doesn't run other group VMs")
    String ACTION_TYPE_FAILED_NEGATIVE_AFFINITY_GROUP();

    @DefaultStringValue("VM is associated with both positive and negative Affinity Groups, please reconfigure VM's affinity groups")
    String ACTION_TYPE_FAILED_MIX_POSITIVE_NEGATIVE_AFFINITY_GROUP();

    @DefaultStringValue("iSCSI bond name must not exceed 50 characters")
    String VALIDATION_ISCSI_BOND_NAME_MAX();

    @DefaultStringValue("iSCSI bond name is required")
    String VALIDATION_ISCSI_BOND_NAME_NOT_NULL();

    @DefaultStringValue("iSCSI bond description must not exceed 4000 characters")
    String VALIDATION_ISCSI_BOND_DESCRIPTION_MAX();

    @DefaultStringValue("iSCSI bond name must be formed from alpha-numeric characters, periods (.), hyphens (-), and underscores (_).")
    String VALIDATION_ISCSI_BOND_NAME_INVALID_CHARACTER();

    @DefaultStringValue("iSCSI bond description must be formed only from alpha-numeric characters and special characters that conform to the standard ASCII character set.")
    String VALIDATION_SCSI_BOND_DESCRIPTION_INVALID();

    @DefaultStringValue("Cannot ${action} ${type}. iSCSI bond with the same name already exists in the Data Center.")
    String ISCSI_BOND_WITH_SAME_NAME_EXIST_IN_DATA_CENTER();

    @DefaultStringValue("Cannot ${action} ${type}. The specified iSCSI bond does not exist.")
    String ISCSI_BOND_NOT_EXIST();

    @DefaultStringValue("Cannot ${action} ${type}. iSCSI Bond is only supported on Data Center compatibility versions 3.4 and higher.")
    String ISCSI_BOND_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. An IDE disk can't be read-only.")
    String ACTION_TYPE_FAILED_IDE_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR();

    @DefaultStringValue("Cannot ${action} ${type}. Custom serial number must be non-empty when \"Custom\" serial number policy is specified.")
    String ACTION_TYPE_FAILED_INVALID_SERIAL_NUMBER();

    @DefaultStringValue("Cannot ${action} ${type}. The following storage connections ${connectionIds} cannot be added to the specified iSCSI bond.\n"
            +
            "Possible reasons:\n" +
            "- They are not of type iSCSI.\n" +
            "- Their status is not one of the following: Unknown, Active. Inactive.\n" +
            "- They already exist in the iSCSI bond.\n" +
            "- They do not belong to the same Data Center as the specified iSCSI bond.\n")
    String ACTION_TYPE_FAILED_STORAGE_CONNECTIONS_CANNOT_BE_ADDED_TO_ISCSI_BOND();

    @DefaultStringValue("Cannot ${action} ${type}. Required network cannot be a part of an iSCSI bond.")
    String ACTION_TYPE_FAILED_ISCSI_BOND_NETWORK_CANNOT_BE_REQUIRED();

    @DefaultStringValue("Cannot ${action} ${type}. ${interface} disks can't be read-only.")
    String ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR();

    @DefaultStringValue("Cannot ${action} ${type}. Trying to manipulate with Random Number Generator device but none is found.")
    String ACTION_TYPE_FAILED_RNG_NOT_FOUND();

    @DefaultStringValue("Cannot ${action} ${type}. Random Number Generator device already exists.")
    String ACTION_TYPE_FAILED_RNG_ALREADY_EXISTS();

    @DefaultStringValue("Cannot ${action} ${type}. Random Number Generator device is not supported in cluster.")
    String ACTION_TYPE_FAILED_RNG_SOURCE_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Operation not supported by QEMU.")
    String ACTION_TYPE_FAILED_QEMU_UNSUPPORTED_OPERATION();

    @DefaultStringValue("Cannot ${action} ${type}. Memory size exceeds supported limit for given cluster version.")
    String ACTION_TYPE_FAILED_MEMORY_EXCEEDS_SUPPORTED_LIMIT();

    @DefaultStringValue("Cannot ${action} ${type}. Kdump detection support is not enabled for host '${VdsName}'.")
    String KDUMP_DETECTION_NOT_ENABLED_FOR_VDS();

    @DefaultStringValue("Cannot ${action} ${type}. Kdump detection support is not properly configured on host '${VdsName}'.")
    String KDUMP_DETECTION_NOT_CONFIGURED_ON_VDS();

    @DefaultStringValue("Cannot ${action} ${type}. Target cluster belongs to different Data Center.")
    String VDS_CLUSTER_ON_DIFFERENT_STORAGE_POOL();

    @DefaultStringValue("Cannot ${action} ${type}. Plugged and unlinked VM network interface with external network is not supported.")
    String PLUGGED_UNLINKED_VM_INTERFACE_WITH_EXTERNAL_NETWORK_IS_NOT_SUPPORTED();

    @DefaultStringValue("Cannot ${action} ${type}. Balloon is not supported on '${clusterArch}' architecture.")
    String BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH();

    @DefaultStringValue("The guest OS doesn't support the following CPUs: ${unsupportedCpus}. Its possible to change the cluster cpu or set a different one per VM")
    String CPU_TYPE_UNSUPPORTED_FOR_THE_GUEST_OS();

    @DefaultStringValue("Cannot ${action} ${type}. This operation is not supported for external networks.")
    String ACTION_TYPE_FAILED_NOT_SUPPORTED_FOR_EXTERNAL_NETWORK();

    @DefaultStringValue("Cannot ${action} ${type}. Change of Data Center ID is not allowed.")
    String ACTION_TYPE_FAILED_DATA_CENTER_ID_CANNOT_BE_CHANGED();

    @DefaultStringValue("CIDR not represnting a network address.\nplease ensure IP and mask are matching to network IP address. \nexample:\n\tvalid network address: 2.2.0.0/16\n\tinvalid: 2.2.0.1/16")
    String CIDR_NOT_NETWORK_ADDRESS();

    @DefaultStringValue("CIDR bad format, expected:\n x.x.x.x/y  where:\n x belongs to [0,255] \n y belongs to [0,32] \n both inclusive")
    String BAD_CIDR_FORMAT();
}

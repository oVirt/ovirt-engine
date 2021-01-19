package org.ovirt.engine.core.common.businessentities;

public final class BusinessEntitiesDefinitions {

    // Data Center (storage_pool)
    public static final int DATACENTER_NAME_SIZE = 40;

    // CLUSTER
    public static final int CLUSTER_NAME_SIZE = 40;
    public static final int CLUSTER_CPU_NAME_SIZE = 255;

    // VM (vm_statis)
    public static final int VM_NAME_SIZE = 255;
    public static final int VM_DESCRIPTION_SIZE = 255;
    public static final int VM_EMULATED_MACHINE_SIZE = 30;
    public static final int VM_CPU_NAME_SIZE = 255;
    public static final int VM_SERIAL_NUMBER_SIZE = 255;

    // VM Pools (vm_pools)
    public static final int VM_POOL_NAME_SIZE = 255;
    public static final int VM_POOL_PARAMS = 200;

    // Templates (vm_templates)
    public static final int VM_TEMPLATE_NAME_SIZE = 255;

    // HOST (vds_static)
    public static final int HOST_NAME_SIZE = 255;
    public static final int HOST_HOSTNAME_SIZE = 255;
    public static final int CONSOLE_ADDRESS_SIZE = 255;
    public static final int HOST_IP_SIZE = 255;
    public static final int HOST_UNIQUE_ID_SIZE = 128;
    public static final int HOST_PM_USER_SIZE = 50;
    public static final int HOST_PM_PASSWD_SIZE = 50;
    public static final int HOST_PM_TYPE_SIZE = 20;
    public static final int HOST_MIN_SPM_PRIORITY = -1;
    public static final int HOST_MAX_SPM_PRIORITY = 10;
    public static final int SSH_KEY_FINGERPRINT_SIZE = 128;
    public static final int SSH_PUBLIC_KEY_SIZE = 8192;

    // Network Interface
    public static final int NETWORK_NAME_SIZE = 256;
    public static final int NETWORK_INTERFACE_NAME_SIZE = 50;
    public static final int BOND_NAME_SIZE = 50;
    public static final int NETWORK_QOS_NAME_SIZE = 50;
    public static final int VNIC_PROFILE_NAME_SIZE = 50;
    public static final int NETWORK_MIN_LEGAL_PORT = 1;
    public static final int NETWORK_MAX_LEGAL_PORT = 65535;
    public static final int HOST_NIC_NAME_LENGTH = 15;
    public static final int BOND_NAME_WARNING_LENGTH = 10;
    public static final String BOND_NAME_PREFIX = "bond";
    public static final String BOND_NAME_PATTERN = "^" + BOND_NAME_PREFIX + "\\w+$";
    public static final String NUM_ONLY_BOND_NAME_PATTERN = "^" + BOND_NAME_PREFIX + "\\d+$";

    // Profiles
    public static final int PROFILE_NAME_SIZE = 50;

    // Bookmark (bookmarks)
    public static final int BOOKMARK_NAME_SIZE = 40;
    public static final int BOOKMARK_VALUE_SIZE = 300;

    // Storage (storage_domain_static)
    public static final int STORAGE_SIZE = 250;
    public static final int STORAGE_NAME_SIZE = 250;

    // Disk (base_disks)
    public static final int DISK_DESCRIPTION_MAX_SIZE = 500;

    // LUNS (luns)
    public static final int LUN_PHYSICAL_VOLUME_ID = 50;
    public static final int LUN_ID = 255;
    public static final int LUN_VOLUME_GROUP_ID = 50;
    public static final int LUN_VENDOR_ID = 50;
    public static final int LUN_PRODUCT_ID = 50;
    public static final String DUMMY_LUN_ID_PREFIX = "DUMMY_LUN_";

    // Roles (roles)
    public static final int ROLE_NAME_SIZE = 126;

    // Tags (tags)
    public static final int TAG_NAME_SIZE = 50;

    // Quota
    public static final int QUOTA_NAME_SIZE = 65;
    public static final int QUOTA_DESCRIPTION_SIZE = 250;

    // Users (users)
    public static final int USER_GROUP_IDS_SIZE = 2048;
    public static final int USER_DEPARTMENT_SIZE = 255;
    public static final int USER_DOMAIN_SIZE = 255;
    public static final int USER_NAMESPACE_SIZE = 2048;
    public static final int USER_EMAIL_SIZE = 255;
    public static final int USER_FIRST_NAME_SIZE = 255;
    public static final int USER_NOTE_SIZE = 255;
    public static final int USER_ROLE_SIZE = 255;
    public static final int USER_LAST_NAME_SIZE = 255;
    public static final int USER_LOGIN_NAME_SIZE = 255;

    // General descriptions
    public static final int GENERAL_NETWORK_ADDR_SIZE = 50;
    public static final int MAX_SUPPORTED_DNS_CONFIGURATIONS = 3;
    public static final int GENERAL_SUBNET_SIZE = 20;
    public static final int GENERAL_GATEWAY_SIZE = 20;
    public static final int GENERAL_TIME_ZONE_SIZE = 40;
    public static final int GENERAL_DOMAIN_SIZE = 40;
    public static final int GENERAL_VERSION_SIZE = 40;
    public static final int GENERAL_MAX_SIZE = 4000;
    public static final int GENERAL_NAME_SIZE = 255;

    // Jobs
    public static final int CORRELATION_ID_SIZE = 50;

    // SPICE
    public static final int SPICE_PROXY_ADDR_SIZE = 255;

    // Providers
    public static final int PROVIDER_PASSWORD_MAX_SIZE = 200;
}

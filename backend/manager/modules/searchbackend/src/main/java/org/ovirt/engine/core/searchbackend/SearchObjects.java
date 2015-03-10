package org.ovirt.engine.core.searchbackend;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.utils.CommonConstants;

public final class SearchObjects {
    // objects
    public static final String VDS_OBJ_NAME = "HOST";
    public static final String VDS_PLU_OBJ_NAME = "HOSTS";
    public static final String VM_OBJ_NAME = "VM";
    public static final String VM_PLU_OBJ_NAME = "VMS";
    public static final String AUDIT_OBJ_NAME = "EVENT";
    public static final String AUDIT_PLU_OBJ_NAME = "EVENTS";
    public static final String AD_USER_OBJ_NAME = "ADUSER";
    public static final String AD_USER_PLU_OBJ_NAME = "ADUSERS";
    public static final String AD_GROUP_OBJ_NAME = "ADGROUP";
    public static final String AD_GROUP_PLU_OBJ_NAME = "ADGROUPS";
    public static final String TEMPLATE_OBJ_NAME = "TEMPLATE";
    public static final String TEMPLATE_PLU_OBJ_NAME = "TEMPLATES";
    public static final String VDC_USER_OBJ_NAME = "USER";
    public static final String VDC_USER_PLU_OBJ_NAME = "USERS";
    public static final String VDC_GROUP_OBJ_NAME = "GROUP";
    public static final String VDC_GROUP_PLU_OBJ_NAME = "GROUPS";
    public static final String VDC_POOL_OBJ_NAME = "POOL";
    public static final String VDC_POOL_PLU_OBJ_NAME = "POOLS";
    public static final String VDC_CLUSTER_OBJ_NAME = "CLUSTER";
    public static final String VDC_CLUSTER_PLU_OBJ_NAME = "CLUSTERS";
    public static final String VDC_STORAGE_POOL_OBJ_NAME = "DATACENTER";
    public static final String VDC_STORAGE_DOMAIN_OBJ_NAME = "STORAGE";
    public static final String VDC_STORAGE_DOMAIN_PLU_OBJ_NAME = "STORAGES";
    public static final String VDC_STORAGE_DOMAIN_IMAGE_OBJ_NAME = "VMIMAGE";
    public static final String DISK_OBJ_NAME = "DISK";
    public static final String DISK_PLU_OBJ_NAME = "DISKS";
    public static final String GLUSTER_VOLUME_OBJ_NAME = "VOLUME";
    public static final String GLUSTER_VOLUME_PLU_OBJ_NAME = "VOLUMES";
    public static final String QUOTA_OBJ_NAME = "QUOTA";
    public static final String QUOTA_PLU_OBJ_NAME = "QUOTAS";
    public static final String NETWORK_OBJ_NAME = "NETWORK";
    public static final String NETWORK_PLU_OBJ_NAME = "NETWORKS";
    public static final String VDS_NETWORK_INTERFACE_OBJ_NAME = "NIC";
    public static final String VM_NETWORK_INTERFACE_OBJ_NAME = "VNIC";
    public static final String NETWORK_CLUSTER_OBJ_NAME = "CLUSTER_NETWORK";
    public static final String NETWORK_HOST_OBJ_NAME = "HOST_NETWORK";
    public static final String PROVIDER_OBJ_NAME = "PROVIDER";
    public static final String PROVIDER_PLU_OBJ_NAME = "PROVIDERS";
    public static final String INSTANCE_TYPE_OBJ_NAME = "INSTANCETYPE";
    public static final String INSTANCE_TYPE_PLU_OBJ_NAME = "INSTANCETYPES";
    public static final String IMAGE_TYPE_OBJ_NAME = "IMAGETYPE";
    public static final String IMAGE_TYPE_PLU_OBJ_NAME = "IMAGETYPES";
    public static final String SESSION_OBJ_NAME = "SESSION";
    public static final String SESSION_PLU_OBJ_NAME = "SESSIONS";

    // Special fields that require tag based search
    public static final String VDC_USER_ROLE_SEARCH = SearchObjects.VDC_USER_OBJ_NAME + "-ROLE";

    //special searches
    private static final String ALERT = "EVENTS: severity=alert";
    private static final String ERROR = "Events: severity=error";
    private static final String HOST_BY_CPU = "Host: sortby cpu_usage desc";
    private static final String DATACENTER_BY_NAME = "DataCenter: sortby name";
    private static final String VM_BY_STATUS = "Vms: status=Up or status=PoweringUp or status=MigratingTo or status=WaitForLaunch or status=RebootInProgress or status=PoweringDown or status=Paused or status=Unknown sortby cpu_usage desc";

    private static final Set<String> SAFE_SEARCH_EXPR = Collections.unmodifiableSet(new HashSet<String>() {
        {
            final char SEPARATOR = ':';

            add(getName(VDS_OBJ_NAME));
            add(getName(VDS_PLU_OBJ_NAME));
            add(getName(VM_OBJ_NAME));
            add(getName(VM_PLU_OBJ_NAME));
            add(getName(AUDIT_OBJ_NAME));
            add(getName(AUDIT_PLU_OBJ_NAME));
            add(getName(TEMPLATE_OBJ_NAME));
            add(getName(TEMPLATE_PLU_OBJ_NAME));
            add(getName(VDC_USER_OBJ_NAME));
            add(getName(VDC_USER_PLU_OBJ_NAME));
            add(getName(VDC_GROUP_OBJ_NAME));
            add(getName(VDC_GROUP_PLU_OBJ_NAME));
            add(getName(VDC_POOL_OBJ_NAME));
            add(getName(VDC_POOL_PLU_OBJ_NAME));
            add(getName(VDC_CLUSTER_OBJ_NAME));
            add(getName(VDC_CLUSTER_PLU_OBJ_NAME));
            add(getName(VDC_STORAGE_POOL_OBJ_NAME));
            add(getName(VDC_STORAGE_DOMAIN_OBJ_NAME));
            add(getName(VDC_STORAGE_DOMAIN_PLU_OBJ_NAME));
            add(getName(VDC_STORAGE_DOMAIN_IMAGE_OBJ_NAME));
            add(getName(DISK_OBJ_NAME));
            add(getName(DISK_PLU_OBJ_NAME));
            add(getName(GLUSTER_VOLUME_OBJ_NAME));
            add(getName(GLUSTER_VOLUME_PLU_OBJ_NAME));
            add(getName(QUOTA_OBJ_NAME));
            add(getName(QUOTA_PLU_OBJ_NAME));
            add(getName(NETWORK_OBJ_NAME));
            add(getName(NETWORK_PLU_OBJ_NAME));
            add(getName(PROVIDER_OBJ_NAME));
            add(getName(PROVIDER_PLU_OBJ_NAME));
            add(getName(ALERT));
            add(getName(ERROR));
            add(getName(HOST_BY_CPU));
            add(getName(DATACENTER_BY_NAME));
            add(getName(VM_BY_STATUS));
            add(getName(INSTANCE_TYPE_OBJ_NAME));
            add(getName(INSTANCE_TYPE_PLU_OBJ_NAME));
            add(getName(IMAGE_TYPE_OBJ_NAME));
            add(getName(IMAGE_TYPE_PLU_OBJ_NAME));
            add(getName(SESSION_OBJ_NAME));
            add(getName(SESSION_PLU_OBJ_NAME));
        }
    });

    private static String getName(String name) {
        return name.toLowerCase() + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR;
    }


    /**
     * Determines if a search expression is safe.
     * @param expr the expression to check.
     * @return boolean
     */
    public static boolean isSafeExpression(String expr) {
        return SAFE_SEARCH_EXPR.contains(expr.toLowerCase());
    }
}

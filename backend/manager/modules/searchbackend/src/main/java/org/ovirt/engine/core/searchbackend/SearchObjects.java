package org.ovirt.engine.core.searchbackend;

import java.util.HashSet;

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
    public static final String VDC_POOL_OBJ_NAME = "POOL";
    public static final String VDC_POOL_PLU_OBJ_NAME = "POOLS";
    public static final String VDC_CLUSTER_OBJ_NAME = "CLUSTER";
    public static final String VDC_CLUSTER_PLU_OBJ_NAME = "CLUSTERS";
    public static final String VDC_STORAGE_POOL_OBJ_NAME = "DATACENTER";
    public static final String VDC_STORAGE_DOMAIN_OBJ_NAME = "STORAGE";
    public static final String DISK_IMAGE_OBJ_NAME = "DISK";
    public static final String DISK_IMAGE_PLU_OBJ_NAME = "DISKS";
    public static final String GLUSTER_VOLUME_OBJ_NAME = "VOLUME";
    public static final String GLUSTER_VOLUME_PLU_OBJ_NAME = "VOLUMES";
    //special searches
    private static final String ALERT = "EVENTS: severity=alert";
    private static final String ERROR = "Events: severity=error";
    private static final String HOST_BY_CPU = "Host: sortby cpu_usage desc";
    private static final String DATACENTER_BY_NAME = "DataCenter: sortby name";
    private static final String VM_BY_STATUS = "Vms: status=Up or status=PoweringUp or status=PoweredDown or status=MigratingTo or status=WaitForLaunch or status=RebootInProgress or status=PoweringDown or status=Paused or status=Unknown sortby cpu_usage desc";
    private static final HashSet<String> SAFE_SEARCH_EXPR = new  HashSet<String>();

    private static void init() {
        final char SEPERATOR = ':';

        SAFE_SEARCH_EXPR.add(VDS_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VDS_PLU_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VM_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VM_PLU_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(AUDIT_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(AUDIT_PLU_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(TEMPLATE_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(TEMPLATE_PLU_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VDC_USER_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VDC_USER_PLU_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VDC_POOL_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VDC_POOL_PLU_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VDC_CLUSTER_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VDC_CLUSTER_PLU_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VDC_STORAGE_POOL_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(VDC_STORAGE_DOMAIN_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(DISK_IMAGE_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(DISK_IMAGE_PLU_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(GLUSTER_VOLUME_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(GLUSTER_VOLUME_PLU_OBJ_NAME.toLowerCase() + SEPERATOR);
        SAFE_SEARCH_EXPR.add(ALERT.toLowerCase());
        SAFE_SEARCH_EXPR.add(ERROR.toLowerCase());
        SAFE_SEARCH_EXPR.add(HOST_BY_CPU.toLowerCase());
        SAFE_SEARCH_EXPR.add(DATACENTER_BY_NAME.toLowerCase());
        SAFE_SEARCH_EXPR.add(VM_BY_STATUS.toLowerCase());
    }

    /**
     * Determines if a search expression is safe.
     * @param expr the expression to check.
     * @return boolean
     */
    public static boolean isSafeExpression(String expr) {
        if (SAFE_SEARCH_EXPR.size() == 0) {
            init();
        }
        return SAFE_SEARCH_EXPR.contains(expr.toLowerCase());
    }
}

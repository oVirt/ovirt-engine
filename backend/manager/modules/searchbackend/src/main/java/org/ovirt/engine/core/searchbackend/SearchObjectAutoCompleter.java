package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.gluster.GlusterVolumeConditionFieldAutoCompleter;
import org.ovirt.engine.core.searchbackend.gluster.GlusterVolumeCrossRefAutoCompleter;

public class SearchObjectAutoCompleter extends SearchObjectsBaseAutoCompleter {
    private final java.util.HashMap<String, String[]> mJoinDictionary = new java.util.HashMap<String, String[]>();

    public SearchObjectAutoCompleter(boolean isDesktopsAllowed) {

        mVerbs.put(SearchObjects.VM_PLU_OBJ_NAME, SearchObjects.VM_PLU_OBJ_NAME);
        if (isDesktopsAllowed) {
            mVerbs.put(SearchObjects.VDC_POOL_PLU_OBJ_NAME, SearchObjects.VDC_POOL_PLU_OBJ_NAME);
        }
        mVerbs.put(SearchObjects.VDS_PLU_OBJ_NAME, SearchObjects.VDS_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.TEMPLATE_PLU_OBJ_NAME, SearchObjects.TEMPLATE_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.AUDIT_PLU_OBJ_NAME, SearchObjects.AUDIT_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_USER_PLU_OBJ_NAME, SearchObjects.VDC_USER_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.DISK_PLU_OBJ_NAME, SearchObjects.DISK_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME);
        mVerbs.put(SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME, SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME);

        buildCompletions();
        mVerbs.put(SearchObjects.VM_OBJ_NAME, SearchObjects.VM_OBJ_NAME);
        if (isDesktopsAllowed) {
            mVerbs.put(SearchObjects.VDC_POOL_OBJ_NAME, SearchObjects.VDC_POOL_OBJ_NAME);
        }
        mVerbs.put(SearchObjects.DISK_OBJ_NAME, SearchObjects.DISK_OBJ_NAME);
        mVerbs.put(SearchObjects.VDS_OBJ_NAME, SearchObjects.VDS_OBJ_NAME);
        mVerbs.put(SearchObjects.TEMPLATE_OBJ_NAME, SearchObjects.TEMPLATE_OBJ_NAME);
        mVerbs.put(SearchObjects.AUDIT_OBJ_NAME, SearchObjects.AUDIT_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_USER_OBJ_NAME, SearchObjects.VDC_USER_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.VDC_CLUSTER_OBJ_NAME);
        mVerbs.put(SearchObjects.GLUSTER_VOLUME_OBJ_NAME, SearchObjects.GLUSTER_VOLUME_OBJ_NAME);

        // vms - vds
        addJoin(SearchObjects.VM_OBJ_NAME, "run_on_vds", SearchObjects.VDS_OBJ_NAME, "vds_id");

        // vms - vmt
        addJoin(SearchObjects.VM_OBJ_NAME, "vmt_guid", SearchObjects.TEMPLATE_OBJ_NAME, "vmt_guid");

        // vms - users
        addJoin(SearchObjects.VM_OBJ_NAME, "vm_guid", SearchObjects.VDC_USER_OBJ_NAME, "vm_guid");

        // vms - audit
        addJoin(SearchObjects.VM_OBJ_NAME, "vm_guid", SearchObjects.AUDIT_OBJ_NAME, "vm_id");

        // vms - storage domain
        addJoin(SearchObjects.VM_OBJ_NAME, "storage_id", SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, "id");

        // templates - storage domain
        addJoin(SearchObjects.TEMPLATE_OBJ_NAME, "storage_id", SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, "id");

        // vds - storage domain
        addJoin(SearchObjects.VDS_OBJ_NAME, "storage_id", SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, "id");

        // cluster - storage domain
        addJoin(SearchObjects.VDC_CLUSTER_OBJ_NAME, "storage_id", SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, "id");

        // vds - audit
        addJoin(SearchObjects.VDS_OBJ_NAME, "vds_id", SearchObjects.AUDIT_OBJ_NAME, "vds_id");

        // users - audit
        addJoin(SearchObjects.VDC_USER_OBJ_NAME, "user_id", SearchObjects.AUDIT_OBJ_NAME, "user_id");

        // Datacenter(Storage_pool) - Cluster(vds group)
        addJoin(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME, "id", SearchObjects.VDC_CLUSTER_OBJ_NAME, "storage_pool_id");

        // Datacenter(Storage_pool) - Storage Domain
        addJoin(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME, "id", SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, "storage_pool_id");

        // audit - cluster
        addJoin(SearchObjects.VDC_CLUSTER_OBJ_NAME, "vds_group_id", SearchObjects.AUDIT_OBJ_NAME, "vds_group_id");

        // gluster volume - cluster
        addJoin(SearchObjects.GLUSTER_VOLUME_OBJ_NAME, "cluster_id", SearchObjects.VDC_CLUSTER_OBJ_NAME, "vds_group_id");
    }

    private void addJoin(String firstObj, String firstColumnName, String secondObj, String secondColumnName) {
        mJoinDictionary.put(firstObj + "." + secondObj, new String[] {firstColumnName, secondColumnName});
        mJoinDictionary.put(secondObj + "." + firstObj, new String[] {secondColumnName, firstColumnName});
    }

    public IAutoCompleter getCrossRefAutoCompleter(String obj) {
        if (obj == null) {
            return null;
        }
        if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            return new AuditCrossRefAutoCompleter();

        }
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            return new TemplateCrossRefAutoCompleter();

        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            return new UserCrossRefAutoCompleter();

        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            return new VdsCrossRefAutoCompleter();

        }
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            return new VmCrossRefAutoCompleter();

        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            return new ClusterCrossRefAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            return new StoragePoolCrossRefAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            return new StorageDomainCrossRefAutoCompleter();

            // no need for empty case before default: case
            // SearchObjects.VDC_POOL_OBJ_NAME:
            // no need for empty case before default: case
            // SearchObjects.VDC_POOL_PLU_OBJ_NAME:
        }
        else if (StringHelper.EqOp(obj, SearchObjects.GLUSTER_VOLUME_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME)) {
            return GlusterVolumeCrossRefAutoCompleter.INSTANCE;
        }
        else {
            return null;
        }
    }

    public boolean isCrossReferece(String text, String obj) {
        IAutoCompleter completer = getCrossRefAutoCompleter(obj);
        boolean retval = false;
        if (completer != null) {
            retval = completer.validate(text);
        }
        return retval;
    }

    public String getInnerJoin(String searchObj, String crossRefObj) {
        String[] JoinKey = mJoinDictionary.get(StringFormat.format("%1$s.%2$s", searchObj, crossRefObj));
        String crossRefTable = getRelatedTableName(crossRefObj);
        String searchObjTable = getRelatedTableName(searchObj);

        return StringFormat.format(" LEFT OUTER JOIN %3$s ON %1$s.%2$s=%3$s.%4$s ", searchObjTable, JoinKey[0],
                crossRefTable, JoinKey[1]);
    }

    public IConditionFieldAutoCompleter getFieldAutoCompleter(String obj) {
        IConditionFieldAutoCompleter retval = null;
        if (obj == null) {
            return null;
        }
        if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = new VdsConditionFieldAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = new VmConditionFieldAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = new VmTemplateConditionFieldAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            retval = new AuditLogConditionFieldAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = new VdcUserConditionFieldAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_POOL_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_POOL_PLU_OBJ_NAME)) {
            retval = new PoolConditionFieldAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.DISK_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.DISK_PLU_OBJ_NAME)) {
            retval = new DiskImageConditionFieldAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = new ClusterConditionFieldAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            retval = new StoragePoolFieldAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = new StorageDomainFieldAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.GLUSTER_VOLUME_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME)) {
            retval = GlusterVolumeConditionFieldAutoCompleter.INSTANCE;
        }
        return retval;
    }

    public String getRelatedTableNameWithOutTags(String obj) {
        String retval;
        if (obj == null) {
            return null;
        }
        if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = "vdc_users";

        }
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = "vms";

        }
        else if (StringHelper.EqOp(obj, SearchObjects.DISK_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.DISK_PLU_OBJ_NAME)) {
            retval = "vm_images_view";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = "vm_templates_view";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = "vds";

        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = "vds_groups";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            retval = "storage_pool";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = "storage_domains_without_storage_pools";

        } else {
            retval = getRelatedTableName(obj);
        }
        return retval;
    }

    public String getRelatedTableName(String obj) {
        String retval = null;
        if (obj == null) {
            return retval;
        }
        if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = "vds_with_tags";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = "vms_with_tags";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = "vm_templates_storage_domain";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            retval = "audit_log";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.DISK_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.DISK_PLU_OBJ_NAME)) {
            retval = "vm_images_view";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = "vdc_users_with_tags";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_POOL_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_POOL_PLU_OBJ_NAME)) {
            retval = "vm_pools_full_view";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = "vds_groups_storage_domain";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            retval = "storage_pool_with_storage_domain";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = "storage_domains_with_hosts_view";

        }
        else if (StringHelper.EqOp(obj, SearchObjects.GLUSTER_VOLUME_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME)) {
            retval = "gluster_volumes";
        }
        return retval;
    }

    public String getPrimeryKeyName(String obj) {
        String retval = null;
        if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = "vds_id";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = "vm_guid";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.DISK_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.DISK_PLU_OBJ_NAME)) {
            retval = "image_guid";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = "vmt_guid";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            retval = "audit_log_id";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = "user_id";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_POOL_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_POOL_PLU_OBJ_NAME)) {
            retval = "vm_pool_id";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = "vds_group_id";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            retval = "id";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = "id";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.GLUSTER_VOLUME_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME)) {
            retval = "id";
        }
        return retval;
    }

    public IAutoCompleter getFieldRelationshipAutoCompleter(String obj, String fieldName) {
        IAutoCompleter retval = null;
        IConditionFieldAutoCompleter curConditionFieldAC = getFieldAutoCompleter(obj);
        if (curConditionFieldAC != null) {
            retval = curConditionFieldAC.getFieldRelationshipAutoCompleter(fieldName);
        }
        return retval;
    }

    public IAutoCompleter getObjectRelationshipAutoCompleter(String obj) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }

    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String obj, String fieldName) {
        IConditionValueAutoCompleter retval = null;
        IConditionFieldAutoCompleter curConditionFieldAC = getFieldAutoCompleter(obj);
        if (curConditionFieldAC != null) {
            retval = curConditionFieldAC.getFieldValueAutoCompleter(fieldName);
        }
        return retval;
    }

    public String getDefaultSort(String obj) {
        String retval = "";
        if (obj == null) {
            return retval;
        }
        if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = "vds_name ASC ";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = "vm_name ASC ";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.DISK_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.DISK_PLU_OBJ_NAME)) {
            retval = "disk_alias ASC ";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            retval = "audit_log_id DESC ";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = "name ASC ";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = "name ASC ";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_POOL_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_POOL_PLU_OBJ_NAME)) {
            retval = "vm_pool_name ASC ";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            retval = "name ASC ";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = "storage_name ASC ";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = "name ASC ";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.GLUSTER_VOLUME_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME)) {
            retval = "vol_name ASC ";
        }
        return retval;
    }
}

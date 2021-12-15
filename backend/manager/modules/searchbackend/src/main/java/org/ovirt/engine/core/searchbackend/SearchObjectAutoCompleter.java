package org.ovirt.engine.core.searchbackend;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.searchbackend.gluster.GlusterVolumeConditionFieldAutoCompleter;
import org.ovirt.engine.core.searchbackend.gluster.GlusterVolumeCrossRefAutoCompleter;

public class SearchObjectAutoCompleter extends SearchObjectsBaseAutoCompleter {
    private final Map<String, String[]> joinDictionary = new HashMap<>();
    private final Map<String, Boolean> requiresFullTable = new HashMap<>();

    public SearchObjectAutoCompleter() {

        verbs.add(SearchObjects.VM_PLU_OBJ_NAME);
        verbs.add(SearchObjects.VDC_POOL_PLU_OBJ_NAME);
        verbs.add(SearchObjects.VDS_PLU_OBJ_NAME);
        verbs.add(SearchObjects.TEMPLATE_PLU_OBJ_NAME);
        verbs.add(SearchObjects.AUDIT_PLU_OBJ_NAME);
        verbs.add(SearchObjects.VDC_USER_PLU_OBJ_NAME);
        verbs.add(SearchObjects.VDC_GROUP_PLU_OBJ_NAME);
        verbs.add(SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME);
        verbs.add(SearchObjects.DISK_PLU_OBJ_NAME);
        verbs.add(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME);
        verbs.add(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME);
        verbs.add(SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME);
        verbs.add(SearchObjects.QUOTA_OBJ_NAME);
        verbs.add(SearchObjects.NETWORK_PLU_OBJ_NAME);
        verbs.add(SearchObjects.PROVIDER_PLU_OBJ_NAME);
        verbs.add(SearchObjects.INSTANCE_TYPE_PLU_OBJ_NAME);
        verbs.add(SearchObjects.IMAGE_TYPE_PLU_OBJ_NAME);
        verbs.add(SearchObjects.SESSION_PLU_OBJ_NAME);
        verbs.add(SearchObjects.JOB_PLU_OBJ_NAME);
        verbs.add(SearchObjects.VNIC_PROFILE_PLU_OBJ_NAME);

        buildCompletions();
        verbs.add(SearchObjects.VM_OBJ_NAME);
        verbs.add(SearchObjects.VDC_POOL_OBJ_NAME);
        verbs.add(SearchObjects.DISK_OBJ_NAME);
        verbs.add(SearchObjects.VDS_OBJ_NAME);
        verbs.add(SearchObjects.TEMPLATE_OBJ_NAME);
        verbs.add(SearchObjects.AUDIT_OBJ_NAME);
        verbs.add(SearchObjects.VDC_USER_OBJ_NAME);
        verbs.add(SearchObjects.VDC_GROUP_OBJ_NAME);
        verbs.add(SearchObjects.VDC_CLUSTER_OBJ_NAME);
        verbs.add(SearchObjects.GLUSTER_VOLUME_OBJ_NAME);
        verbs.add(SearchObjects.NETWORK_OBJ_NAME);
        verbs.add(SearchObjects.PROVIDER_OBJ_NAME);
        verbs.add(SearchObjects.INSTANCE_TYPE_OBJ_NAME);
        verbs.add(SearchObjects.IMAGE_TYPE_OBJ_NAME);
        verbs.add(SearchObjects.SESSION_OBJ_NAME);
        verbs.add(SearchObjects.JOB_OBJ_NAME);
        verbs.add(SearchObjects.VNIC_PROFILE_OBJ_NAME);

        requiresFullTable.put(SearchObjects.VDC_USER_ROLE_SEARCH, true);

        // Adding vm, host and user entities fields such that in case tags are used all the entity fields will be taken
        // from the tag view and not from the regular one.

        // VMS
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.ADDRESS, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.APPS, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.ARCHITECTURE, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.BIOS_TYPE, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.CLUSTER, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.COMMENT, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.CLUSTER, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.COMPATIBILITY_LEVEL, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.CPU_USAGE, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.CREATED_BY_USER_ID, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.CREATIONDATE, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.CUSTOM_COMPATIBILITY_LEVEL, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.CUSTOM_CPU_TYPE, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.CUSTOM_EMULATED_MACHINE, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.DATACENTER, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.DESCRIPTION, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.FQDN, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.GUARANTEED_MEMORY, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.HAS_ILLEGAL_IMAGES, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.HOST, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.HAS_ILLEGAL_IMAGES, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.ID, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.IP, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.LOGGEDINUSER, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.MEM_USAGE, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.MEMORY, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.MIGRATION_PROGRESS_PERCENT, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.NAME, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.NAMESPACE, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.NETWORK_USAGE, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.NEXT_RUN_CONFIG_EXISTS, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.OS, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.POOL, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.QUOTA, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.STATUS, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.TAG, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.TYPE, true);
        requiresFullTable.put(SearchObjects.VM_OBJ_NAME + "-" + VmConditionFieldAutoCompleter.UPTIME, true);
        // Hosts
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.ADDRESS, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.ADDRESS, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.ARCHITECTURE, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.CLUSTER, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.COMMENT, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.COMMITTED_MEM, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.CPU_MODEL, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.CPU_SPEED, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.CPU_USAGE, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.CPUS, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.DATACENTER, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.EXTERNAL_STATUS, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.HA_SCORE, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.HW_ID, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.LOAD, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.MEM_USAGE, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.MEMORY, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.MIGRATING_VMS, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.NAME, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.NETWORK_USAGE, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.SPM_ID, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.STATUS, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.TAG, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.TYPE, true);
        requiresFullTable.put(SearchObjects.VDS_OBJ_NAME + "-" + VdsConditionFieldAutoCompleter.VERSION, true);
        // Users
        requiresFullTable.put(SearchObjects.VDC_USER_OBJ_NAME + "-" + VdcUserConditionFieldAutoCompleter.DEPARTMENT, true);
        requiresFullTable.put(SearchObjects.VDC_USER_OBJ_NAME + "-" + VdcUserConditionFieldAutoCompleter.DIRECTORY, true);
        requiresFullTable.put(SearchObjects.VDC_USER_OBJ_NAME + "-" + VdcUserConditionFieldAutoCompleter.FIRST_NAME, true);
        requiresFullTable.put(SearchObjects.VDC_USER_OBJ_NAME + "-" + VdcUserConditionFieldAutoCompleter.LAST_NAME, true);
        requiresFullTable.put(SearchObjects.VDC_USER_OBJ_NAME + "-" + VdcUserConditionFieldAutoCompleter.LOGIN, true);
        requiresFullTable.put(SearchObjects.VDC_USER_OBJ_NAME + "-" + VdcUserConditionFieldAutoCompleter.POOL, true);
        requiresFullTable.put(SearchObjects.VDC_USER_OBJ_NAME + "-" + VdcUserConditionFieldAutoCompleter.TAG, true);
        requiresFullTable.put(SearchObjects.VDC_USER_OBJ_NAME + "-" + VdcUserConditionFieldAutoCompleter.TYPE, true);
        requiresFullTable.put(SearchObjects.VDC_USER_OBJ_NAME + "-" + VdcUserConditionFieldAutoCompleter.USER_NAME, true);

        // vms - vds
        addJoin(SearchObjects.VM_OBJ_NAME,
                "run_on_vds",
                SearchObjects.VDS_OBJ_NAME,
                "vds_id");

        // vms - vmt
        addJoin(SearchObjects.VM_OBJ_NAME,
                "vmt_guid",
                SearchObjects.TEMPLATE_OBJ_NAME,
                "vmt_guid");

        // vms - users
        addJoin(SearchObjects.VM_OBJ_NAME,
                "vm_guid",
                SearchObjects.VDC_USER_OBJ_NAME,
                "vm_guid");

        // vms - audit
        addJoin(SearchObjects.VM_OBJ_NAME,
                "vm_guid",
                SearchObjects.AUDIT_OBJ_NAME,
                "vm_id");

        // vms - vm network interface
        addJoin(SearchObjects.VM_OBJ_NAME,
                "vm_guid",
                SearchObjects.VM_NETWORK_INTERFACE_OBJ_NAME,
                "vm_guid");

        // vms - storage domain
        addJoin(SearchObjects.VM_OBJ_NAME,
                "storage_id",
                SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME,
                "id");

        // templates - storage domain
        addJoin(SearchObjects.TEMPLATE_OBJ_NAME,
                "storage_id",
                SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME,
                "id");

        // image-type - storage domain
        addJoin(SearchObjects.IMAGE_TYPE_OBJ_NAME,
                "storage_id",
                SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME,
                "id");

        // templates - vm template network interface
        addJoin(SearchObjects.TEMPLATE_OBJ_NAME,
                "vmt_guid",
                SearchObjects.VM_NETWORK_INTERFACE_OBJ_NAME,
                "vmt_guid");

        // instance-types - vm template network interface
        addJoin(SearchObjects.INSTANCE_TYPE_OBJ_NAME,
                "vmt_guid",
                SearchObjects.VM_NETWORK_INTERFACE_OBJ_NAME,
                "vmt_guid");

        // vds - storage domain
        addJoin(SearchObjects.VDS_OBJ_NAME,
                "storage_id",
                SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME,
                "id");

        // cluster - storage domain
        addJoin(SearchObjects.VDC_CLUSTER_OBJ_NAME,
                "storage_id",
                SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME,
                "id");

        // disk - storage domain images
        addJoin(SearchObjects.DISK_OBJ_NAME,
                "image_guid",
                SearchObjects.VDC_STORAGE_DOMAIN_IMAGE_OBJ_NAME,
                "image_guid");

        // storage domain images - storage domain
        addJoin(SearchObjects.VDC_STORAGE_DOMAIN_IMAGE_OBJ_NAME,
                "id",
                SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME,
                "id");

        // vds - audit
        addJoin(SearchObjects.VDS_OBJ_NAME,
                "vds_id",
                SearchObjects.AUDIT_OBJ_NAME,
                "vds_id");

        // users - audit
        addJoin(SearchObjects.VDC_USER_OBJ_NAME,
                "user_id",
                SearchObjects.AUDIT_OBJ_NAME,
                "user_id");

        // Datacenter(Storage_pool) - Cluster(vds group)
        addJoin(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME,
                "id",
                SearchObjects.VDC_CLUSTER_OBJ_NAME,
                "storage_pool_id");

        // Datacenter(Storage_pool) - Storage Domain
        addJoin(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME,
                "id",
                SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME,
                "storage_pool_id");

        // Datacenter(Storage_pool) - Disk
        addJoin(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME,
                "id",
                SearchObjects.DISK_OBJ_NAME,
                "storage_pool_id");

        // audit - cluster
        addJoin(SearchObjects.VDC_CLUSTER_OBJ_NAME,
                "cluster_id",
                SearchObjects.AUDIT_OBJ_NAME,
                "cluster_id");

        // gluster volume - cluster
        addJoin(SearchObjects.GLUSTER_VOLUME_OBJ_NAME,
                "cluster_id",
                SearchObjects.VDC_CLUSTER_OBJ_NAME,
                "cluster_id");

        // cluster - network
        addJoin(SearchObjects.VDC_CLUSTER_OBJ_NAME,
                "cluster_id",
                SearchObjects.NETWORK_CLUSTER_OBJ_NAME,
                "cluster_id");

        // network - cluster
        addJoin(SearchObjects.NETWORK_OBJ_NAME,
                "id",
                SearchObjects.NETWORK_CLUSTER_OBJ_NAME,
                "network_id");

        // network - host
        addJoin(SearchObjects.NETWORK_OBJ_NAME,
                "id",
                SearchObjects.NETWORK_HOST_OBJ_NAME,
                "network_id");

        // audit - gluster volume
        addJoin(SearchObjects.GLUSTER_VOLUME_OBJ_NAME,
                "id",
                SearchObjects.AUDIT_OBJ_NAME,
                "gluster_volume_id");

        // quota - audit
        addJoin(SearchObjects.AUDIT_OBJ_NAME,
                "quota_id",
                SearchObjects.QUOTA_OBJ_NAME,
                "quota_id");

        // data center - network
        addJoin(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME,
                "id",
                SearchObjects.NETWORK_OBJ_NAME,
                "storage_pool_id");

        // host interface - host
        addJoin(SearchObjects.VDS_OBJ_NAME,
                "vds_id",
                SearchObjects.VDS_NETWORK_INTERFACE_OBJ_NAME,
                "vds_id");

        // cluster - vm pool
        addJoin(SearchObjects.VDC_CLUSTER_OBJ_NAME,
                "cluster_id",
                SearchObjects.VDC_POOL_OBJ_NAME,
                "cluster_id");

        // provider - network
        addJoin(SearchObjects.PROVIDER_OBJ_NAME,
                "id",
                SearchObjects.NETWORK_OBJ_NAME,
                "provider_network_provider_id");

        // users - template
        addJoin(SearchObjects.VDC_USER_OBJ_NAME,
                "vm_guid",
                SearchObjects.TEMPLATE_OBJ_NAME,
                "vmt_guid");

        // users - host
        addJoin(SearchObjects.VDC_USER_OBJ_NAME,
                "vm_guid",
                SearchObjects.VDS_OBJ_NAME,
                "vds_id");
    }

    private void addJoin(String firstObj, String firstColumnName, String secondObj, String secondColumnName) {
        joinDictionary.put(firstObj + "." + secondObj, new String[] { firstColumnName, secondColumnName });
        joinDictionary.put(secondObj + "." + firstObj, new String[] { secondColumnName, firstColumnName });
    }

    public static final class EntitySearchInfo {

        public EntitySearchInfo(IAutoCompleter crossRefAutoCompleter,
                IConditionFieldAutoCompleter conditionFieldAutoCompleter,
                String relatedTableNameWithOutTags,
                String relatedTableName,
                String primeryKeyName,
                String defaultSort,
                boolean usingDistinct) {
            this.crossRefAutoCompleter = crossRefAutoCompleter;
            this.conditionFieldAutoCompleter = conditionFieldAutoCompleter;
            this.relatedTableNameWithOutTags = relatedTableNameWithOutTags;
            this.relatedTableName = relatedTableName;
            this.primeryKeyName = primeryKeyName;
            this.defaultSort = defaultSort;
            this.usingDistinct = usingDistinct;
        }

        public EntitySearchInfo(IAutoCompleter crossRefAutoCompleter,
                IConditionFieldAutoCompleter conditionFieldAutoCompleter,
                String relatedTableNameWithOutTags,
                String relatedTableName,
                String primeryKeyName,
                String defaultSort,
                boolean usingDistinct,
                List<String> commaDelimitedListColumns) {
            this(crossRefAutoCompleter, conditionFieldAutoCompleter, relatedTableNameWithOutTags, relatedTableName, primeryKeyName, defaultSort, usingDistinct);
            this.commaDelimitedListColumns = commaDelimitedListColumns;
        }

        final IAutoCompleter crossRefAutoCompleter;
        final IConditionFieldAutoCompleter conditionFieldAutoCompleter;
        final String relatedTableNameWithOutTags;
        final String relatedTableName;
        final String primeryKeyName;
        final String defaultSort;
        /**
         * usingDistinct will generate a sql query with distinct keyword for search results
         * that may produce duplicated entries. Most searches do not produce non unique results but some do
         * like vm template storage domain view
         */
        final boolean usingDistinct;
        List<String> commaDelimitedListColumns;
    }

    @SuppressWarnings("serial")
    private static final Map<String, EntitySearchInfo> entitySearchInfo = Collections.unmodifiableMap(
            new HashMap<String, SearchObjectAutoCompleter.EntitySearchInfo>() {
                {
                    put(SearchObjects.AUDIT_OBJ_NAME, new EntitySearchInfo(new AuditCrossRefAutoCompleter(),
                            new AuditLogConditionFieldAutoCompleter(),
                            null,
                            "audit_log",
                            "audit_log_id",
                            "audit_log_id DESC ",
                            false));
                    put(SearchObjects.TEMPLATE_OBJ_NAME, new EntitySearchInfo(new TemplateCrossRefAutoCompleter(),
                            new VmTemplateConditionFieldAutoCompleter(),
                            "vm_templates_view",
                            "vm_templates_storage_domain",
                            "vmt_guid",
                            "name ASC ",
                            true));
                    put(SearchObjects.INSTANCE_TYPE_OBJ_NAME, new EntitySearchInfo(new TemplateCrossRefAutoCompleter(),
                            new VmTemplateConditionFieldAutoCompleter(),
                            "instance_types_view",
                            "instance_types_storage_domain",
                            "vmt_guid",
                            "name ASC ",
                            true));
                    put(SearchObjects.IMAGE_TYPE_OBJ_NAME, new EntitySearchInfo(new TemplateCrossRefAutoCompleter(),
                            new VmTemplateConditionFieldAutoCompleter(),
                            "image_types_view",
                            "image_types_storage_domain",
                            "vmt_guid",
                            "name ASC ",
                            true));
                    put(SearchObjects.VDC_USER_OBJ_NAME, new EntitySearchInfo(new UserCrossRefAutoCompleter(),
                            new VdcUserConditionFieldAutoCompleter(),
                            "vdc_users",
                            "vdc_users_with_tags",
                            "user_id",
                            "name ASC ",
                            false));
                    put(SearchObjects.VDC_GROUP_OBJ_NAME, new EntitySearchInfo(
                            null,
                            new VdcGroupConditionFieldAutoCompleter(),
                            "ad_groups",
                            "ad_groups",
                            "id",
                            "name ASC ",
                            false));
                    put(SearchObjects.VDS_OBJ_NAME, new EntitySearchInfo(new VdsCrossRefAutoCompleter(),
                            new VdsConditionFieldAutoCompleter(),
                            "vds",
                            "vds_with_tags",
                            "vds_id",
                            "vds_name ASC ",
                            false));
                    put(SearchObjects.VDS_OBJ_NAME + SearchObjects.AUDIT_OBJ_NAME, new EntitySearchInfo(new VdsCrossRefAutoCompleter(),
                            new VdsConditionFieldAutoCompleter(),
                            "vds",
                            "(SELECT distinct vds_id, vds_name FROM vds_with_tags) vds_with_tags_temp",
                            "vds_id",
                            "vds_name ASC ",
                            false));
                    put(SearchObjects.VM_OBJ_NAME, new EntitySearchInfo(new VmCrossRefAutoCompleter(),
                            new VmConditionFieldAutoCompleter(),
                            "vms",
                            "vms_with_tags",
                            "vm_guid",
                            "vm_name ASC ",
                            false));
                    put(SearchObjects.VDC_CLUSTER_OBJ_NAME, new EntitySearchInfo(new ClusterCrossRefAutoCompleter(),
                            new ClusterConditionFieldAutoCompleter(),
                            "cluster_view",
                            "cluster_storage_domain",
                            "cluster_id",
                            "name ASC",
                            false));
                    put(SearchObjects.QUOTA_OBJ_NAME, new EntitySearchInfo(new QuotaConditionFieldAutoCompleter(),
                            new QuotaConditionFieldAutoCompleter(),
                            "quota_view",
                            "quota_view",
                            "quota_id",
                            "quota_name ASC",
                            true));
                    put(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME,
                            new EntitySearchInfo(new StoragePoolCrossRefAutoCompleter(),
                                    new StoragePoolFieldAutoCompleter(),
                                    "storage_pool",
                                    "storage_pool_with_storage_domain",
                                    "id",
                                    "name ASC ",
                                    true));
                    put(SearchObjects.DISK_OBJ_NAME, new EntitySearchInfo(new DiskCrossRefAutoCompleter(),
                            new DiskConditionFieldAutoCompleter(),
                            "all_disks",
                            "all_disks",
                            "disk_id",
                            "disk_alias ASC, disk_id ASC ",
                            true,
                            Arrays.asList("vm_names")));
                    put(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME,
                            new EntitySearchInfo(new StorageDomainCrossRefAutoCompleter(),
                                    new StorageDomainFieldAutoCompleter(),
                                    "storage_domains_for_search",
                                    "storage_domains_with_hosts_view",
                                    "id",
                                    "storage_name ASC ",
                                    true,
                                    Arrays.asList("datacenter")));
                    put(SearchObjects.VDC_STORAGE_DOMAIN_IMAGE_OBJ_NAME,
                            new EntitySearchInfo(null,
                                    null,
                                    null,
                                    "vm_images_storage_domains_view",
                                    "image_guid",
                                    "disk_alias ASC, disk_id ASC ",
                                    true));
                    put(SearchObjects.GLUSTER_VOLUME_OBJ_NAME,
                            new EntitySearchInfo(GlusterVolumeCrossRefAutoCompleter.INSTANCE,
                                    GlusterVolumeConditionFieldAutoCompleter.INSTANCE,
                                    null,
                                    "gluster_volumes_view",
                                    "id",
                                    "vol_name ASC ",
                                    true));
                    put(SearchObjects.VDC_POOL_OBJ_NAME, new EntitySearchInfo(null,
                            new PoolConditionFieldAutoCompleter(),
                            null,
                            "vm_pools_full_view",
                            "vm_pool_id",
                            "vm_pool_name ASC ",
                            true));
                    put(SearchObjects.NETWORK_OBJ_NAME, new EntitySearchInfo(new NetworkCrossRefAutoCompleter(),
                            new NetworkConditionFieldAutoCompleter(),
                            "network_view",
                            "network_view",
                            "id",
                            "storage_pool_name ASC, name ASC",
                            true));
                    put(SearchObjects.VDS_NETWORK_INTERFACE_OBJ_NAME, new EntitySearchInfo(null,
                            new NetworkInterfaceConditionFieldAutoCompleter(),
                            "vds_interface",
                            "vds_interface",
                            "vds_id",
                            "name ASC",
                            false));
                    put(SearchObjects.VM_NETWORK_INTERFACE_OBJ_NAME, new EntitySearchInfo(null,
                            new NetworkInterfaceConditionFieldAutoCompleter(),
                            "vm_interface_view",
                            "vm_interface_view",
                            "name",
                            "vm_id ASC",
                            false));
                    put(SearchObjects.NETWORK_CLUSTER_OBJ_NAME,
                            new EntitySearchInfo(null,
                            new NetworkClusterConditionFieldAutoCompleter(),
                            "network_cluster_view",
                            "network_cluster_view",
                            "cluster_id",
                            "cluster_name ASC",
                            true));
                    put(SearchObjects.NETWORK_HOST_OBJ_NAME, new EntitySearchInfo(null,
                            new NetworkHostConditionFieldAutoCompleter(),
                            "network_vds_view",
                            "network_vds_view",
                            "vds_name",
                            "network_name ASC",
                            true));
                    put(SearchObjects.PROVIDER_OBJ_NAME, new EntitySearchInfo(null,
                            new ProviderConditionFieldAutoCompleter(),
                            "providers",
                            "providers",
                            "id",
                            "name ASC",
                            false));
                    put(SearchObjects.SESSION_OBJ_NAME, new EntitySearchInfo(null,
                            new SessionConditionFieldAutoCompleter(),
                            "engine_sessions",
                            "engine_sessions",
                            "id",
                            "user_name ASC, id",
                            false));
                    put(SearchObjects.JOB_OBJ_NAME, new EntitySearchInfo(null,
                            new JobConditionFieldAutoCompleter(),
                            "job",
                            "job",
                            "job_id",
                            "start_time ASC",
                            false));
                    put(SearchObjects.VNIC_PROFILE_OBJ_NAME, new EntitySearchInfo(null,
                            new VnicProfileConditionFieldAutoCompleter(),
                            "vnic_profiles_view",
                            "vnic_profiles_view",
                            "id",
                            "name ASC",
                            true));
                }
            });

    static EntitySearchInfo getEntitySearchInfo(String key) {
        return entitySearchInfo.get(singular(key));
    }

    @SuppressWarnings("serial")
    private static final Map<String, String> singulars = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            put(SearchObjects.AUDIT_PLU_OBJ_NAME, SearchObjects.AUDIT_OBJ_NAME);
            put(SearchObjects.TEMPLATE_PLU_OBJ_NAME, SearchObjects.TEMPLATE_OBJ_NAME);
            put(SearchObjects.INSTANCE_TYPE_PLU_OBJ_NAME, SearchObjects.INSTANCE_TYPE_OBJ_NAME);
            put(SearchObjects.IMAGE_TYPE_PLU_OBJ_NAME, SearchObjects.IMAGE_TYPE_OBJ_NAME);
            put(SearchObjects.VDC_USER_PLU_OBJ_NAME, SearchObjects.VDC_USER_OBJ_NAME);
            put(SearchObjects.VDC_GROUP_PLU_OBJ_NAME, SearchObjects.VDC_GROUP_OBJ_NAME);
            put(SearchObjects.VDS_PLU_OBJ_NAME, SearchObjects.VDS_OBJ_NAME);
            put(SearchObjects.VM_PLU_OBJ_NAME, SearchObjects.VM_OBJ_NAME);
            put(SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME, SearchObjects.VDC_CLUSTER_OBJ_NAME);
            put(SearchObjects.QUOTA_PLU_OBJ_NAME, SearchObjects.QUOTA_OBJ_NAME);
            put(SearchObjects.DISK_PLU_OBJ_NAME, SearchObjects.DISK_OBJ_NAME);
            put(SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME, SearchObjects.GLUSTER_VOLUME_OBJ_NAME);
            put(SearchObjects.VDC_POOL_PLU_OBJ_NAME, SearchObjects.VDC_POOL_OBJ_NAME);
            put(SearchObjects.VDC_STORAGE_DOMAIN_PLU_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME);
            put(SearchObjects.NETWORK_PLU_OBJ_NAME, SearchObjects.NETWORK_OBJ_NAME);
            put(SearchObjects.PROVIDER_PLU_OBJ_NAME, SearchObjects.PROVIDER_OBJ_NAME);
            put(SearchObjects.SESSION_PLU_OBJ_NAME, SearchObjects.SESSION_OBJ_NAME);
            put(SearchObjects.JOB_PLU_OBJ_NAME, SearchObjects.JOB_OBJ_NAME);
            put(SearchObjects.VNIC_PROFILE_PLU_OBJ_NAME, SearchObjects.VNIC_PROFILE_OBJ_NAME);
        }
    });

    static String singular(String key) {
        return singulars.containsKey(key) ? singulars.get(key) : key;
    }

    public IAutoCompleter getCrossRefAutoCompleter(String obj) {
        if (obj == null) {
            return null;
        }
        if (getEntitySearchInfo(obj) != null) {
            return getEntitySearchInfo(obj).crossRefAutoCompleter;
        } else {
            return null;
        }
    }

    public boolean isCrossReference(String text, String obj) {
        IAutoCompleter completer = getCrossRefAutoCompleter(obj);
        if (completer != null) {
            return completer.validate(text);
        }
        return false;
    }

    public String getInnerJoin(String searchObj, String crossRefObj, boolean useTags) {
        final String[] joinKey = joinDictionary.get(StringFormat.format("%1$s.%2$s", searchObj, crossRefObj));
        // For joins, the table we join with is always the full view (including the tags)
        String crossRefTable;
        String crossRefTableName;
        if (SearchObjects.VDS_OBJ_NAME.equals(crossRefObj) && SearchObjects.AUDIT_OBJ_NAME.equals(searchObj)) {
            crossRefTable = getRelatedTableName(SearchObjects.VDS_OBJ_NAME + SearchObjects.AUDIT_OBJ_NAME, true);
            crossRefTableName = crossRefTable.substring(crossRefTable.indexOf(")") + 1).trim();
        } else {
            crossRefTable = getRelatedTableName(crossRefObj, true);
            crossRefTableName = crossRefTable;
        }
        final String searchObjTable = getRelatedTableName(searchObj, useTags);

        return StringFormat.format(" LEFT OUTER JOIN %3$s ON %1$s.%2$s=%4$s.%5$s ", searchObjTable, joinKey[0],
                crossRefTable, crossRefTableName, joinKey[1]);
    }

    public IConditionFieldAutoCompleter getFieldAutoCompleter(String obj) {
        if (obj == null) {
            return null;
        }
        if (getEntitySearchInfo(obj) != null) {
            return getEntitySearchInfo(obj).conditionFieldAutoCompleter;
        }
        return null;
    }

    public String getRelatedTableName(String obj, String fieldName, boolean useTagsInFrom) {
        return getRelatedTableName(obj, fieldName == null || fieldName.length() == 0
                        || fieldName.toLowerCase().equalsIgnoreCase("tag")
                        || requiresTagsForField(obj, fieldName, useTagsInFrom));
    }

    private boolean requiresTagsForField(String obj, String fieldName, boolean useTagsInFrom) {
        if (useTagsInFrom) {
            return requiresFullTable.containsKey(obj + "-" + fieldName);
        }
        return false;
    }

    public String getRelatedTableName(String obj, boolean useTags) {
        if (useTags) {
            return getRelatedTableName(obj);
        }

        return getRelatedTableNameWithoutTags(obj);
    }

    private String getRelatedTableNameWithoutTags(String obj) {
        if (obj == null) {
            return null;
        } else if (getEntitySearchInfo(obj) != null && getEntitySearchInfo(obj).relatedTableNameWithOutTags != null) {
            return getEntitySearchInfo(obj).relatedTableNameWithOutTags;
        } else {
            return getRelatedTableName(obj);
        }
    }

    private String getRelatedTableName(String obj) {
        if (obj == null) {
            return null;
        } else if (getEntitySearchInfo(obj) != null) {
            return getEntitySearchInfo(obj).relatedTableName;
        }
        return null;
    }

    public String getPrimeryKeyName(String obj) {
        if (getEntitySearchInfo(obj) != null) {
            return getEntitySearchInfo(obj).primeryKeyName;
        }
        return null;
    }

    public boolean isUsingDistinct(String obj) {
        if (getEntitySearchInfo(obj) != null) {
            return getEntitySearchInfo(obj).usingDistinct;
        }
        return false;
    }

    public IAutoCompleter getFieldRelationshipAutoCompleter(String obj, String fieldName) {
        IConditionFieldAutoCompleter curConditionFieldAC = getFieldAutoCompleter(obj);
        if (curConditionFieldAC != null) {
            return curConditionFieldAC.getFieldRelationshipAutoCompleter(fieldName);
        }
        return null;
    }

    public IAutoCompleter getObjectRelationshipAutoCompleter() {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }

    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String obj, String fieldName) {
        final IConditionFieldAutoCompleter curConditionFieldAC = getFieldAutoCompleter(obj);
        if (curConditionFieldAC != null) {
            return curConditionFieldAC.getFieldValueAutoCompleter(fieldName);
        }
        return null;
    }

    public String getDefaultSort(String obj) {
        if (obj == null) {
            return "";
        }
        if (getEntitySearchInfo(obj) != null) {
            return getEntitySearchInfo(obj).defaultSort;
        }
        return "";
    }

    public List<String> getCommaDelimitedListColumns(String obj) {
        return getEntitySearchInfo(obj).commaDelimitedListColumns;
    }
}

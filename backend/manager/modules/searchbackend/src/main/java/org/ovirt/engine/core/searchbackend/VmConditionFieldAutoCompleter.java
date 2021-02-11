package org.ovirt.engine.core.searchbackend;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TimeSpan;
import org.ovirt.engine.core.compat.Version;

// IMPORTANT : Adding any new field to this class will require adding it to SearchObjectAutoCompleter.requiresFullTable Map

public class VmConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String NAME = "NAME";
    public static final String COMMENT = "COMMENT";
    public static final String STATUS = "STATUS";
    public static final String HOST = "ON_HOST";
    public static final String IP = "IP";
    public static final String FQDN = "FQDN";
    public static final String UPTIME = "UPTIME";
    public static final String OS = "OS";
    public static final String CREATIONDATE = "CREATIONDATE";
    public static final String ADDRESS = "ADDRESS";
    public static final String CPU_USAGE = "CPU_USAGE";
    public static final String MEM_USAGE = "MEM_USAGE";
    public static final String NETWORK_USAGE = "NETWORK_USAGE";
    public static final String MIGRATION_PROGRESS_PERCENT = "MIGRATION_PROGRESS_PERCENT";
    public static final String MEMORY = "MEMORY";
    public static final String GUARANTEED_MEMORY = "GUARANTEED_MEMORY";
    public static final String APPS = "APPS";
    public static final String CLUSTER = "CLUSTER";
    public static final String POOL = "POOL";
    public static final String LOGGEDINUSER = "LOGGEDINUSER";
    public static final String TAG = "TAG";
    public static final String DATACENTER = "DATACENTER";
    public static final String TYPE = "TYPE";
    public static final String QUOTA = "QUOTA";
    public static final String ID = "ID";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String ARCHITECTURE = "ARCHITECTURE";
    public static final String CUSTOM_EMULATED_MACHINE = "CUSTOM_EMULATED_MACHINE";
    public static final String CUSTOM_CPU_TYPE = "CUSTOM_CPU_TYPE";
    public static final String COMPATIBILITY_LEVEL = "COMPATIBILITY_LEVEL";
    public static final String CUSTOM_COMPATIBILITY_LEVEL = "CUSTOM_COMPATIBILITY_LEVEL";
    public static final String CREATED_BY_USER_ID = "CREATED_BY_USER_ID";
    public static final String NEXT_RUN_CONFIG_EXISTS = "NEXT_RUN_CONFIG_EXISTS";
    public static final String HAS_ILLEGAL_IMAGES = "HAS_ILLEGAL_IMAGES";
    public static final String BIOS_TYPE = "BIOS_TYPE";
    private static final int MILISECOND = 1000;
    public static final String NAMESPACE = "K8S_NAMESPACE";

    public VmConditionFieldAutoCompleter() {
        // Building the basic verbs Dict
        verbs.add(NAME);
        verbs.add(COMMENT);
        verbs.add(STATUS);
        verbs.add(IP);
        verbs.add(HOST);
        verbs.add(FQDN);
        verbs.add(UPTIME);
        verbs.add(OS);
        verbs.add(CREATIONDATE);
        verbs.add(ADDRESS);
        verbs.add(CPU_USAGE);
        verbs.add(MEM_USAGE);
        verbs.add(NETWORK_USAGE);
        verbs.add(MEMORY);
        verbs.add(GUARANTEED_MEMORY);
        verbs.add(MIGRATION_PROGRESS_PERCENT);
        verbs.add(APPS);
        verbs.add(CLUSTER);
        verbs.add(POOL);
        verbs.add(LOGGEDINUSER);
        verbs.add(TAG);
        verbs.add(DATACENTER);
        verbs.add(TYPE);
        verbs.add(QUOTA);
        verbs.add(ID);
        verbs.add(DESCRIPTION);
        verbs.add(ARCHITECTURE);
        verbs.add(CUSTOM_EMULATED_MACHINE);
        verbs.add(CUSTOM_CPU_TYPE);
        verbs.add(COMPATIBILITY_LEVEL);
        verbs.add(CUSTOM_COMPATIBILITY_LEVEL);
        verbs.add(CREATED_BY_USER_ID);
        verbs.add(NEXT_RUN_CONFIG_EXISTS);
        verbs.add(HAS_ILLEGAL_IMAGES);
        verbs.add(BIOS_TYPE);
        verbs.add(NAMESPACE);
        // Building the autoCompletion Dict
        buildCompletions();

        // Building the types dict
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(COMMENT, String.class);
        getTypeDictionary().put(STATUS, VMStatus.class);
        getTypeDictionary().put(IP, String.class);
        getTypeDictionary().put(FQDN, String.class);
        getTypeDictionary().put(UPTIME, TimeSpan.class);
        getTypeDictionary().put(OS, String.class);
        getTypeDictionary().put(CREATIONDATE, Date.class);
        getTypeDictionary().put(ADDRESS, String.class);
        getTypeDictionary().put(CPU_USAGE, Integer.class);
        getTypeDictionary().put(MEM_USAGE, Integer.class);
        getTypeDictionary().put(NETWORK_USAGE, Integer.class);
        getTypeDictionary().put(MIGRATION_PROGRESS_PERCENT, Integer.class);
        getTypeDictionary().put(MEMORY, Integer.class);
        getTypeDictionary().put(GUARANTEED_MEMORY, Integer.class);
        getTypeDictionary().put(APPS, String.class);
        getTypeDictionary().put(CLUSTER, String.class);
        getTypeDictionary().put(POOL, String.class);
        getTypeDictionary().put(LOGGEDINUSER, String.class);
        getTypeDictionary().put(TAG, String.class);
        getTypeDictionary().put(DATACENTER, String.class);
        getTypeDictionary().put(TYPE, VmType.class);
        getTypeDictionary().put(QUOTA, String.class);
        getTypeDictionary().put(HOST, String.class);
        getTypeDictionary().put(ID, UUID.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(ARCHITECTURE, ArchitectureType.class);
        getTypeDictionary().put(CUSTOM_EMULATED_MACHINE, String.class);
        getTypeDictionary().put(CUSTOM_CPU_TYPE, String.class);
        getTypeDictionary().put(COMPATIBILITY_LEVEL, String.class);
        getTypeDictionary().put(CUSTOM_COMPATIBILITY_LEVEL, Version.class);
        getTypeDictionary().put(CREATED_BY_USER_ID, UUID.class);
        getTypeDictionary().put(NEXT_RUN_CONFIG_EXISTS, Boolean.class);
        getTypeDictionary().put(HAS_ILLEGAL_IMAGES, Boolean.class);
        getTypeDictionary().put(BIOS_TYPE, BiosType.class);
        getTypeDictionary().put(NAMESPACE, String.class);

        // building the ColumnName Dict
        columnNameDict.put(NAME, "vm_name");
        columnNameDict.put(COMMENT, "free_text_comment");
        columnNameDict.put(STATUS, "status");
        columnNameDict.put(IP, "vm_ip");
        columnNameDict.put(FQDN, "vm_fqdn");
        columnNameDict.put(UPTIME, "elapsed_time");
        columnNameDict.put(OS, "os");
        columnNameDict.put(CREATIONDATE, "creation_date");
        columnNameDict.put(ADDRESS, "vm_host");
        columnNameDict.put(MEM_USAGE, "usage_mem_percent");
        columnNameDict.put(NETWORK_USAGE, "usage_network_percent");
        columnNameDict.put(CPU_USAGE, "usage_cpu_percent");
        columnNameDict.put(MIGRATION_PROGRESS_PERCENT, "migration_progress_percent");
        columnNameDict.put(MEMORY, "mem_size_mb");
        columnNameDict.put(GUARANTEED_MEMORY, "min_allocated_mem");
        columnNameDict.put(APPS, "app_list");
        columnNameDict.put(CLUSTER, "cluster_name");
        columnNameDict.put(POOL, "vm_pool_name");
        columnNameDict.put(LOGGEDINUSER, "guest_cur_user_name");
        columnNameDict.put(TAG, "tag_name");
        columnNameDict.put(DATACENTER, "storage_pool_name");
        columnNameDict.put(TYPE, "vm_type");
        columnNameDict.put(QUOTA, "quota_name");
        columnNameDict.put(HOST, "run_on_vds_name");
        columnNameDict.put(ID, "vm_guid");
        columnNameDict.put(DESCRIPTION, "description");
        columnNameDict.put(ARCHITECTURE, "architecture");
        columnNameDict.put(CUSTOM_EMULATED_MACHINE, "custom_emulated_machine");
        columnNameDict.put(CUSTOM_CPU_TYPE, "custom_cpu_name");
        columnNameDict.put(COMPATIBILITY_LEVEL, "cluster_compatibility_version");
        columnNameDict.put(CUSTOM_COMPATIBILITY_LEVEL, "custom_compatibility_version");
        columnNameDict.put(CREATED_BY_USER_ID, "created_by_user_id");
        columnNameDict.put(NEXT_RUN_CONFIG_EXISTS, "next_run_config_exists");
        columnNameDict.put(HAS_ILLEGAL_IMAGES, "has_illegal_images");
        columnNameDict.put(BIOS_TYPE, "bios_type");
        columnNameDict.put(NAMESPACE, "namespace");

        // Override field names for purpose of sorting, if needed
        sortableFieldDict.put(IP, Collections.singletonList(
                new SyntaxChecker.SortByElement("vm_ip_inet_array")));

        /**
         */
        notFreeTextSearchableFieldsList.add(APPS);
        notFreeTextSearchableFieldsList.add(OS);
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if (UPTIME.equals(fieldName) || CREATIONDATE.equals(fieldName)) {
            return BiggerOrSmallerRelationAutoCompleter.INSTANCE;
        } else if (CPU_USAGE.equals(fieldName) || MEM_USAGE.equals(fieldName)
                || MEMORY.equals(fieldName) || GUARANTEED_MEMORY.equals(fieldName)
                || NETWORK_USAGE.equals(fieldName)
                || MIGRATION_PROGRESS_PERCENT.equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else if (TAG.equals(fieldName)) {
            return StringConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if (OS.equals(fieldName)) {
            return SimpleDependencyInjector.getInstance().get(OsValueAutoCompleter.class);
        } else if (STATUS.equals(fieldName)) {
            return new EnumValueAutoCompleter(VMStatus.class);
        } else if (TYPE.equals(fieldName)) {
            return new EnumValueAutoCompleter(VmType.class);
        } else if (QUOTA.equals(fieldName)) {
            return new NullableStringAutoCompleter();
        } else if (ARCHITECTURE.equals(fieldName)) {
            return new EnumValueAutoCompleter(ArchitectureType.class);
        } else if (NEXT_RUN_CONFIG_EXISTS.equals(fieldName)) {
            return new BitValueAutoCompleter();
        } else if (HAS_ILLEGAL_IMAGES.equals(fieldName)) {
            return new BitValueAutoCompleter();
        } else if (BIOS_TYPE.equals(fieldName)) {
            return new EnumValueAutoCompleter(BiosType.class);
        }
        return null;
    }

    @Override
    public void formatValue(String fieldName, Pair<String, String> pair, boolean caseSensitive) {
        if (APPS.equals(fieldName)) {
            pair.setSecond(
                    StringFormat.format(BaseConditionFieldAutoCompleter.getI18NPrefix() + "'%%%1$s%%'",
                            StringHelper.trim(pair.getSecond(), '\'').replace("N'",
                                    "")));
            if ("=".equals(pair.getFirst())) {
                pair.setFirst(BaseConditionFieldAutoCompleter.getLikeSyntax(caseSensitive));
            } else if ("!=".equals(pair.getFirst())) {
                pair.setFirst("NOT " + BaseConditionFieldAutoCompleter.getLikeSyntax(caseSensitive));
            }
        } else if (UPTIME.equals(fieldName)) {
            pair.setSecond(StringHelper.trim(pair.getSecond(), '\''));
            TimeSpan ts = TimeSpan.parse(pair.getSecond());
            pair.setSecond(StringFormat.format("'%1$s'", ts.TotalMilliseconds < MILISECOND ? 0 : ts.TotalMilliseconds / MILISECOND));
        } else if (CREATIONDATE.equals(fieldName)) {
            Date tmp = new Date(Date.parse(StringHelper.trim(pair.getSecond(), '\'')));
            pair.setSecond(StringFormat.format("'%1$s'", tmp));
        } else {
            super.formatValue(fieldName, pair, caseSensitive);
        }
    }
}

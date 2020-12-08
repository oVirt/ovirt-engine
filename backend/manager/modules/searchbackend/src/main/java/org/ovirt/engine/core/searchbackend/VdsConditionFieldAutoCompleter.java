package org.ovirt.engine.core.searchbackend;

import java.math.BigDecimal;
import java.util.UUID;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.VDSNiceType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;

// IMPORTANT : Adding any new field to this class will require adding it to SearchObjectAutoCompleter.requiresFullTable Map

public class VdsConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String NAME = "NAME";
    public static final String ADDRESS = "ADDRESS";
    public static final String CLUSTER = "CLUSTER";
    public static final String DATACENTER = "DATACENTER";
    public static final String STATUS = "STATUS";
    public static final String EXTERNAL_STATUS = "EXTERNAL_STATUS";
    public static final String ACTIVE_VMS = "ACTIVE_VMS";
    public static final String MEM_USAGE = "MEM_USAGE";
    public static final String CPU_USAGE = "CPU_USAGE";
    public static final String NETWORK_USAGE = "NETWORK_USAGE";
    public static final String UPDATE_AVAILABLE = "UPDATE_AVAILABLE";
    public static final String COMMENT = "COMMENT";
    public static final String LOAD = "LOAD";
    public static final String VERSION = "VERSION";
    public static final String CPUS = "CPUS";
    public static final String MEMORY = "MEMORY";
    public static final String CPU_SPEED = "CPU_SPEED";
    public static final String CPU_MODEL = "CPU_MODEL";
    public static final String MIGRATING_VMS = "MIGRATING_VMS";
    public static final String COMMITTED_MEM = "COMMITTED_MEM";
    public static final String TAG = "TAG";
    public static final String TYPE = "TYPE";
    public static final String ARCHITECTURE = "ARCHITECTURE";
    public static final String HA_SCORE = "HA_SCORE";
    public static final String SPM_ID = "SPM_ID";
    public static final String HW_ID = "HW_ID";

    public VdsConditionFieldAutoCompleter() {
        super();
        verbs.add(NAME);
        verbs.add(COMMENT);
        verbs.add(STATUS);
        verbs.add(EXTERNAL_STATUS);
        verbs.add(CLUSTER);
        verbs.add(ADDRESS);
        verbs.add(CPU_USAGE);
        verbs.add(MEM_USAGE);
        verbs.add(NETWORK_USAGE);
        verbs.add(LOAD);
        verbs.add(VERSION);
        verbs.add(CPUS);
        verbs.add(MEMORY);
        verbs.add(CPU_SPEED);
        verbs.add(CPU_MODEL);
        verbs.add(ACTIVE_VMS);
        verbs.add(MIGRATING_VMS);
        verbs.add(COMMITTED_MEM);
        verbs.add(TAG);
        verbs.add(TYPE);
        verbs.add(DATACENTER);
        verbs.add(ARCHITECTURE);
        verbs.add(UPDATE_AVAILABLE);
        verbs.add(HA_SCORE);
        verbs.add(SPM_ID);
        verbs.add(HW_ID);
        buildCompletions();
        verbs.add("ID");
        // Building the types dict
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(COMMENT, String.class);
        getTypeDictionary().put(STATUS, VDSStatus.class);
        getTypeDictionary().put(EXTERNAL_STATUS, ExternalStatus.class);
        getTypeDictionary().put(CLUSTER, String.class);
        getTypeDictionary().put(ADDRESS, String.class);
        getTypeDictionary().put(CPU_USAGE, Integer.class);
        getTypeDictionary().put(MEM_USAGE, Integer.class);
        getTypeDictionary().put(NETWORK_USAGE, Integer.class);
        getTypeDictionary().put(LOAD, BigDecimal.class);
        getTypeDictionary().put(VERSION, String.class);
        getTypeDictionary().put(CPUS, Integer.class);
        getTypeDictionary().put(MEMORY, Integer.class);
        getTypeDictionary().put(CPU_SPEED, BigDecimal.class);
        getTypeDictionary().put(CPU_MODEL, String.class);
        getTypeDictionary().put(ACTIVE_VMS, Integer.class);
        getTypeDictionary().put(MIGRATING_VMS, Integer.class);
        getTypeDictionary().put(COMMITTED_MEM, Integer.class);
        getTypeDictionary().put(TAG, String.class);
        getTypeDictionary().put(TYPE, VDSNiceType.class);
        getTypeDictionary().put(DATACENTER, String.class);
        getTypeDictionary().put("ID", UUID.class);
        getTypeDictionary().put(ARCHITECTURE, ArchitectureType.class);
        getTypeDictionary().put(UPDATE_AVAILABLE, Boolean.class);
        getTypeDictionary().put(HA_SCORE, Integer.class);
        getTypeDictionary().put(SPM_ID, Integer.class);
        getTypeDictionary().put(HW_ID, UUID.class);
        // building the ColumnName Dict
        columnNameDict.put(NAME, "vds_name");
        columnNameDict.put(COMMENT, "free_text_comment");
        columnNameDict.put(STATUS, "status");
        columnNameDict.put(EXTERNAL_STATUS, "external_status");
        columnNameDict.put(CLUSTER, "cluster_name");
        columnNameDict.put(ADDRESS, "host_name");
        columnNameDict.put(CPU_USAGE, "usage_cpu_percent");
        columnNameDict.put(MEM_USAGE, "usage_mem_percent");
        columnNameDict.put(NETWORK_USAGE, "usage_network_percent");
        columnNameDict.put(LOAD, "cpu_load");
        columnNameDict.put(VERSION, "software_version");
        columnNameDict.put(CPUS, "cpu_cores");
        columnNameDict.put(MEMORY, "physical_mem_mb");
        columnNameDict.put(CPU_SPEED, "cpu_speed_mh");
        columnNameDict.put(CPU_MODEL, "cpu_model");
        columnNameDict.put(ACTIVE_VMS, "vm_active");
        columnNameDict.put(MIGRATING_VMS, "vm_migrating");
        columnNameDict.put(COMMITTED_MEM, "mem_commited");
        columnNameDict.put(TAG, "tag_name");
        columnNameDict.put(TYPE, "vds_type");
        columnNameDict.put(DATACENTER, "storage_pool_name");
        columnNameDict.put("ID", "vds_id");
        columnNameDict.put(ARCHITECTURE, "architecture");
        columnNameDict.put(UPDATE_AVAILABLE, "is_update_available");
        columnNameDict.put(HA_SCORE, "ha_score");
        columnNameDict.put(SPM_ID, "vds_spm_id");
        columnNameDict.put(HW_ID, "vds_unique_id");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if (MEMORY.equals(fieldName) || CPUS.equals(fieldName)
                || CPU_USAGE.equals(fieldName) || MEM_USAGE.equals(fieldName)
                || LOAD.equals(fieldName) || CPU_SPEED.equals(fieldName)
                || ACTIVE_VMS.equals(fieldName) || NETWORK_USAGE.equals(fieldName)
                || COMMITTED_MEM.equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if (STATUS.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(VDSStatus.class);
        } else if (EXTERNAL_STATUS.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(ExternalStatus.class);
        } else if (TYPE.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(VDSNiceType.class);
        } else if (ARCHITECTURE.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(ArchitectureType.class);
        } else if (UPDATE_AVAILABLE.equals(fieldName)) {
            retval = new BitValueAutoCompleter();
        }
        return retval;
    }
}

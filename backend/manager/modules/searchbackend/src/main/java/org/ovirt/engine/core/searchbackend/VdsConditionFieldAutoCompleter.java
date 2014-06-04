package org.ovirt.engine.core.searchbackend;

import java.math.BigDecimal;
import java.util.UUID;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VDSNiceType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;

public class VdsConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String NAME = "NAME";
    public static final String ADDRESS = "ADDRESS";
    public static final String CLUSTER = "CLUSTER";
    public static final String DATACENTER = "DATACENTER";
    public static final String STATUS = "STATUS";
    public static final String ACTIVE_VMS = "ACTIVE_VMS";
    public static final String MEM_USAGE = "MEM_USAGE";
    public static final String CPU_USAGE = "CPU_USAGE";
    public static final String NETWORK_USAGE = "NETWORK_USAGE";

    public VdsConditionFieldAutoCompleter() {
        super();
        mVerbs.add(NAME);
        mVerbs.add("COMMENT");
        mVerbs.add(STATUS);
        mVerbs.add(CLUSTER);
        mVerbs.add(ADDRESS);
        mVerbs.add(CPU_USAGE);
        mVerbs.add(MEM_USAGE);
        mVerbs.add(NETWORK_USAGE);
        mVerbs.add("LOAD");
        mVerbs.add("VERSION");
        mVerbs.add("CPUS");
        mVerbs.add("MEMORY");
        mVerbs.add("CPU_SPEED");
        mVerbs.add("CPU_MODEL");
        mVerbs.add(ACTIVE_VMS);
        mVerbs.add("MIGRATING_VMS");
        mVerbs.add("COMMITTED_MEM");
        mVerbs.add("TAG");
        mVerbs.add("TYPE");
        mVerbs.add(DATACENTER);
        mVerbs.add("ARCHITECTURE");
        // mVerbs.Add("NOTE", "NOTE");
        buildCompletions();
        mVerbs.add("ID");
        // Building the types dict
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put("COMMENT", String.class);
        getTypeDictionary().put(STATUS, VDSStatus.class);
        getTypeDictionary().put(CLUSTER, String.class);
        getTypeDictionary().put(ADDRESS, String.class);
        getTypeDictionary().put(CPU_USAGE, Integer.class);
        getTypeDictionary().put(MEM_USAGE, Integer.class);
        getTypeDictionary().put(NETWORK_USAGE, Integer.class);
        getTypeDictionary().put("LOAD", BigDecimal.class);
        getTypeDictionary().put("VERSION", String.class);
        getTypeDictionary().put("CPUS", Integer.class);
        getTypeDictionary().put("MEMORY", Integer.class);
        getTypeDictionary().put("CPU_SPEED", BigDecimal.class);
        getTypeDictionary().put("CPU_MODEL", String.class);
        getTypeDictionary().put(ACTIVE_VMS, Integer.class);
        getTypeDictionary().put("MIGRATING_VMS", Integer.class);
        getTypeDictionary().put("COMMITTED_MEM", Integer.class);
        getTypeDictionary().put("TAG", String.class);
        getTypeDictionary().put("TYPE", VDSNiceType.class);
        getTypeDictionary().put(DATACENTER, String.class);
        getTypeDictionary().put("ID", UUID.class);
        getTypeDictionary().put("ARCHITECTURE", ArchitectureType.class);
        // mTypeDict.Add("NOTE", typeof(string));
        // building the ColumnName Dict
        columnNameDict.put(NAME, "vds_name");
        columnNameDict.put("COMMENT", "free_text_comment");
        columnNameDict.put(STATUS, "status");
        columnNameDict.put(CLUSTER, "vds_group_name");
        columnNameDict.put(ADDRESS, "host_name");
        columnNameDict.put(CPU_USAGE, "usage_cpu_percent");
        columnNameDict.put(MEM_USAGE, "usage_mem_percent");
        columnNameDict.put(NETWORK_USAGE, "usage_network_percent");
        columnNameDict.put("LOAD", "cpu_load");
        columnNameDict.put("VERSION", "software_version");
        columnNameDict.put("CPUS", "cpu_cores");
        columnNameDict.put("MEMORY", "physical_mem_mb");
        columnNameDict.put("CPU_SPEED", "cpu_speed_mh");
        columnNameDict.put("CPU_MODEL", "cpu_model");
        columnNameDict.put(ACTIVE_VMS, "vm_active");
        columnNameDict.put("MIGRATING_VMS", "vm_migrating");
        columnNameDict.put("COMMITTED_MEM", "mem_commited");
        columnNameDict.put("TAG", "tag_name");
        columnNameDict.put("TYPE", "vds_type");
        columnNameDict.put(DATACENTER, "storage_pool_name");
        columnNameDict.put("ID", "vds_id");
        columnNameDict.put("ARCHITECTURE", "architecture");
        // mColumnNameDict.Add("NOTE", "note");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if ("MEMORY".equals(fieldName) || "CPUS".equals(fieldName)
                || CPU_USAGE.equals(fieldName) || MEM_USAGE.equals(fieldName)
                || "LOAD".equals(fieldName) || "CPU_SPEED".equals(fieldName)
                || ACTIVE_VMS.equals(fieldName) || NETWORK_USAGE.equals(fieldName)
                || "COMMITTED_MEM".equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        }
        else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if (STATUS.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(VDSStatus.class);
        }
        else if ("TYPE".equals(fieldName)) {
            retval = new EnumValueAutoCompleter(VDSNiceType.class);
        } else if ("ARCHITECTURE".equals(fieldName)) {
            retval = new EnumValueAutoCompleter(ArchitectureType.class);
        }
        return retval;
    }
}

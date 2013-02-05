package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.VDSNiceType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;

public class VdsConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public VdsConditionFieldAutoCompleter() {
        super();
        mVerbs.put("NAME", "NAME");
        mVerbs.put("STATUS", "STATUS");
        mVerbs.put("CLUSTER", "CLUSTER");
        mVerbs.put("ADDRESS", "ADDRESS");
        mVerbs.put("CPU_USAGE", "CPU_USAGE");
        mVerbs.put("MEM_USAGE", "MEM_USAGE");
        mVerbs.put("NETWORK_USAGE", "NETWORK_USAGE");
        mVerbs.put("LOAD", "LOAD");
        mVerbs.put("VERSION", "VERSION");
        mVerbs.put("CPUS", "CPUS");
        mVerbs.put("MEMORY", "MEMORY");
        mVerbs.put("CPU_SPEED", "CPU_SPEED");
        mVerbs.put("CPU_MODEL", "CPU_MODEL");
        mVerbs.put("ACTIVE_VMS", "ACTIVE_VMS");
        mVerbs.put("MIGRATING_VMS", "MIGRATING_VMS");
        mVerbs.put("COMMITTED_MEM", "COMMITTED_MEM");
        mVerbs.put("TAG", "TAG");
        mVerbs.put("TYPE", "TYPE");
        mVerbs.put("DATACENTER", "DATACENTER");
        // mVerbs.Add("NOTE", "NOTE");
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put("NAME", String.class);
        getTypeDictionary().put("STATUS", VDSStatus.class);
        getTypeDictionary().put("CLUSTER", String.class);
        getTypeDictionary().put("ADDRESS", String.class);
        getTypeDictionary().put("CPU_USAGE", Integer.class);
        getTypeDictionary().put("MEM_USAGE", Integer.class);
        getTypeDictionary().put("NETWORK_USAGE", Integer.class);
        getTypeDictionary().put("LOAD", java.math.BigDecimal.class);
        getTypeDictionary().put("VERSION", String.class);
        getTypeDictionary().put("CPUS", Integer.class);
        getTypeDictionary().put("MEMORY", Integer.class);
        getTypeDictionary().put("CPU_SPEED", java.math.BigDecimal.class);
        getTypeDictionary().put("CPU_MODEL", String.class);
        getTypeDictionary().put("ACTIVE_VMS", Integer.class);
        getTypeDictionary().put("MIGRATING_VMS", Integer.class);
        getTypeDictionary().put("COMMITTED_MEM", Integer.class);
        getTypeDictionary().put("TAG", String.class);
        getTypeDictionary().put("TYPE", VDSNiceType.class);
        getTypeDictionary().put("DATACENTER", String.class);
        // mTypeDict.Add("NOTE", typeof(string));
        // building the ColumnName Dict
        columnNameDict.put("NAME", "vds_name");
        columnNameDict.put("STATUS", "status");
        columnNameDict.put("CLUSTER", "vds_group_name");
        columnNameDict.put("ADDRESS", "host_name");
        columnNameDict.put("CPU_USAGE", "usage_cpu_percent");
        columnNameDict.put("MEM_USAGE", "usage_mem_percent");
        columnNameDict.put("NETWORK_USAGE", "usage_network_percent");
        columnNameDict.put("LOAD", "cpu_load");
        columnNameDict.put("VERSION", "software_version");
        columnNameDict.put("CPUS", "cpu_cores");
        columnNameDict.put("MEMORY", "physical_mem_mb");
        columnNameDict.put("CPU_SPEED", "cpu_speed_mh");
        columnNameDict.put("CPU_MODEL", "cpu_model");
        columnNameDict.put("ACTIVE_VMS", "vm_active");
        columnNameDict.put("MIGRATING_VMS", "vm_migrating");
        columnNameDict.put("COMMITTED_MEM", "mem_commited");
        columnNameDict.put("TAG", "tag_name");
        columnNameDict.put("TYPE", "vds_type");
        columnNameDict.put("DATACENTER", "storage_pool_name");
        // mColumnNameDict.Add("NOTE", "note");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if ("MEMORY".equals(fieldName) || "CPUS".equals(fieldName)
                || "CPU_USAGE".equals(fieldName) || "MEM_USAGE".equals(fieldName)
                || "LOAD".equals(fieldName) || "CPU_SPEED".equals(fieldName)
                || "ACTIVE_VMS".equals(fieldName) || "NETWORK_USAGE".equals(fieldName)
                || "COMMITTED_MEM".equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        }
        else if ("TAG".equals(fieldName)) {
            return StringOnlyEqualConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if ("STATUS".equals(fieldName)) {
            retval = new EnumValueAutoCompleter(VDSStatus.class);
        }
        else if ("TYPE".equals(fieldName)) {
            retval = new EnumValueAutoCompleter(VDSNiceType.class);
        } else {
        }
        return retval;
    }
}

package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.VDSNiceType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.StringHelper;

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
        mColumnNameDict.put("NAME", "vds_name");
        mColumnNameDict.put("STATUS", "status");
        mColumnNameDict.put("CLUSTER", "vds_group_name");
        mColumnNameDict.put("ADDRESS", "host_name");
        mColumnNameDict.put("CPU_USAGE", "usage_cpu_percent");
        mColumnNameDict.put("MEM_USAGE", "usage_mem_percent");
        mColumnNameDict.put("NETWORK_USAGE", "usage_network_percent");
        mColumnNameDict.put("LOAD", "cpu_load");
        mColumnNameDict.put("VERSION", "software_version");
        mColumnNameDict.put("CPUS", "cpu_cores");
        mColumnNameDict.put("MEMORY", "physical_mem_mb");
        mColumnNameDict.put("CPU_SPEED", "cpu_speed_mh");
        mColumnNameDict.put("CPU_MODEL", "cpu_model");
        mColumnNameDict.put("ACTIVE_VMS", "vm_active");
        mColumnNameDict.put("MIGRATING_VMS", "vm_migrating");
        mColumnNameDict.put("COMMITTED_MEM", "mem_commited");
        mColumnNameDict.put("TAG", "tag_name");
        mColumnNameDict.put("TYPE", "vds_type");
        mColumnNameDict.put("DATACENTER", "storage_pool_name");
        // mColumnNameDict.Add("NOTE", "note");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if (StringHelper.EqOp(fieldName, "MEMORY") || StringHelper.EqOp(fieldName, "CPUS")
                || StringHelper.EqOp(fieldName, "CPU_USAGE") || StringHelper.EqOp(fieldName, "MEM_USAGE")
                || StringHelper.EqOp(fieldName, "LOAD") || StringHelper.EqOp(fieldName, "CPU_SPEED")
                || StringHelper.EqOp(fieldName, "ACTIVE_VMS") || StringHelper.EqOp(fieldName, "NETWORK_USAGE")
                || StringHelper.EqOp(fieldName, "COMMITTED_MEM")) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        }
        else if (StringHelper.EqOp(fieldName, "TAG")) {
            return StringOnlyEqualConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if (StringHelper.EqOp(fieldName, "STATUS")) {
            retval = new EnumValueAutoCompleter(VDSStatus.class);
        }
        else if (StringHelper.EqOp(fieldName, "TYPE")) {
            retval = new EnumValueAutoCompleter(VDSNiceType.class);
        } else {
        }
        return retval;
    }
}

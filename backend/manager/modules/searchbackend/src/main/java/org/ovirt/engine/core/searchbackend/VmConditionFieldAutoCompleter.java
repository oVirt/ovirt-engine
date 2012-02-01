package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TimeSpan;

public class VmConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public VmConditionFieldAutoCompleter() {
        // Building the basic vervs Dict
        mVerbs.put("NAME", "NAME");
        mVerbs.put("STATUS", "STATUS");
        mVerbs.put("IP", "IP");
        mVerbs.put("UPTIME", "UPTIME");
        mVerbs.put("DOMAIN", "DOMAIN");
        mVerbs.put("OS", "OS");
        mVerbs.put("CREATIONDATE", "CREATIONDATE");
        mVerbs.put("ADDRESS", "ADDRESS");
        mVerbs.put("CPU_USAGE", "CPU_USAGE");
        mVerbs.put("MEM_USAGE", "MEM_USAGE");
        mVerbs.put("NETWORK_USAGE", "NETWORK_USAGE");
        mVerbs.put("MEMORY", "MEMORY");
        mVerbs.put("APPS", "APPS");
        mVerbs.put("CLUSTER", "CLUSTER");
        mVerbs.put("POOL", "POOL");
        mVerbs.put("LOGGEDINUSER", "LOGGEDINUSER");
        mVerbs.put("TAG", "TAG");
        mVerbs.put("DATACENTER", "DATACENTER");
        mVerbs.put("TYPE", "TYPE");
        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put("NAME", String.class);
        getTypeDictionary().put("STATUS", VMStatus.class);
        getTypeDictionary().put("IP", String.class);
        getTypeDictionary().put("UPTIME", TimeSpan.class);
        getTypeDictionary().put("DOMAIN", String.class);
        getTypeDictionary().put("OS", VmOsType.class);
        getTypeDictionary().put("CREATIONDATE", java.util.Date.class);
        getTypeDictionary().put("ADDRESS", String.class);
        getTypeDictionary().put("CPU_USAGE", Integer.class);
        getTypeDictionary().put("MEM_USAGE", Integer.class);
        getTypeDictionary().put("NETWORK_USAGE", Integer.class);
        getTypeDictionary().put("MEMORY", Integer.class);
        getTypeDictionary().put("APPS", String.class);
        getTypeDictionary().put("CLUSTER", String.class);
        getTypeDictionary().put("POOL", String.class);
        getTypeDictionary().put("LOGGEDINUSER", String.class);
        getTypeDictionary().put("TAG", String.class);
        getTypeDictionary().put("DATACENTER", String.class);
        getTypeDictionary().put("TYPE", VmType.class);
        // building the ColumnName Dict
        mColumnNameDict.put("NAME", "vm_name");
        mColumnNameDict.put("STATUS", "status");
        mColumnNameDict.put("IP", "vm_ip");
        mColumnNameDict.put("UPTIME", "elapsed_time");
        mColumnNameDict.put("DOMAIN", "vm_domain");
        mColumnNameDict.put("OS", "vm_os");
        mColumnNameDict.put("CREATIONDATE", "vm_creation_date");
        mColumnNameDict.put("ADDRESS", "vm_host");
        mColumnNameDict.put("CPU_USAGE", "usage_cpu_percent");
        mColumnNameDict.put("MEM_USAGE", "usage_mem_percent");
        mColumnNameDict.put("NETWORK_USAGE", "usage_network_percent");
        mColumnNameDict.put("MEMORY", "vm_mem_size_mb");
        mColumnNameDict.put("APPS", "app_list");
        mColumnNameDict.put("CLUSTER", "vds_group_name");
        mColumnNameDict.put("POOL", "vm_pool_name");
        // mColumnNameDict.Add("NOTE", "note");
        mColumnNameDict.put("LOGGEDINUSER", "guest_cur_user_name");
        mColumnNameDict.put("TAG", "tag_name");
        mColumnNameDict.put("DATACENTER", "storage_pool_name");
        mColumnNameDict.put("TYPE", "vm_type");
        /**
         */
        mNotFreeTextSearchableFieldsList.add("APPS");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if (StringHelper.EqOp(fieldName, "UPTIME") || StringHelper.EqOp(fieldName, "CREATIONDATE")) {
            return BiggerOrSmallerRelationAutoCompleter.INTSANCE;
        }
        else if (StringHelper.EqOp(fieldName, "CPU_USAGE") || StringHelper.EqOp(fieldName, "MEM_USAGE")
                || StringHelper.EqOp(fieldName, "MEMORY") || StringHelper.EqOp(fieldName, "NETWORK_USAGE")) {
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
        if (StringHelper.EqOp(fieldName, "OS")) {
            retval = new EnumValueAutoCompleter(VmOsType.class);
        }
        else if (StringHelper.EqOp(fieldName, "STATUS")) {
            retval = new EnumValueAutoCompleter(VMStatus.class);
        }
        else if (StringHelper.EqOp(fieldName, "TYPE")) {
            retval = new EnumValueAutoCompleter(VmType.class);
        } else {
        }
        return retval;
    }

    @Override
    public void formatValue(String fieldName, RefObject<String> relations, RefObject<String> value, boolean caseSensitive) {
        if (StringHelper.EqOp(fieldName, "APPS")) {
            value.argvalue =
                    StringFormat.format(BaseConditionFieldAutoCompleter.getI18NPrefix() + "'%%%1$s%%'",
                            StringHelper.trim(value.argvalue, '\'').replace("N'",
                                    ""));
            if (StringHelper.EqOp(relations.argvalue, "=")) {
                relations.argvalue = BaseConditionFieldAutoCompleter.getLikeSyntax(caseSensitive);
            } else if (StringHelper.EqOp(relations.argvalue, "!=")) {
                relations.argvalue = "NOT " + BaseConditionFieldAutoCompleter.getLikeSyntax(caseSensitive);
            }
        }
        else if (StringHelper.EqOp(fieldName, "UPTIME")) {
            value.argvalue = StringHelper.trim(value.argvalue, '\'');
            TimeSpan ts = TimeSpan.Parse(value.argvalue);
            value.argvalue = StringFormat.format("'%1$s'", ts.TotalSeconds);
        }
        else if (StringHelper.EqOp(fieldName, "CREATIONDATE")) {
            java.util.Date tmp = new java.util.Date(java.util.Date.parse(StringHelper.trim(value.argvalue, '\'')));
            value.argvalue = StringFormat.format("'%1$s'", tmp);
        } else {
            super.formatValue(fieldName, relations, value, caseSensitive);
        }
    }
}

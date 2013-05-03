package org.ovirt.engine.core.searchbackend;

import java.util.Date;
import java.util.UUID;

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
        mVerbs.add("NAME");
        mVerbs.add("STATUS");
        mVerbs.add("IP");
        mVerbs.add("UPTIME");
        mVerbs.add("DOMAIN");
        mVerbs.add("OS");
        mVerbs.add("CREATIONDATE");
        mVerbs.add("ADDRESS");
        mVerbs.add("CPU_USAGE");
        mVerbs.add("MEM_USAGE");
        mVerbs.add("NETWORK_USAGE");
        mVerbs.add("MEMORY");
        mVerbs.add("APPS");
        mVerbs.add("CLUSTER");
        mVerbs.add("POOL");
        mVerbs.add("LOGGEDINUSER");
        mVerbs.add("TAG");
        mVerbs.add("DATACENTER");
        mVerbs.add("TYPE");
        mVerbs.add("QUOTA");
        mVerbs.add("HOST");
        // Building the autoCompletion Dict
        buildCompletions();
        mVerbs.add("_VM_ID");

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
        getTypeDictionary().put("QUOTA", String.class);
        getTypeDictionary().put("HOST", String.class);
        getTypeDictionary().put("_VM_ID", UUID.class);

        // building the ColumnName Dict
        columnNameDict.put("NAME", "vm_name");
        columnNameDict.put("STATUS", "status");
        columnNameDict.put("IP", "vm_ip");
        columnNameDict.put("UPTIME", "elapsed_time");
        columnNameDict.put("DOMAIN", "vm_domain");
        columnNameDict.put("OS", "vm_os");
        columnNameDict.put("CREATIONDATE", "vm_creation_date");
        columnNameDict.put("ADDRESS", "vm_host");
        columnNameDict.put("CPU_USAGE", "usage_cpu_percent");
        columnNameDict.put("MEM_USAGE", "usage_mem_percent");
        columnNameDict.put("NETWORK_USAGE", "usage_network_percent");
        columnNameDict.put("MEMORY", "vm_mem_size_mb");
        columnNameDict.put("APPS", "app_list");
        columnNameDict.put("CLUSTER", "vds_group_name");
        columnNameDict.put("POOL", "vm_pool_name");
        // mColumnNameDict.Add("NOTE", "note");
        columnNameDict.put("LOGGEDINUSER", "guest_cur_user_name");
        columnNameDict.put("TAG", "tag_name");
        columnNameDict.put("DATACENTER", "storage_pool_name");
        columnNameDict.put("TYPE", "vm_type");
        columnNameDict.put("QUOTA", "quota_name");
        columnNameDict.put("HOST", "run_on_vds_name");
        columnNameDict.put("_VM_ID", "vm_guid");

        // Override field names for purpose of sorting, if needed
        sortableFieldDict.put("IP", StringFormat.format("fn_get_comparable_ip_list(%s)", getDbFieldName("IP")));

        /**
         */
        notFreeTextSearchableFieldsList.add("APPS");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if ("UPTIME".equals(fieldName) || "CREATIONDATE".equals(fieldName)) {
            return BiggerOrSmallerRelationAutoCompleter.INSTANCE;
        } else if ("CPU_USAGE".equals(fieldName) || "MEM_USAGE".equals(fieldName)
                || "MEMORY".equals(fieldName) || "NETWORK_USAGE".equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else if ("TAG".equals(fieldName)) {
            return StringOnlyEqualConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if ("OS".equals(fieldName)) {
            return new EnumValueAutoCompleter(VmOsType.class);
        } else if ("STATUS".equals(fieldName)) {
            return new EnumValueAutoCompleter(VMStatus.class);
        } else if ("TYPE".equals(fieldName)) {
            return new EnumValueAutoCompleter(VmType.class);
        } else if ("QUOTA".equals(fieldName)) {
            return new NullableStringAutoCompleter();
        }
        return null;
    }

    @Override
    public void formatValue(String fieldName,
            RefObject<String> relations,
            RefObject<String> value,
            boolean caseSensitive) {
        if ("APPS".equals(fieldName)) {
            value.argvalue =
                    StringFormat.format(BaseConditionFieldAutoCompleter.getI18NPrefix() + "'%%%1$s%%'",
                            StringHelper.trim(value.argvalue, '\'').replace("N'",
                                    ""));
            if ("=".equals(relations.argvalue)) {
                relations.argvalue = BaseConditionFieldAutoCompleter.getLikeSyntax(caseSensitive);
            } else if ("!=".equals(relations.argvalue)) {
                relations.argvalue = "NOT " + BaseConditionFieldAutoCompleter.getLikeSyntax(caseSensitive);
            }
        }
        else if ("UPTIME".equals(fieldName)) {
            value.argvalue = StringHelper.trim(value.argvalue, '\'');
            TimeSpan ts = TimeSpan.Parse(value.argvalue);
            value.argvalue = StringFormat.format("'%1$s'", ts.TotalSeconds);
        }
        else if ("CREATIONDATE".equals(fieldName)) {
            Date tmp = new Date(Date.parse(StringHelper.trim(value.argvalue, '\'')));
            value.argvalue = StringFormat.format("'%1$s'", tmp);
        } else {
            super.formatValue(fieldName, relations, value, caseSensitive);
        }
    }
}

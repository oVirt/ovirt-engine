package org.ovirt.engine.core.searchbackend;

import java.util.UUID;

import org.ovirt.engine.core.common.businessentities.DateEnumForSearch;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;

public class VmTemplateConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public VmTemplateConditionFieldAutoCompleter() {
        mVerbs.put("NAME", "NAME");
        mVerbs.put("DOMAIN", "DOMAIN");
        mVerbs.put("OS", "OS");
        mVerbs.put("CREATIONDATE", "CREATIONDATE");
        mVerbs.put("CHILDCOUNT", "CHILEDCOUNT");
        mVerbs.put("MEM", "MEM");
        mVerbs.put("DESCRIPTION", "DESCRIPTION");
        mVerbs.put("STATUS", "STATUS");
        mVerbs.put("CLUSTER", "CLUSTER");
        mVerbs.put("DATACENTER", "DATACENTER");
        mVerbs.put("QUOTA", "QUOTA");

        buildCompletions();
        mVerbs.put("_VMT_ID", "_VMT_ID");
        // Building the types dict
        getTypeDictionary().put("NAME", String.class);
        getTypeDictionary().put("DOMAIN", String.class);
        getTypeDictionary().put("OS", VmOsType.class);
        getTypeDictionary().put("CREATIONDATE", java.util.Date.class);
        getTypeDictionary().put("CHILDCOUNT", Integer.class);
        getTypeDictionary().put("MEM", Integer.class);
        getTypeDictionary().put("DESCRIPTION", String.class);
        getTypeDictionary().put("STATUS", VmTemplateStatus.class);
        getTypeDictionary().put("CLUSTER", String.class);
        getTypeDictionary().put("DATACENTER", String.class);
        getTypeDictionary().put("QUOTA", String.class);
        getTypeDictionary().put("_VMT_ID", UUID.class);

        // building the ColumnName Dict
        columnNameDict.put("NAME", "name");
        columnNameDict.put("DOMAIN", "domain");
        columnNameDict.put("OS", "os");
        columnNameDict.put("CREATIONDATE", "creation_date");
        columnNameDict.put("CHILDCOUNT", "child_count");
        columnNameDict.put("MEM", "mem_size_mb");
        columnNameDict.put("DESCRIPTION", "description");
        columnNameDict.put("STATUS", "status");
        columnNameDict.put("CLUSTER", "vds_group_name");
        columnNameDict.put("DATACENTER", "storage_pool_name");
        columnNameDict.put("QUOTA", "quota_name");
        columnNameDict.put("_VMT_ID", "vmt_guid");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if ("CREATIONDATE".equals(fieldName)) {
            return TimeConditionRelationAutoCompleter.INSTANCE;
        } else if ("CHILDCOUNT".equals(fieldName) || "MEM".equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if ("OS".equals(fieldName)) {
            return new EnumValueAutoCompleter(VmOsType.class);
        } else if ("CREATIONDATE".equals(fieldName)) {
            return new DateEnumValueAutoCompleter(DateEnumForSearch.class);
        } else if ("STATUS".equals(fieldName)) {
            return new EnumValueAutoCompleter(VmTemplateStatus.class);
        } else if ("QUOTA".equals(fieldName)) {
            return new NullableStringAutoCompleter();
        }
        return null;
    }
}

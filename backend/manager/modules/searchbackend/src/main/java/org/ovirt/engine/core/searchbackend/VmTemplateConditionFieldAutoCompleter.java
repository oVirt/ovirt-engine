package org.ovirt.engine.core.searchbackend;

import java.util.UUID;

import org.ovirt.engine.core.common.businessentities.DateEnumForSearch;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;

public class VmTemplateConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public VmTemplateConditionFieldAutoCompleter() {
        mVerbs.add("NAME");
        mVerbs.add("COMMENT");
        mVerbs.add("DOMAIN");
        mVerbs.add("OS");
        mVerbs.add("CREATIONDATE");
        mVerbs.add("CHILDCOUNT");
        mVerbs.add("MEM");
        mVerbs.add("DESCRIPTION");
        mVerbs.add("STATUS");
        mVerbs.add("CLUSTER");
        mVerbs.add("DATACENTER");
        mVerbs.add("QUOTA");

        buildCompletions();
        mVerbs.add("_VMT_ID");
        // Building the types dict
        getTypeDictionary().put("NAME", String.class);
        getTypeDictionary().put("COMMENT", String.class);
        getTypeDictionary().put("DOMAIN", String.class);
        getTypeDictionary().put("OS", String.class);
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
        columnNameDict.put("COMMENT", "free_text_comment");
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
            return SimpleDependecyInjector.getInstance().get(OsValueAutoCompleter.class);
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

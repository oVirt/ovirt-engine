package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

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

        buildCompletions();
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

        // building the ColumnName Dict
        mColumnNameDict.put("NAME", "name");
        mColumnNameDict.put("DOMAIN", "domain");
        mColumnNameDict.put("OS", "os");
        mColumnNameDict.put("CREATIONDATE", "creation_date");
        mColumnNameDict.put("CHILDCOUNT", "child_count");
        mColumnNameDict.put("MEM", "mem_size_mb");
        mColumnNameDict.put("DESCRIPTION", "description");
        mColumnNameDict.put("STATUS", "status");
        mColumnNameDict.put("CLUSTER", "vds_group_name");
        mColumnNameDict.put("DATACENTER", "storage_pool_name");

        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if (StringHelper.EqOp(fieldName, "CREATIONDATE")) {
            return TimeConditionRelationAutoCompleter.INSTANCE;
        } else if (StringHelper.EqOp(fieldName, "CHILDCOUNT") || StringHelper.EqOp(fieldName, "MEM")) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
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
        else if (StringHelper.EqOp(fieldName, "CREATIONDATE")) {
            retval = new DateEnumValueAutoCompleter(DateEnumForSearch.class);
        }
        else if (StringHelper.EqOp(fieldName, "STATUS")) {
            retval = new EnumValueAutoCompleter(VmTemplateStatus.class);

        } else {
        }
        return retval;
    }
}

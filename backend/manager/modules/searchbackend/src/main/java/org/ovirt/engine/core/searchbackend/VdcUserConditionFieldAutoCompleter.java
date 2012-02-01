package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class VdcUserConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public VdcUserConditionFieldAutoCompleter() {
        super();
        // Building the basic vervs Dict
        mVerbs.put("NAME", "NAME");
        mVerbs.put("LASTNAME", "LASTNAME");
        mVerbs.put("USRNAME", "USRNAME");
        mVerbs.put("DEPARTMENT", "DEPARTMENT");
        mVerbs.put("GROUP", "GROUP");
        mVerbs.put("TITLE", "TITLE");
        mVerbs.put("STATUS", "STATUS");
        mVerbs.put("ROLE", "ROLE");
        mVerbs.put("TAG", "TAG");
        mVerbs.put("POOL", "POOL");

        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put("NAME", String.class);
        getTypeDictionary().put("LASTNAME", String.class);
        getTypeDictionary().put("USRNAME", String.class);
        getTypeDictionary().put("DEPARTMENT", String.class);
        getTypeDictionary().put("TITLE", String.class);
        getTypeDictionary().put("GROUP", String.class);
        getTypeDictionary().put("STATUS", AdRefStatus.class);
        getTypeDictionary().put("ROLE", String.class);
        getTypeDictionary().put("TAG", String.class);
        getTypeDictionary().put("POOL", String.class);

        // building the ColumnName Dict
        mColumnNameDict.put("NAME", "name");
        mColumnNameDict.put("LASTNAME", "surname");
        mColumnNameDict.put("USRNAME", "username");
        mColumnNameDict.put("DEPARTMENT", "department");
        mColumnNameDict.put("TITLE", "role");
        mColumnNameDict.put("GROUP", "groups");
        mColumnNameDict.put("STATUS", "status");
        mColumnNameDict.put("ROLE", "mla_role");
        mColumnNameDict.put("TAG", "tag_name");
        mColumnNameDict.put("POOL", "vm_pool_name");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if (StringHelper.EqOp(fieldName, "TAG")) {
            return StringOnlyEqualConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if (StringHelper.EqOp(fieldName, "STATUS")) {
            retval = new EnumValueAutoCompleter(AdRefStatus.class);
        }
        return retval;
    }

    @Override
    public void formatValue(String fieldName, RefObject<String> relations, RefObject<String> value, boolean caseSensitive) {
        if (StringHelper.EqOp(fieldName, "STATUS")) {
            String tmp = StringHelper.trim(value.argvalue, '\'');
            if ((StringHelper.EqOp(relations.argvalue, "=")) && (StringHelper.EqOp(tmp, "1"))) {
                relations.argvalue = ">=";
            }
            if ((StringHelper.EqOp(relations.argvalue, "!=")) && (StringHelper.EqOp(tmp, "1"))) {
                relations.argvalue = "<";
            }
        } else {
            super.formatValue(fieldName, relations, value, caseSensitive);
        }
    }
}

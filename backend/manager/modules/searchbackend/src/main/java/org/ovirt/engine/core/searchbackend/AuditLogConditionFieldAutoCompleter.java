package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.*;

public class AuditLogConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public AuditLogConditionFieldAutoCompleter() {
        super();
        // Building the basic vervs Dict
        mVerbs.put("TYPE", "TYPE");
        mVerbs.put("SEVERITY", "SEVERITY");
        mVerbs.put("MESSAGE", "MESSAGE");
        mVerbs.put("TIME", "TIME");
        mVerbs.put("USRNAME", "USRNAME");
        mVerbs.put("EVENT_HOST", "EVENT_HOST");
        mVerbs.put("EVENT_VM", "EVENT_VM");
        mVerbs.put("EVENT_TEMPLATE", "EVENT_TEMPLATE");
        mVerbs.put("EVENT_STORAGE", "EVENT_STORAGE");
        mVerbs.put("EVENT_DATACENTER", "EVENT_DATACENTER");
        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put("TYPE", Integer.class);
        getTypeDictionary().put("SEVERITY", AuditLogSeverity.class);
        getTypeDictionary().put("MESSAGE", String.class);
        getTypeDictionary().put("TIME", java.util.Date.class);
        getTypeDictionary().put("USRNAME", String.class);
        getTypeDictionary().put("EVENT_HOST", String.class);
        getTypeDictionary().put("EVENT_VM", String.class);
        getTypeDictionary().put("EVENT_TEMPLATE", String.class);
        getTypeDictionary().put("EVENT_STORAGE", String.class);
        getTypeDictionary().put("EVENT_DATACENTER", String.class);

        // building the ColumnName Dict
        mColumnNameDict.put("TYPE", "log_type");
        mColumnNameDict.put("SEVERITY", "severity");
        mColumnNameDict.put("MESSAGE", "message");
        mColumnNameDict.put("TIME", "log_time");
        mColumnNameDict.put("USRNAME", "user_name");
        mColumnNameDict.put("EVENT_HOST", "vds_name");
        mColumnNameDict.put("EVENT_VM", "vm_name");
        mColumnNameDict.put("EVENT_TEMPLATE", "vm_template_name");
        mColumnNameDict.put("EVENT_STORAGE", "storage_domain_name");
        mColumnNameDict.put("EVENT_DATACENTER", "storage_pool_name");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        IAutoCompleter retval = null;
        if (StringHelper.EqOp(fieldName, "SEVERITY")) {
            retval = new NumericConditionRelationAutoCompleter();
        }
        else if (StringHelper.EqOp(fieldName, "TIME")) {
            retval = new TimeConditionRelationAutoCompleter();
        }
        else if (StringHelper.EqOp(fieldName, "TYPE") || StringHelper.EqOp(fieldName, "MESSAGE")
                || StringHelper.EqOp(fieldName, "USRNAME") || StringHelper.EqOp(fieldName, "EVENT_HOST")
                || StringHelper.EqOp(fieldName, "EVENT_VM") || StringHelper.EqOp(fieldName, "EVENT_TEMPLATE")
                || StringHelper.EqOp(fieldName, "EVENT_STORAGE") || StringHelper.EqOp(fieldName, "EVENT_DATACENTER")) {
            retval = new StringConditionRelationAutoCompleter();
        } else {
        }
        return retval;
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if (StringHelper.EqOp(fieldName, "TYPE")) {
            // retval = new EnumValueAutoCompleter(typeof(AuditLogType));
        }
        else if (StringHelper.EqOp(fieldName, "SEVERITY")) {
            retval = new EnumValueAutoCompleter(AuditLogSeverity.class);
        }
        else if (StringHelper.EqOp(fieldName, "TIME")) {
            retval = new DateEnumValueAutoCompleter(DateEnumForSearch.class);
        } else {
        }
        return retval;
    }
}

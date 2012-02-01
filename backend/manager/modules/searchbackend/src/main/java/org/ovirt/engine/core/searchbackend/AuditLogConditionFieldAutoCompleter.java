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
        buildCompletions();
        //These search options remain hidden from the autocompletion
        //but still available for the user interface
        mVerbs.put("_EVENT_VM_ID", "_EVENT_VM_ID");
        mVerbs.put("_EVENT_TEMPLATE_ID", "_EVENT_TEMPLATE_ID");
        mVerbs.put("_EVENT_STORAGE_ID", "_EVENT_STORAGE_ID");
        mVerbs.put("_EVENT_HOST_ID", "_EVENT_HOST_ID");
        mVerbs.put("_EVENT_DATACENTER_ID", "_EVENT_DATACENTER_ID");
        mVerbs.put("USRID", "USRID");
        // Building the autoCompletion Dict
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
        getTypeDictionary().put("USRID", String.class);
        getTypeDictionary().put("_EVENT_HOST_ID", String.class);
        getTypeDictionary().put("_EVENT_VM_ID", String.class);
        getTypeDictionary().put("_EVENT_TEMPLATE_ID", String.class);
        getTypeDictionary().put("_EVENT_STORAGE_ID", String.class);
        getTypeDictionary().put("_EVENT_DATACENTER_ID", String.class);

        // building the ColumnName Dict
        mColumnNameDict.put("TYPE", "log_type");
        mColumnNameDict.put("SEVERITY", "severity");
        mColumnNameDict.put("MESSAGE", "message");
        mColumnNameDict.put("TIME", "log_time");
        mColumnNameDict.put("USRNAME", "user_name");
        mColumnNameDict.put("USRID", "user_id::varchar");
        mColumnNameDict.put("EVENT_HOST", "vds_name");
        mColumnNameDict.put("_EVENT_HOST_ID", "vds_id::varchar");
        mColumnNameDict.put("EVENT_VM", "vm_name");
        mColumnNameDict.put("_EVENT_VM_ID", "vm_id::varchar");
        mColumnNameDict.put("EVENT_TEMPLATE", "vm_template_name");
        mColumnNameDict.put("_EVENT_TEMPLATE_ID", "vm_template_id::varchar");
        mColumnNameDict.put("EVENT_STORAGE", "storage_domain_name");
        mColumnNameDict.put("_EVENT_STORAGE_ID", "storage_domain_id::varchar");
        mColumnNameDict.put("EVENT_DATACENTER", "storage_pool_name");
        mColumnNameDict.put("_EVENT_DATACENTER_ID", "storage_pool_id::varchar");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(final String fieldName) {
        if (StringHelper.EqOp(fieldName, "SEVERITY")) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        }
        else if (StringHelper.EqOp(fieldName, "TIME")) {
            return TimeConditionRelationAutoCompleter.INSTANCE;
        }
        else if (StringHelper.EqOp(fieldName, "TYPE") || StringHelper.EqOp(fieldName, "MESSAGE")
                || StringHelper.EqOp(fieldName, "USRNAME") || StringHelper.EqOp(fieldName, "EVENT_HOST")
                || StringHelper.EqOp(fieldName, "_EVENT_HOST_ID")
                || StringHelper.EqOp(fieldName, "EVENT_VM") || StringHelper.EqOp(fieldName, "_EVENT_VM_ID")
                || StringHelper.EqOp(fieldName, "EVENT_TEMPLATE") || StringHelper.EqOp(fieldName, "_EVENT_TEMPLATE_ID")
                || StringHelper.EqOp(fieldName, "EVENT_STORAGE") || StringHelper.EqOp(fieldName, "EVENT_DATACENTER")
                || StringHelper.EqOp(fieldName, "_EVENT_DATACENTER_ID")) {
            return StringConditionRelationAutoCompleter.INSTANCE;
        } else {
            return null;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        // if (StringHelper.EqOp(fieldName, "TYPE")) {
        // // retval = new EnumValueAutoCompleter(typeof(AuditLogType));
        // }
        // else
        if (StringHelper.EqOp(fieldName, "SEVERITY")) {
            return new EnumValueAutoCompleter(AuditLogSeverity.class);
        }
        else if (StringHelper.EqOp(fieldName, "TIME")) {
            return new DateEnumValueAutoCompleter(DateEnumForSearch.class);
        } else {
            return null;
        }
    }
}

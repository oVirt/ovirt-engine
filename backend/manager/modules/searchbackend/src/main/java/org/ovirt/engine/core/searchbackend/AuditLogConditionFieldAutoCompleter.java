package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.businessentities.DateEnumForSearch;
import org.ovirt.engine.core.compat.StringHelper;

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
        mVerbs.put("EVENT_VOLUME", "EVENT_VOLUME");
        mVerbs.put("CORRELATION_ID", "CORRELATION_ID");
        mVerbs.put("ORIGIN", "ORIGIN");
        mVerbs.put("CUSTOM_EVENT_ID", "CUSTOM_EVENT_ID");
        mVerbs.put("DELETED", "DELETED");
        buildCompletions();
        // These search options remain hidden from the autocompletion
        // but still available for the user interface
        mVerbs.put("_EVENT_VM_ID", "_EVENT_VM_ID");
        mVerbs.put("_EVENT_TEMPLATE_ID", "_EVENT_TEMPLATE_ID");
        mVerbs.put("_EVENT_STORAGE_ID", "_EVENT_STORAGE_ID");
        mVerbs.put("_EVENT_HOST_ID", "_EVENT_HOST_ID");
        mVerbs.put("_EVENT_DATACENTER_ID", "_EVENT_DATACENTER_ID");
        mVerbs.put("_EVENT_QUOTA_ID", "_EVENT_QUOTA_ID");
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
        getTypeDictionary().put("_EVENT_QUOTA_ID", String.class);
        getTypeDictionary().put("EVENT_VOLUME", String.class);
        getTypeDictionary().put("_EVENT_VOLUME_ID", String.class);
        getTypeDictionary().put("CORRELATION_ID", String.class);
        getTypeDictionary().put("ORIGIN", String.class);
        getTypeDictionary().put("CUSTOM_EVENT_ID", Integer.class);
        getTypeDictionary().put("DELETED", Boolean.class);
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
        mColumnNameDict.put("_EVENT_QUOTA_ID", "quota_id::varchar");
        mColumnNameDict.put("EVENT_VOLUME", "gluster_volume_name");
        mColumnNameDict.put("_EVENT_VOLUME_ID", "gluster_volume_id::varchar");
        mColumnNameDict.put("CORRELATION_ID", "correlation_id::varchar");
        mColumnNameDict.put("ORIGIN", "origin::varchar");
        mColumnNameDict.put("CUSTOM_EVENT_ID", "custom_event_id::int");
        mColumnNameDict.put("DELETED", "deleted::boolean");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @SuppressWarnings("deprecation")
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
                || StringHelper.EqOp(fieldName, "_EVENT_DATACENTER_ID")
                || StringHelper.EqOp(fieldName, "_EVENT_QUOTA_ID")
                || StringHelper.EqOp(fieldName, "EVENT_VOLUME") || StringHelper.EqOp(fieldName, "_EVENT_VOLUME_ID") ||
                StringHelper.EqOp(fieldName, "CORRELATION_ID") || StringHelper.EqOp(fieldName, "ORIGIN") ||
                StringHelper.EqOp(fieldName, "CUSTOM_EVENT_ID") || StringHelper.EqOp(fieldName, "DELETED")) {
            return StringConditionRelationAutoCompleter.INSTANCE;
        } else {
            return null;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
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

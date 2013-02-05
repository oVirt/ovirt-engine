package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.businessentities.DateEnumForSearch;

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
        columnNameDict.put("TYPE", "log_type");
        columnNameDict.put("SEVERITY", "severity");
        columnNameDict.put("MESSAGE", "message");
        columnNameDict.put("TIME", "log_time");
        columnNameDict.put("USRNAME", "user_name");
        columnNameDict.put("USRID", "user_id::varchar");
        columnNameDict.put("EVENT_HOST", "vds_name");
        columnNameDict.put("_EVENT_HOST_ID", "vds_id::varchar");
        columnNameDict.put("EVENT_VM", "vm_name");
        columnNameDict.put("_EVENT_VM_ID", "vm_id::varchar");
        columnNameDict.put("EVENT_TEMPLATE", "vm_template_name");
        columnNameDict.put("_EVENT_TEMPLATE_ID", "vm_template_id::varchar");
        columnNameDict.put("EVENT_STORAGE", "storage_domain_name");
        columnNameDict.put("_EVENT_STORAGE_ID", "storage_domain_id::varchar");
        columnNameDict.put("EVENT_DATACENTER", "storage_pool_name");
        columnNameDict.put("_EVENT_DATACENTER_ID", "storage_pool_id::varchar");
        columnNameDict.put("_EVENT_QUOTA_ID", "quota_id::varchar");
        columnNameDict.put("EVENT_VOLUME", "gluster_volume_name");
        columnNameDict.put("_EVENT_VOLUME_ID", "gluster_volume_id::varchar");
        columnNameDict.put("CORRELATION_ID", "correlation_id::varchar");
        columnNameDict.put("ORIGIN", "origin::varchar");
        columnNameDict.put("CUSTOM_EVENT_ID", "custom_event_id::int");
        columnNameDict.put("DELETED", "deleted::boolean");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @SuppressWarnings("deprecation")
    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(final String fieldName) {
        if ("SEVERITY".equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        }
        else if ("TIME".equals(fieldName)) {
            return TimeConditionRelationAutoCompleter.INSTANCE;
        }
        else if ("TYPE".equals(fieldName) || "MESSAGE".equals(fieldName)
                || "USRNAME".equals(fieldName) || "EVENT_HOST".equals(fieldName)
                || "_EVENT_HOST_ID".equals(fieldName)
                || "EVENT_VM".equals(fieldName) || "_EVENT_VM_ID".equals(fieldName)
                || "EVENT_TEMPLATE".equals(fieldName) || "_EVENT_TEMPLATE_ID".equals(fieldName)
                || "EVENT_STORAGE".equals(fieldName) || "EVENT_DATACENTER".equals(fieldName)
                || "_EVENT_DATACENTER_ID".equals(fieldName)
                || "_EVENT_QUOTA_ID".equals(fieldName)
                || "EVENT_VOLUME".equals(fieldName) || "_EVENT_VOLUME_ID".equals(fieldName) ||
                "CORRELATION_ID".equals(fieldName) || "ORIGIN".equals(fieldName) ||
                "CUSTOM_EVENT_ID".equals(fieldName) || "DELETED".equals(fieldName)) {
            return StringConditionRelationAutoCompleter.INSTANCE;
        } else {
            return null;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if ("SEVERITY".equals(fieldName)) {
            return new EnumValueAutoCompleter(AuditLogSeverity.class);
        }
        else if ("TIME".equals(fieldName)) {
            return new DateEnumValueAutoCompleter(DateEnumForSearch.class);
        } else {
            return null;
        }
    }
}

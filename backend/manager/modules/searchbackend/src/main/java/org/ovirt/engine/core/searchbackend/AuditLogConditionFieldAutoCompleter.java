package org.ovirt.engine.core.searchbackend;

import java.util.Date;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.businessentities.DateEnumForSearch;

public class AuditLogConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public static final String TIME = "TIME";
    public static final String TYPE = "TYPE";
    public static final String SEVERITY = "SEVERITY";
    public static final String MESSAGE = "MESSAGE";
    public static final String CORRELATION_ID = "CORRELATION_ID";
    public static final String ORIGIN = "ORIGIN";
    public static final String CUSTOM_EVENT_ID = "CUSTOM_EVENT_ID";

    public AuditLogConditionFieldAutoCompleter() {
        super();
        // Building the basic vervs Dict
        mVerbs.add(TYPE);
        mVerbs.add(SEVERITY);
        mVerbs.add(MESSAGE);
        mVerbs.add(TIME);
        mVerbs.add("USRNAME");
        mVerbs.add("EVENT_HOST");
        mVerbs.add("EVENT_VM");
        mVerbs.add("EVENT_TEMPLATE");
        mVerbs.add("EVENT_STORAGE");
        mVerbs.add("EVENT_DATACENTER");
        mVerbs.add("EVENT_VOLUME");
        mVerbs.add(CORRELATION_ID);
        mVerbs.add(ORIGIN);
        mVerbs.add(CUSTOM_EVENT_ID);
        mVerbs.add("DELETED");
        buildCompletions();
        // These search options remain hidden from the autocompletion
        // but still available for the user interface
        mVerbs.add("_EVENT_VM_ID");
        mVerbs.add("_EVENT_TEMPLATE_ID");
        mVerbs.add("_EVENT_STORAGE_ID");
        mVerbs.add("_EVENT_HOST_ID");
        mVerbs.add("_EVENT_DATACENTER_ID");
        mVerbs.add("_EVENT_QUOTA_ID");
        mVerbs.add("USRID");
        // Building the autoCompletion Dict
        // Building the types dict
        getTypeDictionary().put(TYPE, Integer.class);
        getTypeDictionary().put(SEVERITY, AuditLogSeverity.class);
        getTypeDictionary().put(MESSAGE, String.class);
        getTypeDictionary().put(TIME, Date.class);
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
        getTypeDictionary().put(CORRELATION_ID, String.class);
        getTypeDictionary().put(ORIGIN, String.class);
        getTypeDictionary().put(CUSTOM_EVENT_ID, Integer.class);
        getTypeDictionary().put("DELETED", Boolean.class);
        // building the ColumnName Dict
        columnNameDict.put(TYPE, "log_type");
        columnNameDict.put(SEVERITY, "severity");
        columnNameDict.put(MESSAGE, "message");
        columnNameDict.put(TIME, "log_time");
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
        columnNameDict.put(CORRELATION_ID, "correlation_id::varchar");
        columnNameDict.put(ORIGIN, "origin::varchar");
        columnNameDict.put(CUSTOM_EVENT_ID, "custom_event_id::int");
        columnNameDict.put("DELETED", "deleted::boolean");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @SuppressWarnings("deprecation")
    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(final String fieldName) {
        if (SEVERITY.equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        }
        else if (TIME.equals(fieldName)) {
            return TimeConditionRelationAutoCompleter.INSTANCE;
        }
        else if (TYPE.equals(fieldName) || MESSAGE.equals(fieldName)
                || "USRNAME".equals(fieldName) || "EVENT_HOST".equals(fieldName)
                || "_EVENT_HOST_ID".equals(fieldName)
                || "EVENT_VM".equals(fieldName) || "_EVENT_VM_ID".equals(fieldName)
                || "EVENT_TEMPLATE".equals(fieldName) || "_EVENT_TEMPLATE_ID".equals(fieldName)
                || "EVENT_STORAGE".equals(fieldName) || "EVENT_DATACENTER".equals(fieldName)
                || "_EVENT_DATACENTER_ID".equals(fieldName)
                || "_EVENT_QUOTA_ID".equals(fieldName)
                || "EVENT_VOLUME".equals(fieldName) || "_EVENT_VOLUME_ID".equals(fieldName) ||
                CORRELATION_ID.equals(fieldName) || ORIGIN.equals(fieldName) ||
                CUSTOM_EVENT_ID.equals(fieldName) || "DELETED".equals(fieldName)) {
            return StringConditionRelationAutoCompleter.INSTANCE;
        } else {
            return null;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if (SEVERITY.equals(fieldName)) {
            return new EnumValueAutoCompleter(AuditLogSeverity.class);
        }
        else if (TIME.equals(fieldName)) {
            return new DateEnumValueAutoCompleter(DateEnumForSearch.class);
        } else {
            return null;
        }
    }
}

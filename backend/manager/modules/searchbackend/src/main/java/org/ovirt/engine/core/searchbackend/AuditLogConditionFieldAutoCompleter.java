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
    public static final String USER_NAME = "USRNAME";
    public static final String EVENT_HOST = "EVENT_HOST";
    public static final String EVENT_VM = "EVENT_VM";
    public static final String EVENT_TEMPLATE = "EVENT_TEMPLATE";
    public static final String EVENT_STORAGE = "EVENT_STORAGE";
    public static final String EVENT_DATACENTER = "EVENT_DATACENTER";
    public static final String EVENT_VOLUME = "EVENT_VOLUME";
    public static final String DELETED = "DELETED";
    public static final String EVENT_VM_ID = "_EVENT_VM_ID";
    public static final String EVENT_TEMPLATE_ID = "_EVENT_TEMPLATE_ID";
    public static final String EVENT_STORAGE_ID = "_EVENT_STORAGE_ID";
    public static final String EVENT_HOST_ID = "_EVENT_HOST_ID";
    public static final String EVENT_DATACENTER_ID = "_EVENT_DATACENTER_ID";
    public static final String EVENT_QUOTA_ID = "_EVENT_QUOTA_ID";
    public static final String USRID = "USRID";
    public static final String EVENT_VOLUME_ID = "_EVENT_VOLUME_ID";

    public AuditLogConditionFieldAutoCompleter() {
        super();
        // Building the basic vervs Dict
        verbs.add(TYPE);
        verbs.add(SEVERITY);
        verbs.add(MESSAGE);
        verbs.add(TIME);
        verbs.add(USER_NAME);
        verbs.add(EVENT_HOST);
        verbs.add(EVENT_VM);
        verbs.add(EVENT_TEMPLATE);
        verbs.add(EVENT_STORAGE);
        verbs.add(EVENT_DATACENTER);
        verbs.add(EVENT_VOLUME);
        verbs.add(CORRELATION_ID);
        verbs.add(ORIGIN);
        verbs.add(CUSTOM_EVENT_ID);
        verbs.add(DELETED);
        buildCompletions();
        // These search options remain hidden from the autocompletion
        // but still available for the user interface
        verbs.add(EVENT_VM_ID);
        verbs.add(EVENT_TEMPLATE_ID);
        verbs.add(EVENT_STORAGE_ID);
        verbs.add(EVENT_HOST_ID);
        verbs.add(EVENT_DATACENTER_ID);
        verbs.add(EVENT_QUOTA_ID);
        verbs.add(USRID);
        // Building the autoCompletion Dict
        // Building the types dict
        getTypeDictionary().put(TYPE, Integer.class);
        getTypeDictionary().put(SEVERITY, AuditLogSeverity.class);
        getTypeDictionary().put(MESSAGE, String.class);
        getTypeDictionary().put(TIME, Date.class);
        getTypeDictionary().put(USER_NAME, String.class);
        getTypeDictionary().put(EVENT_HOST, String.class);
        getTypeDictionary().put(EVENT_VM, String.class);
        getTypeDictionary().put(EVENT_TEMPLATE, String.class);
        getTypeDictionary().put(EVENT_STORAGE, String.class);
        getTypeDictionary().put(EVENT_DATACENTER, String.class);
        getTypeDictionary().put(USRID, String.class);
        getTypeDictionary().put(EVENT_HOST_ID, String.class);
        getTypeDictionary().put(EVENT_VM_ID, String.class);
        getTypeDictionary().put(EVENT_TEMPLATE_ID, String.class);
        getTypeDictionary().put(EVENT_STORAGE_ID, String.class);
        getTypeDictionary().put(EVENT_DATACENTER_ID, String.class);
        getTypeDictionary().put(EVENT_QUOTA_ID, String.class);
        getTypeDictionary().put(EVENT_VOLUME, String.class);
        getTypeDictionary().put(EVENT_VOLUME_ID, String.class);
        getTypeDictionary().put(CORRELATION_ID, String.class);
        getTypeDictionary().put(ORIGIN, String.class);
        getTypeDictionary().put(CUSTOM_EVENT_ID, Integer.class);
        getTypeDictionary().put(DELETED, Boolean.class);
        // building the ColumnName Dict
        columnNameDict.put(TYPE, "log_type");
        columnNameDict.put(SEVERITY, "severity");
        columnNameDict.put(MESSAGE, "message");
        columnNameDict.put(TIME, "log_time");
        columnNameDict.put(USER_NAME, "user_name");
        columnNameDict.put(USRID, "user_id::varchar");
        columnNameDict.put(EVENT_HOST, "vds_name");
        columnNameDict.put(EVENT_HOST_ID, "vds_id::varchar");
        columnNameDict.put(EVENT_VM, "vm_name");
        columnNameDict.put(EVENT_VM_ID, "vm_id::varchar");
        columnNameDict.put(EVENT_TEMPLATE, "vm_template_name");
        columnNameDict.put(EVENT_TEMPLATE_ID, "vm_template_id::varchar");
        columnNameDict.put(EVENT_STORAGE, "storage_domain_name");
        columnNameDict.put(EVENT_STORAGE_ID, "storage_domain_id::varchar");
        columnNameDict.put(EVENT_DATACENTER, "storage_pool_name");
        columnNameDict.put(EVENT_DATACENTER_ID, "storage_pool_id::varchar");
        columnNameDict.put(EVENT_QUOTA_ID, "quota_id::varchar");
        columnNameDict.put(EVENT_VOLUME, "gluster_volume_name");
        columnNameDict.put(EVENT_VOLUME_ID, "gluster_volume_id::varchar");
        columnNameDict.put(CORRELATION_ID, "correlation_id");
        columnNameDict.put(ORIGIN, "origin");
        columnNameDict.put(CUSTOM_EVENT_ID, "custom_event_id::int");
        columnNameDict.put(DELETED, "deleted::boolean");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @SuppressWarnings("deprecation")
    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(final String fieldName) {
        if (SEVERITY.equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else if (TIME.equals(fieldName)) {
            return TimeConditionRelationAutoCompleter.INSTANCE;
        } else if (TYPE.equals(fieldName) || MESSAGE.equals(fieldName)
                || USER_NAME.equals(fieldName) || EVENT_HOST.equals(fieldName)
                || EVENT_HOST_ID.equals(fieldName)
                || EVENT_VM.equals(fieldName) || EVENT_VM_ID.equals(fieldName)
                || EVENT_TEMPLATE.equals(fieldName) || EVENT_TEMPLATE_ID.equals(fieldName)
                || EVENT_STORAGE.equals(fieldName) || EVENT_DATACENTER.equals(fieldName)
                || EVENT_DATACENTER_ID.equals(fieldName)
                || EVENT_QUOTA_ID.equals(fieldName)
                || EVENT_VOLUME.equals(fieldName) || EVENT_VOLUME_ID.equals(fieldName) ||
                CORRELATION_ID.equals(fieldName) || ORIGIN.equals(fieldName) ||
                CUSTOM_EVENT_ID.equals(fieldName) || DELETED.equals(fieldName)) {
            return StringConditionRelationAutoCompleter.INSTANCE;
        } else {
            return null;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if (SEVERITY.equals(fieldName)) {
            return new EnumValueAutoCompleter(AuditLogSeverity.class);
        } else if (TIME.equals(fieldName)) {
            return new DateEnumValueAutoCompleter(DateEnumForSearch.class);
        } else if (DELETED.equals(fieldName)) {
            return new BitValueAutoCompleter();
        } else {
            return null;
        }
    }
}

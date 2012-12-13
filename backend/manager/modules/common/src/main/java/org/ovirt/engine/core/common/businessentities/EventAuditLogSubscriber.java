package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class EventAuditLogSubscriber implements Serializable {
    private static final long serialVersionUID = 2766057261065080144L;

    public EventAuditLogSubscriber() {
    }

    public EventAuditLogSubscriber(int event_type, Guid subscriber_id, String event_up_name, int method_id,
            String method_address, String tag_name, long audit_log_id, NGuid user_id, String user_name, NGuid vm_id,
            String vm_name, NGuid vm_template_id, String vm_template_name, NGuid vds_id, String vds_name,
            Guid storage_pool_id, String storage_pool_name, Guid storage_domain_id, String storage_domain_name,
            java.util.Date log_time, int severity, String message) {
        this.event_typeField = event_type;
        this.subscriber_idField = subscriber_id;
        this.event_up_nameField = this.getevent_up_name();
        this.method_idField = method_id;
        this.method_addressField = method_address;
        this.tag_nameField = tag_name;
        this.audit_log_idField = audit_log_id;
        this.user_idField = user_id;
        this.user_nameField = user_name;
        this.vm_idField = vm_id;
        this.vm_nameField = vm_name;
        this.vm_template_idField = vm_template_id;
        this.vm_template_nameField = vm_template_name;
        this.vds_idField = vds_id;
        this.vds_nameField = vds_name;
        this.log_timeField = log_time;
        this.severityField = severity;
        this.messageField = message;
    }

    private int event_typeField;

    public int getevent_type() {
        return this.event_typeField;
    }

    public void setevent_type(int value) {
        this.event_typeField = value;
    }

    private Guid subscriber_idField = new Guid();

    public Guid getsubscriber_id() {
        return this.subscriber_idField;
    }

    public void setsubscriber_id(Guid value) {
        this.subscriber_idField = value;
    }

    private String event_up_nameField;

    public String getevent_up_name() {
        return this.event_up_nameField;
    }

    public void setevent_up_name(String value) {
        this.event_up_nameField = value;
    }

    private int method_idField;

    public int getmethod_id() {
        return this.method_idField;
    }

    public void setmethod_id(int value) {
        this.method_idField = value;
    }

    private String method_addressField;

    public String getmethod_address() {
        return this.method_addressField;
    }

    public void setmethod_address(String value) {
        this.method_addressField = value;
    }

    private String tag_nameField;

    public String gettag_name() {
        return this.tag_nameField;
    }

    public void settag_name(String value) {
        this.tag_nameField = value;
    }

    private long audit_log_idField;

    public long getaudit_log_id() {
        return this.audit_log_idField;
    }

    public void setaudit_log_id(long value) {
        this.audit_log_idField = value;
    }

    private NGuid user_idField;

    public NGuid getuser_id() {
        return this.user_idField;
    }

    public void setuser_id(NGuid value) {
        this.user_idField = value;
    }

    private String user_nameField;

    public String getuser_name() {
        return this.user_nameField;
    }

    public void setuser_name(String value) {
        this.user_nameField = value;
    }

    private NGuid vm_idField;

    public NGuid getvm_id() {
        return this.vm_idField;
    }

    public void setvm_id(NGuid value) {
        this.vm_idField = value;
    }

    private String vm_nameField;

    public String getvm_name() {
        return this.vm_nameField;
    }

    public void setvm_name(String value) {
        this.vm_nameField = value;
    }

    private NGuid vm_template_idField;

    public NGuid getvm_template_id() {
        return this.vm_template_idField;
    }

    public void setvm_template_id(NGuid value) {
        this.vm_template_idField = value;
    }

    private String vm_template_nameField;

    public String getvm_template_name() {
        return this.vm_template_nameField;
    }

    public void setvm_template_name(String value) {
        this.vm_template_nameField = value;
    }

    private NGuid vds_idField;

    public NGuid getvds_id() {
        return this.vds_idField;
    }

    public void setvds_id(NGuid value) {
        this.vds_idField = value;
    }

    private String vds_nameField;

    public String getvds_name() {
        return this.vds_nameField;
    }

    public void setvds_name(String value) {
        this.vds_nameField = value;
    }

    private Guid storage_pool_idField = new Guid();

    public Guid getstorage_pool_id() {
        return this.storage_pool_idField;
    }

    public void setstorage_pool_id(Guid value) {
        storage_pool_idField = value;
    }

    private String storage_pool_nameField;

    public String getstorage_pool_name() {
        return this.storage_pool_nameField;
    }

    public void setstorage_pool_name(String value) {
        this.storage_pool_nameField = value;
    }

    private Guid storage_domain_idField = new Guid();

    public Guid getstorage_domain_id() {
        return this.storage_domain_idField;
    }

    public void setstorage_domain_id(Guid value) {
        storage_domain_idField = value;
    }

    private String storage_domain_nameField;

    public String getstorage_domain_name() {
        return this.storage_domain_nameField;
    }

    public void setstorage_domain_name(String value) {
        this.storage_domain_nameField = value;
    }

    private java.util.Date log_timeField = new java.util.Date(0);

    public java.util.Date getlog_time() {
        return this.log_timeField;
    }

    public void setlog_time(java.util.Date value) {
        this.log_timeField = value;
    }

    private int severityField;

    public int getseverity() {
        return this.severityField;
    }

    public void setseverity(int value) {
        this.severityField = value;
    }

    private String messageField;

    public String getmessage() {
        return this.messageField;
    }

    public void setmessage(String value) {
        this.messageField = value;
    }

}

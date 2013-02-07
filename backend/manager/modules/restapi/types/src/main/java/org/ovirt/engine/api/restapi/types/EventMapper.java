package org.ovirt.engine.api.restapi.types;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.LogSeverity;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.restapi.utils.TypeConversionHelper;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.NGuid;

public class EventMapper {

    @Mapping(from = AuditLog.class, to = Event.class)
    public static Event map(AuditLog entity, Event event) {
        Event model = event != null ? event : new Event();
        model.setId(String.valueOf(entity.getaudit_log_id()));
        model.setCode(entity.getlog_type().getValue());
        model.setSeverity(map(entity.getseverity(), null).value());
        model.setTime(TypeConversionHelper.toXMLGregorianCalendar(entity
                .getlog_time(), null));
        model.setDescription(entity.getmessage());

        if (entity.getuser_id() != null
                && !entity.getuser_id().equals(NGuid.Empty)) {
            User user = new User();
            user.setId(entity.getuser_id().toString());
            model.setUser(user);
        }
        if (entity.getvm_id() != null && !entity.getvm_id().equals(NGuid.Empty)) {
            VM vm = new VM();
            vm.setId(entity.getvm_id().toString());
            model.setVm(vm);
        }
        if (entity.getstorage_domain_id() != null
                && !entity.getstorage_domain_id().equals(NGuid.Empty)) {
            StorageDomain sd = new StorageDomain();
            sd.setId(entity.getstorage_domain_id().toString());
            model.setStorageDomain(sd);
        }
        if (entity.getvds_id() != null
                && !entity.getvds_id().equals(NGuid.Empty)) {
            Host host = new Host();
            host.setId(entity.getvds_id().toString());
            model.setHost(host);
        }
        if (entity.getvm_template_id() != null
                && !entity.getvm_template_id().equals(NGuid.Empty)) {
            Template template = new Template();
            template.setId(entity.getvm_template_id().toString());
            model.setTemplate(template);
        }
        if (entity.getvds_group_id() != null
                && !entity.getvds_group_id().equals(NGuid.Empty)) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getvds_group_id().toString());
            model.setCluster(cluster);
        }
        if (entity.getstorage_pool_id() != null
                && !entity.getstorage_pool_id().equals(NGuid.Empty)) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(entity.getstorage_pool_id().toString());
            model.setDataCenter(dataCenter);
        }
        if (StringUtils.isNotEmpty(entity.getCorrelationId())) {
            model.setCorrelationId(entity.getCorrelationId());
        }
        if (StringUtils.isNotEmpty(entity.getOrigin())) {
            model.setOrigin(entity.getOrigin());
        }
        model.setCustomId(entity.getCustomEventId());
        model.setFloodRate(entity.getEventFloodInSec());
        if (StringUtils.isNotEmpty(entity.getCustomData())) {
            model.setCustomData(entity.getCustomData());
        }
        return model;
    }

    @Mapping(from = Event.class, to = AuditLog.class)
    public static AuditLog map(Event event, AuditLog entity) {
        AuditLog auditLog = (entity != null) ? entity : new AuditLog();
        auditLog.setseverity(map(event.getSeverity(), null));
        auditLog.setlog_time(event.isSetTime() ? event.getTime().toGregorianCalendar().getTime()
                : new Date((Calendar.getInstance().getTimeInMillis())));
        auditLog.setmessage(event.getDescription());
        NGuid guid = (event.isSetUser()) ? new NGuid(event.getUser().getId()) : NGuid.Empty;
        if (!guid.equals(NGuid.Empty)) {
            auditLog.setuser_id(guid);
        }
        guid = (event.isSetVm()) ? new NGuid(event.getVm().getId()) : NGuid.Empty;
        if (!guid.equals(NGuid.Empty)) {
            auditLog.setvm_id(guid);
        }
        guid = (event.isSetStorageDomain()) ? new NGuid(event.getStorageDomain().getId()) : NGuid.Empty;
        if (!guid.equals(NGuid.Empty)) {
            auditLog.setstorage_domain_id(guid);
        }
        guid = (event.isSetHost()) ? new NGuid(event.getHost().getId()) : NGuid.Empty;
        if (!guid.equals(NGuid.Empty)) {
            auditLog.setvds_id(guid);
        }
        guid = (event.isSetTemplate()) ? new NGuid(event.getTemplate().getId()) : NGuid.Empty;
        if (!guid.equals(NGuid.Empty)) {
            auditLog.setvm_template_id(guid);
        }
        guid = (event.isSetCluster()) ? new NGuid(event.getCluster().getId()) : NGuid.Empty;
        if (!guid.equals(NGuid.Empty)) {
            auditLog.setvds_group_id(guid);
        }
        guid = (event.isSetDataCenter()) ? new NGuid(event.getDataCenter().getId()) : NGuid.Empty;
        if (!guid.equals(NGuid.Empty)) {
            auditLog.setstorage_pool_id(guid);
        }
        if (event.isSetCorrelationId()) {
            auditLog.setCorrelationId(event.getCorrelationId());
        }
        if (event.isSetOrigin()) {
            auditLog.setOrigin(event.getOrigin());
        }
        if (event.isSetCustomId()) {
            auditLog.setCustomEventId(event.getCustomId());
        }
        if (event.isSetFloodRate()) {
            auditLog.setEventFloodInSec(event.getFloodRate());
        }
        if (event.isSetCustomData()) {
            auditLog.setCustomData(event.getCustomData());
        }
        return auditLog;
    }
    @Mapping(from = AuditLogSeverity.class, to = LogSeverity.class)
    public static LogSeverity map(AuditLogSeverity entityStatus,
            LogSeverity template) {
        switch (entityStatus) {
        case NORMAL:
            return LogSeverity.NORMAL;
        case WARNING:
            return LogSeverity.WARNING;
        case ERROR:
            return LogSeverity.ERROR;
        case ALERT:
            return LogSeverity.ALERT;
        default:
            return null;
        }
    }

    @Mapping(from = String.class, to = AuditLogSeverity.class)
    public static AuditLogSeverity map(String template, AuditLogSeverity entityStatus) {
        if (AuditLogSeverity.NORMAL.name().equalsIgnoreCase(template)) {
            return AuditLogSeverity.NORMAL;
        }
        else if (AuditLogSeverity.WARNING.name().equalsIgnoreCase(template)) {
            return AuditLogSeverity.WARNING;
        }
        else if (AuditLogSeverity.ERROR.name().equalsIgnoreCase(template)) {
            return AuditLogSeverity.ERROR;
        }
        else if (AuditLogSeverity.ALERT.name().equalsIgnoreCase(template)) {
            return AuditLogSeverity.ALERT;
        }
        else {
            return null;
        }
    }
}

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
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.restapi.utils.TypeConversionHelper;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;

public class EventMapper {

    @Mapping(from = AuditLog.class, to = Event.class)
    public static Event map(AuditLog entity, Event event) {
        Event model = event != null ? event : new Event();
        model.setId(String.valueOf(entity.getAuditLogId()));
        model.setCode(entity.getLogType().getValue());
        model.setSeverity(map(entity.getSeverity(), null));
        model.setTime(TypeConversionHelper.toXMLGregorianCalendar(entity
                .getLogTime(), null));
        model.setDescription(entity.getMessage());

        if (entity.getUserId() != null
                && !entity.getUserId().equals(Guid.Empty)) {
            User user = new User();
            user.setId(entity.getUserId().toString());
            model.setUser(user);
        }
        if (entity.getVmId() != null && !entity.getVmId().equals(Guid.Empty)) {
            Vm vm = new Vm();
            vm.setId(entity.getVmId().toString());
            model.setVm(vm);
        }
        if (entity.getStorageDomainId() != null
                && !entity.getStorageDomainId().equals(Guid.Empty)) {
            StorageDomain sd = new StorageDomain();
            sd.setId(entity.getStorageDomainId().toString());
            model.setStorageDomain(sd);
        }
        if (entity.getVdsId() != null
                && !entity.getVdsId().equals(Guid.Empty)) {
            Host host = new Host();
            host.setId(entity.getVdsId().toString());
            model.setHost(host);
        }
        if (entity.getVmTemplateId() != null
                && !entity.getVmTemplateId().equals(Guid.Empty)) {
            Template template = new Template();
            template.setId(entity.getVmTemplateId().toString());
            model.setTemplate(template);
        }
        if (entity.getClusterId() != null
                && !entity.getClusterId().equals(Guid.Empty)) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getClusterId().toString());
            model.setCluster(cluster);
        }
        if (entity.getStoragePoolId() != null
                && !entity.getStoragePoolId().equals(Guid.Empty)) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(entity.getStoragePoolId().toString());
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
        AuditLogSeverity severity = map(event.getSeverity(), null);
        if (severity != null) {
            auditLog.setSeverity(severity);
        }
        auditLog.setLogTime(event.isSetTime() ? event.getTime().toGregorianCalendar().getTime()
                : new Date(Calendar.getInstance().getTimeInMillis()));
        auditLog.setMessage(event.getDescription());
        if (event.isSetUser() && event.getUser().isSetId()) {
            Guid guid = GuidUtils.asGuid(event.getUser().getId());
            if (!Guid.isNullOrEmpty(guid)) {
                auditLog.setUserId(guid);
            }
        }
        if (event.isSetVm() && event.getVm().isSetId()) {
            Guid guid = GuidUtils.asGuid(event.getVm().getId());
            if (!Guid.isNullOrEmpty(guid)) {
                auditLog.setVmId(guid);
            }
        }
        if (event.isSetStorageDomain() && event.getStorageDomain().isSetId()) {
            Guid guid = GuidUtils.asGuid(event.getStorageDomain().getId());
            if (!Guid.isNullOrEmpty(guid)) {
                auditLog.setStorageDomainId(guid);
            }
        }
        if (event.isSetHost() && event.getHost().isSetId()) {
            Guid guid = GuidUtils.asGuid(event.getHost().getId());
            if (!Guid.isNullOrEmpty(guid)) {
                auditLog.setVdsId(guid);
            }
        }
        if (event.isSetTemplate() && event.getTemplate().isSetId()) {
            Guid guid = GuidUtils.asGuid(event.getTemplate().getId());
            if (!Guid.isNullOrEmpty(guid)) {
                auditLog.setVmTemplateId(guid);
            }
        }
        if (event.isSetCluster() && event.getCluster().isSetId()) {
            Guid guid = GuidUtils.asGuid(event.getCluster().getId());
            if (!Guid.isNullOrEmpty(guid)) {
                auditLog.setClusterId(guid);
            }
        }
        if (event.isSetDataCenter() && event.getDataCenter().isSetId()) {
            Guid guid = GuidUtils.asGuid(event.getDataCenter().getId());
            if (!Guid.isNullOrEmpty(guid)) {
                auditLog.setStoragePoolId(guid);
            }
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

    @Mapping(from = LogSeverity.class, to = AuditLogSeverity.class)
    public static AuditLogSeverity map(LogSeverity template, AuditLogSeverity entityStatus) {
        switch (template) {
        case ALERT:
            return AuditLogSeverity.ALERT;
        case ERROR:
            return AuditLogSeverity.ERROR;
        case NORMAL:
            return AuditLogSeverity.NORMAL;
        case WARNING:
            return AuditLogSeverity.WARNING;
        default:
            return null;
        }
    }
}

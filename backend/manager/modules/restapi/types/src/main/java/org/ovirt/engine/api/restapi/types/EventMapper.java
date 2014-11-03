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
        model.setSeverity(map(entity.getSeverity(), null).value());
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
            VM vm = new VM();
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
        if (entity.getVdsGroupId() != null
                && !entity.getVdsGroupId().equals(Guid.Empty)) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getVdsGroupId().toString());
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
        auditLog.setSeverity(map(event.getSeverity(), null));
        auditLog.setLogTime(event.isSetTime() ? event.getTime().toGregorianCalendar().getTime()
                : new Date((Calendar.getInstance().getTimeInMillis())));
        auditLog.setMessage(event.getDescription());
        Guid guid = (event.isSetUser()) ? GuidUtils.asGuid(event.getUser().getId()) : Guid.Empty;
        if (!guid.equals(Guid.Empty)) {
            auditLog.setUserId(guid);
        }
        guid = (event.isSetVm()) ? GuidUtils.asGuid(event.getVm().getId()) : Guid.Empty;
        if (!guid.equals(Guid.Empty)) {
            auditLog.setVmId(guid);
        }
        guid = (event.isSetStorageDomain()) ? GuidUtils.asGuid(event.getStorageDomain().getId()) : Guid.Empty;
        if (!guid.equals(Guid.Empty)) {
            auditLog.setStorageDomainId(guid);
        }
        guid = (event.isSetHost()) ? GuidUtils.asGuid(event.getHost().getId()) : Guid.Empty;
        if (!guid.equals(Guid.Empty)) {
            auditLog.setVdsId(guid);
        }
        guid = (event.isSetTemplate()) ? GuidUtils.asGuid(event.getTemplate().getId()) : Guid.Empty;
        if (!guid.equals(Guid.Empty)) {
            auditLog.setVmTemplateId(guid);
        }
        guid = (event.isSetCluster()) ? GuidUtils.asGuid(event.getCluster().getId()) : Guid.Empty;
        if (!guid.equals(Guid.Empty)) {
            auditLog.setVdsGroupId(guid);
        }
        guid = (event.isSetDataCenter()) ? GuidUtils.asGuid(event.getDataCenter().getId()) : Guid.Empty;
        if (!guid.equals(Guid.Empty)) {
            auditLog.setStoragePoolId(guid);
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

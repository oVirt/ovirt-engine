package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.LogSeverity;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.api.restapi.utils.TypeConversionHelper;

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
        return model;
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
}

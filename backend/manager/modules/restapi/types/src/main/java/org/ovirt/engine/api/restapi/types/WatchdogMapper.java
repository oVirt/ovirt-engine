package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;

public class WatchdogMapper {
    @Mapping(from = Watchdog.class, to = VmWatchdog.class)
    public static VmWatchdog map(Watchdog model, VmWatchdog template) {
        VmWatchdog entity = template == null ? new VmWatchdog() : template;
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetAction()) {
            entity.setAction(map(model.getAction()));
        }
        if (model.isSetModel()) {
            entity.setModel(map(model.getModel()));
        }
        return entity;
    }

    @Mapping(from = VmWatchdog.class, to = Watchdog.class)
    public static Watchdog map(VmWatchdog entity, Watchdog template) {
        Watchdog model = template == null ? new Watchdog() : template;
        if (entity.getAction() != null) {
            model.setAction(map(entity.getAction()));
        }
        if (entity.getModel() != null) {
            model.setModel(map(entity.getModel()));
        }
        model.setId(entity.getId().toString());
        return model;
    }

    public static VmWatchdogAction map(WatchdogAction action) {
        switch (action) {
        case DUMP:
            return VmWatchdogAction.DUMP;
        case NONE:
            return VmWatchdogAction.NONE;
        case PAUSE:
            return VmWatchdogAction.PAUSE;
        case POWEROFF:
            return VmWatchdogAction.POWEROFF;
        case RESET:
            return VmWatchdogAction.RESET;
        default:
            return null;
        }
    }

    public static WatchdogAction map(VmWatchdogAction action) {
        switch (action) {
        case DUMP:
            return WatchdogAction.DUMP;
        case NONE:
            return WatchdogAction.NONE;
        case PAUSE:
            return WatchdogAction.PAUSE;
        case POWEROFF:
            return WatchdogAction.POWEROFF;
        case RESET:
            return WatchdogAction.RESET;
        default:
            return null;
        }
    }

    public static VmWatchdogType map(WatchdogModel model) {
        switch (model) {
        case I6300ESB:
            return VmWatchdogType.i6300esb;
        case DIAG288:
            return VmWatchdogType.diag288;
        default:
            return null;
        }
    }

    public static WatchdogModel map(VmWatchdogType model) {
        switch (model) {
        case i6300esb:
            return WatchdogModel.I6300ESB;
        case diag288:
            return WatchdogModel.DIAG288;
        default:
            return null;
        }
    }

}

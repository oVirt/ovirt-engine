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
            WatchdogAction wdAction = WatchdogAction.fromValue(model.getAction());
            if (wdAction != null) {
                entity.setAction(map(wdAction));
            }
        }
        if (model.isSetModel()) {
            WatchdogModel wdModel = WatchdogModel.fromValue(model.getModel());
            if (wdModel != null) {
                entity.setModel(map(wdModel));
            }
        }
        return entity;
    }

    @Mapping(from = VmWatchdog.class, to = Watchdog.class)
    public static Watchdog map(VmWatchdog entity, Watchdog template) {
        Watchdog model = template == null ? new Watchdog() : template;
        if (entity.getAction() != null) {
            WatchdogAction action = map(entity.getAction());
            if (action != null) {
                model.setAction(action.name().toLowerCase());
            }
        }
        if (entity.getModel() != null) {
            WatchdogModel wdModel = map(entity.getModel());
            if (wdModel != null) {
                model.setModel(wdModel.name().toLowerCase());
            }
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
        default:
            return null;
        }
    }

    public static WatchdogModel map(VmWatchdogType model) {
        switch (model) {
        case i6300esb:
            return WatchdogModel.I6300ESB;
        default:
            return null;
        }
    }

}

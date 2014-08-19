package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.WatchDog;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;

public class WatchdogMapper {
    @Mapping(from = WatchDog.class, to = VmWatchdog.class)
    public static VmWatchdog map(WatchDog model, VmWatchdog template) {
        VmWatchdog entity = template == null ? new VmWatchdog() : template;
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetAction()) {
            WatchdogAction wdAction = WatchdogAction.fromValue(model.getAction());
            if (wdAction != null) {
                entity.setAction(map(wdAction, null));
            }
        }
        if (model.isSetModel()) {
            WatchdogModel wdModel = WatchdogModel.fromValue(model.getModel());
            if (wdModel != null) {
                entity.setModel(map(wdModel, null));
            }
        }
        return entity;
    }

    @Mapping(from = VmWatchdog.class, to = WatchDog.class)
    public static WatchDog map(VmWatchdog entity, WatchDog template) {
        WatchDog model = template == null ? new WatchDog() : template;
        if (entity.getAction() != null) {
            WatchdogAction action = map(entity.getAction(), null);
            if (action != null) {
                model.setAction(action.name().toLowerCase());
            }
        }
        if (entity.getModel() != null) {
            WatchdogModel wdModel = map(entity.getModel(), null);
            if (wdModel != null) {
                model.setModel(wdModel.name().toLowerCase());
            }
        }
        model.setId(entity.getId().toString());
        return model;
    }

    @Mapping(from = WatchdogAction.class, to = VmWatchdogAction.class)
    public static VmWatchdogAction map(WatchdogAction action, VmWatchdogAction incoming) {
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

    @Mapping(from = VmWatchdogAction.class, to = WatchdogAction.class)
    public static WatchdogAction map(VmWatchdogAction action, WatchdogAction incoming) {
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

    @Mapping(from = WatchdogModel.class, to = VmWatchdogType.class)
    public static VmWatchdogType map(WatchdogModel model, VmWatchdogType incoming) {
        switch (model) {
        case I6300ESB:
            return VmWatchdogType.i6300esb;
        default:
            return null;
        }
    }

    @Mapping(from = VmWatchdogType.class, to = WatchdogModel.class)
    public static WatchdogModel map(VmWatchdogType model, WatchdogModel incoming) {
        switch (model) {
        case i6300esb:
            return WatchdogModel.I6300ESB;
        default:
            return null;
        }
    }

}

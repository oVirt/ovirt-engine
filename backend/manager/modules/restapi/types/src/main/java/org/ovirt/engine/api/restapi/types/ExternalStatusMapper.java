package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.EntityExternalStatus;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;

/**
 * Created by emesika on 6/12/15.
 */
public class ExternalStatusMapper {

    @Mapping(from = ExternalStatus.class, to = EntityExternalStatus.class)
    public static EntityExternalStatus map(ExternalStatus entityStatus, EntityExternalStatus template) {
        switch (entityStatus) {
        case Ok:
            return EntityExternalStatus.OK;
        case Info:
            return EntityExternalStatus.INFO;
        case Warning:
            return EntityExternalStatus.WARNING;
        case Error:
            return EntityExternalStatus.ERROR;
        case Failure:
            return EntityExternalStatus.FAILURE;
        default:
            return null;
        }
    }


    @Mapping(from = EntityExternalStatus.class, to = ExternalStatus.class)
    public static ExternalStatus map(EntityExternalStatus entityStatus, ExternalStatus template) {
        switch (entityStatus) {
        case OK:
            return ExternalStatus.Ok;
        case INFO:
            return ExternalStatus.Info;
        case WARNING:
            return ExternalStatus.Warning;
        case  ERROR:
            return ExternalStatus.Error;
        case FAILURE:
            return ExternalStatus.Failure;
        default:
            return null;
        }
    }

}

package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.ExternalStatus;

/**
 * Created by emesika on 6/12/15.
 */
public class ExternalStatusMapper {

    public static ExternalStatus map(org.ovirt.engine.core.common.businessentities.ExternalStatus status) {
        switch (status) {
        case Ok:
            return ExternalStatus.OK;
        case Info:
            return ExternalStatus.INFO;
        case Warning:
            return ExternalStatus.WARNING;
        case Error:
            return ExternalStatus.ERROR;
        case Failure:
            return ExternalStatus.FAILURE;
        default:
            return null;
        }
    }

    public static org.ovirt.engine.core.common.businessentities.ExternalStatus map(ExternalStatus status) {
        switch (status) {
        case OK:
            return org.ovirt.engine.core.common.businessentities.ExternalStatus.Ok;
        case INFO:
            return org.ovirt.engine.core.common.businessentities.ExternalStatus.Info;
        case WARNING:
            return org.ovirt.engine.core.common.businessentities.ExternalStatus.Warning;
        case  ERROR:
            return org.ovirt.engine.core.common.businessentities.ExternalStatus.Error;
        case FAILURE:
            return org.ovirt.engine.core.common.businessentities.ExternalStatus.Failure;
        default:
            return null;
        }
    }
}

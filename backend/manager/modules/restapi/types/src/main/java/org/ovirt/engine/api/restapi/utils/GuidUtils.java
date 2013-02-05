package org.ovirt.engine.api.restapi.utils;

import org.ovirt.engine.core.compat.Guid;

public class GuidUtils {

    public static Guid asGuid(String id) {
        try {
            return new Guid(id);
        }catch (IllegalArgumentException e) {
            throw new MalformedIdException(e);
        }
    }

}

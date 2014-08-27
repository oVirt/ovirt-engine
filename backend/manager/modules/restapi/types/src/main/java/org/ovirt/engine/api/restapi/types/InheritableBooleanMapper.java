package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.InheritableBoolean;

public class InheritableBooleanMapper {
    public static Boolean map(InheritableBoolean inheritableBoolean) {
        if (inheritableBoolean == null) {
            return null;
        }
        switch (inheritableBoolean) {
            case TRUE:
                return true;
            case FALSE:
                return false;
            case INHERIT:
                return null;
        }
        return null;
    }

    public static InheritableBoolean map(Boolean bool) {
        if (bool == null) {
            return InheritableBoolean.INHERIT;
        } else if (bool) {
            return InheritableBoolean.TRUE;
        } else {
            return InheritableBoolean.FALSE;
        }
    }
}

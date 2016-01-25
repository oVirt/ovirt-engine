package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Method;
import org.ovirt.engine.api.model.Methods;
import org.ovirt.engine.api.model.Sso;
import org.ovirt.engine.core.common.businessentities.SsoMethod;

public class SsoMapper {

    @Mapping(from = SsoMethod.class, to = Sso.class)
    public static Sso map(SsoMethod entity, Sso template) {
        Sso model = (template == null)
                ? new Sso()
                : template;

        model.setMethods(new Methods());

        if (entity == SsoMethod.GUEST_AGENT) {
            Method method = new Method();
            method.setId(org.ovirt.engine.api.model.SsoMethod.GUEST_AGENT);
            model.getMethods().getMethods().add(method);
        }

        return model;
    }

    @Mapping(from = Sso.class, to = SsoMethod.class)
    public static SsoMethod map(Sso model, SsoMethod template) {
        if (model != null && model.getMethods() != null && model.getMethods().getMethods() != null) {
            if (model.getMethods().getMethods().size() == 0) {
                return SsoMethod.NONE;
            }
            if (model.getMethods().getMethods().size() == 1 && model.getMethods().getMethods().get(0).getId() == org.ovirt.engine.api.model.SsoMethod.GUEST_AGENT) {
                return SsoMethod.GUEST_AGENT;
            }
        }

        return null;
    }

}

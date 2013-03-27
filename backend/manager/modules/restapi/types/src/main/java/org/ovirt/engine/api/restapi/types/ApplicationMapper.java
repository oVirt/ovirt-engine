package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Application;
import org.ovirt.engine.core.compat.NGuid;

public class ApplicationMapper {

    @Mapping(from = String.class, to = Application.class)
    public static Application map(String appName, Application template) {
        Application model = template != null ? template : new Application();
        model.setName(appName);
        model.setId(new NGuid(appName.getBytes(), true).toString());
        return model;
    }
}

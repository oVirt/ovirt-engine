package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.compat.Guid;

public class GroupMapper {

    @Mapping(from = DbGroup.class, to = Group.class)
    public static Group map(DbGroup entity, Group template) {
        Group model = template != null ? template : new Group();
        model.setExternalId(entity.getExternalId().toString());
        model.setName(entity.getName());
        model.setId(entity.getId().toString());
        if (!StringUtils.isEmpty(entity.getDomain())) {
            Domain dom = new Domain();
            dom.setId(new Guid(entity.getDomain().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }

    @Mapping(from = LdapGroup.class, to = Group.class)
    public static Group map(LdapGroup entity, Group template) {
        Group model = template != null ? template : new Group();
        model.setName(entity.getname());
        model.setId(entity.getid().toString());
        if (!StringUtils.isEmpty(entity.getdomain())) {
            Domain dom = new Domain();
            dom.setId(new Guid(entity.getdomain().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }

}

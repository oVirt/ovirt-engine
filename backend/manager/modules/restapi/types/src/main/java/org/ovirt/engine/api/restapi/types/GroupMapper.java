package org.ovirt.engine.api.restapi.types;


import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.compat.NGuid;

public class GroupMapper {

    @Mapping(from = LdapGroup.class, to = Group.class)
    public static Group map(LdapGroup entity, Group template) {
        Group model = template != null ? template : new Group();
        model.setName(entity.getname());
        model.setId(entity.getid().toString());
        if (!StringUtils.isEmpty(entity.getdomain())) {
            Domain dom = new Domain();
            dom.setName(entity.getdomain());
            dom.setId(new NGuid(entity.getdomain().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }

    @Mapping(from = DbUser.class, to = Group.class)
    public static Group map(DbUser entity, Group template) {
        Group model = template != null ? template : new Group();
        model.setName(entity.getname());
        model.setId(entity.getuser_id().toString());
        if (!StringUtils.isEmpty(entity.getdomain())) {
            Domain dom = new Domain();
            dom.setName(entity.getdomain());
            dom.setId(new NGuid(entity.getdomain().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }
}

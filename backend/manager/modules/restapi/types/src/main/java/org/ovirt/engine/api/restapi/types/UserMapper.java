package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.authentication.DirectoryGroup;
import org.ovirt.engine.core.authentication.DirectoryUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;

public class UserMapper {

    @Mapping(from = DbUser.class, to = User.class)
    public static User map(DbUser entity, User template) {
        User model = template != null ? template : new User();
        model.setExternalId(entity.getExternalId().toHex());
        model.setName(entity.getFirstName());
        model.setUserName(entity.getLoginName() + "@" + entity.getDomain());
        model.setId(entity.getId().toString());
        model.setLastName(entity.getLastName());
        model.setEmail(entity.getEmail());
        model.setDepartment(entity.getDepartment());
        if (entity.getGroupNames() != null && entity.getGroupNames().trim().length() > 0) {
            model.setGroups(new Groups());
            for (String name : entity.getGroupNames().split(",")) {
                Group group = new Group();
                group.setName(name);
                model.getGroups().getGroups().add(group);
            }
        }
        if (!StringUtils.isEmpty(entity.getDomain())) {
            Domain dom = new Domain();
            dom.setId(new Guid(entity.getDomain().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }

    @Mapping(from = DirectoryUser.class, to = User.class)
    public static User map(DirectoryUser entity, User template) {
        User model = template != null ? template : new User();
        model.setName(entity.getFirstName());
        model.setUserName(entity.getName() + "@" + entity.getDirectory().getName());
        model.setId(entity.getId().toHex());
        model.setLastName(entity.getLastName());
        model.setEmail(entity.getEmail());
        model.setDepartment(entity.getDepartment());
        if (entity.getGroups() != null) {
            model.setGroups(new Groups());
            for (DirectoryGroup directoryGroup : entity.getGroups()) {
                Group group = new Group();
                group.setName(directoryGroup.getName());
                model.getGroups().getGroups().add(group);
            }
        }
        if (!StringUtils.isEmpty(entity.getDirectory().getName())) {
            Domain dom = new Domain();
            dom.setId(new Guid(entity.getDirectory().getName().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }

}

package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.restapi.utils.MalformedIdException;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.utils.ExternalId;
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
        model.setUserName(entity.getName() + "@" + entity.getDirectoryName());
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
        if (!StringUtils.isEmpty(entity.getDirectoryName())) {
            Domain dom = new Domain();
            dom.setId(new Guid(entity.getDirectoryName().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }

    @Mapping(from = User.class, to = DbUser.class)
    public static DbUser map(User model, DbUser template) {
        DbUser entity = template != null? template: new DbUser();
        if (model.isSetName()) {
            entity.setLoginName(model.getName());
        }
        if (model.isSetId()) {
            String id = model.getId();
            try {
                entity.setId(GuidUtils.asGuid(id));
            }
            catch (MalformedIdException exception) {
                // The identifier won't be a UUID if the user comes from /domains/{domain:id}/users.
            }
            if (!model.isSetExternalId()) {
                entity.setExternalId(ExternalId.fromHex(id));
            }
        }
        if (model.isSetExternalId()) {
            entity.setExternalId(ExternalId.fromHex(model.getExternalId()));
        }
        if (model.isSetDomain()) {
            Domain domain = model.getDomain();
            if (domain.isSetName()) {
                entity.setDomain(domain.getName());
            }
        }
        return entity;
    }

}

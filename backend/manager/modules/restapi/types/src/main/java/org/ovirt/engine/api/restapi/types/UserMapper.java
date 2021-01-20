package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.restapi.utils.DirectoryEntryIdUtils;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

public class UserMapper {

    @Mapping(from = DbUser.class, to = User.class)
    public static User map(DbUser entity, User template) {
        User model = template != null ? template : new User();
        model.setName(entity.getFirstName());
        model.setUserName(entity.getLoginName() + "@" + entity.getDomain());
        model.setPrincipal(entity.getLoginName());
        model.setId(entity.getId().toString());
        model.setLastName(entity.getLastName());
        model.setEmail(entity.getEmail());
        model.setDepartment(entity.getDepartment());
        model.setDomainEntryId(DirectoryEntryIdUtils.encode(entity.getExternalId()));
        model.setNamespace(entity.getNamespace());
        if (entity.getGroupNames() != null && entity.getGroupNames().size() > 0) {
            model.setGroups(new Groups());
            for (String name : entity.getGroupNames()) {
                Group group = new Group();
                group.setName(name);
                model.getGroups().getGroups().add(group);
            }
        }
        if (!StringUtils.isEmpty(entity.getDomain())) {
            Domain dom = new Domain();
            dom.setName(entity.getDomain());
            dom.setId(DirectoryEntryIdUtils.encode(dom.getName()));
            model.setDomain(dom);
        }
        return model;
    }

    @Mapping(from = DirectoryUser.class, to = User.class)
    public static User map(DirectoryUser entity, User template) {
        User model = template != null ? template : new User();
        model.setName(entity.getFirstName());
        model.setUserName(entity.getName() + "@" + entity.getDirectoryName());
        model.setId(DirectoryEntryIdUtils.encode(entity.getId()));
        model.setLastName(entity.getLastName());
        model.setEmail(entity.getEmail());
        model.setDepartment(entity.getDepartment());
        model.setPrincipal(entity.getPrincipal());
        model.setNamespace(entity.getNamespace());
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
            dom.setName(entity.getDirectoryName());
            dom.setId(DirectoryEntryIdUtils.encode(dom.getName()));
            model.setDomain(dom);
        }
        return model;
    }

    @Mapping(from = User.class, to = DbUser.class)
    public static DbUser map(User model, DbUser template) {
        DbUser entity = template != null ? template : new DbUser();
        if (model.isSetPrincipal()) {
            entity.setLoginName(model.getPrincipal());
        } else if (model.isSetName()) {
            entity.setLoginName(model.getName());
        }
        if (model.isSetId()) {
            String id = model.getId();
            entity.setId(GuidUtils.asGuid(id));
        }
        if (model.isSetDomain()) {
            Domain domain = model.getDomain();
            if (domain.isSetName()) {
                entity.setDomain(domain.getName());
            }
        }
        if (model.isSetDomainEntryId()) {
            entity.setExternalId(DirectoryEntryIdUtils.decode(model.getDomainEntryId()));
        }
        if (model.isSetNamespace()) {
            entity.setNamespace(model.getNamespace());
        }
        return entity;
    }

}

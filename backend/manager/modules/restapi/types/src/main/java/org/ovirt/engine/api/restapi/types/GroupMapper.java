package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.restapi.utils.DirectoryEntryIdUtils;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.businessentities.aaa.AuthzGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;

public class GroupMapper {

    @Mapping(from = DbGroup.class, to = Group.class)
    public static Group map(DbGroup entity, Group template) {
        Group model = template != null ? template : new Group();
        model.setName(entity.getName());
        model.setId(entity.getId().toString());
        if (!StringUtils.isEmpty(entity.getDomain())) {
            Domain dom = new Domain();
            dom.setName(entity.getDomain());
            dom.setId(DirectoryEntryIdUtils.encode(dom.getName()));
            model.setDomain(dom);
        }
        model.setDomainEntryId(DirectoryEntryIdUtils.encode(entity.getExternalId()));
        model.setNamespace(entity.getNamespace());
        return model;
    }

    @Mapping(from = DirectoryGroup.class, to = Group.class)
    public static Group map(DirectoryGroup entity, Group template) {
        Group model = template != null ? template : new Group();
        model.setName(entity.getName());
        if (!StringUtils.isEmpty(entity.getDirectoryName())) {
            Domain dom = new Domain();
            dom.setName(entity.getDirectoryName());
            dom.setId(DirectoryEntryIdUtils.encode(dom.getName()));
            model.setDomain(dom);
        }
        model.setId(DirectoryEntryIdUtils.encode(entity.getId()));
        model.setNamespace(entity.getNamespace());
        return model;
    }

    @Mapping(from = Group.class, to = DbGroup.class)
    public static DbGroup map(Group model, DbGroup template) {
        DbGroup entity = template != null? template: new DbGroup();
        if (model.isSetName()) {
            entity.setName(model.getName());
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

    @Mapping(from = AuthzGroup.class, to = Group.class)
    public static Group map(AuthzGroup authzGroup, Group template) {
        Group model = template != null ? template : new Group();
        model.setId(DirectoryEntryIdUtils.encode(authzGroup.getId()));
        model.setName(authzGroup.getName());
        model.setNamespace(authzGroup.getNamespace());
        if (!StringUtils.isEmpty(authzGroup.getAuthz())) {
            Domain dom = new Domain();
            dom.setName(authzGroup.getAuthz());
            dom.setId(DirectoryEntryIdUtils.encode(dom.getName()));
            model.setDomain(dom);
        }
        return model;
    }

}

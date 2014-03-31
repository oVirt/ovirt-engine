package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.restapi.utils.MalformedIdException;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.compat.Guid;

public class GroupMapper {

    @Mapping(from = DbGroup.class, to = Group.class)
    public static Group map(DbGroup entity, Group template) {
        Group model = template != null ? template : new Group();
        model.setName(entity.getName());
        model.setId(entity.getId().toString());
        if (!StringUtils.isEmpty(entity.getDomain())) {
            Domain dom = new Domain();
            dom.setId(new Guid(entity.getDomain().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }

    @Mapping(from = DirectoryGroup.class, to = Group.class)
    public static Group map(DirectoryGroup entity, Group template) {
        Group model = template != null ? template : new Group();
        model.setName(entity.getName());
        model.setId(entity.getId());
        if (!StringUtils.isEmpty(entity.getDirectoryName())) {
            Domain dom = new Domain();
            dom.setId(new Guid(entity.getDirectoryName().getBytes(), true).toString());
            model.setDomain(dom);
        }
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
            try {
                entity.setId(GuidUtils.asGuid(id));
                entity.setExternalId(entity.getId().toString());
            }
            catch (MalformedIdException exception) {
                // The identifier won't be a UUID if the group comes from /domains/{domain:id}/groups.
            }
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

package org.ovirt.engine.api.restapi.resource.aaa;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.resource.aaa.DomainGroupResource;
import org.ovirt.engine.api.resource.aaa.DomainGroupsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.api.restapi.resource.ResourceConstants;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.compat.Guid;

/**
 * This resource corresponds to the groups that exist in a directory accessible to the engine. Those groups may or may
 * not have been added to the engine database and the engine can't modify them, and thus the resource doesn't provide
 * any method to modify the collection.
 */
public class BackendDomainGroupsResource
        extends AbstractBackendSubResource<Group, DirectoryGroup>
        implements DomainGroupsResource {

    private BackendDomainResource parent;

    public BackendDomainGroupsResource(String id, BackendDomainResource parent) {
        super(id, Group.class, DirectoryGroup.class);
        this.parent = parent;
    }

    public void setParent(BackendDomainResource parent) {
        this.parent = parent;
    }

    public BackendDomainResource getParent() {
        return parent;
    }

    public Domain getDirectory() {
        return parent.getDirectory();
    }

    @Override
    public DomainGroupResource getGroupResource(String id) {
        return inject(new BackendDomainGroupResource(id, this));
    }

    private String getSearchPattern() {
        String constraint = QueryHelper.getConstraint(httpHeaders, uriInfo, DirectoryGroup.class, false);
        StringBuilder sb = new StringBuilder(128);
        sb.append(MessageFormat.format(ResourceConstants.AAA_GROUPS_SEARCH_TEMPLATE, parent.getDirectory().getName(), ""));
        sb.append(StringUtils.isEmpty(constraint)? "allnames=*": constraint);
        return sb.toString();
    }

    private List<DirectoryGroup> getDomainGroups() {
        return asCollection(
                DirectoryGroup.class,
            getEntity(List.class, SearchType.DirectoryGroup, getSearchPattern())
        );
    }

    private Groups mapGroups(List<DirectoryGroup> entities) {
        Groups collection = new Groups();
        for (DirectoryGroup entity : entities) {
            Group group = map(entity);
            group = populate(group, entity);
            group = addLinks(group, true);
            collection.getGroups().add(group);
        }
        return collection;
    }

    @Override
    public Groups list() {
        return mapGroups(getDomainGroups());
    }

    @Override
    protected Guid asGuidOr404(String id) {
        return null;
    }

}

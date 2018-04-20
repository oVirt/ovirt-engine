package org.ovirt.engine.api.restapi.resource.aaa;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.resource.aaa.GroupResource;
import org.ovirt.engine.api.resource.aaa.GroupsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.resource.ResourceConstants;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.api.restapi.utils.DirectoryEntryIdUtils;
import org.ovirt.engine.api.restapi.utils.aaa.AuthzUtils;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddGroupParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * This resource corresponds to groups that have been looked up in some directory accessible to the engine and then
 * added to the engine database. Groups can be added and removed from the collection, and this will add or remove them
 * from the database (not from the underlying directory).
 */
public class BackendGroupsResource
        extends AbstractBackendCollectionResource<Group, DbGroup>
        implements GroupsResource {

    private static final String GROUPS_SEARCH_PATTERN = "grpname != \"\"";
    private static final String AND_SEARCH_PATTERN = " and ";

    public BackendGroupsResource() {
        super(Group.class, DbGroup.class);
    }

    /**
     * This method calculates the search pattern that will be used to perform the search of database groups during the
     * execution of the {@code list} operation.
     */
    private String getSearchPattern() {
        String userProvidedPattern = QueryHelper.getConstraint(httpHeaders, uriInfo, "",  modelType);
        return userProvidedPattern.equals("Groups : ") ?
               userProvidedPattern + GROUPS_SEARCH_PATTERN
               :
               userProvidedPattern + AND_SEARCH_PATTERN + GROUPS_SEARCH_PATTERN;
    }

    /**
     * Determine what is the name of the directory that corresponds to the given group model. It may contained in the
     * model directly, or it can be embedded in the name.
     *
     * @param group the model of the group
     * @param authzProvidersNames
     *            list of existing authz provider names, including the returned provider name, if exists in the list
     * @return the name of the directory or {@code null} if the group can't be determined
     */
    private String getAuthzProviderName(Group group, Collection<String> authzProvidersNames) {
        if (group.isSetDomain() && group.getDomain().isSetName()) {
            return group.getDomain().getName();
        } else if (group.isSetDomain() && group.getDomain().isSetId()) {
            for (String domain : authzProvidersNames) {
                Guid domainId = new Guid(domain.getBytes(StandardCharsets.UTF_8));
                if (domainId.toString().equals(group.getDomain().getId())) {
                   return domain;
                }
            }
            throw new WebFaultException(
                null,
                "Domain: '" + group.getDomain().getId().toString() + "' does not exist.",
                Response.Status.BAD_REQUEST);
        }
        return AuthzUtils.getAuthzNameFromEntityName(group.getName(), authzProvidersNames);
    }

    /**
     * This method calculates the search pattern used to search for the directory group that will be added to the
     * database when performing the {@code add} operation.
     *
     * @param groupname the name of the user that will be searched in the
     *     directory
     * @param domain the name of the directory where the search will be
     *     performed
     */
    private String getDirectoryGroupSearchPattern(String groupname, String domain) {
        String constraint = QueryHelper.getConstraint(httpHeaders, uriInfo, DbGroup.class, false);
        final StringBuilder sb = new StringBuilder(128);

        sb.append(MessageFormat.format(ResourceConstants.AAA_GROUPS_SEARCH_TEMPLATE, domain, ""));

        sb.append(StringUtils.isEmpty(constraint) ?
                "name=" + groupname
                        :
                        constraint);

        return sb.toString();
    }

    private Groups mapDbGroupCollection(List<DbGroup> entities) {
        Groups collection = new Groups();
        for (DbGroup entity : entities) {
            Group group = map(entity);
            group = populate(group, entity);
            group = addLinks(group, BaseResource.class);
            collection.getGroups().add(group);
        }
        return collection;
    }

    @Override
    public GroupResource getGroupResource(String id) {
        return inject(new BackendGroupResource(id, this));
    }

    @Override
    public Groups list() {
        if (isFiltered()) {
            return mapDbGroupCollection(getBackendCollection(QueryType.GetAllDbGroups, new QueryParametersBase(), SearchType.DBGroup));
        } else {
            return mapDbGroupCollection(getBackendCollection(SearchType.DBGroup, getSearchPattern()));
        }
    }

    @Override
    public Response add(Group group) {
        List<String> authzProvidersNames = getBackendCollection(
                String.class,
                QueryType.GetDomainList,
                new QueryParametersBase());
        validateParameters(group, "name");
        if (AuthzUtils.getAuthzNameFromEntityName(group.getName(), authzProvidersNames) == null) {
            validateParameters(group, "domain.id|name");
        }
        String directoryName = getAuthzProviderName(group, authzProvidersNames);
        DirectoryGroup directoryGroup = findDirectoryGroup(directoryName, group);
        if (directoryGroup == null) {
            return Response.status(Status.BAD_REQUEST)
                .entity("No such group: " + group.getName() + " in directory " + directoryName)
                .build();
        }
        AddGroupParameters parameters = new AddGroupParameters();
        parameters.setGroupToAdd(new DbGroup(directoryGroup));
        QueryIdResolver<Guid> resolver = new QueryIdResolver<>(QueryType.GetDbGroupById, IdQueryParameters.class);
        return performCreate(ActionType.AddGroup, parameters, resolver, BaseResource.class);
    }

    /**
     * Find the directory user that corresponds to the given model.
     *
     * @param directoryName the name of the directory where to perform the search
     * @param groupModel the group model
     * @return the requested directory group or {@code null} if no such group exists
     */
    private DirectoryGroup findDirectoryGroup(String directoryName, Group groupModel) {
        // Try to find a group that matches the identifier contained in the model:
        String namespace = groupModel.getNamespace();
        if (groupModel.isSetId()) {
            return getGroupById(directoryName, namespace, groupModel.getId());
        } else if (groupModel.isSetDomainEntryId()) {
            return getGroupById(directoryName, namespace, groupModel.getDomainEntryId());
        } else if (groupModel.isSetName()) {
            return getEntity(
                    DirectoryGroup.class,
                    SearchType.DirectoryGroup,
                    getDirectoryGroupSearchPattern(AuthzUtils.getEntityNameWithoutAuthz(groupModel.getName(), directoryName), directoryName)
                );
        }

        return null;
    }

    private DirectoryGroup getGroupById(String directoryName, String namespace, String groupId) {
        try {
            groupId = DirectoryEntryIdUtils.decode(groupId);
        } catch(IllegalArgumentException exception) {
            return null;
        }
        return getEntity(
                DirectoryGroup.class,
                QueryType.GetDirectoryGroupById,
                new DirectoryIdQueryParameters(directoryName, namespace, groupId),
                groupId,
                true);
    }

}

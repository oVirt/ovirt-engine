package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.common.util.ReflectionHelper.assignChildModel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.GroupResource;
import org.ovirt.engine.api.resource.GroupsResource;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAdGroupByIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class BackendGroupsResource extends AbstractBackendCollectionResource<Group, ad_groups>
        implements GroupsResource {

    static final String[] SUB_COLLECTIONS = { "permissions", "roles", "tags" };
    protected static final String AD_SEARCH_TEMPLATE = "ADGROUP@{0}: ";
    private static final String GROUPS_SEARCH_PATTERN = "usrname = \"\"";
    private static final String AND_SEARCH_PATTERN = " and ";

    private BackendDomainResource parent;

    public BackendGroupsResource() {
        super(Group.class, ad_groups.class, SUB_COLLECTIONS);
    }

    public BackendGroupsResource(String id, BackendDomainResource parent) {
        super(Group.class, ad_groups.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    public Groups list() {
        if(parent==null)
            return mapDbGroupsCollection(getGroupsCollection(SearchType.DBUser, getSearchPattern()));
        return mapDomainGroupsCollection(getGroupsFromDomain());
    }

    private String getSearchPattern() {
        String user_defined_pattern = QueryHelper.getConstraint(getUriInfo(), "",  User.class);
        return user_defined_pattern.equals("Users : ") ?
               user_defined_pattern + GROUPS_SEARCH_PATTERN
               :
               user_defined_pattern + AND_SEARCH_PATTERN + GROUPS_SEARCH_PATTERN;
    }

    @Override
    public Response add(Group group) {
        validateParameters(group, "name");
        // somewhat counter-intuitively, the AddUserCommand is used
        // to add groups as well as users
        AddUserParameters newGroup = new AddUserParameters();
        newGroup.setAdGroup(getAdGroup(group));
        return performCreation(VdcActionType.AddUser, newGroup, new GroupIdResolver(newGroup.getAdGroup().getid()));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveAdGroup, new AdElementParametersBase(asGuid(id)));
    }

    @Override
    @SingleEntityResource
    public GroupResource getGroupSubResource(String id) {
        return inject(new BackendGroupResource(id, this));
    }

    protected Groups mapDomainGroupsCollection(List<ad_groups> entities) {
        Groups collection = new Groups();
        for (ad_groups entity : entities) {
            collection.getGroups().add(addLinks(modifyDomain(mapAdGroup(entity)), true));

        }
        return collection;
    }

    protected Groups mapDbGroupsCollection(List<DbUser> entities) {
        Groups collection = new Groups();
        for (DbUser entity : entities) {
            collection.getGroups().add(addLinks(modifyDomain(mapDbUser(entity))));
        }
        return collection;
    }

    private Group modifyDomain(Group group) {
        if(group.getDomain()!=null)
            group.getDomain().setName(null);
        return group;
    }

    protected Group mapAdGroup(ad_groups entity) {
        return getMapper(ad_groups.class, Group.class).map(entity, null);
    }

    protected Group mapDbUser(DbUser entity) {
        return getMapper(DbUser.class, Group.class).map(entity, null);
    }

    @Override
    protected Group addParents(Group group) {
        if(parent!=null){
            assignChildModel(group, Group.class).setId(parent.get().getId());
        }
        return group;
    }

    protected String getSearchPattern(String param) {
        String constraint = QueryHelper.getConstraint(getUriInfo(), ad_groups.class, false);
        StringBuffer sb = new StringBuffer();

        sb.append(MessageFormat.format(AD_SEARCH_TEMPLATE,
                  parent!=null?
                        parent.getDirectory().getName()
                        :
                        getCurrent().get(Principal.class).getDomain()));

        sb.append(StringHelper.isNullOrEmpty(constraint)?
                        "name="+param
                        :
                        constraint);

        return sb.toString();
    }

    protected ad_groups getAdGroup(Group group) {
        List<ad_groups> adGroups = asCollection(getEntity(ArrayList.class,
                                                          SearchType.AdGroup,
                                                          getSearchPattern("*")));
        for (ad_groups adGroup : adGroups) {
            if (adGroup.getname().equals(group.getName())) {
                return adGroup;
            }
        }
        return handleError(new EntityNotFoundException(group.getName()), true);
    }

    protected List<ad_groups> getGroupsFromDomain() {
        return asCollection(ad_groups.class,
                getEntity(ArrayList.class,
                        SearchType.AdGroup,
                        getSearchPattern("*")));

    }

    public ad_groups lookupGroupById(Guid id) {
        return getEntity(ad_groups.class,
                         VdcQueryType.GetAdGroupById,
                         new GetAdGroupByIdParameters(id),
                         id.toString());
    }

    protected class GroupIdResolver extends EntityIdResolver {

        private Guid id;

        GroupIdResolver(Guid id) {
            this.id = id;
        }

        @Override
        public ad_groups lookupEntity(Guid nullId) {
            return lookupGroupById(id);
        }
    }

    protected List<DbUser> getGroupsCollection(SearchType searchType, String constraint) {
        return getBackendCollection(DbUser.class,
                                    VdcQueryType.Search,
                                    new SearchParameters(constraint,
                                                         searchType));
    }
}

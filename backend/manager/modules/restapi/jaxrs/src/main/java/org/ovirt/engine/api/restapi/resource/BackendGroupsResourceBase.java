package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.common.util.ReflectionHelper.assignChildModel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAdGroupByIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendGroupsResourceBase extends AbstractBackendCollectionResource<Group, LdapGroup> {

    static final String[] SUB_COLLECTIONS = { "permissions", "roles", "tags" };
    private static final String AD_SEARCH_TEMPLATE = "ADGROUP@{0}: ";
    private static final String GROUPS_SEARCH_PATTERN = "usrname = \"\"";
    private static final String AND_SEARCH_PATTERN = " and ";

    private BackendDomainResource parent;

    public BackendGroupsResourceBase() {
        super(Group.class, LdapGroup.class, SUB_COLLECTIONS);
    }

    public BackendGroupsResourceBase(String id, BackendDomainResource parent) {
        super(Group.class, LdapGroup.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    public BackendGroupsResourceBase(Class<Group> class1, Class<LdapGroup> class2, String[] subCollections) {
        super(class1, class2, subCollections);
    }

    protected String getSearchPattern() {
        String user_defined_pattern = QueryHelper.getConstraint(getUriInfo(), "",  User.class);
        return user_defined_pattern.equals("Users : ") ?
               user_defined_pattern + GROUPS_SEARCH_PATTERN
               :
               user_defined_pattern + AND_SEARCH_PATTERN + GROUPS_SEARCH_PATTERN;
    }


    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveAdGroup, new AdElementParametersBase(asGuid(id)));
    }

    protected Groups mapDomainGroupsCollection(List<LdapGroup> entities) {
        Groups collection = new Groups();
        for (LdapGroup entity : entities) {
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

    protected Group mapAdGroup(LdapGroup entity) {
        return getMapper(LdapGroup.class, Group.class).map(entity, null);
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
        return getSearchPattern(param, null);
    }

    protected String getSearchPattern(String param, String domain) {
        String constraint = QueryHelper.getConstraint(getUriInfo(), LdapGroup.class, false);
        final StringBuilder sb = new StringBuilder(128);

        sb.append(MessageFormat.format(AD_SEARCH_TEMPLATE,
                  parent!=null?
                        parent.getDirectory().getName()
                        :
                        domain==null?
                              getCurrent().get(Principal.class).getDomain()
                              :
                              domain));

        sb.append(StringUtils.isEmpty(constraint) ?
                        "name="+param
                        :
                        constraint);

        return sb.toString();
    }

    protected LdapGroup getAdGroup(Group group) {
        if(group.getId() != null) {
            return lookupGroupById(asGuid(group.getId()));
        }

        List<LdapGroup> adGroups = asCollection(getEntity(ArrayList.class,
                                                          SearchType.AdGroup,
                                                          getSearchPattern("*", getDomainName(group.getName()))));
        for (LdapGroup adGroup : adGroups) {
            if (adGroup.getname().equals(group.getName())) {
                return adGroup;
            }
        }
        return entityNotFound();
    }

    private String getDomainName(String groupName) {
        int index = groupName.indexOf("/");
        if(index == -1) {
            return null;
        }
        return groupName.substring(0, index);
    }

    protected List<LdapGroup> getGroupsFromDomain() {
        return asCollection(LdapGroup.class,
                getEntity(ArrayList.class,
                        SearchType.AdGroup,
                        getSearchPattern("*")));

    }

    public LdapGroup lookupGroupById(Guid id) {
        return getEntity(LdapGroup.class,
                         VdcQueryType.GetAdGroupById,
                         new GetAdGroupByIdParameters(id),
                         id.toString(),
                         true);
    }

    protected class GroupIdResolver extends EntityIdResolver<Guid> {

        private Guid id;

        GroupIdResolver(Guid id) {
            this.id = id;
        }

        @Override
        public LdapGroup lookupEntity(Guid nullId) throws BackendFailureException {
            return lookupGroupById(this.id);
        }
    }

    protected List<DbUser> getGroupsCollection(SearchType searchType, String constraint) {
        return getBackendCollection(DbUser.class,
                                    VdcQueryType.Search,
                                    new SearchParameters(constraint,
                                                         searchType));
    }

    @Override
    protected Group doPopulate(Group model, LdapGroup entity) {
        return model;
    }
}

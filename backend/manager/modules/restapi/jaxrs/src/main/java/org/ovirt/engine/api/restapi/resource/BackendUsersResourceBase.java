package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.common.util.ReflectionHelper.assignChildModel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Users;
import org.ovirt.engine.api.restapi.resource.BaseBackendResource.WebFaultException;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.common.queries.GetDbUserByUserIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;

public class BackendUsersResourceBase extends AbstractBackendCollectionResource<User, DbUser> {

    static final String[] SUB_COLLECTIONS = { "permissions", "roles", "tags" };
    protected static final String AD_SEARCH_TEMPLATE = "ADUSER@{0}: ";
    private static final String USERS_SEARCH_PATTERN = "usrname != \"\"";
    private static final String AND_SEARCH_PATTERN = " and ";

    private BackendDomainResource parent;

    public BackendUsersResourceBase() {
        super(User.class, DbUser.class, SUB_COLLECTIONS);
    }

    public BackendUsersResourceBase(String id, BackendDomainResource parent) {
        super(User.class, DbUser.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    public BackendUsersResourceBase(Class<User> class1, Class<DbUser> class2, String[] subCollections) {
        super(class1, class2, subCollections);
    }

    protected String getSearchPattern() {
        String user_defined_pattern = QueryHelper.getConstraint(getUriInfo(), "",  modelType);
        return user_defined_pattern.equals("Users : ") ?
               user_defined_pattern + USERS_SEARCH_PATTERN
               :
               user_defined_pattern + AND_SEARCH_PATTERN + USERS_SEARCH_PATTERN;
    }

    protected String getDomain(User user) {
        if (user.isSetDomain() && user.getDomain().isSetName()) {
            return user.getDomain().getName();
        }
        else if (user.isSetDomain() && user.getDomain().isSetId()) {
            List<String> domains = getBackendCollection(
               String.class,
               VdcQueryType.GetDomainList,
               new VdcQueryParametersBase());
            for (String domain :domains) {
                NGuid domainId = new NGuid(domain.getBytes(), true);
                if (domainId.toString().equals(user.getDomain().getId())) {
                   return domain;
                }
            }
            throw new WebFaultException(null, "Domain: '" + user.getDomain().getId().toString() + "' does not exist.", Response.Status.BAD_REQUEST);
        }
        else if (user.isSetUserName() && user.getUserName().contains("@")) {
            return user.getUserName().substring(user.getUserName().indexOf("@")+1);
        }
        return null;
    }

    protected String getCurrentDomain() {
        return getCurrent().get(Principal.class).getDomain();
    }

    protected String getSearchPattern(String username, String domain) {
        String constraint = QueryHelper.getConstraint(getUriInfo(), DbUser.class, false);
        final StringBuilder sb = new StringBuilder(128);

        sb.append(MessageFormat.format(AD_SEARCH_TEMPLATE,
                  parent!=null?
                        parent.getDirectory().getName()
                        :
                        domain));

         sb.append(StringHelper.isNullOrEmpty(constraint)?
                        "allnames=" + username
                        :
                        constraint);

         return sb.toString();
    }

    protected List<LdapUser> getUsersFromDomain() {
        return asCollection(LdapUser.class,
                getEntity(ArrayList.class,
                        SearchType.AdUser,
                        getSearchPattern("*", getCurrentDomain())));

    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveUser, new AdElementParametersBase(asGuid(id)));
    }

    protected Users mapDbUserCollection(List<DbUser> entities) {
        Users collection = new Users();
        for (DbUser entity : entities) {
            collection.getUsers().add(addLinks(modifyDomain(map(entity)),
                                               BaseResource.class));
        }
        return collection;
    }

    @Override
    protected User addParents(User user) {
        if(parent!=null){
            assignChildModel(user, User.class).setId(parent.get().getId());
        }
        return user;
    }

    protected Users mapDomainUserCollection(List<LdapUser> entities) {
        Users collection = new Users();
        for (LdapUser entity : entities) {
            collection.getUsers().add(addLinks(modifyDomain(mapAdUser(entity)),
                                               true));
        }
        return collection;
    }

    private User modifyDomain(User user) {
        if(user.getDomain()!=null)
            user.getDomain().setName(null);
        return user;
    }

    protected VdcUser map(LdapUser adUser) {
        return getMapper(LdapUser.class, VdcUser.class).map(adUser, null);
    }

    protected User mapAdUser(LdapUser adUser) {
        return getMapper(LdapUser.class, User.class).map(adUser, null);
    }

    public DbUser lookupUserById(Guid id) {
        return getEntity(DbUser.class,
                         VdcQueryType.GetDbUserByUserId,
                         new GetDbUserByUserIdParameters(id),
                         id.toString());
    }

    protected class UserIdResolver extends EntityIdResolver<Guid> {

        private Guid id;

        UserIdResolver(Guid id) {
            this.id = id;
        }

        @Override
        public DbUser lookupEntity(Guid id) throws BackendFailureException {
            return lookupUserById(this.id);
        }
    }
}

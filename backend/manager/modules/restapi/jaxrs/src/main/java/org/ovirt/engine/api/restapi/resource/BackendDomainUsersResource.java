package org.ovirt.engine.api.restapi.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Users;
import org.ovirt.engine.api.resource.DomainUserResource;
import org.ovirt.engine.api.resource.DomainUsersResource;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.utils.ExternalId;

/**
 * This resource corresponds to the users that exist in a directory accessible
 * to the engine. Those users may or may not have been added to the engine
 * database and the engine can't modify them, and thus the resource doesn't
 * provide any method to modify the collection.
 */
public class BackendDomainUsersResource
       extends AbstractBackendSubResource<User, LdapUser>
       implements DomainUsersResource {

    private static final String SEARCH_TEMPLATE = "ADUSER@{0}: ";

    private BackendDomainResource parent;

    public BackendDomainUsersResource(String id, BackendDomainResource parent) {
        super(id, User.class, LdapUser.class);
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
    @SingleEntityResource
    public DomainUserResource getDomainUserSubResource(String id) {
        try {
            byte[] bytes = Hex.decodeHex(id.toCharArray());
            ExternalId externalId = new ExternalId(bytes);
            return inject(new BackendDomainUserResource(externalId, this));
        }
        catch (DecoderException exception) {
            throw new WebFaultException(
                exception,
                "Can't decode domain user identifier '" + id + "'.",
                Response.Status.INTERNAL_SERVER_ERROR
            );
        }

    }

    private String getSearchPattern() {
        String constraint = QueryHelper.getConstraint(getUriInfo(), LdapUser.class, false);
        StringBuilder sb = new StringBuilder(128);
        sb.append(MessageFormat.format(SEARCH_TEMPLATE, parent.getDirectory().getName()));
        sb.append(StringUtils.isEmpty(constraint)? "allnames=*": constraint);
        return sb.toString();
    }

    private List<LdapUser> getDomainUsers() {
        return asCollection(LdapUser.class,
                getEntity(ArrayList.class,
                        SearchType.AdUser,
                        getSearchPattern()));
    }

    private Users mapUsers(List<LdapUser> entities) {
        Users collection = new Users();
        for (LdapUser entity : entities) {
            User user = map(entity);
            user = populate(user, entity);
            user = addLinks(user, true);
            collection.getUsers().add(user);
        }
        return collection;
    }

    @Override
    public Users list() {
        return mapUsers(getDomainUsers());
    }

    @Override
    protected User doPopulate(User model, LdapUser entity) {
        return model;
    }

}

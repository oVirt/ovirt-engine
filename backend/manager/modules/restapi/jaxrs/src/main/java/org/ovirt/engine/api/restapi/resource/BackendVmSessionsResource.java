package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.model.Sessions;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.VmSessionResource;
import org.ovirt.engine.api.resource.VmSessionsResource;
import org.ovirt.engine.api.resource.aaa.UserResource;
import org.ovirt.engine.api.resource.aaa.UsersResource;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.resource.aaa.BackendUserResource;
import org.ovirt.engine.api.restapi.resource.aaa.BackendUsersResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.rsdl.ServiceTree;
import org.ovirt.engine.api.rsdl.ServiceTreeNode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.GetDbUserByUserNameAndDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmSessionsResource extends AbstractBackendCollectionResource<Session, Object> implements VmSessionsResource {

    private Guid vmId;
    private BackendUserResource userResource;

    public BackendVmSessionsResource(Guid vmId) {
        super(Session.class, Object.class);
        this.vmId = vmId;
    }

    @Override
    public Sessions list() {
        Object obj = getEntity(entityType, QueryType.GetVmByVmId, new IdQueryParameters(vmId), vmId.toString(), true);
        VM vm = (VM)obj;
        Sessions sessions = VmMapper.map(vm, new Sessions());
        org.ovirt.engine.api.model.Vm vmModel = new org.ovirt.engine.api.model.Vm();
        vmModel.setId(vm.getId().toString());
        if (sessions.isSetSessions()) {
            for (Session session : sessions.getSessions()) {
                setSessionId(session);
                setSessionVmId(vmModel, session);
                // only console user assumed to be an ovirt user, and only an ovirt-user has an ID & href
                if (session.isSetConsoleUser() && session.isConsoleUser()) {
                    addLinksIncludingUser(session);
                } else {
                    addLinks(session, org.ovirt.engine.api.model.Vm.class);
                }
            }
        }
        return sessions;
    }

    /**
     * Special handling of adding links to the user and domain of the session.
     */
    private void addLinksIncludingUser(Session session) {
        String domainName = session.getUser().getDomain().getName();
        addLinks(session, org.ovirt.engine.api.model.Vm.class);
        session.getUser().setDomain(new Domain());
        session.getUser().getDomain().setName(domainName);
        setSessionUser(session);
    }

    private void setSessionVmId(org.ovirt.engine.api.model.Vm vmModel, Session session) {
        session.setVm(vmModel);
    }

    /**
     * A session is not a business-entity in the engine and does not have an ID. This method generates an ID for the
     * session object, based on its attributes.
     */
    private void setSessionId(Session session) {
        String idString = session.getUser().getName();
        if (session.isSetIp() && session.getIp().isSetAddress()) {
            idString += session.getIp().getAddress();
        }
        if (session.isSetProtocol()) {
            idString += session.getProtocol();
        }
        session.setId(GuidUtils.generateGuidUsingMd5(idString).toString());
    }

    /**
     * The console user, if exists, is a real ovirt-user. Use its name to get ID and href information, and set them
     * inside the user object, inside the session. If user is not found in database, those information won't be set.
     */
    private void setSessionUser(Session session) {
        // Get the user entity and don't fail if user is not found in database:
        DbUser entity = getEntity(
            DbUser.class,
            QueryType.GetDbUserByUserNameAndDomain,
            new GetDbUserByUserNameAndDomainQueryParameters(
                session.getUser().getUserName(),
                session.getUser().getDomain().getName()
            ),
            "",
            false,
            false
        );

        // Map the database user to model user:
        if (entity != null) {
            User user = null;
            try {
                user = BackendApiResource.getInstance()
                    .getUsersResource()
                    .getUserResource(entity.getId().toString())
                    .get();
            } catch (WebApplicationException e) {
                //in case of failure settle for providing a link to the user
                //and not the user information (better practice anyway, but
                //won't be changed due to backwards compatibility).
                //
                //An example of a failure is when this query is run by a non-admin
                //user. The list of users returned will be filtered and thus empty.
                //
                //(https://bugzilla.redhat.com/1568669)
                addLinkToUser(session);
                return;
            }
            /**
             * TODO: It's enough to set user ID and href, we don't have to set also domain, please consider this
             * when moving to API version 5.
             */
            if (user != null) {
                session.getUser().setId(user.getId());
                session.getUser().setHref(user.getHref());
                session.getUser().getDomain().setId(user.getDomain().getId());
                session.getUser().getDomain().setHref(user.getDomain().getHref());
            }
        }
    }

    private void addLinkToUser(Session session) {
         ServiceTreeNode node = ServiceTree.getNode(UsersResource.class);
         String href = CurrentManager.get().getAbsolutePath(node.getPath(), session.getUser().getId());
         session.getUser().setHref(href);
    }

    private BackendUserResource getUserResource() {
        if (this.userResource == null) {
            BackendUsersResource usersResource = new BackendUsersResource();
            inject(usersResource);
            UserResource userResource = usersResource.getUserResource("");
            this.userResource = (BackendUserResource) userResource;
        }
        return this.userResource;
    }

    @Override
    public VmSessionResource getSessionResource(String id) {
        return inject(new BackendVmSessionResource(this, id));
    }

    public void setUserResource(BackendUserResource userResource) {
        this.userResource = userResource;
    }
}

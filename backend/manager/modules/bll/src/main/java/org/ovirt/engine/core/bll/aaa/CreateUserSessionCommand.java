package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateUserSessionParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class CreateUserSessionCommand<T extends CreateUserSessionParameters> extends CommandBase<T> {
    private static final Guid BOTTOM_OBJECT_ID = new Guid("BBB00000-0000-0000-0000-123456789BBB");

    @Inject
    private SessionDataContainer sessionDataContainer;

    public CreateUserSessionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private DbUser buildUser(T params, String authzName) {
        DbUser dbUser = dbUserDao.getByExternalId(authzName, params.getPrincipalId());
        DbUser user = new DbUser(dbUser);
        user.setId(dbUser == null ? Guid.newGuid() : dbUser.getId());
        user.setExternalId(params.getPrincipalId());
        user.setDomain(authzName);
        user.setEmail(params.getEmail());
        user.setFirstName(params.getFirstName());
        user.setLastName(params.getLastName());
        user.setNamespace(params.getNamespace());
        user.setLoginName(params.getPrincipalName());
        List<Guid> groupIds = new ArrayList<>();
        List<String> groupRecordIds = new ArrayList<>();
        flatGroups((Collection<ExtMap>) params.getGroupIds(), groupRecordIds);
        for (String groupId : groupRecordIds) {
            DbGroup dbGroup = dbGroupDao.getByExternalId(authzName, groupId);
            if (dbGroup != null) {
                groupIds.add(dbGroup.getId());
            }
        }
        user.setGroupIds(groupIds);
        user.setAdmin(
            !roleDao.getAnyAdminRoleForUserAndGroups(
                user.getId(),
                StringUtils.join(user.getGroupIds(), ",")
            ).isEmpty()
        );

        if (dbUser == null) {
            dbUserDao.save(user);
        } else if (!dbUser.equals(user)) {
            dbUserDao.update(user);
        }
        return user;
    }

    @Override
    protected void executeCommand() {
        final AuthenticationProfile profile = AuthenticationProfileRepository.getInstance()
                .getProfile(getParameters().getProfileName());
        if (profile == null) {
            setSucceeded(false);
        } else {
            final DbUser user = buildUser(getParameters(), profile.getAuthzName());
            boolean isAdmin = !roleDao.getAnyAdminRoleForUserAndGroups(user.getId(),
                    StringUtils.join(user.getGroupIds(), ",")).isEmpty();
            user.setAdmin(isAdmin);

            if (getParameters().isAdminRequired() && !isAdmin) {
                setSucceeded(false);
            } else if (permissionDao.getEntityPermissionsForUserAndGroups(user.getId(),
                    StringUtils.join(user.getGroupIds(), ","),
                    ActionGroup.LOGIN,
                    BOTTOM_OBJECT_ID,
                    VdcObjectType.Bottom,
                    true) == null) {
                setSucceeded(false);
            } else {
                String engineSessionId = sessionDataContainer.generateEngineSessionId();
                sessionDataContainer.setSourceIp(engineSessionId, getParameters().getSourceIp());
                sessionDataContainer.setUser(engineSessionId, user);
                sessionDataContainer.refresh(engineSessionId);
                sessionDataContainer.setProfile(engineSessionId, profile);
                sessionDataContainer.setPrincipalName(engineSessionId, getParameters().getPrincipalName());
                sessionDataContainer.setSsoAccessToken(engineSessionId, getParameters().getSsoToken());
                getReturnValue().setActionReturnValue(engineSessionId);
                setSucceeded(true);
            }
        }
    }

    private static void flatGroups(Collection<ExtMap> groupIds, List<String> accumulator) {
        for (ExtMap group : groupIds) {
            if (!accumulator.contains(group.<String>get(Authz.GroupRecord.ID))) {
                accumulator.add(group.get(Authz.GroupRecord.ID));
                flatGroups(group, Authz.GroupRecord.GROUPS, accumulator);
            }
        }
    }

    private static void flatGroups(ExtMap entity, ExtKey key, List<String> accumulator) {
        for (ExtMap group : entity.<Collection<ExtMap>>get(key, Collections.<ExtMap>emptyList())) {
            if (!accumulator.contains(group.<String>get(Authz.GroupRecord.ID))) {
                accumulator.add(group.get(Authz.GroupRecord.ID));
                flatGroups(group, Authz.GroupRecord.GROUPS, accumulator);
            }
        }
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

}

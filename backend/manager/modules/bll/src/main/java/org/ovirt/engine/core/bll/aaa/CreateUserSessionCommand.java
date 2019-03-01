package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.CreateUserSessionsError;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateUserSessionParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDao;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.utils.EngineLocalConfig;

@NonTransactiveCommandAttribute
public class CreateUserSessionCommand<T extends CreateUserSessionParameters> extends CommandBase<T> {
    private static final Guid BOTTOM_OBJECT_ID = new Guid("BBB00000-0000-0000-0000-123456789BBB");
    private static final int UNLIMITED_SESSIONS = -1;
    private final int maxUserSessions;

    @Inject
    private SessionDataContainer sessionDataContainer;
    @Inject
    private PermissionDao permissionDao;
    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private DbGroupDao dbGroupDao;
    @Inject
    private RoleDao roleDao;

    private static final String UNKNOWN = "UNKNOWN";
    private static final String OVIRT_ADMINISTRATOR = "ovirt-administrator";

    private String sessionId;
    private String sourceIp;

    public CreateUserSessionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        maxUserSessions = EngineLocalConfig.getInstance().getInteger("ENGINE_MAX_USER_SESSIONS");
    }

    private DbUser buildUser(T params, String authzName) {
        boolean externalSsoEnabled = EngineLocalConfig.getInstance().getBoolean("ENGINE_SSO_ENABLE_EXTERNAL_SSO");
        DbUser dbUser = externalSsoEnabled ?
                dbUserDao.getByUsernameAndDomain(params.getPrincipalName(), authzName) :
                dbUserDao.getByExternalId(authzName, params.getPrincipalId());
        DbUser user = new DbUser(dbUser);
        user.setId(dbUser == null ? Guid.newGuid() : dbUser.getId());
        user.setExternalId(dbUser == null && externalSsoEnabled ? Guid.newGuid().toString() : params.getPrincipalId());
        user.setDomain(authzName);
        user.setEmail(params.getEmail());
        user.setFirstName(params.getFirstName());
        user.setLastName(params.getLastName());
        user.setNamespace(params.getNamespace());
        user.setLoginName(params.getPrincipalName());
        List<Guid> groupIds = new ArrayList<>();
        Map<String, ExtMap> groupRecords = new HashMap<>();
        flatGroups((Collection<ExtMap>) params.getGroupIds(), groupRecords);
        for (Map.Entry<String, ExtMap> group: groupRecords.entrySet()) {
            String name = group.getValue().get(Authz.GroupRecord.NAME);
            // The domain is empty for predefined ovirt-administrator group
            String domain = name.equals(OVIRT_ADMINISTRATOR) ? "" : authzName;
            // If external sso integration is enabled we retrieve the group by their name and domain. We do not have
            // the external id for the groups in the OpenID Connect claim set.
            DbGroup dbGroup = externalSsoEnabled ?
                    dbGroupDao.getByNameAndDomain(name, domain) :
                    dbGroupDao.getByExternalId(authzName, group.getKey());
            if (dbGroup != null) {
                dbGroup.setName(name);
                dbGroupDao.update(dbGroup);
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
        sourceIp = getParameters().getSourceIp();
        if (profile == null) {
            setSucceeded(false);
        } else {
            final DbUser user = buildUser(getParameters(), profile.getAuthzName());
            boolean isAdmin = !roleDao.getAnyAdminRoleForUserAndGroups(user.getId(),
                    StringUtils.join(user.getGroupIds(), ",")).isEmpty();
            user.setAdmin(isAdmin);
            setCurrentUser(user);
            setUserName(String.format("%s@%s", getCurrentUser().getLoginName(), getCurrentUser().getDomain()));

            if (getParameters().isAdminRequired() && !isAdmin) {
                setActionReturnValue(CreateUserSessionsError.USER_NOT_AUTHORIZED);
                setSucceeded(false);
            } else if (permissionDao.getEntityPermissionsForUserAndGroups(user.getId(),
                    StringUtils.join(user.getGroupIds(), ","),
                    ActionGroup.LOGIN,
                    BOTTOM_OBJECT_ID,
                    VdcObjectType.Bottom,
                    true) == null) {
                setActionReturnValue(CreateUserSessionsError.USER_NOT_AUTHORIZED);
                setSucceeded(false);
            } else if (maxUserSessions != UNLIMITED_SESSIONS
                    && sessionDataContainer.getNumUserSessions(user) >= maxUserSessions) {
                setActionReturnValue(CreateUserSessionsError.NUM_OF_SESSIONS_EXCEEDED);
                setSucceeded(false);
            } else {
                String engineSessionId = sessionDataContainer.generateEngineSessionId();
                sessionDataContainer.setSourceIp(engineSessionId, getParameters().getSourceIp());
                sessionDataContainer.setUser(engineSessionId, user);
                sessionDataContainer.refresh(engineSessionId);
                sessionDataContainer.setProfile(engineSessionId, profile);
                sessionDataContainer.setPrincipalName(engineSessionId, getParameters().getPrincipalName());
                sessionDataContainer.setSsoAccessToken(engineSessionId, getParameters().getSsoToken());
                sessionDataContainer.setSsoOvirtAppApiScope(engineSessionId, getParameters().getAppScope());
                getReturnValue().setActionReturnValue(engineSessionId);
                setSucceeded(true);
                sessionId = engineSessionId;
            }
        }
    }

    private static void flatGroups(Collection<ExtMap> groupIds, Map<String, ExtMap> accumulator) {
        for (ExtMap group : groupIds) {
            if (!accumulator.containsKey(group.<String>get(Authz.GroupRecord.ID))) {
                accumulator.put(group.get(Authz.GroupRecord.ID), group);
                flatGroups(group, Authz.GroupRecord.GROUPS, accumulator);
            }
        }
    }

    private static void flatGroups(ExtMap entity, ExtKey key, Map<String, ExtMap> accumulator) {
        for (ExtMap group : entity.<Collection<ExtMap>>get(key, Collections.<ExtMap>emptyList())) {
            if (!accumulator.containsKey(group.<String>get(Authz.GroupRecord.ID))) {
                accumulator.put(group.get(Authz.GroupRecord.ID), group);
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

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("SessionID", StringUtils.isEmpty(sessionId) ? UNKNOWN : sessionId);
        addCustomValue("SourceIP", StringUtils.isEmpty(sourceIp) ? UNKNOWN : sourceIp);
        addCustomValue("LoginErrMsg", "");
        if (getSucceeded()) {
            return AuditLogType.USER_VDC_LOGIN;
        }
        if (getActionReturnValue() == CreateUserSessionsError.NUM_OF_SESSIONS_EXCEEDED) {
            addCustomValue("MaxUserSessions", String.valueOf(maxUserSessions));
            return AuditLogType.USER_MAX_SESSIONS_EXCEEDED;
        }
        return AuditLogType.USER_VDC_LOGIN_FAILED;
    }
}

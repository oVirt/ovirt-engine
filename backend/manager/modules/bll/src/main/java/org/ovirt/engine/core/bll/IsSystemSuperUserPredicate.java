package org.ovirt.engine.core.bll;

import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.dao.PermissionDao;

@Named
@Singleton
final class IsSystemSuperUserPredicate implements Predicate<DbUser> {

    private final PermissionDao permissionDao;

    @Inject
    public IsSystemSuperUserPredicate(PermissionDao permissionDao) {
        Objects.requireNonNull(permissionDao, "permissionDao cannot be null");

        this.permissionDao = permissionDao;
    }

    @Override
    public boolean test(DbUser user) {
        // TODO: Investigate how to inject admin@internal to CoCo for all scheduled jobs. Current user is populated
        // only from logged-in user who executed a command from UI or RESTAPI. But for methods executed from
        // scheduler we don't have any logged-in user
        return user == null
                || getPermissionDao().getForRoleAndAdElementAndObjectWithGroupCheck(
                        PredefinedRoles.SUPER_USER.getId(),
                        user.getId(),
                        MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID) != null;
    }

    PermissionDao getPermissionDao() {
        return permissionDao;
    }
}

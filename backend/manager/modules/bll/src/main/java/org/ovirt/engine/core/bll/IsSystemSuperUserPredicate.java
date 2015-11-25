package org.ovirt.engine.core.bll;

import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDao;

@Named
@Singleton
final class IsSystemSuperUserPredicate implements Predicate<Guid> {

    private final PermissionDao permissionDao;

    @Inject
    public IsSystemSuperUserPredicate(PermissionDao permissionDao) {
        Objects.requireNonNull(permissionDao, "permissionDao cannot be null");

        this.permissionDao = permissionDao;
    }

    @Override
    public boolean test(Guid userId) {
        Permission superUserPermission = getPermissionDao()
                        .getForRoleAndAdElementAndObjectWithGroupCheck(
                                PredefinedRoles.SUPER_USER.getId(),
                                userId,
                                MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        return superUserPermission != null;
    }

    PermissionDao getPermissionDao() {
        return permissionDao;
    }
}

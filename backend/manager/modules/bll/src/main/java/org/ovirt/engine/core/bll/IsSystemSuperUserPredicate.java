package org.ovirt.engine.core.bll;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.utils.linq.Predicate;

@Named
@Singleton
final class IsSystemSuperUserPredicate implements Predicate<Guid> {

    private final PermissionDAO permissionDao;

    @Inject
    public IsSystemSuperUserPredicate(PermissionDAO permissionDao) {
        Objects.requireNonNull(permissionDao, "permissionDao cannot be null");

        this.permissionDao = permissionDao;
    }

    @Override
    public boolean eval(Guid userId) {
        Permissions superUserPermission = getPermissionDao()
                        .getForRoleAndAdElementAndObjectWithGroupCheck(
                                PredefinedRoles.SUPER_USER.getId(),
                                userId,
                                MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        return superUserPermission != null;
    }

    PermissionDAO getPermissionDao() {
        return permissionDao;
    }
}

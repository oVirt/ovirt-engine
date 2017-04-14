package org.ovirt.engine.ui.uicommonweb.comparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGroup;

public class UserGroupComparator {
    private static final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

    /**
     * Comparator for the group name field in {@code UserGroup}.
     */
    public static final Comparator<UserGroup> NAME = Comparator.comparing(UserGroup::getGroupName, lexoNumeric);

    /**
     * Comparator for the namespace field in {@code UserGroup}.
     */
    public static final Comparator<UserGroup> NAMESPACE = Comparator.comparing(UserGroup::getNamespace, lexoNumeric);

    /**
     * Comparator for the authorization provider (authz) field in {@code UserGroup}.
     */
    public static final Comparator<UserGroup> AUTHZ = Comparator.comparing(UserGroup::getAuthz, lexoNumeric);
}

package org.ovirt.engine.ui.uicommonweb.comparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGroup;

public class UserGroupComparator {
    private static final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

    /**
     * Comparator for the group name field in {@code UserGroup}.
     */
    public static final Comparator<UserGroup> NAME = new Comparator<UserGroup>() {
        @Override
        public int compare(UserGroup userGroup1, UserGroup userGroup2) {
            return lexoNumeric.compare(userGroup1.getGroupName(), userGroup2.getGroupName());
        }
    };

    /**
     * Comparator for the namespace field in {@code UserGroup}.
     */
    public static final Comparator<UserGroup> NAMESPACE = new Comparator<UserGroup>() {
        @Override
        public int compare(UserGroup userGroup1, UserGroup userGroup2) {
            return lexoNumeric.compare(userGroup1.getNamespace(), userGroup2.getNamespace());
        }
    };

    /**
     * Comparator for the authorization provider (authz) field in {@code UserGroup}.
     */
    public static final Comparator<UserGroup> AUTHZ = new Comparator<UserGroup>() {
        @Override
        public int compare(UserGroup userGroup1, UserGroup userGroup2) {
            return lexoNumeric.compare(userGroup1.getAuthz(), userGroup2.getAuthz());
        }
    };
}

package org.ovirt.engine.core.bll;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

public class UserProfileTestHelper {
    public static void checkAssertsForSshProp(UserProfileProperty inputSshProp, UserProfileProperty outputProp) {
        assertThat(outputProp.getContent()).as("content").isEqualTo(inputSshProp.getContent());
        assertThat(outputProp.getType()).as("type")
                .isEqualTo(UserProfileProperty.PropertyType.SSH_PUBLIC_KEY);
        assertThat(outputProp.getName()).as("key").isEqualTo(inputSshProp.getName());
    }

    public static void checkAssertsForGenericProp(UserProfileProperty inputProp, UserProfileProperty outputProp) {
        assertThat(outputProp.getType()).as("type").isEqualTo(inputProp.getType());
        assertThat(outputProp.getUserId()).as("user ID").isEqualTo(inputProp.getUserId());
        assertThat(outputProp.getPropertyId()).as("key ID").isEqualTo(outputProp.getPropertyId());
    }

    public static String buildValidationMessage(ActionReturnValue returnValue) {
        return String.join("|", returnValue
                .getValidationMessages());
    }

    public static DbUser createWithId(Guid id) {
        DbUser user = new DbUser();
        user.setId(id);
        return user;
    }

    public static DbUser emptyUser() {
        return createWithId(Guid.Empty);
    }

}

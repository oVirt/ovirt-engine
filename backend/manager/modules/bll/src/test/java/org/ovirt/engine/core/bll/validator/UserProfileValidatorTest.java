package org.ovirt.engine.core.bll.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.ovirt.engine.core.bll.UserProfileTestHelper.emptyUser;
import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.JSON;
import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.SSH_PUBLIC_KEY;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

class UserProfileValidatorTest {

    private final UserProfileValidator validator = new UserProfileValidator();

    @Test
    void noProperty() {
        assertFalse(validator.propertyProvided(null).isValid());
    }

    @Test
    void propertyProvided() {
        assertTrue(validator.propertyProvided(mock(UserProfileProperty.class)).isValid());
    }

    @Test
    void propertyWithGivenNameAlreadyExists() {
        UserProfile profile = UserProfile.builder()
                .withProp(UserProfileProperty.builder()
                        .withDefaultSshProp().build()
                ).build();
        assertFalse(validator.firstPropertyWithGivenName(SSH_PUBLIC_KEY.name(), profile).isValid());
    }

    @Test
    void noPropertyWithGivenName() {
        assertTrue(validator.firstPropertyWithGivenName(SSH_PUBLIC_KEY.name(), UserProfile.builder().build())
                .isValid());
    }

    @Test
    void notAuthorizedDueToNullCurrentUser() {
        assertFalse(validator.authorized(null, Guid.Empty).isValid());
    }

    @Test
    void notAuthorizedDueToNullCurrentUserId() {
        assertFalse(validator.authorized(mock(DbUser.class), Guid.Empty).isValid());
    }

    @Test
    void notAuthorizedDueToNullTargetId() {
        assertFalse(validator.authorized(emptyUser(), null).isValid());
    }

    @Test
    void notAuthorizedDueToDifferentTargetId() {
        assertFalse(validator.authorized(emptyUser(), Guid.newGuid()).isValid());
    }

    @Test
    void authorized() {
        assertTrue(validator.authorized(emptyUser(), Guid.Empty).isValid());
    }

    @Test
    void authorizedAsAdminForOtherUser() {
        DbUser user = emptyUser();
        user.setAdmin(true);
        assertTrue(validator.authorized(user, Guid.newGuid()).isValid());
    }

    @Test
    void authorizedAsAdminForHimself() {
        DbUser user = emptyUser();
        user.setAdmin(true);
        assertTrue(validator.authorized(user, Guid.Empty).isValid());
    }

    @Test
    void validPublicSshKeyWhenNotSshKey() {
        assertTrue(validator.validPublicSshKey(mock(UserProfileProperty.class)).isValid());
    }

    @Test
    void invalidPublicSshKeyDueToNoKey() {
        assertFalse(validator.validPublicSshKey(UserProfileProperty.builder().withDefaultSshProp().build()).isValid());
    }

    @Test
    void invalidPublicSshKeyDueToEmptyKey() {
        UserProfileProperty prop = UserProfileProperty.builder().withDefaultSshProp().
                withContent("").build();
        assertFalse(validator.validPublicSshKey(prop).isValid());
    }

    @Test
    void validPublicSshKey() {
        UserProfileProperty prop = UserProfileProperty.builder()
                .withDefaultSshProp()
                .withContent(
                        "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCjtqZW+UG0fTSjizjAWUfjFnz7DEevRDGLwGzm0bHXqVfhxXCBoJpvRwue84liIbQ/56G9+gz0yeXHvQVYRPYWfmLrvIVH0uHkXP287Dzrbr+sDlIWZCuh+oMR2s3sD4hAi+rYVrtkPbUHKthNjjJKRGu0rKsc++Bg5aykgHKftpf0Dlw5lrDKBwXAgkAklp6Lz6hKr503la7WkBCDxGgxDKsp3RFuhtfQvBRE6UAkKMl/f8VMs14J1GMtnG8hBawY5l5wmjBXglaOVjH7b/kF+UchvVbDGWf8iJrvH3X1Yo0dr1zbR+iz4uO1ZkfkIjmc6C1Q8NshJexT/DFLjLCT")
                .build();
        assertTrue(validator.validPublicSshKey(prop).isValid());
    }

    @Test
    void differentOwner() {
        assertFalse(validator.sameOwner(Guid.Empty, Guid.newGuid()).isValid());
    }

    @Test
    void sameOwner() {
        assertTrue(validator.sameOwner(Guid.Empty, Guid.Empty).isValid());
    }

    @Test
    void secondSshKey() {
        UserProfile profile = UserProfile.builder()
                .withProp(UserProfileProperty.builder().withDefaultSshProp().build())
                .build();
        UserProfileProperty sshProp = UserProfileProperty.builder().withDefaultSshProp().build();
        assertFalse(validator.firstPublicSshKey(profile, sshProp).isValid());
    }

    @Test
    void noSshKeyYet() {
        UserProfileProperty sshProp = UserProfileProperty.builder().withDefaultSshProp().build();
        assertThat(validator.firstPublicSshKey(new UserProfile(), sshProp).isValid());
    }

    @Test
    void sameName() {
        assertTrue(validator.sameName(SSH_PUBLIC_KEY.name(), SSH_PUBLIC_KEY.name(), Guid.Empty).isValid());
    }

    @Test
    void differentName() {
        assertFalse(validator.sameName(SSH_PUBLIC_KEY.name(), JSON.name(), Guid.Empty).isValid());
    }

    @Test
    void sameType() {
        assertTrue(validator.sameType(SSH_PUBLIC_KEY, SSH_PUBLIC_KEY, Guid.Empty).isValid());
    }

    @Test
    void differentType() {
        assertFalse(validator.sameType(SSH_PUBLIC_KEY, JSON, Guid.Empty).isValid());
    }

}

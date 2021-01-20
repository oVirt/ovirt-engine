package org.ovirt.engine.core.common.businessentities;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.Guid;

class UserProfileTest {

    @Test
    void contentFromSshPropOnly() {
        UserProfileProperty sshProp = UserProfileProperty.builder()
                .withDefaultSshProp()
                .withPropertyId(Guid.Empty)
                .withContent("ssh_content")
                .build();
        UserProfileProperty jsonProp = UserProfileProperty.builder()
                .withTypeJson()
                .withName("json_prop")
                .withPropertyId(Guid.newGuid())
                .withContent("{}")
                .build();
        UserProfile profile = UserProfile.builder()
                .withProp(sshProp)
                .withProp(jsonProp)
                .build();
        assertThat(profile.getSshProperties())
                .containsOnly(UserProfileProperty.builder()
                        .from(sshProp)
                        .build());
    }

    @Test
    void nullProperties() {
        assertThrows(NullPointerException.class, () -> new UserProfile(Guid.Empty, null));
        assertThrows(NullPointerException.class, () -> new UserProfile(Guid.Empty, Collections.singletonList(null)));
    }

    @Test
    void duplicatedPropertyKeys() {
        UserProfileProperty[] props = {
                UserProfileProperty.builder().withName("A").build(),
                UserProfileProperty.builder().withName("A").build()
        };
        assertThrows(IllegalStateException.class, () -> new UserProfile(Guid.Empty, Arrays.asList(props)));
    }

    @Test
    void emptyProfileFromBuilder() {
        assertEquals(UserProfile.builder().build(), new UserProfile());
    }

    @Test
    void propertiesFromBuilder() {
        UserProfileProperty inputProp = UserProfileProperty.builder()
                .withType(UserProfileProperty.PropertyType.UNKNOWN)
                .withContent("custom_content")
                .withPropertyId(Guid.newGuid())
                .withName("custom_key")
                .withUserId(Guid.newGuid())
                .build();
        UserProfile profile = UserProfile.builder()
                .withUserId(Guid.newGuid())
                .withProperties(Collections.singletonList(inputProp))
                .build();

        assertThat(profile.getProperties()).isNotEmpty();
        assertThat(profile.getProperties()).hasOnlyOneElementSatisfying(prop -> {
            assertThat(prop.getType()).isEqualTo(inputProp.getType());
            assertThat(prop.getName()).isEqualTo(inputProp.getName());
            assertThat(prop.getPropertyId()).isEqualTo(inputProp.getPropertyId());
            assertThat(prop.getContent()).isEqualTo(inputProp.getContent());
            assertThat(prop.getUserId()).isEqualTo(profile.getUserId());
        });
    }

    @Test
    void propertiesFromDifferentUser() {
        UserProfile profileA = UserProfile.builder()
                .withUserId(Guid.newGuid())
                .withProp(UserProfileProperty.builder()
                        .withDefaultSshProp()
                        .build())
                .build();
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(Guid.Empty, profileA.getProperties()));
    }

}

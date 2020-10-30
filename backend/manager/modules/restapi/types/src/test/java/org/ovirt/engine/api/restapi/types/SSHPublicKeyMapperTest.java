package org.ovirt.engine.api.restapi.types;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.compat.Guid;

class SSHPublicKeyMapperTest {

    @Test
    void fromPropertyToKey() {
        UserProfileProperty prop = UserProfileProperty.builder()
                .withNewIdIfEmpty()
                .withContent("from_prop")
                .withDefaultSshProp()
                .build();
        SshPublicKey result = SSHPublicKeyMapper.map(prop, null);
        assertNotNull(result);
        assertThat(result.getContent()).isEqualTo("from_prop");
        assertThat(result.getId()).isEqualTo(prop.getPropertyId().toString());
    }

    @Test
    void fromKeyToPropertyNoTemplateNoId() {
        UserProfileProperty result = SSHPublicKeyMapper.map(keyWithId(null), null);

        assertThat(result).matches(UserProfileProperty::isSshPublicKey);
        assertThat(result.getContent()).isEqualTo("content");
        assertThat(result.getPropertyId()).isNotEqualTo(Guid.Empty);
        assertThat(result.getPropertyId().toString()).isNotBlank();
    }

    @ParameterizedTest
    @MethodSource
    void fromKeyToProperty(SshPublicKey key, UserProfileProperty template, String expectedKeyId) {
        UserProfileProperty result = SSHPublicKeyMapper.map(key, template);

        assertThat(result).matches(UserProfileProperty::isSshPublicKey);
        assertThat(result.getContent()).isEqualTo("content");
        assertThat(result.getPropertyId().toString()).isEqualTo(expectedKeyId);
    }

    private static Stream<Arguments> fromKeyToProperty() {
        UserProfileProperty template = UserProfileProperty.builder()
                .withNewIdIfEmpty()
                .withContent("content_from_template")
                .withDefaultSshProp()
                .build();

        SshPublicKey keyWithId = keyWithId(Guid.newGuid().toString());
        SshPublicKey keyWithoutId = keyWithId(null);

        return Stream.of(
                Arguments.of(keyWithId, template, keyWithId.getId()),
                Arguments.of(keyWithoutId, template, template.getPropertyId().toString()),
                Arguments.of(keyWithId, null, keyWithId.getId())
        );
    }

    private static SshPublicKey keyWithId(String id) {
        SshPublicKey key = new SshPublicKey();
        key.setId(id);
        key.setContent("content");
        return key;
    }
}

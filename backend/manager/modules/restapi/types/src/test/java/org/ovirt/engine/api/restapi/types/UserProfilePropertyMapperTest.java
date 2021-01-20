package org.ovirt.engine.api.restapi.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.model.UserOption;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.compat.Guid;

class UserProfilePropertyMapperTest {

    @Test
    void fromPropertyToKey() {
        UserProfileProperty prop = UserProfileProperty.builder()
                .withNewIdIfEmpty()
                .withContent("from_prop")
                .withDefaultSshProp()
                .build();
        SshPublicKey result = UserProfilePropertyMapper.map(prop, (SshPublicKey) null);
        assertNotNull(result);
        assertThat(result.getContent()).isEqualTo("from_prop");
        assertThat(result.getId()).isEqualTo(prop.getPropertyId().toString());
    }

    @Test
    void fromPropertyToOption() {
        UserProfileProperty prop = UserProfileProperty.builder()
                .withNewIdIfEmpty()
                .withContent("from_prop")
                .withTypeJson()
                .withName("some_option")
                .build();
        UserOption result = UserProfilePropertyMapper.map(prop, (UserOption) null);
        assertNotNull(result);
        assertThat(result.getContent()).isEqualTo("from_prop");
        assertThat(result.getId()).isEqualTo(prop.getPropertyId().toString());
        assertThat(result.getName()).isEqualTo(prop.getName());
    }

    @Test
    void fromEmptyKeyToPropertyNoTemplate() {
        UserProfileProperty result = UserProfilePropertyMapper.map(new SshPublicKey(), null);

        assertThat(result).matches(UserProfileProperty::isSshPublicKey);
        assertThat(result.getContent()).isNull();
        assertThat(result.getPropertyId()).isNotEqualTo(Guid.Empty);
        assertThat(result.getPropertyId().toString()).isNotBlank();
    }

    @Test
    void fromEmptyOptionToPropertyNoTemplate() {
        assertThrows(NullPointerException.class, () -> UserProfilePropertyMapper.map(new UserOption(), null));
    }

    @Test
    void fromAlmostEmptyOptionToPropertyNoTemplate() {
        UserOption option = new UserOption();
        // there is no default for name
        option.setName("name");

        UserProfileProperty result = UserProfilePropertyMapper.map(option, null);

        assertThat(result).matches(UserProfileProperty::isJsonProperty);
        assertThat(result.getContent()).isNull();
        assertThat(result.getName()).isEqualTo("name");
        assertThat(result.getPropertyId()).isNotEqualTo(Guid.Empty);
        assertThat(result.getPropertyId().toString()).isNotBlank();
    }

    @Test
    void fromCompleteOptionToProperty() {
        UserOption option = new UserOption();
        option.setName("name");
        Guid id = Guid.newGuid();
        option.setId(id.toString());
        option.setContent("content");

        UserProfileProperty result = UserProfilePropertyMapper.map(option, null);

        assertThat(result).matches(UserProfileProperty::isJsonProperty);
        assertThat(result.getContent()).isEqualTo("content");
        assertThat(result.getName()).isEqualTo("name");
        assertThat(result.getPropertyId()).isEqualTo(id);
    }

    @ParameterizedTest
    @MethodSource
    void fromKeyToProperty(SshPublicKey key,
            UserProfileProperty template,
            String expectedKeyId,
            String expectedContent) {
        UserProfileProperty result = UserProfilePropertyMapper.map(key, template);

        assertThat(result).matches(UserProfileProperty::isSshPublicKey);
        assertThat(result.getContent()).isEqualTo(expectedContent);
        assertThat(result.getPropertyId().toString()).isEqualTo(expectedKeyId);
        assertThat(result.getName()).isEqualTo(UserProfileProperty.PropertyType.SSH_PUBLIC_KEY.name());
    }

    private static Stream<Arguments> fromKeyToProperty() {
        Guid templateId = Guid.newGuid();
        UserProfileProperty template = UserProfileProperty.builder()
                .withPropertyId(templateId)
                .withContent("content_from_template")
                .withDefaultSshProp()
                .build();
        String keyId = Guid.newGuid().toString();
        SshPublicKey keyWithIdNoContent = keyWithId(keyId);
        keyWithIdNoContent.setContent(null);

        return Stream.of(
                Arguments.of(keyWithId(keyId), template, keyId, "content"),
                Arguments.of(keyWithId(keyId), null, keyId, "content"),
                Arguments.of(keyWithId(null), template, templateId.toString(), "content"),
                Arguments.of(keyWithIdNoContent, template, keyId, "content_from_template"),
                Arguments.of(new SshPublicKey(), template, templateId.toString(), "content_from_template")
        );
    }

    private static SshPublicKey keyWithId(String id) {
        SshPublicKey key = new SshPublicKey();
        key.setId(id);
        key.setContent("content");
        return key;
    }
}

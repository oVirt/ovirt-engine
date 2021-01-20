package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.JSON;
import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.SSH_PUBLIC_KEY;

import java.util.Optional;

import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.model.UserOption;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType;
import org.ovirt.engine.core.compat.Guid;

public class UserProfilePropertyMapper {
    @Mapping(from = UserProfileProperty.class, to = SshPublicKey.class)
    public static SshPublicKey map(UserProfileProperty entity, SshPublicKey template) {
        SshPublicKey model = template != null ? template : new SshPublicKey();
        model.setId(entity.getPropertyId().toString());
        model.setContent(entity.getContent());
        return model;
    }

    @Mapping(from = SshPublicKey.class, to = UserProfileProperty.class)
    public static UserProfileProperty map(SshPublicKey entity, UserProfileProperty template) {
        return map(
                entity.getId(),
                SSH_PUBLIC_KEY,
                SSH_PUBLIC_KEY.name(),
                entity.getContent(),
                template);
    }

    @Mapping(from = UserProfileProperty.class, to = UserOption.class)
    public static UserOption map(UserProfileProperty entity, UserOption template) {
        UserOption model = template != null ? template : new UserOption();
        model.setId(entity.getPropertyId().toString());
        model.setContent(entity.getContent());
        model.setName(entity.getName());
        return model;
    }

    @Mapping(from = UserOption.class, to = UserProfileProperty.class)
    public static UserProfileProperty map(UserOption entity, UserProfileProperty template) {
        return map(
                entity.getId(),
                JSON,
                entity.getName(),
                entity.getContent(),
                template);
    }

    private static UserProfileProperty map(String id,
            PropertyType type, String name, String content, UserProfileProperty template) {
        if (template == null) {
            return UserProfileProperty.builder()
                    .withType(type)
                    .withPropertyId(Optional.ofNullable(id).map(GuidUtils::asGuid).orElse(Guid.newGuid()))
                    .withContent(content)
                    .withName(name)
                    .build();
        }

        return UserProfileProperty.builder()
                .from(template)
                .withType(type)
                .withPropertyId(Optional.ofNullable(id)
                        .map(GuidUtils::asGuid)
                        .orElse(template.getPropertyId()))
                .withContent(Optional.ofNullable(content).orElse(template.getContent()))
                .withName(name)
                .build();
    }
}

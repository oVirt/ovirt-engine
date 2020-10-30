package org.ovirt.engine.api.restapi.types;

import java.util.Optional;

import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.compat.Guid;

public class SSHPublicKeyMapper {

    @Mapping(from = UserProfileProperty.class, to = SshPublicKey.class)
    public static SshPublicKey map(UserProfileProperty entity, SshPublicKey template) {
        SshPublicKey model = template != null ? template : new SshPublicKey();
        model.setId(entity.getPropertyId().toString());
        model.setContent(entity.getContent());
        return model;
    }

    @Mapping(from = SshPublicKey.class, to = UserProfileProperty.class)
    public static UserProfileProperty map(SshPublicKey model, UserProfileProperty template) {
        if (template == null) {
            return UserProfileProperty.builder()
                    .withDefaultSshProp()
                    .withPropertyId(Optional.ofNullable(model.getId()).map(GuidUtils::asGuid).orElse(Guid.newGuid()))
                    .withContent(model.getContent())
                    .build();
        }

        return UserProfileProperty.builder()
                .from(template)
                .withDefaultSshProp()
                .withPropertyId(Optional.ofNullable(model.getId()).map(GuidUtils::asGuid).orElse(template.getPropertyId()))
                .withContent(Optional.ofNullable(model.getContent()).orElse(template.getContent()))
                .build();
    }
}

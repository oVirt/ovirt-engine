package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.UserProfile;

public class SSHPublicKeyMapper {

    @Mapping(from = UserProfile.class, to = SshPublicKey.class)
    public static SshPublicKey map(UserProfile entity, SshPublicKey template) {
        SshPublicKey model = template != null ? template : new SshPublicKey();
        model.setId(entity.getSshPublicKeyId().toString());
        model.setContent(entity.getSshPublicKey());
        return model;
    }

    @Mapping(from = SshPublicKey.class, to = UserProfile.class)
    public static UserProfile map(SshPublicKey model, UserProfile template) {
        UserProfile entity = template != null ? template : new UserProfile();
        if (model.isSetContent()) {
            entity.setSshPublicKey(model.getContent());
        }
        if (model.isSetId()) {
            entity.setSshPublicKeyId(GuidUtils.asGuid(model.getId()));
        }
        return entity;
    }
}

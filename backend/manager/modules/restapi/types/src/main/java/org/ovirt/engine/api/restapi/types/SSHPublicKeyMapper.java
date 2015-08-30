package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.SSHPublicKey;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.UserProfile;

public class SSHPublicKeyMapper {

    @Mapping(from = UserProfile.class, to = SSHPublicKey.class)
    public static SSHPublicKey map(UserProfile entity, SSHPublicKey template) {
        SSHPublicKey model = template != null ? template : new SSHPublicKey();
        model.setId(entity.getSshPublicKeyId().toString());
        model.setContent(entity.getSshPublicKey());
        return model;
    }

    @Mapping(from = SSHPublicKey.class, to = UserProfile.class)
    public static UserProfile map(SSHPublicKey model, UserProfile template) {
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

package org.ovirt.engine.ui.uicommonweb.models.options;

import java.util.Objects;
import java.util.Optional;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

class PublicSshKeyField implements Field<String> {

    private final EntityModel<String> publicKey;
    private String originalPublicKey = "";
    private UserProfileProperty sshProp;

    public PublicSshKeyField(EntityModel<String> model, UserProfileProperty sshProp) {
        this.publicKey = model;
        this.sshProp = sshProp;
    }

    public UserProfileProperty toProp() {
        return UserProfileProperty.builder()
                .from(sshProp)
                .withContent(getNewPublicKey())
                .build();
    }

    @Override
    public void fromProp(UserProfileProperty prop) {
        this.sshProp = prop;
        String content = Optional.ofNullable(prop)
                .map(UserProfileProperty::getContent)
                .map(String::trim)
                .orElse("");
        this.originalPublicKey = content;
        this.publicKey.setEntity(content);
    }

    @Override
    public boolean isSupported(UserProfileProperty prop) {
        return prop.isSshPublicKey();
    }

    @Override
    public EntityModel<String> getEntity() {
        return publicKey;
    }

    @Override
    public boolean isUpdated() {
        return !Objects.equals(originalPublicKey, getNewPublicKey())
                && !getNewPublicKey().isEmpty();
    }

    private String getNewPublicKey() {
        return Optional.ofNullable(publicKey.getEntity()).orElse("").trim();
    }

    @Override
    public boolean isRemoved() {
        return !Objects.equals(originalPublicKey, getNewPublicKey())
                && !originalPublicKey.isEmpty()
                && getNewPublicKey().isEmpty();
    }
}

package org.ovirt.engine.core.dao;

import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.SSH_PUBLIC_KEY;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.node.NullNode;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.UserSshKey;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class UserProfileDaoImpl extends BaseDao implements UserProfileDao {

    private static class UserProfilePropertyView {

        private final UserProfileProperty property;

        private final String loginName;

        public UserProfilePropertyView(UserProfileProperty property, String loginName) {
            this.property = property;
            this.loginName = loginName;
        }

        public String getLoginName() {
            return loginName;
        }

        public UserProfileProperty getProperty() {
            return property;
        }
    }

    private static final RowMapper<UserProfilePropertyView> userProfilePropertyRowMapper = (rs, rowNum) -> {
        UserProfileProperty prop = UserProfileProperty.builder()
                .withUserId(getGuidDefaultEmpty(rs, "user_id"))
                .withPropertyId(getGuidDefaultEmpty(rs, "property_id"))
                .withName(rs.getString("property_name"))
                .withType(UserProfileProperty.PropertyType.valueOf(rs.getString("property_type")))
                .withContent(Optional.ofNullable(rs.getString("property_content"))
                        .orElse(NullNode.getInstance().asText()))
                .build();
        UserProfilePropertyView entity = new UserProfilePropertyView(prop, rs.getString("login_name"));
        return deserializeSshKeys(entity);
    };

    /**
     * Repackage to public type because of GWT serialization.
     * GWT serialization requires declaring all DTO in *.gwt.xml.
     */
    private static Optional<UserProfileProperty> toPublicEntity(UserProfilePropertyView view) {
        return Optional.ofNullable(view).map(UserProfilePropertyView::getProperty);
    }

    private static UserProfilePropertyView deserializeSshKeys(UserProfilePropertyView view) {
        if (!view.getProperty().isSshPublicKey()) {
            return view;
        }

        return new UserProfilePropertyView(
                UserProfileProperty.builder()
                        .from(view.getProperty())
                        .withContent(Optional
                                .ofNullable(SerializationFactory.getDeserializer().deserialize(
                                        view.getProperty().getContent(),
                                        String.class))
                                .orElse(""))
                        .build(),
                view.getLoginName()
        );
    }

    private static String serializeSshKeys(String content, UserProfileProperty.PropertyType type) {
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        if (SSH_PUBLIC_KEY.equals(type)) {
            return serializer.serializeUnformattedJson(content);
        }
        return content;
    }

    private UserProfile reduceToProfile(Guid userId,
            List<UserProfilePropertyView> props) {
        if (props == null || props.isEmpty() || props.contains(null)) {
            return new UserProfile(userId, Collections.emptyList());
        }

        return new UserProfile(
                userId,
                props.stream()
                        .map(UserProfileDaoImpl::toPublicEntity)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public UserProfileProperty get(Guid propertyId) {
        if (propertyId == null || Guid.Empty.equals(propertyId)) {
            return null;
        }
        return toPublicEntity(getCallsHandler().executeRead(
                "GetUserProfileProperty",
                userProfilePropertyRowMapper,
                getCustomMapSqlParameterSource().addValue("property_id", propertyId))
        ).orElse(null);
    }

    @Override
    public void save(UserProfileProperty prop) {
        if (Guid.Empty.equals(prop.getPropertyId())) {
            throw new IllegalArgumentException("Illegal UUID:" + Guid.Empty);
        }
        getCallsHandler().executeModification("InsertUserProfileProperty", createOptionMapper(prop));
    }

    private MapSqlParameterSource createOptionMapper(UserProfileProperty prop) {
        return getCustomMapSqlParameterSource()
                .addValue("user_id", prop.getUserId())
                .addValue("property_id", prop.getPropertyId())
                .addValue("property_name", prop.getName())
                .addValue("property_type", prop.getType().name())
                .addValue("property_content", serializeSshKeys(prop.getContent(), prop.getType()));
    }

    @Override
    public void update(UserProfileProperty property, Guid newKeyId) {
        if (Guid.Empty.equals(newKeyId)) {
            throw new IllegalArgumentException("Illegal UUID:" + Guid.Empty);
        }

        getCallsHandler().executeModification("UpdateUserProfileProperty",
                createOptionMapper(property).addValue("new_property_id", newKeyId));

    }

    @Override
    public void remove(Guid keyId) {
        getCallsHandler().executeModification("DeleteUserProfileProperty",
                getCustomMapSqlParameterSource().addValue("property_id", keyId));
    }

    @Override
    public List<UserProfileProperty> getAll(Guid userId) {
        return getAllInternal(userId)
                .stream()
                .map(UserProfileDaoImpl::toPublicEntity)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Override
    public UserProfile getProfile(Guid userId) {
        if (userId == null || Guid.Empty.equals(userId)) {
            return new UserProfile();
        }

        return reduceToProfile(userId, getAllInternal(userId));
    }

    private List<UserProfilePropertyView> getAllInternal(Guid userId) {
        if (userId == null || Guid.Empty.equals(userId)) {
            return Collections.emptyList();
        }

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userId);

        return getCallsHandler()
                .executeReadList(
                        "GetUserProfileByUserId",
                        userProfilePropertyRowMapper,
                        parameterSource);
    }

    @Override
    public List<UserSshKey> getAllPublicSshKeys() {
        return getCallsHandler()
                .executeReadList(
                        "GetAllPublicSshKeysFromUserProfiles",
                        userProfilePropertyRowMapper,
                        getCustomMapSqlParameterSource()
                ).stream()
                .map(UserProfileDaoImpl::toSshKey)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private static Optional<UserSshKey> toSshKey(UserProfilePropertyView view) {
        if (view == null || view.getProperty() == null || !view.getProperty().isSshPublicKey()) {
            return Optional.empty();
        }

        return Optional.of(new UserSshKey(
                view.getLoginName(),
                view.getProperty().getUserId(),
                view.getProperty().getContent()
        ));
    }

}

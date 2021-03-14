package org.ovirt.engine.core.dao;

import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.SSH_PUBLIC_KEY;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.UserSshKey;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.fasterxml.jackson.databind.node.NullNode;

@Named
@Singleton
public class UserProfileDaoImpl extends BaseDao implements UserProfileDao {

    private static final RowMapper<UserProfileProperty> userProfilePropertyRowMapper =
            (rs, rowNum) -> deserializeSshKeys(UserProfileProperty.builder()
                    .withUserId(getGuidDefaultEmpty(rs, "user_id"))
                    .withPropertyId(getGuidDefaultEmpty(rs, "property_id"))
                    .withName(rs.getString("property_name"))
                    .withType(UserProfileProperty.PropertyType.valueOf(rs.getString("property_type")))
                    .withContent(Optional.ofNullable(rs.getString("property_content"))
                            .orElse(NullNode.getInstance().asText()))
                    .build());

    private static final RowMapper<UserSshKey> userSshKeyRowMapper = (rs, rowNum) -> new UserSshKey(
            rs.getString("login_name"),
            getGuidDefaultEmpty(rs, "user_id"),
            deserializeSshKeys(rs.getString("property_content"))
    );

    private static String deserializeSshKeys(String encodedKey) {
        if (encodedKey == null) {
            return "";
        }

        return Optional
                .ofNullable(SerializationFactory.getDeserializer().deserialize(
                        encodedKey,
                        String.class))
                .orElse("");
    }

    private static UserProfileProperty deserializeSshKeys(UserProfileProperty view) {
        if (!view.isSshPublicKey()) {
            return view;
        }

        return UserProfileProperty.builder()
                .from(view)
                .withContent(deserializeSshKeys(view.getContent()))
                .build();

    }

    private static String serializeSshKeys(String content, UserProfileProperty.PropertyType type) {
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        if (SSH_PUBLIC_KEY.equals(type)) {
            return serializer.serializeUnformattedJson(content);
        }
        return content;
    }

    @Override
    public UserProfileProperty get(Guid propertyId) {
        if (propertyId == null || Guid.Empty.equals(propertyId)) {
            return null;
        }
        return getCallsHandler().executeRead(
                "GetUserProfileProperty",
                userProfilePropertyRowMapper,
                getCustomMapSqlParameterSource().addValue("property_id", propertyId));
    }

    @Override
    public Guid save(UserProfileProperty prop) {
        Guid id = Guid.newGuid();
        getCallsHandler().executeModification(
                "InsertUserProfileProperty",
                createOptionMapper(prop)
                        .addValue("property_id", id));
        return id;
    }

    private MapSqlParameterSource createOptionMapper(UserProfileProperty prop) {
        return getCustomMapSqlParameterSource()
                .addValue("user_id", prop.getUserId())
                .addValue("property_name", prop.getName())
                .addValue("property_type", prop.getType().name())
                .addValue("property_content", serializeSshKeys(prop.getContent(), prop.getType()));
    }

    @Override
    public UserProfileProperty update(UserProfileProperty property) {
        return getCallsHandler().executeRead(
                "UpdateUserProfileProperty",
                userProfilePropertyRowMapper,
                createOptionMapper(property)
                        .addValue("property_id", property.getPropertyId())
                        .addValue("new_property_id", Guid.newGuid()));
    }

    @Override
    public void remove(Guid keyId) {
        getCallsHandler().executeModification("DeleteUserProfileProperty",
                getCustomMapSqlParameterSource().addValue("property_id", keyId));
    }

    @Override
    public List<UserProfileProperty> getAll(Guid userId) {
        return getAllInternal(userId);
    }

    @Override
    public UserProfile getProfile(Guid userId) {
        if (userId == null || Guid.Empty.equals(userId)) {
            return new UserProfile();
        }

        return new UserProfile(userId, getAllInternal(userId));
    }

    private List<UserProfileProperty> getAllInternal(Guid userId) {
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
                        userSshKeyRowMapper,
                        getCustomMapSqlParameterSource()
                );
    }

    @Override
    public UserProfileProperty getByName(String propertyName, Guid userId) {
        if (userId == null || Guid.Empty.equals(userId)) {
            return null;
        }
        return getCallsHandler().executeRead(
                "GetUserProfilePropertyByName",
                userProfilePropertyRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", userId)
                        .addValue("property_name", propertyName));
    }
}

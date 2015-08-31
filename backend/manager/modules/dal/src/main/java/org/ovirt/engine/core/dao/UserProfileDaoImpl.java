package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class UserProfileDaoImpl extends BaseDao implements UserProfileDao {

    private static final class UserProfileRowMapper implements RowMapper<UserProfile> {
        public static final UserProfileRowMapper instance = new UserProfileRowMapper();

        @Override
        public UserProfile mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            UserProfile entity = new UserProfile();
            entity.setId(getGuidDefaultEmpty(rs, "profile_id"));
            entity.setUserId(getGuidDefaultEmpty(rs, "user_id"));
            entity.setSshPublicKeyId(getGuidDefaultEmpty(rs, "ssh_public_key_id"));
            entity.setSshPublicKey(rs.getString("ssh_public_key"));
            entity.setLoginName(rs.getString("login_name"));
            entity.setUserPortalVmLoginAutomatically(rs.getBoolean("user_portal_vm_auto_login"));
            return entity;
        }
    }

    @Override
    public UserProfile get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("profile_id", id);

        return getCallsHandler().executeRead("GetUserProfileByProfileId", UserProfileRowMapper.instance, parameterSource);
    }

    @Override
    public UserProfile getByUserId(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id);

        return getCallsHandler().executeRead("GetUserProfileByUserId", UserProfileRowMapper.instance, parameterSource);
    }

    @Override
    public List<UserProfile> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        return getCallsHandler().executeReadList("GetAllFromUserProfiles", UserProfileRowMapper.instance, parameterSource);
    }

    private MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("profile_id", id);
    }

    @Override
    public void save(UserProfile profile) {
        getCallsHandler().executeModification("InsertUserProfile",
                createIdParameterMapper(profile.getId())
                        .addValue("user_id", profile.getUserId())
                        .addValue("ssh_public_key_id", profile.getSshPublicKeyId())
                        .addValue("ssh_public_key", profile.getSshPublicKey())
                        .addValue("user_portal_vm_auto_login", profile.isUserPortalVmLoginAutomatically()));
    }

    @Override
    public void update(UserProfile profile) {
        getCallsHandler().executeModification("UpdateUserProfile",
                                  createIdParameterMapper(profile.getId())
                                                  .addValue("user_id", profile.getUserId())
                                                  .addValue("ssh_public_key_id", profile.getSshPublicKeyId())
                                                  .addValue("ssh_public_key", profile.getSshPublicKey())
                                                  .addValue("user_portal_vm_auto_login", profile.isUserPortalVmLoginAutomatically()));
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = createIdParameterMapper(id);

        getCallsHandler().executeModification("DeleteUserProfile", parameterSource);
    }
}

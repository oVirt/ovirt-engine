package org.ovirt.engine.core.dao.provider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.KVMVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenStackProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.VmwareVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.XENVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.ovirt.engine.core.dao.provider.crypt.PasswordCryptorFactory;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class ProviderDaoImpl extends DefaultGenericDao<Provider<?>, Guid> implements ProviderDao {

    public ProviderDaoImpl() {
        super("Provider");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(Provider<?> entity) {
        MapSqlParameterSource mapper = createBaseProviderParametersMapper(entity);
        String tenantName = null;
        String pluginType = null;
        boolean readOnly = false;
        boolean autoSync = false;
        String userDomainName = null;
        String projectName = null;
        String projectDomainName = null;

        AdditionalProperties additionalProperties = null;

        if (entity.getAdditionalProperties() != null) {
            switch (entity.getType()) {
            case EXTERNAL_NETWORK:
            case OPENSTACK_NETWORK:
                OpenstackNetworkProviderProperties networkProperties =
                        (OpenstackNetworkProviderProperties) entity.getAdditionalProperties();
                readOnly = networkProperties.getReadOnly();
                pluginType = networkProperties.getPluginType();
                autoSync = networkProperties.getAutoSync();
            case OPENSTACK_IMAGE:
            case OPENSTACK_VOLUME:
                OpenStackProviderProperties openStackProviderProperties =
                        (OpenStackProviderProperties) entity.getAdditionalProperties();
                tenantName = openStackProviderProperties.getTenantName();
                userDomainName = openStackProviderProperties.getUserDomainName();
                projectName = openStackProviderProperties.getProjectName();
                projectDomainName = openStackProviderProperties.getProjectDomainName();
                break;
            case VMWARE:
            case KVM:
            case XEN:
            case KUBEVIRT:
                additionalProperties = entity.getAdditionalProperties();
                break;
            default:
                break;
            }
        }

        // We always add the values since JdbcTeplate expects them to be set, otherwise it throws an exception.
        mapper.addValue("tenant_name", tenantName);
        mapper.addValue("plugin_type", pluginType);
        mapper.addValue("additional_properties", SerializationFactory.getSerializer().serialize(additionalProperties));
        mapper.addValue("read_only", readOnly);
        mapper.addValue("auto_sync", autoSync);
        mapper.addValue("user_domain_name", userDomainName);
        mapper.addValue("project_name", projectName);
        mapper.addValue("project_domain_name", projectDomainName);
        return mapper;
    }

    protected MapSqlParameterSource createBaseProviderParametersMapper(Provider<?> entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("name", entity.getName())
                .addValue("description", entity.getDescription())
                .addValue("url", entity.getUrl())
                .addValue("provider_type", EnumUtils.nameOrNull(entity.getType()))
                .addValue("auth_required", entity.isRequiringAuthentication())
                .addValue("auth_username", entity.getUsername())
                .addValue("auth_password",
                        PasswordCryptorFactory.create(entity.getType()).encryptPassword(entity.getPassword()))
                .addValue("custom_properties",
                        SerializationFactory.getSerializer().serialize(entity.getCustomProperties()))
                .addValue("auth_url", entity.getAuthUrl())
                .addValue("is_unmanaged", entity.getIsUnmanaged());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<Provider<?>> createEntityRowMapper() {
        return ProviderRowMapper.INSTANCE;
    }

    @Override
    public Provider<?> getByName(String name) {
        return getCallsHandler().executeRead("GetProviderByName",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("name", name));
    }

    private static class ProviderRowMapper implements RowMapper<Provider<?>> {

        public static final ProviderRowMapper INSTANCE = new ProviderRowMapper();

        private ProviderRowMapper() {
        }

        @Override
        @SuppressWarnings("unchecked")
        public Provider<?> mapRow(ResultSet rs, int index) throws SQLException {
            Provider<AdditionalProperties> entity = new Provider<>();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setName(rs.getString("name"));
            entity.setDescription(rs.getString("description"));
            entity.setUrl(rs.getString("url"));
            entity.setType(ProviderType.valueOf(rs.getString("provider_type")));
            entity.setRequiringAuthentication(rs.getBoolean("auth_required"));
            entity.setUsername(rs.getString("auth_username"));
            entity.setPassword(PasswordCryptorFactory.create(entity.getType())
                    .decryptPassword(rs.getString("auth_password")));
            entity.setCustomProperties(SerializationFactory.getDeserializer()
                    .deserialize(rs.getString("custom_properties"), HashMap.class));
            entity.setAdditionalProperties(mapAdditionalProperties(rs, entity));
            entity.setAuthUrl(rs.getString("auth_url"));
            entity.setIsUnmanaged(rs.getBoolean("is_unmanaged"));

            return entity;
        }

        private AdditionalProperties mapAdditionalProperties(ResultSet rs, Provider<?> entity) throws SQLException {
            switch (entity.getType()) {
            case EXTERNAL_NETWORK:
            case OPENSTACK_NETWORK:
                OpenstackNetworkProviderProperties networkProperties = new OpenstackNetworkProviderProperties();
                mapOpenStackProperties(rs, networkProperties);
                networkProperties.setReadOnly(rs.getBoolean("read_only"));
                networkProperties.setAutoSync(rs.getBoolean("auto_sync"));
                networkProperties.setPluginType(rs.getString("plugin_type"));
                return networkProperties;
            case OPENSTACK_IMAGE:
                OpenStackImageProviderProperties imageProperties = new OpenStackImageProviderProperties();
                mapOpenStackProperties(rs, imageProperties);
                return imageProperties;
            case OPENSTACK_VOLUME:
                OpenStackVolumeProviderProperties volumeProperties = new OpenStackVolumeProviderProperties();
                mapOpenStackProperties(rs, volumeProperties);
                return volumeProperties;
            case VMWARE:
                return SerializationFactory.getDeserializer().deserialize(rs.getString("additional_properties"), VmwareVmProviderProperties.class);
            case KVM:
                return SerializationFactory.getDeserializer().deserialize(rs.getString("additional_properties"), KVMVmProviderProperties.class);
            case XEN:
                return SerializationFactory.getDeserializer().deserialize(rs.getString("additional_properties"), XENVmProviderProperties.class);
            case KUBEVIRT:
                return SerializationFactory.getDeserializer().deserialize(rs.getString("additional_properties"), KubevirtProviderProperties.class);
            default:
                return null;
            }
        }

        private void mapOpenStackProperties(ResultSet rs, OpenStackProviderProperties openStackProviderProperties) throws SQLException {
            openStackProviderProperties.setTenantName(rs.getString("tenant_name"));
            openStackProviderProperties.setUserDomainName(rs.getString("user_domain_name"));
            openStackProviderProperties.setProjectName(rs.getString("project_name"));
            openStackProviderProperties.setProjectDomainName(rs.getString("project_domain_name"));
        }
    }

    @Override
    public List<Provider<?>> getAllByTypes(ProviderType ... providerTypes) {
        if (providerTypes == null) {
            return Collections.emptyList();
        }
        return getCallsHandler().executeReadList("GetAllFromProvidersByTypes",
                                                 ProviderRowMapper.INSTANCE,
                                                 getCustomMapSqlParameterSource().addValue("provider_types", createArrayOf("varchar",
                                                         Arrays.stream(providerTypes).map(ProviderType::name).toArray())));
    }

    public List<Provider<?>> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, ProviderRowMapper.INSTANCE);
    }
}

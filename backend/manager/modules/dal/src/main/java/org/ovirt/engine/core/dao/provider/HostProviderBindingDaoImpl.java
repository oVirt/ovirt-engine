package org.ovirt.engine.core.dao.provider;

import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDao;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class HostProviderBindingDaoImpl extends BaseDao implements HostProviderBindingDao {

    public String get(Guid vdsId, String pluginType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
            .addValue("vds_id", vdsId)
            .addValue("plugin_type", pluginType);

        return getCallsHandler().executeRead("GetHostProviderBinding",
            (rs, nowNum) ->  rs.getString(1),
            parameterSource);
    }

    public void update(Guid vdsId, Map<String, Object> values) {
        int entryCount = values!=null ? values.size() : 0;
        String[] pluginTypes = new String[entryCount];
        String[] bindingIds = new String[entryCount];
        if (values!=null) {
            int i=0;
            for (Map.Entry<String, Object> pair: values.entrySet()) {
                pluginTypes[i] = pair.getKey();
                bindingIds[i] = (String) pair.getValue();
                i++;
            }
        }
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
            .addValue("vds_id", vdsId)
            .addValue("plugin_types", createArrayOf("text", pluginTypes))
            .addValue("provider_binding_host_ids" , createArrayOf("text", bindingIds));
        getCallsHandler().executeModification("UpdateHostProviderBinding", parameterSource);
    }
}

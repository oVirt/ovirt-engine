package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Named
@Singleton
public class VmIconDaoImpl extends DefaultGenericDaoDbFacade<VmIcon, Guid> implements VmIconDao {

    private static final String ID_COLUMN = "id";
    private static final String DATA_URL_COLUMN = "data_url";

    public VmIconDaoImpl() {
        super("VmIcon");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmIcon entity) {
        return createIdParameterMapper(entity.getId())
                .addValue(DATA_URL_COLUMN, entity.getDataUrl());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource()
                .addValue(ID_COLUMN, id);
    }

    @Override
    protected RowMapper<VmIcon> createEntityRowMapper() {
        return new RowMapper<VmIcon>() {
            @Override
            public VmIcon mapRow(ResultSet rs, int rowNum) throws SQLException {
                VmIcon icon = new VmIcon();
                icon.setId(getGuid(rs, ID_COLUMN));
                icon.setDataUrl(rs.getString(DATA_URL_COLUMN));
                return icon;
            }
        };
    }

    @Override
    public List<VmIcon> getByDataUrl(String dataUrl) {
        return getCallsHandler().executeReadList("GetVmIconByVmIconDataUrl", createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue(DATA_URL_COLUMN, dataUrl));
    }

    @Override
    public void removeIfUnused(Guid iconId) {
        getCallsHandler().executeModification("DeleteVmIconIfUnused",
                getCustomMapSqlParameterSource().addValue(ID_COLUMN, iconId));
    }

    @Override
    public Guid ensureIconInDatabase(final String icon) {
        if (icon == null) {
            throw new IllegalArgumentException("Argument 'icon' should not be null");
        }
        return TransactionSupport.executeInNewTransaction(new TransactionMethod<Guid>() {
            @Override
            public Guid runInTransaction() {
                final List<VmIcon> existingIcons = getByDataUrl(icon);
                if (!existingIcons.isEmpty()) {
                    return existingIcons.get(0).getId();
                }
                final VmIcon newIcon = new VmIcon(Guid.newGuid(), icon);
                save(newIcon);
                return newIcon.getId();
            }
        });
    }

    @Override
    public void removeAllUnusedIcons() {
        getCallsHandler().executeModification("DeleteAllUnusedVmIcons",
                getCustomMapSqlParameterSource());
    }
}

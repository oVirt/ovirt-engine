package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmIconDaoImpl extends DefaultGenericDao<VmIcon, Guid> implements VmIconDao {

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
        return (rs, rowNum) -> {
            VmIcon icon = new VmIcon();
            icon.setId(getGuid(rs, ID_COLUMN));
            icon.setDataUrl(rs.getString(DATA_URL_COLUMN));
            return icon;
        };
    }

    @Override
    public List<VmIcon> getAll(Guid userId, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllFromVmIconsFiltered",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("user_id", userId)
                        .addValue("is_filtered", isFiltered));
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
        return TransactionSupport.executeInNewTransaction(() -> {
            final List<VmIcon> existingIcons = getByDataUrl(icon);
            if (!existingIcons.isEmpty()) {
                return existingIcons.get(0).getId();
            }
            final VmIcon newIcon = new VmIcon(Guid.newGuid(), icon);
            save(newIcon);
            return newIcon.getId();
        });
    }

    @Override
    public void removeAllUnusedIcons() {
        getCallsHandler().executeModification("DeleteAllUnusedVmIcons",
                getCustomMapSqlParameterSource());
    }

    @Override
    public boolean exists(Guid id) {
        return getCallsHandler().executeRead("IsVmIconExist",
                SingleColumnRowMapper.newInstance(Boolean.class),
                getCustomMapSqlParameterSource().addValue(ID_COLUMN, id));
    }
}

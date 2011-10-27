/**
 *
 */
package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;


/**
 * DiskImageTemplate jdbc template DAO implementation
 */
public class DiskImageTemplateDAODbFacadeImpl extends BaseDAODbFacade implements DiskImageTemplateDAO {

    /**
     *
     */
    public DiskImageTemplateDAODbFacadeImpl() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.dao.ReadDao#get(java.io.Serializable)
     */
    @Override
    public DiskImageTemplate get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("it_guid", id);

        ParameterizedRowMapper<DiskImageTemplate> mapper = new ParameterizedRowMapper<DiskImageTemplate>() {
            @Override
            public DiskImageTemplate mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImageTemplate entity = new DiskImageTemplate();
                entity.setvtim_it_guid(Guid.createGuidFromString(rs
                        .getString("vtim_it_guid")));
                entity.setvmt_guid(Guid.createGuidFromString(rs
                        .getString("vmt_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setos(rs.getString("os"));
                entity.setos_version(rs.getString("os_version"));
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setsize(rs.getLong("size"));
                entity.setbootable((Boolean) rs.getObject("bootable"));
                entity.setdescription(rs.getString("description"));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetVmTemplateDisksByImageTemplateGuid", mapper, parameterSource);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.dao.ReadDao#getAll()
     */
    @Override
    public List<DiskImageTemplate> getAll() {
        throw new NotImplementedException();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.dao.ModificationDao#save(org.ovirt.engine.core.common.businessentities.BusinessEntity)
     */
    @Override
    public void save(DiskImageTemplate template) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("creation_date", template.getcreation_date())
                .addValue("description", template.getdescription())
                .addValue("it_guid", template.getit_guid())
                .addValue("vtim_it_guid", template.getvtim_it_guid())
                .addValue("size", template.getsize())
                .addValue("os", template.getos())
                .addValue("os_version", template.getos_version())
                .addValue("bootable", template.getbootable())
                .addValue("vmt_guid", template.getvmt_guid())
                .addValue("internal_drive_mapping",
                        template.getinternal_drive_mapping());

        getCallsHandler().executeModification("InsertVmTemplateDisk", parameterSource);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.dao.ModificationDao#update(org.ovirt.engine.core.common.businessentities.BusinessEntity)
     */
    @Override
    public void update(DiskImageTemplate template) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("creation_date", template.getcreation_date())
                .addValue("description", template.getdescription())
                .addValue("it_guid", template.getit_guid())
                .addValue("vtim_it_guid", template.getvtim_it_guid())
                .addValue("size", template.getsize())
                .addValue("os", template.getos())
                .addValue("os_version", template.getos_version())
                .addValue("bootable", template.getbootable())
                .addValue("vmt_guid", template.getvmt_guid())
                .addValue("internal_drive_mapping",
                        template.getinternal_drive_mapping());

        getCallsHandler().executeModification("UpdateVmTemplateDisk", parameterSource);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.dao.ModificationDao#remove(java.io.Serializable)
     */
    @Override
    public void remove(Guid diskImageTemplate) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
        .addValue("it_guid",  diskImageTemplate);
        getCallsHandler().executeModification("DeleteVmTemplateDisk", parameterSource);
    }

    @Override
    public DiskImageTemplate getByVmTemplateAndId(Guid vm, Guid vmTemplate) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vmt_guid", vm).addValue("it_guid", vmTemplate);

        ParameterizedRowMapper<DiskImageTemplate> mapper = new ParameterizedRowMapper<DiskImageTemplate>() {
            @Override
            public DiskImageTemplate mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImageTemplate entity = new DiskImageTemplate();
                entity.setvtim_it_guid(Guid.createGuidFromString(rs
                        .getString("vtim_it_guid")));
                entity.setvmt_guid(Guid.createGuidFromString(rs
                        .getString("vmt_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setos(rs.getString("os"));
                entity.setos_version(rs.getString("os_version"));
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setsize(rs.getLong("size"));
                entity.setbootable((Boolean) rs.getObject("bootable"));
                entity.setdescription(rs.getString("description"));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetVmTemplateDiskByVmtGuidAndItGuid", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DiskImageTemplate> getAllByVmTemplate(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vmt_guid", id);

        ParameterizedRowMapper<DiskImageTemplate> mapper = new ParameterizedRowMapper<DiskImageTemplate>() {
            @Override
            public DiskImageTemplate mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImageTemplate entity = new DiskImageTemplate();
                entity.setvtim_it_guid(Guid.createGuidFromString(rs
                        .getString("vtim_it_guid")));
                entity.setvmt_guid(Guid.createGuidFromString(rs
                        .getString("vmt_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setos(rs.getString("os"));
                entity.setos_version(rs.getString("os_version"));
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setsize(rs.getLong("size"));
                entity.setbootable((Boolean) rs.getObject("bootable"));
                entity.setdescription(rs.getString("description"));
                return entity;
            }
        };


        return getCallsHandler().executeReadList("GetVmTemplateDisksByVmtGuid", mapper, parameterSource);
    }

}

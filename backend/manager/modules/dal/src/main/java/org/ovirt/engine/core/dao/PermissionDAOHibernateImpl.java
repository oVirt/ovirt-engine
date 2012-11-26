package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class PermissionDAOHibernateImpl extends BaseDAOHibernateImpl<permissions, Guid> implements PermissionDAO {
    public PermissionDAOHibernateImpl() {
        super(permissions.class);
    }

    @Override
    public permissions get(Guid id) {
        Query query = getSession().createQuery("select perms " +
                "from permissions as perms, " +
                "roles as role " +
                "where role.id = perms.roleId " +
                "and perms.id = :permission_id");

        query.setParameter("permission_id", id);

        permissions result = (permissions) query.uniqueResult();
        return fillInPermissionDetails(result);
    }

    @Override
    public permissions getForRoleAndAdElementAndObject(Guid roleid, Guid elementid, Guid objectid) {
        return fillInPermissionDetails(findOneByCriteria(
                Restrictions.eq("roleId", roleid),
                Restrictions.eq("adElementId", elementid),
                Restrictions.eq("objectId", objectid)));
    }

    @Override
    public permissions getForRoleAndAdElementAndObjectWithGroupCheck(Guid roleid, Guid elementid, Guid objectid) {
        Query query = getSession().createQuery("from DbUser where id = :id");

        query.setParameter("id", objectid);

        DbUser user = (DbUser) query.uniqueResult();

        query = getSession().createQuery("from ad_groups where name in :names");
        query.setParameterList("names", user.getGroupsAsArray());

        @SuppressWarnings("unchecked")
        List<ad_groups> groups = query.list();

        List<Guid> ids = new ArrayList<Guid>();

        ids.add(user.getuser_id());
        for (ad_groups group : groups) {
            ids.add(group.getid());
        }

        return fillInPermissionDetails(findOneByCriteria(
                Restrictions.eq("roleId", roleid),
                Restrictions.eq("adElementId", elementid),
                Restrictions.eq("objectId", objectid),
                Restrictions.in("adElementId", ids)));
    }

    @Override
    public List<permissions> getAllForAdElement(Guid id) {
        return fillInPermissionDetails(findByCriteria(Restrictions.eq("adElementId", id)));
    }

    @Override
    public List<permissions> getAllForAdElement(Guid id, Guid userID, boolean isFiltered) {
        throw new NotImplementedException();
    }

    @Override
    public List<permissions> getAllForRole(Guid id) {
        return fillInPermissionDetails(findByCriteria(Restrictions.eq("roleId", id)));
    }

    @Override
    public List<permissions> getAllForRoleAndAdElement(Guid roleid, Guid elementid) {
        return fillInPermissionDetails(findByCriteria(Restrictions.eq("roleId", roleid),
                Restrictions.eq("adElementId", elementid)));
    }

    @Override
    public List<permissions> getAllForRoleAndObject(Guid roleid, Guid objectid) {
        return fillInPermissionDetails(findByCriteria(Restrictions.eq("roleId", roleid),
                Restrictions.eq("objectId", objectid)));
    }

    @Override
    public List<permissions> getAllForEntity(Guid id) {
        return fillInPermissionDetails(findByCriteria(Restrictions.eq("objectId", id)));
    }

    @Override
    public List<permissions> getAllForEntity(Guid id, Guid userID, boolean isFiltered) {
        throw new NotImplementedException();
    }

    @Override
    public List<permissions> getTreeForEntity(Guid id, VdcObjectType type) {
        List<NGuid> ids = new ArrayList<NGuid>();

        // all use the system id
        ids.add(Guid.SYSTEM);
        ids.add(id);

        switch (type) {
        case VM:
            getVmParentIds(id, ids);
            break;
        case VDS:
            getVdsParentIds(id, ids);
            break;
        case VmTemplate:
            getVmTemplateParentIds(id, ids);
            break;
        case VmPool:
            getVmPoolParentIds(id, ids);
            break;
        case VdsGroups:
            getVdsGroupParentIds(id, ids);
            break;
        case System:
        case StoragePool:
        case Storage:
        case User:
        case Role:
            ids.add(id);
            break;
        default:
            // no IDs are returned, so exit with an empty result set
            ids.clear();
            return new ArrayList<permissions>();
        }

        return fillInPermissionDetails(findByCriteria(Restrictions.in("objectId", ids)));
    }

    @Override
    public List<permissions> getTreeForEntity(Guid id, VdcObjectType type, Guid userID, boolean isFiltered) {
        throw new NotImplementedException();
    }

    private void getVmParentIds(Guid id, List<NGuid> ids) {
        Query query = getSession().createQuery("from VmStatic where id = :id");
        query.setParameter("id", id);

        VmStatic vmStatic = (VmStatic) query.uniqueResult();

        if (vmStatic != null) {
            addVdsGroupId(vmStatic.getvds_group_id(), ids);
        }
    }

    private void getVdsParentIds(Guid id, List<NGuid> ids) {
        Query query = getSession().createQuery("from VdsStatic where id = :id");
        query.setParameter("id", id);

        VdsStatic vdsStatic = (VdsStatic) query.uniqueResult();

        if (vdsStatic != null) {
            addVdsGroupId(vdsStatic.getvds_group_id(), ids);
        }
    }

    @SuppressWarnings("unused")
    private void getVmTemplateParentIds(Guid id, List<NGuid> ids) {
        // Not implemented yet
    }

    private void getVmPoolParentIds(Guid id, List<NGuid> ids) {
        Query query = getSession().createQuery("from vm_pools where id = :id");
        query.setParameter("id", id);

        vm_pools vmPool = (vm_pools) query.uniqueResult();

        if (vmPool != null) {
            addVdsGroupId(vmPool.getvds_group_id(), ids);
        }
    }

    private void getVdsGroupParentIds(Guid id, List<NGuid> ids) {
        addVdsGroupId(id, ids);
    }

    private void addVdsGroupId(Guid id, List<NGuid> ids) {
        ids.add(id);

        Query query = getSession().createQuery("from VDSGroup where id = :id");

        query.setParameter("id", id);

        VDSGroup vdsGroup = (VDSGroup) query.uniqueResult();

        if (vdsGroup != null) {
            ids.add(vdsGroup.getStoragePoolId());
        }
    }

    @Override
    public void removeForEntity(Guid id) {
        Query query = getSession().createQuery("delete from permissions perms where perms.objectId = :id");

        query.setParameter("id", id);

        getSession().beginTransaction();
        query.executeUpdate();
        getSession().getTransaction().commit();
    }

    private List<permissions> fillInPermissionDetails(List<permissions> found) {
        for (permissions permission : found) {
            fillInPermissionDetails(permission);
        }

        return found;
    }

    /**
     * Retrieves the extra information previously retrieved by storage procedures.
     *
     * @param permission
     *            the instance
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    private permissions fillInPermissionDetails(permissions permission) {
        if (permission != null) {
            // get the object name
            if (permission.getad_element_id().equals(Guid.EVERYONE)) {
                permission.setObjectName("Everyone");
            } else {
                Query query = getSession().createQuery("from DbUser where id = :id");

                query.setParameter("id", permission.getad_element_id());

                DbUser user = (DbUser) query.uniqueResult();

                if (user != null)
                    permission.setOwnerName(user.getCoalescedName());
            }

            // get the entity name
            Query query = null;
            String entityName = null;

            switch (permission.getObjectType()) {
            case System:
                entityName = "System";
                break;
            case VM:
                query = getSession().createQuery("select name from VmStatic where id = :id");
                break;
            case VDS:
                query = getSession().createQuery("select name from VdsStatic where id = :id");
                break;
            case VmTemplate:
                query = getSession().createQuery("select name from VmTemplate where id = :id");
                break;
            case VmPool:
                query = getSession().createQuery("select name from vm_pools where id = :id");
                break;
            case VdsGroups:
                query = getSession().createQuery("select name from VDSGroup where id = :id");
                break;
            case Storage:
                query = getSession().createQuery("select storageName from storage_domain_static where id = :id");
                break;
            case StoragePool:
                query = getSession().createQuery("select name from storage_pool where id = :id");
                break;
            }

            if (query != null) {
                query.setParameter("id", permission.getObjectId());
                entityName = (String) query.uniqueResult();
            }

            permission.setObjectName(entityName);

            // get the role details
            query = getSession().createQuery("from roles where id = :id");
            query.setParameter("id", permission.getrole_id().toString());

            Role role = (Role) query.uniqueResult();

            if (role != null) {
                permission.setRoleName(role.getname());
                permission.setRoleType(role.getType());
            }
        }

        return permission;
    }

    @Override
    public List<permissions> getAllDirectPermissionsForAdElement(Guid id) {
        throw new NotImplementedException();
    }

    @Override
    public List<permissions> getConsumedPermissionsForQuotaId(Guid quotaId) {
        throw new NotImplementedException("This method is not implemented for Hibernate yet");
    }
}

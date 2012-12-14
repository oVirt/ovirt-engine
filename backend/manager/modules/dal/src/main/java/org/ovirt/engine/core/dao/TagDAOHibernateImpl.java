package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.common.businessentities.tags_user_map;
import org.ovirt.engine.core.common.businessentities.tags_vds_map;
import org.ovirt.engine.core.common.businessentities.TagsVdsMapId;
import org.ovirt.engine.core.common.businessentities.tags_vm_map;
import org.ovirt.engine.core.common.businessentities.tags_vm_pool_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.tags.TagUserGroupMapDAO;
import org.ovirt.engine.core.dao.tags.TagUserMapDAO;
import org.ovirt.engine.core.dao.tags.TagVdsMapDAO;
import org.ovirt.engine.core.dao.tags.TagVmMapDAO;

/**
 * <code>TagDAOHibernateImpl</code> provides an implementation of {@link TagDAO} based on Hibernate.
 *
 */
public class TagDAOHibernateImpl extends BaseDAOHibernateImpl<tags, Guid> implements TagDAO {
    private TagUserGroupMapDAO tagUserGroupMapDAO = new TagUserGroupMapDAO();
    private TagUserMapDAO tagUserMapDAO = new TagUserMapDAO();
    private TagVdsMapDAO tagVdsMapDAO = new TagVdsMapDAO();
    private TagVmMapDAO tagVmMapDAO = new TagVmMapDAO();

    public TagDAOHibernateImpl() {
        super(tags.class);
    }

    @Override
    public void setSession(Session session) {
        super.setSession(session);

        tagUserGroupMapDAO.setSession(session);
        tagUserMapDAO.setSession(session);
        tagVdsMapDAO.setSession(session);
        tagVmMapDAO.setSession(session);
    }

    @Override
    public List<tags> getAllForParent(Guid id) {
        return findByCriteria(Restrictions.eq("parent", id));
    }

    @Override
    public List<tags> getAllForUserGroups(String ids) {
        // TODO need to refactor the caller to send in Guids
        List<Guid> filter = convertIdsToGuids(ids);

        return findByCriteria(Restrictions.in("userGroup.id", filter.toArray()));
    }

    @Override
    public List<tags> getAllUserGroupTagsWithIds(String ids) {
        List<Guid> filter = convertIdsToGuids(ids);

        return findByCriteria(Restrictions.in("id", filter),
                Restrictions.isNotNull("userGroup"));
    }

    @Override
    public List<tags> getAllForVds(String ids) {
        List<Guid> filter = convertIdsToGuids(ids);

        return findByCriteria(Restrictions.in("vds.id", filter));
    }

    @Override
    public List<tags> getAllForVdsWithIds(String ids) {
        List<Guid> filter = convertIdsToGuids(ids);

        return findByCriteria(Restrictions.in("id", filter), Restrictions.isNotNull("vds"));
    }

    @Override
    public List<tags> getAllForVm(String ids) {
        List<Guid> filter = convertIdsToGuids(ids);

        return findByCriteria(Restrictions.in("vm.id", filter));
    }

    @Override
    public List<tags> getAllVmTagsWithIds(String ids) {
        List<Guid> filter = convertIdsToGuids(ids);

        return findByCriteria(Restrictions.in("id", filter), Restrictions.isNotNull("vm"));
    }

    @Override
    public List<tags> getAllForVmPools(String ids) {
        List<Guid> filter = convertIdsToGuids(ids);

        return findByCriteria(Restrictions.in("vmPool.id", filter));
    }

    @Override
    public List<tags> getAllForUsers(String ids) {
        List<Guid> filter = convertIdsToGuids(ids);

        return findByCriteria(Restrictions.in("user.id", filter));
    }

    @Override
    public List<tags> getAllForUsersWithIds(String ids) {
        List<Guid> filter = convertIdsToGuids(ids);

        return findByCriteria(Restrictions.in("id", filter),
                Restrictions.isNotNull("user"));
    }

    @Override
    public TagsUserGroupMap getTagUserGroupByGroupIdAndByTagId(Guid tagId, Guid groupId) {
        return tagUserGroupMapDAO.findOneByCriteria(Restrictions.eq("tagId", tagId),
                Restrictions.eq("groupId", groupId));
    }

    @Override
    public void detachUserGroupFromTag(Guid tagId, Guid groupId) {
        tagUserGroupMapDAO.remove(tagId, groupId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TagsUserGroupMap> getTagUserGroupMapsForTagName(String tagName) {
        Session session = getSession();
        Query query = session.getNamedQuery("get_all_tags_user_group_maps_for_tag_name");

        query.setParameter("tag_name", tagName);

        return query.list();
    }

    @Override
    public tags_user_map getTagUserByTagIdAndByuserId(Guid tagId, Guid userId) {
        return tagUserMapDAO.findOneByCriteria(Restrictions.eq("tagId", tagId), Restrictions.eq("userId", userId));
    }

    @Override
    public void attachUserGroupToTag(TagsUserGroupMap map) {
        tagUserGroupMapDAO.save(map);
    }

    @Override
    public void attachUserToTag(tags_user_map tagUserMap) {
        tagUserMapDAO.save(tagUserMap);
    }

    @Override
    public void detachUserFromTag(Guid tagId, Guid userId) {
        tagUserMapDAO.remove(tagId, userId);
    }

    @Override
    public List<tags_user_map> getTagUserMapByTagName(String tagName) {
        return tagUserMapDAO.getTaguserMapByTagName(tagName);
    }

    @Override
    public tags_vds_map getTagVdsByTagIdAndByVdsId(Guid tagId, Guid vdsId) {
        return tagVdsMapDAO.findOneByCriteria(Restrictions.eq("id.tagId", tagId), Restrictions.eq("id.vdsId", vdsId));
    }

    @Override
    public void attachVdsToTag(tags_vds_map tagVdsMap) {
        tagVdsMapDAO.save(tagVdsMap);
    }

    @Override
    public void detachVdsFromTag(Guid tagId, Guid vdsId) {
        tagVdsMapDAO.remove(new TagsVdsMapId(tagId, vdsId));
    }

    @Override
    public List<tags_vds_map> getTagVdsMapByTagName(String tagName) {
        tags tag = getByName(tagName);

        if (tag != null) {
            return tagVdsMapDAO.findByCriteria(Restrictions.eq("id.tagId", tag.gettag_id()));
        } else {
            return new ArrayList<tags_vds_map>();
        }
    }

    @Override
    public tags_vm_map getTagVmByTagIdAndByVmId(Guid tagId, Guid vmId) {
        return tagVmMapDAO.findOneByCriteria(Restrictions.eq("id.tagId", tagId), Restrictions.eq("id.vmId", vmId));
    }

    @Override
    public void attachVmToTag(tags_vm_map tagVmMap) {
        tagVmMapDAO.save(tagVmMap);
    }

    @Override
    public void updateDefaultDisplayForVmTag(tags_vm_map tagsVmMap) {
        tagVmMapDAO.save(tagsVmMap);
    }

    @Override
    public void detachVmFromTag(Guid tagId, Guid vmId) {
        tagVmMapDAO.remove(tagId, vmId);
    }

    @Override
    public List<tags_vm_map> getTagVmMapByTagName(String tagName) {
        tags tag = getByName(tagName);

        if (tag != null) {
            return tagVmMapDAO.findByCriteria(Restrictions.eq("id.tagId", tag.gettag_id()));
        } else {
            return new ArrayList<tags_vm_map>();
        }
    }

    @Override
    public List<tags_vm_map> getTagVmMapByVmIdAndDefaultTag(Guid vmid) {
        List<tags> defaultTags = findByCriteria(Restrictions.eq("type", TagsType.AdElementTag));
        List<Guid> ids = new ArrayList<Guid>();

        for (tags tag : defaultTags) {
            ids.add(tag.gettag_id());
        }

        return tagVmMapDAO.findByCriteria(Restrictions.in("id.tagId", ids));
    }

    @Override
    public List<tags_vm_map> getTimeLeasedUserVmsByAdGroupAndVmPoolId(Guid adGroupId, Guid vmPoolId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<tags_vm_pool_map> getVmPoolTagsByVmPoolIdAndAdElementId(NGuid vmPoolId, Guid adElementId) {
        // TODO Auto-generated method stub
        return null;
    }
}

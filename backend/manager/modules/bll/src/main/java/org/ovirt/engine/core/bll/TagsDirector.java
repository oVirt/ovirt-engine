package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.TagDAO;
import org.ovirt.engine.core.utils.collections.CopyOnAccessMap;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

enum TagReturnValueIndicator {
    ID,
    NAME
}
/**
 * This class responsible to In memory Tags handling. On Vdc starting in memory tags tree initialized. All Tags changing
 * operations go throw this class
 */
public class TagsDirector {


    private static Log log = LogFactory.getLog(TagsDirector.class);

    protected static final Guid ROOT_TAG_ID = Guid.Empty;

    /**
     * In memory nodes cache for quicker access to each node by ID: O(1) instead O(lnN) of tree
     */
    private final java.util.Map<Guid, tags> tagsMapByID =
            new CopyOnAccessMap<Guid, tags>(new java.util.HashMap<Guid, tags>());
    /**
     * In memory nodes cache for quicker access to each node by name
     */
    private final java.util.Map<String, tags> tagsMapByName =
            new CopyOnAccessMap<String, tags>(new java.util.HashMap<String, tags>());

    private static TagsDirector instance = new TagsDirector();

    private TagsDirector() {
    }

    /**
     * In memory tree initialized during initialization
     */

    protected void init() {
        log.info("TagsDirector initialization");
        tagsMapByID.clear();
        tagsMapByName.clear();
        tags root = new tags("root", null, true, ROOT_TAG_ID, "root");
        AddTagToHash(root);
        AddChildren(root);
    }


    private void AddTagToHash(tags tag) {
        tagsMapByID.put(tag.gettag_id(), tag);
        tagsMapByName.put(tag.gettag_name(), tag);
        if (tag.getparent_id() != null) {
            // If the tag has a parent, the parent should have in its children the added tag instead
            // of the old version of the tag , if exists
            tags parentTag = tagsMapByID.get(tag.getparent_id());
            if (parentTag == null) {
                log.error(String.format("Could not obtain tag for guid %1$s", tag.getparent_id()));
                return;
            }
            List<tags> parentChildren = parentTag.getChildren();
            replaceTagInChildren(tag, parentChildren);
            AddTagToHash(parentTag); // replace the parent tag after the modification
        }
    }

    private void replaceTagInChildren(tags tag, List<tags> parentChildren) {
        for (int counter = 0; counter < parentChildren.size(); counter++) {
            if (parentChildren.get(counter).gettag_id().equals(tag.gettag_id())) {
                parentChildren.set(counter, tag);
                break;
            }
        }
    }

    private void RemoveTagFromHash(tags tag) {
        tagsMapByID.remove(tag.gettag_id());
        tagsMapByName.remove(tag.gettag_name());
    }

    /**
     * Recurcive tree initialization call
     *
     * @param tag
     */

    private void AddChildren(tags tag) {
        log.infoFormat("Tag {0} added to tree", tag.gettag_name());
        List<tags> children = getTagDAO().getAllForParent(tag.gettag_id());
        for (tags child : children) {
            AddChildren(child);
            log.infoFormat("Tag {0} added as child to parent {1}", child.gettag_name(), tag.gettag_name());
            tag.getChildren().add(child);
            AddTagToHash(tag);
            AddTagToHash(child);
        }
    }

    protected TagDAO getTagDAO() {
        return DbFacade.getInstance().getTagDAO();
    }

    private void RemoveTagAndChildren(tags tag) {
        for (tags child : tag.getChildren()) {
            RemoveTagAndChildren(child);
        }
        RemoveTagFromHash(tag);
    }

    public static TagsDirector getInstance() {
        return instance;
    }

    public void AddTag(tags tag) {
        if (tagsMapByID.containsKey(tag.getparent_id())) {
            tags parent = tagsMapByID.get(tag.getparent_id());
            parent.getChildren().add(tag);
            AddTagToHash(tag);
            AddTagToHash(parent);
        } else {
            log.errorFormat("Trying to add tag {0}, parent doesn't exist in Data Structure - {1}", tag.gettag_name(),
                    tag.getparent_id());
        }
    }

    /**
     * Remove tag operation. For tag with children all tag's children will be removed as well
     *
     * @param tagId
     *            tag to remove
     */
    public void RemoveTag(Guid tagId) {
        if (tagsMapByID.containsKey(tagId)) {
            tags tag = tagsMapByID.get(tagId);
            RemoveTagAndChildren(tag);
            tags parent = tagsMapByID.get(tag.getparent_id());
            parent.getChildren().remove(tag);
            AddTagToHash(parent);
        } else {
            log.warnFormat("Trying to remove tag, not exists in Data Structure - {0}", tagId);
        }
    }

    /**
     * Update tag. We assume that the id doesn't change.
     *
     * @param tag
     */
    public void UpdateTag(tags tag) {
        if (tagsMapByID.containsKey(tag.gettag_id())) {
            tags tagFromCache = tagsMapByID.get(tag.gettag_id());
            String oldName = tagFromCache.gettag_name();
            // check if tag name has changed. If it has - modify name dictionary
            // accordingly:
            if (!tag.gettag_name().equals(oldName)) {
                tagsMapByName.remove(oldName);
            }

            // Copy the children of the cached tag to keep the object hierarchy consistent.
            tag.setChildren(tagFromCache.getChildren());

            AddTagToHash(tag);
        } else {
            log.warnFormat("Trying to update tag, not exists in Data Structure - {0}", tag.gettag_name());
        }
    }

    public void MoveTag(Guid tagId, Guid newParent) {
        if (tagsMapByID.containsKey(tagId)) {
            tags tag = tagsMapByID.get(tagId);
            if (tagsMapByID.containsKey(newParent)) {
                if (tagsMapByID.containsKey(tag.getparent_id())) {
                    tags parentTag = tagsMapByID.get(tag.getparent_id());
                    parentTag.getChildren().remove(tag);
                    AddTagToHash(parentTag);
                } else {
                    log.warnFormat("Trying to move tag from parent that doesn't exist in Data Structure - {0}",
                            tag.getparent_id());
                }
                tags newParentTag = tagsMapByID.get(newParent);
                newParentTag.getChildren().add(tag);
                tag.setparent_id(newParent);
                AddTagToHash(newParentTag); // Parent got changed, modify it.
                updateTagInBackend(tag);
            } else {
                log.errorFormat("Trying to move tag, to parent not exists in Data Structure - {0}", newParent);
            }
        } else {
            log.errorFormat("Trying to move tag, not exists in Data Structure - {0}", tagId);
        }
    }

    protected void updateTagInBackend(tags tag) {
        Backend.getInstance().runInternalAction(VdcActionType.UpdateTag, new TagsOperationParameters(tag));
    }

    private String GetTagIdAndParentsIds(tags tag) {
        StringBuilder builder = new StringBuilder();
        builder.append(tag.gettag_id());
        Guid tempTagId = new Guid(tag.getparent_id().toString());
        while (!tempTagId.equals(Guid.Empty)) {
            builder.append(String.format(",%1$s", tempTagId));
            tag = GetTagById(tempTagId);
            tempTagId = new Guid(tag.getparent_id().toString());
        }

        return builder.toString();
    }

    /**
     * This function will return the tag's ID and its parents IDs.
     *
     * @param tagId
     *            the tag ID.
     * @return a comma seperated list of IDs.
     */
    public String GetTagIdAndParentsIds(Guid tagId) {
        tags tag = GetTagById(tagId);
        return GetTagIdAndParentsIds(tag);
    }

    /**
     * This function will return the tag's ID and its parents IDs.
     *
     * @param tagId
     *            the tag ID.
     * @return a comma seperated list of IDs.
     */
    public String GetTagIdAndParentsIds(String tagName) {
        tags tag = GetTagByName(tagName);
        return GetTagIdAndParentsIds(tag);
    }

    /**
     * This function will return the tag's ID and its children IDs. Its used to determine if a tag is assigned to an
     * entity. Tag is determined as assigned to an entity if the entity is assigned to the tag or to one of its
     * children.
     *
     * @param tagId
     *            the ID of the 'root' tag.
     * @return a comma separated list of IDs.
     */
    public String GetTagIdAndChildrenIds(Guid tagId) {
        tags tag = GetTagById(tagId);
        if (tag == null) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = tag.GetTagIdAndChildrenIds();
        return sb.toString();
    }

    public String GetTagNameAndChildrenNames(Guid tagId) {
        tags tag = GetTagById(tagId);
        StringBuilder sb = tag.GetTagNameAndChildrenNames();
        return sb.toString();
    }

    public java.util.HashSet<Guid> GetTagIdAndChildrenIdsAsSet(Guid tagId) {
        tags tag = GetTagById(tagId);
        java.util.HashSet<Guid> set = new java.util.HashSet<Guid>();
        tag.GetTagIdAndChildrenIdsAsList(set);
        return set;
    }

    /**
     * This function will return the tag's ID and its children IDs. Its used to determine if a tag is assigned to an
     * entity. Tag is determined as assigned to an entity if the entity is assigned to the tag or to one of its
     * children.
     *
     * @param tagName
     *            the name of the 'root' tag.
     * @return a comma separated list of IDs.
     */
    public String GetTagIdAndChildrenIds(String tagName) {
        tags tag = GetTagByName(tagName);
        StringBuilder sb = tag.GetTagIdAndChildrenIds();
        return sb.toString();
    }

    /**
     * This function will return the tags IDs of all tags that their names match the specified regular expression and
     * ALL their children (regardless if the children match the reg-exp or not). Its used to determine if a tag is
     * assigned to an entity. Tag is determined as assigned to an entity if the entity is assigned to the tag or to one
     * of its children.
     *
     * @param tagName
     *            the name of the 'root' tag.
     * @return a comma separated list of IDs.
     */
    public String GetTagIdsAndChildrenIdsByRegExp(String tagNameRegExp) {
        // add RegEx chars or beginning of string ('^') and end of string ('$'):
        tagNameRegExp = String.format("^%1$s$", tagNameRegExp);
        // convert to the regular expression format:
        tagNameRegExp = tagNameRegExp.replace("*", ".*");
        StringBuilder sb = new StringBuilder();
        RecursiveGetTagsAndChildrenByRegExp(tagNameRegExp, sb, GetRootTag(), TagReturnValueIndicator.ID);
        return sb.toString();
    }

    public String GetTagNamesAndChildrenNamesByRegExp(String tagNameRegExp) {
        // add RegEx chars or beginning of string ('^') and end of string ('$'):
        tagNameRegExp = String.format("^%1$s$", tagNameRegExp);
        // convert to the regular expression format:
        tagNameRegExp = tagNameRegExp.replace("*", ".*");
        StringBuilder sb = new StringBuilder();
        RecursiveGetTagsAndChildrenByRegExp(tagNameRegExp, sb, GetRootTag(), TagReturnValueIndicator.NAME);
        return sb.toString();
    }

    private void RecursiveGetTagsAndChildrenByRegExp(String tagNameRegExp, StringBuilder sb, tags tag, TagReturnValueIndicator indicator ) {
        if ((tag.getChildren() != null) && (tag.getChildren().size() > 0)) {
            for (tags child : tag.getChildren()) {
                if (Regex.IsMatch(child.gettag_name(), tagNameRegExp))
                // the tag matches the regular expression -> add it and all its
                // children
                // (we prevent searching a regular expression match on them -
                // unnecessary).
                {
                    if (sb.length() == 0) {
                        if (indicator == TagReturnValueIndicator.ID)
                            sb.append(child.GetTagIdAndChildrenIds());
                        else
                            sb.append(child.GetTagNameAndChildrenNames());
                    } else {
                        if (indicator == TagReturnValueIndicator.ID)
                            sb.append(String.format(",%1$s", child.GetTagIdAndChildrenIds()));
                        else
                            sb.append(String.format(",%1$s", child.GetTagNameAndChildrenNames()));
                    }
                } else {
                    RecursiveGetTagsAndChildrenByRegExp(tagNameRegExp, sb, child, indicator);
                }
            }
        }
    }

    /**
     * Get tag from in memory data structure (by ID). This tag will be with all children tree initialized as opposite to
     * tag from db.
     *
     * @param tagId
     * @return
     */
    public tags GetTagById(Guid tagId) {
        if (tagsMapByID.containsKey(tagId)) {
            return tagsMapByID.get(tagId);
        } else {
            return null;
        }
    }

    /**
     * Get tag from in memory data structure (by name).
     *
     * @param tagName
     * @return
     */
    public tags GetTagByName(String tagName) {
        if (tagsMapByName.containsKey(tagName)) {
            return tagsMapByName.get(tagName);
        } else {
            return null;
        }
    }

    /**
     * Gets a list of all the tags in the system.
     *
     * @return a tags list.
     */
    public java.util.ArrayList<tags> GetAllTags() {
        java.util.ArrayList<tags> ret = new java.util.ArrayList<tags>(tagsMapByID.values());
        // remove the root - it is not a real tag:
        ret.remove(GetRootTag());
        return ret;
    }

    /**
     * Returns the root tag in the system.
     *
     * @return the root tag.
     */
    public tags GetRootTag() {
        return tagsMapByID.get(ROOT_TAG_ID);
    }

    public boolean IsTagDescestorOfTag(Guid sourceTagId, Guid potentialDescestorId) {
        if (sourceTagId.equals(potentialDescestorId)) {
            return true;
        }
        tags tag = GetTagById(sourceTagId);
        if (tag != null && tag.getChildren() != null) {
            for (tags childTag : tag.getChildren()) {
                if (IsTagDescestorOfTag(childTag.gettag_id(), potentialDescestorId)) {
                    return true;
                }
            }
        }
        return false;
    }
}

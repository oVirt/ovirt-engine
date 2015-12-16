package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.TagDao;
import org.ovirt.engine.core.utils.collections.CopyOnAccessMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class responsible to In memory Tags handling. On Vdc starting in memory tags tree initialized. All Tags changing
 * operations go throw this class
 */
public class TagsDirector {

    /**
     * This pattern is used to replace '\\' in the expression that may be added by handling a '_' character with an
     * empty string. Since we use both String and RegExp , each backslash char is represented by four backslash
     * characters, so for marching two backslashes we will need eight.
     */
    private static final Pattern BACKSLASH_REMOVER = Pattern.compile("\\\\\\\\");

    private enum TagReturnValueIndicator {
        ID,
        NAME
    }

    private static final Logger log = LoggerFactory.getLogger(TagsDirector.class);

    protected static final Guid ROOT_TAG_ID = Guid.Empty;

    /**
     * In memory nodes cache for quicker access to each node by ID: O(1) instead O(lnN) of tree
     */
    private final Map<Guid, Tags> tagsMapByID = new CopyOnAccessMap<>(new HashMap<>());
    /**
     * In memory nodes cache for quicker access to each node by name
     */
    private final Map<String, Tags> tagsMapByName = new CopyOnAccessMap<>(new HashMap<>());

    private static TagsDirector instance = new TagsDirector();

    private TagsDirector() {
    }

    /**
     * In memory tree initialized during initialization
     */

    protected void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        tagsMapByID.clear();
        tagsMapByName.clear();
        Tags root = new Tags("root", null, true, ROOT_TAG_ID, "root");
        addTagToHash(root);
        addChildren(root);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }


    private void addTagToHash(Tags tag) {
        tagsMapByID.put(tag.getTagId(), tag);
        tagsMapByName.put(tag.getTagName(), tag);
        if (tag.getParentId() != null) {
            // If the tag has a parent, the parent should have in its children the added tag instead
            // of the old version of the tag , if exists
            Tags parentTag = tagsMapByID.get(tag.getParentId());
            if (parentTag == null) {
                log.error("Could not obtain tag for guid '{}'", tag.getParentId());
                return;
            }
            List<Tags> parentChildren = parentTag.getChildren();
            replaceTagInChildren(tag, parentChildren);
            addTagToHash(parentTag); // replace the parent tag after the modification
        }
    }

    private static void replaceTagInChildren(Tags tag, List<Tags> parentChildren) {
        for (int counter = 0; counter < parentChildren.size(); counter++) {
            if (parentChildren.get(counter).getTagId().equals(tag.getTagId())) {
                parentChildren.set(counter, tag);
                break;
            }
        }
    }

    private void removeTagFromHash(Tags tag) {
        tagsMapByID.remove(tag.getTagId());
        tagsMapByName.remove(tag.getTagName());
    }

    /**
     * Recursive tree initialization call
     */
    private void addChildren(Tags tag) {
        log.info("Tag '{}' added to tree", tag.getTagName());
        List<Tags> children = getTagDao().getAllForParent(tag.getTagId());
        for (Tags child : children) {
            addChildren(child);
            log.info("Tag '{}' added as child to parent '{}'", child.getTagName(), tag.getTagName());
            tag.getChildren().add(child);
            addTagToHash(tag);
            addTagToHash(child);
        }
    }

    protected TagDao getTagDao() {
        return DbFacade.getInstance().getTagDao();
    }

    private void removeTagAndChildren(Tags tag) {
        for (Tags child : tag.getChildren()) {
            removeTagAndChildren(child);
        }
        removeTagFromHash(tag);
    }

    public static TagsDirector getInstance() {
        return instance;
    }

    public void addTag(Tags tag) {
        if (tagsMapByID.containsKey(tag.getParentId())) {
            Tags parent = tagsMapByID.get(tag.getParentId());
            parent.getChildren().add(tag);
            addTagToHash(tag);
            addTagToHash(parent);
        } else {
            log.error("Trying to add tag '{}', parent doesn't exist in Data Structure - '{}'", tag.getTagName(),
                    tag.getParentId());
        }
    }

    /**
     * Remove tag operation. For tag with children all tag's children will be removed as well
     *
     * @param tagId
     *            tag to remove
     */
    public void removeTag(Guid tagId) {
        if (tagsMapByID.containsKey(tagId)) {
            Tags tag = tagsMapByID.get(tagId);
            removeTagAndChildren(tag);
            Tags parent = tagsMapByID.get(tag.getParentId());
            parent.getChildren().remove(tag);
            addTagToHash(parent);
        } else {
            log.warn("Trying to remove tag, not exists in Data Structure - '{}'", tagId);
        }
    }

    /**
     * Update tag. We assume that the id doesn't change.
     */
    public void updateTag(Tags tag) {
        if (tagsMapByID.containsKey(tag.getTagId())) {
            Tags tagFromCache = tagsMapByID.get(tag.getTagId());
            String oldName = tagFromCache.getTagName();
            // check if tag name has changed. If it has - modify name dictionary
            // accordingly:
            if (!tag.getTagName().equals(oldName)) {
                tagsMapByName.remove(oldName);
            }

            // Copy the children of the cached tag to keep the object hierarchy consistent.
            tag.setChildren(tagFromCache.getChildren());

            addTagToHash(tag);
        } else {
            log.warn("Trying to update tag, not exists in Data Structure - '{}'", tag.getTagName());
        }
    }

    public void moveTag(Guid tagId, Guid newParent) {
        if (tagsMapByID.containsKey(tagId)) {
            Tags tag = tagsMapByID.get(tagId);
            if (tagsMapByID.containsKey(newParent)) {
                if (tagsMapByID.containsKey(tag.getParentId())) {
                    Tags parentTag = tagsMapByID.get(tag.getParentId());
                    parentTag.getChildren().remove(tag);
                    addTagToHash(parentTag);
                } else {
                    log.warn("Trying to move tag from parent that doesn't exist in Data Structure - '{}'",
                            tag.getParentId());
                }
                Tags newParentTag = tagsMapByID.get(newParent);
                newParentTag.getChildren().add(tag);
                tag.setParentId(newParent);
                addTagToHash(newParentTag); // Parent got changed, modify it.
                updateTagInBackend(tag);
            } else {
                log.error("Trying to move tag, to parent not exists in Data Structure - '{}'", newParent);
            }
        } else {
            log.error("Trying to move tag, not exists in Data Structure - '{}'", tagId);
        }
    }

    protected void updateTagInBackend(Tags tag) {
        Backend.getInstance().runInternalAction(VdcActionType.UpdateTag, new TagsOperationParameters(tag));
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
    public String getTagIdAndChildrenIds(Guid tagId) {
        Tags tag = getTagById(tagId);
        if (tag == null) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = tag.getTagIdAndChildrenIds();
        return sb.toString();
    }

    public String getTagNameAndChildrenNames(Guid tagId) {
        Tags tag = getTagById(tagId);
        StringBuilder sb = tag.getTagNameAndChildrenNames();
        return sb.toString();
    }

    public HashSet<Guid> getTagIdAndChildrenIdsAsSet(Guid tagId) {
        Tags tag = getTagById(tagId);
        HashSet<Guid> set = new HashSet<>();
        tag.getTagIdAndChildrenIdsAsList(set);
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
    public String getTagIdAndChildrenIds(String tagName) {
        Tags tag = getTagByName(tagName);
        StringBuilder sb = tag.getTagIdAndChildrenIds();
        return sb.toString();
    }

    public String getTagNamesAndChildrenNamesByRegExp(String tagNameRegExp) {
        // add RegEx chars or beginning of string ('^') and end of string ('$'):
        tagNameRegExp = String.format("^%1$s$", tagNameRegExp);
        // convert to the regular expression format:
        tagNameRegExp = tagNameRegExp.replace("*", ".*");
        StringBuilder sb = new StringBuilder();
        recursiveGetTagsAndChildrenByRegExp(tagNameRegExp, sb, getRootTag(), TagReturnValueIndicator.NAME);
        return sb.toString();
    }

    private static void recursiveGetTagsAndChildrenByRegExp(String tagNameRegExp, StringBuilder sb, Tags tag, TagReturnValueIndicator indicator) {
        if ((tag.getChildren() != null) && !tag.getChildren().isEmpty()) {
            tagNameRegExp = BACKSLASH_REMOVER.matcher(tagNameRegExp).replaceAll("");
            for (Tags child : tag.getChildren()) {
                Pattern tagNamePattern = Pattern.compile(tagNameRegExp);
                if (tagNamePattern.matcher(child.getTagName()).find()) {
                // the tag matches the regular expression -> add it and all its
                // children
                // (we prevent searching a regular expression match on them -
                // unnecessary).
                    if (sb.length() == 0) {
                        if (indicator == TagReturnValueIndicator.ID) {
                            sb.append(child.getTagIdAndChildrenIds());
                        } else {
                            sb.append(child.getTagNameAndChildrenNames());
                        }
                    } else {
                        if (indicator == TagReturnValueIndicator.ID) {
                            sb.append(String.format(",%1$s", child.getTagIdAndChildrenIds()));
                        } else {
                            sb.append(String.format(",%1$s", child.getTagNameAndChildrenNames()));
                        }
                    }
                } else {
                    recursiveGetTagsAndChildrenByRegExp(tagNameRegExp, sb, child, indicator);
                }
            }
        }
    }

    /**
     * Get tag from in memory data structure (by ID). This tag will be with all children tree initialized as opposite to
     * tag from db.
     */
    public Tags getTagById(Guid tagId) {
        if (tagsMapByID.containsKey(tagId)) {
            return tagsMapByID.get(tagId);
        } else {
            return null;
        }
    }

    /**
     * Get tag from in memory data structure (by name).
     */
    public Tags getTagByName(String tagName) {
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
    public ArrayList<Tags> getAllTags() {
        ArrayList<Tags> ret = new ArrayList<>(tagsMapByID.values());
        // remove the root - it is not a real tag:
        ret.remove(getRootTag());
        return ret;
    }

    /**
     * Returns the root tag in the system.
     *
     * @return the root tag.
     */
    public Tags getRootTag() {
        return tagsMapByID.get(ROOT_TAG_ID);
    }

    public boolean isTagDescestorOfTag(Guid sourceTagId, Guid potentialDescestorId) {
        if (sourceTagId.equals(potentialDescestorId)) {
            return true;
        }
        Tags tag = getTagById(sourceTagId);
        if (tag != null && tag.getChildren() != null) {
            for (Tags childTag : tag.getChildren()) {
                if (isTagDescestorOfTag(childTag.getTagId(), potentialDescestorId)) {
                    return true;
                }
            }
        }
        return false;
    }
}

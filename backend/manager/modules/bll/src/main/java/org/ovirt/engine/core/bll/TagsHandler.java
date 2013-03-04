package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.interfaces.ITagsHandler;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class TagsHandler implements ITagsHandler {

    public void MoveTag(Guid tagId, Guid newParent) {
        TagsDirector.getInstance().MoveTag(tagId, newParent);
    }

    @Override
    public tags GetTagByTagName(String tagName) {
        return TagsDirector.getInstance().GetTagByName(tagName);
    }

    @Override
    public String GetTagNamesAndChildrenNamesByRegExp(String tagNameRegExp) {
        return TagsDirector.getInstance().GetTagNamesAndChildrenNamesByRegExp(tagNameRegExp);
    }

    @Override
    public String GetTagIdAndChildrenIds(Guid tagId) {
        return TagsDirector.getInstance().GetTagIdAndChildrenIds(tagId);
    }

    @Override
    public String GetTagNameAndChildrenNames(Guid tagId) {
        return TagsDirector.getInstance().GetTagNameAndChildrenNames(tagId);
    }

    public static boolean IsUserAttachedToTag(Guid tagId, Guid userId) {
        return (DbFacade
                .getInstance()
                .getTagDao()
                .getAllForUsersWithIds(
                        TagsDirector.getInstance()
                                .GetTagIdAndChildrenIds(tagId)).size() != 0);
    }

    public static boolean IsUserGroupAttachedToTag(Guid tagId, Guid groupId) {
        return (DbFacade
                .getInstance()
                .getTagDao()
                .getAllForUsersWithIds(
                        TagsDirector.getInstance()
                                .GetTagIdAndChildrenIds(tagId)).size() != 0);
    }

    public static boolean IsVmAttachedToTag(Guid tagId, Guid vmId) {
        return (DbFacade
                .getInstance()
                .getTagDao()
                .getAllVmTagsWithIds(
                        TagsDirector.getInstance()
                                .GetTagIdAndChildrenIds(tagId)).size() != 0);
    }

    public static boolean IsVdsAttachedToTag(Guid tagId, int vdsId) {
        return (DbFacade
                .getInstance()
                .getTagDao()
                .getAllForVdsWithIds(
                        TagsDirector.getInstance()
                                .GetTagIdAndChildrenIds(tagId)).size() != 0);
    }
}

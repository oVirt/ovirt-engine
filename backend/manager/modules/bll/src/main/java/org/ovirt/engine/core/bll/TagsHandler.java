package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class TagsHandler implements ITagsHandler {

    public void MoveTag(Guid tagId, Guid newParent) {
        TagsDirector.getInstance().MoveTag(tagId, newParent);
    }

    @Override
    public tags GetTagByTagName(String tagName) {
        return TagsDirector.getInstance().GetTagByName(tagName);
    }

    @Override
    public String GetTagIdsAndChildrenIdsByRegExp(String tagNameRegExp) {
        return TagsDirector.getInstance().GetTagIdsAndChildrenIdsByRegExp(tagNameRegExp);
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
                .getTagDAO()
                .getAllForUsersWithIds(
                        TagsDirector.getInstance()
                                .GetTagIdAndChildrenIds(tagId)).size() != 0);
    }

    public static boolean IsUserGroupAttachedToTag(Guid tagId, Guid groupId) {
        return (DbFacade
                .getInstance()
                .getTagDAO()
                .getAllForUsersWithIds(
                        TagsDirector.getInstance()
                                .GetTagIdAndChildrenIds(tagId)).size() != 0);
    }

    public static boolean IsVmAttachedToTag(Guid tagId, Guid vmId) {
        return (DbFacade
                .getInstance()
                .getTagDAO()
                .getAllVmTagsWithIds(
                        TagsDirector.getInstance()
                                .GetTagIdAndChildrenIds(tagId)).size() != 0);
    }

    public static boolean IsVdsAttachedToTag(Guid tagId, int vdsId) {
        return (DbFacade
                .getInstance()
                .getTagDAO()
                .getAllForVdsWithIds(
                        TagsDirector.getInstance()
                                .GetTagIdAndChildrenIds(tagId)).size() != 0);
    }
}

package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.interfaces.ITagsHandler;
import org.ovirt.engine.core.compat.Guid;

public class TagsHandler implements ITagsHandler {

    @Override
    public Tags GetTagByTagName(String tagName) {
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
}

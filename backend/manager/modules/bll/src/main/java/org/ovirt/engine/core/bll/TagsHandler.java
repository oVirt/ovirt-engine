package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.interfaces.ITagsHandler;
import org.ovirt.engine.core.compat.Guid;

public class TagsHandler implements ITagsHandler {

    @Override
    public Tags getTagByTagName(String tagName) {
        return TagsDirector.getInstance().getTagByName(tagName);
    }

    @Override
    public String getTagNamesAndChildrenNamesByRegExp(String tagNameRegExp) {
        return TagsDirector.getInstance().getTagNamesAndChildrenNamesByRegExp(tagNameRegExp);
    }

    @Override
    public String getTagIdAndChildrenIds(Guid tagId) {
        return TagsDirector.getInstance().getTagIdAndChildrenIds(tagId);
    }

    @Override
    public String getTagNameAndChildrenNames(Guid tagId) {
        return TagsDirector.getInstance().getTagNameAndChildrenNames(tagId);
    }
}

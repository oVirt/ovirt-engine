package org.ovirt.engine.core.common.interfaces;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;

public interface ITagsHandler {
    // void MoveTag(int tagId, int newParent);
    Tags getTagByTagName(String tagName);

    String getTagIdAndChildrenIds(Guid tagId);

    String getTagNameAndChildrenNames(Guid tagId);

    String getTagNamesAndChildrenNamesByRegExp(String tagNameRegExp);
}

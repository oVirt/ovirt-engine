package org.ovirt.engine.core.common.interfaces;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;

public interface ITagsHandler {
    // void MoveTag(int tagId, int newParent);
    Tags GetTagByTagName(String tagName);

    String GetTagIdAndChildrenIds(Guid tagId);

    String GetTagNameAndChildrenNames(Guid tagId);

    String GetTagNamesAndChildrenNamesByRegExp(String tagNameRegExp);
}

package org.ovirt.engine.ui.uicommonweb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;

public interface TagAssigningModel<T> {

    default void postGetAttachedTags(TagListModel tagListModel) {
        if (getLastExecutedCommand() == getAssignTagsCommand()) {
            ArrayList<Tags> attachedTags =
                    Linq.distinct(getAllAttachedTags(), new TagsEqualityComparer());
            for (Tags tag : attachedTags) {
                int count = 0;
                for (Tags tag2 : getAllAttachedTags()) {
                    if (tag2.getTagId().equals(tag.getTagId())) {
                        count++;
                    }
                }
                getAttachedTagsToEntities().put(tag.getTagId(), count == getSelectedItems().size());
            }
            tagListModel.setAttachedTagsToEntities(getAttachedTagsToEntities());
        }
        else if ("OnAssignTags".equals(getLastExecutedCommand().getName())) { //$NON-NLS-1$
            postOnAssignTags(tagListModel.getAttachedTagsToEntities());
        }
    }

    UICommand getAssignTagsCommand();

    UICommand getLastExecutedCommand();

    List<Tags> getAllAttachedTags();

    Map<Guid, Boolean> getAttachedTagsToEntities();

    List<T> getSelectedItems();

    void postOnAssignTags(Map<Guid, Boolean> attachedTags);
}

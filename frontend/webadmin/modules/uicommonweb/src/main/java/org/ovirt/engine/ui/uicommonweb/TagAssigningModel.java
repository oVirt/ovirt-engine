package org.ovirt.engine.ui.uicommonweb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;

public interface TagAssigningModel<T> {

    default void postGetAttachedTags(TagListModel tagListModel) {
        if (getLastExecutedCommand() == getAssignTagsCommand()) {
            getAllAttachedTags().stream()
                    .collect(Collectors.groupingBy(Tags::getTagId, Collectors.counting()))
                    .forEach((id, count) ->
                            getAttachedTagsToEntities().put(id, count.intValue() == getSelectedItems().size()));

            tagListModel.setAttachedTagsToEntities(getAttachedTagsToEntities());
        } else if ("OnAssignTags".equals(getLastExecutedCommand().getName())) { //$NON-NLS-1$
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

package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

public class SpiceMenu {
    private List<SpiceMenuItem> items;

    public List<SpiceMenuItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }

        return items;
    }

    public List<SpiceMenuItem> descendants() {
        ArrayList<SpiceMenuItem> list = new ArrayList<>();
        for (SpiceMenuItem item : items) {
            descendantsInternal(list, item);
        }

        return list;
    }

    private void descendantsInternal(List<SpiceMenuItem> list, SpiceMenuItem root) {
        list.add(root);
        if (root instanceof SpiceMenuContainerItem) {
            for (SpiceMenuItem item : ((SpiceMenuContainerItem) root).getItems()) {
                descendantsInternal(list, item);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (SpiceMenuItem item : getItems()) {
            builder.append(itemToString(item, null));
        }

        return builder.toString();
    }

    private String itemToString(SpiceMenuItem item, SpiceMenuItem parent) {
        StringBuilder builder = new StringBuilder();
        int parentID = parent != null ? parent.getId() : 0;

        if (item instanceof SpiceMenuCommandItem) {
            SpiceMenuCommandItem commandItem = (SpiceMenuCommandItem) item;
            builder.append(formatSpiceMenuItem(
                    parentID,
                    commandItem.getId(),
                    commandItem.getText(),
                    commandItem.getIsEnabled() ? 0 : 2));
        }

        if (item instanceof SpiceMenuContainerItem) {
            SpiceMenuContainerItem containerItem = (SpiceMenuContainerItem) item;
            builder.append(formatSpiceMenuItem(
                    parentID,
                    containerItem.getId(),
                    containerItem.getText(),
                    4));

            if (containerItem.getItems().size() > 0) {
                for (SpiceMenuItem localItem : containerItem.getItems()) {
                    builder.append(itemToString(localItem, containerItem));
                }
            }
        }

        if (item instanceof SpiceMenuSeparatorItem) {
            builder.append(formatSpiceMenuItem(
                    parentID,
                    item.getId(),
                    "-", //$NON-NLS-1$
                    1));
        }

        return builder.toString();
    }

    private String formatSpiceMenuItem(int parentId, int itemId, String itemText, int itemCode) {
        return new StringBuilder(Integer.toString(parentId))
            .append("\r").append(itemId) //$NON-NLS-1$
            .append("\r").append(itemText.replaceAll("_", "__")) // $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
            .append("\r").append(itemCode) //$NON-NLS-1$
            .append("\r\n").toString(); //$NON-NLS-1$
    }

}

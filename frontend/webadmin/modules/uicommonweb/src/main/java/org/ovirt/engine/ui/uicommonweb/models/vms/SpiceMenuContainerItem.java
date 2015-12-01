package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SpiceMenuContainerItem extends SpiceMenuItem {
    private String privateText;

    public String getText() {
        return privateText;
    }

    public void setText(String value) {
        privateText = value;
    }

    private List<SpiceMenuItem> items;

    public List<SpiceMenuItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }

        return items;
    }

    public SpiceMenuContainerItem(int id, String text) {
        setId(id);
        setText(text);
    }
}

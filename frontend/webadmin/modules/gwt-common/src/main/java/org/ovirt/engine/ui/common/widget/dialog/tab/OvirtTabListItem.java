package org.ovirt.engine.ui.common.widget.dialog.tab;

import java.util.List;

import org.gwtbootstrap3.client.ui.TabListItem;
import org.ovirt.engine.ui.common.widget.HasValidation;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public class OvirtTabListItem extends TabListItem implements HasValidation {

    public static interface TabListItemResources extends ClientBundle {
        @Source("org/ovirt/engine/ui/common/css/OvirtTabListItem.css")
        TabListItemStyle tabListItemStyle();
    }

    public static interface TabListItemStyle extends CssResource {
        String hasError();
    }

    private static final TabListItemResources RESOURCES = GWT.create(TabListItemResources.class);

    private final TabListItemStyle style;

    /**
     * Defines if this {@code OvirtTabListItem} is valid or not.
     */
    private boolean valid;

    /**
     * Constructor
     * @param text The text of the item.
     */
    public OvirtTabListItem(String text) {
        super(text);
        style = RESOURCES.tabListItemStyle();
        style.ensureInjected();
    }

    @Override
    public void markAsValid() {
        valid = true;
        removeStyleName(style.hasError());
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        valid = false;
        addStyleName(style.hasError());
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public void setAnchorStyle(String styleName) {
        anchor.addStyleName(styleName);
    }
}

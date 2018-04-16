package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.action.ActionButton;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class is the kebab (three vertical dots) button and menu.
 * <p>
 * The class allows you to add items to the drop down as well as dividers if needed.
 */
public class Kebab extends ButtonGroup {

    private static final String FA_ELLIPSIS_V = "fa-ellipsis-v"; // $NON-NLS-1$
    private static final String BTN_DEFAULT = "btn-default"; // $NON-NLS-1$
    private static final String BTN_LINK = "btn-link"; // $NON-NLS-1$
    private static final String DROPDOWN_MENU_RIGHT = "dropdown-menu-right"; // $NON-NLS-1$

    private Button kebabButton;
    private DropDownMenu kebabMenu;

    public Kebab() {
        addStyleName(PatternflyConstants.PF_KEBAB_DROPDOWN);
        addStyleName(Styles.DROPDOWN);
        addStyleName(Styles.PULL_RIGHT);

        kebabButton = createKebabButton();
        // setDataToggle will call a deferred method to add the 'caret' so we need to add our deferred after
        // that in the queue so we can modify the caret to be a kebab.
        Scheduler.get().scheduleDeferred(() -> {
            // Unfortunately we have to make assumptions about the structure of the widget in order to get at the
            // reference for the caretWidget (which is normally a down arrow) to replace it with a kebab icon.
            if (kebabButton.getWidgetCount() > 2) {
                Widget caretWidget = kebabButton.getWidget(2);
                caretWidget.removeStyleName(PatternflyConstants.PF_CARET);
                caretWidget.addStyleName(Styles.FONT_AWESOME_BASE);
                caretWidget.addStyleName(FA_ELLIPSIS_V);
            }
        });
        add(kebabButton);

        kebabMenu = createDropDownMenu();
        add(kebabMenu);
    }

    private DropDownMenu createDropDownMenu() {
        DropDownMenu dropDownMenu = new DropDownMenu();
        dropDownMenu.addStyleName(DROPDOWN_MENU_RIGHT);
        return dropDownMenu;
    }

    private Button createKebabButton() {
        Button button = new Button();
        button.addStyleName(Styles.DROPDOWN_TOGGLE);
        button.addStyleName(BTN_LINK);
        button.removeStyleName(BTN_DEFAULT);
        button.setDataToggle(Toggle.DROPDOWN);
        return button;
    }

    public void addMenuItem(ActionButton actionButton) {
        addMenuItem(actionButton, Integer.MAX_VALUE);
    }

    public void addMenuItem(ActionButton actionButton, int index) {
        if (index > kebabMenu.getWidgetCount()) {
            kebabMenu.add(actionButton);
        } else {
            kebabMenu.insert(actionButton.asWidget(), index);
        }
    }

    public boolean containsMenuItem(ActionButton actionButton) {
        return kebabMenu.getWidgetIndex(actionButton) > -1;
    }

    public boolean hasMenuItems() {
        return kebabMenu.getWidgetCount() > 0;
    }

    public void addDivider() {
        kebabMenu.add(new Divider());
    }

}

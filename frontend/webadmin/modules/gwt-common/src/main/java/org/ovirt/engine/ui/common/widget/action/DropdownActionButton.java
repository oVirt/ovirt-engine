package org.ovirt.engine.ui.common.widget.action;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.DropDownHeader;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;

public class DropdownActionButton<E, T> extends ButtonGroup implements ActionButton {

    DropDownMenu menuPopup;
    Button textButton;
    Button button;
    Button caretButton;
    WidgetTooltip toolTip;

    private List<Pair<AnchorListItem, ActionButtonDefinition<E, T>>> items = new ArrayList<>();

    private final SelectedItemsProvider<E, T> selectedItemsProvider;

    public DropdownActionButton(List<ActionButtonDefinition<E, T>> actions,
            SelectedItemsProvider<E, T> selectedItemsProvider) {
        this(actions, selectedItemsProvider, false, null);
    }

    public DropdownActionButton(List<ActionButtonDefinition<E, T>> actions,
            SelectedItemsProvider<E, T> selectedItemsProvider, IconType icon) {
        this(actions, selectedItemsProvider, false, icon);
    }

    public DropdownActionButton(List<ActionButtonDefinition<E, T>> actions,
            SelectedItemsProvider<E, T> selectedItemsProvider, boolean splitButton, IconType icon) {
        this.selectedItemsProvider = selectedItemsProvider;
        initDropdownButton(icon, splitButton);
        initMenuPopup(new ArrayList<ActionButtonDefinition<E, T>>(actions));
    }

    private void initDropdownButton(IconType icon, boolean splitButton) {
        button = new Button();
        caretButton = new Button();
        caretButton.setDataToggle(Toggle.DROPDOWN);
        caretButton.setToggleCaret(true);
        if (splitButton) {
            caretButton.getElement().getStyle().setMarginLeft(0, Unit.PX);
            caretButton.getElement().getStyle().setLeft(-1, Unit.PX);
            textButton = button;
            toolTip = new WidgetTooltip(textButton);
            add(toolTip);
            add(caretButton);
        } else {
            textButton = caretButton;
            toolTip = new WidgetTooltip(textButton);
            add(toolTip);
        }
        if (icon != null) {
            textButton.setIcon(icon);
        }
    }

    private void initMenuPopup(List<ActionButtonDefinition<E, T>> actions) {
        if (menuPopup == null) {
            menuPopup = new DropDownMenu();
        }

        for (final ActionButtonDefinition<E, T> buttonDef : actions) {
            if (buttonDef instanceof UiMenuBarButtonDefinition) {
                UiMenuBarButtonDefinition<E, T> menuBarDef = (UiMenuBarButtonDefinition<E, T>) buttonDef;
                DropDownHeader subMenuHeader = new DropDownHeader(buttonDef.getText());
                menuPopup.add(new Divider());
                menuPopup.add(subMenuHeader);
                initMenuPopup(menuBarDef.getSubActions());
            } else {
                AnchorListItem menuItem = new AnchorListItem(buttonDef.getText());
                menuItem.addClickHandler(e -> {
                    buttonDef.onClick(selectedItemsProvider.getParentEntity(), selectedItemsProvider.getSelectedItems());
                });
                updateMenuItem(menuItem, buttonDef, selectedItemsProvider.getParentEntity(), selectedItemsProvider.getSelectedItems());
                menuPopup.add(menuItem);
                items.add(new Pair<>(menuItem, buttonDef));
            }
        }
        add(menuPopup);
    }

    /**
     * Ensures that the specified action button is visible or hidden and enabled or disabled as it should.
     */
    void updateMenuItem(AnchorListItem item, ActionButtonDefinition<E, T> buttonDef, E parent, List<T> selectedItems) {
        item.setVisible(buttonDef.isAccessible(parent, selectedItems) && buttonDef.isVisible(parent, selectedItems));
        item.setEnabled(buttonDef.isEnabled(parent, selectedItems));

        if (buttonDef.getTooltip() != null) {
            ElementTooltipUtils.setTooltipOnElement(item.getElement(), buttonDef.getTooltip());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
        textButton.setEnabled(enabled);
        caretButton.setEnabled(enabled);
        for (Pair<AnchorListItem, ActionButtonDefinition<E, T>> item : items) {
            updateMenuItem(item.getFirst(), item.getSecond(), selectedItemsProvider.getParentEntity(), selectedItemsProvider.getSelectedItems());
        }
    }

    public interface SelectedItemsProvider<E, T> {
        E getParentEntity();
        List<T> getSelectedItems();
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return textButton.addClickHandler(handler);
    }

    @Override
    public boolean isEnabled() {
        return textButton.isEnabled();
    }

    @Override
    public void setTooltip(SafeHtml tooltipText) {
        toolTip.setText(tooltipText.asString());
    }

    @Override
    public void setTooltip(SafeHtml tooltipText, Placement placement) {
        setTooltip(tooltipText);
        toolTip.setPlacement(placement);
    }


    @Override
    public void setText(String label) {
        textButton.setText(label);
    }

}

package org.ovirt.engine.ui.common.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.Kebab;
import org.ovirt.engine.ui.common.widget.action.ActionAnchorListItem;
import org.ovirt.engine.ui.common.widget.action.ActionButton;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.DropdownActionButton;
import org.ovirt.engine.ui.common.widget.action.DropdownActionButton.SelectedItemsProvider;
import org.ovirt.engine.ui.common.widget.action.SimpleActionButton;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class ActionPanelView<E, T> extends AbstractView implements ActionPanelPresenterWidget.ViewDef<E, T> {

    private static final String HIDDEN_KEBAB = "hidden-kebab"; // $NON-NLS-1$

    public interface ViewUiBinder extends UiBinder<Widget, ActionPanelView<?, ?>> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ActionPanelView<?, ?>> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    FlowPanel mainContainer;

    @UiField
    FlowPanel actionContainer;

    @UiField
    FormGroup actionFormGroup;

    @UiField
    Kebab actionKebab;

    @UiField
    Container filterResults;

    @UiField
    FormGroup searchBarContainer;

    private String elementId = DOM.createUniqueId();

    // Map of ActionButtonDefinition to AnchorListItems.
    private final Map<ActionButtonDefinition<?, T>, ActionButton> actionItemMap = new HashMap<>();

    public ActionPanelView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void addSearchPanel(IsWidget searchPanel) {
        if (searchPanel != null) {
            searchBarContainer.add(searchPanel);
            searchBarContainer.setVisible(true);
        } else {
            searchBarContainer.clear();
            searchBarContainer.setVisible(false);
        }
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public ActionButton addMenuListItem(ActionButtonDefinition<E, T> menuItemDef) {
        ActionAnchorListItem menuItem = new ActionAnchorListItem(menuItemDef.getText());
        // Set menu item ID for better accessibility
        String menuItemId = menuItemDef.getUniqueId();
        if (menuItemId != null) {
            menuItem.asWidget().getElement().setId(ElementIdUtils.createElementId(getElementId(), menuItemId));
        }

        actionItemMap.put(menuItemDef, menuItem);
        actionKebab.addMenuItem(menuItem, menuItemDef.getIndex());
        return menuItem;
    }

    /**
     * Adds a new button to the action panel.
     */
    @Override
    public ActionButton addActionButton(ActionButtonDefinition<?, T> buttonDef) {
        SimpleActionButton newActionButton = createNewActionButton(buttonDef);
        initButton(buttonDef, newActionButton);
        return newActionButton;
    }

    @Override
    public ActionButton addDropdownActionButton(ActionButtonDefinition<E, T> buttonDef,
            List<ActionButtonDefinition<E, T>> subActions, SelectedItemsProvider<E, T> selectedItemsProvider) {
        DropdownActionButton<E, T> dropdownActionButton = new DropdownActionButton<>(subActions, selectedItemsProvider);
        initButton(buttonDef, dropdownActionButton);
        return dropdownActionButton;
    }

    @Override
    public ActionButton addDropdownComboActionButton(ActionButtonDefinition<E, T> buttonDef,
            List<ActionButtonDefinition<E, T>> subActions, SelectedItemsProvider<E, T> selectedItemsProvider) {
        DropdownActionButton<E, T> dropdownActionButton;
        if (buttonDef.getIcon() instanceof IconType) {
            dropdownActionButton = new DropdownActionButton<>(subActions, selectedItemsProvider,
                true, (IconType)buttonDef.getIcon());
        } else {
            dropdownActionButton = new DropdownActionButton<>(subActions, selectedItemsProvider,
                    true, null);
        }
        initButton(buttonDef, dropdownActionButton);
        return dropdownActionButton;
    }

    private void initButton(ActionButtonDefinition<?, T> buttonDef, ActionButton button) {
        button.setText(buttonDef.getText());
        // Set button element ID for better accessibility
        String buttonId = buttonDef.getUniqueId();
        if (buttonId != null) {
            button.asWidget().getElement().setId(
                    ElementIdUtils.createElementId(getElementId(), buttonId));
        }

        // No insert available so need to remove the kebab and then add it at the end.
        actionFormGroup.remove(actionKebab);

        if (buttonDef.getIndex() >= actionFormGroup.getWidgetCount()) {
            // Add the button at the end
            actionFormGroup.add(button);
            actionFormGroup.add(actionKebab);
        } else {
            // Re-create ordered button list
            List<Widget> currentButtons = new ArrayList<>();
            for (int i = 0; i < actionFormGroup.getWidgetCount(); i++) {
                currentButtons.add(actionFormGroup.getWidget(i));
            }
            currentButtons.add(buttonDef.getIndex(), button.asWidget());

            // Re-add all buttons
            actionFormGroup.clear();
            currentButtons.forEach(existingButton -> actionFormGroup.add(existingButton));
            actionFormGroup.add(actionKebab);
        }

        actionItemMap.put(buttonDef, button);
    }

    private SimpleActionButton createNewActionButton(ActionButtonDefinition<?, T> buttonDef) {
        SimpleActionButton result = new SimpleActionButton();
        if (buttonDef.getIcon() instanceof IconType) {
            result.setIcon((IconType) buttonDef.getIcon());
        }
        return result;
    }

    @Override
    public void updateActionButton(boolean isVisible, boolean isEnabled, ActionButtonDefinition<?, T> buttonDef) {
        ActionButton button = actionItemMap.get(buttonDef);
        if (button != null) {
            button.asWidget().setVisible(isVisible);
            button.setEnabled(isEnabled);
            if (buttonDef.getTooltip() != null) {
                // this Panel is special. show the tooltips below the buttons because they're too
                // hard to read with the default TOP placement.
                button.setTooltip(buttonDef.getTooltip(), Placement.BOTTOM);
            }
        }
    }

    @Override
    public void updateMenuItem(boolean isVisible, boolean isEnabled, ActionButtonDefinition<?, T> menuItemDef) {
        ActionButton item = actionItemMap.get(menuItemDef);
        if (item != null) {
            item.asWidget().setVisible(isVisible);
            item.setEnabled(isEnabled);
            if (menuItemDef.getMenuItemTooltip() != null) {
                ElementTooltipUtils.setTooltipOnElement(item.asWidget().getElement(), menuItemDef.getMenuItemTooltip());
            }
        }
        updateKebabVisibleState();
    }

    public void updateKebabVisibleState() {
        boolean hasVisibleItems = actionKebab.hasMenuItems() && anyMenuItemVisible();
        actionKebab.setVisible(hasVisibleItems);
        if (!hasVisibleItems) {
            mainContainer.addStyleName(HIDDEN_KEBAB);
        } else {
            mainContainer.removeStyleName(HIDDEN_KEBAB);
        }
    }

    private boolean anyMenuItemVisible() {
        return actionItemMap.values().stream().anyMatch(actionButton ->
            actionKebab.containsMenuItem(actionButton) && actionButton.asWidget().isVisible()
        );
    }

    @Override
    public void setFilterResult(IsWidget result) {
        filterResults.add(result);
    }

    @Override
     public void addDividerToKebab() {
        actionKebab.addDivider();
    }

    @Override
    public Map<ActionButtonDefinition<?, T>, ActionButton> getActionItems() {
        return actionItemMap;
    }

}

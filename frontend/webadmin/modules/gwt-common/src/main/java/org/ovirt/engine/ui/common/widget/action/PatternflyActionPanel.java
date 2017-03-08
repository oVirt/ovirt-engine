package org.ovirt.engine.ui.common.widget.action;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.Kebab;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public class PatternflyActionPanel extends Row {
    private FormGroup searchFormGroup;
    private FormGroup actionFormGroup;
    private Column column;
    private Kebab actionKebab;

    public PatternflyActionPanel() {
        FlowPanel actionToolbarColumn = new FlowPanel();
        addStyleName(PatternflyConstants.PF_TOOLBAR);

        column = new Column(ColumnSize.SM_12);
        column.add(actionToolbarColumn);
        add(column);
        FlowPanel actionContainer = new FlowPanel();
        actionContainer.addStyleName(PatternflyConstants.PF_TOOLBAR_ACTIONS);
        actionToolbarColumn.add(actionContainer);

        searchFormGroup = new FormGroup();
        searchFormGroup.addStyleName(PatternflyConstants.PF_TOOLBAR_FILTER);
        // Need to add 1 px to show left side of search bar. TODO: remove me once we have right layout.
        searchFormGroup.getElement().getStyle().setPaddingLeft(1, Style.Unit.PX);
        actionContainer.add(searchFormGroup);

        actionFormGroup = new FormGroup();
        actionContainer.add(actionFormGroup);

        actionKebab = new Kebab();
        actionKebab.setVisible(false);
        actionFormGroup.add(actionKebab);
        setVisible(false);
    }

    public void setSearchPanel(IsWidget searchPanel) {
        searchFormGroup.clear();
        if (searchPanel != null) {
            searchFormGroup.add(searchPanel);
        }
    }

    public void addResult(IsWidget result) {
        column.add(result);
    }

    public void addButtonToActionGroup(ActionButton button) {
        if (actionFormGroup.getWidgetCount() > 0) {
            actionFormGroup.remove(actionFormGroup.getWidgetCount() - 1);
        }
        actionFormGroup.add(button);
        actionFormGroup.add(actionKebab);
        setVisible(true);
    }

    public void addMenuItemToKebab(ActionButton menuItem) {
        actionKebab.addActionButton(menuItem);
        actionKebab.setVisible(true);
        setVisible(true);
    }

    public void addDividerToKebab() {
        actionKebab.addDivider();
    }
}

package org.ovirt.engine.ui.common.widget.action;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.Kebab;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public class PatternflyActionPanel extends FlowPanel {
    private FormGroup searchFormGroup;
    private FormGroup actionFormGroup;
    private FlowPanel container;
    private Kebab actionKebab;

    public PatternflyActionPanel() {
        FlowPanel actionToolbarColumn = new FlowPanel();
        addStyleName(PatternflyConstants.PF_TOOLBAR);

        container = new FlowPanel();
        container.add(actionToolbarColumn);
        add(container);
        FlowPanel actionContainer = new FlowPanel();
        actionContainer.addStyleName(PatternflyConstants.PF_TOOLBAR_ACTIONS);
        actionToolbarColumn.add(actionContainer);

        searchFormGroup = new FormGroup();
        searchFormGroup.addStyleName(PatternflyConstants.PF_TOOLBAR_FILTER);
        // Need to add 1 px to show left side of search bar. TODO: remove me once we have right layout.
        searchFormGroup.getElement().getStyle().setPaddingLeft(1, Style.Unit.PX);
        actionContainer.add(searchFormGroup);

        actionFormGroup = new FormGroup();
        actionFormGroup.addStyleName(Styles.ROW);
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
            actionFormGroup.removeStyleName(Styles.ROW);
        }
    }

    public void addResult(IsWidget result) {
        container.add(result);
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

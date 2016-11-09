package org.ovirt.engine.ui.common.widget.dialog.tab;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidget;
import org.ovirt.engine.ui.common.widget.HasValidation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.Widget;

public class DialogTab extends AbstractValidatedWidget {

    interface WidgetUiBinder extends UiBinder<Widget, DialogTab> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided=true)
    OvirtTabListItem tabListItem;

    private Widget tabContent;

    public DialogTab() {
        tabListItem = new OvirtTabListItem("#"); //$NON-NLS-1$
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected Widget getValidatedWidget() {
        return tabListItem;
    }

    public String getLabel() {
        return tabListItem.getText();
    }

    public void setLabel(String label) {
        tabListItem.setText(label);
    }

    public OvirtTabListItem getTabListItem() {
        return tabListItem;
    }

    @UiChild(tagname = "content", limit = 1)
    public void setContent(Widget widget) {
        this.tabContent = widget;
    }

    public Widget getContent() {
        return tabContent;
    }

    public void activate() {
        getTabListItem().setEnabled(true);
    }

    public void deactivate() {
        getTabListItem().setEnabled(false);
    }

    public void setTabLabelStyle(String styleName) {
        tabListItem.setStyleName(styleName);
    }

    public void setTabAnchorStyle(String styleName) {
        tabListItem.setAnchorStyle(styleName);
    }

    /**
     * Disables the content widget recursively, using {@link HasEnabled} interface.
     */
    public void disableContent() {
        disable(getContent());
    }

    private void disable(Widget content) {
        if (content instanceof IndexedPanel) {
            for (int i = 0; i < ((IndexedPanel) content).getWidgetCount(); i++) {
                disable(((IndexedPanel) content).getWidget(i));
            }
        } else if (content instanceof HasEnabled) {
            ((HasEnabled) content).setEnabled(false);
        }
    }

    public List<HasValidation> getInvalidWidgets() {
        return getInvalidWidgets(getContent());
    }

    private List<HasValidation> getInvalidWidgets(Widget content) {
        List<HasValidation> hasValidations = new ArrayList<>();
        if (content instanceof IndexedPanel) {
            for (int i = 0; i < ((IndexedPanel) content).getWidgetCount(); i++) {
                hasValidations.addAll(getInvalidWidgets(((IndexedPanel) content).getWidget(i)));
            }
        } else if (content instanceof HasValidation && !((HasValidation) content).isValid()) {
            hasValidations.add((HasValidation) content);
        }

        return hasValidations;
    }

    public int setTabIndexes(int nextTabIndex) {
        tabListItem.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        tabListItem.setVisible(visible);
    }

    public HandlerRegistration addClickHandler(ClickHandler clickHandler) {
        return tabListItem.addClickHandler(clickHandler);
    }
}

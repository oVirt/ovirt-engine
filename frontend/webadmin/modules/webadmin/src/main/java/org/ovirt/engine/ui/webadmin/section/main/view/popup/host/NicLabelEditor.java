package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.PatternFlyCompatible;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Focusable;

public class NicLabelEditor extends AbstractModelBoundPopupWidget<ListModel<String>>
    implements HasValueChangeHandlers<ListModel<String>>, Focusable, PatternFlyCompatible {

    public interface Driver extends UiCommonEditorDriver<ListModel<String>, NicLabelEditor> {
    }

    private final Driver driver = GWT.create(Driver.class);

    @Path("selectedItem")
    protected ListModelSuggestBoxEditor suggestBoxEditor;

    public NicLabelEditor() {
        suggestBoxEditor = new ListModelSuggestBoxEditor();
        initWidget(suggestBoxEditor);
        driver.initialize(this);
    }

    @Override
    public void edit(final ListModel<String> model) {
        driver.edit(model);
        model.getSelectedItemChangedEvent().addListener((ev, sender, args) -> ValueChangeEvent.fire(NicLabelEditor.this, model));
    }

    @Override
    public ListModel<String> flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ListModel<String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public int getTabIndex() {
        return suggestBoxEditor.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        suggestBoxEditor.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        suggestBoxEditor.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        suggestBoxEditor.setTabIndex(index);
    }

    @Override
    public void setUsePatternFly(boolean usePatternfly) {
        suggestBoxEditor.setUsePatternFly(usePatternfly);
        suggestBoxEditor.hideLabel();
    }
}

package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Focusable;

public class NicLabelEditor extends AbstractModelBoundPopupWidget<ListModel<String>> implements HasValueChangeHandlers<ListModel<String>>, Focusable {

    public interface Driver extends SimpleBeanEditorDriver<ListModel<String>, NicLabelEditor> {
    }

    private final Driver driver = GWT.create(Driver.class);

    @Path("selectedItem")
    protected ListModelSuggestBoxEditor suggestBoxEditor;

    public NicLabelEditor() {
        suggestBoxEditor = new ListModelSuggestBoxEditor();
        initWidget(suggestBoxEditor);
        getElement().getStyle().setMarginTop(5, Unit.PX);
        getElement().getStyle().setMarginBottom(10, Unit.PX);
        getElement().getStyle().setMarginRight(15, Unit.PX);
        driver.initialize(this);
    }

    @Override
    public void edit(final ListModel<String> model) {
        driver.edit(model);
        model.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                ValueChangeEvent.fire(NicLabelEditor.this, model);
            }
        });
    }

    @Override
    public ListModel<String> flush() {
        return driver.flush();
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
}

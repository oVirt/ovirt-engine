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

public class NicLabelEditor extends AbstractModelBoundPopupWidget<ListModel<String>> implements HasValueChangeHandlers<ListModel<String>> {

    public interface Driver extends SimpleBeanEditorDriver<ListModel<String>, NicLabelEditor> {
    }

    private final Driver driver = GWT.create(Driver.class);

    @Path("selectedItem")
    protected ListModelSuggestBoxEditor suggestBoxEditor;

    public NicLabelEditor() {
        suggestBoxEditor = new ListModelSuggestBoxEditor();
        initWidget(suggestBoxEditor);
        getElement().getStyle().setMarginTop(10, Unit.PX);
        driver.initialize(this);
    }

    @Override
    public void edit(final ListModel<String> model) {
        driver.edit(model);
        model.getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
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
}

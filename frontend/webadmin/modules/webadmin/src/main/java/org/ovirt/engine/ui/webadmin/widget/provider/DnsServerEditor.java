package org.ovirt.engine.ui.webadmin.widget.provider;

import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class DnsServerEditor extends AbstractModelBoundPopupWidget<EntityModel<String>> implements HasValueChangeHandlers<EntityModel<String>> {

    public interface Driver extends SimpleBeanEditorDriver<EntityModel<String>, DnsServerEditor> {
    }


    private final Driver driver = GWT.create(Driver.class);

    @Path("entity")
    protected StringEntityModelTextBoxEditor stringEditor;

    public DnsServerEditor() {
        stringEditor = new StringEntityModelTextBoxEditor();
        initWidget(stringEditor);
        getElement().getStyle().setMarginTop(10, Unit.PX);
        driver.initialize(this);
    }

    @Override
    public void edit(final EntityModel<String> model) {
        driver.edit(model);
        stringEditor.fireValueChangeOnKeyDown();
        stringEditor.asValueBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                ValueChangeEvent.fire(DnsServerEditor.this, model);
            }
        });
    }

    @Override
    public EntityModel<String> flush() {
        return driver.flush();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<EntityModel<String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}

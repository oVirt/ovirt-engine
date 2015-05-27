package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsNicLabelModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class VfsNicLabelWidget extends NicLabelWidget implements HasValueChangeHandlers<Set<String>> {

    private String labelEditorStyle;
    private String editorWrapperStyle;

    public void setLabelEditorStyle(String labelEditorStyle) {
        this.labelEditorStyle = labelEditorStyle;
    }

    public void setEditorWrapperStyle(String editorWrapperStyle) {
        this.editorWrapperStyle = editorWrapperStyle;
    }

    @Override
    protected NicLabelEditor createWidget(ListModel<String> value) {
        NicLabelEditor editor = super.createWidget(value);
        editor.suggestBoxEditor.addContentWidgetContainerStyleName(labelEditorStyle);
        editor.suggestBoxEditor.addWrapperStyleName(editorWrapperStyle);
        return editor;
    }

    @Override
    protected void init(VfsNicLabelModel model) {
        super.init(model);
        for (ListModel<String> labelModel : model.getItems()) {
            labelModel.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {

                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    ValueChangeEvent.fire(VfsNicLabelWidget.this, null);

                }
            });
        }
    }

    @Override
    protected void onAdd(ListModel<String> value, NicLabelEditor widget) {
        super.onAdd(value, widget);
        value.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                ValueChangeEvent.fire(VfsNicLabelWidget.this, null);
            }
        });
    }

    @Override
    protected void onRemove(ListModel<String> value, NicLabelEditor widget) {
        super.onRemove(value, widget);
        ValueChangeEvent.fire(this, null);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Set<String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}

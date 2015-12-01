package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.Collection;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.ScrollableAddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsNicLabelModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class VfsNicLabelWidget extends ScrollableAddRemoveRowWidget<VfsNicLabelModel, ListModel<String>, NicLabelEditor> implements HasValueChangeHandlers<Set<String>> {

    private String labelEditorStyle;
    private String editorWrapperStyle;

    @UiField
    @Ignore
    Label titleLabel;

    public interface WidgetUiBinder extends UiBinder<Widget, VfsNicLabelWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private Collection<String> suggestions;

    public VfsNicLabelWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected ListModel<String> createGhostValue() {
        ListModel<String> value = new ListModel<>();
        value.setItems(suggestions);
        value.setSelectedItem(""); //$NON-NLS-1$
        return value;
    }

    @Override
    protected boolean isGhost(ListModel<String> value) {
        String text = value.getSelectedItem();
        return text == null || text.isEmpty();
    }

    @Override
    public void edit(VfsNicLabelModel model) {
        suggestions = model.getSuggestedLabels();
        super.edit(model);
    }

    public void setLabelEditorStyle(String labelEditorStyle) {
        this.labelEditorStyle = labelEditorStyle;
    }

    public void setEditorWrapperStyle(String editorWrapperStyle) {
        this.editorWrapperStyle = editorWrapperStyle;
    }

    @Override
    protected NicLabelEditor createWidget(ListModel<String> value) {
        NicLabelEditor editor = new NicLabelEditor();
        editor.edit(value);
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

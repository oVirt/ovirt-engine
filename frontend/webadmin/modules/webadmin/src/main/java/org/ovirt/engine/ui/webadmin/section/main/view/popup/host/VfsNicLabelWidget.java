package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.Set;

import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsNicLabelModel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class VfsNicLabelWidget extends AddRemoveRowWidget<VfsNicLabelModel, ListModel<String>, NicLabelEditor> implements HasValueChangeHandlers<Set<String>> {

    public interface WidgetUiBinder extends UiBinder<Widget, VfsNicLabelWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public VfsNicLabelWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected ListModel<String> createGhostValue() {
        ListModel<String> value = new ListModel<>();
        value.setSelectedItem(""); //$NON-NLS-1$
        return value;
    }

    @Override
    protected boolean isGhost(ListModel<String> value) {
        String text = value.getSelectedItem();
        return text == null || text.isEmpty();
    }

    @Override
    protected NicLabelEditor createWidget(ListModel<String> value) {
        NicLabelEditor editor = new NicLabelEditor();
        editor.setUsePatternFly(true);
        editor.edit(value);
        return editor;
    }

    @Override
    protected void init(final VfsNicLabelModel model) {
        super.init(model);
        getModel().updateSuggestedLabels();
        for (ListModel<String> labelModel : model.getItems()) {
            labelModel.getSelectedItemChangedEvent().addListener((ev, sender, args) -> selectedLabelsChanged());
        }
    }

    @Override
    protected void onAdd(ListModel<String> value, NicLabelEditor widget) {
        super.onAdd(value, widget);
        getModel().updateSuggestedLabels();
        value.getSelectedItemChangedEvent().addListener((ev, sender, args) -> selectedLabelsChanged());
    }

    @Override
    protected void onRemove(ListModel<String> value, NicLabelEditor widget) {
        super.onRemove(value, widget);
        getModel().updateSuggestedLabels();
    }

    private void selectedLabelsChanged() {
        ValueChangeEvent.fire(this, null);
        getModel().updateSuggestedLabels();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Set<String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}

package org.ovirt.engine.ui.common.widget.profile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.editor.TakesConstrainedValueEditor;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;

public class ProfilesInstanceTypeEditor extends Composite implements IsEditor<TakesValueEditor<Object>>, TakesValue<Object>, HasElementId, HasConstrainedValue<Object> {

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Object> handler) {
        // not needed - there is no selected item because all are edited
        return null;
    }

    interface WidgetUiBinder extends UiBinder<Widget, ProfilesInstanceTypeEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private String elementId = DOM.createUniqueId();

    @UiField
    FlowPanel contentPanel;

    @UiField
    Label headerLabel;

    protected static final CommonApplicationMessages messages = GWT.create(CommonApplicationMessages.class);

    private List<ProfileInstanceTypeEditor> editors;

    public ProfilesInstanceTypeEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        editors = new ArrayList<ProfileInstanceTypeEditor>();
    }

    @Override
    public void setValue(Object listModelValues) {
        // not needed - there is no selected item because all are edited
    }

    @Override
    public void setValue(Object value, boolean fireEvents) {
        // not needed - there is no selected item because all are edited
    }

    @Override
    public void setAcceptableValues(Collection<Object> values) {
        editors.clear();
        contentPanel.clear();

        if (values == null || values.size() == 0) {
            headerLabel.setText(messages.assignNicsNothingToAssign());
            return;
        }

        for (Object value : values) {
            ProfileInstanceTypeEditor networkEditor = new ProfileInstanceTypeEditor();
            networkEditor.setElementId(elementId);
            editors.add(networkEditor);
            networkEditor.setValue((VnicInstanceType) value);
            contentPanel.add(networkEditor);
        }

        if (values.size() == 1) {
            headerLabel.setText(messages.assignNicsToProfilesSingular());
        } else {
            headerLabel.setText(messages.assignNicsToProfilesPlural(values.size()));
        }

    }

    public void flush() {
        // this flushes it
        getValue();
    }

    @Override
    public ListModel getValue() {
        List<VnicInstanceType> values = new ArrayList<VnicInstanceType>();
        for (ProfileInstanceTypeEditor editor : editors) {
            values.add(editor.getValue());
        }

        ListModel model = new ListModel();
        model.setItems(values);
        return model;
    }

    @Override
    public TakesValueEditor<Object> asEditor() {
        return TakesConstrainedValueEditor.of(this, this, this);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

}

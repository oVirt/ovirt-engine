package org.ovirt.engine.ui.common.widget.profile;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.editor.TakesConstrainedValueEditor;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ProfileInstanceTypeEditor extends Composite implements IsEditor<TakesValueEditor<VnicInstanceType>>, TakesValue<VnicInstanceType>, HasElementId {

    interface WidgetUiBinder extends UiBinder<Widget, ProfileInstanceTypeEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    ProfileEditor profileEditor;
    private VnicInstanceType vnicIntsanceType;

    private String elementId;

    public ProfileInstanceTypeEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        ((HasValueChangeHandlers) profileEditor.asEditor().getSubEditor()).addValueChangeHandler(new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                profileEditor.asEditor().getSubEditor().setValue(event.getValue());
            }
        });
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public void setValue(VnicInstanceType vnicInstanceType) {
        this.vnicIntsanceType = vnicInstanceType;
        profileEditor.setLabel(vnicInstanceType.getNetworkInterface().getName());
        profileEditor.asEditor().getSubEditor().setValue((VnicProfileView) vnicInstanceType.getSelectedItem());
        ((TakesConstrainedValueEditor) profileEditor.asEditor().getSubEditor()).setAcceptableValues((Collection) vnicInstanceType.getItems());

        profileEditor.setElementId(ElementIdUtils.createElementId(elementId, vnicInstanceType.getNetworkInterface()
                .getName()));
    }

    @Override
    public VnicInstanceType getValue() {
        // flush
        VnicProfileView profile = (VnicProfileView) profileEditor.asEditor().getSubEditor().getValue();
        vnicIntsanceType.getNetworkInterface().setVnicProfileId(profile != null ? profile.getId() : null);
        return vnicIntsanceType;
    }

    @Override
    public TakesValueEditor<VnicInstanceType> asEditor() {
        return TakesValueEditor.of(this);
    }

}

package org.ovirt.engine.ui.common.widget.profile;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.label.LabelWithTextTruncation;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class ProfileInstanceTypeEditor extends AbstractModelBoundPopupWidget<VnicInstanceType> implements HasValueChangeHandlers<VnicInstanceType>, HasElementId {

    interface Driver extends UiCommonEditorDriver<VnicInstanceType, ProfileInstanceTypeEditor> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface WidgetUiBinder extends UiBinder<Widget, ProfileInstanceTypeEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {
        String contentStyle();

        String noDisplay();
    }

    @UiField
    @Path(value = "networkInterface.name")
    LabelWithTextTruncation vnicLabel;

    @UiField
    @Path(value = "selectedItem")
    ProfileEditor profileEditor;

    @UiField
    Style style;

    private String elementId;

    public ProfileInstanceTypeEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<VnicInstanceType> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void edit(final VnicInstanceType model) {
        driver.edit(model);

        profileEditor.hideLabel();
        profileEditor.addContentWidgetContainerStyleName(style.contentStyle());
        profileEditor.setElementId(ElementIdUtils.createElementId(elementId, model.getNetworkInterface().getName()));
        syncSelectedItemWithNetworkInterface(model);
        model.getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            syncSelectedItemWithNetworkInterface(model);
            ValueChangeEvent.fire(ProfileInstanceTypeEditor.this, model);
        });
    }

    private void syncSelectedItemWithNetworkInterface(final VnicInstanceType model) {
        final VmNetworkInterface vnic = model.getNetworkInterface();
        VnicProfileView profile = model.getSelectedItem();
        vnic.setVnicProfileId(profile != null ? profile.getId() : null);
        vnic.setNetworkName(profile != null ? profile.getNetworkName() : null);
    }

    @Override
    public VnicInstanceType flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}

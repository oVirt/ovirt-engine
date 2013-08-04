package org.ovirt.engine.ui.webadmin.widget.vnicProfile;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.widget.vnicProfile.VnicProfileWidget.ViewIdHandler;
import org.ovirt.engine.ui.webadmin.widget.vnicProfile.VnicProfilesEditor.WidgetStyle;
import org.ovirt.engine.ui.webadmin.widget.vnicProfile.VnicProfilesEditor.WidgetUiBinder;
import org.ovirt.engine.ui.common.widget.Align;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class VnicProfileWidget extends AbstractModelBoundPopupWidget<VnicProfileModel> {

    interface Driver extends SimpleBeanEditorDriver<VnicProfileModel, VnicProfileWidget> {
    }

    interface WidgetUiBinder extends UiBinder<Widget, VnicProfileWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VnicProfileWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path("name.entity")
    @WithElementId("name")
    EntityModelTextBoxOnlyEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "publicUse.entity")
    public EntityModelCheckBoxEditor publicUseEditor;

    private VnicProfileModel vnicProfileModel;

    @UiField
    WidgetStyle style;

    private final Driver driver = GWT.create(Driver.class);

    private final static ApplicationConstants constants = GWT.create(ApplicationConstants.class);

    public VnicProfileWidget() {
        publicUseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        publicUseEditor.setLabel(constants.profilePublicUseInstanceTypeLabel());
        publicUseEditor.addContentWidgetStyleName(style.publicUse());
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void edit(VnicProfileModel object) {
        driver.edit(object);
    }

    @Override
    public VnicProfileModel flush() {
        return driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String publicUse();
    }

}

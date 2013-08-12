package org.ovirt.engine.ui.webadmin.widget.vnicProfile;

import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
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

    @UiField(provided = true)
    @Path(value = "networkQoS.selectedItem")
    @WithElementId("networkQoS")
    public ListModelListBoxEditor<Object> networkQoSEditor;

    private VnicProfileModel vnicProfileModel;

    @UiField
    WidgetStyle style;

    private final Driver driver = GWT.create(Driver.class);

    private final static ApplicationConstants constants = GWT.create(ApplicationConstants.class);

    public VnicProfileWidget() {
        publicUseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        networkQoSEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return (((NetworkQoS)object).getName());
            }
        });
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        publicUseEditor.setLabel(constants.profilePublicUseInstanceTypeLabel());
        networkQoSEditor.setLabel(constants.profileQoSInstanceTypeLabel());

        initStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initStyles() {
        nameEditor.addContentWidgetStyleName(style.name());
        publicUseEditor.addContentWidgetStyleName(style.publicUse());
        networkQoSEditor.addContentWidgetStyleName(style.qos());
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
        String name();

        String publicUse();

        String qos();
    }

}

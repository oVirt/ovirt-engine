package org.ovirt.engine.ui.webadmin.widget.vnicProfile;

import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class VnicProfileWidget extends AbstractModelBoundPopupWidget<VnicProfileModel> implements HasValueChangeHandlers<VnicProfileModel> {

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
    StringEntityModelTextBoxOnlyEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "publicUse.entity")
    public EntityModelCheckBoxEditor publicUseEditor;

    @UiField(provided = true)
    public InfoIcon publicInfo;

    @UiField(provided = true)
    @Path(value = "networkQoS.selectedItem")
    @WithElementId("networkQoS")
    public ListModelListBoxEditor<NetworkQoS> networkQoSEditor;

    @UiField
    WidgetStyle style;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public VnicProfileWidget() {
        publicUseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        publicInfo = new InfoIcon(templates.italicText(constants.profilePublicUseLabel()));
        networkQoSEditor = new ListModelListBoxEditor<>(new NameRenderer<NetworkQoS>());
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        publicUseEditor.setLabel(constants.profilePublicUseInstanceTypeLabel());
        networkQoSEditor.setLabel(constants.profileQoSInstanceTypeLabel());

        initStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initStyles() {
        nameEditor.addContentWidgetContainerStyleName(style.name());
        publicUseEditor.addContentWidgetContainerStyleName(style.publicUse());
        networkQoSEditor.addContentWidgetContainerStyleName(style.qos());
    }

    @Override
    public void edit(final VnicProfileModel model) {
        driver.edit(model);
        publicInfo.setVisible(model.getPublicUse().getIsAvailable());
        nameEditor.fireValueChangeOnKeyDown();
        model.getName().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                ValueChangeEvent.fire(VnicProfileWidget.this, model);
            }
        });
    }

    @Override
    public VnicProfileModel flush() {
        return driver.flush();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<VnicProfileModel> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    interface WidgetStyle extends CssResource {
        String name();

        String publicUse();

        String qos();
    }

}

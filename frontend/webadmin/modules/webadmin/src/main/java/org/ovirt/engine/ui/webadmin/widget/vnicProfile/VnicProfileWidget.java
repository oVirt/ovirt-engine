package org.ovirt.engine.ui.webadmin.widget.vnicProfile;

import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.common.widget.Align;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
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

    private final static ApplicationConstants constants = GWT.create(ApplicationConstants.class);
    private final static ApplicationResources resources = GWT.create(ApplicationResources.class);
    private final static ApplicationTemplates templates = GWT.create(ApplicationTemplates.class);

    public VnicProfileWidget() {
        publicUseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        publicInfo = new InfoIcon(templates.italicText(constants.profilePublicUseLabel()), resources);
        networkQoSEditor = new ListModelListBoxEditor<NetworkQoS>(new NullSafeRenderer<NetworkQoS>() {
            @Override
            public String renderNullSafe(NetworkQoS networkQoS) {
                return networkQoS.getName();
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
    public void edit(final VnicProfileModel model) {
        driver.edit(model);
        publicInfo.setVisible(model.getPublicUse().getIsAvailable());
        nameEditor.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        ValueChangeEvent.fire(nameEditor.asValueBox(), nameEditor.asValueBox().getValue());
                    }
                });
            }
        });
        model.getName().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
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

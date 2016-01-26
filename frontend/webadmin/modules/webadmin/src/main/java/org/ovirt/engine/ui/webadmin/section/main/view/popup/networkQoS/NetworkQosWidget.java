package org.ovirt.engine.ui.webadmin.section.main.view.popup.networkQoS;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.BaseNetworkQosModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;

public class NetworkQosWidget extends AbstractModelBoundPopupWidget<BaseNetworkQosModel> {

    interface Driver extends SimpleBeanEditorDriver<BaseNetworkQosModel, NetworkQosWidget> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<FlowPanel, NetworkQosWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<NetworkQosWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface WidgetStyle extends CssResource {
        String valueWidth();
    }

    @UiField
    WidgetStyle style;

    @UiField
    FlowPanel mainPanel;

    @UiField(provided = true)
    @Path(value = "inbound.enabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor inboundEnabled;

    @UiField(provided = true)
    @Path(value = "outbound.enabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor outboundEnabled;

    @UiField
    @Path(value = "inbound.average.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor inboundAverageEditor;

    @UiField
    @Path(value = "inbound.peak.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor inboundPeakEditor;

    @UiField
    @Path(value = "inbound.burst.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor inboundBurstEditor;

    @UiField
    @Path(value = "outbound.average.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor outboundAverageEditor;

    @UiField
    @Path(value = "outbound.peak.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor outboundPeakEditor;

    @UiField
    @Path(value = "outbound.burst.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor outboundBurstEditor;

    private BaseNetworkQosModel model;
    private final IEventListener<PropertyChangedEventArgs> availabilityListener;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public NetworkQosWidget() {
        inboundEnabled = new EntityModelCheckBoxEditor(Align.RIGHT);
        outboundEnabled = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        setStyle();
        localize();
        driver.initialize(this);

        availabilityListener = new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                    toggleVisibility();
                }
            }
        };
    }

    private void setStyle() {
        inboundAverageEditor.setContentWidgetContainerStyleName(style.valueWidth());
        inboundPeakEditor.setContentWidgetContainerStyleName(style.valueWidth());
        inboundBurstEditor.setContentWidgetContainerStyleName(style.valueWidth());
        outboundAverageEditor.setContentWidgetContainerStyleName(style.valueWidth());
        outboundPeakEditor.setContentWidgetContainerStyleName(style.valueWidth());
        outboundBurstEditor.setContentWidgetContainerStyleName(style.valueWidth());
    }

    private void localize() {
        inboundEnabled.setLabel(constants.inboundLabelQoSPopup());
        outboundEnabled.setLabel(constants.outboundLabelQoSPopup());
        inboundAverageEditor.setTitle(constants.averageNetworkQoSPopup() + constants.inMegabitsNetworkQoSPopup());
        inboundPeakEditor.setTitle(constants.peakNetworkQoSPopup() + constants.inMegabitsNetworkQoSPopup());
        inboundBurstEditor.setTitle(constants.burstNetworkQoSPopup() + constants.inMegabytesNetworkQoSPopup());
        outboundAverageEditor.setTitle(constants.averageNetworkQoSPopup() + constants.inMegabitsNetworkQoSPopup());
        outboundPeakEditor.setTitle(constants.peakNetworkQoSPopup() + constants.inMegabitsNetworkQoSPopup());
        outboundBurstEditor.setTitle(constants.burstNetworkQoSPopup() + constants.inMegabytesNetworkQoSPopup());
    }

    private void toggleVisibility() {
        mainPanel.setVisible(model.getIsAvailable());
    }

    @Override
    public void edit(BaseNetworkQosModel model) {
        driver.edit(model);

        if (this.model != null) {
            this.model.getPropertyChangedEvent().removeListener(availabilityListener);
        }
        this.model = model;
        model.getPropertyChangedEvent().addListener(availabilityListener);
        toggleVisibility();
    }

    @Override
    public BaseNetworkQosModel flush() {
        return driver.flush();
    }

}

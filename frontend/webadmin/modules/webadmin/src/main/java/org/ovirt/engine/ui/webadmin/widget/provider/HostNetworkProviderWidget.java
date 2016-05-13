package org.ovirt.engine.ui.webadmin.widget.provider;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.PatternFlyCompatible;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.providers.HostNetworkProviderModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.NeutronAgentModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class HostNetworkProviderWidget extends AbstractModelBoundPopupWidget<HostNetworkProviderModel>
    implements PatternFlyCompatible {

    interface Driver extends SimpleBeanEditorDriver<HostNetworkProviderModel, HostNetworkProviderWidget> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<FlowPanel, HostNetworkProviderWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostNetworkProviderWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField(provided = true)
    @WithElementId("networkProvider")
    public EntityModelWidgetWithInfo networkProvider;

    @Ignore
    @WithElementId("networkProviderLabel")
    public EnableableFormLabel networkProviderLabel;

    @Path(value = "networkProviders.selectedItem")
    @WithElementId("networkProviderEditor")
    public ListModelListBoxOnlyEditor<Provider<OpenstackNetworkProviderProperties>> networkProviderEditor;

    @UiField(provided = true)
    @Path(value = "networkProviderType.selectedItem")
    @WithElementId("networkProviderType")
    public ListModelListBoxEditor<ProviderType> networkProviderTypeEditor;

    @UiField
    @Path(value = "providerPluginType.selectedItem")
    @WithElementId("providerPluginType")
    public ListModelSuggestBoxEditor providerPluginTypeEditor;

    @UiField
    FlowPanel neutronAgentPanel;

    @UiField(provided = true)
    @Ignore
    NeutronAgentWidget neutronAgentWidget;

    @Inject
    public HostNetworkProviderWidget() {

        networkProviderLabel = new EnableableFormLabel();
        networkProviderEditor = new ListModelListBoxOnlyEditor<>(new NameRenderer<Provider<OpenstackNetworkProviderProperties>>());
        networkProvider = new EntityModelWidgetWithInfo(networkProviderLabel, networkProviderEditor);
        networkProviderTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer<ProviderType>());
        neutronAgentWidget = new NeutronAgentWidget();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        networkProviderLabel.setText(constants.externalNetworkProviderLabel());
        networkProvider.setExplanation(templates.italicText(constants.externalProviderExplanation()));
        networkProviderTypeEditor.setLabel(constants.typeProvider());
        providerPluginTypeEditor.setLabel(constants.pluginType());

        driver.initialize(this);
    }

    @Override
    public void edit(HostNetworkProviderModel model) {
        driver.edit(model);

        final NeutronAgentModel neutronAgentModel = model.getNeutronAgentModel();
        neutronAgentWidget.edit(neutronAgentModel);
        neutronAgentPanel.setVisible(neutronAgentModel.isPluginConfigurationAvailable().getEntity());
        neutronAgentModel.isPluginConfigurationAvailable().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                neutronAgentPanel.setVisible(neutronAgentModel.isPluginConfigurationAvailable().getEntity());
            }
        });
    }

    @Override
    public HostNetworkProviderModel flush() {
        neutronAgentWidget.flush();
        return driver.flush();
    }

    @Override
    public void setUsePatternFly(boolean use) {
        networkProvider.setUsePatternFly(use);
        networkProviderTypeEditor.setUsePatternFly(use);
        providerPluginTypeEditor.setUsePatternFly(use);
    }

}

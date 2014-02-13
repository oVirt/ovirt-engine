package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.ComboBox;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextAreaEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInitModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import java.util.Map;

public abstract class VmInitWidget extends AbstractModelBoundPopupWidget<VmInitModel> implements IndexedPanel {

    private static final CommonApplicationTemplates applicationTemplates = GWT.create(CommonApplicationTemplates.class);
    private static final CommonApplicationResources applicationResources = GWT.create(CommonApplicationResources.class);

    interface Driver extends SimpleBeanEditorDriver<VmInitModel, VmInitWidget> {
    }

    interface ViewUiBinder extends UiBinder<Widget, VmInitWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmInitWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);

    public static interface BasicStyle extends CssResource {
        String DEFAULT_CSS = "org/ovirt/engine/ui/common/css/BaseVmInitStyle.css"; //$NON-NLS-1$

        String primaryOption();

        String customScript();

        String expanderContent();
    }

    public static interface Resources extends ClientBundle {
        @Source({ CellTable.Style.DEFAULT_CSS})
        BasicStyle createStyle();
    }

    interface Style extends CssResource {
        String displayNone();
    }

    private final BasicStyle customizableStyle;

    @UiField
    Style style;

    @UiField
    @Ignore
    FlowPanel mainPanel;

    @UiField
    @Ignore
    FlowPanel syspreptOptionsContent;

    @UiField
    @Path(value = "windowsSysprepTimeZone.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> windowsSysprepTimeZoneEditor;

    @UiField
    @Path(value = "windowsSysprepTimeZoneEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor windowsSyspreptimeZoneEnabledEditor;

    @UiField
    @Ignore
    FlowPanel cloudInitOptionsContent;

    @UiField
    @Path(value = "hostname.entity")
    @WithElementId
    EntityModelTextBoxEditor hostnameEditor;

    @UiField
    @Path(value = "domain.entity")
    @WithElementId
    EntityModelTextBoxEditor domainEditor;

    @UiField
    @Path(value = "authorizedKeys.entity")
    @WithElementId
    EntityModelTextAreaEditor authorizedKeysEditor;

    @UiField
    @Path(value = "passwordSet.entity")
    @WithElementId
    EntityModelCheckBoxEditor passwordSetEditor;

    @UiField
    @Path(value = "customScript.entity")
    @WithElementId
    EntityModelTextAreaEditor customScriptEditor;

    @UiField(provided = true)
    public InfoIcon customScriptInfoIcon;

    @UiField
    @Path(value = "regenerateKeysEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor regenerateKeysEnabledEditor;


    @UiField
    @Path(value = "timeZoneEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor timeZoneEnabledEditor;

    @UiField(provided = true)
    @Path(value = "timeZoneList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> timeZoneEditor;

    @UiField
    @Ignore
    AdvancedParametersExpander authenticationExpander;

    @UiField
    @Ignore
    FlowPanel authenticationExpanderContent;

    @UiField
    @Path(value = "rootPassword.entity")
    @WithElementId
    EntityModelPasswordBoxEditor rootPasswordEditor;

    @UiField
    @Path(value = "rootPasswordVerification.entity")
    @WithElementId
    EntityModelPasswordBoxEditor rootPasswordVerificationEditor;


    @UiField
    @Ignore
    AdvancedParametersExpander networkExpander;

    @UiField
    @Ignore
    FlowPanel networkExpanderContent;

    @UiField(provided = true)
    @Path(value = "networkEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor networkEnabledEditor;

    @Path(value = "networkSelectedName.entity")
    @WithElementId
    EntityModelTextBoxEditor networkNameEditor;

    @Path(value = "networkList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> networkListEditor;

    @UiField(provided = true)
    @WithElementId
    ComboBox networkComboBox;

    @UiField
    @Ignore
    Label networkSelectLabel;

    @UiField
    @Ignore
    Label networkLabelSepSelectAdd;

    @UiField
    PushButton networkAddButton;

    @UiField
    @Ignore
    Label networkAddLabel;

    @UiField
    @Ignore
    AdvancedParametersExpander customScriptExpander;

    @UiField
    @Ignore
    FlowPanel customScriptExpanderContent;

    @UiField
    @Ignore
    Label networkLabelSepAddRemove;

    @UiField
    PushButton networkRemoveButton;

    @UiField
    @Ignore
    Label networkRemoveLabel;

    @UiField
    @Ignore
    FlowPanel networkOptions;

    @UiField
    @Path(value = "networkDhcp.entity")
    @WithElementId
    EntityModelCheckBoxEditor networkDhcpEditor;

    @UiField
    @Path(value = "networkIpAddress.entity")
    @WithElementId
    EntityModelTextBoxEditor networkIpAddressEditor;

    @UiField
    @Path(value = "networkNetmask.entity")
    @WithElementId
    EntityModelTextBoxEditor networkNetmaskEditor;

    @UiField
    @Path(value = "networkGateway.entity")
    @WithElementId
    EntityModelTextBoxEditor networkGatewayEditor;

    @UiField
    @Path(value = "networkStartOnBoot.entity")
    @WithElementId
    EntityModelCheckBoxEditor networkStartOnBootEditor;

    @UiField
    @Path(value = "dnsServers.entity")
    @WithElementId
    EntityModelTextBoxEditor dnsServers;

    @UiField
    @Path(value = "dnsSearchDomains.entity")
    @WithElementId
    EntityModelTextBoxEditor dnsSearchDomains;

    public VmInitWidget(BasicStyle style) {
        style.ensureInjected();

        this.customizableStyle = style;

        customScriptInfoIcon =
                new InfoIcon(applicationTemplates.italicText(constants.customScriptInfo()), applicationResources);

        initCheckBoxEditors();
        initListBoxEditors();
        initComboBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        initAdvancedParameterExpanders();

        localize();
        addStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);

        driver.initialize(this);
    }

    private void initAdvancedParameterExpanders() {
        authenticationExpander.initWithContent(authenticationExpanderContent.getElement());
        networkExpander.initWithContent(networkExpanderContent.getElement());
        customScriptExpander.initWithContent(customScriptExpanderContent.getElement());
    }

    void initCheckBoxEditors() {
        networkEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        networkDhcpEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        networkStartOnBootEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void initListBoxEditors() {
        timeZoneEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, String> entry = (Map.Entry<String, String>) object;
                return entry.getValue();
            }
        });

    }

    void initComboBoxEditors() {
        networkListEditor = new ListModelListBoxEditor<Object>();
        networkNameEditor = new EntityModelTextBoxEditor();
        networkComboBox = new ComboBox(networkListEditor, networkNameEditor);

    }


    void localize() {
        hostnameEditor.setLabel(constants.cloudInitHostnameLabel());
        authorizedKeysEditor.setLabel(constants.cloudInitAuthorizedKeysLabel());
        passwordSetEditor.setLabel(constants.vmInitPasswordSetLabel());
        regenerateKeysEnabledEditor.setLabel(constants.cloudInitRegenerateKeysLabel());
        timeZoneEnabledEditor.setLabel(constants.cloudInitConfigureTimeZoneLabel());
        timeZoneEditor.setLabel(constants.cloudInitTimeZoneLabel());
        windowsSyspreptimeZoneEnabledEditor.setLabel(constants.cloudInitConfigureTimeZoneLabel());
        windowsSysprepTimeZoneEditor.setLabel(constants.cloudInitTimeZoneLabel());
        rootPasswordEditor.setLabel(constants.cloudInitRootPasswordLabel());
        rootPasswordVerificationEditor.setLabel(constants.cloudInitRootPasswordVerificationLabel());

        networkEnabledEditor.setLabel(constants.cloudInitNetworkLabel());
        domainEditor.setLabel(constants.domainVmPopup());

        String sep = "|"; //$NON-NLS-1$
        // sequence is: <select label> | [+] <add label> | [-] <remove label>
        networkSelectLabel.setText(constants.cloudInitNetworkSelectLabel());
        networkLabelSepSelectAdd.setText(sep);
        networkAddLabel.setText(constants.cloudInitObjectAddLabel());
        networkLabelSepAddRemove.setText(sep);
        networkRemoveLabel.setText(constants.cloudInitObjectRemoveLabel());

        networkDhcpEditor.setLabel(constants.cloudInitNetworkDhcpLabel());
        networkIpAddressEditor.setLabel(constants.cloudInitNetworkIpAddressLabel());
        networkNetmaskEditor.setLabel(constants.cloudInitNetworkNetmaskLabel());
        networkGatewayEditor.setLabel(constants.cloudInitNetworkGatewayLabel());
        networkStartOnBootEditor.setLabel(constants.cloudInitNetworkStartOnBootLabel());
        dnsServers.setLabel(constants.cloudInitDnsServersLabel());
        dnsSearchDomains.setLabel(constants.cloudInitDnsSearchDomainsLabel());

        hostnameEditor.setTitle(constants.cloudInitHostnameToolTip());
        authorizedKeysEditor.setTitle(constants.cloudInitAuthorizedKeysToolTip());
        passwordSetEditor.setTitle(constants.vmInitPasswordSetToolTip());
        customScriptEditor.setTitle(constants.customScriptToolTip());
        regenerateKeysEnabledEditor.setTitle(constants.cloudInitRegenerateKeysToolTip());
        timeZoneEditor.setTitle(constants.cloudInitTimeZoneToolTip());
        rootPasswordEditor.setTitle(constants.cloudInitRootPasswordToolTip());
        rootPasswordVerificationEditor.setTitle(constants.cloudInitRootPasswordVerificationToolTip());

        networkListEditor.setTitle(constants.cloudInitNetworkToolTip());
        networkNameEditor.setTitle(constants.cloudInitNetworkToolTip());
        networkDhcpEditor.setTitle(constants.cloudInitNetworkDhcpToolTip());
        networkIpAddressEditor.setTitle(constants.cloudInitNetworkIpAddressToolTip());
        networkNetmaskEditor.setTitle(constants.cloudInitNetworkNetmaskToolTip());
        networkGatewayEditor.setTitle(constants.cloudInitNetworkGatewayToolTip());
        networkStartOnBootEditor.setTitle(constants.cloudInitNetworkStartOnBootToolTip());
        dnsServers.setTitle(constants.cloudInitDnsServersToolTip());
        dnsSearchDomains.setTitle(constants.cloudInitDnsSearchDomainsToolTip());

        networkExpander.setTitleWhenExpended(constants.cloudInitNetworskLabel());
        networkExpander.setTitleWhenCollapsed(constants.cloudInitNetworskLabel());

        authenticationExpander.setTitleWhenExpended(constants.cloudInitAuthenticationLabel());
        authenticationExpander.setTitleWhenCollapsed(constants.cloudInitAuthenticationLabel());

        customScriptExpander.setTitleWhenExpended(constants.customScriptLabel());
        customScriptExpander.setTitleWhenCollapsed(constants.customScriptLabel());
    }

    void addStyles() {
        networkListEditor.addLabelStyleName(style.displayNone());
        setNetworkDetailsStyle(false);
        setNetworkStaticDetailsStyle(false);

        windowsSyspreptimeZoneEnabledEditor.addStyleName(customizableStyle.primaryOption());
        windowsSyspreptimeZoneEnabledEditor.addStyleName(customizableStyle.primaryOption());
        windowsSysprepTimeZoneEditor.addStyleName(customizableStyle.primaryOption());
        hostnameEditor.addStyleName(customizableStyle.primaryOption());
        domainEditor.addStyleName(customizableStyle.primaryOption());
        timeZoneEnabledEditor.addStyleName(customizableStyle.primaryOption());
        timeZoneEditor.addStyleName(customizableStyle.primaryOption());
        rootPasswordEditor.addStyleName(customizableStyle.primaryOption());
        rootPasswordVerificationEditor.addStyleName(customizableStyle.primaryOption());
        authorizedKeysEditor.addStyleName(customizableStyle.primaryOption());
        passwordSetEditor.addStyleName(customizableStyle.primaryOption());
        regenerateKeysEnabledEditor.addStyleName(customizableStyle.primaryOption());
        networkExpanderContent.addStyleName(customizableStyle.primaryOption());

        customScriptEditor.setContentWidgetStyleName(customizableStyle.primaryOption());
        customScriptEditor.setContentWidgetStyleName(customizableStyle.customScript());

        authenticationExpanderContent.addStyleName(customizableStyle.expanderContent());
        networkExpanderContent.addStyleName(customizableStyle.expanderContent());
        customScriptExpanderContent.addStyleName(customizableStyle.expanderContent());
    }

    /* Controls style for network options based on network selection */
    private void setNetworkDetailsStyle(boolean enabled) {
        networkNameEditor.setEnabled(enabled);
        networkListEditor.setEnabled(enabled);
        setLabelEnabled(networkSelectLabel, enabled);
        setLabelEnabled(networkLabelSepSelectAdd, enabled);
        setLabelEnabled(networkLabelSepAddRemove, enabled);
        setLabelEnabled(networkRemoveLabel, enabled);
        networkRemoveButton.setEnabled(enabled);
        networkOptions.setVisible(enabled);
    }

    /* Sets visibility for static networking options */
    private void setNetworkStaticDetailsStyle(boolean visible) {
        networkIpAddressEditor.setVisible(visible);
        networkNetmaskEditor.setVisible(visible);
        networkGatewayEditor.setVisible(visible);
    }

    private void setLabelEnabled(Label label, boolean enabled) {
        label.getElement().getStyle().setColor(enabled ? "#000000" : "#999999"); //$NON-NLS-1$ //$NON-NLS-2$
    }


    @Override
    public void edit(final VmInitModel model) {
        driver.edit(model);

        initializeEnabledCBBehavior(model);

        networkAddButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.getAddNetworkCommand().execute();
            }
        });

        networkRemoveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.getRemoveNetworkCommand().execute();
            }
        });

        model.getNetworkList().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                // Can't use ListModel.isEmpty() because ListModel.SetItems(<empty list>)) will
                // cause the ItemsChanged and SelectedItemChanged events to be fired before we
                // can update the isEmpty() flag, causing erroneous readings upon item removal.
                setNetworkDetailsStyle(model.getNetworkList().getSelectedItem() != null);
            }
        });

        model.getNetworkDhcp().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                setNetworkStaticDetailsStyle(model.getNetworkDhcp().getEntity() == null
                        || !(Boolean) model.getNetworkDhcp().getEntity());
            }
        });

        model.getPasswordSet().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).propertyName;
                if ("IsChangable".equals(propName)) { //$NON-NLS-1$
                    passwordSetEditor.setTitle(
                            model.getPasswordSet().getIsChangable() ?
                            constants.vmInitPasswordSetToolTip() : constants.vmInitPasswordNotSetToolTip()
                    );
                }
            }
        });

    }

    void initializeEnabledCBBehavior(final VmInitModel model) {
        if (model.getRegenerateKeysEnabled().getEntity() != null) {
            regenerateKeysEnabledEditor.setEnabled((Boolean) model.getRegenerateKeysEnabled().getEntity());
        }

        if (model.getTimeZoneEnabled().getEntity() != null) {
            timeZoneEnabledEditor.setEnabled((Boolean) model.getTimeZoneEnabled().getEntity());
        }
        model.getWindowsSysprepTimeZoneEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                windowsSysprepTimeZoneEditor.setEnabled((Boolean) model.getWindowsSysprepTimeZoneEnabled().getEntity());
            }
        });

        if (model.getWindowsSysprepTimeZoneEnabled().getEntity() != null) {
            windowsSysprepTimeZoneEditor.setEnabled((Boolean) model.getWindowsSysprepTimeZoneEnabled().getEntity());
        }
        model.getTimeZoneEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                timeZoneEditor.setEnabled((Boolean) model.getTimeZoneEnabled().getEntity());
            }
        });

        if (model.getNetworkEnabled().getEntity() != null) {
            networkEnabledEditor.setEnabled((Boolean) model.getNetworkEnabled().getEntity());
        }
        model.getNetworkEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean enabled = (Boolean) model.getNetworkEnabled().getEntity();
                networkAddButton.setEnabled(enabled);
                setLabelEnabled(networkAddLabel, enabled);
                // See note above re: parameter to this method call
                setNetworkDetailsStyle(enabled && model.getNetworkList().getSelectedItem() != null);
            }
        });

    }

    public void setCloudInitContentVisible(boolean visible) {
        cloudInitOptionsContent.setVisible(visible);
    }

    public void setSyspepContentVisible(boolean visible) {
        syspreptOptionsContent.setVisible(visible);
    }

    @Override
    public Widget getWidget(int index) {
        return mainPanel.getWidget(index);
    }

    @Override
    public int getWidgetCount() {
        return mainPanel.getWidgetCount();
    }

    @Override
    public int getWidgetIndex(Widget child) {
        return mainPanel.getWidgetIndex(child);
    }

    @Override
    public boolean remove(int index) {
        return mainPanel.remove(index);
    }

    @Override
    public VmInitModel flush() {
        return driver.flush();
    }
}

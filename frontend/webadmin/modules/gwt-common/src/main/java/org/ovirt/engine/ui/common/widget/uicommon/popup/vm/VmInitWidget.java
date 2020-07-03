package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import static org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor.AUTOCOMPLETE_NEW_PASSWORD;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.network.CloudInitNetworkProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.ComboBox;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInitModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class VmInitWidget extends AbstractModelBoundPopupWidget<VmInitModel> implements IndexedPanel {

    interface Driver extends UiCommonEditorDriver<VmInitModel, VmInitWidget> {
    }

    interface ViewUiBinder extends UiBinder<Widget, VmInitWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmInitWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    public static interface BasicStyle extends CssResource {
        String DEFAULT_CSS = "org/ovirt/engine/ui/common/css/BaseVmInitStyle.css"; //$NON-NLS-1$

        String primaryOption();

        String customScript();

        String expanderContent();
    }

    public static interface Resources extends ClientBundle {
        @Source(CellTable.Style.DEFAULT_CSS)
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

    @UiField(provided = true)
    @Path(value = "windowsSysprepTimeZone.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Map.Entry<String, String>> windowsSysprepTimeZoneEditor;

    @UiField(provided = true)
    @Path(value = "windowsSysprepTimeZoneEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor windowsSyspreptimeZoneEnabledEditor;

    @UiField
    @Ignore
    FlowPanel cloudInitOptionsContent;

    @Path(value = "windowsHostname.entity")
    @WithElementId
    StringEntityModelTextBoxOnlyEditor windowsHostnameEditor;

    @UiField(provided = true)
    @Ignore
    public EntityModelWidgetWithInfo windowsHostnameEditorWithInfo;

    @UiField
    @Path(value = "sysprepOrgName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor sysprepOrgNameEditor;

    @UiField
    @Path(value = "sysprepDomain.selectedItem")
    @WithElementId
    ListModelSuggestBoxEditor sysprepDomainEditor;

    @UiField
    @Path(value = "inputLocale.entity")
    @WithElementId
    StringEntityModelTextBoxEditor inputLocaleEditor;

    @UiField
    @Path(value = "uiLanguage.entity")
    @WithElementId
    StringEntityModelTextBoxEditor uiLanguageEditor;

    @UiField
    @Path(value = "systemLocale.entity")
    @WithElementId
    StringEntityModelTextBoxEditor systemLocaleEditor;

    @UiField
    @Path(value = "userLocale.entity")
    @WithElementId
    StringEntityModelTextBoxEditor userLocaleEditor;

    @UiField
    @Path(value = "sysprepScript.entity")
    @WithElementId
    StringEntityModelTextAreaEditor sysprepScriptEditor;

    @UiField
    @Path(value = "activeDirectoryOU.entity")
    @WithElementId
    StringEntityModelTextBoxEditor activeDirectoryOUEditor;

    @UiField
    @Path(value = "userName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor userNameEditor;

    @UiField
    @Path(value = "hostname.entity")
    @WithElementId
    StringEntityModelTextBoxEditor hostnameEditor;

    @UiField
    @Path(value = "authorizedKeys.entity")
    @WithElementId
    StringEntityModelTextAreaEditor authorizedKeysEditor;

    @UiField
    @Path(value = "customScript.entity")
    @WithElementId
    StringEntityModelTextAreaEditor customScriptEditor;

    @UiField(provided = true)
    public InfoIcon customScriptInfoIcon;

    @UiField(provided = true)
    @Path(value = "regenerateKeysEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor regenerateKeysEnabledEditor;


    @UiField (provided = true)
    @Path(value = "timeZoneEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor timeZoneEnabledEditor;

    @UiField(provided = true)
    @Path(value = "timeZoneList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Map.Entry<String, String>> timeZoneEditor;

    @UiField
    @Ignore
    AdvancedParametersExpander authenticationExpander;

    @UiField
    @Ignore
    FlowPanel authenticationExpanderContent;

    @UiField (provided = true)
    @Path(value = "cloudInitPasswordSet.entity")
    @WithElementId
    EntityModelCheckBoxEditor cloudInitPasswordSetEditor;

    @Path(value = "cloudInitRootPassword.entity")
    @WithElementId
    StringEntityModelPasswordBoxEditor cloudInitRootPasswordEditor;

    @UiField(provided = true)
    @Ignore
    EntityModelWidgetWithInfo cloudInitRootPasswordEditorWithInfo;

    @UiField
    @Path(value = "cloudInitRootPasswordVerification.entity")
    @WithElementId
    StringEntityModelPasswordBoxEditor cloudInitRootPasswordVerificationEditor;

    @UiField (provided = true)
    @Path(value = "sysprepPasswordSet.entity")
    @WithElementId
    EntityModelCheckBoxEditor sysprepPasswordSetEditor;

    @UiField
    @Path(value = "sysprepAdminPassword.entity")
    @WithElementId
    StringEntityModelPasswordBoxEditor sysprepAdminPasswordEditor;

    @UiField
    @Path(value = "sysprepAdminPasswordVerification.entity")
    @WithElementId
    StringEntityModelPasswordBoxEditor sysprepAdminPasswordVerificationEditor;

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
    StringEntityModelTextBoxEditor networkNameEditor;

    @Path(value = "networkList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<String> networkListEditor;

    @UiField(provided = true)
    @WithElementId
    ComboBox<String> networkComboBox;

    @UiField
    Button networkAddButton;

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
    AdvancedParametersExpander sysprepScriptExpander;

    @UiField
    @Ignore
    FlowPanel sysprepScriptExpanderContent;

    @UiField
    @Ignore
    AdvancedParametersExpander sysprepPasswordExpander;

    @UiField
    @Ignore
    FlowPanel sysprepPasswordExpanderContent;


    @UiField
    @Ignore
    AdvancedParametersExpander sysprepInputsExpander;

    @UiField
    @Ignore
    FlowPanel sysprepInputsExpanderContent;

    @UiField
    @Ignore
    Label networkLabelSepAddRemove;

    @UiField
    Button networkRemoveButton;

    @UiField
    @Ignore
    Label networkRemoveLabel;

    @UiField
    @Ignore
    Row networkOptions;

    @UiField(provided = true)
    @Path(value = "ipv4BootProtocolList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Ipv4BootProtocol> ipv4BootProtocolEditor;

    @UiField(provided = true)
    @Path(value = "ipv6BootProtocolList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Ipv6BootProtocol> ipv6BootProtocolEditor;

    @UiField
    @Path(value = "networkIpAddress.entity")
    @WithElementId
    StringEntityModelTextBoxEditor networkIpv4AddressEditor;

    @UiField
    @Path(value = "networkNetmask.entity")
    @WithElementId
    StringEntityModelTextBoxEditor networkIpv4NetmaskEditor;

    @UiField
    @Path(value = "networkGateway.entity")
    @WithElementId
    StringEntityModelTextBoxEditor networkIpv4GatewayEditor;

    @UiField
    @Path(value = "networkIpv6Address.entity")
    @WithElementId
    StringEntityModelTextBoxEditor networkIpv6AddressEditor;

    @UiField
    @Path(value = "networkIpv6Prefix.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor networkIpv6PrefixEditor;

    @UiField
    @Path(value = "networkIpv6Gateway.entity")
    @WithElementId
    StringEntityModelTextBoxEditor networkIpv6GatewayEditor;

    @UiField (provided = true)
    @Path(value = "networkStartOnBoot.entity")
    @WithElementId
    EntityModelCheckBoxEditor networkStartOnBootEditor;
    @UiField
    @Path(value = "dnsServers.entity")
    @WithElementId
    StringEntityModelTextBoxEditor dnsServers;

    @UiField
    @Path(value = "dnsSearchDomains.entity")
    @WithElementId
    StringEntityModelTextBoxEditor dnsSearchDomains;

    @Path(value = "cloudInitProtocolList.selectedItem")
    @WithElementId("cloudInitProtocolEditor")
    ListModelListBoxOnlyEditor<CloudInitNetworkProtocol> cloudInitProtocolEditor;

    @UiField(provided = true)
    @Ignore
    EntityModelWidgetWithInfo cloudInitProtocolEditorWithInfo;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public VmInitWidget(BasicStyle style) {
        style.ensureInjected();

        this.customizableStyle = style;

        customScriptInfoIcon = new InfoIcon(templates.italicText(constants.customScriptInfo()));

        initEditorsWithIcon();
        initCheckBoxEditors();
        initListBoxEditors();
        initComboBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        sysprepScriptEditor.hideLabel();
        sysprepScriptEditor.asValueBox().getElement().getStyle().setHeight(610, Unit.PX);
        sysprepScriptEditor.asValueBox().getElement().getStyle().setWidth(100, Unit.PCT);
        customScriptEditor.hideLabel();
        customScriptEditor.asValueBox().getElement().getStyle().setHeight(610, Unit.PX);
        customScriptEditor.asValueBox().getElement().getStyle().setWidth(100, Unit.PCT);
        initAdvancedParameterExpanders();

        networkRemoveButton.setIcon(IconType.MINUS);
        networkAddButton.setIcon(IconType.PLUS);
        localize();
        addStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);

        driver.initialize(this);
    }

    private void showIgnitionFields(){
        timeZoneEnabledEditor.setVisible(false);
        timeZoneEditor.setVisible(false);
        networkExpander.setVisible(false);
        networkExpander.toggleExpander(false);
        regenerateKeysEnabledEditor.setVisible(false);
        cloudInitRootPasswordEditorWithInfo.setLabel(constants.ignitionRootPasswordLabel());
        cloudInitRootPasswordEditorWithInfo.showInfoIcon();
        customScriptInfoIcon.setText(templates.italicText(constants.ignitionScriptInfo()));
        customScriptExpander.setTitleWhenExpanded(constants.ignitionScriptLabel());
        customScriptExpander.setTitleWhenCollapsed(constants.ignitionScriptLabel());
    }

    private void showCloudInitFields(){
        timeZoneEnabledEditor.setVisible(true);
        timeZoneEditor.setVisible(true);
        networkExpander.setVisible(true);
        networkExpander.toggleExpander(false);
        regenerateKeysEnabledEditor.setVisible(true);
        cloudInitRootPasswordEditorWithInfo.setLabel(constants.cloudInitRootPasswordLabel());
        cloudInitRootPasswordEditorWithInfo.hideInfoIcon();
        customScriptInfoIcon.setText(templates.italicText(constants.customScriptInfo()));
        customScriptExpander.setTitleWhenExpanded(constants.customScriptLabel());
        customScriptExpander.setTitleWhenCollapsed(constants.customScriptLabel());
    }

    private void initEditorsWithIcon() {
        windowsHostnameEditor = new StringEntityModelTextBoxOnlyEditor();

        EnableableFormLabel label = new EnableableFormLabel();
        label.setText(constants.cloudInitHostnameLabel());
        windowsHostnameEditorWithInfo = new EntityModelWidgetWithInfo(label, windowsHostnameEditor);
        windowsHostnameEditorWithInfo.setExplanation(templates.italicText(constants.windowsHostNameInfo()));

        cloudInitRootPasswordEditor = new StringEntityModelPasswordBoxEditor();
        cloudInitRootPasswordEditor.setLabelDisplay(Display.NONE);
        cloudInitRootPasswordEditorWithInfo = new EntityModelWidgetWithInfo(new EnableableFormLabel(), cloudInitRootPasswordEditor);
        cloudInitRootPasswordEditorWithInfo.setExplanation(templates.italicText(constants.ignitionPasswordInfo()));
    }

    private void initAdvancedParameterExpanders() {
        authenticationExpander.initWithContent(authenticationExpanderContent.getElement());
        networkExpander.initWithContent(networkExpanderContent.getElement());
        customScriptExpander.initWithContent(customScriptExpanderContent.getElement());
        sysprepScriptExpander.initWithContent(sysprepScriptExpanderContent.getElement());
        sysprepPasswordExpander.initWithContent(sysprepPasswordExpanderContent.getElement());
        sysprepInputsExpander.initWithContent(sysprepInputsExpanderContent.getElement());
    }

    void initCheckBoxEditors() {
        networkEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        networkStartOnBootEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        timeZoneEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        cloudInitPasswordSetEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        regenerateKeysEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        windowsSyspreptimeZoneEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        sysprepPasswordSetEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void initListBoxEditors() {
        timeZoneEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Map.Entry<String, String>>() {
            @Override
            public String renderNullSafe(Map.Entry<String, String> object) {
                return object.getValue();
            }
        });

        windowsSysprepTimeZoneEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Map.Entry<String, String>>() {
            @Override
            public String renderNullSafe(Map.Entry<String, String> object) {
                return object.getValue();
            }
        });

        ipv4BootProtocolEditor = new ListModelListBoxEditor<>(new EnumRenderer<Ipv4BootProtocol>());
        ipv6BootProtocolEditor = new ListModelListBoxEditor<>(new EnumRenderer<Ipv6BootProtocol>());
        cloudInitProtocolEditor = new ListModelListBoxOnlyEditor<>(new EnumRenderer<CloudInitNetworkProtocol>(), (source, desiredVisibility) -> true);
        EnableableFormLabel label = new EnableableFormLabel();
        label.setText(constants.cloudInitProtocolLabel());
        cloudInitProtocolEditorWithInfo = new EntityModelWidgetWithInfo(label, cloudInitProtocolEditor);
        cloudInitProtocolEditorWithInfo.setExplanation(templates.italicText(constants.cloudInitProtocolInfo()));

    }

    void initComboBoxEditors() {
        networkListEditor = new ListModelListBoxEditor<>();
        networkNameEditor = new StringEntityModelTextBoxEditor();
        networkComboBox = new ComboBox<>(networkListEditor, networkNameEditor);
    }

    void localize() {
        hostnameEditor.setLabel(constants.cloudInitHostnameLabel());
        sysprepOrgNameEditor.setLabel(constants.sysprepOrgNameLabel());
        sysprepDomainEditor.setLabel(constants.domainVmPopup());
        inputLocaleEditor.setLabel(constants.inputLocaleLabel());
        uiLanguageEditor.setLabel(constants.uiLanguageLabel());
        sysprepScriptEditor.setWidgetTooltip(constants.sysprepLabel());
        activeDirectoryOUEditor.setLabel(constants.activeDirectoryOU());
        activeDirectoryOUEditor.setWidgetTooltip(constants.activeDirectoryOUToolTip());
        systemLocaleEditor.setLabel(constants.systemLocaleLabel());
        userLocaleEditor.setLabel(constants.userLocaleLabel());
        userNameEditor.setLabel(constants.cloudInitUserNameLabel());
        authorizedKeysEditor.setLabel(constants.cloudInitAuthorizedKeysLabel());
        timeZoneEditor.setLabel(constants.cloudInitTimeZoneLabel());
        windowsSyspreptimeZoneEnabledEditor.setLabel(constants.cloudInitConfigureTimeZoneLabel());
        windowsSysprepTimeZoneEditor.setLabel(constants.cloudInitTimeZoneLabel());
        cloudInitRootPasswordEditorWithInfo.setLabel(constants.cloudInitRootPasswordLabel());
        cloudInitRootPasswordEditor.setAutocomplete(AUTOCOMPLETE_NEW_PASSWORD);
        cloudInitRootPasswordVerificationEditor.setLabel(constants.cloudInitRootPasswordVerificationLabel());
        cloudInitRootPasswordVerificationEditor.setAutocomplete(AUTOCOMPLETE_NEW_PASSWORD);
        sysprepAdminPasswordEditor.setLabel(constants.sysprepAdminPasswordLabel());
        sysprepAdminPasswordEditor.setAutocomplete(AUTOCOMPLETE_NEW_PASSWORD);
        sysprepAdminPasswordVerificationEditor.setLabel(constants.sysprepAdminPasswordVerificationLabel());
        sysprepAdminPasswordVerificationEditor.setAutocomplete(AUTOCOMPLETE_NEW_PASSWORD);

        String sep = "|"; //$NON-NLS-1$
        // sequence is: <select label> | [+] <add label> | [-] <remove label>
        networkAddLabel.setText(constants.cloudInitObjectAddLabel());
        networkLabelSepAddRemove.setText(sep);
        networkRemoveLabel.setText(constants.cloudInitObjectRemoveLabel());

        ipv4BootProtocolEditor.setLabel(constants.cloudInitNetworkIpv4BootProtocolLabel());
        networkIpv4AddressEditor.setLabel(constants.cloudInitNetworkIpv4AddressLabel());
        networkIpv4NetmaskEditor.setLabel(constants.cloudInitNetworkIpv4NetmaskLabel());
        networkIpv4GatewayEditor.setLabel(constants.cloudInitNetworkIpv4GatewayLabel());

        ipv6BootProtocolEditor.setLabel(constants.cloudInitNetworkIpv6BootProtocolLabel());
        networkIpv6AddressEditor.setLabel(constants.cloudInitNetworkIpv6AddressLabel());
        networkIpv6PrefixEditor.setLabel(constants.cloudInitNetworkIpv6PrefixLabel());
        networkIpv6GatewayEditor.setLabel(constants.cloudInitNetworkIpv6GatewayLabel());

        dnsServers.setLabel(constants.cloudInitDnsServersLabel());
        dnsSearchDomains.setLabel(constants.cloudInitDnsSearchDomainsLabel());

        hostnameEditor.setWidgetTooltip(constants.cloudInitHostnameToolTip());
        windowsHostnameEditor.setWidgetTooltip(constants.cloudInitHostnameToolTip());
        authorizedKeysEditor.setWidgetTooltip(constants.cloudInitAuthorizedKeysToolTip());
        cloudInitPasswordSetEditor.setWidgetTooltip(constants.vmInitPasswordSetToolTip());
        sysprepPasswordSetEditor.setWidgetTooltip(constants.vmInitPasswordSetToolTip());
        customScriptEditor.setWidgetTooltip(constants.customScriptToolTip());
        regenerateKeysEnabledEditor.setWidgetTooltip(constants.cloudInitRegenerateKeysToolTip());
        timeZoneEditor.setWidgetTooltip(constants.cloudInitTimeZoneToolTip());
        cloudInitRootPasswordEditor.setWidgetTooltip(constants.cloudInitRootPasswordToolTip());
        cloudInitRootPasswordVerificationEditor.setWidgetTooltip(constants.cloudInitRootPasswordVerificationToolTip());
        sysprepAdminPasswordEditor.setWidgetTooltip(constants.sysprepAdminPasswordToolTip());
        sysprepAdminPasswordVerificationEditor.setWidgetTooltip(constants.sysprepAdminPasswordVerificationToolTip());

        networkListEditor.setWidgetTooltip(constants.cloudInitNetworkToolTip());
        networkNameEditor.setWidgetTooltip(constants.cloudInitNetworkToolTip());

        ipv4BootProtocolEditor.setWidgetTooltip(constants.cloudInitNetworkIpv4BootProtocolToolTip());
        networkIpv4AddressEditor.setWidgetTooltip(constants.cloudInitNetworkIpv4AddressToolTip());
        networkIpv4NetmaskEditor.setWidgetTooltip(constants.cloudInitNetworkIpv4NetmaskToolTip());
        networkIpv4GatewayEditor.setWidgetTooltip(constants.cloudInitNetworkIpv4GatewayToolTip());

        ipv6BootProtocolEditor.setWidgetTooltip(constants.cloudInitNetworkIpv6BootProtocolToolTip());
        networkIpv6AddressEditor.setWidgetTooltip(constants.cloudInitNetworkIpv6AddressToolTip());
        networkIpv6PrefixEditor.setWidgetTooltip(constants.cloudInitNetworkIpv6PrefixToolTip());
        networkIpv6GatewayEditor.setWidgetTooltip(constants.cloudInitNetworkIpv6GatewayToolTip());

        networkStartOnBootEditor.setWidgetTooltip(constants.cloudInitNetworkStartOnBootToolTip());
        dnsServers.setWidgetTooltip(constants.cloudInitDnsServersToolTip());
        dnsSearchDomains.setWidgetTooltip(constants.cloudInitDnsSearchDomainsToolTip());

        networkExpander.setTitleWhenExpanded(constants.cloudInitNetworskLabel());
        networkExpander.setTitleWhenCollapsed(constants.cloudInitNetworskLabel());

        authenticationExpander.setTitleWhenExpanded(constants.cloudInitAuthenticationLabel());
        authenticationExpander.setTitleWhenCollapsed(constants.cloudInitAuthenticationLabel());

        customScriptExpander.setTitleWhenExpanded(constants.customScriptLabel());
        customScriptExpander.setTitleWhenCollapsed(constants.customScriptLabel());

        sysprepScriptExpander.setTitleWhenExpanded(constants.sysprepLabel());
        sysprepScriptExpander.setTitleWhenCollapsed(constants.sysprepLabel());

        sysprepPasswordExpander.setTitleWhenExpanded(constants.sysprepAdminPasswordLabel());
        sysprepPasswordExpander.setTitleWhenCollapsed(constants.sysprepAdminPasswordLabel());

        sysprepInputsExpander.setTitleWhenExpanded(constants.customLocaleLabel());
        sysprepInputsExpander.setTitleWhenCollapsed(constants.customLocaleLabel());
    }

    void addStyles() {
        networkListEditor.hideLabel();
        setNetworkDetailsStyle(false);
        setNetworkIpv4StaticDetailsStyle(false);
        setNetworkIpv6StaticDetailsStyle(false);

        windowsSyspreptimeZoneEnabledEditor.addStyleName(customizableStyle.primaryOption());
        sysprepDomainEditor.addStyleName(customizableStyle.primaryOption());

        ipv4BootProtocolEditor.addStyleName(customizableStyle.primaryOption());
        networkIpv4AddressEditor.addStyleName(customizableStyle.primaryOption());
        networkIpv4NetmaskEditor.addStyleName(customizableStyle.primaryOption());
        networkIpv4GatewayEditor.addStyleName(customizableStyle.primaryOption());

        ipv6BootProtocolEditor.addStyleName(customizableStyle.primaryOption());
        networkIpv6AddressEditor.addStyleName(customizableStyle.primaryOption());
        networkIpv6PrefixEditor.addStyleName(customizableStyle.primaryOption());
        networkIpv6GatewayEditor.addStyleName(customizableStyle.primaryOption());

        networkStartOnBootEditor.addStyleName(customizableStyle.primaryOption());

        windowsSysprepTimeZoneEditor.addStyleName(customizableStyle.primaryOption());
        inputLocaleEditor.addStyleName(customizableStyle.primaryOption());
        uiLanguageEditor.addStyleName(customizableStyle.primaryOption());
        activeDirectoryOUEditor.addStyleName(customizableStyle.primaryOption());
        systemLocaleEditor.addStyleName(customizableStyle.primaryOption());
        userLocaleEditor.addStyleName(customizableStyle.primaryOption());
        userNameEditor.addStyleName(customizableStyle.primaryOption());
        hostnameEditor.addStyleName(customizableStyle.primaryOption());
        windowsHostnameEditor.addStyleName(customizableStyle.primaryOption());
        sysprepOrgNameEditor.addStyleName(customizableStyle.primaryOption());
        timeZoneEnabledEditor.addStyleName(customizableStyle.primaryOption());
        timeZoneEditor.addStyleName(customizableStyle.primaryOption());
        cloudInitRootPasswordEditor.addStyleName(customizableStyle.primaryOption());
        cloudInitRootPasswordVerificationEditor.addStyleName(customizableStyle.primaryOption());
        sysprepAdminPasswordEditor.addStyleName(customizableStyle.primaryOption());
        sysprepAdminPasswordVerificationEditor.addStyleName(customizableStyle.primaryOption());
        authorizedKeysEditor.addStyleName(customizableStyle.primaryOption());
        cloudInitPasswordSetEditor.addStyleName(customizableStyle.primaryOption());
        sysprepPasswordSetEditor.addStyleName(customizableStyle.primaryOption());
        regenerateKeysEnabledEditor.addStyleName(customizableStyle.primaryOption());
        authenticationExpanderContent.addStyleName(customizableStyle.expanderContent());
        customScriptExpanderContent.addStyleName(customizableStyle.expanderContent());
        sysprepScriptExpanderContent.addStyleName(customizableStyle.expanderContent());
        sysprepPasswordExpanderContent.addStyleName(customizableStyle.expanderContent());
        sysprepInputsExpanderContent.addStyleName(customizableStyle.expanderContent());
        networkExpanderContent.addStyleName(customizableStyle.expanderContent());
    }

    public void setUsePatternFly(boolean use) {
        windowsHostnameEditorWithInfo.setUsePatternFly(use);
        sysprepDomainEditor.setUsePatternFly(use);
        sysprepOrgNameEditor.setUsePatternFly(use);
        activeDirectoryOUEditor.setUsePatternFly(use);
        windowsSyspreptimeZoneEnabledEditor.setUsePatternFly(use);
        windowsSysprepTimeZoneEditor.setUsePatternFly(use);
        sysprepPasswordSetEditor.setUsePatternFly(use);
        sysprepAdminPasswordEditor.setUsePatternFly(use);
        sysprepAdminPasswordVerificationEditor.setUsePatternFly(use);
        inputLocaleEditor.setUsePatternFly(use);
        uiLanguageEditor.setUsePatternFly(use);
        systemLocaleEditor.setUsePatternFly(use);
        userLocaleEditor.setUsePatternFly(use);
        sysprepScriptEditor.setUsePatternFly(use);
        hostnameEditor.setUsePatternFly(use);
        timeZoneEnabledEditor.setUsePatternFly(use);
        timeZoneEditor.setUsePatternFly(use);
        userNameEditor.setUsePatternFly(use);
        cloudInitPasswordSetEditor.setUsePatternFly(use);
        cloudInitRootPasswordEditor.setUsePatternFly(use);
        cloudInitRootPasswordVerificationEditor.setUsePatternFly(use);
        authorizedKeysEditor.setUsePatternFly(use);
        regenerateKeysEnabledEditor.setUsePatternFly(use);
        dnsServers.setUsePatternFly(use);
        dnsSearchDomains.setUsePatternFly(use);
        networkEnabledEditor.setUsePatternFly(use);
        networkStartOnBootEditor.setUsePatternFly(use);
        if (use) {
            authenticationExpanderContent.removeStyleName(customizableStyle.expanderContent());
            customScriptExpanderContent.removeStyleName(customizableStyle.expanderContent());
            sysprepScriptExpanderContent.removeStyleName(customizableStyle.expanderContent());
            sysprepPasswordExpanderContent.removeStyleName(customizableStyle.expanderContent());
            sysprepInputsExpanderContent.removeStyleName(customizableStyle.expanderContent());
            networkExpanderContent.removeStyleName(customizableStyle.expanderContent());
            windowsSyspreptimeZoneEnabledEditor.removeStyleName(customizableStyle.primaryOption());
            sysprepDomainEditor.removeStyleName(customizableStyle.primaryOption());

            ipv4BootProtocolEditor.removeStyleName(customizableStyle.primaryOption());
            networkIpv4AddressEditor.removeStyleName(customizableStyle.primaryOption());
            networkIpv4NetmaskEditor.removeStyleName(customizableStyle.primaryOption());
            networkIpv4GatewayEditor.removeStyleName(customizableStyle.primaryOption());

            ipv6BootProtocolEditor.removeStyleName(customizableStyle.primaryOption());
            networkIpv6AddressEditor.removeStyleName(customizableStyle.primaryOption());
            networkIpv6PrefixEditor.removeStyleName(customizableStyle.primaryOption());
            networkIpv6GatewayEditor.removeStyleName(customizableStyle.primaryOption());

            networkStartOnBootEditor.removeStyleName(customizableStyle.primaryOption());

            windowsSysprepTimeZoneEditor.removeStyleName(customizableStyle.primaryOption());
            inputLocaleEditor.removeStyleName(customizableStyle.primaryOption());
            uiLanguageEditor.removeStyleName(customizableStyle.primaryOption());
            activeDirectoryOUEditor.removeStyleName(customizableStyle.primaryOption());
            systemLocaleEditor.removeStyleName(customizableStyle.primaryOption());
            userLocaleEditor.removeStyleName(customizableStyle.primaryOption());
            userNameEditor.removeStyleName(customizableStyle.primaryOption());
            hostnameEditor.removeStyleName(customizableStyle.primaryOption());
            windowsHostnameEditor.removeStyleName(customizableStyle.primaryOption());
            sysprepOrgNameEditor.removeStyleName(customizableStyle.primaryOption());
            timeZoneEnabledEditor.removeStyleName(customizableStyle.primaryOption());
            timeZoneEditor.removeStyleName(customizableStyle.primaryOption());
            cloudInitRootPasswordEditor.removeStyleName(customizableStyle.primaryOption());
            cloudInitRootPasswordVerificationEditor.removeStyleName(customizableStyle.primaryOption());
            sysprepAdminPasswordEditor.removeStyleName(customizableStyle.primaryOption());
            sysprepAdminPasswordVerificationEditor.removeStyleName(customizableStyle.primaryOption());
            authorizedKeysEditor.removeStyleName(customizableStyle.primaryOption());
            cloudInitPasswordSetEditor.removeStyleName(customizableStyle.primaryOption());
            sysprepPasswordSetEditor.removeStyleName(customizableStyle.primaryOption());
            regenerateKeysEnabledEditor.removeStyleName(customizableStyle.primaryOption());
        } else {
            sysprepScriptEditor.setContentWidgetContainerStyleName(customizableStyle.customScript());
        }
    }

    /* Controls style for network options based on network selection */
    private void setNetworkDetailsStyle(boolean enabled) {
        networkNameEditor.setEnabled(enabled);
        networkListEditor.setEnabled(enabled);
        setLabelEnabled(networkLabelSepAddRemove, enabled);
        setLabelEnabled(networkRemoveLabel, enabled);
        networkRemoveButton.setEnabled(enabled);
        networkOptions.setVisible(enabled);
    }

    /* Sets visibility for static networking options */
    private void setNetworkIpv4StaticDetailsStyle(boolean visible) {
        networkIpv4AddressEditor.setVisible(visible);
        networkIpv4NetmaskEditor.setVisible(visible);
        networkIpv4GatewayEditor.setVisible(visible);
    }

    /* Sets visibility for static networking options */
    private void setNetworkIpv6StaticDetailsStyle(boolean visible) {
        networkIpv6AddressEditor.setVisible(visible);
        networkIpv6PrefixEditor.setVisible(visible);
        networkIpv6GatewayEditor.setVisible(visible);
    }

    private void setLabelEnabled(Label label, boolean enabled) {
        label.getElement().getStyle().setColor(enabled ? "#000000" : "#999999"); //$NON-NLS-1$ //$NON-NLS-2$
    }


    @Override
    public void edit(final VmInitModel model) {
        driver.edit(model);

        initializeEnabledCBBehavior(model);

        networkAddButton.addClickHandler(event -> model.getAddNetworkCommand().execute());

        networkRemoveButton.addClickHandler(event -> model.getRemoveNetworkCommand().execute());

        model.getNetworkList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            // Can't use ListModel.isEmpty() because ListModel.SetItems(<empty list>)) will
            // cause the ItemsChanged and SelectedItemChanged events to be fired before we
            // can update the isEmpty() flag, causing erroneous readings upon item removal.
            setNetworkDetailsStyle(model.getNetworkList().getSelectedItem() != null);
        });

        model.getIpv4BootProtocolList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> setNetworkIpv4StaticDetailsStyle(model.getIpv4BootProtocolList().getSelectedItem() != null
                && model.getIpv4BootProtocolList().getSelectedItem() == Ipv4BootProtocol.STATIC_IP));

        model.getIpv6BootProtocolList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> setNetworkIpv6StaticDetailsStyle(model.getIpv6BootProtocolList().getSelectedItem() != null
                && model.getIpv6BootProtocolList().getSelectedItem() == Ipv6BootProtocol.STATIC_IP));

        model.getCloudInitPasswordSet().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("IsChangable".equals(propName)) { //$NON-NLS-1$
                cloudInitPasswordSetEditor.setWidgetTooltip(
                        model.getCloudInitPasswordSet().getIsChangable() ?
                        constants.vmInitPasswordSetToolTip() : constants.vmInitPasswordNotSetToolTip()
                );
            }
        });

        model.getSysprepPasswordSet().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("IsChangable".equals(propName)) { //$NON-NLS-1$
                sysprepPasswordSetEditor.setWidgetTooltip(
                        model.getSysprepPasswordSet().getIsChangable() ?
                        constants.vmInitPasswordSetToolTip() : constants.vmInitPasswordNotSetToolTip()
                );
            }
        });

    }

    void initializeEnabledCBBehavior(final VmInitModel model) {
        if (model.getRegenerateKeysEnabled().getEntity() != null) {
            regenerateKeysEnabledEditor.setEnabled(model.getRegenerateKeysEnabled().getEntity());
        }

        if (model.getTimeZoneEnabled().getEntity() != null) {
            timeZoneEnabledEditor.setEnabled(model.getTimeZoneEnabled().getEntity());
        }
        model.getWindowsSysprepTimeZoneEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> windowsSysprepTimeZoneEditor.setEnabled(model.getWindowsSysprepTimeZoneEnabled().getEntity()));

        if (model.getWindowsSysprepTimeZoneEnabled().getEntity() != null) {
            windowsSysprepTimeZoneEditor.setEnabled(model.getWindowsSysprepTimeZoneEnabled().getEntity());
        }
        model.getTimeZoneEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> timeZoneEditor.setEnabled(model.getTimeZoneEnabled().getEntity()));

        if (model.getNetworkEnabled().getEntity() != null) {
            networkEnabledEditor.setEnabled(model.getNetworkEnabled().getEntity());
        }
        model.getNetworkEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> {
            boolean enabled = model.getNetworkEnabled().getEntity();
            networkAddButton.setEnabled(enabled);
            setLabelEnabled(networkAddLabel, enabled);
            // See note above re: parameter to this method call
            setNetworkDetailsStyle(enabled && model.getNetworkList().getSelectedItem() != null);
        });

    }

    public void setCloudInitContentVisible(boolean visible) {
        cloudInitOptionsContent.setVisible(visible);
        if (visible) {
            showCloudInitFields();
        }
    }

    public void setIgnitionContentVisible(boolean visible) {
        cloudInitOptionsContent.setVisible(visible);
        if (visible) {
            showIgnitionFields();
        }
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

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}

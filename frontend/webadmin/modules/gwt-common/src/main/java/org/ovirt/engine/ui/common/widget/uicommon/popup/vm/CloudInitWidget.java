package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import java.util.Map;

import org.ovirt.engine.core.common.action.CloudInitParameters.Attachment;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.ComboBox;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextAreaEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.CloudInitModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class CloudInitWidget extends AbstractModelBoundPopupWidget<CloudInitModel> {

    interface Driver extends SimpleBeanEditorDriver<CloudInitModel, CloudInitWidget> {
    }

    interface ViewUiBinder extends UiBinder<Widget, CloudInitWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<CloudInitWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);

    interface Style extends CssResource {
        String displayNone();
    }

    @UiField
    Style style;

    @UiField(provided = true)
    @Path(value = "hostnameEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor hostnameEnabledEditor;

    @UiField
    @Path(value = "hostname.entity")
    @WithElementId
    EntityModelTextBoxEditor hostnameEditor;


    @UiField(provided = true)
    @Path(value = "authorizedKeysEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor authorizedKeysEnabledEditor;

    @UiField
    @Path(value = "authorizedKeys.entity")
    @WithElementId
    EntityModelTextAreaEditor authorizedKeysEditor;


    @UiField(provided = true)
    @Path(value = "regenerateKeysEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor regenerateKeysEnabledEditor;


    @UiField(provided = true)
    @Path(value = "timeZoneEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor timeZoneEnabledEditor;

    @UiField(provided = true)
    @Path(value = "timeZoneList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> timeZoneEditor;


    @UiField(provided = true)
    @Path(value = "rootPasswordEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor rootPasswordEnabledEditor;

    @UiField
    @Path(value = "rootPassword.entity")
    @WithElementId
    EntityModelPasswordBoxEditor rootPasswordEditor;

    @UiField
    @Path(value = "rootPasswordVerification.entity")
    @WithElementId
    EntityModelPasswordBoxEditor rootPasswordVerificationEditor;


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


    @UiField(provided = true)
    @Path(value = "attachmentEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor attachmentEnabledEditor;

    @Path(value = "attachmentSelectedPath.entity")
    @WithElementId
    EntityModelTextBoxEditor attachmentPathEditor;

    @Path(value = "attachmentList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> attachmentListEditor;

    @UiField(provided = true)
    @WithElementId
    ComboBox attachmentComboBox;

    @UiField
    @Ignore
    Label attachmentSelectLabel;

    @UiField
    @Ignore
    Label attachmentLabelSepSelectAdd;

    @UiField
    PushButton attachmentAddButton;

    @UiField
    @Ignore
    Label attachmentAddLabel;

    @UiField
    @Ignore
    Label attachmentLabelSepAddRemove;

    @UiField
    PushButton attachmentRemoveButton;

    @UiField
    @Ignore
    Label attachmentRemoveLabel;

    @UiField
    @Ignore
    FlowPanel attachmentOptions;

    @UiField(provided = true)
    @Path(value = "attachmentType.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> attachmentTypeEditor;

    @UiField
    @Path(value = "attachmentContent.entity")
    @WithElementId
    EntityModelTextAreaEditor attachmentContentEditor;


    public CloudInitWidget() {
        initCheckBoxEditors();
        initListBoxEditors();
        initComboBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        localize();
        addStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);

        driver.initialize(this);
    }

    void initCheckBoxEditors() {
        hostnameEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        authorizedKeysEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        regenerateKeysEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        timeZoneEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        rootPasswordEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        networkEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        networkDhcpEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        networkStartOnBootEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachmentEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
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

        attachmentTypeEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                @SuppressWarnings("unchecked")
                Map.Entry<Attachment.AttachmentType, String> entry
                        = (Map.Entry<Attachment.AttachmentType, String>) object;
                return entry.getValue();
            }
        });
    }

    void initComboBoxEditors() {
        networkListEditor = new ListModelListBoxEditor<Object>();
        networkNameEditor = new EntityModelTextBoxEditor();
        networkComboBox = new ComboBox(networkListEditor, networkNameEditor);

        attachmentListEditor = new ListModelListBoxEditor<Object>();
        attachmentPathEditor = new EntityModelTextBoxEditor();
        attachmentComboBox = new ComboBox(attachmentListEditor, attachmentPathEditor);
    }


    void localize() {
        hostnameEnabledEditor.setLabel(constants.cloudInitHostnameLabel());
        authorizedKeysEnabledEditor.setLabel(constants.cloudInitAuthorizedKeysLabel());
        regenerateKeysEnabledEditor.setLabel(constants.cloudInitRegenerateKeysLabel());
        timeZoneEnabledEditor.setLabel(constants.cloudInitTimeZoneLabel());
        rootPasswordEnabledEditor.setLabel(constants.cloudInitRootPasswordLabel());
        rootPasswordVerificationEditor.setLabel(constants.cloudInitRootPasswordVerificationLabel());
        networkEnabledEditor.setLabel(constants.cloudInitNetworkLabel());
        attachmentEnabledEditor.setLabel(constants.cloudInitAttachmentLabel());

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

        attachmentSelectLabel.setText(constants.cloudInitAttachmentSelectLabel());
        attachmentLabelSepSelectAdd.setText(sep);
        attachmentAddLabel.setText(constants.cloudInitObjectAddLabel());
        attachmentLabelSepAddRemove.setText(sep);
        attachmentRemoveLabel.setText(constants.cloudInitObjectRemoveLabel());

        attachmentTypeEditor.setLabel(constants.cloudInitAttachmentTypeLabel());
        attachmentContentEditor.setLabel(constants.cloudInitAttachmentContentLabel());

        hostnameEditor.setTitle(constants.cloudInitHostnameToolTip());
        authorizedKeysEditor.setTitle(constants.cloudInitAuthorizedKeysToolTip());
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

        attachmentListEditor.setTitle(constants.cloudInitAttachmentToolTip());
        attachmentPathEditor.setTitle(constants.cloudInitAttachmentToolTip());
        attachmentTypeEditor.setTitle(constants.cloudInitAttachmentTypeToolTip());
        /* attachmentContentEditor tool-tip set below based on attachment type */
    }

    void addStyles() {
        networkListEditor.addLabelStyleName(style.displayNone());
        setNetworkDetailsStyle(false);
        setNetworkStaticDetailsStyle(false);

        attachmentListEditor.addLabelStyleName(style.displayNone());
        setAttachmentDetailsStyle(false);
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

    /* Controls style for attachment options based on attachment selection */
    private void setAttachmentDetailsStyle(boolean enabled) {
        attachmentPathEditor.setEnabled(enabled);
        attachmentListEditor.setEnabled(enabled);
        setLabelEnabled(attachmentSelectLabel, enabled);
        setLabelEnabled(attachmentLabelSepSelectAdd, enabled);
        setLabelEnabled(attachmentLabelSepAddRemove, enabled);
        setLabelEnabled(attachmentRemoveLabel, enabled);
        attachmentRemoveButton.setEnabled(enabled);
        attachmentOptions.setVisible(enabled);
    }

    private void setLabelEnabled(Label label, boolean enabled) {
        label.getElement().getStyle().setColor(enabled ? "#000000" : "#999999"); //$NON-NLS-1$ //$NON-NLS-2$
    }


    @Override
    public void edit(final CloudInitModel model) {
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

        attachmentAddButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.getAddAttachmentCommand().execute();
            }
        });

        attachmentRemoveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.getRemoveAttachmentCommand().execute();
            }
        });

        model.getAttachmentList().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                // See note above regarding parameter to setNetworkDetailsStyle
                setAttachmentDetailsStyle(model.getAttachmentList().getSelectedItem() != null);
            }
        });

        model.getAttachmentType().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                setAttachmentContentToolTip(model);
            }
        });
        setAttachmentContentToolTip(model);
    }

    private void setAttachmentContentToolTip(final CloudInitModel model) {
        @SuppressWarnings("unchecked")
        Map.Entry<Attachment.AttachmentType, String> entry
                = (Map.Entry<Attachment.AttachmentType, String>) model.getAttachmentType().getSelectedItem();
        if (entry != null && entry.getKey() == Attachment.AttachmentType.BASE64) {
            attachmentContentEditor.setTitle(constants.cloudInitAttachmentContentBase64ToolTip());
        } else {
            attachmentContentEditor.setTitle(constants.cloudInitAttachmentContentTextToolTip());
        }
    }

    void initializeEnabledCBBehavior(final CloudInitModel model) {
        // Initialize default checkbox state and add event listeners for user-initiated changes
        if (model.getHostnameEnabled().getEntity() != null) {
            hostnameEnabledEditor.setEnabled((Boolean) model.getHostnameEnabled().getEntity());
        }
        model.getHostnameEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                hostnameEditor.setEnabled((Boolean) model.getHostnameEnabled().getEntity());
            }
        });

        if (model.getAuthorizedKeysEnabled().getEntity() != null) {
            authorizedKeysEnabledEditor.setEnabled((Boolean) model.getAuthorizedKeysEnabled().getEntity());
        }
        model.getAuthorizedKeysEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
            authorizedKeysEditor.setEnabled((Boolean) model.getAuthorizedKeysEnabled().getEntity());
            }
        });

        if (model.getRegenerateKeysEnabled().getEntity() != null) {
            regenerateKeysEnabledEditor.setEnabled((Boolean) model.getRegenerateKeysEnabled().getEntity());
        }

        if (model.getTimeZoneEnabled().getEntity() != null) {
            timeZoneEnabledEditor.setEnabled((Boolean) model.getTimeZoneEnabled().getEntity());
        }
        model.getTimeZoneEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                timeZoneEditor.setEnabled((Boolean) model.getTimeZoneEnabled().getEntity());
            }
        });

        if (model.getRootPasswordEnabled().getEntity() != null) {
            rootPasswordEnabledEditor.setEnabled((Boolean) model.getRootPasswordEnabled().getEntity());
        }
        model.getRootPasswordEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                rootPasswordEditor.setEnabled((Boolean) model.getRootPasswordEnabled().getEntity());
                rootPasswordVerificationEditor.setEnabled((Boolean) model.getRootPasswordEnabled().getEntity());
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

        if (model.getAttachmentEnabled().getEntity() != null) {
            attachmentEnabledEditor.setEnabled((Boolean) model.getAttachmentEnabled().getEntity());
        }
        model.getAttachmentEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean enabled = (Boolean) model.getAttachmentEnabled().getEntity();
                attachmentAddButton.setEnabled(enabled);
                setLabelEnabled(attachmentAddLabel, enabled);
                // See note above re: parameter to this method call
                setAttachmentDetailsStyle(enabled && model.getAttachmentList().getSelectedItem() != null);
            }
        });
    }


    @Override
    public CloudInitModel flush() {
        return driver.flush();
    }
}

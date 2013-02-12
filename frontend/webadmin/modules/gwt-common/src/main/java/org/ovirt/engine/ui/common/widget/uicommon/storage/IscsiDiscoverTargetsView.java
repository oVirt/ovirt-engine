package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.FocusComposite;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IscsiDiscoverTargetsView extends FocusComposite implements HasEditorDriver<SanStorageModelBase>, HasKeyPressHandlers {

    interface Driver extends SimpleBeanEditorDriver<SanStorageModelBase, IscsiDiscoverTargetsView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, IscsiDiscoverTargetsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    HorizontalPanel discoverTargetsLabelPanel;

    @UiField
    @Ignore
    ToggleButton discoverTargetsImageButton;

    @UiField
    VerticalPanel discoverTargetsPanel;

    @UiField
    FlowPanel discoverTargetsPanelInner;

    @UiField
    @Path(value = "address.entity")
    EntityModelTextBoxEditor addressEditor;

    @UiField
    @Path(value = "port.entity")
    EntityModelTextBoxEditor portEditor;

    @UiField(provided = true)
    @Path(value = "useUserAuth.entity")
    EntityModelCheckBoxEditor useUserAuthEditor;

    @UiField
    @Path(value = "userName.entity")
    EntityModelTextBoxEditor chapUserEditor;

    @UiField
    @Path(value = "password.entity")
    EntityModelTextBoxEditor chapPassEditor;

    @UiField
    UiCommandButton discoverButton;

    @UiField
    UiCommandButton loginAllButton;

    @UiField
    @Ignore
    Label messageLabel;

    protected static CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);
    protected static CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);
    protected static CommonApplicationTemplates templates = GWT.create(CommonApplicationTemplates.class);

    @Inject
    public IscsiDiscoverTargetsView() {
        initCheckBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        addStyles(templates, constants, resources);
        Driver.driver.initialize(this);

        addFocusWidget(addressEditor.asValueBox());
        addFocusWidget(portEditor.asValueBox());
        addFocusWidget(chapUserEditor.asValueBox());
        addFocusWidget(chapPassEditor.asValueBox());
        addFocusWidget(useUserAuthEditor.asCheckBox());
    }

    void initCheckBoxEditors() {
        useUserAuthEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void addStyles(CommonApplicationTemplates templates,
            CommonApplicationConstants constants, CommonApplicationResources resources) {
        useUserAuthEditor.addLabelStyleName(style.userAuthLabel());
        addressEditor.addContentWidgetStyleName(style.textBox());
        portEditor.addContentWidgetStyleName(style.textBox());
        chapUserEditor.addContentWidgetStyleName(style.chapBox());
        chapPassEditor.addContentWidgetStyleName(style.chapBox());
        chapUserEditor.addLabelStyleName(style.chapLabel());
        chapPassEditor.addLabelStyleName(style.chapLabel());

        discoverTargetsImageButton.setStylePrimaryName("discoverTargetsButton"); //$NON-NLS-1$

        SafeHtml expanderImage = SafeHtmlUtils
                .fromTrustedString(AbstractImagePrototype.create(
                        resources.expanderImage()).getHTML());
        SafeHtml expanderDownImage = SafeHtmlUtils
                .fromTrustedString(AbstractImagePrototype.create(
                        resources.expanderDownImage()).getHTML());

        discoverTargetsImageButton.getUpFace().setHTML(
                templates.imageTextButton(expanderImage,
                        constants.storageIscsiDiscoverTargetsLabel()));
        discoverTargetsImageButton.getDownFace().setHTML(
                templates.imageTextButton(expanderDownImage,
                        constants.storageIscsiDiscoverTargetsLabel()));
    }

    void localize(CommonApplicationConstants constants) {
        addressEditor.setLabel(constants.storageIscsiPopupAddressLabel());
        portEditor.setLabel(constants.storageIscsiPopupPortLabel());
        useUserAuthEditor.setLabel(constants.storageIscsiPopupUserAuthLabel());
        chapUserEditor.setLabel(constants.storageIscsiPopupChapUserLabel());
        chapPassEditor.setLabel(constants.storageIscsiPopupChapPassLabel());
        discoverButton.setLabel(constants.storageIscsiPopupDiscoverButtonLabel());
        loginAllButton.setLabel(constants.storageIscsiPopupLoginAllButtonLabel());
    }

    private void setProposeDiscover(boolean propose) {
        discoverTargetsImageButton.setDown(propose);
        discoverTargetsPanelInner.setVisible(propose);
        discoverTargetsPanel.setStyleName(propose ?
                style.expandedDiscoverTargetsPanel() : style.collapsedDiscoverTargetsPanel());
    }

    @Override
    public void edit(final SanStorageModelBase object) {
        Driver.driver.edit(object);

        // Handle property change event
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if (propName.equals("ProposeDiscoverTargets")) { //$NON-NLS-1$
                    setProposeDiscover(object.getProposeDiscoverTargets());
                }
                else if (propName.equals("Message")) { //$NON-NLS-1$
                    messageLabel.setText(object.getMessage());
                }
            }
        });

        // Handle key press event
        addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                    object.getDiscoverTargetsCommand().Execute();
                    setIsFocused(false);
                }
            }
        });

        setProposeDiscover(object.getProposeDiscoverTargets());
        initButtons(object);
    }

    void initButtons(final SanStorageModelBase object) {
        discoverButton.setCommand(object.getDiscoverTargetsCommand());
        loginAllButton.setCommand(object.getLoginAllCommand());

        discoverTargetsPanelInner.setVisible(discoverTargetsImageButton.isDown());
        discoverTargetsImageButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                object.setProposeDiscoverTargets(discoverTargetsImageButton.isDown());
            }
        });

        discoverButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                discoverButton.getCommand().Execute();
            }
        });

        loginAllButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loginAllButton.getCommand().Execute();
            }
        });
    }

    public void setEnabled(boolean enabled) {
        Visibility visibility = enabled ? Visibility.VISIBLE : Visibility.HIDDEN;
        discoverTargetsLabelPanel.getElement().getStyle().setVisibility(visibility);
    }

    @Override
    public SanStorageModelBase flush() {
        return Driver.driver.flush();
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return addDomHandler(handler, KeyPressEvent.getType());
    }

    public boolean isDiscoverPanelFocused() {
        return isFocused();
    }

    interface WidgetStyle extends CssResource {

        String textBox();

        String chapBox();

        String chapLabel();

        String userAuthLabel();

        String collapsedDiscoverTargetsPanel();

        String expandedDiscoverTargetsPanel();
    }

}

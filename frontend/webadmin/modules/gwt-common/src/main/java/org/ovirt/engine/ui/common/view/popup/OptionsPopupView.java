package org.ovirt.engine.ui.common.view.popup;

import static org.ovirt.engine.ui.uicommonweb.models.options.OptionsModel.RESET_SETTINGS;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.place.ApplicationPlaceManager;
import org.ovirt.engine.ui.common.section.main.presenter.OptionsPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.LeftAlignedUiCommandButton;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextArea;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.models.options.EditOptionsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class OptionsPopupView extends AbstractModelBoundPopupView<EditOptionsModel> implements OptionsPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<OptionsPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Driver extends UiCommonEditorDriver<EditOptionsModel, OptionsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, OptionsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @UiField
    @Path(value = "publicKey.entity")
    StringEntityModelTextArea publicKeyEditor;

    @UiField
    InfoIcon consolePublicKeyInfoIcon;

    @UiField
    InfoIcon localStoragePersistedOnServerInfoIcon;

    @UiField(provided = true)
    @Path(value = "localStoragePersistedOnServer.entity")
    EntityModelCheckBoxEditor localStoragePersistedOnServerCheckBox;

    @UiField(provided = true)
    @Path(value = "confirmSuspendingVm.entity")
    EntityModelCheckBoxEditor confirmSuspendingVmCheckBox;

    @UiField
    DialogTabPanel tabPanel;

    @UiField
    DialogTab generalTab;

    @UiField
    DialogTab confirmationTab;

    @UiField
    @Path(value = "userName.entity")
    Label userName;

    @UiField
    @Path(value = "email.entity")
    Label email;

    @UiField(provided = true)
    @Ignore
    public EntityModelRadioButtonEditor isHomePageDefault;

    @UiField(provided = true)
    @Path(value = "isHomePageCustom.entity")
    public EntityModelRadioButtonEditor isHomePageCustom;

    @UiField
    InfoIcon isHomePageCustomInfo;

    @UiField
    @Path(value = "customHomePage.entity")
    StringEntityModelTextArea customHomePage;

    @UiField (provided = true)
    @Ignore
    public EntityModelRadioButtonEditor consoleNativeRadioButton;

    @UiField (provided = true)
    @Ignore
    public EntityModelRadioButtonEditor consoleNoVncRadioButton;

    @UiField (provided = true)
    @Ignore
    public EntityModelRadioButtonEditor consoleDefaultRadioButton;

    @Inject
    public OptionsPopupView(EventBus eventBus, PlaceManager manager) {
        super(eventBus);

        localStoragePersistedOnServerCheckBox = new EntityModelCheckBoxEditor(Align.RIGHT);
        confirmSuspendingVmCheckBox = new EntityModelCheckBoxEditor(Align.RIGHT);
        isHomePageCustom = new EntityModelRadioButtonEditor("homePage", Align.RIGHT); // $NON-NLS-1$
        isHomePageDefault = new EntityModelRadioButtonEditor("homePage", Align.RIGHT); // $NON-NLS-1$

        consoleNativeRadioButton = new EntityModelRadioButtonEditor("Vnc", Align.RIGHT); // $NON-NLS-1$
        consoleNativeRadioButton.setLabel(constants.nativeClient());
        consoleNoVncRadioButton = new EntityModelRadioButtonEditor("Vnc", Align.RIGHT); // $NON-NLS-1$
        consoleNoVncRadioButton.setLabel(constants.noVnc());
        consoleDefaultRadioButton = new EntityModelRadioButtonEditor("Vnc", Align.RIGHT); // $NON-NLS-1$
        consoleDefaultRadioButton.setLabel(constants.defaultClient());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        consolePublicKeyInfoIcon.setText(SafeHtmlUtils.fromString(constants.consolePublicKeyMessage()));
        localStoragePersistedOnServerInfoIcon.setText(createTooltipForGridSettings());
        isHomePageCustomInfo.setText(createTooltipForHomePage());
        isHomePageDefault.setLabel(
                messages.homePageDefault("#" + // $NON-NLS-1$
                        ((ApplicationPlaceManager) manager).getDefaultPlace().getNameToken()));

        driver.initialize(this);
    }

    private SafeHtml createTooltipForHomePage() {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(SafeHtmlUtils.fromString(constants.homePageFormat()));
        builder.append(SafeHtmlUtils.fromSafeConstant(constants.lineBreak()));
        builder.append(SafeHtmlUtils.fromSafeConstant(constants.lineBreak()));
        builder.append(SafeHtmlUtils.fromString(constants.homePageIgnoreInvalid()));
        return builder.toSafeHtml();
    }

    private SafeHtml createTooltipForGridSettings() {
        SafeHtmlBuilder listItemBuilder = new SafeHtmlBuilder();
        listItemBuilder.append(templates.listItem(SafeHtmlUtils.fromString(constants.hideDisplayColumns())));
        listItemBuilder.append(templates.listItem(SafeHtmlUtils.fromString(constants.swapColumns())));

        SafeHtmlBuilder tooltipBuilder = new SafeHtmlBuilder();
        tooltipBuilder.append(templates.text(constants.persistGridSettingsOnServerTooltip()));
        tooltipBuilder.append(templates.unorderedList(listItemBuilder.toSafeHtml()));

        return tooltipBuilder.toSafeHtml();
    }

    private DelegateProvider createDelegateFor(StringEntityModelTextArea textArea) {
        return new DelegateProvider() {
            @Override
            public HasChangeHandlers asHasChangeHandlers() {
                return textArea;
            }

            @Override
            public HasValue<String> asHasValue() {
                return textArea;
            }

            @Override
            public HasText asHasText() {
                return textArea;
            }
        };
    }

    @Override
    public DelegateProvider getPublicKeyEditor() {
        return createDelegateFor(publicKeyEditor);
    }

    private void toggleHomePage(EditOptionsModel model) {
        boolean isHomePageCustom = Boolean.TRUE.equals(model.getIsHomePageCustom().getEntity());
        customHomePage.setEnabled(isHomePageCustom);
    }

    @Override
    public void edit(final EditOptionsModel model) {
        driver.edit(model);
        boolean isHomePageCustom = Boolean.TRUE.equals(model.getIsHomePageCustom().getEntity());
        this.isHomePageCustom.asRadioButton().setValue(isHomePageCustom);
        isHomePageDefault.asRadioButton().setValue(!isHomePageCustom);
        toggleHomePage(model);
    }

    @Override
    public EditOptionsModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    protected UiCommandButton createCommandButton(String label, String uniqueId) {
        if (RESET_SETTINGS.equals(uniqueId)) {
            return new LeftAlignedUiCommandButton(label);
        }

        return super.createCommandButton(label, uniqueId);
    }

    @Override
    public void init(EditOptionsModel model) {
        super.init(model);
        // resettable fields require  field <-> label mapping to display a human-readable confirmation
        // since the mapping is stored in the model we need to propagate labels from model to the view
        confirmSuspendingVmCheckBox.setLabel(model.getConfirmSuspendingVm().getTitle());
        localStoragePersistedOnServerCheckBox.setLabel(model.getLocalStoragePersistedOnServer().getTitle());
        isHomePageCustom.setLabel(model.getIsHomePageCustom().getTitle());
    }

    @Override
    public IEventListener<? super PropertyChangedEventArgs> createHomePageListener(EditOptionsModel model) {
        return (event, sender, args) -> toggleHomePage(model);
    }

    @Override
    public HasValueChangeHandlers<Boolean> getHomePageDefaultSwitch() {
        return isHomePageDefault.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getConsoleVncNativeRadioButton() {
        return consoleNativeRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getConsoleNoVncRadioButton() {
        return consoleNoVncRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getConsoleDefaultRadioButton() {
        return consoleDefaultRadioButton.asRadioButton();
    }

    @Override
    public void setConsoleNoVncSelected(boolean selected, ConsoleOptionsFrontendPersister consoleOptionsPersister, EditOptionsModel model) {
        consoleNoVncRadioButton.asRadioButton().setValue(selected);
        if (selected) {
            storeSelectedConsole(VncConsoleModel.ClientConsoleMode.NoVnc.toString(), consoleOptionsPersister, model);
        }
    }

    @Override
    public void setConsoleVncNativeSelected(boolean selected, ConsoleOptionsFrontendPersister consoleOptionsPersister, EditOptionsModel model) {
        consoleNativeRadioButton.asRadioButton().setValue(selected);
        if (selected) {
            storeSelectedConsole(VncConsoleModel.ClientConsoleMode.Native.toString(), consoleOptionsPersister, model);
        }
    }

    @Override
    public void setConsoleDefaultSelected(boolean selected, ConsoleOptionsFrontendPersister consoleOptionsPersister, EditOptionsModel model) {
        consoleDefaultRadioButton.asRadioButton().setValue(selected);
        if (selected) {
            consoleOptionsPersister.removeGeneralVncType();
            model.getOkCommand().setIsExecutionAllowed(true);
        }
    }

    private void storeSelectedConsole(String consoleType, ConsoleOptionsFrontendPersister persister, EditOptionsModel model) {
        persister.storeGeneralVncType(consoleType);
        model.getOkCommand().setIsExecutionAllowed(true);
    }

}

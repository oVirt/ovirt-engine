package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.section.main.presenter.OptionsPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextArea;
import org.ovirt.engine.ui.uicommonweb.models.options.EditOptionsModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class OptionsPopupView extends AbstractModelBoundPopupView<EditOptionsModel>
        implements OptionsPopupPresenterWidget.ViewDef {

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

    @Inject
    public OptionsPopupView(EventBus eventBus) {
        super(eventBus);

        localStoragePersistedOnServerCheckBox = new EntityModelCheckBoxEditor(Align.RIGHT);
        confirmSuspendingVmCheckBox = new EntityModelCheckBoxEditor(Align.RIGHT);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        consolePublicKeyInfoIcon.setText(SafeHtmlUtils.fromString(constants.consolePublicKeyMessage()));
        localStoragePersistedOnServerInfoIcon.setText(createTooltip());

        driver.initialize(this);
    }

    private SafeHtml createTooltip() {
        SafeHtmlBuilder listItemBuilder = new SafeHtmlBuilder();
        listItemBuilder.append(templates.listItem(SafeHtmlUtils.fromString(constants.hideDisplayColumns())));
        listItemBuilder.append(templates.listItem(SafeHtmlUtils.fromString(constants.swapColumns())));

        SafeHtmlBuilder tooltipBuilder = new SafeHtmlBuilder();
        tooltipBuilder.append(templates.text(constants.persistGridSettingsOnServerTooltip()));
        tooltipBuilder.append(templates.unorderedList(listItemBuilder.toSafeHtml()));

        return tooltipBuilder.toSafeHtml();
    }

    @Override
    public HasChangeHandlers getPublicKeyEditor() {
        return publicKeyEditor;
    }

    @Override
    public void edit(final EditOptionsModel model) {
        driver.edit(model);
    }

    @Override
    public EditOptionsModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}

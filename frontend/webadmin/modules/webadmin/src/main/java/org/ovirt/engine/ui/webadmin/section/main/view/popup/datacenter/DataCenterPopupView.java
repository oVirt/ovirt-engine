package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractTabbedModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.BooleanRenderer;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacPoolModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool.MacPoolWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class DataCenterPopupView extends AbstractTabbedModelBoundPopupView<DataCenterModel> implements DataCenterPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<DataCenterModel, DataCenterPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DataCenterPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<DataCenterPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    DialogTabPanel tabPanel;

    @UiField
    @WithElementId
    DialogTab generalTab;

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "comment.entity")
    @WithElementId
    StringEntityModelTextBoxEditor commentEditor;

    @UiField(provided = true)
    @Path(value = "storagePoolType.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Boolean> storagePoolTypeEditor;

    @UiField(provided = true)
    @Path(value = "version.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Version> versionEditor;

    @UiField(provided = true)
    @Path(value = "quotaEnforceTypeListModel.selectedItem")
    @WithElementId
    ListModelListBoxEditor<QuotaEnforcementTypeEnum> quotaEnforceTypeEditor;

    @UiField
    @WithElementId
    DialogTab macPoolTab;

    @UiField(provided = true)
    @Path(value = "macPoolListModel.selectedItem")
    @WithElementId
    ListModelListBoxEditor<MacPool> macPoolListEditor;

    @UiField
    @Ignore
    @WithElementId
    MacPoolWidget macPoolWidget;

    @UiField
    UiCommandButton addMacPoolButton;

    @UiField
    Style style;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public DataCenterPopupView(EventBus eventBus) {
        super(eventBus);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        addContentStyleName(style.contentStyle());
        driver.initialize(this);
    }

    void initListBoxEditors() {
        storagePoolTypeEditor = new ListModelListBoxEditor<>(new BooleanRenderer(constants.storageTypeLocal(), constants.storageTypeShared()));

        versionEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Version>() {
            @Override
            public String renderNullSafe(Version object) {
                return object.getValue();
            }
        });

        quotaEnforceTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer());

        macPoolListEditor = new ListModelListBoxEditor<>(new NameRenderer<MacPool>());
    }

    void localize() {
        nameEditor.setLabel(constants.nameLabel());
        descriptionEditor.setLabel(constants.descriptionLabel());
        commentEditor.setLabel(constants.commentLabel());
        storagePoolTypeEditor.setLabel(constants.dataCenterPopupStorageTypeLabel());
        versionEditor.setLabel(constants.dataCenterPopupVersionLabel());
        quotaEnforceTypeEditor.setLabel(constants.dataCenterPopupQuotaEnforceTypeLabel());
        macPoolListEditor.setLabel(constants.dataCenterPopupMacPoolLabel());
    }

    @Override
    public void edit(DataCenterModel object) {
        driver.edit(object);
        updateMacPool(object.getMacPoolModel());
    }

    @Override
    public DataCenterModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void updateMacPool(MacPoolModel macPoolModel) {
        macPoolWidget.edit(macPoolModel);
    }

    @Override
    public HasUiCommandClickHandlers getMacPoolButton() {
        return addMacPoolButton;
    }

    public void addContentStyleName(String styleName) {
        this.asWidget().addContentStyleName(styleName);
    }

    @Override
    protected void populateTabMap() {
        getTabNameMapping().put(TabName.GENERAL_TAB, this.generalTab);
        getTabNameMapping().put(TabName.MAC_POOL_TAB, macPoolTab);
    }

    interface Style extends CssResource {
        String contentStyle();
    }

    @Override
    public DialogTabPanel getTabPanel() {
        return tabPanel;
    }

}

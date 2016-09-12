package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.BooleanRenderer;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class DataCenterPopupView extends AbstractModelBoundPopupView<DataCenterModel> implements DataCenterPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<DataCenterModel, DataCenterPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DataCenterPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<DataCenterPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

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
    }

    void localize() {
        nameEditor.setLabel(constants.nameLabel());
        descriptionEditor.setLabel(constants.descriptionLabel());
        commentEditor.setLabel(constants.commentLabel());
        storagePoolTypeEditor.setLabel(constants.dataCenterPopupStorageTypeLabel());
        versionEditor.setLabel(constants.dataCenterPopupVersionLabel());
        quotaEnforceTypeEditor.setLabel(constants.dataCenterPopupQuotaEnforceTypeLabel());
    }

    @Override
    public void edit(DataCenterModel object) {
        driver.edit(object);
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
    public void cleanup() {
        driver.cleanup();
    }

    public void addContentStyleName(String styleName) {
        this.asWidget().addContentStyleName(styleName);
    }

    interface Style extends CssResource {
        String contentStyle();
    }

}

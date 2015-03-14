package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.VmsSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model.AffinityGroupModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling.AffinityGroupPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class AffinityGroupPopupView extends AbstractModelBoundPopupView<AffinityGroupModel> implements AffinityGroupPopupPresenterWidget.ViewDef {
    interface Driver extends SimpleBeanEditorDriver<AffinityGroupModel, AffinityGroupPopupView> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AffinityGroupPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<AffinityGroupPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "positive.entity")
    @WithElementId("positive")
    EntityModelCheckBoxEditor positiveEditor;

    @UiField(provided = true)
    @Path(value = "enforcing.entity")
    @WithElementId("enforcing")
    EntityModelCheckBoxEditor enforcingEditor;

    @UiField(provided = true)
    @Ignore
    protected KeyValueWidget<VmsSelectionModel> addRemoveVmWidget;

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public AffinityGroupPopupView(EventBus eventBus) {
        super(eventBus);
        initCheckBoxEditors();
        initAddRemoveWidget();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        driver.initialize(this);
    }

    private void initAddRemoveWidget() {
        addRemoveVmWidget = new KeyValueWidget<VmsSelectionModel>("120px"); //$NON-NLS-1$
    }

    private void initCheckBoxEditors() {
        positiveEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        enforcingEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void localize() {
        nameEditor.setLabel(constants.affinityGroupNameLabel());
        descriptionEditor.setLabel(constants.affinityDescriptionLabel());
        positiveEditor.setLabel(constants.affinityGroupPolarityLabel());
        enforcingEditor.setLabel(constants.affinityGroupEnforceTypeLabel());
    }

    public void edit(AffinityGroupModel model) {
        driver.edit(model);
        addRemoveVmWidget.edit(model.getVmsSelectionModel());
    }

    @Override
    public AffinityGroupModel flush() {
        return driver.flush();
    }

}

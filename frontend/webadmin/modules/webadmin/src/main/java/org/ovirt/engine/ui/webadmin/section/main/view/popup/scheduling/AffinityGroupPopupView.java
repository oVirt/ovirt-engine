package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
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

    @Path(value = "positive.entity")
    @WithElementId("positive")
    EntityModelCheckBoxOnlyEditor positiveEditor;

    @UiField(provided = true)
    @Ignore
    EntityModelWidgetWithInfo positiveEditorWithInfo;

    @Path(value = "enforcing.entity")
    @WithElementId("enforcing")
    EntityModelCheckBoxOnlyEditor enforcingEditor;

    @UiField(provided = true)
    @Ignore
    EntityModelWidgetWithInfo enforcingEditorWithInfo;

    @UiField(provided = true)
    @Ignore
    protected KeyValueWidget<VmsSelectionModel> addRemoveVmWidget;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

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
        addRemoveVmWidget = new KeyValueWidget<>("120px"); //$NON-NLS-1$
    }

    private void initCheckBoxEditors() {
        positiveEditor = new EntityModelCheckBoxOnlyEditor();

        EnableableFormLabel posLabel = new EnableableFormLabel();
        posLabel.setText(constants.affinityGroupPolarityLabel());

        positiveEditorWithInfo = new EntityModelWidgetWithInfo(posLabel, positiveEditor);
        positiveEditorWithInfo.setExplanation(templates.italicText(constants.affinityGroupPolarityInfo()));

        enforcingEditor = new EntityModelCheckBoxOnlyEditor();

        EnableableFormLabel enfLabel = new EnableableFormLabel();
        enfLabel.setText(constants.affinityGroupEnforceTypeLabel());

        enforcingEditorWithInfo = new EntityModelWidgetWithInfo(enfLabel, enforcingEditor);
        enforcingEditorWithInfo.setExplanation(templates.italicText(constants.affinityGroupEnforcInfo()));
    }

    private void localize() {
        nameEditor.setLabel(constants.affinityGroupNameLabel());
        descriptionEditor.setLabel(constants.affinityDescriptionLabel());
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

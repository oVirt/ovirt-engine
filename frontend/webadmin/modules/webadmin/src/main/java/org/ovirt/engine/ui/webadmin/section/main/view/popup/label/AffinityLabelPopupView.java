package org.ovirt.engine.ui.webadmin.section.main.view.popup.label;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.model.AffinityLabelModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.EntitySelectionModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.label.AffinityLabelPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;


public class AffinityLabelPopupView extends AbstractModelBoundPopupView<AffinityLabelModel> implements AffinityLabelPopupPresenterWidget.ViewDef {
    interface Driver extends UiCommonEditorDriver<AffinityLabelModel, AffinityLabelPopupView> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AffinityLabelPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<AffinityLabelPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    FlowPanel implicitAffinityGroupPanel;

    @UiField(provided = true)
    @Path(value = "implicitAffinityGroup.entity")
    @WithElementId("implicitAffinityGroup")
    EntityModelCheckBoxEditor implicitAffinityGroup;

    @UiField(provided = true)
    InfoIcon implicitAffinityGroupInfoIcon;

    @UiField
    @Ignore
    protected KeyWidget<EntitySelectionModel> addRemoveVmWidget;

    @UiField
    @Ignore
    protected KeyWidget<EntitySelectionModel> addRemoveHostWidget;

    @Inject
    public AffinityLabelPopupView(EventBus eventBus) {
        super(eventBus);

        implicitAffinityGroup = new EntityModelCheckBoxEditor(Align.RIGHT);
        implicitAffinityGroupInfoIcon = new InfoIcon(templates.italicText(constants.affinityLabelsImplicitGroupInfo()));

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    public void edit(final AffinityLabelModel model) {
        driver.edit(model);
        addRemoveVmWidget.edit(model.getVmsSelectionModel());
        addRemoveHostWidget.edit(model.getHostsSelectionModel());

        implicitAffinityGroupPanel.setVisible(model.isAffinityGroupAvailable());
    }

    @Override
    public AffinityLabelModel flush() {
        addRemoveHostWidget.flush();
        addRemoveVmWidget.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}

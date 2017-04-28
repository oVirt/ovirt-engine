package org.ovirt.engine.ui.webadmin.section.main.view.popup.label;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.model.AffinityLabelModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.HostsSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.VmsSelectionModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.label.AffinityLabelPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Ignore
    protected KeyWidget<VmsSelectionModel> addRemoveVmWidget;

    @UiField
    @Ignore
    protected KeyWidget<HostsSelectionModel> addRemoveHostWidget;

    @Inject
    public AffinityLabelPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    public void edit(final AffinityLabelModel model) {
        driver.edit(model);
        addRemoveVmWidget.edit(model.getVmsSelectionModel());
        addRemoveHostWidget.edit(model.getHostsSelectionModel());
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

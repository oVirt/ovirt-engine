package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.QosModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.QosParametersModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

public abstract class QosPopupView<T extends QosBase, P extends QosParametersModel<T>> extends AbstractModelBoundPopupView<QosModel<T, P>> {

    @UiField(provided = true)
    @Path(value = "dataCenters.selectedItem")
    @WithElementId
    ListModelListBoxEditor<StoragePool> dataCenterEditor;

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    protected QosWidget<T, P> qosWidget;

    protected final ApplicationConstants constants;

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, QosPopupView<?, ?>> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<QosPopupView<?, ?>> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final SimpleBeanEditorDriver<QosModel<T, P>, QosPopupView<T, P>> driver;

    public QosPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        this.constants = constants;

        initListBoxEditors();
        createQosWidget();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        localize();

        driver = createDriver();
        driver.initialize(this);
    }

    private void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<StoragePool>(new NullSafeRenderer<StoragePool>() {
            @Override
            public String renderNullSafe(StoragePool dataCenter) {
                return dataCenter.getName();
            }
        });
    }

    private void localize() {
        nameEditor.setLabel(constants.qosName());
        descriptionEditor.setLabel(constants.qosDescription());
        dataCenterEditor.setLabel(constants.dataCenterQosPopup());
    }

    @Override
    public void edit(QosModel<T, P> object) {
        driver.edit(object);
        qosWidget.edit(object.getQosParametersModel());
    }

    @Override
    public QosModel<T, P> flush() {
        qosWidget.flush();
        return driver.flush();
    }

    abstract protected void createQosWidget();

    abstract protected SimpleBeanEditorDriver<QosModel<T, P>, QosPopupView<T, P>> createDriver();

}

package org.ovirt.engine.ui.webadmin.section.main.view.popup.provider;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.uicommonweb.models.providers.NewExternalSubnetModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ExternalSubnetPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.ExternalSubnetWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class ExternalSubnetPopupView extends AbstractModelBoundPopupView<NewExternalSubnetModel> implements ExternalSubnetPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<NewExternalSubnetModel, ExternalSubnetPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ExternalSubnetPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ExternalSubnetPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    StringEntityModelLabelEditor networkEditor;

    @UiField
    @Ignore
    ExternalSubnetWidget subnetWidget;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ExternalSubnetPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        networkEditor.setLabel(constants.networkExternalSubnet());

        driver.initialize(this);
    }

    @Override
    public void focusInput() {
        subnetWidget.focusInput();
    }

    @Override
    public void edit(final NewExternalSubnetModel subnet) {
        driver.edit(subnet);
        networkEditor.asValueBox().setValue(subnet.getNetwork().getEntity().getName());
        subnetWidget.edit(subnet.getSubnetModel());
    }

    @Override
    public NewExternalSubnetModel flush() {
        subnetWidget.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        nextTabIndex = networkEditor.setTabIndexes(nextTabIndex);
        nextTabIndex = subnetWidget.setTabIndexes(nextTabIndex);
        return nextTabIndex;
    }

}

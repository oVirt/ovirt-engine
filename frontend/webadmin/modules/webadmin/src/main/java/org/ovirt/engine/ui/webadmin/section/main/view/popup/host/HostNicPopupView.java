package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostNicModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostNicPopupPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class HostNicPopupView extends AbstractModelBoundPopupView<HostNicModel> implements HostNicPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostNicModel, HostNicPopupView> {
    }

    private Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostNicPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Ignore
    NicLabelWidget labelsWidget;

    @Inject
    public HostNicPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    @Override
    public void edit(HostNicModel model) {
        driver.edit(model);
        labelsWidget.edit(model.getLabelsModel());
    }

    @Override
    public HostNicModel flush() {
        labelsWidget.flush();
        return driver.flush();
    }

    @Override
    public void focusInput() {
        super.focusInput();
        labelsWidget.focusInput();
    }

}

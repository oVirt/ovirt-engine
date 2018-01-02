package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.AlertWithIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.ResetBrickModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.ResetBrickPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class ResetBrickPopupView extends AbstractModelBoundPopupView<ResetBrickModel> implements ResetBrickPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ResetBrickModel, ResetBrickPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ResetBrickPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ResetBrickPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    AlertWithIcon message;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public ResetBrickPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void edit(final ResetBrickModel object) {
        driver.edit(object);
        }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        this.message.setText(message);
    }

    @Override
    public ResetBrickModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

}

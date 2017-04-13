package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.HasErrata;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ErrataListWithDetailsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.errata.ErrataDetailModelForm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

/**
 * View for the popup widget that renders errata (singular: Erratum). The popup is basically a split panel,
 * with a grid of Erratum in the top pane, and the selected Erratum's detail in the bottom pane.
 */
public abstract class ErrataListWithDetailsPopupView extends AbstractModelBoundPopupView<AbstractErrataCountModel>
    implements ErrataListWithDetailsPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ErrataListWithDetailsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    FlowPanel errataDetailPanel;

    @UiField
    HTMLPanel errataTitle;

    @UiField (provided = true)
    @Ignore
    ErrataDetailModelForm errataDetailModelForm;

    @UiField
    ErrataTableView errataTableView;

    protected final AbstractErrataListModel model;

    public ErrataListWithDetailsPopupView(EventBus eventBus, AbstractErrataListModel model) {
        super(eventBus);
        errataDetailModelForm = new ErrataDetailModelForm();
        model.setItemsFilter(null);
        this.model = model;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        errataDetailModelForm.initialize();
    }

    @Override
    public void edit(AbstractErrataCountModel transferObj) {
        errataTableView.getErrataTable().setLoadingState(LoadingState.LOADING);

        model.setGuid(transferObj.getGuid());
        errataTableView.init(model);
        HasErrata entity = transferObj.getEntity();
        model.setEntity(entity);
        model.search();

        setErrataDetailPanelVisibilty(false);
    }

    public void showErrataList() {
        errataTableView.edit();
    }

    public ErrataTableView getErrataTable() {
        return errataTableView;
    }

    public void setErrataDetailPanelVisibilty(boolean visible) {
        splitLayoutPanel.setWidgetHidden(errataDetailPanel, !visible);
        Scheduler.get().scheduleDeferred(() -> errataTableView.onResize());
    }

    public void updateErrataDetailFormPanel(Erratum erratum) {
        errataTitle.clear();
        errataTitle.add(new HTML(erratum.getTitle()));
        EntityModel<Erratum> entityModel = new EntityModel<>();
        entityModel.setEntity(erratum);
        errataDetailModelForm.setModel(entityModel);
        errataDetailModelForm.update();
    }

    @Override
    public Erratum getSelectedErratum() {
        return errataTableView.getSelectedErratum();
    }

    @Override
    public AbstractErrataCountModel flush() {
        return null;
    }

    @Override
    public void cleanup() {
        // TODO clean up stuff if needed
    }

    @Override
    public void hide() {
        super.hide();
        errataTableView.clearHandlers();
    }
}

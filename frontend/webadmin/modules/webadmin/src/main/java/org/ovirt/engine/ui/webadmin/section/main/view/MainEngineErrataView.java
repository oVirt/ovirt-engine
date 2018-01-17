package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.PlaceTransitionHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel.Type;
import org.ovirt.engine.ui.uicommonweb.models.EngineErrataListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainEngineErrataPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.ErrataTableView;
import org.ovirt.engine.ui.webadmin.widget.errata.ErrataFilterPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View for the main tab that contains errata (singular: Erratum) for the engine itself.
 * <p>
 * It is a little different from a typical main tab view -- it supports an AlertPanel for setting
 * a message upon failing to retrieve the errata. (Errata query results rely on integration with a
 * Katello provider, and if the provider has an issue, we want to notify the user right in the tab body.
 */
public class MainEngineErrataView extends AbstractMainWithDetailsTableView<Erratum,
    EngineErrataListModel> implements MainEngineErrataPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainEngineErrataView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface MainEngineErrataViewUiBinder extends UiBinder<Widget, MainEngineErrataView> {
    }

    @UiField
    public FlowPanel tablePanel;

    @UiField
    AlertPanel errorMessagePanel;

    protected ErrataFilterPanel errataFilterPanel;

    private static MainEngineErrataViewUiBinder uiBinder = GWT.create(MainEngineErrataViewUiBinder.class);

    @Inject
    public MainEngineErrataView(MainModelProvider<Erratum, EngineErrataListModel> modelProvider) {
        super(modelProvider);

        ViewIdHandler.idHandler.generateAndSetIds(this);

        initWidget(uiBinder.createAndBindUi(this));
        errorMessagePanel.setVisible(false);
        errorMessagePanel.setType(Type.WARNING);

        initFilterPanel();
        getTable().setTableOverhead(errataFilterPanel);
        getTable().enableColumnResizing();

        tablePanel.add(getTable());
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initFilterPanel() {
        errataFilterPanel = new ErrataFilterPanel();
        errataFilterPanel.init(true, true, true);
    }

    public void clearErrorMessage() {
        errorMessagePanel.clearMessages();
        errorMessagePanel.setVisible(false);
        tablePanel.setVisible(true);
    }

    @Override
    public void showErrorMessage(SafeHtml message) {
        tablePanel.setVisible(false);

        errorMessagePanel.clearMessages();
        errorMessagePanel.setVisible(true);
        errorMessagePanel.addMessage(message);
    }

    public ErrataFilterPanel getErrataFilterPanel() {
        return errataFilterPanel;
    }

    @Override
    public void setPlaceTransitionHandler(PlaceTransitionHandler handler) {
        super.setPlaceTransitionHandler(handler);
        // configure the table columns -- share config with ErrataTableView
        ErrataTableView.initErrataGrid(getTable(), true, handler);
    }
}

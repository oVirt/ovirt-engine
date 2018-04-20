package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.ErrataFilterValue;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.ErrataTableView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Pop-up widget that renders Errata (singular: Erratum). The pop-up is basically a split panel, with
 * a grid of Erratum in the top pane, and the selected Erratum's detail in the bottom pane.
 */
public abstract class ErrataListWithDetailsPopupPresenterWidget<T extends
    SearchableDetailModelProvider<Erratum, ? extends SearchableListModel, ? extends AbstractErrataListModel>>
    extends AbstractModelBoundPopupPresenterWidget<AbstractErrataCountModel, ErrataListWithDetailsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<AbstractErrataCountModel> {
        void setErrataDetailPanelVisibilty(boolean visible);
        void updateErrataDetailFormPanel(Erratum erratum);
        ErrataTableView getErrataTable();
        Erratum getSelectedErratum();
        void showErrataList();
    }

    private final T modelProvider;
    private IEventListener<PropertyChangedEventArgs> changeListener;

    @Inject
    public ErrataListWithDetailsPopupPresenterWidget(EventBus eventBus, ViewDef view, T modelProvider) {

        super(eventBus, view);

        this.modelProvider = modelProvider;
    }

    @Override
    public void init(final AbstractErrataCountModel clickSource) {

        super.init(clickSource);

        AbstractErrataListModel model = modelProvider.getModel();
        model.setItemsFilter(createFilter(clickSource.getFilterCommand()));
    }

    private ErrataFilterValue createFilter(String filterCommand) {
        ErrataFilterValue filterValue = new ErrataFilterValue(false, false, false);
        if (AbstractErrataCountModel.SHOW_BUGS_COMMAND.equals(filterCommand)) {
            filterValue.setBugs(true);
        } else if (AbstractErrataCountModel.SHOW_ENHANCEMENTS_COMMAND.equals(filterCommand)) {
            filterValue.setEnhancements(true);
        } else if (AbstractErrataCountModel.SHOW_SECURITY_COMMAND.equals(filterCommand)) {
            filterValue.setSecurity(true);
        }
        return filterValue;
    }

    @Override
    protected void onBind() {
        super.onBind();

        //
        // Handle the query returning a new list of errata -> simple view update.
        //
        changeListener = (ev, sender, args) -> getView().showErrataList();
        modelProvider.getModel().addItemsChangeListener(changeListener);

        // Handle the errata selection changing -> simple view update.
        //
        getView().getErrataTable().addSelectionChangeHandler(event -> {

            Erratum erratum = getView().getSelectedErratum();

            if (erratum == null) {
                getView().setErrataDetailPanelVisibilty(false);
            } else {
                getView().updateErrataDetailFormPanel(erratum);
                getView().setErrataDetailPanelVisibilty(true);
            }
        });
    }

    @Override
    public void onUnbind() {
        super.onUnbind();
        if ( changeListener != null) {
            modelProvider.getModel().getPropertyChangedEvent().removeListener(changeListener);
        }
    }

}

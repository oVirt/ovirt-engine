package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.ErrataTableView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;

/**
 * Pop-up widget that renders Errata (singular: Erratum). The pop-up is basically a split panel, with
 * a grid of Erratum in the top pane, and the selected Erratum's detail in the bottom pane.
 */
public abstract class ErrataListWithDetailsPopupPresenterWidget<T extends SearchableDetailModelProvider<Erratum,
    ? extends SearchableListModel, ? extends AbstractErrataListModel>>
    extends AbstractModelBoundPopupPresenterWidget<AbstractErrataCountModel,
        ErrataListWithDetailsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<AbstractErrataCountModel> {
        public void setErrataDetailPanelVisibilty(boolean visible);
        public void updateErrataDetailFormPanel(Erratum erratum);
        public ErrataTableView getErrataTable();
        public Erratum getSelectedErratum();
        public void showErrataList();
    }

    private final T modelProvider;

    @Inject
    public ErrataListWithDetailsPopupPresenterWidget(EventBus eventBus, ViewDef view, T modelProvider) {

        super(eventBus, view);

        this.modelProvider = modelProvider;
    }

    @Override
    public void init(final AbstractErrataCountModel clickSource) {

        super.init(clickSource);

        AbstractErrataListModel model = modelProvider.getModel();

        // Handle the query returning a new list of errata -> simple view update.
        //
        model.addItemsChangeListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                getView().showErrataList();
            }
        });
    }

    @Override
    protected void onBind() {
        super.onBind();

        // Handle the errata selection changing -> simple view update.
        //
        getView().getErrataTable().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {

                Erratum erratum = getView().getSelectedErratum();

                if (erratum == null) {
                    getView().setErrataDetailPanelVisibilty(false);
                }
                else {
                    getView().updateErrataDetailFormPanel(erratum);
                    getView().setErrataDetailPanelVisibilty(true);
                }
            }
        });
    }
}

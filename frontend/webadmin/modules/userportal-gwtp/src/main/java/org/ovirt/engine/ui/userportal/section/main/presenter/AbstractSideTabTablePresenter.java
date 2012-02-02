package org.ovirt.engine.ui.userportal.section.main.presenter;

import java.util.List;

import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 * Base class for table-based side tab presenters.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            Model type.
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractSideTabTablePresenter<T, M extends SearchableListModel, V extends AbstractSideTabTablePresenter.ViewDef<T>, P extends Proxy<?>>
        extends Presenter<V, P> {

    public interface ViewDef<T> extends View {

        /**
         * Returns the selection model used by the side tab table widget.
         */
        OrderedMultiSelectionModel<T> getTableSelectionModel();

    }

    private final SearchableTableModelProvider<T, M> modelProvider;

    public AbstractSideTabTablePresenter(EventBus eventBus, V view, P proxy,
            SearchableTableModelProvider<T, M> modelProvider) {
        super(eventBus, view, proxy);
        this.modelProvider = modelProvider;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainTabExtendedPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getTableSelectionModel()
                .addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        // Update model selection
                        modelProvider.setSelectedItems(getSelectedItems());
                    }
                }));

        // Turn multiple selection feature off by default
        getView().getTableSelectionModel().setMultiSelectEnabled(false);
        getView().getTableSelectionModel().setMultiRangeSelectEnabled(false);
    }

    /**
     * Returns items currently selected in the table.
     */
    protected List<T> getSelectedItems() {
        return getView().getTableSelectionModel().getSelectedList();
    }

}

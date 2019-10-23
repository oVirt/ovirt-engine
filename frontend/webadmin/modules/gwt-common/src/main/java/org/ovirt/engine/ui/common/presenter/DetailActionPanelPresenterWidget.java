package org.ovirt.engine.ui.common.presenter;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

import com.google.web.bindery.event.shared.EventBus;

public abstract class DetailActionPanelPresenterWidget<E, T, M extends ListWithDetailsModel, D extends HasEntity>
    extends ActionPanelPresenterWidget<E, T, M> {

    public interface ViewDef<E, T> extends ActionPanelPresenterWidget.ViewDef<E, T> {
    }

    @Inject
    public DetailActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<E, T> view,
            SearchableDetailModelProvider<T, ?, ?> dataProvider) {
        super(eventBus, view, (SearchableTabModelProvider<T, M>) dataProvider);
    }

    protected D getDetailModel() {
        return (D) ((SearchableDetailModelProvider<T, ?, ?>)getDataProvider()).getModel();
    }

}

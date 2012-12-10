package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;

/**
 * Base class for table-based main tab views that work with {@link ListWithDetailsModel}.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            Main model type.
 */
public abstract class AbstractMainTabWithDetailsTableView<T, M extends ListWithDetailsModel> extends AbstractMainTabTableView<T, M>
        implements AbstractMainTabWithDetailsPresenter.ViewDef<T> {

    public AbstractMainTabWithDetailsTableView(MainModelProvider<T, M> modelProvider) {
        super(modelProvider);
    }
}

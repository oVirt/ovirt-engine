package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

/**
 * Used to resolve UiCommon main and detail models from the root {@link CommonModel} instance.
 */
public abstract class UiCommonModelResolver {

    /**
     * Resolves a main model instance.
     *
     * @param <M>
     *            Main model type.
     */
    @SuppressWarnings("unchecked")
    public static <M extends SearchableListModel> M getMainListModel(
            CommonModel commonModel, Class<M> mainModelClass) {
        assert (commonModel != null) : "CommonModel must not be null";

        for (SearchableListModel list : commonModel.getItems()) {
            if (list != null && list.getClass().equals(mainModelClass)) {
                return (M) list;
            }
        }

        throw new IllegalStateException("Cannot resolve main list model [" + mainModelClass + "]");
    }

    /**
     * Resolves a detail model instance according to the given main model class.
     *
     * @param <M>
     *            Main model type.
     * @param <D>
     *            Detail model type.
     */
    @SuppressWarnings("unchecked")
    public static <M extends ListWithDetailsModel, D extends EntityModel> D getDetailListModel(
            CommonModel commonModel, Class<M> mainModelClass, Class<D> detailModelClass) {
        assert (commonModel != null) : "CommonModel must not be null";

        M mainListModel = getMainListModel(commonModel, mainModelClass);

        for (EntityModel details : mainListModel.getDetailModels()) {
            if (details != null && details.getClass().equals(detailModelClass)) {
                return (D) details;
            }
        }

        throw new IllegalStateException("Cannot resolve detail list model [" + detailModelClass
                + "] from main list model [" + mainModelClass + "]");
    }

}

package org.ovirt.engine.ui.uicommonweb.models;

/**
 * Simple {@link org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel}
 * that has the same detail entity as its list elements.
 *
 * @param <E> {@link org.ovirt.engine.ui.uicommonweb.models.SearchableListModel.E}
 * @param <T> {@link org.ovirt.engine.ui.uicommonweb.models.SearchableListModel.T}
 */
public abstract class ListWithSimpleDetailsModel<E, T> extends ListWithDetailsModel<E, T, T> {
    @Override
    protected T provideDetailModelEntity(T selectedItem) {
        return selectedItem;
    }
}

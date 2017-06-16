package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

/**
 * Provider of searchable detail model instances.
 * <p>
 * Contains main model type information to distinguish detail models of the same type for different main models.
 *
 * @param <T> Detail item type.
 * @param <M> Main model type (extends ListWithDetailsModel).
 * @param <D> Detail model type (extends SearchableListModel).
 */
public interface SearchableDetailModelProvider<T, M extends ListWithDetailsModel, D extends SearchableListModel> extends DetailModelProvider<M, D>, SearchableTableModelProvider<T, D> {
}

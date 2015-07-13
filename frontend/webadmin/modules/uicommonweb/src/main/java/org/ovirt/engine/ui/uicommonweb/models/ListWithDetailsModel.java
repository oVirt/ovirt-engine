package org.ovirt.engine.ui.uicommonweb.models;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

/**
 * @param <E> {@link org.ovirt.engine.ui.uicommonweb.models.SearchableListModel.E}
 * @param <D> type of the detail entity
 * @param <T> {@link org.ovirt.engine.ui.uicommonweb.models.SearchableListModel.T}
 */
@SuppressWarnings("unused")
public abstract class ListWithDetailsModel<E, D, T> extends SearchableListModel<E, T> {

    private List<HasEntity<D>> detailModels;

    public List<HasEntity<D>> getDetailModels() {
        return detailModels;
    }

    public void setDetailModels(List<HasEntity<D>> value) {
        if (detailModels != value) {
            detailModels = value;
            onPropertyChanged(new PropertyChangedEventArgs("DetailModels")); //$NON-NLS-1$
        }
    }

    private HasEntity<D> activeDetailModel;

    public HasEntity<D> getActiveDetailModel() {
        return activeDetailModel;
    }

    public void setActiveDetailModel(HasEntity<D> value) {
        if (activeDetailModel != value) {
            activeDetailModelChanging(value, getActiveDetailModel());
            activeDetailModel = value;
            activeDetailModelChanged();
            onPropertyChanged(new PropertyChangedEventArgs("ActiveDetailModel")); //$NON-NLS-1$
        }
    }

    protected void updateDetailsAvailability() {
    }

    private void activeDetailModelChanging(HasEntity<D> newValue, HasEntity<D> oldValue) {
        // Make sure we had set an entity property of details model.
        if (oldValue != null) {
            oldValue.setEntity(null);

            if (oldValue instanceof SearchableListModel) {
                ((SearchableListModel) oldValue).stopRefresh();
            }
        }

        if (newValue != null) {
            newValue.setEntity(provideDetailModelEntity(getSelectedItem()));
        }
    }

    protected abstract D provideDetailModelEntity(T selectedItem);

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();

        if (getSelectedItem() != null) {
            // Try to choose default (first) detail model.
            updateDetailsAvailability();
            if (getDetailModels() != null) {
                if ((getActiveDetailModel() != null && !getActiveDetailModel().getIsAvailable())
                        || getActiveDetailModel() == null) {
                    // ActiveDetailModel = DetailModels.FirstOrDefault(AvailabilityDecorator.GetIsAvailable);
                    HasEntity<D> model = null;
                    for (HasEntity<D> item : getDetailModels()) {
                        if (item.getIsAvailable()) {
                            model = item;
                            break;
                        }
                    }
                    setActiveDetailModel(model);
                }
            }

            // if (DetailModels != null && ActiveDetailModel == null)
            // {
            // ActiveDetailModel = DetailModels.FirstOrDefault();
            // }
        }
        else {
            // If selected item become null, make sure we stop all activity on an active detail model.
            if (getActiveDetailModel() != null && getActiveDetailModel() instanceof SearchableListModel) {
                ((SearchableListModel) getActiveDetailModel()).stopRefresh();
            }
        }

        // Synchronize selected item with the entity of an active details model.
        HasEntity<D> activeDetailModel = getActiveDetailModel();
        if (getSelectedItem() != null && activeDetailModel != null) {
            if (activeDetailModel instanceof HostInterfaceListModel) {
                ((HostInterfaceListModel) activeDetailModel).setEntity((VDS) provideDetailModelEntity(getSelectedItem()));
            }
            else {
                activeDetailModel.setEntity(provideDetailModelEntity(getSelectedItem()));
            }
        }
    }

    protected void activeDetailModelChanged() {
    }

    @Override
    public void stopRefresh() {
        super.stopRefresh();

        if (getDetailModels() != null) {
            // Stop search on all list models.
            for (HasEntity<D> model : getDetailModels()) {
                if (model instanceof SearchableListModel) {
                    SearchableListModel listModel = (SearchableListModel) model;
                    listModel.stopRefresh();
                }
            }
        }
    }

}

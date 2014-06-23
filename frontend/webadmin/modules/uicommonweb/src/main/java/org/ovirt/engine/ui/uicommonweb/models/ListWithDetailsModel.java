package org.ovirt.engine.ui.uicommonweb.models;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public abstract class ListWithDetailsModel extends SearchableListModel
{

    private List<EntityModel> detailModels;

    public List<EntityModel> getDetailModels()
    {
        return detailModels;
    }

    public void setDetailModels(List<EntityModel> value)
    {
        if (detailModels != value)
        {
            detailModels = value;
            onPropertyChanged(new PropertyChangedEventArgs("DetailModels")); //$NON-NLS-1$
        }
    }

    private EntityModel activeDetailModel;

    public EntityModel getActiveDetailModel()
    {
        return activeDetailModel;
    }

    public void setActiveDetailModel(EntityModel value)
    {
        if (activeDetailModel != value)
        {
            activeDetailModelChanging(value, getActiveDetailModel());
            activeDetailModel = value;
            activeDetailModelChanged();
            onPropertyChanged(new PropertyChangedEventArgs("ActiveDetailModel")); //$NON-NLS-1$
        }
    }

    protected void updateDetailsAvailability()
    {
    }

    private void activeDetailModelChanging(EntityModel newValue, EntityModel oldValue)
    {
        // Make sure we had set an entity property of details model.
        if (oldValue != null)
        {
            oldValue.setEntity(null);

            if (oldValue instanceof SearchableListModel)
            {
                ((SearchableListModel) oldValue).stopRefresh();
            }
        }

        if (newValue != null)
        {
            newValue.setEntity(provideDetailModelEntity(getSelectedItem()));
        }
    }

    protected Object provideDetailModelEntity(Object selectedItem)
    {
        return selectedItem;
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();

        if (getSelectedItem() != null)
        {
            // Try to choose default (first) detail model.
            updateDetailsAvailability();
            if (getDetailModels() != null)
            {
                if ((getActiveDetailModel() != null && !getActiveDetailModel().getIsAvailable())
                        || getActiveDetailModel() == null)
                {
                    // ActiveDetailModel = DetailModels.FirstOrDefault(AvailabilityDecorator.GetIsAvailable);
                    EntityModel model = null;
                    for (EntityModel item : getDetailModels())
                    {
                        if (item.getIsAvailable())
                        {
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
        else
        {
            // If selected item become null, make sure we stop all activity on an active detail model.
            if (getActiveDetailModel() != null && getActiveDetailModel() instanceof SearchableListModel)
            {
                ((SearchableListModel) getActiveDetailModel()).stopRefresh();
            }
        }

        // Synchronize selected item with the entity of an active details model.
        EntityModel activeDetailModel = getActiveDetailModel();
        if (getSelectedItem() != null && activeDetailModel != null)
        {
            if (activeDetailModel instanceof HostInterfaceListModel)
            {
                ((HostInterfaceListModel) activeDetailModel).setEntity((VDS) provideDetailModelEntity(getSelectedItem()));
            }
            else
            {
                activeDetailModel.setEntity(provideDetailModelEntity(getSelectedItem()));
            }
        }
    }

    protected void activeDetailModelChanged()
    {
    }

    @Override
    public void stopRefresh()
    {
        super.stopRefresh();

        if (getDetailModels() != null)
        {
            // Stop search on all list models.
            for (EntityModel model : getDetailModels())
            {
                if (model instanceof SearchableListModel)
                {
                    SearchableListModel listModel = (SearchableListModel) model;
                    listModel.stopRefresh();
                }
            }
        }
    }

}

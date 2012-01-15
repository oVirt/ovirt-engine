package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

@SuppressWarnings("unused")
public abstract class ImportSanStorageModel extends SanStorageModelBase
{

    private java.util.List<storage_domains> candidates;

    public java.util.List<storage_domains> getCandidates()
    {
        return candidates;
    }

    public void setCandidates(java.util.List<storage_domains> value)
    {
        if (candidates != value)
        {
            candidates = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Candidates"));
            getCandidatesList().setItems(ToEntityModelList(candidates));
        }
    }

    private ListModel candidatesList;

    public ListModel getCandidatesList()
    {
        return candidatesList;
    }

    public void setCandidatesList(ListModel value)
    {
        if (candidatesList != value)
        {
            candidatesList = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CandidatesList"));
        }
    }

    private String error;

    public String getError()
    {
        return error;
    }

    public void setError(String value)
    {
        if (!StringHelper.stringsEqual(error, value))
        {
            error = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Error"));
        }
    }

    private VDS oldHost;

    protected ImportSanStorageModel()
    {
        setCandidatesList(new ListModel());
        InitializeItems(null);
    }

    @Override
    protected void PostDiscoverTargets(java.util.ArrayList<SanTargetModel> newItems)
    {
        super.PostDiscoverTargets(newItems);

        InitializeItems(newItems);
    }

    @Override
    protected void UpdateInternal()
    {
        super.UpdateInternal();

        if (getContainer().getProgress() != null)
        {
            return;
        }

        VDS host = (VDS) getContainer().getHost().getSelectedItem();
        if (host == null)
        {
            return;
        }

        if (host != oldHost)
        {
            setItems(null);
            oldHost = host;
        }

        InitializeItems(null);

        setError(null);
        getContainer().StartProgress(null);

        Frontend.RunQuery(VdcQueryType.GetExistingStorageDomainList,
                new GetExistingStorageDomainListParameters(host.getvds_id(), getType(), getRole(), ""),
                new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object target, Object returnValue) {

                                ImportSanStorageModel model = (ImportSanStorageModel) target;
                                Object result = ((VdcQueryReturnValue) returnValue).getReturnValue();
                                if (result != null)
                                {
                                    model.setCandidates((java.util.ArrayList<storage_domains>) result);
                                }
                                else
                                {
                                    setError("Error while retrieving list of domains. Please consult your Storage Administrator.");
                                }
                                model.getContainer().StopProgress();

                            }
                        },
                        true));
    }

    private void InitializeItems(java.util.List<SanTargetModel> newItems)
    {
        if (getItems() == null)
        {
            setItems(new ObservableCollection<SanTargetModel>());
        }
        else
        {
            java.util.ArrayList<SanTargetModel> items = new java.util.ArrayList<SanTargetModel>();
            items.addAll((java.util.List<SanTargetModel>) getItems());

            // Add new targets.
            if (newItems != null)
            {
                for (SanTargetModel newItem : newItems)
                {
                    if (Linq.FirstOrDefault(items, new Linq.TargetPredicate(newItem)) == null)
                    {
                        items.add(newItem);
                    }
                }
            }

            setItems(items);
            UpdateLoginAllAvailability();
        }
    }

    @Override
    public boolean Validate()
    {
        boolean isValid = getSelectedItem() != null || getCandidatesList().getSelectedItem() != null;
        if (!isValid)
        {
            getInvalidityReasons().add("Please select a Storage Domain to import");
        }

        setIsValid(isValid);

        return super.Validate() && getIsValid();
    }

    private Iterable ToEntityModelList(java.util.List<storage_domains> list)
    {
        java.util.ArrayList<EntityModel> entityModelList = new java.util.ArrayList<EntityModel>();
        for (Object storage : list)
        {
            EntityModel model = new EntityModel();
            model.setEntity(storage);
            entityModelList.add(model);
        }
        return entityModelList;
    }
}

package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public abstract class ImportSanStorageModel extends SanStorageModelBase
{

    private List<StorageDomain> candidates;

    public List<StorageDomain> getCandidates()
    {
        return candidates;
    }

    public void setCandidates(List<StorageDomain> value)
    {
        if (candidates != value)
        {
            candidates = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Candidates")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("CandidatesList")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("Error")); //$NON-NLS-1$
        }
    }

    private VDS oldHost;

    protected ImportSanStorageModel()
    {
        setCandidatesList(new ListModel());
        InitializeItems(null);
    }

    @Override
    protected void PostDiscoverTargets(ArrayList<SanTargetModel> newItems)
    {
        super.PostDiscoverTargets(newItems);

        InitializeItems(newItems);
    }

    @Override
    protected void UpdateInternal()
    {
        super.UpdateInternal();

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

        AsyncQuery asyncQuery = new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                ImportSanStorageModel model = (ImportSanStorageModel) target;
                Object result = ((VdcQueryReturnValue) returnValue).getReturnValue();
                if (result != null)
                {
                    model.setCandidates((ArrayList<StorageDomain>) result);
                }
                else
                {
                    setError(ConstantsManager.getInstance()
                            .getConstants()
                            .errorWhileRetrievingListOfDomainsImportSanStorage());
                }
            }
        }, true);
        asyncQuery.setContext(getHash());
        Frontend.RunQuery(VdcQueryType.GetExistingStorageDomainList,
                new GetExistingStorageDomainListParameters(host.getId(), getType(), getRole(), ""), //$NON-NLS-1$
                asyncQuery);
    }

    private void InitializeItems(List<SanTargetModel> newItems)
    {
        if (getItems() == null)
        {
            setItems(new ObservableCollection<SanTargetModel>());
        }

        // Add new targets.
        if (newItems != null)
        {
            ArrayList<SanTargetModel> items = new ArrayList<SanTargetModel>();
            items.addAll((List<SanTargetModel>) getItems());

            for (SanTargetModel newItem : newItems)
            {
                if (Linq.FirstOrDefault(items, new Linq.TargetPredicate(newItem)) == null)
                {
                    items.add(newItem);
                }
            }

            setItems(items);
        }

        UpdateLoginAllAvailability();
    }

    @Override
    public boolean Validate()
    {
        boolean isValid = getSelectedItem() != null || getCandidatesList().getSelectedItem() != null;
        if (!isValid)
        {
            getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .pleaseSelectStorageDomainToImportImportSanStorage());
        }

        setIsValid(isValid);

        return super.Validate() && getIsValid();
    }

    private Iterable ToEntityModelList(List<StorageDomain> list)
    {
        ArrayList<EntityModel> entityModelList = new ArrayList<EntityModel>();
        for (Object storage : list)
        {
            EntityModel model = new EntityModel();
            model.setEntity(storage);
            entityModelList.add(model);
        }
        return entityModelList;
    }
}

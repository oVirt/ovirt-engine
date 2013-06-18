package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class StorageIsoListModel extends SearchableListModel
{

    public StorageIsoListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().imagesTitle());
        setHashName("images"); // $//$NON-NLS-1$

        setIsTimerDisabled(true);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getIsAvailable())
        {
            getSearchCommand().execute();
        }
    }

    @Override
    public void setEntity(Object value)
    {
        if (value == null || !value.equals(getEntity())) {
            super.setEntity(value);
        }
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            super.search();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    protected void syncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.syncSearch();

        if (getProgress() != null)
        {
            return;
        }

        StorageDomain storageDomain = (StorageDomain) getEntity();

        GetImagesListParameters imagesListParams = new GetImagesListParameters(storageDomain.getId(), ImageFileType.All);
        imagesListParams.setForceRefresh(true);
        imagesListParams.setRefresh(getIsQueryFirstTime());

        startProgress(null);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.setHandleFailure(true);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnObject)
            {
                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) returnObject;

                stopProgress();

                if (returnValue == null || returnValue.getReturnValue() == null || !returnValue.getSucceeded()) {
                    return;
                }

                ArrayList<RepoImage> repoFileList = (ArrayList<RepoImage>)
                        returnValue.getReturnValue();

                Collections.sort(repoFileList, new Comparator<RepoImage>() {
                    @Override
                    public int compare(RepoImage a, RepoImage b) {
                        return a.getRepoImageId().compareToIgnoreCase(b.getRepoImageId());
                    }
                });

                setItems(repoFileList);
                setIsEmpty(repoFileList.isEmpty());
            }
        };

        Frontend.RunQuery(VdcQueryType.GetImagesList, imagesListParams, _asyncQuery);
    }

    @Override
    protected String getListName() {
        return "StorageIsoListModel"; //$NON-NLS-1$
    }
}

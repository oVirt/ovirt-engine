package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.HasErrata;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

/**
 * Model object representing List of errata (singular: Erratum) for a VM, Host, or the engine itself (aka System).
 *
 * @see {@link EngineErrataListModel}
 * @see {@link HostErrataListModel}
 * @see {@link VmErrataListModel}
 *
 */
public abstract class AbstractErrataListModel extends ListWithSimpleDetailsModel<HasErrata, EntityModel<Erratum>> {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    protected Collection<Erratum> unfilteredResultList;

    protected ErrataFilterValue filter;
    protected Guid guid;

    public AbstractErrataListModel() {

        setIsTimerDisabled(true);
        setTitle(ConstantsManager.getInstance().getConstants().errata());
        setHelpTag(HelpTag.errata);
        setHashName("errata"); //$NON-NLS-1$

        setDefaultSearchString("Errata:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setAvailableInModes(ApplicationMode.AllModes);

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);

    }

    @Override
    protected void syncSearch() {
        super.syncSearch();
        runQuery(getGuid());
    }

    public void addErrorMessageChangeListener(IEventListener<PropertyChangedEventArgs> listener) {
        getPropertyChangedEvent().addListener(listener);
    }

    public void addItemsChangeListener(IEventListener<PropertyChangedEventArgs> listener) {
        getPropertyChangedEvent().addListener(listener);
    }

    public void retrieveEngineErrata() {
        runQuery(guid);
    }

    public void retrieveErrata(Guid guid) {
        runQuery(guid);
    }

    public void retrieveErrata() {
        runQuery(getGuid());
    }

    private void runQuery(Guid guid) {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.setHandleFailure(true);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object returnValue) {
                AbstractErrataListModel errataListModel = (AbstractErrataListModel) model;
                VdcQueryReturnValue returnValueObject = (VdcQueryReturnValue) returnValue;
                if (returnValueObject.getSucceeded()) {
                    unfilteredResultList = (List<Erratum>) returnValueObject.getReturnValue();
                    // manual client-side filter
                    setItems(filter(unfilteredResultList));
                }
                else {
                    errataListModel.setMessage(
                            constants.katelloProblemRetrievingErrata() + " " + returnValueObject.getExceptionMessage()); //$NON-NLS-1$
                }
            }
        };

        Frontend.getInstance().runQuery(getQueryType(), new IdQueryParameters(guid), _asyncQuery);
    }

    protected Collection<EntityModel<Erratum>> filter(Collection<Erratum> resultList) {
        List<EntityModel<Erratum>> ret = new ArrayList<>();
        for (Erratum e : resultList) {
            if ((filter == null || (filter.isSecurity() && filter.isBugs() && filter.isEnhancements())) ||
                    (filter.isSecurity() && e.getType() == Erratum.ErrataType.SECURITY) ||
                    (filter.isBugs() && e.getType() == Erratum.ErrataType.BUGFIX) ||
                    (filter.isEnhancements() && e.getType() == Erratum.ErrataType.ENHANCEMENT) ) {
                EntityModel<Erratum> erratum = new EntityModel<>();
                erratum.setEntity(e);
                ret.add(erratum);
            }
        }

        return ret;
    }

    public void setGuid(Guid id) {
        this.guid = id;
    }

    public Guid getGuid() {
        return guid;
    }

    public void setItemsFilter(ErrataFilterValue filter) {
        this.filter = filter;
    }

    public void reFilter() {
        setItems(filter(unfilteredResultList));
    }

    /**
     * Override and return the place token.
     */
    protected String getPlace() {
        return null;
    }

    protected abstract VdcQueryType getQueryType();
}

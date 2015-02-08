package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ValueEventArgs;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public abstract class SanStorageModel extends SanStorageModelBase
{
    private boolean isGrouppedByTarget;

    /**
     * Gets or sets the value determining whether the items containing target/LUNs or LUN/targets.
     */
    public boolean getIsGrouppedByTarget()
    {
        return isGrouppedByTarget;
    }

    public void setIsGrouppedByTarget(boolean value)
    {
        if (isGrouppedByTarget != value)
        {
            isGrouppedByTarget = value;
            isGrouppedByTargetChanged();
            onPropertyChanged(new PropertyChangedEventArgs("IsGrouppedByTarget")); //$NON-NLS-1$
        }
    }

    private String getLUNsFailure;

    public String getGetLUNsFailure()
    {
        return getLUNsFailure;
    }

    public void setGetLUNsFailure(String value)
    {
        if (!ObjectUtils.objectsEqual(getLUNsFailure, value))
        {
            getLUNsFailure = value;
            onPropertyChanged(new PropertyChangedEventArgs("GetLUNsFailure")); //$NON-NLS-1$
        }
    }

    private StorageDomain storageDomain;

    public StorageDomain getStorageDomain() {
        return storageDomain;
    }

    public void setStorageDomain(StorageDomain storageDomain) {
        this.storageDomain = storageDomain;
    }

    private boolean force;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    private final List<LunModel> includedLUNs;
    private final ArrayList<SanTargetModel> lastDiscoveredTargets;
    private boolean isTargetModelList;

    protected SanStorageModel()
    {
        includedLUNs = new ArrayList<LunModel>();
        lastDiscoveredTargets = new ArrayList<SanTargetModel>();

        initializeItems(null, null);
    }

    @Override
    protected void postDiscoverTargets(ArrayList<SanTargetModel> newItems)
    {
        super.postDiscoverTargets(newItems);

        initializeItems(null, newItems);

        // Remember all discovered targets.
        lastDiscoveredTargets.clear();
        lastDiscoveredTargets.addAll(newItems);
    }

    @Override
    protected void update()
    {
        lastDiscoveredTargets.clear();

        super.update();
    }

    @Override
    protected void updateInternal()
    {
        super.updateInternal();

        if (!(getContainer().isNewStorage() || getContainer().isStorageActive())) {
            return;
        }

        VDS host = (VDS) getContainer().getHost().getSelectedItem();
        if (host == null)
        {
            proposeDiscover();
            return;
        }

        clearItems();
        initializeItems(null, null);

        final SanStorageModel model = this;
        Object target = getWidgetModel() != null ? getWidgetModel() : getContainer();
        AsyncQuery asyncQuery = new AsyncQuery(target, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                VdcQueryReturnValue response = (VdcQueryReturnValue) returnValue;
                if (response.getSucceeded()) {
                    model.applyData((ArrayList<LUNs>) response.getReturnValue(), false);
                    model.setGetLUNsFailure(""); //$NON-NLS-1$
                }
                else {
                    model.setGetLUNsFailure(
                            ConstantsManager.getInstance().getConstants().couldNotRetrieveLUNsLunsFailure());
                }
            }
        }, true);
        Frontend.getInstance().runQuery(VdcQueryType.GetDeviceList,
                new GetDeviceListQueryParameters(host.getId(), getType()),
                asyncQuery);
    }

    private void clearItems()
    {
        if (getItems() == null)
        {
            return;
        }

        if (getIsGrouppedByTarget())
        {
            List<SanTargetModel> items = (List<SanTargetModel>) getItems();

            for (SanTargetModel target : Linq.toList(items))
            {
                boolean found = false;

                // Ensure remove targets that are not in last dicovered targets list.
                if (Linq.firstOrDefault(lastDiscoveredTargets, new Linq.TargetPredicate(target)) != null)
                {
                    found = true;
                }
                else
                {
                    // Ensure remove targets that are not contain already included LUNs.
                    for (LunModel lun : target.getLuns())
                    {
                        LunModel foundItem = Linq.firstOrDefault(includedLUNs, new Linq.LunPredicate(lun));
                        if (foundItem == null)
                        {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found)
                {
                    items.remove(target);
                }
            }
        }
        else
        {
            List<LunModel> items = (List<LunModel>) getItems();

            // Ensure remove targets that are not contain already included LUNs.
            for (LunModel lun : Linq.toList(items))
            {
                LunModel foundItem = Linq.firstOrDefault(includedLUNs, new Linq.LunPredicate(lun));
                if (foundItem == null)
                {
                    items.remove(lun);
                }
            }
        }
    }

    /**
     * Creates model items from the provided list of business entities.
     */
    public void applyData(List<LUNs> source, boolean isIncluded)
    {
        ArrayList<LunModel> newItems = new ArrayList<LunModel>();

        for (LUNs a : source)
        {
            if (a.getLunType() == getType() || a.getLunType() == StorageType.UNKNOWN)
            {
                ArrayList<SanTargetModel> targets = createTargetModelList(a);

                LunModel lunModel = new LunModel();
                lunModel.setLunId(a.getLUN_id());
                lunModel.setVendorId(a.getVendorId());
                lunModel.setProductId(a.getProductId());
                lunModel.setSerial(a.getSerial());
                lunModel.setMultipathing(a.getPathCount());
                lunModel.setTargets(targets);
                lunModel.setSize(a.getDeviceSize());
                lunModel.setIsAccessible(a.getAccessible());
                lunModel.setStatus(a.getStatus());
                lunModel.setIsIncluded(isIncluded);
                lunModel.setIsSelected(isIncluded);
                lunModel.setEntity(a);

                // Add LunModel
                newItems.add(lunModel);

                // Update isGrayedOut and grayedOutReason properties
                updateGrayedOut(lunModel);

                // Remember included LUNs to prevent their removal while updating items.
                if (isIncluded)
                {
                    includedLUNs.add(lunModel);
                }
            }
        }

        initializeItems(newItems, null);
        proposeDiscover();
    }

    private ArrayList<SanTargetModel> createTargetModelList(LUNs a) {
        ArrayList<SanTargetModel> targetModelList = new ArrayList<SanTargetModel>();
        if (a.getLunConnections() != null)
        {
            for (StorageServerConnections b : a.getLunConnections())
            {
                SanTargetModel tempVar = new SanTargetModel();
                tempVar.setAddress(b.getconnection());
                tempVar.setPort(b.getport());
                tempVar.setName(b.getiqn());
                tempVar.setIsSelected(true);
                tempVar.setIsLoggedIn(true);
                tempVar.setLuns(new ObservableCollection<LunModel>());
                SanTargetModel model = tempVar;
                model.getLoginCommand().setIsExecutionAllowed(false);

                targetModelList.add(model);
            }
        }
        return targetModelList;
    }

    private void updateGrayedOut(LunModel lunModel) {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        UIMessages messages = ConstantsManager.getInstance().getMessages();

        LUNs lun = (LUNs) lunModel.getEntity();
        boolean nonEmpty = lun.getStorageDomainId() != null || lun.getDiskId() != null ||
                lun.getStatus() == LunStatus.Unusable;

        // Graying out LUNs
        lunModel.setIsGrayedOut(isIgnoreGrayedOut() ? lun.getDiskId() != null : nonEmpty);

        // Adding 'GrayedOutReasons'
        if (lun.getDiskId() != null) {
            lunModel.getGrayedOutReasons().add(
                    messages.lunUsedByDiskWarning(lun.getDiskAlias()));
        }
        else if (lun.getStorageDomainId() != null) {
            lunModel.getGrayedOutReasons().add(
                    messages.lunAlreadyPartOfStorageDomainWarning(lun.getStorageDomainName()));
        }
        else if (lun.getStatus() == LunStatus.Unusable) {
            lunModel.getGrayedOutReasons().add(
                    constants.lunUnusable());
        }
    }

    private void isGrouppedByTargetChanged()
    {
        initializeItems(null, null);
    }

    /**
     * Organizes items according to the current groupping flag. When new items provided takes them in account and add to
     * the Items collection.
     */
    protected void initializeItems(List<LunModel> newLuns, List<SanTargetModel> newTargets)
    {
        if (getIsGrouppedByTarget())
        {
            if (getItems() == null)
            {
                setItems(new ObservableCollection<SanTargetModel>());
                isTargetModelList = true;
            }
            else
            {
                // Convert to list of another type as neccessary.
                if (!isTargetModelList)
                {
                    setItems(toTargetModelList((List<LunModel>) getItems()));
                }
            }

            ArrayList<SanTargetModel> items = new ArrayList<SanTargetModel>();
            items.addAll((List<SanTargetModel>) getItems());

            // Add new targets.
            if (newTargets != null)
            {
                for (SanTargetModel newItem : newTargets)
                {
                    if (Linq.firstOrDefault(items, new Linq.TargetPredicate(newItem)) == null)
                    {
                        items.add(newItem);
                    }
                }
            }

            // Merge luns into targets.
            if (newLuns != null)
            {
                mergeLunsToTargets(newLuns, items);
            }

            setItems(items);

            updateLoginAvailability();
        }
        else
        {
            if (getItems() == null)
            {
                setItems(new ObservableCollection<LunModel>());
                isTargetModelList = false;
            }
            else
            {
                // Convert to list of another type as neccessary.
                if (isTargetModelList)
                {
                    setItems(toLunModelList((List<SanTargetModel>) getItems()));
                }
            }

            ArrayList<LunModel> items = new ArrayList<LunModel>();
            items.addAll((List<LunModel>) getItems());

            // Add new LUNs.
            if (newLuns != null)
            {
                for (LunModel newItem : newLuns)
                {
                    LunModel existingItem = Linq.firstOrDefault(items, new Linq.LunPredicate(newItem));
                    if (existingItem == null)
                    {
                        items.add(newItem);
                    }
                    else
                    {
                        existingItem.setIsIncluded(existingItem.getIsIncluded() || newItem.getIsIncluded());
                    }
                }
            }

            setItems(items);
        }

        if (!isMultiSelection() && newLuns != null) {
            addLunModelSelectionEventListeners(newLuns);
        }
    }

    private void addLunModelSelectionEventListeners(List<LunModel> luns) {
        for (LunModel lun : luns) {
            // Adding PropertyEventListener to LunModel if needed
            if (!lun.getPropertyChangedEvent().getListeners().contains(lunModelEventListener)) {
                lun.getPropertyChangedEvent().addListener(lunModelEventListener);
            }
        }
    }

    private void mergeLunsToTargets(List<LunModel> newLuns, List<SanTargetModel> targets)
    {
        for (LunModel lun : newLuns)
        {
            for (SanTargetModel target : lun.getTargets())
            {
                SanTargetModel item = Linq.firstOrDefault(targets, new Linq.TargetPredicate(target));
                if (item == null)
                {
                    item = target;
                    targets.add(item);
                }

                LunModel currLun = Linq.firstOrDefault(item.getLuns(), new Linq.LunPredicate(lun));
                if (currLun == null) {
                    item.getLuns().add(lun);
                } else {
                    currLun.setLunId(lun.getLunId());
                    currLun.setVendorId(lun.getVendorId());
                    currLun.setProductId(lun.getProductId());
                    currLun.setSerial(lun.getSerial());
                    currLun.setMultipathing(lun.getMultipathing());
                    currLun.setTargets(createTargetModelList((LUNs) lun.getEntity()));
                    currLun.setSize(lun.getSize());
                    currLun.setIsAccessible(lun.getIsAccessible());
                    currLun.setStatus(lun.getStatus());
                    currLun.setIsIncluded(lun.getIsIncluded());
                    currLun.setIsSelected(lun.getIsSelected());
                    currLun.setEntity(lun.getEntity());
                }
            }
        }
    }

    private EventDefinition lunSelectionChangedEventDefinition = new EventDefinition("lunSelectionChanged", SanStorageModel.class); //$NON-NLS-1$
    private Event lunSelectionChangedEvent = new Event(lunSelectionChangedEventDefinition);

    public Event getLunSelectionChangedEvent() {
        return lunSelectionChangedEvent;
    }

    final IEventListener<PropertyChangedEventArgs> lunModelEventListener = new IEventListener<PropertyChangedEventArgs>() {
        @Override
        public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
            String propName = args.propertyName;
            if (propName.equals("IsSelected")) { //$NON-NLS-1$
                LunModel selectedLunModel = (LunModel) sender;

                if (!selectedLunModel.getIsSelected() || !getItems().iterator().hasNext()) {
                    return;
                }

                // Clear LUNs selection
                for (Model model : (List<Model>) getItems()) {
                    if (model instanceof LunModel) {
                        LunModel lunModel = (LunModel) model;
                        if (!lunModel.equals(selectedLunModel)) {
                            lunModel.setIsSelected(false);
                        }
                    }
                    else {
                        SanTargetModel sanTargetModel = (SanTargetModel) model;
                        boolean isIncludeSelected = false;

                        for (LunModel lunModel : sanTargetModel.getLuns()) {
                            if (!lunModel.equals(selectedLunModel)) {
                                lunModel.setIsSelected(false);
                            }
                            else {
                                isIncludeSelected = true;
                            }
                        }

                        if (!isIncludeSelected && sanTargetModel.getLunsList().getSelectedItem() != null) {
                            sanTargetModel.getLunsList().setSelectedItem(null);
                        }
                    }
                }
                lunSelectionChangedEvent.raise(this, new ValueEventArgs<LunModel>(selectedLunModel));
            }
        }
    };

    private List<SanTargetModel> toTargetModelList(List<LunModel> source)
    {
        ObservableCollection<SanTargetModel> list = new ObservableCollection<SanTargetModel>();

        for (LunModel lun : source)
        {
            for (SanTargetModel target : lun.getTargets())
            {
                SanTargetModel item = Linq.firstOrDefault(list, new Linq.TargetPredicate(target));
                if (item == null)
                {
                    item = target;
                    list.add(item);
                }

                if (Linq.firstOrDefault(item.getLuns(), new Linq.LunPredicate(lun)) == null)
                {
                    item.getLuns().add(lun);
                }
            }
        }

        // Merge with last discovered targets list.
        for (SanTargetModel target : lastDiscoveredTargets)
        {
            if (Linq.firstOrDefault(list, new Linq.TargetPredicate(target)) == null)
            {
                list.add(target);
            }
        }

        isTargetModelList = true;

        return list;
    }

    private List<LunModel> toLunModelList(List<SanTargetModel> source)
    {
        ObservableCollection<LunModel> list = new ObservableCollection<LunModel>();

        for (SanTargetModel target : source)
        {
            for (LunModel lun : target.getLuns())
            {
                LunModel item = Linq.firstOrDefault(list, new Linq.LunPredicate(lun));
                if (item == null)
                {
                    item = lun;
                    list.add(item);
                }

                if (Linq.firstOrDefault(item.getTargets(), new Linq.TargetPredicate(target)) == null)
                {
                    item.getTargets().add(target);
                }
            }
        }

        isTargetModelList = false;

        return list;
    }

    protected void proposeDiscover() {
        setProposeDiscoverTargets(getItems() == null || Linq.count(getItems()) == 0);
    }

    @Override
    protected void isAllLunsSelectedChanged()
    {
        if (!getIsGrouppedByTarget())
        {
            List<LunModel> items = (List<LunModel>) getItems();
            for (LunModel lun : items)
            {
                if (!lun.getIsIncluded() && lun.getIsAccessible())
                {
                    lun.setIsSelected(getIsAllLunsSelected());
                }
            }
        }
    }

    public ArrayList<LunModel> getAddedLuns()
    {
        ArrayList<LunModel> luns = new ArrayList<LunModel>();
        if (getIsGrouppedByTarget())
        {
            List<SanTargetModel> items = (List<SanTargetModel>) getItems();
            for (SanTargetModel item : items)
            {
                for (LunModel lun : item.getLuns())
                {
                    if (lun.getIsSelected() && !lun.getIsIncluded()
                            && Linq.firstOrDefault(luns, new Linq.LunPredicate(lun)) == null)
                    {
                        luns.add(lun);
                    }
                }
            }
        }
        else
        {
            List<LunModel> items = (List<LunModel>) getItems();
            for (LunModel lun : items)
            {
                if (lun.getIsSelected() && !lun.getIsIncluded()
                        && Linq.firstOrDefault(luns, new Linq.LunPredicate(lun)) == null)
                {
                    luns.add(lun);
                }
            }
        }

        return luns;
    }

    public ArrayList<String> getUsedLunsMessages() {
        ArrayList<String> usedLunsMessages = new ArrayList<String>();
        UIMessages messages = ConstantsManager.getInstance().getMessages();

        for (LunModel lunModel : getAddedLuns()) {
            if (lunModel.getStatus() == LunStatus.Used) {
                String reason = null;
                LUNs lun = (LUNs) lunModel.getEntity();

                if (lun.getvolume_group_id() != null && !lun.getvolume_group_id().isEmpty()) {
                    reason = messages.lunUsedByVG(lun.getvolume_group_id());
                }

                usedLunsMessages.add(reason == null ? lunModel.getLunId() :
                        lunModel.getLunId() + " (" + reason + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return usedLunsMessages;
    }

    public ArrayList<String> getPartOfSdLunsMessages() {
        ArrayList<String> partOfSdLunsMessages = new ArrayList<String>();
        UIMessages messages = ConstantsManager.getInstance().getMessages();

        for (LunModel lunModel : getAddedLuns()) {
            String reason = null;
            LUNs lun = (LUNs) lunModel.getEntity();

            if (lun.getStorageDomainId() != null) {
                reason = messages.lunAlreadyPartOfStorageDomainWarning(lun.getStorageDomainName());
                partOfSdLunsMessages.add(lunModel.getLunId() + " (" + reason + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return partOfSdLunsMessages;
    }

    @Override
    public boolean validate()
    {
        boolean isValid = getAddedLuns().size() > 0 || includedLUNs.size() > 0;

        if (!isValid)
        {
            getInvalidityReasons().add(ConstantsManager.getInstance().getConstants().noLUNsSelectedInvalidReason());
        }

        setIsValid(isValid);

        return super.validate() && getIsValid();
    }
}

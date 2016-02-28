package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ValueEventArgs;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public abstract class SanStorageModel extends SanStorageModelBase {
    private boolean isGrouppedByTarget;

    /**
     * Gets or sets the value determining whether the items containing target/LUNs or LUN/targets.
     */
    public boolean getIsGrouppedByTarget() {
        return isGrouppedByTarget;
    }

    public void setIsGrouppedByTarget(boolean value) {
        if (isGrouppedByTarget != value) {
            isGrouppedByTarget = value;
            isGrouppedByTargetChanged();
            onPropertyChanged(new PropertyChangedEventArgs("IsGrouppedByTarget")); //$NON-NLS-1$
        }
    }

    private String getLUNsFailure;

    public String getGetLUNsFailure() {
        return getLUNsFailure;
    }

    public void setGetLUNsFailure(String value) {
        if (!Objects.equals(getLUNsFailure, value)) {
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

    protected SanStorageModel() {
        includedLUNs = new ArrayList<>();
        lastDiscoveredTargets = new ArrayList<>();

        initializeItems(null, null);
    }

    @Override
    protected void postDiscoverTargets(ArrayList<SanTargetModel> newItems) {
        super.postDiscoverTargets(newItems);

        initializeItems(null, newItems);

        // Remember all discovered targets.
        lastDiscoveredTargets.clear();
        lastDiscoveredTargets.addAll(newItems);
    }

    @Override
    protected void update() {
        lastDiscoveredTargets.clear();

        super.update();
    }

    @Override
    protected void updateInternal() {
        super.updateInternal();

        if (!(getContainer().isNewStorage() || getContainer().isStorageActive())) {
            return;
        }

        VDS host = getContainer().getHost().getSelectedItem();
        if (host == null) {
            proposeDiscover();
            return;
        }

        final Collection<EntityModel<?>> prevSelected = Linq.findSelectedItems((Collection<EntityModel<?>>) getSelectedItem());
        clearItems();
        initializeItems(null, null);

        final SanStorageModel model = this;
        Object target = getWidgetModel() != null ? getWidgetModel() : getContainer();
        AsyncQuery asyncQuery = new AsyncQuery(target, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                VdcQueryReturnValue response = (VdcQueryReturnValue) returnValue;
                if (response.getSucceeded()) {
                    model.applyData((ArrayList<LUNs>) response.getReturnValue(), false, prevSelected);
                    model.setGetLUNsFailure(""); //$NON-NLS-1$
                }
                else {
                    model.setGetLUNsFailure(
                            ConstantsManager.getInstance().getConstants().couldNotRetrieveLUNsLunsFailure());
                }
            }
        }, true);
        Frontend.getInstance().runQuery(VdcQueryType.GetDeviceList,
                new GetDeviceListQueryParameters(host.getId(), getType(), false, null),
                asyncQuery);
    }

    private void clearItems() {
        if (getItems() == null) {
            return;
        }

        if (getIsGrouppedByTarget()) {
            List<SanTargetModel> items = (List<SanTargetModel>) getItems();

            for (SanTargetModel target : Linq.toList(items)) {
                boolean found = false;

                // Ensure remove targets that are not in last dicovered targets list.
                if (Linq.firstOrNull(lastDiscoveredTargets, new Linq.TargetPredicate(target)) != null) {
                    found = true;
                }
                else {
                    // Ensure remove targets that are not contain already included LUNs.
                    for (LunModel lun : target.getLuns()) {
                        LunModel foundItem = Linq.firstOrNull(includedLUNs, new Linq.LunPredicate(lun));
                        if (foundItem == null) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    items.remove(target);
                }
            }
        }
        else {
            List<LunModel> items = (List<LunModel>) getItems();

            // Ensure remove targets that are not contain already included LUNs.
            for (LunModel lun : Linq.toList(items)) {
                LunModel foundItem = Linq.firstOrNull(includedLUNs, new Linq.LunPredicate(lun));
                if (foundItem == null) {
                    items.remove(lun);
                }
            }
        }
    }

    /**
     * Creates model items from the provided list of business entities.
     */
    public void applyData(List<LUNs> source, boolean isIncluded, Collection<EntityModel<?>> selectedItems) {
        ArrayList<LunModel> newItems = new ArrayList<>();

        for (LUNs a : source) {
            if (a.getLunType() == getType() || a.getLunType() == StorageType.UNKNOWN) {
                ArrayList<SanTargetModel> targets = createTargetModelList(a);

                LunModel lunModel = new LunModel();
                lunModel.setLunId(a.getLUNId());
                lunModel.setVendorId(a.getVendorId());
                lunModel.setProductId(a.getProductId());
                lunModel.setSerial(a.getSerial());
                lunModel.setMultipathing(a.getPathCount());
                lunModel.setTargets(targets);
                lunModel.setSize(a.getDeviceSize());
                lunModel.setAdditionalAvailableSize(getAdditionalAvailableSize(a));
                lunModel.setAdditionalAvailableSizeSelected(false);
                lunModel.setIsAccessible(a.getAccessible());
                lunModel.setStatus(a.getStatus());
                lunModel.setIsIncluded(isIncluded);
                lunModel.setIsSelected(containsLun(lunModel, selectedItems, isIncluded));
                lunModel.setEntity(a);

                // Add LunModel
                newItems.add(lunModel);

                // Update isGrayedOut and grayedOutReason properties
                updateGrayedOut(lunModel);

                // Remember included LUNs to prevent their removal while updating items.
                if (isIncluded) {
                    includedLUNs.add(lunModel);
                }
            }
        }

        initializeItems(newItems, null);
        proposeDiscover();
    }

    private int getAdditionalAvailableSize(LUNs lun) {
        int pvSize = lun.getPvSize();
        if (pvSize == 0) {
            return 0;
        }
        // The PV size is always smaller by 1 GB from the device due to LVM metadata
        int additionalAvailableSize = lun.getDeviceSize() - pvSize - 1;
        if (additionalAvailableSize < 0) {
            additionalAvailableSize = 0;
        }
        return additionalAvailableSize;
    }

    private boolean containsLun(LunModel lunModel, Collection<EntityModel<?>> models, boolean isIncluded) {
        if (models == null) {
            return isIncluded;
        }

        for (EntityModel<?> model : models) {
            if (model instanceof LunModel) {
                if (((LunModel) model).getLunId().equals(lunModel.getLunId())) {
                    return true;
                }
            }
        }

        return false;
    }

    private ArrayList<SanTargetModel> createTargetModelList(LUNs a) {
        ArrayList<SanTargetModel> targetModelList = new ArrayList<>();
        if (a.getLunConnections() != null) {
            for (StorageServerConnections b : a.getLunConnections()) {
                SanTargetModel model = new SanTargetModel();
                model.setAddress(b.getConnection());
                model.setPort(b.getPort());
                model.setName(b.getIqn());
                model.setIsSelected(true);
                model.setIsLoggedIn(true);
                model.setLuns(new ObservableCollection<LunModel>());
                model.getLoginCommand().setIsExecutionAllowed(false);

                targetModelList.add(model);
            }
        }
        return targetModelList;
    }

    private void updateGrayedOut(LunModel lunModel) {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        UIMessages messages = ConstantsManager.getInstance().getMessages();

        LUNs lun = lunModel.getEntity();
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

    private void isGrouppedByTargetChanged() {
        initializeItems(null, null);
    }

    /**
     * Organizes items according to the current groupping flag. When new items provided takes them in account and add to
     * the Items collection.
     */
    protected void initializeItems(List<LunModel> newLuns, List<SanTargetModel> newTargets) {
        if (getIsGrouppedByTarget()) {
            if (getItems() == null) {
                setItems(new ObservableCollection<SanTargetModel>());
                isTargetModelList = true;
            }
            else {
                // Convert to list of another type as neccessary.
                if (!isTargetModelList) {
                    setItems(toTargetModelList((List<LunModel>) getItems()));
                }
            }

            ArrayList<SanTargetModel> items = new ArrayList<>();
            items.addAll((List<SanTargetModel>) getItems());

            // Add new targets.
            if (newTargets != null) {
                for (SanTargetModel newItem : newTargets) {
                    if (Linq.firstOrNull(items, new Linq.TargetPredicate(newItem)) == null) {
                        items.add(newItem);
                    }
                }
            }

            // Merge luns into targets.
            if (newLuns != null) {
                mergeLunsToTargets(newLuns, items);
            }

            setItems(items);

            updateLoginAvailability();
        }
        else {
            if (getItems() == null) {
                setItems(new ObservableCollection<LunModel>());
                isTargetModelList = false;
            }
            else {
                // Convert to list of another type as neccessary.
                if (isTargetModelList) {
                    setItems(toLunModelList((List<SanTargetModel>) getItems()));
                }
            }

            ArrayList<LunModel> items = new ArrayList<>();
            items.addAll((List<LunModel>) getItems());

            // Add new LUNs.
            if (newLuns != null) {
                for (LunModel newItem : newLuns) {
                    LunModel existingItem = Linq.firstOrNull(items, new Linq.LunPredicate(newItem));
                    if (existingItem == null) {
                        items.add(newItem);
                    }
                    else {
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

    private void mergeLunsToTargets(List<LunModel> newLuns, List<SanTargetModel> targets) {
        for (LunModel lun : newLuns) {
            for (SanTargetModel target : lun.getTargets()) {
                SanTargetModel item = Linq.firstOrNull(targets, new Linq.TargetPredicate(target));
                if (item == null) {
                    item = target;
                    targets.add(item);
                }

                LunModel currLun = Linq.firstOrNull(item.getLuns(), new Linq.LunPredicate(lun));
                if (currLun == null) {
                    item.getLuns().add(lun);
                } else {
                    currLun.setLunId(lun.getLunId());
                    currLun.setVendorId(lun.getVendorId());
                    currLun.setProductId(lun.getProductId());
                    currLun.setSerial(lun.getSerial());
                    currLun.setMultipathing(lun.getMultipathing());
                    currLun.setTargets(createTargetModelList(lun.getEntity()));
                    currLun.setSize(lun.getSize());
                    currLun.setAdditionalAvailableSize(lun.getAdditionalAvailableSize());
                    currLun.setAdditionalAvailableSizeSelected(lun.isAdditionalAvailableSizeSelected());
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
                lunSelectionChangedEvent.raise(this, new ValueEventArgs<>(selectedLunModel));
            }
        }
    };

    private List<SanTargetModel> toTargetModelList(List<LunModel> source) {
        ObservableCollection<SanTargetModel> list = new ObservableCollection<>();

        for (LunModel lun : source) {
            for (SanTargetModel target : lun.getTargets()) {
                SanTargetModel item = Linq.firstOrNull(list, new Linq.TargetPredicate(target));
                if (item == null) {
                    item = target;
                    list.add(item);
                }

                if (Linq.firstOrNull(item.getLuns(), new Linq.LunPredicate(lun)) == null) {
                    item.getLuns().add(lun);
                }
            }
        }

        // Merge with last discovered targets list.
        for (SanTargetModel target : lastDiscoveredTargets) {
            if (Linq.firstOrNull(list, new Linq.TargetPredicate(target)) == null) {
                list.add(target);
            }
        }

        isTargetModelList = true;

        return list;
    }

    private List<LunModel> toLunModelList(List<SanTargetModel> source) {
        ObservableCollection<LunModel> list = new ObservableCollection<>();

        for (SanTargetModel target : source) {
            for (LunModel lun : target.getLuns()) {
                LunModel item = Linq.firstOrNull(list, new Linq.LunPredicate(lun));
                if (item == null) {
                    item = lun;
                    list.add(item);
                }

                if (Linq.firstOrNull(item.getTargets(), new Linq.TargetPredicate(target)) == null) {
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
    protected void isAllLunsSelectedChanged() {
        if (!getIsGrouppedByTarget()) {
            List<LunModel> items = (List<LunModel>) getItems();
            for (LunModel lun : items) {
                if (!lun.getIsIncluded() && lun.getIsAccessible()) {
                    lun.setIsSelected(getIsAllLunsSelected());
                }
            }
        }
    }

    public ArrayList<LunModel> getAddedLuns() {
        ArrayList<LunModel> luns = new ArrayList<>();
        if (getIsGrouppedByTarget()) {
            List<SanTargetModel> items = (List<SanTargetModel>) getItems();
            for (SanTargetModel item : items) {
                for (LunModel lun : item.getLuns()) {
                    if (lun.getIsSelected() && !lun.getIsIncluded()
                            && Linq.firstOrNull(luns, new Linq.LunPredicate(lun)) == null) {
                        luns.add(lun);
                    }
                }
            }
        }
        else {
            List<LunModel> items = (List<LunModel>) getItems();
            for (LunModel lun : items) {
                if (lun.getIsSelected() && !lun.getIsIncluded()
                        && Linq.firstOrNull(luns, new Linq.LunPredicate(lun)) == null) {
                    luns.add(lun);
                }
            }
        }

        return luns;
    }

    public ArrayList<LunModel> getLunsToRefresh() {
        ArrayList<LunModel> luns = new ArrayList<>();
        if (!getIsGrouppedByTarget()) {
            List<LunModel> items = (List<LunModel>) getItems();
            for (LunModel lun : items) {
                if (lun.getIsIncluded()) {
                    if (lun.isAdditionalAvailableSizeSelected()
                            && Linq.firstOrNull(luns, new Linq.LunPredicate(lun)) == null) {
                        luns.add(lun);
                    }
                }
            }
        }
        return luns;
    }

    public ArrayList<String> getUsedLunsMessages(List<LUNs> luns) {
        ArrayList<String> usedLunsMessages = new ArrayList<>();
        UIMessages messages = ConstantsManager.getInstance().getMessages();

        for (LUNs lun : luns) {
            if (lun.getStatus() == LunStatus.Used) {
                String reason = null;

                if (lun.getVolumeGroupId() != null && !lun.getVolumeGroupId().isEmpty()) {
                    reason = messages.lunUsedByVG(lun.getVolumeGroupId());
                }

                usedLunsMessages.add(reason == null ? lun.getLUNId() :
                        messages.usedLunIdReason(lun.getLUNId(), reason));
            }
        }

        return usedLunsMessages;
    }

    public ArrayList<String> getPartOfSdLunsMessages() {
        ArrayList<String> partOfSdLunsMessages = new ArrayList<>();
        UIMessages messages = ConstantsManager.getInstance().getMessages();

        for (LunModel lunModel : getAddedLuns()) {
            LUNs lun = lunModel.getEntity();

            if (lun.getStorageDomainId() != null) {
                String reason = messages.lunAlreadyPartOfStorageDomainWarning(lun.getStorageDomainName());
                partOfSdLunsMessages.add(lunModel.getLunId() + " (" + reason + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return partOfSdLunsMessages;
    }

    @Override
    public boolean validate() {
        boolean isValid = getAddedLuns().size() > 0 || includedLUNs.size() > 0;

        if (!isValid) {
            getInvalidityReasons().add(ConstantsManager.getInstance().getConstants().noLUNsSelectedInvalidReason());
        }

        setIsValid(isValid);

        return super.validate() && getIsValid();
    }

    public boolean isEditable(StorageDomain storage) {
        return getContainer().isStorageActive() || getContainer().isNewStorage();
    }

    public void prepareForEdit(final StorageDomain storage) {
        if (isEditable(storage)) {
            final SanStorageModel thisModel = this;
            getContainer().getHost().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    postPrepareSanStorageForEdit(thisModel, true, storage);
                }
            });
        }
        else {
            postPrepareSanStorageForEdit(this, false, storage);
        }
    }

    private void postPrepareSanStorageForEdit(final SanStorageModel model, boolean isStorageActive, StorageDomain storage) {
        model.setStorageDomain(storage);

        VDS host = getContainer().getHost().getSelectedItem();
        Guid hostId = host != null && isStorageActive ? host.getId() : null;

        AsyncDataProvider.getInstance().getLunsByVgId(new AsyncQuery(getContainer(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ArrayList<LUNs> lunList = (ArrayList<LUNs>) returnValue;
                model.applyData(lunList, true, Linq.findSelectedItems((Collection<EntityModel<?>>) getSelectedItem()));
            }
        }), storage.getStorage(), hostId);
    }
}

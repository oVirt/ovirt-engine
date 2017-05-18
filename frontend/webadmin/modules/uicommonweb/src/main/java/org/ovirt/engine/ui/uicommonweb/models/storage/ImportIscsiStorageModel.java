package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ImportIscsiStorageModel extends ImportSanStorageModel {

    protected ListModel<SanTargetModel> targets;

    public ImportIscsiStorageModel() {
        setStorageDomains(new ListModel<>());
        getStorageDomains().setItems(new ArrayList<>());

        setTargets(new ListModel<>());
        getTargets().setItems(new ArrayList<>());

        addListeners();
    }

    private void addListeners() {
        getTargets().getSelectedItemsChangedEvent().addListener((ev, sender, args) -> getLoginCommand().setIsExecutionAllowed(getTargets().getSelectedItems() != null &&
                !getTargets().getSelectedItems().isEmpty()));
    }

    @Override
    public StorageType getType() {
        return StorageType.ISCSI;
    }

    @Override
    public String getLoginButtonLabel() {
        return ConstantsManager.getInstance().getConstants().loginButtonLabel();
    }

    public ListModel<SanTargetModel> getTargets() {
        return targets;
    }

    public void setTargets(ListModel<SanTargetModel> targets) {
        this.targets = targets;
    }

    @Override
    protected void update() {
        getStorageDomains().setItems(new ArrayList<>());
        getTargets().setItems(new ArrayList<>());
        proposeDiscover();
    }

    @Override
    protected void proposeDiscover() {
        boolean proposeDiscover = getTargets().getItems().isEmpty();
        setProposeDiscoverTargets(proposeDiscover);
    }

    protected void postDiscoverTargetsInternal(ArrayList<StorageServerConnections> newItems) {
        if (newItems.isEmpty()) {
            setMessage(ConstantsManager.getInstance().getConstants().noNewDevicesWereFoundMsg());
            return;
        }

        SortedSet<SanTargetModel> targetsSet = getTargetsSet();
        targetsSet.addAll(getTargets().getItems());
        targetsSet.addAll(getSanTargetModels(newItems.iterator()));
        getTargets().setItems(new ArrayList<>(targetsSet));

        proposeDiscover();
    }

    private SortedSet<SanTargetModel> getTargetsSet() {
        return new TreeSet<>(Comparator.comparing(t -> t.getEntity().getIqn(), new LexoNumericComparator()));
    }

    private List<SanTargetModel> getSanTargetModels(Iterator<StorageServerConnections> itemsIterator) {
        List<SanTargetModel> targets = new ArrayList<>();
        while (itemsIterator.hasNext()) {
            StorageServerConnections connection = itemsIterator.next();
            SanTargetModel targetModel = getSanTargetModelByConnection(connection);
            if (targetModel == null) {
                targetModel = new SanTargetModel();
                targetModel.setEntity(connection);
            }
            targets.add(targetModel);
        }
        return targets;
    }

    private SanTargetModel getSanTargetModelByConnection(StorageServerConnections connection) {
        for (SanTargetModel targetModel : getTargets().getItems()) {
            if (targetModel.getEntity().getIqn().equals(connection.getIqn())) {
                return targetModel;
            }
        }
        return null;
    }

    private List<StorageServerConnections> getStorageServerConnections(List<SanTargetModel> targetModels) {
        return targetModels.stream().map(SanTargetModel::getEntity).collect(Collectors.toList());
    }

    @Override
    protected void login() {
        getStorageDomainsBySelectedConnections();
    }

    private void markLoggedinTargets(List<SanTargetModel> targets) {
        for (SanTargetModel targetModel : targets) {
            targetModel.setIsLoggedIn(true);
            targetModel.setIsChangeable(false);
        }
        getTargets().setSelectedItems(new ArrayList<>());
    }

    @Override
    protected void postGetUnregisteredStorageDomains(List<StorageDomain> storageDomains, List<StorageServerConnections> connections) {
        if (connections != null) {
            markLoggedinTargets(getSanTargetModels(connections.iterator()));
        }
    }

    private void getStorageDomainsBySelectedConnections() {
        List<StorageServerConnections> connections = getStorageServerConnections(getTargets().getSelectedItems());
        getUnregisteredStorageDomains(connections);
    }
}

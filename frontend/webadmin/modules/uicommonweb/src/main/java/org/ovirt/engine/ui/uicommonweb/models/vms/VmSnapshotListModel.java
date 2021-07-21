package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.AddVmTemplateFromSnapshotParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.template.UnitToAddVmTemplateParametersBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.template.VmBaseToVmBaseForTemplateCompositeBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.FullUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UnitToGraphicsDeviceParamsBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.VmSpecificUnitToVmBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.gwt.i18n.client.DateTimeFormat;

public class VmSnapshotListModel extends SearchableListModel<VM, Snapshot> {
    // This constant is intended to be exported to a generic UTILS class later on
    private static final String DATE_FORMAT = "yyyy-MM-dd, HH:mm"; //$NON-NLS-1$

    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();
    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private UICommand newCommand;

    public UICommand getNewCommand() {
        return newCommand;
    }

    private void setNewCommand(UICommand value) {
        newCommand = value;
    }

    private UICommand previewCommand;

    public UICommand getPreviewCommand() {
        return previewCommand;
    }

    private void setPreviewCommand(UICommand value) {
        previewCommand = value;
    }

    private UICommand customPreviewCommand;

    public UICommand getCustomPreviewCommand() {
        return customPreviewCommand;
    }

    private void setCustomPreviewCommand(UICommand value) {
        customPreviewCommand = value;
    }

    private UICommand commitCommand;

    public UICommand getCommitCommand() {
        return commitCommand;
    }

    private void setCommitCommand(UICommand value) {
        commitCommand = value;
    }

    private UICommand undoCommand;

    public UICommand getUndoCommand() {
        return undoCommand;
    }

    private void setUndoCommand(UICommand value) {
        undoCommand = value;
    }

    private UICommand removeCommand;

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    private UICommand cloneVmCommand;

    public UICommand getCloneVmCommand() {
        return cloneVmCommand;
    }

    private void setCloneVmCommand(UICommand value) {
        cloneVmCommand = value;
    }

    private UICommand cloneTemplateCommand;

    public UICommand getCloneTemplateCommand() {
        return cloneTemplateCommand;
    }

    public void setCloneTemplateCommand(UICommand cloneTemplateCommand) {
        this.cloneTemplateCommand = cloneTemplateCommand;
    }

    private EntityModel<Boolean> canSelectSnapshot;

    public EntityModel<Boolean> getCanSelectSnapshot() {
        return canSelectSnapshot;
    }

    private void setCanSelectSnapshot(EntityModel<Boolean> value) {
        canSelectSnapshot = value;
    }

    private Map<Guid, SnapshotModel> snapshotsMap;

    public Map<Guid, SnapshotModel> getSnapshotsMap() {
        return snapshotsMap;
    }

    public void setSnapshotsMap(Map<Guid, SnapshotModel> value) {
        snapshotsMap = value;
        onPropertyChanged(new PropertyChangedEventArgs("SnapshotsMap")); //$NON-NLS-1$
    }

    private boolean memorySnapshotSupported;

    public boolean isMemorySnapshotSupported() {
        return memorySnapshotSupported;
    }

    private void setMemorySnapshotSupported(boolean value) {
        if (memorySnapshotSupported != value) {
            memorySnapshotSupported = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsMemorySnapshotSupported")); //$NON-NLS-1$
        }
    }

    private List<DiskImage> vmDisks;

    public List<DiskImage> getVmDisks() {
        return vmDisks;
    }

    public void setVmDisks(List<DiskImage> value) {
        vmDisks = value;
    }

    public VmSnapshotListModel() {
        setTitle(constants.snapshotsTitle());
        setHelpTag(HelpTag.snapshots);
        setHashName("snapshots"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setPreviewCommand(new UICommand("Preview", this)); //$NON-NLS-1$
        setCustomPreviewCommand(new UICommand("CustomPreview", this)); //$NON-NLS-1$
        setCommitCommand(new UICommand("Commit", this)); //$NON-NLS-1$
        setUndoCommand(new UICommand("Undo", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setCloneVmCommand(new UICommand("CloneVM", this)); //$NON-NLS-1$
        setCloneTemplateCommand(new UICommand("CloneTemplate", this)); //$NON-NLS-1$

        setCanSelectSnapshot(new EntityModel<>());
        getCanSelectSnapshot().setEntity(true);

        setSnapshotsMap(new HashMap<>());
        setVmDisks(new ArrayList<>());

        setComparator(Comparator.comparing(
                (Snapshot s) -> s.getType() == SnapshotType.ACTIVE || s.getType() == SnapshotType.PREVIEW)
                .thenComparing(Snapshot::getCreationDate)
                .reversed());
    }

    @Override
    public void setItems(Collection<Snapshot> value) {
        List<Snapshot> snapshots = value != null ? new ArrayList<>(value) : new ArrayList<>();
        snapshots.forEach(snapshot -> {
            SnapshotModel snapshotModel = snapshotsMap.computeIfAbsent(snapshot.getId(), id -> new SnapshotModel());
            snapshotModel.setEntity(snapshot);
        });

        // Filter active snapshot when in preview mode
        boolean hasNoPreviewSnapshot = snapshots.stream().noneMatch(s -> s.getType() == SnapshotType.PREVIEW);
        snapshots = snapshots.stream().filter(
                snapshot -> snapshot.getType() != SnapshotType.ACTIVE || hasNoPreviewSnapshot
        ).sorted(comparator).collect(Collectors.toList());

        if (snapshots.stream().anyMatch(s -> s.getStatus() == SnapshotStatus.IN_PREVIEW)) {
            updatePreviewedDiskSnapshots(snapshots);
        } else {
            updateItems(snapshots);
        }
    }

    private void updateItems(List<Snapshot> snapshots) {
        super.setItems(snapshots);

        // Try to select the last created snapshot (fallback to active snapshot)
        if (getSelectedItem() == null && !snapshots.isEmpty()) {
            setSelectedItem(snapshots.size() > 1 ? snapshots.get(1) : snapshots.get(0));
        }

        updateActionAvailability();
    }

    @Override
    public void setEntity(VM value) {
        updateIsMemorySnapshotSupported(value);
        super.setEntity(value);
        updateVmActiveDisks();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch() {
        VM vm = getEntity();
        if (vm == null) {
            return;
        }

        super.syncSearch(QueryType.GetAllVmSnapshotsByVmId, new IdQueryParameters(vm.getId()));
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void remove() {
        if (getEntity() != null) {
            if (getWindow() != null) {
                return;
            }

            Snapshot snapshot = getSelectedItem();
            ConfirmationModel model = new ConfirmationModel();
            setWindow(model);
            model.setTitle(constants.deleteSnapshotTitle());
            model.setHelpTag(HelpTag.delete_snapshot);
            model.setHashName("delete_snapshot"); //$NON-NLS-1$
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(messages.areYouSureYouWantToDeleteSanpshot(DateTimeFormat.getFormat(DATE_FORMAT)
                    .format(snapshot.getCreationDate()), snapshot.getDescription()));
            String backupSupportedRawDisks = getRawFormatDisksWithBackupEnabled();
            if (!backupSupportedRawDisks.isEmpty()) {
                stringBuilder.append(messages.incrementalBackupEnableWillRemovedForDisks(backupSupportedRawDisks));
            }
            model.setMessage(stringBuilder.toString());

            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
            model.getCommands().add(tempVar);
            UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
            model.getCommands().add(tempVar2);
        }
    }

    private String getRawFormatDisksWithBackupEnabled() {
        Snapshot snapshot = getSelectedItem();

        List<Snapshot> snapshots = (List<Snapshot>) getItems();
        int snapshotIndex = snapshots.indexOf(snapshot);
        Snapshot parentSnapshot = snapshots.get(snapshotIndex - 1);

        // Get all disks ids that support incremental backup on the parent snapshot
        List<Guid> backupEnabledDisksIds = snapshotsMap.get(parentSnapshot.getId()).getDisks()
                .stream()
                .filter(diskImage -> diskImage.getBackup() == DiskBackup.Incremental)
                .map(DiskImage::getId)
                .collect(Collectors.toList());

        if (backupEnabledDisksIds.isEmpty()) {
            return constants.emptyString();
        }

        // Get all disks names that support incremental backup and
        // going to have RAW after the snapshot deletion
        return snapshotsMap.get(snapshot.getId()).getDisks()
                .stream()
                .filter(diskImage -> backupEnabledDisksIds.contains(diskImage.getId()))
                .filter(diskImage -> diskImage.getVolumeFormat() == VolumeFormat.RAW)
                .map(DiskImage::getName)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }

    private void onRemove() {
        Snapshot snapshot = getSelectedItem();
        if (snapshot == null) {
            cancel();
            return;
        }

        VM vm = getEntity();
        if (vm != null) {
            Frontend.getInstance().runAction(ActionType.RemoveSnapshot,
                    new RemoveSnapshotParameters(snapshot.getId(), vm.getId()), null, null);
        }

        getCanSelectSnapshot().setEntity(false);

        cancel();
    }

    private void undo() {
        VM vm = getEntity();
        if (vm != null) {
            Frontend.getInstance().runAction(ActionType.RestoreAllSnapshots,
                    new RestoreAllSnapshotsParameters(vm.getId(), SnapshotActionEnum.UNDO),
                    null,
                    null);
        }
    }

    private void onCommit() {
        VM vm = getEntity();
        if (vm != null) {
            Frontend.getInstance().runAction(ActionType.RestoreAllSnapshots,
                    new RestoreAllSnapshotsParameters(vm.getId(), SnapshotActionEnum.COMMIT),
                    null,
                    null);
        }
        cancel();
    }

    private void preview() {
        VM vm = getEntity();
        if (vm == null) {
            return;
        }

        final Snapshot snapshot = getSelectedItem();
        AsyncDataProvider.getInstance().getVmConfigurationBySnapshot(new AsyncQuery<>(v -> {
            ArrayList<DiskImage> snapshotDisks = v.getDiskList();
            List<DiskImage> disksExcludedFromSnapshot = imagesSubtract(getVmDisks(), snapshotDisks);

            boolean showMemorySnapshotWarning = isMemorySnapshotSupported() && snapshot.containsMemory();
            boolean showPartialSnapshotWarning = !disksExcludedFromSnapshot.isEmpty();

            if (showMemorySnapshotWarning || showPartialSnapshotWarning) {
                SnapshotModel model = new SnapshotModel();
                model.setVmDisks(getVmDisks());
                model.setDisks(snapshotDisks);
                model.setShowMemorySnapshotWarning(showMemorySnapshotWarning);
                model.setShowPartialSnapshotWarning(showPartialSnapshotWarning);
                if (showMemorySnapshotWarning) {
                    model.setOldClusterVersionOfSnapshotWithMemory(v);
                }
                setWindow(model);

                model.setTitle(showPartialSnapshotWarning ?
                        constants.previewPartialSnapshotTitle() :
                        constants.previewSnapshotTitle());
                model.setHelpTag(showPartialSnapshotWarning ? HelpTag.preview_partial_snapshot : HelpTag.preview_snapshot);
                model.setHashName(showPartialSnapshotWarning ? "preview_partial_snapshot" : "preview_snapshot"); //$NON-NLS-1$ //$NON-NLS-2$

                addCommands(model, "OnPreview"); //$NON-NLS-1$
            } else {
                runTryBackToAllSnapshotsOfVm(null, v, snapshot, false, null, true, null);
            }
        }), snapshot.getId());
    }

    private void updateVmActiveDisks() {
        VM vm = getEntity();
        if (vm == null) {
            return;
        }

        AsyncDataProvider.getInstance().getVmDiskList(new AsyncQuery<>(disks -> {
            setVmDisks(disks
                    .stream()
                    .filter(d -> d.getDiskStorageType() != DiskStorageType.LUN)
                    .map(d -> (DiskImage) d)
                    .collect(Collectors.toList()));
        }), vm.getId());
    }

    private void updatePreviewedDiskSnapshots(final List<Snapshot> snapshots) {
        getVmDisks().stream().filter(d -> d.getSnapshots().size() > 1)
                .forEach(d -> getSnapshotsMap().get(d.getSnapshots().get(1).getVmSnapshotId())
                        .getEntity().getDiskImages().add(d));
        updateItems(snapshots);
    }

    private void customPreview() {
        VM vm = getEntity();
        if (vm == null) {
            return;
        }
        PreviewSnapshotModel model = new PreviewSnapshotModel(vm, getSelectedItem().getId());
        model.setVmId(vm.getId());
        model.initialize();

        // Update according to the selected snapshot
        Snapshot selectedSnapshot = getSelectedItem();
        if (selectedSnapshot != null) {
            model.setSnapshotModel(getSnapshotsMap().get(selectedSnapshot.getId()));
        }

        setWindow(model);

        model.setTitle(constants.customPreviewSnapshotTitle());
        model.setHelpTag(HelpTag.custom_preview_snapshot);
        model.setHashName("custom_preview_snapshot"); //$NON-NLS-1$

        addCommands(model, "OnCustomPreview"); //$NON-NLS-1$
    }

    private void onPreview() {
        Snapshot snapshot = getSelectedItem();
        if (snapshot == null) {
            cancel();
            return;
        }

        VM vm = getEntity();
        SnapshotModel snapshotModel = (SnapshotModel) getWindow();
        boolean memory = false;
        List<DiskImage> disks = null;

        if (snapshotModel.isShowPartialSnapshotWarning()) {
            switch (snapshotModel.getPartialPreviewSnapshotOptions().getSelectedItem()) {
                case preserveActiveDisks:
                    // get snapshot disks
                    disks = snapshotModel.getDisks();
                    // add active disks missed from snapshot
                    disks.addAll(imagesSubtract(getVmDisks(), disks));
                    break;
                case excludeActiveDisks:
                    // nothing to do - default behaviour
                    break;
                case openCustomPreviewDialog:
                    setWindow(null);
                    getCustomPreviewCommand().execute();
                    return;
            }
        }

        if (snapshotModel.isShowMemorySnapshotWarning()) {
            memory = snapshotModel.getMemory().getEntity();
        }

        runTryBackToAllSnapshotsOfVm(snapshotModel, vm, snapshot, memory, disks, true, null);
    }

    private static List<DiskImage> imagesSubtract(Collection<DiskImage> images, Collection<DiskImage> imagesToSubtract) {
        Set<Guid> idsToSubtract = imagesToSubtract.stream().map(DiskImage::getId).collect(Collectors.toSet());
        return images.stream().filter(new Linq.IdsPredicate<>(idsToSubtract).negate()).collect(Collectors.toList());
    }

    private void onCustomPreview() {
        VM vm = getEntity();
        PreviewSnapshotModel previewSnapshotModel = (PreviewSnapshotModel) getWindow();
        Snapshot snapshot = previewSnapshotModel.getSnapshotModel().getEntity();
        boolean memory = Boolean.TRUE.equals(previewSnapshotModel.getSnapshotModel().getMemory().getEntity());
        List<DiskImage> disks = previewSnapshotModel.getSelectedDisks();
        boolean isSnapshotsContainsLeases = previewSnapshotModel.isSnapshotsContainsLeases();
        Guid selectedSnapshotLeaseDomainId = previewSnapshotModel.getSelectedLease();

        runTryBackToAllSnapshotsOfVm(previewSnapshotModel,
                vm,
                snapshot,
                memory,
                disks,
                !(isSnapshotsContainsLeases && selectedSnapshotLeaseDomainId == null),
                selectedSnapshotLeaseDomainId);
    }

    private void commit() {
        if (getEntity() != null) {
            if (getWindow() != null) {
                return;
            }

            Optional<Snapshot> inPreviewSnapshot =
                    getItems().stream().filter(s -> s.getStatus() == SnapshotStatus.IN_PREVIEW).findFirst();

            if (!inPreviewSnapshot.isPresent()) {
                // 'Commit' is only allowed when there is a snapshot in 'IN_PREVIEW' state,
                // therefore a previewed snapshot should be present.
                return;
            }

            Snapshot snapshot = inPreviewSnapshot.get();
            ConfirmationModel model = new ConfirmationModel();
            setWindow(model);
            model.setTitle(constants.commitSnapshotTitle());
            model.setHelpTag(HelpTag.commit_snapshot);
            model.setHashName("commit_snapshot"); //$NON-NLS-1$
            model.setMessage(ConstantsManager.getInstance()
                    .getMessages()
                    .areYouSureYouWantToCommitSnapshot( DateTimeFormat.getFormat(DATE_FORMAT).format(snapshot.getCreationDate()),
                            snapshot.getDescription()));

            model.getCommands().add(UICommand.createDefaultOkUiCommand("OnCommit", this)); //$NON-NLS-1$
            model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
        }
    }

    private void runTryBackToAllSnapshotsOfVm(final Model model,
            VM vm,
            Snapshot snapshot,
            boolean memory,
            List<DiskImage> disks,
            boolean isRestoreLease,
            Guid leaseDomainId) {
        if (model != null) {
            model.startProgress();
        }
        TryBackToAllSnapshotsOfVmParameters params = new TryBackToAllSnapshotsOfVmParameters(
                vm.getId(), snapshot.getId(), memory, disks);

        if (leaseDomainId != null) {
            params.setDstLeaseDomainId(leaseDomainId);
        }
        params.setRestoreLease(isRestoreLease);
        Frontend.getInstance().runAction(ActionType.TryBackToAllSnapshotsOfVm, params,
                result -> {
                    if (model != null) {
                        model.stopProgress();
                    }

                    if (result.getReturnValue().getSucceeded()) {
                        cancel();
                    }
                });
    }

    private void newEntity() {
        VM vm = getEntity();
        if (vm == null || getWindow() != null) {
            return;
        }

        SnapshotModel model = SnapshotModel.createNewSnapshotModel(this);
        setWindow(model);
        model.setVm(vm);
        model.initialize();
    }

    private void addCommands(Model model, String okCommandName) {
        model.getCommands().add(UICommand.createDefaultOkUiCommand(okCommandName, this)); //$NON-NLS-1$
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void cancel() {
        setWindow(null);
    }

    private void cloneTemplate() {
        Snapshot snapshot = getSelectedItem();
        if (snapshot == null) {
            return;
        }

        if (getWindow() != null) {
            return;
        }

        final UnitVmModel model = new UnitVmModel(createNewTemplateBehavior(snapshot.getId()), this);
        setWindow(model);
        model.startProgress();

        AsyncDataProvider.getInstance().getVmConfigurationBySnapshot(new AsyncQuery<>(vm -> {
            NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) model.getBehavior();
            behavior.setVm(vm);

            model.setTitle(constants.newTemplateTitle());
            model.setHelpTag(HelpTag.clone_template_from_snapshot);
            model.setHashName("clone_template_from_snapshot"); //$NON-NLS-1$
            model.setIsNew(true);
            model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());
            model.initialize();
            model.getVmType().setSelectedItem(vm.getVmType());
            model.getIsHighlyAvailable().setEntity(vm.getStaticData().isAutoStartup());
            model.getCommands().add(
                    new UICommand("OnNewTemplate", VmSnapshotListModel.this) //$NON-NLS-1$
                            .setTitle(constants.ok())
                            .setIsDefault(true));

            model.getCommands().add(UICommand.createCancelUiCommand("Cancel", VmSnapshotListModel.this)); //$NON-NLS-1$
            model.stopProgress();
        }), snapshot.getId());
    }

    protected NewTemplateVmModelBehavior createNewTemplateBehavior(Guid snapshotId) {
        return new NewTemplateVmModelBehavior(snapshotId);
    }

    private void onCloneTemplate() {
        final UnitVmModel model = (UnitVmModel) getWindow();
        NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) model.getBehavior();
        Snapshot snapshot = getSelectedItem();
        if (snapshot == null) {
            cancel();
            return;
        }

        final VM vm = behavior.getVm();

        if (!model.validate(false)) {
            model.setIsValid(false);
        } else  if (model.getIsSubTemplate().getEntity()) {
            postNameUniqueCheck(vm);
        } else {
            String name = model.getName().getEntity();

            // Check name uniqueness.
            AsyncDataProvider.getInstance().isTemplateNameUnique(new AsyncQuery<>(
                            isNameUnique -> {
                                if (!isNameUnique) {
                                    model.getInvalidityReasons().clear();
                                    model.getName()
                                            .getInvalidityReasons()
                                            .add(ConstantsManager.getInstance()
                                                    .getConstants()
                                                    .nameMustBeUniqueInvalidReason());
                                    model.getName().setIsValid(false);
                                    model.setIsValid(false);
                                    model.fireValidationCompleteEvent();
                                } else {
                                    postNameUniqueCheck(vm);
                                }

                            }),
                    name, model.getSelectedDataCenter().getId());
        }
    }

    private void postNameUniqueCheck(VM vm) {
        UnitVmModel model = (UnitVmModel) getWindow();

        VM newVm = buildVmOnNewTemplate(model, vm);

        AddVmTemplateFromSnapshotParameters parameters =
                new AddVmTemplateFromSnapshotParameters(newVm.getStaticData(),
                        model.getName().getEntity(),
                        model.getDescription().getEntity(),
                        getSelectedItem().getId());
        BuilderExecutor.build(model, parameters, new UnitToAddVmTemplateParametersBuilder());
        model.startProgress();
        Frontend.getInstance().runAction(ActionType.AddVmTemplateFromSnapshot,
                parameters,
                result -> {

                    VmSnapshotListModel vmSnapshotListModel = (VmSnapshotListModel) result.getState();
                    vmSnapshotListModel.getWindow().stopProgress();
                    ActionReturnValue returnValueBase = result.getReturnValue();
                    if (returnValueBase != null && returnValueBase.getSucceeded()) {
                        vmSnapshotListModel.cancel();
                    }

                }, this);
    }

    protected static VM buildVmOnNewTemplate(UnitVmModel model, VM vm) {
        VM resultVm = new VM();
        resultVm.setId(vm.getId());
        BuilderExecutor.build(model, resultVm.getStaticData(), new CommonUnitToVmBaseBuilder());
        BuilderExecutor.build(vm.getStaticData(), resultVm.getStaticData(), new VmBaseToVmBaseForTemplateCompositeBaseBuilder());
        return resultVm;
    }

    private void cloneVM() {
        Snapshot snapshot = getSelectedItem();
        if (snapshot == null) {
            return;
        }

        if (getWindow() != null) {
            return;
        }

        VM selectedVm = getEntity();

        UnitVmModel model = new UnitVmModel(new CloneVmFromSnapshotModelBehavior(), this);
        model.getVmType().setSelectedItem(selectedVm.getVmType());
        model.setIsAdvancedModeLocalStorageKey("wa_snapshot_dialog");  //$NON-NLS-1$
        setWindow(model);

        model.startProgress();

        AsyncDataProvider.getInstance().getVmConfigurationBySnapshot(new AsyncQuery<>(vm -> {
            UnitVmModel unitVmModel = (UnitVmModel) getWindow();

            CloneVmFromSnapshotModelBehavior behavior = (CloneVmFromSnapshotModelBehavior) unitVmModel.getBehavior();
            behavior.setVm(vm);

            unitVmModel.setTitle(constants.cloneVmFromSnapshotTitle());
            unitVmModel.setHelpTag(HelpTag.clone_vm_from_snapshot);
            unitVmModel.setHashName("clone_vm_from_snapshot"); //$NON-NLS-1$
            unitVmModel.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());
            unitVmModel.initialize();

            VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
            switchModeCommand.init(unitVmModel);
            unitVmModel.getCommands().add(switchModeCommand);

            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnCloneVM", VmSnapshotListModel.this); //$NON-NLS-1$
            unitVmModel.getCommands().add(tempVar);
            UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", VmSnapshotListModel.this); //$NON-NLS-1$
            unitVmModel.getCommands().add(tempVar2);

            stopProgress();
        }), snapshot.getId());
    }

    private void onCloneVM() {
        UnitVmModel model = (UnitVmModel) getWindow();
        CloneVmFromSnapshotModelBehavior behavior = (CloneVmFromSnapshotModelBehavior) model.getBehavior();
        Snapshot snapshot = getSelectedItem();
        if (snapshot == null) {
            cancel();
            return;
        }

        if (!model.validate()) {
            return;
        }

        VM vm = behavior.getVm();

        String name = model.getName().getEntity();

        // Check name uniqueness.
        AsyncDataProvider.getInstance().isVmNameUnique(new AsyncQuery<>(
                        isNameUnique -> {
                            if (!isNameUnique) {
                                model.getInvalidityReasons().clear();
                                model.getName()
                                        .getInvalidityReasons()
                                        .add(ConstantsManager.getInstance()
                                                .getConstants()
                                                .nameMustBeUniqueInvalidReason());
                                model.getName().setIsValid(false);
                                model.setIsValid(false);
                                model.fireValidationCompleteEvent();
                            } else {
                                postNameUniqueCheckVM(vm, snapshot, behavior, model);
                            }
                        }),
                name, model.getSelectedDataCenter().getId());
    }

    private void postNameUniqueCheckVM(VM vm, Snapshot snapshot,
                                       CloneVmFromSnapshotModelBehavior behavior, UnitVmModel model) {
        // Save changes.
        buildVmOnClone(model, vm);

        vm.setUseHostCpuFlags(model.getHostCpu().getEntity());
        vm.setDiskMap(behavior.getVm().getDiskMap());

        Map<Guid, DiskImage> imageToDestinationDomainMap =
                model.getDisksAllocationModel().getImageToDestinationDomainMap();

        AddVmFromSnapshotParameters parameters =
                new AddVmFromSnapshotParameters(vm.getStaticData(), snapshot.getId());
        parameters.setDiskInfoDestinationMap(imageToDestinationDomainMap);
        parameters.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        parameters.setVirtioScsiEnabled(model.getIsVirtioScsiEnabled().getEntity());

        BuilderExecutor.build(model, parameters, new UnitToGraphicsDeviceParamsBuilder());

        if (model.getIsHeadlessModeEnabled().getEntity()) {
            parameters.getVmStaticData().setDefaultDisplayType(DisplayType.none);
        }
        if (!StringHelper.isNullOrEmpty(model.getVmId().getEntity())) {
            parameters.setVmId(new Guid(model.getVmId().getEntity()));
        }

        model.startProgress();

        Frontend.getInstance().runAction(ActionType.AddVmFromSnapshot, parameters,
                result -> {

                    VmSnapshotListModel vmSnapshotListModel = (VmSnapshotListModel) result.getState();
                    vmSnapshotListModel.getWindow().stopProgress();
                    ActionReturnValue returnValueBase = result.getReturnValue();
                    if (returnValueBase != null && returnValueBase.getSucceeded()) {
                        vmSnapshotListModel.cancel();
                        vmSnapshotListModel.updateActionAvailability();
                    }
                }, this);
    }

    protected static void buildVmOnClone(UnitVmModel model, VM vm) {
        BuilderExecutor.build(model, vm.getStaticData(), new FullUnitToVmBaseBuilder());
        BuilderExecutor.build(model, vm, new VmSpecificUnitToVmBuilder());
    }

    public void updateActionAvailability() {
        if (getItems() == null) {
            // no need to update action availability
            return;
        }

        VM vm = getEntity();
        Snapshot snapshot = getSelectedItem();
        List<VM> vmList = vm != null ? Collections.singletonList(vm) : Collections.emptyList();

        boolean isVmDown = vm != null && vm.getStatus() == VMStatus.Down;
        boolean isVmImageLocked = vm != null && vm.getStatus() == VMStatus.ImageLocked;
        boolean isVmQualifiedForSnapshotMerge = vm != null && vm.getStatus().isQualifiedForSnapshotMerge();
        boolean isPreviewing = getItems().stream().anyMatch(s -> s.getStatus() == SnapshotStatus.IN_PREVIEW);
        boolean isLocked = getItems().stream().anyMatch(s -> s.getStatus() == SnapshotStatus.LOCKED);
        boolean isSelected = snapshot != null && snapshot.getType() == SnapshotType.REGULAR;
        boolean isStateless = getItems().stream().anyMatch(s -> s.getType() == SnapshotType.STATELESS);
        boolean isVmConfigurationBroken = snapshot != null && snapshot.isVmConfigurationBroken();
        boolean isManaged = vm != null && vm.isManaged();

        getCanSelectSnapshot().setEntity(!isPreviewing && !isLocked && !isStateless
                && ActionUtils.canExecute(vmList, VM.class, ActionType.CreateSnapshotForVm));
        getNewCommand().setIsExecutionAllowed(!isPreviewing && !isLocked && !isVmImageLocked && !isStateless && isManaged);
        getPreviewCommand().setIsExecutionAllowed(isSelected && !isLocked && !isPreviewing && isVmDown && !isStateless);
        getCustomPreviewCommand().setIsExecutionAllowed(getPreviewCommand().getIsExecutionAllowed());
        getCommitCommand().setIsExecutionAllowed(isPreviewing && isVmDown && !isStateless);
        getUndoCommand().setIsExecutionAllowed(isPreviewing && isVmDown && !isStateless);
        getRemoveCommand().setIsExecutionAllowed(isSelected && !isLocked && !isPreviewing && !isStateless
                && isVmQualifiedForSnapshotMerge);
        getCloneVmCommand().setIsExecutionAllowed(isSelected && !isLocked && !isPreviewing
                && !isVmImageLocked && !isStateless && !isVmConfigurationBroken);
        getCloneTemplateCommand().setIsExecutionAllowed(isSelected && !isLocked && !isPreviewing
                && !isVmImageLocked && !isStateless && !isVmConfigurationBroken);
    }

    private void updateIsMemorySnapshotSupported(Object entity) {
        if (entity == null) {
            return;
        }

        VM vm = (VM) entity;

        setMemorySnapshotSupported(AsyncDataProvider.getInstance().isMemorySnapshotSupported(vm));
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newEntity();
        } else if (command == getPreviewCommand()) {
            preview();
        } else if (command == getCustomPreviewCommand()) {
            customPreview();
        } else if (command == getCommitCommand()) {
            commit();
        } else if (command == getUndoCommand()) {
            undo();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if (command == getCloneVmCommand()) {
            cloneVM();
        } else if (command == getCloneTemplateCommand()) {
            cloneTemplate();
        } else if ("OnNewTemplate".equals(command.getName())) { //$NON-NLS-1$
            onCloneTemplate();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnCloneVM".equals(command.getName())) { //$NON-NLS-1$
            onCloneVM();
        } else if ("OnPreview".equals(command.getName())) { //$NON-NLS-1$
            onPreview();
        } else if ("OnCustomPreview".equals(command.getName())) { //$NON-NLS-1$
            onCustomPreview();
        } else if ("OnCommit".equals(command.getName())) { //$NON-NLS-1$
            onCommit();
        }
    }

    @Override
    protected String getListName() {
        return "VmSnapshotListModel"; //$NON-NLS-1$
    }

    @Override
    protected boolean isSingleSelectionOnly() {
        // Single selection model
        return true;
    }
}

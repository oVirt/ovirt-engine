package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.PairFirstComparator;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.IconUtils;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsolesFactory;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.ICancelable;

public abstract class AbstractUserPortalListModel extends ListWithDetailsModel<Void, /* VmOrPool */ Object, UserPortalItemModel> implements ICancelable {
    private UICommand editConsoleCommand;

    protected ConsolesFactory consolesFactory;

    public AbstractUserPortalListModel() {
        setEditConsoleCommand(new UICommand("NewServer", this)); //$NON-NLS-1$
    }

    protected Iterable filterVms(List all) {
        List<VM> result = new LinkedList<>();
        for (Object o : all) {
            if (o instanceof VM) {
                result.add((VM) o);
            }
        }
        return result;
    }

    public List<VmConsoles> getAutoConnectableConsoles() {
        List<VmConsoles> autoConnectableConsoles = new LinkedList<>();

        if (items != null) {
            for (UserPortalItemModel upItem : items) {

                if (!upItem.isPool() && upItem.getVmConsoles().canConnectToConsole()) {
                    autoConnectableConsoles.add(upItem.getVmConsoles());
                }
            }
        }

        return autoConnectableConsoles;
    }

    public boolean getCanConnectAutomatically() {
        return getAutoConnectableConsoles().size() == 1;
    }

    public UICommand getEditConsoleCommand() {
        return editConsoleCommand;
    }

    private void setEditConsoleCommand(UICommand editConsoleCommand) {
        this.editConsoleCommand = editConsoleCommand;
    }

    @Override
    protected Object provideDetailModelEntity(UserPortalItemModel selectedItem) {
        // Each item in this list model is not a business entity,
        // therefore select an Entity property to provide it to
        // the detail models.
        if (selectedItem == null) {
            return null;
        }

        return selectedItem.getEntity();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getEditConsoleCommand()) {
            editConsole();
        } else if ("OnEditConsoleSave".equals(command.getName())) { //$NON-NLS-1$
            onEditConsoleSave();
        } else if (Model.CANCEL_COMMAND.equals(command.getName())) {
            cancel();
        }
    }

    private void onEditConsoleSave() {
        cancel();
    }

    private void editConsole() {
        if (getWindow() != null || getSelectedItem().getVmConsoles() == null) {
            return;
        }

        ConsolePopupModel model = new ConsolePopupModel();
        model.setVmConsoles(getSelectedItem().getVmConsoles());
        model.setHelpTag(HelpTag.editConsole);
        model.setHashName("editConsole"); //$NON-NLS-1$
        setWindow(model);

        UICommand saveCommand = UICommand.createDefaultOkUiCommand("OnEditConsoleSave", this); //$NON-NLS-1$
        model.getCommands().add(saveCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    protected abstract ConsoleContext getConsoleContext();

    protected abstract boolean fetchLargeIcons();

    protected abstract Event<EventArgs> getSearchCompletedEvent();

    public void onVmAndPoolLoad(final List<VM> vms, List<VmPool> pools) {
        if (vms == null || pools == null) {
            return;
        }

        // Remove pools that has provided VMs.
        final ArrayList<VmPool> filteredPools = new ArrayList<>();
        for (VmPool pool : pools) {
            // Add pool to map.

            int attachedVmsCount = 0;
            for (VM vm : vms) {
                if (vm.getVmPoolId() != null && vm.getVmPoolId().equals(pool.getVmPoolId())) {
                    attachedVmsCount++;
                }
            }

            if (attachedVmsCount < pool.getMaxAssignedVmsPerUser()) {
                filteredPools.add(pool);
            }
        }

        final List<Pair<Object, VM>> vmPairs =
                vms.stream().map(v -> new Pair<Object, VM>(v, null)).collect(Collectors.toList());

        if (filteredPools.isEmpty()) {
            IconUtils.prefetchIcons(vms, true, fetchLargeIcons(), idToIconMap -> finishSearch(vmPairs));
        } else { // if we have pools we have to update their console cache and THEN finish search
            List<VdcQueryType> poolQueryList = new ArrayList<>();
            List<VdcQueryParametersBase> poolParamList = new ArrayList<>();

            for (VmPool p : filteredPools) {
                poolQueryList.add(VdcQueryType.GetVmDataByPoolId);
                poolParamList.add(new IdQueryParameters(p.getVmPoolId()));
            }

            Frontend.getInstance().runMultipleQueries(
                    poolQueryList, poolParamList,
                    result -> {
                        final List<Pair<Object, VM>> all =
                                Stream.concat(vmPairs.stream(),
                                        IntStream.range(0, filteredPools.size())
                                                .filter(i -> result.getReturnValues().get(i).getReturnValue() != null)
                                                .mapToObj(i -> new Pair<Object, VM>(
                                                        filteredPools.get(i),
                                                        result.getReturnValues().get(i).getReturnValue())
                                                )
                                ).collect(Collectors.toList());

                        final List<VM> vmsAndPoolRepresentants = extractVms(all);
                        IconUtils.prefetchIcons(vmsAndPoolRepresentants, true, fetchLargeIcons(),
                                idToIconMap -> finishSearch(all));
                    });
        }
    }

    /**
     * @param vmsAndPools List of pairs of these types: (VM, null) or (VmPool, VM representative)
     * @return all VMs from the input
     */
    private List<VM> extractVms(List<Pair<Object, VM>> vmsAndPools) {
        final ArrayList<VM> result = new ArrayList<>();
        for (Pair<Object, VM> vmOrPool : vmsAndPools) {
            if (vmOrPool.getFirst() instanceof VM) {
                result.add((VM) vmOrPool.getFirst());
                continue;
            }
            if (vmOrPool.getSecond() != null) {
                result.add(vmOrPool.getSecond());
            }
        }
        return result;
    }

    /**
     *
     * @param poolsWithRepresentatives list of pairs of (VmPool; VM representative if exists, otherwise null)
     * @return only pools with existing representatives
     */
    List<Pair<Object, VM>> filterNonNullPools(List<Pair<Object, VM>> poolsWithRepresentatives) {
        return poolsWithRepresentatives.stream()
                .filter(p -> p.getSecond() != null)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    private void finishSearch(List<Pair<Object, VM>> vmOrPoolAndPoolRepresentants) {
        Collections.sort((List) vmOrPoolAndPoolRepresentants, new PairFirstComparator<>(new NameableComparator()));

        ArrayList<UserPortalItemModel> items = new ArrayList<>();
        for (Pair<Object, VM> item : vmOrPoolAndPoolRepresentants) {
            UserPortalItemModel model = new UserPortalItemModel(item.getFirst(), item.getSecond(), consolesFactory);
            model.setEntity(item.getFirst());
            items.add(model);
        }

        setItems(items);

        getSearchCompletedEvent().raise(this, EventArgs.EMPTY);
    }

    public void cancel() {
        setWindow(null);
        setConfirmWindow(null);
    }

}

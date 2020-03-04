package org.ovirt.engine.ui.uicommonweb.models.storage;

import static org.ovirt.engine.ui.uicommonweb.Linq.where;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmFromConfParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.ErrorTranslator;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.RegisterVmData;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class RegisterVmModel extends RegisterEntityModel<VM, RegisterVmData> {

    private static final String NEWLINE = "\n"; //$NON-NLS-1$
    private static final Predicate<RegisterVmData> REASSIGN_MACS_PREDICATE = rvd -> rvd.getReassignMacs().getEntity();
    private static final Predicate<RegisterVmData> NOT_REASSIGN_MACS_PREDICATE = REASSIGN_MACS_PREDICATE.negate();

    public RegisterVmModel() {
    }

    @Override
    public void initialize() {
        addVnicProfileMappingCommand();
        getCluster().getItemsChangedEvent().addListener((ev, sender, args) -> postClustersInit());
        super.initialize();
    }

    private void postClustersInit() {

        validateAllMacs();

        for (final RegisterVmData registerVmData : getEntities().getItems()) {
            final IEventListener<EventArgs> validateMacsListener =
                    (ev, sender, args) -> validateVmMacs(Collections.singletonList(registerVmData));
            registerVmData.getReassignMacs().getEntityChangedEvent().addListener(validateMacsListener);
            registerVmData.getCluster().getSelectedItemChangedEvent().addListener(validateMacsListener);
        }
    }

    private void validateAllMacs() {
        validateVmMacs(getEntities().getItems());
    }

    private void validateVmMacs(final Collection<RegisterVmData> registerVmData) {
        final Collection<RegisterVmData> vmsToBeValidated = getVmsForMacValidation(registerVmData);
        clearVmMacsWarningOnReassignedVms(registerVmData);
        if (vmsToBeValidated.isEmpty()) {
            return;
        }

        final Map<Guid, List<VM>> vmsByClusterId = mapVmsByTargetClusterId(vmsToBeValidated);
        AsyncDataProvider.getInstance().validateVmMacs(
                new AsyncQuery<>(
                        returnValue -> processVmMacsValidationResult(vmsToBeValidated, returnValue)),
                vmsByClusterId);
    }

    private void processVmMacsValidationResult(
            Collection<RegisterVmData> vmsToBeValidated,
            Map<Guid, List<List<String>>> validationResult) {
        for (RegisterVmData vmData : vmsToBeValidated) {
            final List<List<String>> vmMessages = validationResult.get(vmData.getVm().getId());
            if (vmMessages == null) {
                continue;
            }
            final String translatedMessage = translateMessages(vmMessages);
            updateVmWithMacsValidationResult(vmData, translatedMessage);
        }
    }

    private void updateVmWithMacsValidationResult(RegisterVmData vmData, String translatedMessage) {
        if (translatedMessage.isEmpty()) {
            clearVmMacsWarning(vmData);
        } else {
            setVmMacsWarning(vmData, translatedMessage, Boolean.TRUE);
        }
    }

    private void setVmMacsWarning(RegisterVmData vmData, String message, Boolean isValid) {
        vmData.setWarning(message);
        vmData.getBadMacsExist().setEntity(isValid);
    }

    private void clearVmMacsWarning(RegisterVmData vmData) {
        setVmMacsWarning(vmData, null, Boolean.FALSE);
    }

    private void clearVmMacsWarningOnReassignedVms(Collection<RegisterVmData> registerVmData) {
        final Collection<RegisterVmData> vmsToBeReassigned = where(registerVmData, REASSIGN_MACS_PREDICATE);
        for (RegisterVmData vmData : vmsToBeReassigned) {
            clearVmMacsWarning(vmData);
        }
    }

    private Collection<RegisterVmData> getVmsForMacValidation(Collection<RegisterVmData> registerVmData) {
        return where(registerVmData, NOT_REASSIGN_MACS_PREDICATE);
    }

    private Map<Guid, List<VM>> mapVmsByTargetClusterId(Collection<RegisterVmData> vms) {
        final Map<Guid, List<VM>> result = new HashMap<>();
        for (RegisterVmData vmData : vms) {
            final Guid clusterId = vmData.getCluster().getSelectedItem().getId();
            final List<VM> clusterVms;
            if (result.containsKey(clusterId)) {
                clusterVms = result.get(clusterId);
            } else {
                clusterVms = new ArrayList<>();
                result.put(clusterId, clusterVms);
            }
            clusterVms.add(vmData.getVm());
        }
        return result;
    }

    private String translateMessages(List<List<String>> vmMessages) {
        StringBuilder stringBuilder = new StringBuilder();
        final ErrorTranslator appErrorsTranslator = Frontend.getInstance().getAppErrorsTranslator();
        for (List<String> vmMessage : vmMessages) {
            if (vmMessage.isEmpty()) {
                continue;
            }
            for (String translatedMessage : appErrorsTranslator.translateErrorText(new ArrayList<>(vmMessage))) {
                stringBuilder.append(translatedMessage);
            }
            stringBuilder.append(NEWLINE);
        }
        return stringBuilder.toString();
    }

    protected void onSave() {
        if (!validate()) {
            return;
        }

        List<ActionParametersBase> parameters = prepareParameters();
        ActionType actionType = ActionType.ImportVmFromConfiguration;
        onSave(actionType, parameters);
    }

    private List<ActionParametersBase> prepareParameters() {
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (RegisterVmData registerVmData : getEntities().getItems()) {
            VM vm = registerVmData.getEntity();
            Cluster cluster = registerVmData.getCluster().getSelectedItem();

            ImportVmFromConfParameters params = new ImportVmFromConfParameters(
                    cloneExternalVnicProfiles(cluster),
                    registerVmData.getReassignMacs().getEntity());
            params.setContainerId(vm.getId());
            params.setStorageDomainId(getStorageDomainId());
            params.setName(vm.getName());
            params.setImagesExistOnTargetStorageDomain(true);
            params.setClusterId(cluster != null ? cluster.getId() : null);
            params.setAllowPartialImport(registerVmData.getAllowPartialImport().getEntity());

            if (isQuotaEnabled()) {
                Quota quota = registerVmData.getClusterQuota().getSelectedItem();
                params.setQuotaId(quota != null ? quota.getId() : null);
                params.setDiskMap(vm.getDiskMap());
                updateDiskQuotas(new ArrayList<>(params.getDiskMap().values()));
            }

            parameters.add(params);
        }
        return parameters;
    }

    protected List<VmNetworkInterface> getInterfaces(RegisterVmData importEntityData) {
        return importEntityData.getEntity().getInterfaces();
    }

    @Override
    protected String createSearchPattern(Collection<RegisterVmData> entities) {
        String vm_guidKey = "ID = "; //$NON-NLS-1$
        String vm_nameKey = "NAME = "; //$NON-NLS-1$
        String orKey = " or "; //$NON-NLS-1$
        String prefix = "VM: "; //$NON-NLS-1$

        StringJoiner sj = new StringJoiner(orKey, prefix, "");
        entities.stream().map(ImportVmData::getVm).forEach(vm -> {
            sj.add(vm_guidKey + vm.getId().toString());
            sj.add(vm_nameKey + vm.getName());
        });

        return sj.toString();
    }

    @Override
    protected SearchType getSearchType() {
        return SearchType.VM;
    }

    @Override
    protected void updateExistingEntities(List<VM> vms, Guid storagePoolId) {
        Set<String> existingNames = vms
                .stream()
                .filter(vm -> vm.getStoragePoolId().equals(storagePoolId))
                .map(VM::getName)
                .collect(Collectors.toSet());

        for (RegisterVmData vmData : getEntities().getItems()) {
            if (vms.contains(vmData.getVm())) {
                vmData.setExistsInSystem(true);
            }
            vmData.setNameExistsInSystem(existingNames.contains(vmData.getVm().getName()));
        }
    }
}

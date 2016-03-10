package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.HostInterfaceValidator;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class NicLabelValidator {
    private HostSetupNetworksParameters params;
    private BusinessEntityMap<VdsNetworkInterface> existingInterfacesMap;
    private BusinessEntityMap<CreateOrUpdateBond> createOrUpdateBondBusinessEntityMap;
    private HostSetupNetworksValidatorHelper hostSetupNetworksValidatorHelper;

    public NicLabelValidator(HostSetupNetworksParameters params,
            BusinessEntityMap<VdsNetworkInterface> existingInterfacesMap,
            BusinessEntityMap<CreateOrUpdateBond> createOrUpdateBondBusinessEntityMap,
            HostSetupNetworksValidatorHelper hostSetupNetworksValidatorHelper) {
        this.params = params;
        this.existingInterfacesMap = existingInterfacesMap;
        this.createOrUpdateBondBusinessEntityMap = createOrUpdateBondBusinessEntityMap;

        this.hostSetupNetworksValidatorHelper = hostSetupNetworksValidatorHelper;
    }

    public ValidationResult validate() {
        ValidationResult vr = ValidationResult.VALID;
        vr = skipValidation(vr) ? vr : validNewOrModifiedLabels();
        vr = skipValidation(vr) ? vr : validRemovedLabels();
        vr = skipValidation(vr) ? vr : labelAppearsOnlyOnceInParams();
        return vr;
    }

    public ValidationResult validNewOrModifiedLabels() {
        ValidationResult vr = ValidationResult.VALID;

        Iterator<NicLabel> iterator = params.getLabels().iterator();
        while (iterator.hasNext() && vr.isValid()) {
            NicLabel nicLabel = iterator.next();

            vr = skipValidation(vr) ? vr : validateCoherentNicIdentification(nicLabel);
            vr = skipValidation(vr) ? vr : nicActuallyExistsOrReferencesNewBond(nicLabel);
            vr = skipValidation(vr) ? vr : labelBeingAttachedToNonVlanNonSlaveInterface(nicLabel);
            vr = skipValidation(vr) ? vr : labelBeingAttachedToValidBond(nicLabel);
        }

        return vr;
    }

    ValidationResult validateCoherentNicIdentification(NicLabel nicLabel) {
        return validateCoherentIdentification(nicLabel.getLabel(),
                nicLabel.getNicId(),
                nicLabel.getNicName(),
                EngineMessage.NIC_LABEL_REFERENCES_NICS_INCOHERENTLY, existingInterfacesMap);
    }

    <T extends BusinessEntity<Guid> & Nameable> ValidationResult validateCoherentIdentification(String violatingEntityId,
            Guid referringId,
            String referringName,
            EngineMessage message,
            BusinessEntityMap<T> map) {
        return hostSetupNetworksValidatorHelper.validateCoherentIdentification(violatingEntityId,
                referringId,
                referringName,
                message,
                map);
    }

    ValidationResult nicActuallyExistsOrReferencesNewBond(NicLabel nicLabel) {
        if (nicLabel.getNicName() != null) {
            boolean nicActuallyExistsOrReferencesNewBond =
                    isNicActuallyExistsOrReferencesNewBond(nicLabel.getNicName(), nicLabel.getNicId());

            boolean nicIsBeingRemoved =
                    nicLabel.getNicId() != null && params.getRemovedBonds().contains(nicLabel.getNicId());

            if (nicActuallyExistsOrReferencesNewBond && !nicIsBeingRemoved) {
                return ValidationResult.VALID;
            }
        }

        return new ValidationResult(EngineMessage.INTERFACE_ON_NIC_LABEL_NOT_EXIST,
                ReplacementUtils.createSetVariableString("INTERFACE_ON_NIC_LABEL_NOT_EXIST_ENTITY",
                        nicLabel.getLabel()),
                ReplacementUtils.createSetVariableString("interfaceName",
                        nicLabel.getNicName() != null ? nicLabel.getNicName() : nicLabel.getNicId()));
    }

    boolean isNicActuallyExistsOrReferencesNewBond(String nicName, Guid nicId) {
        return hostSetupNetworksValidatorHelper.isNicActuallyExistsOrReferencesNewBond(existingInterfacesMap,
                createOrUpdateBondBusinessEntityMap,
                nicName,
                nicId);
    }

    public ValidationResult validRemovedLabels() {
        ValidationResult vr = ValidationResult.VALID;

        Iterator<String> iterator = params.getRemovedLabels().iterator();
        while (iterator.hasNext() && vr.isValid()) {
            String label = iterator.next();

            vr = skipValidation(vr) ? vr : removedLabelExistsOnTheHost(label);
        }

        return vr;
    }

    ValidationResult removedLabelExistsOnTheHost(String label) {
        Set<String> exisitingHostLabels = new HashSet<>();

        for (VdsNetworkInterface nic : existingInterfacesMap.unmodifiableEntitiesByIdMap().values()) {
            if (NetworkUtils.isLabeled(nic)) {
                exisitingHostLabels.addAll(nic.getLabels());
            }
        }

        return ValidationResult.failWith(EngineMessage.LABEL_NOT_EXIST_IN_HOST,
                ReplacementUtils.createSetVariableString("LABEL_NOT_EXIST_IN_HOST_ENTITY", label))
                .unless(exisitingHostLabels.contains(label));
    }

    public ValidationResult labelAppearsOnlyOnceInParams() {
        Set<String> existingLabels = new HashSet<>();
        Set<String> duplicateLabels = new HashSet<>();

        for (NicLabel nicLabel : params.getLabels()) {
            String label = nicLabel.getLabel();
            addLabelToList(existingLabels, duplicateLabels, label);
        }

        for (String label : params.getRemovedLabels()) {
            addLabelToList(existingLabels, duplicateLabels, label);
        }

        return ValidationResult.failWith(EngineMessage.PARAMS_CONTAIN_DUPLICATE_LABELS,
                ReplacementUtils.replaceWith("PARAMS_CONTAIN_DUPLICATE_LABELS_LIST", new ArrayList<>(duplicateLabels)))
                .unless(duplicateLabels.isEmpty());
    }

    private void addLabelToList(Set<String> existingLabels, Set<String> duplicateLabels, String label) {
        if (existingLabels.contains(label)) {
            duplicateLabels.add(label);
        } else {
            existingLabels.add(label);
        }
    }

    ValidationResult labelBeingAttachedToNonVlanNonSlaveInterface(NicLabel nicLabel) {
        String interfaceName = nicLabel.getNicName();
        boolean isBondSlave = shouldBeConfigureAsBondSlave(interfaceName);
        VdsNetworkInterface existingNic = existingInterfacesMap.get(interfaceName);
        boolean isVlanDevice = existingNic == null ? false : NetworkCommonUtils.isVlan(existingNic);

        return ValidationResult.failWith(EngineMessage.LABEL_ATTACH_TO_IMPROPER_INTERFACE,
                ReplacementUtils.createSetVariableString(
                        "LABEL_ATTACH_TO_IMPROPER_INTERFACE_ENTITY",
                        interfaceName)).when(isBondSlave || isVlanDevice);
    }

    private boolean shouldBeConfigureAsBondSlave(String interfaceName) {
        // Check if the interface was updated to be a bond's slave
        for (CreateOrUpdateBond createOrUpdateBond : params.getCreateOrUpdateBonds()) {
            if (createOrUpdateBond.getSlaves().contains(interfaceName)) {
                return true;
            }
        }

        // The interface wasn't updated to be a slave, check if currently it is a slave.
        VdsNetworkInterface existingNic = existingInterfacesMap.get(interfaceName);

        if (existingNic == null) {
            return false;
        }

        if (existingNic.isPartOfBond()) {
            String bondName = existingNic.getBondName();
            VdsNetworkInterface bond = existingInterfacesMap.get(bondName);
            boolean bondWasRemoved = params.getRemovedBonds().contains(bond.getId());
            boolean slaveWasRemovedFromBond =
                    createOrUpdateBondBusinessEntityMap.containsKey(bondName)
                            && !createOrUpdateBondBusinessEntityMap.get(bondName).getSlaves().contains(interfaceName);

            return !bondWasRemoved && !slaveWasRemovedFromBond;
        }

        return false;
    }

    ValidationResult labelBeingAttachedToValidBond(NicLabel nicLabel) {
        String interfaceName = nicLabel.getNicName();
        VdsNetworkInterface nic = existingInterfacesMap.get(interfaceName);

        if (nic instanceof Bond) {
            CreateOrUpdateBond createOrUpdateBond = createOrUpdateBondBusinessEntityMap.containsKey(interfaceName)
                    ? createOrUpdateBondBusinessEntityMap.get(interfaceName)
                    : CreateOrUpdateBond.fromBond((Bond) nic);

            if (createOrUpdateBond.getSlaves().size() < 2) {
                return new ValidationResult(EngineMessage.IMPROPER_BOND_IS_LABELED,
                        ReplacementUtils.createSetVariableString(
                                HostInterfaceValidator.VAR_BOND_NAME,
                                createOrUpdateBond.getName()));
            }

        }

        return ValidationResult.VALID;
    }

    private boolean skipValidation(ValidationResult validationResult) {
        return !validationResult.isValid();
    }
}

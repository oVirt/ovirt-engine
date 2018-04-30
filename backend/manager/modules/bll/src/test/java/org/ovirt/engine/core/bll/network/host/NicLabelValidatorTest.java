package org.ovirt.engine.core.bll.network.host;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.HostInterfaceValidator;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class NicLabelValidatorTest {

    @Test
    public void validateCoherentNicIdentificationTest() {
        NicLabelValidator validator = spy(createNicLabelValidator());
        ValidationResult validationReuslt = new ValidationResult(EngineMessage.Unassigned);
        doReturn(validationReuslt).when(validator).validateCoherentIdentification(any(), any(), any(), any(), any());

        assertEquals(validationReuslt, validator.validateCoherentNicIdentification(new NicLabel()));
    }

    @Test
    public void nicActuallyExistsOrReferencesNoNicName() {
        NicLabelValidator validator = spy(createNicLabelValidator());
        mockIsNicActuallyExistsOrReferencesNewBond(validator, true);

        assertThatNicActuallyExistsOrReferencesNewBondFailed(validator, new NicLabel());
    }

    @Test
    public void nicActuallyExistsOrReferencesNewBondTrue() {
        NicLabelValidator validator = spy(createNicLabelValidator());
        mockIsNicActuallyExistsOrReferencesNewBond(validator, true);

        NicLabel nicLabel = new NicLabel();
        nicLabel.setNicName("anyName");

        assertThat(validator.nicActuallyExistsOrReferencesNewBond(nicLabel), isValid());
    }

    @Test
    public void nicActuallyExistsOrReferencesNewBondFalse() {
        NicLabelValidator validator = spy(createNicLabelValidator());
        mockIsNicActuallyExistsOrReferencesNewBond(validator, false);

        NicLabel nicLabel = new NicLabel();

        assertThatNicActuallyExistsOrReferencesNewBondFailed(validator, nicLabel);
    }

    @Test
    public void nicActuallyExistsOrReferencesNewBondTrueButBondIsRemoved() {
        HostSetupNetworksParameters params = createHostSetupNetworksParams();
        Guid removedBondId = Guid.newGuid();
        params.getRemovedBonds().add(removedBondId);
        NicLabelValidator validator = spy(createNicLabelValidator(params));
        mockIsNicActuallyExistsOrReferencesNewBond(validator, true);

        NicLabel nicLabel = new NicLabel();
        nicLabel.setNicId(removedBondId);
        nicLabel.setNicName("anyName");

        assertThatNicActuallyExistsOrReferencesNewBondFailed(validator, nicLabel);
    }

    private void assertThatNicActuallyExistsOrReferencesNewBondFailed(NicLabelValidator validator, NicLabel nicLabel) {
        assertThat(validator.nicActuallyExistsOrReferencesNewBond(nicLabel),
                failsWith(EngineMessage.INTERFACE_ON_NIC_LABEL_NOT_EXIST,
                        ReplacementUtils.createSetVariableString("INTERFACE_ON_NIC_LABEL_NOT_EXIST_ENTITY",
                                nicLabel.getLabel()),
                        ReplacementUtils.createSetVariableString("interfaceName",
                                nicLabel.getNicName())));
    }

    private void mockIsNicActuallyExistsOrReferencesNewBond(NicLabelValidator validator, boolean returnValue) {
        doReturn(returnValue).when(validator).isNicActuallyExistsOrReferencesNewBond(any(), any());
    }

    @Test
    public void labelAppearsOnlyOnceInParamsValid() {
        HostSetupNetworksParameters params =
                setLabelsOnParams(Collections.singleton(createNicLabel("lbl1")), Collections.singleton("lbl2"));

        assertThat(createNicLabelValidator(params).labelAppearsOnlyOnceInParams(), isValid());
    }

    @Test
    public void labelAppearsOnlyOnceInParamsTwiceInLabels() {
        HostSetupNetworksParameters params =
                setLabelsOnParams(createSet(createNicLabel("lbl1"), createNicLabel("lbl1")), createSet("lbl2"));

        asserThatLabelAppearsOnlyOnceInParamsFailed(createNicLabelValidator(params), new HashSet<>(
                Collections.singletonList("lbl1")));
    }

    @Test
    public void labelAppearsOnlyOnceInParamsInBothLabelsAndRemoveLabels() {
        HostSetupNetworksParameters params =
                setLabelsOnParams(createSet(createNicLabel("lbl1")), createSet("lbl1"));

        asserThatLabelAppearsOnlyOnceInParamsFailed(createNicLabelValidator(params), new HashSet<>(
                Collections.singletonList("lbl1")));
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> createSet(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    private HostSetupNetworksParameters setLabelsOnParams(Set<NicLabel> labels, Set<String> removedLabels) {
        HostSetupNetworksParameters params = createHostSetupNetworksParams();
        params.setLabels(labels);
        params.setRemovedLabels(removedLabels);

        return params;
    }

    private void asserThatLabelAppearsOnlyOnceInParamsFailed(NicLabelValidator validator, Set<String> duplicateLabels) {
        assertThat(validator.labelAppearsOnlyOnceInParams(),
                failsWith(EngineMessage.PARAMS_CONTAIN_DUPLICATE_LABELS,
                        ReplacementUtils.replaceWith("PARAMS_CONTAIN_DUPLICATE_LABELS_LIST",
                                new ArrayList<>(duplicateLabels))));
    }

    @Test
    public void removedLabelExistsOnTheHostNotExist() {
        assertThat(createNicLabelValidator().removedLabelExistsOnTheHost("lbl1"),
                failsWith(EngineMessage.LABEL_NOT_EXIST_IN_HOST,
                        ReplacementUtils.createSetVariableString("LABEL_NOT_EXIST_IN_HOST_ENTITY", "lbl1")));
    }

    @Test
    public void removedLabelExistsOnTheHostNotExistValid() {
        VdsNetworkInterface nic = createNic();
        nic.setLabels(createSet("lbl1"));
        List<VdsNetworkInterface> nics = Collections.singletonList(nic);
        assertThat(createNicLabelValidator(nics).removedLabelExistsOnTheHost("lbl1"), isValid());
    }

    @Test
    public void labelBeingAttachedToNonVlanNonSlaveInterfaceValid() {
        VdsNetworkInterface nic = createNic();
        List<VdsNetworkInterface> nics = Collections.singletonList(nic);

        NicLabel nicLabel = new NicLabel(nic.getId(), nic.getName(), "lbl1");

        assertThat(createNicLabelValidator(nics).labelBeingAttachedToNonVlanNonSlaveInterface(nicLabel), isValid());
    }

    @Test
    public void labelBeingAttachedToNonVlanNonSlaveInterfaceAttachToVlan() {
        VdsNetworkInterface nic = createNic();
        nic.setVlanId(1);

        assertLabelBeingAttachedToNonVlanNonSlaveInterfaceFailed(createHostSetupNetworksParams(), nic);
    }

    @Test
    public void labelBeingAttachedToNonVlanNonSlaveInterfaceAttachToExistingSlave() {
        VdsNetworkInterface nic = createNic();
        nic.setBondName("bond");

        assertLabelBeingAttachedToNonVlanNonSlaveInterfaceFailed(createHostSetupNetworksParams(), nic);
    }

    @Test
    public void labelBeingAttachedToNonVlanNonSlaveInterfaceAttachToNewSlave() {
        HostSetupNetworksParameters params = createHostSetupNetworksParams();

        VdsNetworkInterface slave = createNic();

        CreateOrUpdateBond createOrUpdateBond = new CreateOrUpdateBond();
        createOrUpdateBond.setName("bond");
        createOrUpdateBond.setSlaves(Collections.singleton(slave.getName()));

        params.setCreateOrUpdateBonds(Collections.singletonList(createOrUpdateBond));

        assertLabelBeingAttachedToNonVlanNonSlaveInterfaceFailed(params, slave);
    }

    @Test
    public void labelBeingAttachedToNonVlanNonSlaveInterfaceAttachToRemovedSlave() {
        HostSetupNetworksParameters params = createHostSetupNetworksParams();

        VdsNetworkInterface slave = createNic();

        Bond bondWithSlave = new Bond("bond");
        bondWithSlave.setSlaves(Collections.singletonList(slave.getName()));

        CreateOrUpdateBond updatedBond = new CreateOrUpdateBond();
        updatedBond.setName(bondWithSlave.getName());
        updatedBond.setSlaves(new HashSet<>());
        params.setCreateOrUpdateBonds(Collections.singletonList(updatedBond));

        NicLabel nicLabel = new NicLabel();
        nicLabel.setNicName(slave.getName());

        NicLabelValidator nicLabelValidator = createNicLabelValidator(params, Arrays.asList(bondWithSlave, slave));
        assertThat(nicLabelValidator.labelBeingAttachedToNonVlanNonSlaveInterface(nicLabel), isValid());
    }

    @Test
    public void labelBeingAttachedToNonVlanNonSlaveInterfaceAttachToSlaveOnRemovedBond() {
        HostSetupNetworksParameters params = createHostSetupNetworksParams();

        VdsNetworkInterface slave = createNic();

        Bond bondWithSlave = new Bond("bond");
        bondWithSlave.setId(Guid.newGuid());
        bondWithSlave.setSlaves(Collections.singletonList(slave.getName()));

        params.getRemovedBonds().add(bondWithSlave.getId());

        NicLabel nicLabel = new NicLabel();
        nicLabel.setNicName(slave.getName());

        assertThat(createNicLabelValidator(params,
                Arrays.asList(bondWithSlave, slave)).labelBeingAttachedToNonVlanNonSlaveInterface(nicLabel),
                isValid());
    }

    private void assertLabelBeingAttachedToNonVlanNonSlaveInterfaceFailed(HostSetupNetworksParameters params,
            VdsNetworkInterface attachLabelToNic) {
        List<VdsNetworkInterface> nics = new ArrayList<>();
        nics.add(attachLabelToNic);
        if (attachLabelToNic.getBondName() != null) {
            Bond bond = new Bond(attachLabelToNic.getBondName());
            bond.setId(Guid.newGuid());
            nics.add(bond);
        }

        NicLabel nicLabel = new NicLabel(attachLabelToNic.getId(), attachLabelToNic.getName(), "lbl1");
        assertThat(createNicLabelValidator(params, nics).labelBeingAttachedToNonVlanNonSlaveInterface(nicLabel),
                failsWith(EngineMessage.LABEL_ATTACH_TO_IMPROPER_INTERFACE,
                        ReplacementUtils.createSetVariableString(
                                "LABEL_ATTACH_TO_IMPROPER_INTERFACE_ENTITY",
                                attachLabelToNic.getName())));
    }

    @Test
    public void labelBeingAttachedToValidBondNotBond() {
        VdsNetworkInterface nic = createNic();
        List<VdsNetworkInterface> nics = Collections.singletonList(nic);

        NicLabel nicLabel = new NicLabel(nic.getId(), nic.getName(), "lbl1");
        assertThat(createNicLabelValidator(nics).labelBeingAttachedToValidBond(nicLabel), isValid());
    }

    @Test
    public void labelBeingAttachedToValidBondExistingBondValid() {
        Bond bond = new Bond("bond");
        bond.setSlaves(Arrays.asList("slave1", "slave2"));
        List<VdsNetworkInterface> nics = new ArrayList<>(Collections.singletonList(bond));

        NicLabel nicLabel = new NicLabel(bond.getId(), bond.getName(), "lbl1");
        assertThat(createNicLabelValidator(nics).labelBeingAttachedToValidBond(nicLabel), isValid());
    }

    @Test
    public void labelBeingAttachedToValidBondNewBondValid() {
        CreateOrUpdateBond createOrUpdateBond = new CreateOrUpdateBond();
        createOrUpdateBond.setName("bond");
        createOrUpdateBond.setSlaves(new HashSet<>(Arrays.asList("slave1", "slave2")));

        HostSetupNetworksParameters params = createHostSetupNetworksParams();
        params.getCreateOrUpdateBonds().add(createOrUpdateBond);
        NicLabel nicLabel = new NicLabel(createOrUpdateBond.getId(), createOrUpdateBond.getName(), "lbl1");
        assertThat(createNicLabelValidator(params, new ArrayList<>()).labelBeingAttachedToValidBond(nicLabel),
                isValid());
    }

    @Test
    public void labelBeingAttachedToValidBondExistingBondNotValid() {
        Bond bond = new Bond("bond");
        bond.setSlaves(Collections.singletonList("slave1"));

        assertLabelBeingAttachedToValidBondFailed(createHostSetupNetworksParams(), bond);
    }

    @Test
    public void labelBeingAttachedToValidBondAttachedBondBecomingNotValid() {
        Bond existingBond = new Bond("bond");
        existingBond.setSlaves(Arrays.asList("slave1", "slave2"));

        HostSetupNetworksParameters params = createHostSetupNetworksParams();
        CreateOrUpdateBond updatedBond = new CreateOrUpdateBond();
        updatedBond.setName(existingBond.getName());
        updatedBond.setSlaves(new HashSet<>());
        params.getCreateOrUpdateBonds().add(updatedBond);

        assertLabelBeingAttachedToValidBondFailed(params, existingBond);
    }

    private void assertLabelBeingAttachedToValidBondFailed(HostSetupNetworksParameters params, VdsNetworkInterface nic) {
        List<VdsNetworkInterface> nics = Collections.singletonList(nic);
        NicLabel nicLabel = new NicLabel(nic.getId(), nic.getName(), "lbl1");
        assertThat(createNicLabelValidator(params, nics).labelBeingAttachedToValidBond(nicLabel),
                failsWith(EngineMessage.IMPROPER_BOND_IS_LABELED,
                        ReplacementUtils.createSetVariableString(
                                HostInterfaceValidator.VAR_BOND_NAME,
                                nic.getName())));
    }

    private VdsNetworkInterface createNic() {
        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setId(Guid.newGuid());
        nic.setName(nic.getId().toString());
        return nic;
    }

    private NicLabelValidator createNicLabelValidator() {
        return createNicLabelValidator(createHostSetupNetworksParams(),
                Collections.emptyList());
    }

    private NicLabelValidator createNicLabelValidator(List<VdsNetworkInterface> nics) {
        return createNicLabelValidator(createHostSetupNetworksParams(),
                nics);
    }

    private NicLabelValidator createNicLabelValidator(HostSetupNetworksParameters params) {
        return createNicLabelValidator(params, Collections.emptyList());
    }

    private NicLabelValidator createNicLabelValidator(HostSetupNetworksParameters params,
            List<VdsNetworkInterface> nics) {
        NicLabelValidator validator =
                new NicLabelValidator(params,
                        new BusinessEntityMap<>(nics),
                        new BusinessEntityMap<>(params.getCreateOrUpdateBonds()),
                        new HostSetupNetworksValidatorHelper());
        return validator;
    }

    private HostSetupNetworksParameters createHostSetupNetworksParams() {
        return new HostSetupNetworksParameters(Guid.newGuid());
    }

    private NicLabel createNicLabel(String label) {
        Guid id = Guid.newGuid();
        return new NicLabel(id, id.toString(), label);
    }
}

package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class HostInterfaceValidatorTest {

    @Test
    public void testInterfaceExistsWhenInterfaceIsNull() throws Exception {
        assertThat(new HostInterfaceValidator(null).interfaceExists(),
                failsWith(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST));
    }

    @Test
    public void testInterfaceExistsWhenInterfaceIsNotNull() throws Exception {
        assertThat(new HostInterfaceValidator(createVdsNetworkInterfaceWithName()).interfaceExists(), isValid());
    }

    @Test
    public void testInterfaceAlreadyLabeledWithWhenLabelsIsNull() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setLabels(null);
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceAlreadyLabeledWith("labelA"), isValid());
    }

    @Test
    public void testInterfaceAlreadyLabeledWithWhenInterfaceIsNotLabeled() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setLabels(new HashSet<String>());
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceAlreadyLabeledWith("labelA"), isValid());
    }

    @Test
    public void testInterfaceAlreadyLabeledWithWhenInterfaceIsLabeledByDifferentLabel() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setLabels(Collections.singleton("labelB"));
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceAlreadyLabeledWith("labelA"), isValid());
    }

    @Test
    public void testInterfaceAlreadyLabeledWithWhenInterfaceIsLabeledBySameLabel() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setLabels(Collections.singleton("labelA"));
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceAlreadyLabeledWith("labelA"),
                failsWith(EngineMessage.INTERFACE_ALREADY_LABELED));
    }

    @Test
    public void testInterfaceInHostWhenInSameHost() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        Guid vdsId = Guid.newGuid();
        vdsNetworkInterface.setVdsId(vdsId);
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceInHost(vdsId), isValid());
    }

    @Test
    public void testInterfaceInHostWhenInDifferentHost() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setVdsId(Guid.newGuid());
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceInHost(Guid.newGuid()),
                failsWith(EngineMessage.NIC_NOT_EXISTS_ON_HOST));
    }

    @Test
    public void testLabeledValidBondWhenInterfaceIsNotBonded() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setBonded(false);

        List<VdsNetworkInterface> existingInterfaces = Collections.emptyList();
        assertThat("not bonded interface can be labeled",
                new HostInterfaceValidator(vdsNetworkInterface).labeledValidBond(existingInterfaces),
                isValid());
    }

    @Test
    public void testLabeledValidBondWhenBondHasNoSlave() throws Exception {
        assertCorrectSlaveCountInLabeledValidBondsWhenInsufficientBonds(0);
    }

    @Test
    public void testLabeledValidBondWhenBondHasOneSlave() throws Exception {
        assertCorrectSlaveCountInLabeledValidBondsWhenInsufficientBonds(1);
    }

    @Test
    public void testLabeledValidBondWhenSufficientNumberOfSlaves() throws Exception {
        assertCorrectSlaveCountInLabeledValidBonds(
            2,
            "bonded interface with two or more slaves should be valid bond",
            isValid());
    }

    private void assertCorrectSlaveCountInLabeledValidBondsWhenInsufficientBonds(int numberOfSlaves) {
        assertCorrectSlaveCountInLabeledValidBonds(
            numberOfSlaves,
            String.format("bonded interface with only %1$d slaves is not valid bond", numberOfSlaves),
            failsWith(EngineMessage.IMPROPER_BOND_IS_LABELED));
    }

    private void assertCorrectSlaveCountInLabeledValidBonds(int numberOfSlaves,
        String reason,
        Matcher<ValidationResult> valid) {
        String bondName = "bondName";
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName(bondName, true);
        List<VdsNetworkInterface> slaves = createGivenCountOfSlavesForBond(bondName, numberOfSlaves);

        assertThat(reason, new HostInterfaceValidator(vdsNetworkInterface).labeledValidBond(slaves), valid);
    }


    @Test
    public void testAddLabelToNicAndValidateWhenUsingInvalidLabel() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setLabels(new HashSet<String>());
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).addLabelToNicAndValidate("**uups!**",
                        new ArrayList<Class<?>>()),
                failsWith(EngineMessage.IMPROPER_INTERFACE_IS_LABELED));
    }

    private VdsNetworkInterface createVdsNetworkInterfaceWithName() {
        return createVdsNetworkInterfaceWithName("name");
    }

    private VdsNetworkInterface createVdsNetworkInterfaceWithName(String name) {
        return createVdsNetworkInterfaceWithName(name, false);
    }

    private VdsNetworkInterface createVdsNetworkInterfaceWithName(String name, boolean bonded) {
        VdsNetworkInterface vdsNetworkInterface = new VdsNetworkInterface();
        vdsNetworkInterface.setName(name);
        vdsNetworkInterface.setBonded(bonded);
        return vdsNetworkInterface;
    }

    @Test
    public void testAddLabelToNicAndValidateWhenUsingValidLabel() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setLabels(new HashSet<String>());
        HostInterfaceValidator validator = new HostInterfaceValidator(vdsNetworkInterface);
        assertThat(validator.addLabelToNicAndValidate("ok", Collections.<Class<?>> emptyList()), isValid());
    }

    @Test
    public void testValidBond() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setBonded(false);
        HostInterfaceValidator validator = new HostInterfaceValidator(vdsNetworkInterface);
        assertThat(validator.validBond(Collections.<VdsNetworkInterface> emptyList()), isValid());
    }

    @Test
    public void testValidBondWhenBondHasNoSlave() throws Exception {
        assertCorrectSlaveCountInValidBondsWhenInsufficientBonds(0);
    }

    @Test
    public void testValidBondWhenBondHasOneSlave() throws Exception {
        assertCorrectSlaveCountInValidBondsWhenInsufficientBonds(1);
    }

    @Test
    public void testValidBondWhenSufficientNumberOfSlaves() throws Exception {
        assertCorrectSlaveCountInValidBonds(2,
            "bonded interface with two or more slaves should be valid bond",
            isValid());
    }

    private void assertCorrectSlaveCountInValidBondsWhenInsufficientBonds(int numberOfSlaves) {
        assertCorrectSlaveCountInValidBonds(
            numberOfSlaves,
            String.format("bonded interface with only %1$d slaves is not valid bond", numberOfSlaves),
            failsWith(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT));
    }

    private void assertCorrectSlaveCountInValidBonds(int numberOfSlaves,
        String reason,
        Matcher<ValidationResult> matcher) {

        String bondName = "bondName";
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName(bondName, true);
        List<VdsNetworkInterface> slaves = createGivenCountOfSlavesForBond(bondName, numberOfSlaves);

        assertThat(reason, new HostInterfaceValidator(vdsNetworkInterface).validBond(slaves), matcher);
    }

    private List<VdsNetworkInterface> createGivenCountOfSlavesForBond(String bondName, int numberOfSlaves) {
        List<VdsNetworkInterface> slaves = new ArrayList<>();
        for (int i = 0; i < numberOfSlaves; i++) {
            VdsNetworkInterface slave = createVdsNetworkInterfaceWithName();
            slave.setBondName(bondName);
            slaves.add(slave);
        }
        return slaves;
    }

    @Test
    public void testAnotherInterfaceAlreadyLabeledWithThisLabel() throws Exception {
        //identity of nics is compared based on names.
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName("name");
        VdsNetworkInterface preexistingVdsNetworkInterface = createVdsNetworkInterfaceWithName("differentName");

        //set same label
        String label = "label";
        vdsNetworkInterface.setLabels(Collections.singleton(label));
        preexistingVdsNetworkInterface.setLabels(Collections.singleton(label));

        HostInterfaceValidator validator = new HostInterfaceValidator(vdsNetworkInterface);
        List<VdsNetworkInterface> preexistingInterfaces = Collections.singletonList(preexistingVdsNetworkInterface);
        assertThat("different nics cannot have same label set.",
                validator.anotherInterfaceAlreadyLabeledWithThisLabel(label, preexistingInterfaces),
                failsWith(EngineMessage.OTHER_INTERFACE_ALREADY_LABELED));
    }

    @Test
    public void testAnotherInterfaceAlreadyLabeledWithThisLabelDoNotCompareWithSelf() throws Exception {
        //identity of nics is compared based on names.
        String sameName = "sameName";
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName(sameName);
        VdsNetworkInterface preexistingVdsNetworkInterface = createVdsNetworkInterfaceWithName(sameName);

        //set same label
        String label = "label";
        vdsNetworkInterface.setLabels(Collections.singleton(label));
        preexistingVdsNetworkInterface.setLabels(Collections.singleton(label));

        HostInterfaceValidator validator = new HostInterfaceValidator(vdsNetworkInterface);
        List<VdsNetworkInterface> preexistingInterfaces = Collections.singletonList(preexistingVdsNetworkInterface);
        assertThat(validator.anotherInterfaceAlreadyLabeledWithThisLabel(label, preexistingInterfaces), isValid());
    }

    @Test
    public void testLabeledNetworkAttachedToThisInterfaceWhenNetworkIsAssignedToWrongInterface() throws Exception {
        String networkName = "networkA";
        Network network = new Network();
        network.setName(networkName);

        VdsNetworkInterface nicToWhichLabeledNetworkShouldBeAttached = createVdsNetworkInterfaceWithName("nicA");
        VdsNetworkInterface nicImproperlyAssignedToNetwork = createVdsNetworkInterfaceWithName("nicB");
        nicImproperlyAssignedToNetwork.setNetworkName(networkName);

        List<VdsNetworkInterface> hostInterfaces = Arrays.asList(
                nicToWhichLabeledNetworkShouldBeAttached,
                nicImproperlyAssignedToNetwork);


        HostInterfaceValidator validator = new HostInterfaceValidator(nicToWhichLabeledNetworkShouldBeAttached);
        assertThat(validator.networksAreAttachedToThisInterface(hostInterfaces, Collections.singletonList(network)),
                failsWith(EngineMessage.LABELED_NETWORK_ATTACHED_TO_WRONG_INTERFACE));
    }

    @Test
    public void testLabeledNetworkAttachedToThisInterface() throws Exception {
        String networkName = "networkA";
        Network network = new Network();
        network.setName(networkName);

        VdsNetworkInterface nicToWhichLabeledNetworkShouldBeAttached = createVdsNetworkInterfaceWithName("nicA");
        nicToWhichLabeledNetworkShouldBeAttached.setNetworkName(networkName);

        List<VdsNetworkInterface> hostInterfaces = Arrays.asList(
                nicToWhichLabeledNetworkShouldBeAttached,
                createVdsNetworkInterfaceWithName("nicB"));


        HostInterfaceValidator validator = new HostInterfaceValidator(nicToWhichLabeledNetworkShouldBeAttached);
        List<Network> clusterNetworksWithLabel = Collections.singletonList(network);
        assertThat(validator.networksAreAttachedToThisInterface(hostInterfaces, clusterNetworksWithLabel), isValid());
    }

    @Test
    public void testNetworkCanBeAttachedNetworkAttachmentCannotBeAddedOnVlanInterfaces() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setVlanId(1);

        assertThat(new HostInterfaceValidator(vdsNetworkInterface).networkCanBeAttached(),
                failsWith(EngineMessage.CANNOT_ADD_NETWORK_ATTACHMENT_ON_SLAVE_OR_VLAN));
    }

    @Test
    public void testNetworkCanBeAttachedNetworkAttachmentCannotBeAddedOnBondSlave() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setBondName("bondName");

        assertThat(new HostInterfaceValidator(vdsNetworkInterface).networkCanBeAttached(),
                failsWith(EngineMessage.CANNOT_ADD_NETWORK_ATTACHMENT_ON_SLAVE_OR_VLAN));
    }

    @Test
    public void testNetworkCanBeAttached() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).networkCanBeAttached(), isValid());
    }

    @Test
    public void testInterfaceByNameExists() throws Exception {
        assertThat(new HostInterfaceValidator(createVdsNetworkInterfaceWithName()).interfaceByNameExists(), isValid());
    }

    @Test
    public void testInterfaceByNameExistsWhenInterfaceIsNull() throws Exception {
        assertThat(new HostInterfaceValidator(null).interfaceByNameExists(),
                failsWith(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST));
    }

    @Test
    public void testInterfaceByNameExistsWhenInterfacesNameIsNull() throws Exception {
        assertThat(new HostInterfaceValidator(createVdsNetworkInterfaceWithName(null)).interfaceByNameExists(),
                failsWith(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST));
    }

    @Test
    public void testInterfaceIsBondWhenInterfaceIsNull() throws Exception {
        assertThat(new HostInterfaceValidator(null).interfaceIsBondOrNull(), isValid());
    }

    @Test
    public void testInterfaceIsBondWhenInterfaceIsBonded() throws Exception {
        VdsNetworkInterface iface = createVdsNetworkInterfaceWithName();
        iface.setBonded(true);
        assertThat(new HostInterfaceValidator(iface).interfaceIsBondOrNull(), isValid());
    }

    @Test
    public void testInterfaceIsBondWhenInterfaceIsNotBonded() throws Exception {
        VdsNetworkInterface iface = createVdsNetworkInterfaceWithName();
        iface.setBonded(false);
        assertThat(new HostInterfaceValidator(iface).interfaceIsBondOrNull(),
                failsWith(EngineMessage.NETWORK_INTERFACE_IS_NOT_BOND));
    }

    @Test
    public void testInterfaceIsValidSlave() throws Exception {
        assertThat(new HostInterfaceValidator(createVdsNetworkInterfaceWithName()).interfaceIsValidSlave(), isValid());
    }

    @Test
    public void testInterfaceIsValidSlaveWhenInterfaceIsBond() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setBonded(true);
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceIsValidSlave(),
                failsWith(EngineMessage.NETWORK_INTERFACE_BOND_OR_VLAN_CANNOT_BE_SLAVE));
    }

    @Test
    public void testInterfaceIsValidSlaveWhenInterfaceIsVlan() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setVlanId(1);
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceIsValidSlave(),
                failsWith(EngineMessage.NETWORK_INTERFACE_BOND_OR_VLAN_CANNOT_BE_SLAVE));
    }
}

package org.ovirt.engine.core.bll.network.host;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.bll.network.host.UnmanagedNetworkValidator.LABEL;
import static org.ovirt.engine.core.bll.network.host.UnmanagedNetworkValidator.NETWORK;
import static org.ovirt.engine.core.bll.network.host.UnmanagedNetworkValidator.NIC;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ReplacementUtils;


@RunWith(MockitoJUnitRunner.class)
public class UnmanagedNetworkValidatorTest {

    private UnmanagedNetworkValidator validator = new UnmanagedNetworkValidator();

    @Test
    public void testFilterNicsWithUnmanagedNetworks(){
        VdsNetworkInterface nicManaged = createNicWithNetworkImplementationDetails("eth0", true);
        VdsNetworkInterface nicUnmanaged = createNicWithNetworkImplementationDetails("eth1", false);
        List<VdsNetworkInterface> existingInterfaces = Arrays.asList(nicManaged, nicUnmanaged);
        Set<String> unmanagedNicsSet = validator.filterNicsWithUnmanagedNetworks(existingInterfaces, Collections.emptySet());
        assertEquals(1, unmanagedNicsSet.size());
        assertTrue(unmanagedNicsSet.contains("eth1"));
    }

    @Test
    public void testFilterNicsWithUnmanagedNetworksNetworkImplementationDetailsIsNull(){
        VdsNetworkInterface nicWithNetworkImplementationsDetailsNull = createNic("eth1");
        List<VdsNetworkInterface> existingInterfaces = Arrays.asList(nicWithNetworkImplementationsDetailsNull);
        Set<String> unmanagedNicsSet = validator.filterNicsWithUnmanagedNetworks(existingInterfaces, Collections.emptySet());
        assertTrue(unmanagedNicsSet.isEmpty());
    }

    public void testFilterNicsWithUnmanagedNetworksUmngmtNetRemoved(){

        VdsNetworkInterface nicWithUnmanagedToBeRemoved = createNicWithNetworkImplementationDetails("eth_unmanaged1_toBeRemoved", false);
        VdsNetworkInterface nicWithUnmanaged = createNicWithNetworkImplementationDetails("eth_unmanaged", false);

        VdsNetworkInterface nicWithManagedToBeRemoved = createNicWithNetworkImplementationDetails("eth_managed_toBeRemoved", true);
        VdsNetworkInterface nicWithManaged = createNicWithNetworkImplementationDetails("eth_managed", true);

        List<VdsNetworkInterface> existingInterfaces = Arrays.asList(nicWithUnmanagedToBeRemoved, nicWithUnmanaged, nicWithManagedToBeRemoved, nicWithManaged);

        Collection<String> removedUnmanagedNetworks = Arrays.asList("eth_unmanaged1_toBeRemoved", "eth_managed_toBeRemoved");
        Set<String> unmanagedNicsSet = validator.filterNicsWithUnmanagedNetworks(existingInterfaces, removedUnmanagedNetworks);
        assertEquals(1, unmanagedNicsSet.size());
        assertTrue(unmanagedNicsSet.contains("eth_unmanaged"));
    }

    @Test
    public void testValidateLabels(){
        String nicWithUnmanagedNetwork = "eth_unmanaged";

        NicLabel label = new NicLabel(null, "eth1", "label1");
        NicLabel labelWithUnmanagedNetwork = new NicLabel(null, nicWithUnmanagedNetwork, "labelOnUnmanaged");
        Collection<NicLabel> labels = Arrays.asList(label, labelWithUnmanagedNetwork);

        ValidationResult result = validator.validateLabels(nicWithUnmanagedNetwork, labels);
        assertThat(result,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_LABEL_ON_UNMANAGED_NETWORK,
                        ReplacementUtils.createSetVariableString(LABEL, "labelOnUnmanaged"),
                        ReplacementUtils.createSetVariableString(NIC, "eth_unmanaged")));


        NicLabel labelWithoutUnmanaged = new NicLabel(null, "eth_other", "label1");
        Collection<NicLabel> labelsNotOnUnmanagedNetwork = Arrays.asList(labelWithoutUnmanaged);

        result = validator.validateLabels(nicWithUnmanagedNetwork, labelsNotOnUnmanagedNetwork);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidateLabelsForVlan(){
        Nic nic = createNic("eth0");

        NicLabel label = new NicLabel(null, "eth1", "label1");
        NicLabel labelWithUnmanagedNetwork = new NicLabel(null, "eth0", "labelOnUnmanaged");
        Collection<NicLabel> labels = Arrays.asList(label, labelWithUnmanagedNetwork);

        VdsNetworkInterface vlanNic = createVlanNic(nic, "eth0.100", 100, false);

        List<VdsNetworkInterface> existingInterfaces = Arrays.asList(vlanNic);
        Set<String> nicsWithUnmanagedNetworks = validator.filterNicsWithUnmanagedNetworks(existingInterfaces, Collections.emptySet());
        assertEquals(1, nicsWithUnmanagedNetworks.size());
        String vlanNicName = nicsWithUnmanagedNetworks.iterator().next();


        ValidationResult result = validator.validateLabels(vlanNicName, labels);
        assertThat(result,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_LABEL_ON_UNMANAGED_NETWORK,
                        ReplacementUtils.createSetVariableString(LABEL, "labelOnUnmanaged"),
                        ReplacementUtils.createSetVariableString(NIC, "eth0")));


        NicLabel labelWithoutUnmanaged = new NicLabel(null, "eth_other", "label1");
        Collection<NicLabel> labelsNotOnUnmanagedNetwork = Arrays.asList(labelWithoutUnmanaged);

        result = validator.validateLabels(vlanNicName, labelsNotOnUnmanagedNetwork);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidateAttachements(){
        String unmanagedNicName = "eth0";
        NetworkAttachment attachement1 = new NetworkAttachment();
        attachement1.setNetworkName("network1");
        attachement1.setNicName(unmanagedNicName);

        NetworkAttachment attachement2 = new NetworkAttachment();
        attachement2.setNetworkName("any2");
        attachement2.setNicName("eth1");

        List<NetworkAttachment> attachementList = Arrays.asList(attachement1, attachement2);
        Nic unmgmtNic = createNicWithNetworkImplementationDetails(unmanagedNicName, false);

        List<VdsNetworkInterface> existingInterfaces = Arrays.asList(unmgmtNic);
        Set<String> nicsWithUnmanagedNetworks = validator.filterNicsWithUnmanagedNetworks(existingInterfaces, Collections.emptySet());
        assertEquals(1, nicsWithUnmanagedNetworks.size());
        String filteredNicName = nicsWithUnmanagedNetworks.iterator().next();
        assertEquals(filteredNicName, unmanagedNicName);

        ValidationResult result = validator.validateAttachements(filteredNicName, attachementList);
        assertThat(result,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_ATTACHEMENT_ON_UNMANAGED_NETWORK,
                        ReplacementUtils.createSetVariableString(NETWORK, "network1"),
                        ReplacementUtils.createSetVariableString(NIC, "eth0")));

        result = validator.validateAttachements("eth7", attachementList);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidateAttachementsForVlans(){
        String unmanagedBaseNicName = "eth0";

        NetworkAttachment attachement1 = new NetworkAttachment();
        attachement1.setNetworkName("network1");
        attachement1.setNicName(unmanagedBaseNicName);

        NetworkAttachment attachement2 = new NetworkAttachment();
        attachement2.setNetworkName("any2");
        attachement2.setNicName("eth1");

        List<NetworkAttachment> attachementList = Arrays.asList(attachement1, attachement2);
        Nic nic = createNic(unmanagedBaseNicName);
        VdsNetworkInterface vlanNic = createVlanNic(nic, "eth0.100", 100, false);

        List<VdsNetworkInterface> existingInterfaces = Arrays.asList(vlanNic);
        Set<String> nicsWithUnmanagedNetworks = validator.filterNicsWithUnmanagedNetworks(existingInterfaces, Collections.emptySet());
        assertEquals(1, nicsWithUnmanagedNetworks.size());
        String filteredNicName = nicsWithUnmanagedNetworks.iterator().next();
        assertEquals(filteredNicName, unmanagedBaseNicName);

        ValidationResult result = validator.validateAttachements(filteredNicName, attachementList);
        assertThat(result,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_ATTACHEMENT_ON_UNMANAGED_NETWORK,
                        ReplacementUtils.createSetVariableString(NETWORK, "network1"),
                        ReplacementUtils.createSetVariableString(NIC, "eth0")));

        result = validator.validateAttachements("eth7", attachementList);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidateRemovedUnmanagedNetworks(){
        String networkName = "networkName";

        Nic nic = createNicWithNetworkImplementationDetails("eth0", false);
        nic.setNetworkName(networkName);

        Network network = new Network();
        network.setName(networkName);

        ValidationResult result = validator.validateRemovedUnmanagedNetworks(
                Arrays.asList(networkName),
                Arrays.asList(nic),
                new BusinessEntityMap<>(Collections.emptySet()));
        assertTrue(result.isValid());

        result = validator.validateRemovedUnmanagedNetworks(
                Arrays.asList(networkName),
                Arrays.asList(nic),
                new BusinessEntityMap<>(Arrays.asList(network)));

        assertThat(result,
                failsWith(EngineMessage.REMOVED_UNMANAGED_NETWORK_IS_A_CLUSTER_NETWORK,
                        ReplacementUtils.createSetVariableString(NETWORK, networkName)));

        String unmanagedNetworkNotPresentOnAnyNic = "unmanagedNetworkNotPresentOnAnyNic";
        result = validator.validateRemovedUnmanagedNetworks(
                Arrays.asList(networkName, unmanagedNetworkNotPresentOnAnyNic),
                Arrays.asList(nic),
                new BusinessEntityMap<>(Collections.emptySet()));

        assertThat(result,
                failsWith(EngineMessage.REMOVED_UNMANAGED_NETWORK_DOES_NOT_EXISIT_ON_HOST,
                        ReplacementUtils.createSetVariableString(NETWORK, unmanagedNetworkNotPresentOnAnyNic)));
    }

    private Nic createNic(String name){
        Nic nic = new Nic();
        nic.setName(name);
        return nic;
    }

    public VdsNetworkInterface createVlanNic(VdsNetworkInterface baseNic, String nicName, Integer vlanId, boolean isManaged) {
        VdsNetworkInterface vlanNic = new VdsNetworkInterface();
        vlanNic.setId(Guid.newGuid());
        vlanNic.setName(nicName);
        vlanNic.setVlanId(vlanId);
        vlanNic.setBaseInterface(baseNic.getName());
        vlanNic.setNetworkName("unmanagedNetworkName");
        vlanNic.setNetworkImplementationDetails(new NetworkImplementationDetails(true, isManaged));

        return vlanNic;
    }

    private Nic createNicWithNetworkImplementationDetails(String name, boolean isManaged){
        Nic nic = createNic(name);
        NetworkImplementationDetails networkImplementationDetails1 = new NetworkImplementationDetails(true, isManaged);
        nic.setNetworkImplementationDetails(networkImplementationDetails1);
        return nic;
    }
}

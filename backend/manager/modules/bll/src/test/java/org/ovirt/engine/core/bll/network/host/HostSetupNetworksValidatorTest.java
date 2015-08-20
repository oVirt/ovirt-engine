package org.ovirt.engine.core.bll.network.host;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.network.host.HostSetupNetworksValidator.VAR_NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS_LIST;
import static org.ovirt.engine.core.bll.network.host.HostSetupNetworksValidator.VAR_NETWORK_NAMES;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;
import static org.ovirt.engine.core.utils.ReplacementUtils.replaceWith;
import static org.ovirt.engine.core.utils.linq.LinqUtils.concat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.validator.HostInterfaceValidator;
import org.ovirt.engine.core.bll.validator.HostNetworkQosValidator;
import org.ovirt.engine.core.bll.validator.ValidationResultMatchers;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.vdsbroker.EffectiveHostNetworkQos;

@RunWith(MockitoJUnitRunner.class)
public class HostSetupNetworksValidatorTest {

    private VDS host;
    private ManagementNetworkUtil managementNetworkUtil;

    @Mock
    private NetworkDao networkDaoMock;

    @Mock
    private NetworkAttachmentDao networkAttachmentDaoMock;

    @Mock
    private NetworkClusterDao networkClusterDaoMock;

    @Mock
    private VdsDao vdsDaoMock;

    @Mock
    private VmDao vmDao;

    @Mock
    private EffectiveHostNetworkQos effectiveHostNetworkQos;

    private Bond bond;

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(
        mockConfig(ConfigValues.NetworkCustomPropertiesSupported, Version.v3_4.toString(), false),
        mockConfig(ConfigValues.NetworkCustomPropertiesSupported, Version.v3_5.toString(), true),
        mockConfig(ConfigValues.NetworkCustomPropertiesSupported, Version.v3_6.toString(), true),
        mockConfig(ConfigValues.HostNetworkQosSupported, Version.v3_4.toString(), false),
        mockConfig(ConfigValues.HostNetworkQosSupported, Version.v3_5.toString(), false),
        mockConfig(ConfigValues.HostNetworkQosSupported, Version.v3_6.toString(), true));

    @Before
    public void setUp() throws Exception {
        host = new VDS();
        host.setId(Guid.newGuid());
        host.setVdsGroupCompatibilityVersion(Version.v3_5);

        managementNetworkUtil = Mockito.mock(ManagementNetworkUtil.class);

        bond = new Bond();
        bond.setId(Guid.newGuid());
    }

    public void testNotRemovingLabeledNetworksReferencingUnlabeledNetworkRemovalIsOk() throws Exception {
        Network unlabeledNetwork = new Network();
        unlabeledNetwork.setId(Guid.newGuid());

        NetworkAttachment networkAttachment = createNetworkAttachment(unlabeledNetwork);

        HostSetupNetworksValidator validator =
            createHostSetupNetworksValidator(Collections.singletonList(unlabeledNetwork));
        assertThat(validator.notRemovingLabeledNetworks(networkAttachment), isValid());
    }

    @Test
    public void testNotRemovingLabeledNetworksWhenNicNameDoesNotReferenceExistingNicItsOkToRemove() throws Exception {
        Network labeledNetwork = new Network();
        labeledNetwork.setId(Guid.newGuid());
        labeledNetwork.setLabel("label");

        NetworkAttachment networkAttachment = createNetworkAttachment(labeledNetwork);
        networkAttachment.setNicName("noLongerExistingNicName");

        VdsNetworkInterface existingNic = new VdsNetworkInterface();
        existingNic.setName("existingNicName");

        HostSetupNetworksValidator validator =
            createHostSetupNetworksValidator(Collections.singletonList(labeledNetwork));
        assertThat(validator.notRemovingLabeledNetworks(networkAttachment), isValid());
    }

    @Test
    public void testNotRemovingLabeledNetworksWhenRemovingLabeledNetworkUnrelatedToRemovedBond() throws Exception {
        String nicName = "nicName";
        String label = "label";

        Network labeledNetwork = new Network();
        labeledNetwork.setName("networkName");
        labeledNetwork.setId(Guid.newGuid());
        labeledNetwork.setLabel(label);

        NetworkAttachment networkAttachment = createNetworkAttachment(labeledNetwork);
        networkAttachment.setNicName(nicName);

        VdsNetworkInterface existingNic = new VdsNetworkInterface();
        existingNic.setLabels(Collections.singleton(label));
        existingNic.setName(nicName);

        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(
            new HostSetupNetworksParameters(host.getId()),
            Collections.singletonList(existingNic),
            Collections.<NetworkAttachment> emptyList(),
            Collections.singletonList(labeledNetwork));

        assertThat(validator.notRemovingLabeledNetworks(networkAttachment),
            failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC,
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC_LIST,
                    labeledNetwork.getName())));

    }

    @Test
    public void testNotRemovingLabeledNetworksLabelRemovedFromNicValid() {
        VdsNetworkInterface nicWithLabel = createNic("nicWithLabel");
        nicWithLabel.setLabels(Collections.singleton("lbl1"));

        Network network = createNetworkWithNameAndLabel("net", "lbl1");
        NetworkAttachment removedAttachment = createNetworkAttachment(network, nicWithLabel);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.getRemovedLabels().add("lbl1");

        assertTestNotRemovingLabeledNetworksValid(nicWithLabel, removedAttachment, params, network);
    }

    @Test
    public void testNotRemovingLabeledNetworksLabelMovedToAnotherNicValid() {
        VdsNetworkInterface nicWithLabel = createNic("nicWithLabel");
        nicWithLabel.setLabels(Collections.singleton("lbl1"));

        Network network = createNetworkWithNameAndLabel("net", "lbl1");
        NetworkAttachment removedAttachment = createNetworkAttachment(network, nicWithLabel);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        NicLabel nicLabel = new NicLabel(Guid.newGuid(), nicWithLabel.getName() + "not", "lbl1");
        params.getLabels().add(nicLabel);

        assertTestNotRemovingLabeledNetworksValid(nicWithLabel, removedAttachment, params, network);
    }

    @Test
    public void testNotRemovingLabeledNetworksNicHasLabelOldAttachRemovedNewAttachWithSameNetworkAddedToNicValid() {
        VdsNetworkInterface nicWithLabel = createNic("nicWithLabel");
        nicWithLabel.setLabels(Collections.singleton("lbl1"));

        Network network = createNetworkWithNameAndLabel("net", "lbl1");
        NetworkAttachment removedAttachment = createNetworkAttachment(network, nicWithLabel);

        NetworkAttachment addedAttachment = new NetworkAttachment(removedAttachment);
        addedAttachment.setId(Guid.newGuid());

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.getNetworkAttachments().add(addedAttachment);

        assertTestNotRemovingLabeledNetworksValid(nicWithLabel, removedAttachment, params, network);
    }

    @Test
    public void testNotRemovingLabeledNetworksLabelAddedToNicOldAttachRemovedNewAttachWithSameNetworkAddedToNicValid() {
        VdsNetworkInterface nic = createNic("nicWithNoLabel");

        Network network = createNetworkWithNameAndLabel("net", "lbl1");
        NetworkAttachment removedAttachment = createNetworkAttachment(network, nic);
        NetworkAttachment addedAttachment = new NetworkAttachment(removedAttachment);
        addedAttachment.setId(Guid.newGuid());

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.getNetworkAttachments().add(addedAttachment);

        NicLabel nicLabel = new NicLabel(nic.getId(), nic.getName(), "lbl1");
        params.getLabels().add(nicLabel);

        assertTestNotRemovingLabeledNetworksValid(nic, removedAttachment, params, network);
    }

    private void assertTestNotRemovingLabeledNetworksValid(VdsNetworkInterface nic,
            NetworkAttachment removedAttachment,
            HostSetupNetworksParameters params, Network network) {
        HostSetupNetworksValidator validator =
                new HostSetupNetworksValidatorBuilder().setParams(params)
                        .setHost(host)
                        .setExistingInterfaces(Collections.singletonList(nic))
                        .addNetworks(network)
                        .build();

        assertThat(validator.notRemovingLabeledNetworks(removedAttachment), isValid());
    }

    @Test
    public void testNotMovingLabeledNetworkToDifferentNicWhenRemovingLabeledNetworkUnrelatedToRemovedBond() throws Exception {
        String label = "label";

        Network labeledNetwork = new Network();
        labeledNetwork.setId(Guid.newGuid());
        labeledNetwork.setLabel(label);

        VdsNetworkInterface existingNic = new VdsNetworkInterface();
        existingNic.setLabels(Collections.singleton(label));
        existingNic.setId(Guid.newGuid());
        existingNic.setName("nic1");

        VdsNetworkInterface existingNic2 = new VdsNetworkInterface();
        existingNic2.setId(Guid.newGuid());
        existingNic2.setName("nic2");

        Guid attachmentId = Guid.newGuid();
        NetworkAttachment existingNetworkAttachment = createNetworkAttachment(labeledNetwork, attachmentId);
        existingNetworkAttachment.setNicId(existingNic.getId());
        existingNetworkAttachment.setNicName(existingNic.getName());

        NetworkAttachment updatedNetworkAttachment = createNetworkAttachment(labeledNetwork, attachmentId);
        updatedNetworkAttachment.setNicId(existingNic2.getId());
        updatedNetworkAttachment.setNicName(existingNic2.getName());

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(Collections.singletonList(updatedNetworkAttachment));

        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(
            params,
            Arrays.asList(existingNic, existingNic2),
            Collections.singletonList(existingNetworkAttachment),
            Collections.singletonList(labeledNetwork)
        );

        assertThat(validator.notMovingLabeledNetworkToDifferentNic(updatedNetworkAttachment),
            failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC,
                ReplacementUtils.createSetVariableString(
                    "networkName", labeledNetwork.getName()),
                ReplacementUtils.createSetVariableString(
                    HostSetupNetworksValidator.ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC_ENTITY,
                    labeledNetwork.getLabel())));
    }

    @Test
    public void testNotRemovingLabeledNetworksWhenLabelRelatedToRemovedBond() throws Exception {
        String label = "label";
        String nicName = "nicName";

        Network labeledNetwork = new Network();
        labeledNetwork.setId(Guid.newGuid());
        labeledNetwork.setLabel(label);

        NetworkAttachment networkAttachment = createNetworkAttachment(labeledNetwork);
        networkAttachment.setNicName(nicName);

        bond.setLabels(Collections.singleton(label));
        bond.setName(nicName);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setRemovedBonds(Collections.singleton(bond.getId()));

        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(params,
            Collections.<VdsNetworkInterface> singletonList(bond),
            null,
            Collections.singletonList(labeledNetwork));
        assertThat(validator.notRemovingLabeledNetworks(networkAttachment), isValid());
    }

    @Test
    public void notMovingLabeledNetworkToDifferentNicNewLabelIsAddedToNic() {
        notMovingLabeledNetworkToDifferentNicCommonTest(/* nicContainslabel */false,
            false,
                /* labelShouldBeAddedToNic */true,
            false);
    }

    @Test
    public void notMovingLabeledNetworkToDifferentNicNoLabelOnNic() {
        notMovingLabeledNetworkToDifferentNicCommonTest(/* nicContainslabel */false,
                false,
                /* labelShouldBeAddedToNic */false,
                true);
    }

    @Test
    public void notMovingLabeledNetworkToDifferentNicHasLabel() {
        notMovingLabeledNetworkToDifferentNicCommonTest(/* nicContainslabel */true,
                /* labelShouldBeRemovedFromNic */ false,
            false,
            false);
    }

    @Test
    public void notMovingLabeledNetworkToDifferentNicLabelIsRemovedFromNic() {
        notMovingLabeledNetworkToDifferentNicCommonTest(/* nicContainslabel */true,
                /* labelShouldBeRemovedFromNic */true,
            false,
            true);
    }

    private void notMovingLabeledNetworkToDifferentNicCommonTest(boolean nicContainslabel, boolean labelShouldBeRemovedFromNic,
            boolean labelShouldBeAddedToNic,
            boolean valid) {
        VdsNetworkInterface nic = createNic("nicWithLabel");

        if (nicContainslabel) {
            nic.setLabels(Collections.singleton("lbl1"));
        }

        Network movedNetwork = createNetworkWithNameAndLabel("net", "lbl1");
        NetworkAttachment existingAttachment = createNetworkAttachment(movedNetwork, nic);
        NetworkAttachment updatedAttachment = new NetworkAttachment(existingAttachment);
        updatedAttachment.setNicId(Guid.newGuid());
        updatedAttachment.setNicName(nic.getName() + "not");

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());

        if (labelShouldBeRemovedFromNic) {
            params.getRemovedLabels().add("lbl1");
        }

        if (labelShouldBeAddedToNic) {
            NicLabel nicLabel = new NicLabel(nic.getId(), nic.getName(), "lbl1");
            params.getLabels().add(nicLabel);
        }

        HostSetupNetworksValidator validator =
                new HostSetupNetworksValidatorBuilder().setParams(params)
                        .setHost(host)
                        .setExistingInterfaces(Collections.singletonList(nic))
                        .setExistingAttachments(Collections.singletonList(existingAttachment))
                        .addNetworks(movedNetwork)
                        .build();
        if (valid) {
            assertThat(validator.notMovingLabeledNetworkToDifferentNic(updatedAttachment), isValid());
        } else {
            assertThat(validator.notMovingLabeledNetworkToDifferentNic(updatedAttachment),
                    failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC,
                            ReplacementUtils.createSetVariableString(
                                    "networkName", movedNetwork.getName()),
                            ReplacementUtils.createSetVariableString(
                                HostSetupNetworksValidator.ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC_ENTITY,
                                movedNetwork.getLabel())));
        }
    }

    @Test
    public void testValidRemovedBondsWhenNotRemovingAnyBond() throws Exception {
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setRemovedBonds(Collections.<Guid> emptySet());

        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(params, null);

        assertThat(validator.validRemovedBonds(Collections.<NetworkAttachment> emptyList()), isValid());
    }

    @Test
    public void testValidRemovedBondsWhenReferencedInterfaceIsNotBond() throws Exception {
        VdsNetworkInterface notABond = createNic("nicName");

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setRemovedBonds(Collections.singleton(notABond.getId()));

        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(params,
            Collections.singletonList(notABond));

        assertThat(validator.validRemovedBonds(Collections.<NetworkAttachment> emptyList()),
            failsWith(EngineMessage.NETWORK_INTERFACE_IS_NOT_BOND,
                ReplacementUtils.createSetVariableString(HostInterfaceValidator.VAR_NETWORK_INTERFACE_IS_NOT_BOND_ENTITY,
                    notABond.getName())));

    }

    @Test
    public void testValidRemovedBondsWhenReferencedInterfaceBondViaInexistingId() throws Exception {
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        Guid idOfInexistingInterface = Guid.newGuid();
        params.setRemovedBonds(Collections.singleton(idOfInexistingInterface));

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setHost(host)
            .setParams(params)
            .build();

        assertThat(validator.validRemovedBonds(Collections.<NetworkAttachment> emptyList()),
            failsWith(EngineMessage.NETWORK_BOND_RECORD_DOES_NOT_EXISTS,
                replaceWith(
                    HostSetupNetworksValidator.VAR_NETWORK_BOND_RECORD_DOES_NOT_EXISTS_LIST,
                    Collections.singletonList(idOfInexistingInterface))));

    }

    @Test
    public void testValidRemovedBondsWhenBondIsRequired() throws Exception {
        String nicName = "nicName";
        bond.setName(nicName);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setRemovedBonds(Collections.singleton(bond.getId()));

        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(params,
            Collections.<VdsNetworkInterface> singletonList(bond));

        NetworkAttachment requiredNetworkAttachment = new NetworkAttachment();
        requiredNetworkAttachment.setNicName(nicName);

        List<String> replacements = new ArrayList<>();
        replacements.add(ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_BOND_NAME, nicName));
        //null -- new network attachment with null id.
        replacements.addAll(replaceWith(HostSetupNetworksValidator.VAR_ATTACHMENT_IDS,
            Collections.<Guid> singletonList(null)));

        assertThat(validator.validRemovedBonds(Collections.singletonList(requiredNetworkAttachment)),
            failsWith(EngineMessage.BOND_USED_BY_NETWORK_ATTACHMENTS,
                replacements));

    }

    @Test
    public void testValidRemovedBondsWhenBondIsNotRequired() throws Exception {
        String nicName = "nicName";
        bond.setName(nicName);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setRemovedBonds(Collections.singleton(bond.getId()));

        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(params,
            Collections.<VdsNetworkInterface> singletonList(bond));

        assertThat(validator.validRemovedBonds(Collections.<NetworkAttachment> emptyList()), isValid());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAttachmentsToConfigureWhenNoChangesWereSent() throws Exception {
        Network networkA = createNetworkWithName("networkA");
        Network networkB = createNetworkWithName("networkB");

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setHost(host)
            .setParams(new HostSetupNetworksParameters(host.getId()))
            .setExistingAttachments(Arrays.asList(networkAttachmentA, networkAttachmentB))
            .build();

        Collection<NetworkAttachment> attachmentsToConfigure = validator.getAttachmentsToConfigure();
        assertThat(attachmentsToConfigure.size(), is(2));
        assertThat(attachmentsToConfigure.contains(networkAttachmentA), is(true));
        assertThat(attachmentsToConfigure.contains(networkAttachmentB), is(true));
    }

    @Test
    public void testGetAttachmentsToConfigureWhenUpdatingNetworkAttachments() throws Exception {
        Network networkA = createNetworkWithName("networkA");
        Network networkB = createNetworkWithName("networkB");

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(Arrays.asList(networkAttachmentA, networkAttachmentB));

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setHost(host)
            .setParams(params)
            .setExistingAttachments(Arrays.asList(networkAttachmentA, networkAttachmentB))
            .build();

        Collection<NetworkAttachment> attachmentsToConfigure = validator.getAttachmentsToConfigure();
        assertThat(attachmentsToConfigure.size(), is(2));
        assertThat(attachmentsToConfigure.contains(networkAttachmentA), is(true));
        assertThat(attachmentsToConfigure.contains(networkAttachmentB), is(true));
    }

    @Test
    public void testGetAttachmentsToConfigureWhenRemovingNetworkAttachments() throws Exception {
        Network networkA = createNetworkWithName("networkA");
        Network networkB = createNetworkWithName("networkB");

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(Collections.singletonList(networkAttachmentB));
        params.setRemovedNetworkAttachments(Collections.singleton(networkAttachmentA.getId()));
        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(params,
            Collections.<VdsNetworkInterface> emptyList(),
            Arrays.asList(networkAttachmentA, networkAttachmentB),
            null);

        Collection<NetworkAttachment> attachmentsToConfigure = validator.getAttachmentsToConfigure();
        assertThat(attachmentsToConfigure.size(), is(1));
        assertThat(attachmentsToConfigure.contains(networkAttachmentA), is(false));
        assertThat(attachmentsToConfigure.contains(networkAttachmentB), is(true));
    }

    @Test
    public void testGetAttachmentsToConfigureWhenAddingNewNetworkAttachments() throws Exception {
        Network networkA = createNetworkWithName("networkA");
        Network networkB = createNetworkWithName("networkB");

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA, (Guid) null);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB, (Guid)null);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(Arrays.asList(networkAttachmentA, networkAttachmentB));
        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(params,
            Collections.<VdsNetworkInterface> emptyList(),
            Collections.<NetworkAttachment> emptyList(),
            null);

        Collection<NetworkAttachment> attachmentsToConfigure = validator.getAttachmentsToConfigure();
        assertThat(attachmentsToConfigure.size(), is(2));
        assertThat(attachmentsToConfigure.contains(networkAttachmentA), is(true));
        assertThat(attachmentsToConfigure.contains(networkAttachmentB), is(true));
    }

    private NetworkAttachment createNetworkAttachment(Network networkA, VdsNetworkInterface nic) {
        NetworkAttachment attachment = createNetworkAttachment(networkA, Guid.newGuid());
        attachment.setNicId(nic.getId());
        attachment.setNicName(nic.getName());
        return attachment;
    }

    private NetworkAttachment createNetworkAttachment(Network network) {
        return createNetworkAttachment(network, Guid.newGuid());
    }

    private NetworkAttachment createNetworkAttachment(Network network, Guid id) {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setId(id);
        networkAttachment.setNetworkId(network.getId());
        networkAttachment.setNetworkName(network.getName());
        return networkAttachment;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testValidateNotRemovingUsedNetworkByVmsWhenUsedByVms() throws Exception {
        String nameOfNetworkA = "networkA";
        String nameOfNetworkB = "networkB";
        Network networkA = createNetworkWithName(nameOfNetworkA);
        Network networkB = createNetworkWithName(nameOfNetworkB);

        VdsNetworkInterface nicA = createNic("nicA");
        VdsNetworkInterface nicB = createNic("nicB");

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);
        networkAttachmentA.setNicId(nicA.getId());
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);
        networkAttachmentB.setNicId(nicB.getId());

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setRemovedNetworkAttachments(new HashSet<>(Arrays.asList(networkAttachmentA.getId(),
            networkAttachmentB.getId())));

        HostSetupNetworksValidator validator = spy(createHostSetupNetworksValidator(params,
            Arrays.asList(nicA, nicB),
            Arrays.asList(networkAttachmentA, networkAttachmentB),
            Arrays.asList(networkA, networkB)));

        VmInterfaceManager vmInterfaceManagerMock = mock(VmInterfaceManager.class);
        doReturn(vmInterfaceManagerMock).when(validator).getVmInterfaceManager();

        List<String> vmNames = Arrays.asList("vmName1", "vmName2");
        when(vmInterfaceManagerMock.findActiveVmsUsingNetworks(any(Guid.class), any(Collection.class)))
            .thenReturn(vmNames);

        final List<String> errorNetworkNames = Arrays.asList(nameOfNetworkA, nameOfNetworkB);
        assertThat(validator.validateNotRemovingUsedNetworkByVms(),
                failsWith(EngineMessage.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS,
                        concat(replaceWith(VAR_NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS_LIST, vmNames),
                               replaceWith(VAR_NETWORK_NAMES, errorNetworkNames))));


        ArgumentCaptor<Collection> collectionArgumentCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(vmInterfaceManagerMock).findActiveVmsUsingNetworks(eq(host.getId()), collectionArgumentCaptor.capture());
        assertThat(collectionArgumentCaptor.getValue().size(), is(2));
        assertThat(collectionArgumentCaptor.getValue().contains(nameOfNetworkA), is(true));
        assertThat(collectionArgumentCaptor.getValue().contains(nameOfNetworkB), is(true));
    }

    public VdsNetworkInterface createNic(String nicName) {
        VdsNetworkInterface existingNic = new VdsNetworkInterface();
        existingNic.setId(Guid.newGuid());
        existingNic.setName(nicName);
        return existingNic;
    }

    public VdsNetworkInterface createVlanNic(VdsNetworkInterface baseNic, String nicName, Integer vlanId) {
        VdsNetworkInterface existingNic = new VdsNetworkInterface();
        existingNic.setId(Guid.newGuid());
        existingNic.setName(nicName);
        existingNic.setVlanId(vlanId);
        existingNic.setBaseInterface(baseNic.getName());
        return existingNic;
    }

    private Network createNetworkWithName(String nameOfNetworkA) {
        Network networkA = new Network();
        networkA.setName(nameOfNetworkA);
        networkA.setId(Guid.newGuid());
        return networkA;
    }

    private Network createNetworkWithNameAndLabel(String name, String label) {
        Network network = createNetworkWithName(name);
        network.setLabel(label);
        return network;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testValidateNotRemovingUsedNetworkByVmsWhenNotUsedByVms() throws Exception {
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());

        HostSetupNetworksValidator validator = spy(createHostSetupNetworksValidator(params,
            Collections.<VdsNetworkInterface> emptyList(),
            null,
            Collections.<Network> emptyList()));

        VmInterfaceManager vmInterfaceManagerMock = mock(VmInterfaceManager.class);
        doReturn(vmInterfaceManagerMock).when(validator).getVmInterfaceManager();

        when(vmInterfaceManagerMock.findActiveVmsUsingNetworks(any(Guid.class), any(Collection.class)))
            .thenReturn(Collections.<String> emptyList());

        assertThat(validator.validateNotRemovingUsedNetworkByVms(), isValid());
    }

    @Test
    public void testNetworksUniquelyConfiguredOnHostWhenUniquelyConfigured() throws Exception {
        Network networkA = new Network();
        networkA.setId(Guid.newGuid());

        Network networkB = new Network();
        networkB.setId(Guid.newGuid());

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);

        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(Arrays.asList(networkA, networkB));

        assertThat(validator.networksUniquelyConfiguredOnHost(Arrays.asList(networkAttachmentA, networkAttachmentB)),
            isValid());
    }

    @Test
    public void testNetworksUniquelyConfiguredOnHostWhenNotUniquelyConfigured() throws Exception {
        Guid id = Guid.newGuid();
        String networkName = "networkName";

        Network networkA = new Network();
        networkA.setName(networkName);
        networkA.setId(id);

        NetworkAttachment networkAttachment = createNetworkAttachment(networkA);
        NetworkAttachment networkAttachmentReferencingSameNetwork = createNetworkAttachment(networkA);

        HostSetupNetworksValidator validator = createHostSetupNetworksValidator(Collections.singletonList(networkA));

        assertThat(validator.networksUniquelyConfiguredOnHost(Arrays.asList(networkAttachment,
                networkAttachmentReferencingSameNetwork)),
            failsWith(EngineMessage.NETWORKS_ALREADY_ATTACHED_TO_IFACES,
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORKS_ALREADY_ATTACHED_TO_IFACES_LIST,
                    networkName)));

    }

    @Test
    public void testValidModifiedBondsFailsWhenBondIsUnnamed() throws Exception {
        doTestValidModifiedBonds(new Bond(),
            new ValidationResult(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST),
            ValidationResult.VALID,
            new ValidationResult(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST),
            ValidationResult.VALID);
    }

    @Test
    public void testValidModifiedBondsFailsWhenReferencingExistingNonBondInterface() throws Exception {
        Bond bond = createBond();
        ValidationResult notABondValidationResult = new ValidationResult(EngineMessage.NETWORK_INTERFACE_IS_NOT_BOND,
            HostInterfaceValidator.VAR_NETWORK_INTERFACE_IS_NOT_BOND_ENTITY,
            bond.getName());

        doTestValidModifiedBonds(bond,
            ValidationResult.VALID,
            notABondValidationResult,
            notABondValidationResult,

            ValidationResult.VALID);
    }

    @Test
    public void testValidModifiedBondsFailsWhenInsufficientNumberOfSlaves() throws Exception {
        Bond bond = createBond();
        doTestValidModifiedBonds(bond,
            ValidationResult.VALID,
            ValidationResult.VALID,
            new ValidationResult(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT,
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_BONDS_INVALID_SLAVE_COUNT_LIST,
                    bond.getName())),

            ValidationResult.VALID);
    }

    @Test
    public void testValidModifiedBondsFailsWhenSlavesValidationFails() throws Exception {
        ValidationResult slavesValidationResult = new ValidationResult(EngineMessage.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE,
            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE_ENTITY,
                "slaveA"),
            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME,
                "networkName"));

        Bond bond = createBond();
        bond.setSlaves(Arrays.asList("slaveA", "slaveB"));
        doTestValidModifiedBonds(bond,
            ValidationResult.VALID,
            ValidationResult.VALID,
            /*this mocks validateModifiedBondSlaves to just verify, that caller method will behave ok, when
            validateModifiedBondSlaves return invalid result*/
            slavesValidationResult,
            slavesValidationResult);

    }

    @Test
    public void testValidModifiedBondsWhenAllOk() throws Exception {
        Bond bond = new Bond("bond1");
        bond.setSlaves(Arrays.asList("slaveA", "slaveB"));
        doTestValidModifiedBonds(bond,
            ValidationResult.VALID,
            ValidationResult.VALID,
            ValidationResult.VALID,
            ValidationResult.VALID);
    }

    private void doTestValidModifiedBonds(Bond bond,
        ValidationResult interfaceByNameExistValidationResult,
        ValidationResult interfaceIsBondValidationResult,
        ValidationResult expectedValidationResult,
        ValidationResult slavesValidationValidationResult) {
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setBonds(Collections.singletonList(bond));

        HostSetupNetworksValidator validator =
            spy(createHostSetupNetworksValidator(params, null, null, null));

        HostInterfaceValidator hostInterfaceValidatorMock = mock(HostInterfaceValidator.class);
        when(hostInterfaceValidatorMock.interfaceByNameExists()).thenReturn(interfaceByNameExistValidationResult);
        when(hostInterfaceValidatorMock.interfaceIsBondOrNull()).thenReturn(interfaceIsBondValidationResult);

        doReturn(hostInterfaceValidatorMock).when(validator).createHostInterfaceValidator(any(VdsNetworkInterface.class));
        doReturn(slavesValidationValidationResult).when(validator).validateModifiedBondSlaves(any(Bond.class));

        if (expectedValidationResult.isValid()) {
            assertThat(validator.validNewOrModifiedBonds(), isValid());
        } else {
            assertThat(validator.validNewOrModifiedBonds(),
                failsWith(expectedValidationResult.getMessage(), expectedValidationResult.getVariableReplacements()));
        }

        verify(hostInterfaceValidatorMock).interfaceByNameExists();

        //assert only if previous call was successful, otherwise this method was not called.
        if (interfaceByNameExistValidationResult.isValid()) {
            verify(hostInterfaceValidatorMock).interfaceIsBondOrNull();
        }
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveInterfaceDoesNotExist() throws Exception {
        Bond bond = createBond();
        bond.setSlaves(Arrays.asList("slaveA", "slaveB"));

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setBonds(Collections.singletonList(bond));

        doTestValidateModifiedBondSlaves(
            params,
            null,
            Collections.<NetworkAttachment> emptyList(),
            Collections.<Network> emptyList(),
            new ValidationResult(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST),
            ValidationResult.VALID,
            new ValidationResult(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST));
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveIsNotValid() throws Exception {
        Bond bond = createBond();
        bond.setSlaves(Arrays.asList("slaveA", "slaveB"));

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setBonds(Collections.singletonList(bond));

        ValidationResult cannotBeSlaveValidationResult = new ValidationResult(EngineMessage.NETWORK_INTERFACE_BOND_OR_VLAN_CANNOT_BE_SLAVE,
            HostInterfaceValidator.VAR_INTERFACE_NAME,
            bond.getName());
        doTestValidateModifiedBondSlaves(
            params,
            null,
            Collections.<NetworkAttachment> emptyList(),
            Collections.<Network> emptyList(),
            ValidationResult.VALID,
            cannotBeSlaveValidationResult,
            cannotBeSlaveValidationResult);

    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveAlreadySlavesForDifferentBond() throws Exception {
        Bond bond = createBond("bond1");
        Bond differentBond = createBond("bond2");

        VdsNetworkInterface slaveA = createBondSlave(bond, "slaveA");
        VdsNetworkInterface slaveB = createBondSlave(differentBond, "slaveB");

        bond.setSlaves(Arrays.asList(slaveA.getName(), slaveB.getName()));

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setBonds(Collections.singletonList(bond));

        doTestValidateModifiedBondSlaves(
            params,
            Arrays.asList(bond, differentBond, slaveA, slaveB),
            Collections.<NetworkAttachment> emptyList(),
            Collections.<Network> emptyList(),
            ValidationResult.VALID,
            ValidationResult.VALID,
            new ValidationResult(EngineMessage.NETWORK_INTERFACE_ALREADY_IN_BOND,
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_INTERFACE_ALREADY_IN_BOND_ENTITY,
                    slaveB.getName())));

    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveAlreadySlavesForDifferentBondWhichGetsRemoved() throws Exception {
        Bond bond = createBond("bondName");
        Bond differentBond = createBond("differentBond");

        VdsNetworkInterface slaveA = createBondSlave(bond, "slaveA");
        VdsNetworkInterface slaveB = createBondSlave(differentBond, "slaveB");

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setRemovedBonds(Collections.singleton(differentBond.getId()));

        bond.setSlaves(Arrays.asList(slaveA.getName(), slaveB.getName()));
        doTestValidateModifiedBondSlaves(
            params,
            Arrays.asList(bond, differentBond, slaveA, slaveB),
            Collections.<NetworkAttachment> emptyList(),
            Collections.<Network> emptyList(),
            ValidationResult.VALID,
            ValidationResult.VALID,
            ValidationResult.VALID);
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveAlreadySlavesForDifferentBondButItsDetachedFromItAsAPartOfRequest() throws Exception {
        Bond bond = createBond("bond1");
        Bond differentBond = createBond("bond2");

        VdsNetworkInterface slaveA = createBondSlave(bond, "slaveA");
        VdsNetworkInterface slaveB = createBondSlave(differentBond, "slaveB");
        VdsNetworkInterface slaveC = createBondSlave(differentBond, "slaveC");
        VdsNetworkInterface slaveD = createBondSlave(differentBond, "slaveD");

        bond.setSlaves(Arrays.asList(slaveA.getName(), slaveB.getName()));
        differentBond.setSlaves(Arrays.asList(slaveC.getName(), slaveD.getName()));

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setBonds(Arrays.asList(bond, differentBond));

        doTestValidateModifiedBondSlaves(
            params,
            Arrays.asList(bond, differentBond, slaveA, slaveB, slaveC, slaveD),
            Collections.<NetworkAttachment> emptyList(),
            Collections.<Network> emptyList(),
            ValidationResult.VALID,
            ValidationResult.VALID,
            ValidationResult.VALID);
    }

    public Bond createBond(String bondName) {
        Bond bond = new Bond();
        bond.setName(bondName);
        bond.setId(Guid.newGuid());
        return bond;
    }

    private Bond createBond() {
        return createBond("bond1");
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveHasNetworkAssignedWhichIsNotRemovedAsAPartOfRequest() throws Exception {
        Bond bond = createBond();

        Network networkBeingRemoved = new Network();
        networkBeingRemoved.setName("assignedNetwork");

        VdsNetworkInterface slaveA = createBondSlave(bond, "slaveA");
        slaveA.setNetworkName("assignedNetwork");

        NetworkAttachment attachmentOfNetworkToSlaveA = createNetworkAttachment(networkBeingRemoved, slaveA);

        VdsNetworkInterface slaveB = createBondSlave(bond, "slaveB");

        bond.setSlaves(Arrays.asList(slaveA.getName(), slaveB.getName()));

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setBonds(Collections.singletonList(bond));

        doTestValidateModifiedBondSlaves(
            params,
            Arrays.asList(bond, slaveA, slaveB),
            Collections.singletonList(attachmentOfNetworkToSlaveA),
            Collections.singletonList(networkBeingRemoved),
            ValidationResult.VALID,
            ValidationResult.VALID,
            new ValidationResult(EngineMessage.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE,
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE_ENTITY,
                    slaveA.getName()),
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME,
                    networkBeingRemoved.getName())));

    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveHasNetworkAssignedWhichIsRemovedAsAPartOfRequest() throws Exception {
        Bond bond = createBond();

        Network networkBeingRemoved = new Network();
        networkBeingRemoved.setName("assignedNetwork");

        VdsNetworkInterface slaveA = createBondSlave(bond, "slaveA");
        slaveA.setNetworkName(networkBeingRemoved.getName());
        VdsNetworkInterface slaveB = createBondSlave(bond, "slaveB");

        NetworkAttachment removedNetworkAttachment = new NetworkAttachment();
        removedNetworkAttachment.setId(Guid.newGuid());
        removedNetworkAttachment.setNicName(slaveA.getName());

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setRemovedNetworkAttachments(Collections.singleton(removedNetworkAttachment.getId()));

        bond.setSlaves(Arrays.asList(slaveA.getName(), slaveB.getName()));
        doTestValidateModifiedBondSlaves(
            params,
            Arrays.asList(bond, slaveA, slaveB),
            Collections.singletonList(removedNetworkAttachment),
            Collections.singletonList(networkBeingRemoved),
            ValidationResult.VALID,
            ValidationResult.VALID,
            ValidationResult.VALID);
    }

    private void doTestValidateModifiedBondSlaves(HostSetupNetworksParameters params,
        List<VdsNetworkInterface> existingInterfaces,
        List<NetworkAttachment> existingAttachments,
        Collection<Network> networks,
        ValidationResult interfaceExistValidationResult,
        ValidationResult interfaceIsValidSlaveValidationResult,
        ValidationResult expectedValidationResult) {

        HostSetupNetworksValidator validator = spy(createHostSetupNetworksValidator(params,
            existingInterfaces,
            existingAttachments,
            networks));

        HostInterfaceValidator hostInterfaceValidatorMock = mock(HostInterfaceValidator.class);
        when(hostInterfaceValidatorMock.interfaceExists()).thenReturn(interfaceExistValidationResult);
        when(hostInterfaceValidatorMock.interfaceByNameExists()).thenReturn(interfaceExistValidationResult);
        when(hostInterfaceValidatorMock.interfaceIsValidSlave()).thenReturn(interfaceIsValidSlaveValidationResult);
        when(hostInterfaceValidatorMock.interfaceIsBondOrNull()).thenReturn(ValidationResult.VALID);        //TODO MM: test for this.

        doReturn(hostInterfaceValidatorMock).when(validator).createHostInterfaceValidator(any(VdsNetworkInterface.class));

        if (expectedValidationResult.isValid()) {
            assertThat(validator.validNewOrModifiedBonds(), isValid());
        } else {
            assertThat(validator.validNewOrModifiedBonds(),
                failsWith(expectedValidationResult.getMessage(), expectedValidationResult.getVariableReplacements()));
        }
    }


    @Test
    public void testValidateCustomPropertiesWhenAttachmentDoesNotHaveCustomProperties() throws Exception {
        Network networkA = createNetworkWithName("networkA");
        Network networkB = createNetworkWithName("networkB");

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);
        networkAttachmentA.setProperties(null);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);
        networkAttachmentB.setProperties(new HashMap<String, String>());

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(Arrays.asList(networkAttachmentA, networkAttachmentB));

        HostSetupNetworksValidator validator =
            createHostSetupNetworksValidator(Arrays.asList(networkA, networkB), params);

        assertThat(validator.validateCustomProperties(SimpleCustomPropertiesUtil.getInstance(),
                Collections.<String, String> emptyMap(),
                Collections.<String, String> emptyMap()),
            isValid());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testValidateCustomPropertiesWhenCustomPropertiesFeatureIsNotSupported() throws Exception {
        Network networkA = createNetworkWithName("networkA");

        NetworkAttachment networkAttachment = createNetworkAttachment(networkA);

        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("a", "b");
        networkAttachment.setProperties(customProperties);

        VDS host = new VDS();
        host.setVdsGroupCompatibilityVersion(Version.v3_4);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(Collections.singletonList(networkAttachment));

        HostSetupNetworksValidator validator =
            spy(new HostSetupNetworksValidatorBuilder()
                .setHost(host)
                .setParams(params)
                .addNetworks(networkA)
                .build());

        assertThat(validator.validateCustomProperties(null,
                Collections.<String, String> emptyMap(),
                Collections.<String, String> emptyMap()),
            failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED,
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED_LIST,
                    networkA.getName())));

    }

    @Test
    public void testValidateCustomPropertiesWhenCustomPropertyValidationFailed() throws Exception {
        Network networkA = createNetworkWithName("networkA");

        NetworkAttachment networkAttachment = createNetworkAttachment(networkA);

        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("a", "b");
        networkAttachment.setProperties(customProperties);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(Collections.singletonList(networkAttachment));

        HostSetupNetworksValidator validator =
            spy(new HostSetupNetworksValidatorBuilder()
                .setHost(host)
                .setParams(params)
                .addNetworks(networkA)
                .build());

        //this was added just because of DI issues with 'Backend.getInstance().getErrorsTranslator()' is 'spyed' method
        //noinspection unchecked
        doReturn(Collections.emptyList()).when(validator).translateErrorMessages(any(List.class));

        assertThat(validator.validateCustomProperties(SimpleCustomPropertiesUtil.getInstance(),
                Collections.<String, String> emptyMap(),
                Collections.<String, String> emptyMap()),
            failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT,
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT_LIST,
                    networkA.getName())));

    }

    @Test
    public void testValidateCustomProperties() throws Exception {
        Network networkA = createNetworkWithName("networkA");

        NetworkAttachment networkAttachment = createNetworkAttachment(networkA);

        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("a", "b");
        networkAttachment.setProperties(customProperties);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(Collections.singletonList(networkAttachment));

        HostSetupNetworksValidator validator =
            new HostSetupNetworksValidatorBuilder()
                .setHost(host)
                .setParams(params)
                .addNetworks(networkA)
                .build();

        //we do not test SimpleCustomPropertiesUtil here, we just state what happens if it does not find ValidationError
        SimpleCustomPropertiesUtil simpleCustomPropertiesUtilMock = mock(SimpleCustomPropertiesUtil.class);
        when(simpleCustomPropertiesUtilMock.validateProperties(any(Map.class), any(Map.class)))
            .thenReturn(Collections.<ValidationError> emptyList());

        assertThat(validator.validateCustomProperties(simpleCustomPropertiesUtilMock,
                Collections.<String, String> emptyMap(),
                Collections.<String, String> emptyMap()),
            isValid());
    }

    //TODO MM: same test for vlan.
    @Test
    public void testAddNetworkToNicAlongWithAddingItIntoBond() {
        Network networkA = createNetworkWithName("networkA");

        VdsNetworkInterface nicA = createNic("nicA");
        VdsNetworkInterface nicB = createNic("nicB");

        NetworkAttachment networkAttachment = createNetworkAttachment(networkA, (Guid)null);
        networkAttachment.setNicId(nicA.getId());
        networkAttachment.setNicName(nicA.getName());
        networkAttachment.setNetworkId(networkA.getId());
        networkAttachment.setNetworkName(networkA.getName());

        Bond bond = new Bond();
        bond.setName("bond1");
        bond.setSlaves(Arrays.asList(nicA.getName(), nicB.getName()));

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(Collections.singletonList(networkAttachment));
        params.setBonds(Collections.singletonList(bond));

        when(networkDaoMock.get(eq(networkA.getId()))).thenReturn(networkA);
        when(networkClusterDaoMock.get(new NetworkClusterId(host.getVdsGroupId(), networkA.getId())))
            .thenReturn(mock(NetworkCluster.class));

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setExistingAttachments(Collections.<NetworkAttachment> emptyList())
            .setParams(params)
            .setExistingInterfaces(Arrays.asList(nicA, nicB))
            .addNetworks(networkA)
            .setHost(host)
            .build();

        ValidationResult validate = validator.validate();

        assertThat(validate, CoreMatchers.not(isValid()));

        assertThat(validate,
            failsWith(EngineMessage.NETWORK_INTERFACE_ADDED_TO_BOND_AND_NETWORK_IS_ATTACHED_TO_IT_AT_THE_SAME_TIME,
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.NETWORK_INTERFACE_ADDED_TO_BOND_AND_NETWORK_IS_ATTACHED_TO_IT_AT_THE_SAME_TIME_ENTITY,
                    nicA.getName()),
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME,
                    networkA.getName())));
    }

    @Test
    public void validateSlaveHasNoLabelsHasNoOldNorNewLabelsValid() {
        VdsNetworkInterface slave = createNic("slave");
        HostSetupNetworksValidator validator =
                new HostSetupNetworksValidatorBuilder().setParams(new HostSetupNetworksParameters(host.getId()))
                        .setExistingInterfaces(Collections.singletonList(slave)).setHost(host).build();
        assertThat(validator.validateSlaveHasNoLabels(slave.getName()), isValid());
    }

    @Test
    public void validateSlaveHasNoLabelsOldLabelWasRemovedValid() {
        VdsNetworkInterface slave = createNic("slave");
        slave.setLabels(Collections.singleton("lbl1"));

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.getRemovedLabels().add("lbl1");

        HostSetupNetworksValidator validator =
                new HostSetupNetworksValidatorBuilder().setParams(params)
                        .setExistingInterfaces(Collections.singletonList(slave)).setHost(host).build();
        assertThat(validator.validateSlaveHasNoLabels(slave.getName()), isValid());
    }

    @Test
    public void validateSlaveHasNoLabelsOldLabelWasMovedToAnotherNicValid() {
        VdsNetworkInterface slave = createNic("slave");
        slave.setLabels(Collections.singleton("lbl1"));

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        NicLabel nicLabel = new NicLabel(Guid.newGuid(), slave.getName() + "not", "lbl1");
        params.getLabels().add(nicLabel);

        HostSetupNetworksValidator validator =
                new HostSetupNetworksValidatorBuilder().setParams(params)
                        .setExistingInterfaces(Collections.singletonList(slave)).setHost(host).build();
        assertThat(validator.validateSlaveHasNoLabels(slave.getName()), isValid());
    }

    @Test
    public void validateSlaveHasNoLabelsHasOldLabel() {
        VdsNetworkInterface slave = createNic("slave");
        slave.setLabels(Collections.singleton("lbl1"));

        HostSetupNetworksValidator validator =
                new HostSetupNetworksValidatorBuilder().setParams(new HostSetupNetworksParameters(host.getId()))
                        .setExistingInterfaces(Collections.singletonList(slave)).setHost(host).build();
        assertValidateSlaveHasNoLabelsFailed(validator, slave.getName());
    }

    @Test
    public void validateSlaveHasNoLabelsHasNewLabel() {
        VdsNetworkInterface slave = createNic("slave");

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        NicLabel nicLabel = new NicLabel(slave.getId(), slave.getName(), "lbl1");
        params.getLabels().add(nicLabel);

        HostSetupNetworksValidator validator =
                new HostSetupNetworksValidatorBuilder().setParams(params)
                        .setExistingInterfaces(Collections.singletonList(slave)).setHost(host).build();
        assertValidateSlaveHasNoLabelsFailed(validator, slave.getName());
    }

    private void assertValidateSlaveHasNoLabelsFailed(HostSetupNetworksValidator validator, String slaveName) {
        assertThat(validator.validateSlaveHasNoLabels(slaveName),
            failsWith(EngineMessage.LABEL_ATTACH_TO_IMPROPER_INTERFACE,
                ReplacementUtils.createSetVariableString(
                    "LABEL_ATTACH_TO_IMPROPER_INTERFACE_ENTITY",
                    slaveName)));
    }

    @Test
    public void modifiedAttachmentNotRemovedAttachmentModifiedAndRemoved() {
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        NetworkAttachment modifiedAttachment = createNetworkAttachment(new Network());
        params.getRemovedNetworkAttachments().add(modifiedAttachment.getId());

        HostSetupNetworksValidator validator =
                new HostSetupNetworksValidatorBuilder().setParams(params).setHost(host).build();

        assertThat(validator.modifiedAttachmentNotRemoved(modifiedAttachment),
            failsWith(EngineMessage.NETWORK_ATTACHMENT_IN_BOTH_LISTS,
                ReplacementUtils.createSetVariableString("NETWORK_ATTACHMENT_IN_BOTH_LISTS_ENTITY",
                    modifiedAttachment.getId().toString())));
    }

    @Test
    public void modifiedAttachmentNotRemovedAttachmentModifiedButNotRemovedValid() {
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());

        HostSetupNetworksValidator validator =
                new HostSetupNetworksValidatorBuilder().setParams(params).setHost(host).build();

        NetworkAttachment modifiedAttachment = createNetworkAttachment(new Network());
        assertThat(validator.modifiedAttachmentNotRemoved(modifiedAttachment), isValid());
    }

    @Test
    public void attachmentAndNicLabelReferenceSameLabelNotConflict() {
        final boolean referenceSameNic = true;
        attachmentAndNicLabelReferenceSameLabelCommonTest(referenceSameNic, true);
    }

    @Test
    public void attachmentAndNicLabelReferenceSameLabelConflict() {
        boolean referenceSameNic = false;
        attachmentAndNicLabelReferenceSameLabelCommonTest(referenceSameNic, false);
    }

    @Test
    public void testValidateQosOverriddenInterfacesWhenNoAttachmentsPassed() {
        List<NetworkAttachment> networkAttachments = Collections.emptyList();
        HostSetupNetworksValidator validator = createHostSetupNetworkValidator(networkAttachments);

        assertThat(validator.validateQosOverriddenInterfaces(), ValidationResultMatchers.isValid());
    }

    @Test
    public void testValidateQosOverriddenInterfacesWhenAttachmentDoesNotHaveQosOverridden() {
        NetworkAttachment networkAttachment = new NetworkAttachment();

        List<NetworkAttachment> networkAttachments = Collections.singletonList(networkAttachment);
        HostSetupNetworksValidator validator = createHostSetupNetworkValidator(networkAttachments);

        assertThat(validator.validateQosOverriddenInterfaces(), ValidationResultMatchers.isValid());
    }

    @Test
    public void testValidateQosOverriddenInterfacesWhenHostNetworkQosIsNotSupported() {
        Network network = createNetworkWithName("network");

        HostSetupNetworksValidator validator = createValidatorForTestingValidateQosOverridden(network);


        assertThat(validator.validateQosOverriddenInterfaces(),
            ValidationResultMatchers.failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED,
                ReplacementUtils.createSetVariableString(
                    HostSetupNetworksValidator.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED_LIST,
                    network.getName())));
    }

    @Test
    public void testValidateQosOverriddenInterfacesWhenAttachmentHasQosOverriddenAndRequiredValuesNotPresent() {
        EngineMessage hostNetworkQosValidatorFailure =
            EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_SETUP_NETWORKS_MISSING_VALUES;

        host.setVdsGroupCompatibilityVersion(Version.v3_6);
        Network network = createNetworkWithName("network");
        HostSetupNetworksValidator validator = createValidatorForTestingValidateQosOverridden(network);

        HostSetupNetworksValidator validatorSpy = spy(validator);
        HostNetworkQosValidator hostNetworkQosValidatorMock = mock(HostNetworkQosValidator.class);

        when(hostNetworkQosValidatorMock.requiredQosValuesPresentForOverriding(eq(network.getName()))).
            thenReturn(new ValidationResult(hostNetworkQosValidatorFailure));

        doReturn(hostNetworkQosValidatorMock).when(validatorSpy)
            .createHostNetworkQosValidator(any(HostNetworkQos.class));


        assertThat(validatorSpy.validateQosOverriddenInterfaces(),
            ValidationResultMatchers.failsWith(hostNetworkQosValidatorFailure));
        verify(hostNetworkQosValidatorMock).requiredQosValuesPresentForOverriding(eq(network.getName()));
        verifyNoMoreInteractions(hostNetworkQosValidatorMock);

    }

    @Test
    public void testValidateQosOverriddenInterfacesWhenAttachmentHasQosOverriddenAndRequiredValuesPresentButInconsistent() {
        EngineMessage hostNetworkQosValidatorFailure =
            EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_INCONSISTENT_VALUES;

        host.setVdsGroupCompatibilityVersion(Version.v3_6);
        Network network = createNetworkWithName("network");
        HostSetupNetworksValidator validator = createValidatorForTestingValidateQosOverridden(network);

        HostSetupNetworksValidator validatorSpy = spy(validator);
        HostNetworkQosValidator hostNetworkQosValidatorMock = mock(HostNetworkQosValidator.class);


        when(hostNetworkQosValidatorMock.requiredQosValuesPresentForOverriding(eq(network.getName()))).
            thenReturn(ValidationResult.VALID);

        when(hostNetworkQosValidatorMock.valuesConsistent(eq(network.getName()))).
            thenReturn(new ValidationResult(hostNetworkQosValidatorFailure));

        doReturn(hostNetworkQosValidatorMock).when(validatorSpy)
            .createHostNetworkQosValidator(any(HostNetworkQos.class));


        assertThat(validatorSpy.validateQosOverriddenInterfaces(),
            ValidationResultMatchers.failsWith(hostNetworkQosValidatorFailure));
        verify(hostNetworkQosValidatorMock).requiredQosValuesPresentForOverriding(eq(network.getName()));
        verify(hostNetworkQosValidatorMock).valuesConsistent(eq(network.getName()));
        verifyNoMoreInteractions(hostNetworkQosValidatorMock);

    }

    @Test
    public void testValidateQosNotPartiallyConfiguredWhenNotUpdatingAttachments() {
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setHost(host)
            .setParams(new HostSetupNetworksParameters(host.getId()))
            .build();

        Collection<NetworkAttachment> networkAttachments = Collections.emptyList();
        assertThat(validator.validateQosNotPartiallyConfigured(networkAttachments), isValid());
    }

    @Test
    public void testValidateQosNotPartiallyConfiguredWhenBothHasQos() {
        testValidateQosNotPartiallyConfigured(true, true, isValid());
    }

    @Test
    public void testValidateQosNotPartiallyConfiguredWhenNoneHasQos() {
        testValidateQosNotPartiallyConfigured(true, true, isValid());
    }

    @Test
    public void testValidateQosNotPartiallyConfiguredWhenOnlyOneHasQos() {
        testValidateQosNotPartiallyConfigured(true,
            false,
            failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_INTERFACES_WITHOUT_QOS));
    }

    private void testValidateQosNotPartiallyConfigured(boolean networkAttachment1HasQos,
        boolean networkAttachment2HasQos,
        Matcher<ValidationResult> matcher) {
        VdsNetworkInterface baseNic = createNic("baseNic");
        VdsNetworkInterface vlanNic1 = createVlanNic(baseNic, "vlanNic1", 10);
        VdsNetworkInterface vlanNic2 = createVlanNic(baseNic, "vlanNic2", 11);
        Network network1 = createNetworkWithName("network1");
        Network network2 = createNetworkWithName("network2");
        NetworkAttachment networkAttachment1 = createNetworkAttachment(network1, baseNic);
        NetworkAttachment networkAttachment2 = createNetworkAttachment(network2, baseNic);
        HostNetworkQos qos = createHostNetworkQos(10, 10, 10);


        if (networkAttachment1HasQos) {
            networkAttachment1.setHostNetworkQos(qos);
        }

        if (networkAttachment2HasQos) {
            networkAttachment2.setHostNetworkQos(qos);
        }

        Collection<NetworkAttachment> networkAttachments = Arrays.asList(networkAttachment1, networkAttachment2);


        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setHost(host)
            .setParams(new HostSetupNetworksParameters(host.getId()))
            .addNetworks(network1, network2)
            .setExistingInterfaces(Arrays.asList(baseNic, vlanNic1, vlanNic2))
            .build();


        assertThat(validator.validateQosNotPartiallyConfigured(networkAttachments), matcher);
    }

    private HostSetupNetworksValidator createValidatorForTestingValidateQosOverridden(Network network) {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkId(network.getId());
        networkAttachment.setHostNetworkQos(new HostNetworkQos());

        List<NetworkAttachment> networkAttachments = Collections.singletonList(networkAttachment);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(networkAttachments);

        return new HostSetupNetworksValidatorBuilder()
            .setHost(host)
            .setParams(params)
            .addNetworks(network)
            .build();
    }

    private HostNetworkQos createHostNetworkQos(int outAverageRealtime,
        int outAverageUpperlimit, int outAverageLinkshare) {
        HostNetworkQos qos = new HostNetworkQos();
        qos.setOutAverageRealtime(outAverageRealtime);
        qos.setOutAverageUpperlimit(outAverageUpperlimit);
        qos.setOutAverageLinkshare(outAverageLinkshare);
        return qos;
    }

    private void attachmentAndNicLabelReferenceSameLabelCommonTest(boolean referenceSameNic, boolean valid) {
        VdsNetworkInterface nic = createNic("nic");
        Network network = createNetworkWithNameAndLabel("net", "lbl1");
        NetworkAttachment attachment = createNetworkAttachment(network, nic);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());

        NicLabel nicLabel =
                referenceSameNic ? new NicLabel(nic.getId(), nic.getName(), "lbl1") : new NicLabel(Guid.newGuid(),
                        nic.getName() + "not",
                        "lbl1");
        params.getLabels().add(nicLabel);

        HostSetupNetworksValidator validator =
                new HostSetupNetworksValidatorBuilder().setParams(params)
                        .addNetworks(network)
                        .setExistingInterfaces(Collections.singletonList(nic))
                        .setHost(host)
                        .build();

        if (valid) {
            assertThat(validator.validateAttachmentAndNicReferenceSameLabelNotConflict(attachment), isValid());
        } else {
            assertThat(validator.validateAttachmentAndNicReferenceSameLabelNotConflict(attachment),
                    failsWith(EngineMessage.NETWORK_SHOULD_BE_ATTACHED_VIA_LABEL_TO_ANOTHER_NIC,
                            ReplacementUtils.createSetVariableString("NETWORK_SHOULD_BE_ATTACHED_VIA_LABEL_TO_ANOTHER_NIC_ENTITY",
                                    network.getName()),
                            ReplacementUtils.createSetVariableString("interfaceName", attachment.getNicName()),
                            ReplacementUtils.createSetVariableString("labeledInterfaceName", nicLabel.getNicName())));
        }
    }

    private VdsNetworkInterface createBondSlave(Bond bond, String slaveName) {
        VdsNetworkInterface slave = new VdsNetworkInterface();
        slave.setId(Guid.newGuid());
        slave.setName(slaveName);
        slave.setBondName(bond.getName());
        slave.setBonded(false);
        return slave;
    }

    private HostSetupNetworksValidator createHostSetupNetworkValidator(List<NetworkAttachment> networkAttachments) {
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(networkAttachments);

        return new HostSetupNetworksValidatorBuilder()
            .setHost(host)
            .setParams(params)
            .build();
    }

    private HostSetupNetworksValidator createHostSetupNetworksValidator(List<Network> networks) {
        return createHostSetupNetworksValidator(networks, new HostSetupNetworksParameters(host.getId()));
    }

    private HostSetupNetworksValidator createHostSetupNetworksValidator(List<Network> networks,
        HostSetupNetworksParameters params) {
        return new HostSetupNetworksValidatorBuilder()
            .setHost(host)
            .setParams(params)
            .addNetworks(networks)
            .build();
    }

    private HostSetupNetworksValidator createHostSetupNetworksValidator(HostSetupNetworksParameters params,
        List<VdsNetworkInterface> existingIfaces) {

        return new HostSetupNetworksValidatorBuilder()
            .setHost(host)
            .setParams(params)
            .setExistingInterfaces(existingIfaces)
            .build();
    }

    private HostSetupNetworksValidator createHostSetupNetworksValidator(HostSetupNetworksParameters params,
        List<VdsNetworkInterface> existingIfaces,
        List<NetworkAttachment> existingAttachments,
        Collection<Network> networks) {

        return new HostSetupNetworksValidatorBuilder()
            .setHost(host)
            .setParams(params)
            .setExistingInterfaces(existingIfaces)
            .setExistingAttachments(existingAttachments)
            .addNetworks(networks)
            .build();
    }

    public class HostSetupNetworksValidatorBuilder {
        private VDS host;
        private HostSetupNetworksParameters params;
        private List<VdsNetworkInterface> existingInterfaces = Collections.emptyList();
        private List<NetworkAttachment> existingAttachments = Collections.emptyList();
        private List<Network> networks = new ArrayList<>();

        public HostSetupNetworksValidatorBuilder setHost(VDS host) {
            this.host = host;
            return this;
        }

        public HostSetupNetworksValidatorBuilder setParams(HostSetupNetworksParameters params) {
            this.params = params;
            return this;
        }

        public HostSetupNetworksValidatorBuilder setExistingInterfaces(List<VdsNetworkInterface> existingInterfaces) {
            this.existingInterfaces = existingInterfaces;
            return this;
        }

        public HostSetupNetworksValidatorBuilder setExistingAttachments(List<NetworkAttachment> existingAttachments) {
            this.existingAttachments = existingAttachments;
            return this;
        }

        public HostSetupNetworksValidatorBuilder addNetworks(Network ... networks) {
            if (networks.length == 0) {
                return this;
            }

            addNetworks(Arrays.asList(networks));
            return this;
        }

        public HostSetupNetworksValidatorBuilder addNetworks(Collection<Network> networks) {
            if (networks == null) {
                return this;
            }

            this.networks.addAll(networks);
            return this;
        }

        public HostSetupNetworksValidator build() {
            return new HostSetupNetworksValidator(host,
                params,
                existingInterfaces,
                existingAttachments,
                new BusinessEntityMap<>(networks),
                managementNetworkUtil,
                networkClusterDaoMock,
                    networkDaoMock,
                vdsDaoMock,
                new HostSetupNetworksValidatorHelper(),
                vmDao,
                effectiveHostNetworkQos);
        }
    }
}

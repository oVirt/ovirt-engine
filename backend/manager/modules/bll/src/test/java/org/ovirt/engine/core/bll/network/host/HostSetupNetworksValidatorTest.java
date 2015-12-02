package org.ovirt.engine.core.bll.network.host;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;
import static org.ovirt.engine.core.utils.ReplacementUtils.replaceWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.validator.HostInterfaceValidator;
import org.ovirt.engine.core.bll.validator.HostNetworkQosValidator;
import org.ovirt.engine.core.bll.validator.ValidationResultMatchers;
import org.ovirt.engine.core.bll.validator.network.NetworkAttachmentIpConfigurationValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidatorResolver;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.BondMode;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
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

    private static final String TEST_VERSION = "123.456";
    private static final Set<Version> TEST_SUPPORTED_VERSIONS = Collections.singleton(new Version(TEST_VERSION));

    private VDS host;

    @Mock
    private NetworkDao networkDaoMock;

    @Mock
    private NetworkAttachmentDao networkAttachmentDaoMock;

    @Mock
    private BusinessEntityMap<Network> mockBusinessEntityMap;

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
        mockConfig(ConfigValues.NetworkCustomPropertiesSupported, Version.v3_4, false),
        mockConfig(ConfigValues.NetworkCustomPropertiesSupported, Version.v3_5, true),
        mockConfig(ConfigValues.NetworkCustomPropertiesSupported, Version.v3_6, true),
        mockConfig(ConfigValues.HostNetworkQosSupported, Version.v3_4, false),
        mockConfig(ConfigValues.HostNetworkQosSupported, Version.v3_5, false),
        mockConfig(ConfigValues.HostNetworkQosSupported, Version.v3_6, true));

    @Mock
    private ManagementNetworkUtil managementNetworkUtil;
    @Mock
    private NetworkExclusivenessValidatorResolver mockNetworkExclusivenessValidatorResolver;
    @Mock
    private NetworkExclusivenessValidator mockNetworkExclusivenessValidator;
    @Mock
    private NetworkAttachmentIpConfigurationValidator mockNetworkAttachmentIpConfigurationValidator;

    @Before
    public void setUp() throws Exception {
        host = new VDS();
        host.setId(Guid.newGuid());
        host.setVdsGroupCompatibilityVersion(Version.v3_5);
        final VdsDynamic vdsDynamic = new VdsDynamic();
        vdsDynamic.setSupportedClusterLevels(TEST_VERSION);
        host.setDynamicData(vdsDynamic);

        bond = new Bond();
        bond.setId(Guid.newGuid());

        when(mockNetworkExclusivenessValidatorResolver.resolveNetworkExclusivenessValidator(TEST_SUPPORTED_VERSIONS))
                .thenReturn(mockNetworkExclusivenessValidator);
        when(mockNetworkAttachmentIpConfigurationValidator.validateNetworkAttachmentIpConfiguration(any())).thenReturn(ValidationResult.VALID);
    }

    public void testNotRemovingLabeledNetworksReferencingUnlabeledNetworkRemovalIsOk() throws Exception {
        Network unlabeledNetwork = new Network();
        unlabeledNetwork.setId(Guid.newGuid());

        NetworkAttachment networkAttachment = createNetworkAttachment(unlabeledNetwork);

        HostSetupNetworksValidator validator =
            new HostSetupNetworksValidatorBuilder()
                .addNetworks(Collections.singletonList(unlabeledNetwork))
                .build();
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
            new HostSetupNetworksValidatorBuilder()
                .addNetworks(Collections.singletonList(labeledNetwork))
                .build();
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

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .addExistingInterfaces(Collections.singletonList(existingNic))
            .addNetworks(Collections.singletonList(labeledNetwork))
            .build();

        EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC;
        assertThat(validator.notRemovingLabeledNetworks(networkAttachment), failsWith(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, labeledNetwork.getName())));

    }

    @Test
    public void testNotRemovingLabeledNetworksLabelRemovedFromNicValid() {
        VdsNetworkInterface nicWithLabel = createNic("nicWithLabel");
        final String labelName = "lbl1";
        nicWithLabel.setLabels(Collections.singleton(labelName));

        Network network = createNetworkWithNameAndLabel("net", labelName);
        NetworkAttachment removedAttachment = createNetworkAttachment(network, nicWithLabel);

        assertTestNotRemovingLabeledNetworksValid(nicWithLabel,
            removedAttachment,
            new ParametersBuilder().addRemovedLabels(labelName).build(),
            network);
    }

    @Test
    public void testNotRemovingLabeledNetworksLabelMovedToAnotherNicValid() {
        VdsNetworkInterface nicWithLabel = createNic("nicWithLabel");
        final String labelName = "lbl1";
        nicWithLabel.setLabels(Collections.singleton(labelName));

        Network network = createNetworkWithNameAndLabel("net", labelName);
        NetworkAttachment removedAttachment = createNetworkAttachment(network, nicWithLabel);

        NicLabel nicLabel = new NicLabel(Guid.newGuid(), nicWithLabel.getName() + "not", labelName);

        assertTestNotRemovingLabeledNetworksValid(nicWithLabel,
            removedAttachment,
            new ParametersBuilder().addLabels(nicLabel).build(),
            network);
    }

    @Test
    public void testNotRemovingLabeledNetworksNicHasLabelOldAttachRemovedNewAttachWithSameNetworkAddedToNicValid() {
        VdsNetworkInterface nicWithLabel = createNic("nicWithLabel");
        nicWithLabel.setLabels(Collections.singleton("lbl1"));

        Network network = createNetworkWithNameAndLabel("net", "lbl1");
        NetworkAttachment removedAttachment = createNetworkAttachment(network, nicWithLabel);

        NetworkAttachment addedAttachment = new NetworkAttachment(removedAttachment);
        addedAttachment.setId(Guid.newGuid());

        assertTestNotRemovingLabeledNetworksValid(nicWithLabel,
            removedAttachment,
            new ParametersBuilder().addNetworkAttachments(addedAttachment).build(),
            network);
    }

    @Test
    public void testNotRemovingLabeledNetworksLabelAddedToNicOldAttachRemovedNewAttachWithSameNetworkAddedToNicValid() {
        VdsNetworkInterface nic = createNic("nicWithNoLabel");
        NicLabel nicLabel = new NicLabel(nic.getId(), nic.getName(), "lbl1");

        Network network = createNetworkWithNameAndLabel("net", nicLabel.getLabel());
        NetworkAttachment removedAttachment = createNetworkAttachment(network, nic);
        NetworkAttachment addedAttachment = new NetworkAttachment(removedAttachment);
        addedAttachment.setId(Guid.newGuid());

        assertTestNotRemovingLabeledNetworksValid(nic,
            removedAttachment,
            new ParametersBuilder()
                .addNetworkAttachments(addedAttachment)
                .addLabels(nicLabel)
                .build(),
            network);
    }

    private void assertTestNotRemovingLabeledNetworksValid(VdsNetworkInterface nic,
            NetworkAttachment removedAttachment,
            HostSetupNetworksParameters params, Network network) {

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(params)
            .addExistingInterfaces(nic)
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

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addNetworkAttachments(updatedNetworkAttachment))
            .addExistingInterfaces(Arrays.asList(existingNic, existingNic2))
            .addExistingAttachments(Collections.singletonList(existingNetworkAttachment))
            .addNetworks(Collections.singletonList(labeledNetwork))
            .build();

        EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC;
        assertThat(validator.notMovingLabeledNetworkToDifferentNic(updatedNetworkAttachment),
            failsWith(engineMessage,
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME,
                    labeledNetwork.getName()),
                ReplacementUtils.getVariableAssignmentString(engineMessage, labeledNetwork.getLabel())));
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

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedBonds(bond.getId()))
            .addExistingInterfaces(Collections.<VdsNetworkInterface>singletonList(bond))
            .addExistingAttachments((List<NetworkAttachment>) null)
            .addNetworks(Collections.singletonList(labeledNetwork))
            .build();
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

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(params)
            .addExistingInterfaces(nic)
            .addExistingAttachments(existingAttachment)
            .addNetworks(movedNetwork)
            .build();
        if (valid) {
            assertThat(validator.notMovingLabeledNetworkToDifferentNic(updatedAttachment), isValid());
        } else {
            EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC;
            assertThat(validator.notMovingLabeledNetworkToDifferentNic(updatedAttachment),

                    failsWith(engineMessage,
                            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME, movedNetwork.getName()),
                            ReplacementUtils.getVariableAssignmentString(engineMessage, movedNetwork.getLabel())));
        }
    }

    @Test
    public void testValidRemovedBondsWhenNotRemovingAnyBond() throws Exception {
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .addExistingInterfaces((List<VdsNetworkInterface>) null)
            .build();

        assertThat(validator.validRemovedBonds(Collections.<NetworkAttachment> emptyList()), isValid());
    }

    @Test
    public void testValidRemovedBondsWhenReferencedInterfaceIsNotBond() throws Exception {
        VdsNetworkInterface notABond = createNic("nicName");

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedBonds(notABond.getId()))
            .addExistingInterfaces(Collections.singletonList(notABond))
            .build();

        final EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_IS_NOT_BOND;
        assertThat(validator.validRemovedBonds(Collections.<NetworkAttachment> emptyList()),
            failsWith(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, notABond.getName())));
    }

    @Test
    public void testValidRemovedBondsWhenReferencedInterfaceBondViaInexistingId() throws Exception {
        Guid idOfInexistingInterface = Guid.newGuid();
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedBonds(idOfInexistingInterface))
            .build();

        EngineMessage engineMessage = EngineMessage.NETWORK_BOND_RECORD_DOES_NOT_EXISTS;
        assertThat(validator.validRemovedBonds(Collections.<NetworkAttachment> emptyList()),
            failsWith(engineMessage,
                ReplacementUtils.getListVariableAssignmentString(engineMessage,
                    Collections.singletonList(idOfInexistingInterface))));

    }

    @Test
    public void testValidRemovedBondsWhenBondIsRequired() throws Exception {
        String nicName = "nicName";
        bond.setName(nicName);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedBonds(bond.getId()))
            .addExistingInterfaces(Collections.<VdsNetworkInterface>singletonList(bond))
            .build();

        NetworkAttachment requiredNetworkAttachment = new NetworkAttachment();
        requiredNetworkAttachment.setNicName(nicName);

        List<String> replacements = new ArrayList<>();
        replacements.add(ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_BOND_NAME, nicName));
        //null -- new network attachment with null id.
        replacements.addAll(replaceWith(HostSetupNetworksValidator.VAR_ATTACHMENT_IDS,
            Collections.<Guid> singletonList(null)));

        assertThat(validator.validRemovedBonds(Collections.singletonList(requiredNetworkAttachment)),
            failsWith(EngineMessage.BOND_USED_BY_NETWORK_ATTACHMENTS, replacements));

    }

    @Test
    public void testValidRemovedBondsWhenBondIsNotRequired() throws Exception {
        String nicName = "nicName";
        bond.setName(nicName);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedBonds(bond.getId()))
            .addExistingInterfaces(Collections.<VdsNetworkInterface>singletonList(bond))
            .build();

        assertThat(validator.validRemovedBonds(Collections.<NetworkAttachment>emptyList()), isValid());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAttachmentsToConfigureWhenNoChangesWereSent() throws Exception {
        Network networkA = createNetworkWithName("networkA");
        Network networkB = createNetworkWithName("networkB");

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .addExistingAttachments(networkAttachmentA, networkAttachmentB)
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

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addNetworkAttachments(networkAttachmentA, networkAttachmentB))
            .addExistingAttachments(networkAttachmentA, networkAttachmentB)
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

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder()
                    .addNetworkAttachments(networkAttachmentB)
                    .addRemovedNetworkAttachments(networkAttachmentA)
                    .build())
            .addExistingAttachments(Arrays.asList(networkAttachmentA, networkAttachmentB))
            .addNetworks((Collection<Network>) null)
            .build();

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
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB, (Guid) null);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addNetworkAttachments(networkAttachmentA, networkAttachmentB))
            .build();

        Collection<NetworkAttachment> attachmentsToConfigure = validator.getAttachmentsToConfigure();
        assertThat(attachmentsToConfigure.size(), is(2));
        assertThat(attachmentsToConfigure.contains(networkAttachmentA), is(true));
        assertThat(attachmentsToConfigure.contains(networkAttachmentB), is(true));
    }

    @Test
    public void testInvalidNetworkAttachmentIpConfiguration() {
        HostSetupNetworksValidator validator = initValidator();
        NetworkAttachment networkAttachment = validator.getAttachmentsToConfigure().iterator().next();
        Collection<String> replacements = createReplacement(networkAttachment);
        EngineMessage engineMessage = EngineMessage.NETWORK_ATTACHMENT_MISSING_IP_CONFIGURATION;
        initMockNetworkAttachmentIpConfigurationValidator(engineMessage, replacements);

        assertThat(validator.validNewOrModifiedNetworkAttachments(),
                failsWith(engineMessage, replacements));
    }

    @Test
    public void testValidNetworkAttachmentIpConfiguration() {
        HostSetupNetworksValidator validator = initValidator();
        Collection<String> replacements = new ArrayList<>();
        EngineMessage engineMessage = null;
        initMockNetworkAttachmentIpConfigurationValidator(engineMessage, replacements);
        ValidationResult actual = validator.validNewOrModifiedNetworkAttachments();
        Assert.assertEquals(ValidationResult.VALID, actual);

    }

    private Collection<String> createReplacement(NetworkAttachment networkAttachment) {
        Collection<String> replacements = new ArrayList<>();
        replacements.add(ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME,
                networkAttachment.getNetworkName()));
        replacements.add(ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_INTERFACE_NAME,
                networkAttachment.getNicName()));
        return replacements;
    }

    private HostSetupNetworksValidator initValidator() {
        Network network = addNewNetworkToDaoMock();
        VdsNetworkInterface vdsNetworkInterface = createNic(HostSetupNetworksValidator.VAR_INTERFACE_NAME);
        NetworkAttachment networkAttachment = createNetworkAttachment(network, vdsNetworkInterface, null);
        when(networkAttachmentDaoMock.get(networkAttachment.getId())).thenReturn(networkAttachment);
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
                .setParams(new ParametersBuilder().addNetworkAttachments(networkAttachment))
                .addNetworks(network)
                .addExistingInterfaces(vdsNetworkInterface)
                .build();

        return validator;
    }

    private Network addNewNetworkToDaoMock() {
        Network network = createNetworkWithName(HostSetupNetworksValidator.VAR_NETWORK_NAME);
        addNetworkIdToNetworkDaoMock(network);
        addNetworkToClusterDaoMock(network.getId());
        return network;
    }

    private void addNetworkToClusterDaoMock(Guid guid) {
        when(networkClusterDaoMock.get(new NetworkClusterId(host.getVdsGroupId(), guid)))
                .thenReturn(mock(NetworkCluster.class));
    }

    private void addNetworkIdToNetworkDaoMock(Network network) {
        when(networkDaoMock.get(eq(network.getId()))).thenReturn(network);
    }

    private void initMockNetworkAttachmentIpConfigurationValidator(EngineMessage engineMessage,
            Collection<String> replacements) {
        ValidationResult validationResult =
                replacements.isEmpty() ? ValidationResult.VALID : new ValidationResult(engineMessage, replacements);
        when(mockNetworkAttachmentIpConfigurationValidator.validateNetworkAttachmentIpConfiguration(any()))
                .thenReturn(validationResult);
    }

    private NetworkAttachment createNetworkAttachment(Network networkA, VdsNetworkInterface nic, Guid guid) {
        NetworkAttachment attachment = createNetworkAttachment(networkA, guid);
        if (nic != null) {
            attachment.setNicId(nic.getId());
            attachment.setNicName(nic.getName());
        }
        return attachment;
    }

    private NetworkAttachment createNetworkAttachment(Network networkA, VdsNetworkInterface nic) {
        return createNetworkAttachment(networkA, nic, Guid.newGuid());
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
        HostSetupNetworksValidator validator = spy(new HostSetupNetworksValidatorBuilder()
            .build());

        VmInterfaceManager vmInterfaceManagerMock = mock(VmInterfaceManager.class);
        doReturn(vmInterfaceManagerMock).when(validator).getVmInterfaceManager();

        when(vmInterfaceManagerMock.findActiveVmsUsingNetworks(any(Guid.class), any(Collection.class)))
            .thenReturn(Collections.<String>emptyList());

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

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .addNetworks(Arrays.asList(networkA, networkB))
            .build();

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

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .addNetworks(Collections.singletonList(networkA))
            .build();

        assertThat(validator.networksUniquelyConfiguredOnHost(Arrays.asList(networkAttachment,
                networkAttachmentReferencingSameNetwork)),
            failsWith(EngineMessage.NETWORKS_ALREADY_ATTACHED_TO_IFACES,
                ReplacementUtils.getVariableAssignmentString(EngineMessage.NETWORKS_ALREADY_ATTACHED_TO_IFACES,
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
        final EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_IS_NOT_BOND;
        ValidationResult notABondValidationResult = new ValidationResult(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, bond.getName()));

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
                ReplacementUtils.getVariableAssignmentString(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT,
                    bond.getName())),

            ValidationResult.VALID);
    }

    @Test
    public void testValidModifiedBondsFailsWhenSlavesValidationFails() throws Exception {
        EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE;
        ValidationResult slavesValidationResult = new ValidationResult(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, "slaveA"),
            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME, "networkName"));

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

        HostSetupNetworksValidator validator =
            spy(new HostSetupNetworksValidatorBuilder()
                .setParams(new ParametersBuilder().addBonds(bond))
                .addExistingInterfaces((List<VdsNetworkInterface>) null)
                .addExistingAttachments((List<NetworkAttachment>) null)
                .addNetworks((Collection<Network>) null)
                .build());

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

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(bond))
            .build();
        doTestValidateModifiedBondSlaves(
            spy(validator), new ValidationResult(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST),
            ValidationResult.VALID,
            failsWith(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST));
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveIsNotValid() throws Exception {
        Bond bond = createBond();
        bond.setSlaves(Arrays.asList("slaveA", "slaveB"));

        ValidationResult cannotBeSlaveValidationResult = new ValidationResult(EngineMessage.NETWORK_INTERFACE_BOND_OR_VLAN_CANNOT_BE_SLAVE,
        ReplacementUtils.createSetVariableString(HostInterfaceValidator.VAR_INTERFACE_NAME, bond.getName()));

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(bond))
            .build();
        doTestValidateModifiedBondSlaves(
            spy(validator), ValidationResult.VALID,
            cannotBeSlaveValidationResult,
            failsWith(cannotBeSlaveValidationResult));

    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveAlreadySlavesForDifferentBond() throws Exception {
        Bond bond = createBond("bond1");
        Bond differentBond = createBond("bond2");

        VdsNetworkInterface slaveA = createBondSlave(bond, "slaveA");
        VdsNetworkInterface slaveB = createBondSlave(differentBond, "slaveB");

        setBondSlaves(bond, slaveA, slaveB);

        EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_ALREADY_IN_BOND;
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(bond))
            .addExistingInterfaces(bond, differentBond, slaveA, slaveB)
            .build();

        doTestValidateModifiedBondSlaves(
            spy(validator), ValidationResult.VALID,
            ValidationResult.VALID,
            failsWith(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, slaveB.getName())));
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveAlreadySlavesForDifferentBondWhichGetsRemoved() throws Exception {
        Bond bond = createBond("bondName");
        Bond differentBond = createBond("differentBond");

        VdsNetworkInterface slaveA = createBondSlave(bond, "slaveA");
        VdsNetworkInterface slaveB = createBondSlave(differentBond, "slaveB");

        setBondSlaves(bond, slaveA, slaveB);
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedBonds(differentBond.getId()))
            .addExistingInterfaces(bond, differentBond, slaveA, slaveB)
            .build();
        doTestValidateModifiedBondSlaves(
            spy(validator), ValidationResult.VALID,
            ValidationResult.VALID,
            isValid());
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveAlreadySlavesForDifferentBondButItsDetachedFromItAsAPartOfRequest() throws Exception {
        Bond bond = createBond("bond1");
        Bond differentBond = createBond("bond2");

        VdsNetworkInterface slaveA = createBondSlave(bond, "slaveA");
        VdsNetworkInterface slaveB = createBondSlave(differentBond, "slaveB");
        VdsNetworkInterface slaveC = createBondSlave(differentBond, "slaveC");
        VdsNetworkInterface slaveD = createBondSlave(differentBond, "slaveD");

        setBondSlaves(bond, slaveA, slaveB);
        setBondSlaves(differentBond, slaveC, slaveD);

        HostSetupNetworksValidator build = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(bond, differentBond))
            .addExistingInterfaces(bond, differentBond, slaveA, slaveB, slaveC, slaveD)
            .build();
        doTestValidateModifiedBondSlaves(
            spy(build), ValidationResult.VALID,
            ValidationResult.VALID,
            isValid());
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

        setBondSlaves(bond, slaveA, slaveB);

        EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE;
        final HostSetupNetworksValidator build = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(bond))
            .addExistingInterfaces(bond, slaveA, slaveB)
            .addExistingAttachments(attachmentOfNetworkToSlaveA)
            .addNetworks(networkBeingRemoved)
            .build();

        doTestValidateModifiedBondSlaves(
            spy(build), ValidationResult.VALID,
            ValidationResult.VALID,
            failsWith(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, slaveA.getName()),
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

        setBondSlaves(bond, slaveA, slaveB);
        final HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedNetworkAttachments(removedNetworkAttachment))
            .addExistingInterfaces(bond, slaveA, slaveB)
            .addExistingAttachments(removedNetworkAttachment)
            .addNetworks(networkBeingRemoved)
            .build();

        doTestValidateModifiedBondSlaves(spy(validator),
            ValidationResult.VALID,
            ValidationResult.VALID,
            isValid());
    }

    private void setBondSlaves(Bond bond, VdsNetworkInterface slaveA, VdsNetworkInterface slaveB) {
        bond.setSlaves(Arrays.asList(slaveA.getName(), slaveB.getName()));
    }

    private void doTestValidateModifiedBondSlaves(HostSetupNetworksValidator validator,
        ValidationResult interfaceExistValidationResult,
        ValidationResult interfaceIsValidSlaveValidationResult,
        Matcher<ValidationResult> matcher) {

        HostInterfaceValidator hostInterfaceValidatorMock = mock(HostInterfaceValidator.class);
        when(hostInterfaceValidatorMock.interfaceExists()).thenReturn(interfaceExistValidationResult);
        when(hostInterfaceValidatorMock.interfaceByNameExists()).thenReturn(interfaceExistValidationResult);
        when(hostInterfaceValidatorMock.interfaceIsValidSlave()).thenReturn(interfaceIsValidSlaveValidationResult);
        when(hostInterfaceValidatorMock.interfaceIsBondOrNull()).thenReturn(ValidationResult.VALID);        //TODO MM: test for this.

        doReturn(hostInterfaceValidatorMock).when(validator).createHostInterfaceValidator(any(VdsNetworkInterface.class));

        assertThat(validator.validNewOrModifiedBonds(), matcher);
    }


    @Test
    public void testValidateCustomPropertiesWhenAttachmentDoesNotHaveCustomProperties() throws Exception {
        Network networkA = createNetworkWithName("networkA");
        Network networkB = createNetworkWithName("networkB");

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);
        networkAttachmentA.setProperties(null);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);
        networkAttachmentB.setProperties(new HashMap<>());

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addNetworkAttachments(networkAttachmentA, networkAttachmentB))
            .addNetworks(Arrays.asList(networkA, networkB))
            .build();

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

        host.setVdsGroupCompatibilityVersion(Version.v3_4);

        HostSetupNetworksParameters params = new HostSetupNetworksParameters(host.getId());
        params.setNetworkAttachments(Collections.singletonList(networkAttachment));

        HostSetupNetworksValidator validator =
            spy(new HostSetupNetworksValidatorBuilder()
                .setParams(params)
                .addNetworks(networkA)
                .build());

        EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED;
        assertThat(validator.validateCustomProperties(null,
                Collections.<String, String> emptyMap(),
                Collections.<String, String> emptyMap()),
            failsWith(engineMessage,
                ReplacementUtils.getVariableAssignmentStringWithMultipleValues(engineMessage, networkA.getName())));
    }

    @Test
    public void testValidateCustomPropertiesWhenCustomPropertyValidationFailed() throws Exception {
        Network networkA = createNetworkWithName("networkA");

        NetworkAttachment networkAttachment = createNetworkAttachment(networkA);

        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("a", "b");
        networkAttachment.setProperties(customProperties);

        HostSetupNetworksValidator validator = spy(new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addNetworkAttachments(networkAttachment))
            .addNetworks(networkA)
            .build());

        //this was added just because of DI issues with 'Backend.getInstance().getErrorsTranslator()' is 'spyed' method
        //noinspection unchecked
        doReturn(Collections.emptyList()).when(validator).translateErrorMessages(any(List.class));

        EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT;
        assertThat(validator.validateCustomProperties(SimpleCustomPropertiesUtil.getInstance(),
                Collections.<String, String> emptyMap(),
                Collections.<String, String> emptyMap()),
            failsWith(engineMessage,
                ReplacementUtils.getVariableAssignmentStringWithMultipleValues(engineMessage, networkA.getName())));

    }

    @Test
    public void testValidateCustomProperties() throws Exception {
        Network networkA = createNetworkWithName("networkA");

        NetworkAttachment networkAttachment = createNetworkAttachment(networkA);

        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("a", "b");
        networkAttachment.setProperties(customProperties);

        HostSetupNetworksValidator validator =
            new HostSetupNetworksValidatorBuilder()
                .setParams(new ParametersBuilder().addNetworkAttachments(networkAttachment))
                .addNetworks(networkA)
                .build();

        //we do not test SimpleCustomPropertiesUtil here, we just state what happens if it does not find ValidationError
        SimpleCustomPropertiesUtil simpleCustomPropertiesUtilMock = mock(SimpleCustomPropertiesUtil.class);
        when(simpleCustomPropertiesUtilMock.validateProperties(any(Map.class), any(Map.class)))
            .thenReturn(Collections.<ValidationError>emptyList());

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
        setBondSlaves(bond, nicA, nicB);

        addNetworkIdToNetworkDaoMock(networkA);
        addNetworkToClusterDaoMock(networkA.getId());

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder()
                    .addNetworkAttachments(networkAttachment)
                    .addBonds(bond).build())
            .addExistingInterfaces(nicA, nicB)
            .addNetworks(networkA)
            .build();

        ValidationResult validate = validator.validate();

        assertThat(validate, not(isValid()));

        EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_ADDED_TO_BOND_AND_NETWORK_IS_ATTACHED_TO_IT_AT_THE_SAME_TIME;
        assertThat(validate,
            failsWith(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, nicA.getName()),
                ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME,
                    networkA.getName())));
    }

    @Test
    public void validateSlaveHasNoLabelsHasNoOldNorNewLabelsValid() {
        VdsNetworkInterface slave = createNic("slave");
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .addExistingInterfaces(slave)
            .build();
        assertThat(validator.validateSlaveHasNoLabels(slave.getName()), isValid());
    }

    @Test
    public void validateSlaveHasNoLabelsOldLabelWasRemovedValid() {
        final String removedLabelName = "lbl1";
        VdsNetworkInterface slave = createNic("slave");
        slave.setLabels(Collections.singleton(removedLabelName));

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedLabels(removedLabelName))
            .addExistingInterfaces(slave)
            .build();
        assertThat(validator.validateSlaveHasNoLabels(slave.getName()), isValid());
    }

    @Test
    public void validateSlaveHasNoLabelsOldLabelWasMovedToAnotherNicValid() {
        VdsNetworkInterface slave = createNic("slave");
        slave.setLabels(Collections.singleton("lbl1"));
        NicLabel nicLabel = new NicLabel(Guid.newGuid(), slave.getName() + "not", "lbl1");

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addLabels(nicLabel))
            .addExistingInterfaces(slave)
            .build();
        assertThat(validator.validateSlaveHasNoLabels(slave.getName()), isValid());
    }

    @Test
    public void validateSlaveHasNoLabelsHasOldLabel() {
        VdsNetworkInterface slave = createNic("slave");
        slave.setLabels(Collections.singleton("lbl1"));

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
                .addExistingInterfaces(slave)
                .build();
        assertValidateSlaveHasNoLabelsFailed(validator, slave.getName());
    }

    @Test
    public void validateSlaveHasNoLabelsHasNewLabel() {
        VdsNetworkInterface slave = createNic("slave");
        NicLabel nicLabel = new NicLabel(slave.getId(), slave.getName(), "lbl1");


        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
                .setParams(new ParametersBuilder().addLabels(nicLabel))
                .addExistingInterfaces(slave)
                .build();
        assertValidateSlaveHasNoLabelsFailed(validator, slave.getName());
    }

    private void assertValidateSlaveHasNoLabelsFailed(HostSetupNetworksValidator validator, String slaveName) {
        final EngineMessage engineMessage = EngineMessage.LABEL_ATTACH_TO_IMPROPER_INTERFACE;
        assertThat(validator.validateSlaveHasNoLabels(slaveName),
            failsWith(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, slaveName)));
    }

    @Test
    public void modifiedAttachmentNotRemovedAttachmentModifiedAndRemoved() {
        NetworkAttachment modifiedAttachment = createNetworkAttachment(new Network());

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedNetworkAttachments(modifiedAttachment))
            .build();

        assertThat(validator.modifiedAttachmentNotRemoved(modifiedAttachment),
            failsWith(EngineMessage.NETWORK_ATTACHMENT_IN_BOTH_LISTS,
                ReplacementUtils.createSetVariableString("NETWORK_ATTACHMENT_IN_BOTH_LISTS_ENTITY",
                    modifiedAttachment.getId().toString())));
    }

    @Test
    public void modifiedAttachmentNotRemovedAttachmentModifiedButNotRemovedValid() {
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder().build();

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
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .build();

        assertThat(validator.validateQosOverriddenInterfaces(), ValidationResultMatchers.isValid());
    }

    @Test
    public void testValidateQosOverriddenInterfacesWhenAttachmentDoesNotHaveQosOverridden() {
        NetworkAttachment networkAttachment = new NetworkAttachment();

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addNetworkAttachments(networkAttachment))
            .build();

        assertThat(validator.validateQosOverriddenInterfaces(), ValidationResultMatchers.isValid());
    }

    @Test
    public void testValidateQosOverriddenInterfacesWhenHostNetworkQosIsNotSupported() {
        Network network = createNetworkWithName("network");

        HostSetupNetworksValidator validator = createValidatorForTestingValidateQosOverridden(network);


        assertThat(validator.validateQosOverriddenInterfaces(),
            ValidationResultMatchers.failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED,
                ReplacementUtils.getVariableAssignmentStringWithMultipleValues(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED,
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

    @Test
    public void testValidateBondModeForLabeledVmNetwork(){
        Bond bond = new Bond();
        bond.setNetworkName(null);
        bond.setName("bondName");
        bond.setLabels(new HashSet<>(Arrays.asList("label")));
        bond.setNetworkName("vmNetwork");

        Network vmNetwork = createNetworkWithName("vmNetwork");
        vmNetwork.setVmNetwork(true);
        vmNetwork.setLabel("label");

        NetworkAttachment vmNetworkNetworkAttachment = createNetworkAttachment(vmNetwork, bond);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(new Bond[]{bond}))
            .addNetworks(vmNetwork)
            .addExistingInterfaces(bond)
            .build();

        for (BondMode bondMode : BondMode.values()){
            bond.setBondOptions(bondMode.getConfigurationValue());
            ValidationResult result = validator.validateBondModeVsNetworksAttachedToIt(Arrays.asList(vmNetworkNetworkAttachment));
            if (bondMode.isBondModeValidForVmNetwork()){
                assertThat(result, isValid());
            } else {
                assertThat(result,
                    failsWith(EngineMessage.INVALID_BOND_MODE_FOR_BOND_WITH_LABELED_VM_NETWORK,
                            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_BOND_NAME, "bondName"),
                            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME, "vmNetwork"),
                            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_LABEL, "label")
                            ));
            }
        }
    }

    @Test
    public void testValidateBondModeForVmNetwork(){
        validateBondMode(true);
    }

    @Test
    public void testValidateBondModeForNonVmNetwork(){
        validateBondMode(false);
    }

    private void validateBondMode(boolean isVmNetwork){

        Bond bond = new Bond();
        bond.setNetworkName(null);
        bond.setName("bondName");
        bond.setNetworkName("networkName");

        Network network = createNetworkWithName("networkName");
        network.setVmNetwork(isVmNetwork);
        NetworkAttachment vmNetworkNetworkAttachment = createNetworkAttachment(network, bond);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(new Bond[]{bond}))
            .addNetworks(network)
            .addExistingInterfaces(bond)
            .build();

        for (BondMode bondMode : BondMode.values()){
            bond.setBondOptions(bondMode.getConfigurationValue());
            ValidationResult result = validator.validateBondModeVsNetworksAttachedToIt(Arrays.asList(vmNetworkNetworkAttachment));
            if (!isVmNetwork || bondMode.isBondModeValidForVmNetwork()){
                assertThat(result, isValid());
            } else {
                assertThat(result,
                    failsWith(EngineMessage.INVALID_BOND_MODE_FOR_BOND_WITH_VM_NETWORK,
                            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_BOND_NAME, "bondName"),
                            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME, "networkName")
                            ));
            }
        }
    }

    @Test
    public void testValidateBondOptionsForNewAttachementWithVmNetwork(){
        validateValidBonds(false, true, true, false);
    }

    @Test
    public void testValidateBondOptionsForNewAttachementWithNonVmNetwork(){
        validateValidBonds(true, false, true, false);
    }

    @Test
    public void validateBondOptionsForNewAttachementWithOutOfSyncVmNetworkNotOverridden(){
        validateValidBonds(true, true, false, false);
    }

    @Test
    public void validateBondOptionsForNewAttachementWithOutOfSyncVmNetworOverridden(){
        validateValidBonds(false, true, false, true);
    }

    private void validateValidBonds(boolean isValidForAllModes, boolean isVmNetwork, boolean isInSync, boolean isOverriddenConfiguration){
        Network network = createNetworkWithName("network");
        network.setVmNetwork(isVmNetwork);

        Bond bond = new Bond();
        bond.setNetworkName(null);
        bond.setName("bondName");
        bond.setNetworkName("network");
        NetworkImplementationDetails networkImplementationDetails = new NetworkImplementationDetails(isInSync, true);
        bond.setNetworkImplementationDetails(networkImplementationDetails);

        NetworkAttachment networkAttachment = createNetworkAttachment(network, bond);
        networkAttachment.setOverrideConfiguration(isOverriddenConfiguration);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(new Bond[]{bond}))
            .addNetworks(network)
            .addExistingInterfaces(bond)
            .build();

        if (isValidForAllModes){
            validateValidForAllBondModes(bond, networkAttachment, validator);
        } else {
            validateValidOnlyForNonVmNetworksBondMode(bond, networkAttachment, validator);

        }
    }

    private void validateValidForAllBondModes(Bond bond, NetworkAttachment networkNetworkAttachment, HostSetupNetworksValidator validator){
        for (BondMode bondMode : BondMode.values()){
            bond.setBondOptions(bondMode.getConfigurationValue());
            ValidationResult result = validator.validateBondModeVsNetworksAttachedToIt(Arrays.asList(networkNetworkAttachment));
            assertThat(result, isValid());
        }
    }

    private void validateValidOnlyForNonVmNetworksBondMode(Bond bond, NetworkAttachment networkNetworkAttachment, HostSetupNetworksValidator validator){
        for (BondMode bondMode : BondMode.values()){
            bond.setBondOptions(bondMode.getConfigurationValue());
            ValidationResult result = validator.validateBondModeVsNetworksAttachedToIt(Arrays.asList(networkNetworkAttachment));
            if (bondMode.isBondModeValidForVmNetwork()){
                assertThat(result, isValid());
            } else {
                assertThat(result,
                    failsWith(EngineMessage.INVALID_BOND_MODE_FOR_BOND_WITH_VM_NETWORK,
                            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_BOND_NAME, "bondName"),
                            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME, "network")
                            ));
            }
        }
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
            .addNetworks(network1, network2)
            .addExistingInterfaces(baseNic, vlanNic1, vlanNic2)
            .build();


        assertThat(validator.validateQosNotPartiallyConfigured(networkAttachments), matcher);
    }

    private HostSetupNetworksValidator createValidatorForTestingValidateQosOverridden(Network network) {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkId(network.getId());
        networkAttachment.setHostNetworkQos(new HostNetworkQos());

        return new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addNetworkAttachments(networkAttachment))
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
        final String labelName = "lbl1";
        Network network = createNetworkWithNameAndLabel("net", labelName);
        NetworkAttachment attachment = createNetworkAttachment(network, nic);

        NicLabel nicLabel = referenceSameNic
            ? new NicLabel(nic.getId(), nic.getName(), labelName)
            : new NicLabel(Guid.newGuid(), nic.getName() + "not", labelName);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addLabels(nicLabel))
            .addNetworks(network)
            .addExistingInterfaces(nic)
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

    public class ParametersBuilder {
        private HostSetupNetworksParameters parameters = new HostSetupNetworksParameters(host.getId());

        private HostSetupNetworksParameters build() {
            //for case host instance was changed after instantiating this class.
            parameters.setVdsId(host.getId());
            return parameters;
        }

        public ParametersBuilder addNetworkAttachments(NetworkAttachment... networkAttachments) {
            if (nullParameters(networkAttachments)) {
                return this;
            }

            if (parameters.getNetworkAttachments() == null) {
                parameters.setNetworkAttachments(new ArrayList<>());
            }

            parameters.getNetworkAttachments().addAll(Arrays.asList(networkAttachments));
            return this;
        }

        public ParametersBuilder addRemovedBonds(Guid... ids) {
            if (nullParameters(ids)) {
                return this;
            }

            if (parameters.getRemovedBonds() == null) {
                parameters.setRemovedBonds(new HashSet<>());
            }

            parameters.getRemovedBonds().addAll(Arrays.asList(ids));
            return this;
        }

        public ParametersBuilder addRemovedNetworkAttachments(NetworkAttachment... networkAttachments) {
            if (nullParameters(networkAttachments)) {
                return this;
            }

            if (parameters.getRemovedNetworkAttachments() == null) {
                parameters.setRemovedNetworkAttachments(new HashSet<>());
            }

            for (NetworkAttachment networkAttachment : networkAttachments) {
                parameters.getRemovedNetworkAttachments().add(networkAttachment.getId());
            }
            return this;
        }

        public ParametersBuilder addBonds(Bond... bonds) {
            if (nullParameters(bonds)) {
                return this;
            }

            if (parameters.getBonds() == null) {
                parameters.setBonds(new ArrayList<>());
            }

            parameters.getBonds().addAll(Arrays.asList(bonds));
            return this;
        }

        public ParametersBuilder addRemovedLabels(String... removedLabels) {
            if (nullParameters(removedLabels)) {
                return this;
            }

            if (parameters.getRemovedLabels() == null) {
                parameters.setRemovedLabels(new HashSet<>());
            }

            parameters.getRemovedLabels().addAll(Arrays.asList(removedLabels));
            return this;
        }

        public ParametersBuilder addLabels(NicLabel... nicLabels) {
            if (nullParameters(nicLabels)) {
                return this;
            }

            if (parameters.getLabels() == null) {
                parameters.setLabels(new HashSet<>());
            }

            parameters.getLabels().addAll(Arrays.asList(nicLabels));
            return this;
        }

        private <T>boolean nullParameters(T[] ids) {
            return ids == null;
        }
    }

    public class HostSetupNetworksValidatorBuilder {
        private HostSetupNetworksParameters params = new ParametersBuilder().build();
        private List<VdsNetworkInterface> existingInterfaces = new ArrayList<>();
        private List<NetworkAttachment> existingAttachments = new ArrayList<>();
        private List<Network> networks = new ArrayList<>();

        public HostSetupNetworksValidatorBuilder setEmptyParams() {
            setParams(new ParametersBuilder().build());
            return this;
        }

        public HostSetupNetworksValidatorBuilder setParams(HostSetupNetworksParameters params) {
            this.params = params;
            return this;
        }

        public HostSetupNetworksValidatorBuilder setParams(ParametersBuilder builder) {
            return setParams(builder.build());
        }

        public HostSetupNetworksValidatorBuilder addExistingInterfaces(Collection<VdsNetworkInterface> existingInterfaces) {
            if (existingInterfaces == null) {
                return this;
            }

            this.existingInterfaces.addAll(existingInterfaces);
            return this;
        }

        public HostSetupNetworksValidatorBuilder addExistingInterfaces(VdsNetworkInterface... existingInterfaces) {
            if (existingInterfaces == null) {
                return this;
            }

            return addExistingInterfaces(Arrays.asList(existingInterfaces));
        }

        public HostSetupNetworksValidatorBuilder addExistingAttachments(NetworkAttachment... existingAttachments) {
            if (existingAttachments == null) {
                return this;
            }

            return addExistingAttachments(Arrays.asList(existingAttachments));
        }

        public HostSetupNetworksValidatorBuilder addExistingAttachments(Collection<NetworkAttachment> existingAttachments) {
            if (existingAttachments == null) {
                return this;
            }

            this.existingAttachments.addAll(existingAttachments);
            return this;
        }

        public HostSetupNetworksValidatorBuilder addNetworks(Network ... networks) {
            if (networks == null) {
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
                mockNetworkExclusivenessValidatorResolver,
                mockNetworkAttachmentIpConfigurationValidator);
        }
    }
}

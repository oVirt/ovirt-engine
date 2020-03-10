package org.ovirt.engine.core.bll.network.host;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.network.host.HostSetupNetworksValidator.VAR_NETWORK_NAME;
import static org.ovirt.engine.core.bll.network.host.HostSetupNetworksValidator.VAR_VM_NAMES;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.ReplacementUtils.replaceWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.FindActiveVmsUsingNetwork;
import org.ovirt.engine.core.bll.validator.HostInterfaceValidator;
import org.ovirt.engine.core.bll.validator.HostNetworkQosValidator;
import org.ovirt.engine.core.bll.validator.ValidationResultMatchers;
import org.ovirt.engine.core.bll.validator.network.NetworkAttachmentIpConfigurationValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidatorResolver;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.BondMode;
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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.ReplacementUtils;

@ExtendWith({ MockitoExtension.class, MockConfigExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
public class HostSetupNetworksValidatorTest {

    private static final String SEPARATOR = ",";

    private VDS host;

    @Mock
    private NetworkDao networkDaoMock;

    @Mock
    private NetworkAttachmentDao networkAttachmentDaoMock;

    @Mock
    private FindActiveVmsUsingNetwork findActiveVmsUsingNetwork;

    @Mock
    private NetworkClusterDao networkClusterDaoMock;

    @Mock
    private VdsDao vdsDaoMock;

    private Bond bond;

    @Mock
    private NetworkExclusivenessValidatorResolver mockNetworkExclusivenessValidatorResolver;
    @Mock
    private NetworkExclusivenessValidator mockNetworkExclusivenessValidator;
    @Mock
    private NetworkAttachmentIpConfigurationValidator mockNetworkAttachmentIpConfigurationValidator;
    @Mock
    private BackendInternal backendInternal;

    @Captor
    private ArgumentCaptor<Collection<String>> collectionArgumentCaptor;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.concat(AbstractQueryTest.mockConfiguration(),
                Stream.of(
                        MockConfigDescriptor.of(ConfigValues.CustomBondNameSupported, Version.getLast(), true),
                        MockConfigDescriptor.of(ConfigValues.CustomBondNameSupported, Version.v4_2, false)
                )
        );
    }

    @BeforeEach
    public void setUp() {
        host = new VDS();
        host.setId(Guid.newGuid());
        final VdsDynamic vdsDynamic = new VdsDynamic();
        host.setDynamicData(vdsDynamic);
        host.setClusterCompatibilityVersion(Version.getLast());

        bond = new Bond();
        bond.setId(Guid.newGuid());

        when(mockNetworkExclusivenessValidatorResolver.resolveNetworkExclusivenessValidator())
                .thenReturn(mockNetworkExclusivenessValidator);
    }

    public void testNotRemovingLabeledNetworksReferencingUnlabeledNetworkRemovalIsOk() {
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
    public void testNotRemovingLabeledNetworksWhenNicNameDoesNotReferenceExistingNicItsOkToRemove() {
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
    public void testNotRemovingLabeledNetworksWhenRemovingLabeledNetworkUnrelatedToRemovedBond() {
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
            ReplacementUtils.getVariableAssignmentStringWithMultipleValues(engineMessage, labeledNetwork.getName())));

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
    public void testNotMovingLabeledNetworkToDifferentNicWhenRemovingLabeledNetworkUnrelatedToRemovedBond() {
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
    public void testNotRemovingLabeledNetworksWhenLabelRelatedToRemovedBond() {
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
            .addExistingInterfaces(Collections.singletonList(bond))
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
    public void testValidRemovedBondsWhenNotRemovingAnyBond() {
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .addExistingInterfaces((List<VdsNetworkInterface>) null)
            .build();

        assertThat(validator.validRemovedBonds(Collections.emptyList()), isValid());
    }

    @Test
    public void testValidRemovedBondsWhenReferencedInterfaceIsNotBond() {
        VdsNetworkInterface notABond = createNic("nicName");

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedBonds(notABond.getId()))
            .addExistingInterfaces(Collections.singletonList(notABond))
            .build();

        final EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_IS_NOT_BOND;
        assertThat(validator.validRemovedBonds(Collections.emptyList()),
            failsWith(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, notABond.getName())));
    }

    @Test
    public void testValidRemovedBondsWhenReferencedInterfaceBondViaInexistingId() {
        Guid idOfInexistingInterface = Guid.newGuid();
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedBonds(idOfInexistingInterface))
            .build();

        EngineMessage engineMessage = EngineMessage.NETWORK_BOND_RECORDS_DOES_NOT_EXISTS;
        assertThat(validator.validRemovedBonds(Collections.emptyList()),
            failsWith(engineMessage,
                ReplacementUtils.getListVariableAssignmentString(engineMessage,
                    Collections.singletonList(idOfInexistingInterface))));

    }

    @Test
    public void testValidRemovedBondsWhenBondIsRequired() {
        String nicName = "nicName";
        bond.setName(nicName);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedBonds(bond.getId()))
            .addExistingInterfaces(Collections.singletonList(bond))
            .build();

        NetworkAttachment requiredNetworkAttachment = new NetworkAttachment();
        requiredNetworkAttachment.setNicName(nicName);

        List<String> replacements = new ArrayList<>();
        EngineMessage engineMessage = EngineMessage.BOND_USED_BY_NETWORK_ATTACHMENTS;
        replacements.add(ReplacementUtils.getVariableAssignmentString(engineMessage, nicName));
        //null -- new network attachment with null id.
        replacements.addAll(replaceWith(HostSetupNetworksValidator.VAR_ATTACHMENT_IDS,
            Collections.<Guid> singletonList(null)));

        assertThat(validator.validRemovedBonds(Collections.singletonList(requiredNetworkAttachment)),
            failsWith(engineMessage, replacements));

    }

    @Test
    public void testValidRemovedBondsWhenBondIsNotRequired() {
        String nicName = "nicName";
        bond.setName(nicName);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedBonds(bond.getId()))
            .addExistingInterfaces(Collections.singletonList(bond))
            .build();

        assertThat(validator.validRemovedBonds(Collections.emptyList()), isValid());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAttachmentsToConfigureWhenNoChangesWereSent() {
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
    public void testGetAttachmentsToConfigureWhenUpdatingNetworkAttachments() {
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
    public void testGetAttachmentsToConfigureWhenRemovingNetworkAttachments() {
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
    public void testGetAttachmentsToConfigureWhenAddingNewNetworkAttachments() {
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
        ValidationResult actual = validator.validNewOrModifiedNetworkAttachments();
        assertEquals(ValidationResult.VALID, actual);

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
        when(networkClusterDaoMock.get(new NetworkClusterId(host.getClusterId(), guid)))
                .thenReturn(mock(NetworkCluster.class));
    }

    private void addNetworkIdToNetworkDaoMock(Network network) {
        when(networkDaoMock.get(eq(network.getId()))).thenReturn(network);
    }

    private void initMockNetworkAttachmentIpConfigurationValidator(EngineMessage engineMessage,
            Collection<String> replacements) {
        ValidationResult validationResult = new ValidationResult(engineMessage, replacements);
        when(mockNetworkAttachmentIpConfigurationValidator.validateNetworkAttachmentIpConfiguration(any(), any(), eq(false)))
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

    @Test
    public void testValidateNotRemovingUsedNetworkByVmsWhenUsedByVms() {
        String nameOfNetworkA = "networkA";
        Network networkA = createNetworkWithName(nameOfNetworkA);

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);
        VdsNetworkInterface nicA = createNic("nicA");
        networkAttachmentA.setNicId(nicA.getId());

        final HostSetupNetworksValidator underTest =
                new HostSetupNetworksValidatorBuilder()
                        .setParams(new ParametersBuilder()
                                .addRemovedNetworkAttachments(networkAttachmentA)
                                .build())
                        .addExistingInterfaces(Collections.singletonList(nicA))
                        .addExistingAttachments(Collections.singletonList(networkAttachmentA))
                        .addNetworks(Collections.singletonList(networkA))
                        .build();

        List<String> vmNames = Arrays.asList("vmName1", "vmName2");
        when(findActiveVmsUsingNetwork.findNamesOfActiveVmsUsingNetworks(any(), anyCollection())).thenReturn(vmNames);

        final List<String> removedNetworkNames = Collections.singletonList(nameOfNetworkA);
        assertThat(underTest.validateNotRemovingUsedNetworkByVms(nameOfNetworkA),
                failsWith(EngineMessage.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS,
                        Stream.concat(
                                ReplacementUtils.replaceWith(VAR_NETWORK_NAME, removedNetworkNames, SEPARATOR).stream(),
                                ReplacementUtils.replaceWith(VAR_VM_NAMES, vmNames, SEPARATOR).stream()
                        ).collect(Collectors.toList())));

        verify(findActiveVmsUsingNetwork).findNamesOfActiveVmsUsingNetworks(
                eq(host.getId()),
                collectionArgumentCaptor.capture());
        assertThat(collectionArgumentCaptor.getValue(), contains(nameOfNetworkA));
    }

    @Test
    public void testValidateNotRemovingPhysicalNetworksLinkedToExternalUsedByVMs() {
        String nameOfNetworkA = "networkA";
        Network networkA = createNetworkWithName(nameOfNetworkA);

        String nameOfPhysicalNetwork = "physicalNetwork";
        Network physicalNetwork = createNetworkWithName(nameOfPhysicalNetwork);

        NetworkAttachment networkAttachmentA = createNetworkAttachment(physicalNetwork);
        VdsNetworkInterface nicA = createNic("nicA");
        networkAttachmentA.setNicId(nicA.getId());

        final HostSetupNetworksValidator underTest =
                new HostSetupNetworksValidatorBuilder()
                        .setParams(new ParametersBuilder()
                                .addRemovedNetworkAttachments(networkAttachmentA)
                                .build())
                        .addExistingInterfaces(Collections.singletonList(nicA))
                        .addExistingAttachments(Collections.singletonList(networkAttachmentA))
                        .addNetworks(Collections.singletonList(physicalNetwork))
                        .build();

        List<String> vmNames = Arrays.asList("vmName1", "vmName2");
        when(findActiveVmsUsingNetwork.findNamesOfActiveVmsUsingNetworks(any(), anyCollection())).thenReturn(vmNames);
        when(networkDaoMock.getAllExternalNetworksLinkedToPhysicalNetwork(any())).thenReturn(Collections.singletonList(
                networkA));

        final List<String> removedNetworkNames = Collections.singletonList(nameOfPhysicalNetwork);
        assertThat(underTest.validateNotRemovingPhysicalNetworksLinkedToExternalUsedByVMs(physicalNetwork.getId(),
                nameOfPhysicalNetwork),
                failsWith(EngineMessage.NETWORK_CANNOT_DETACH_PHYSICAL_NETWORK_LINKED_TO_EXTERNAL_USED_BY_VM,
                        Stream.concat(
                                ReplacementUtils.replaceWith(VAR_NETWORK_NAME, removedNetworkNames, SEPARATOR).stream(),
                                ReplacementUtils.replaceWith(VAR_VM_NAMES, vmNames, SEPARATOR).stream()
                        ).collect(Collectors.toList())));

        verify(findActiveVmsUsingNetwork).findNamesOfActiveVmsUsingNetworks(
                eq(host.getId()),
                collectionArgumentCaptor.capture());
        assertThat(collectionArgumentCaptor.getValue(), contains(nameOfNetworkA));
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

    @Test
    public void testValidateNotRemovingUsedNetworkByVmsWhenNotUsedByVms() {
        HostSetupNetworksValidator validator = spy(new HostSetupNetworksValidatorBuilder()
            .build());

        assertThat(validator.validateNotRemovingUsedNetworkByVms("removedNet"), isValid());
    }

    @Test
    public void testValidateNotRemovingPhysicalNetworksLinkedToExternalUsedByVMsWhenNotUsedByVms() {
        HostSetupNetworksValidator validator = spy(new HostSetupNetworksValidatorBuilder()
                .build());

        when(networkDaoMock.getAllExternalNetworksLinkedToPhysicalNetwork(any())).thenReturn(Collections.emptyList());
        assertThat(validator.validateNotRemovingPhysicalNetworksLinkedToExternalUsedByVMs(Guid.newGuid(), "removedNet"),
                isValid());
    }

    @Test
    public void testNetworksUniquelyConfiguredOnHostWhenUniquelyConfigured() {
        Network networkA = new Network();
        networkA.setId(Guid.newGuid());
        networkA.setName("A");

        Network networkB = new Network();
        networkB.setId(Guid.newGuid());
        networkA.setName("B");

        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .addNetworks(Arrays.asList(networkA, networkB))
            .build();

        assertThat(validator.networksUniquelyConfiguredOnHost(Arrays.asList(networkAttachmentA, networkAttachmentB)),
            isValid());
    }

    @Test
    public void testNetworksUniquelyConfiguredOnHostWhenNotUniquelyConfigured() {
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
                ReplacementUtils.getVariableAssignmentStringWithMultipleValues(EngineMessage.NETWORKS_ALREADY_ATTACHED_TO_IFACES,
                    networkName)));

    }

    @Test
    public void testValidModifiedBondsFailsWhenBondNotHaveNameAndId() {
        CreateOrUpdateBond createOrUpdateBond = new CreateOrUpdateBond();
        doTestValidModifiedBonds(createOrUpdateBond,
                ValidationResult.VALID,
                new ValidationResult(EngineMessage.BOND_DOES_NOT_HAVE_NEITHER_ID_NOR_NAME_SPECIFIED),
                ValidationResult.VALID);
    }

    @Test
    public void testValidModifiedBondsFailsWhenReferencingExistingNonBondInterface() {
        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBondWithNameAndId();

        final EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_IS_NOT_BOND;
        ValidationResult notABondValidationResult = new ValidationResult(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, createOrUpdateBond.getName()));

        doTestValidModifiedBonds(createOrUpdateBond,
                notABondValidationResult,
                notABondValidationResult,
                ValidationResult.VALID);
    }

    @Test
    public void testValidModifiedBondsFailsWhenInsufficientNumberOfSlaves() {
        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBondWithNameAndId();
        doTestValidModifiedBonds(createOrUpdateBond,
                ValidationResult.VALID,
                new ValidationResult(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT,
                        ReplacementUtils.getVariableAssignmentString(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT,
                                createOrUpdateBond.getName())),

            ValidationResult.VALID);
    }

    @Test
    public void testValidModifiedBondsFailsWhenSlavesValidationFails() {
        EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE;
        ValidationResult slavesValidationResult = new ValidationResult(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, "slaveA"),
            ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME, "networkName"));

        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBondWithNameAndId();
        createOrUpdateBond.setSlaves(Stream.of("slaveA", "slaveB").collect(toSet()));
        doTestValidModifiedBonds(createOrUpdateBond,
                ValidationResult.VALID,
            /*this mocks validateModifiedBondSlaves to just verify, that caller method will behave ok, when
            validateModifiedBondSlaves return invalid result*/
                slavesValidationResult,
                slavesValidationResult);
    }

    @Test
    public void testValidModifiedBondsWhenAllOk() {
        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBond(null, "bond1", "slaveA", "slaveB");
        doTestValidModifiedBonds(
                createOrUpdateBond,
                ValidationResult.VALID,
                ValidationResult.VALID,
                ValidationResult.VALID);
    }

    private void doTestValidModifiedBonds(CreateOrUpdateBond createOrUpdateBond,
            ValidationResult interfaceIsBondValidationResult,
            ValidationResult expectedValidationResult,
            ValidationResult slavesValidationValidationResult) {

        HostSetupNetworksValidator validator =
                spy(new HostSetupNetworksValidatorBuilder()
                        .setParams(new ParametersBuilder().addBonds(createOrUpdateBond))
                        .addExistingInterfaces((List<VdsNetworkInterface>) null)
                        .addExistingAttachments((List<NetworkAttachment>) null)
                        .addNetworks((Collection<Network>) null)
                        .build());

        HostInterfaceValidator hostInterfaceValidatorMock = mock(HostInterfaceValidator.class);
        when(hostInterfaceValidatorMock.interfaceIsBondOrNull()).thenReturn(interfaceIsBondValidationResult);

        doReturn(hostInterfaceValidatorMock).when(validator).createHostInterfaceValidator(any());
        doReturn(slavesValidationValidationResult).when(validator).validateModifiedBondSlaves(any());

        if (expectedValidationResult.isValid()) {
            assertThat(validator.validNewOrModifiedBonds(), isValid());
        } else {
            assertThat(validator.validNewOrModifiedBonds(),
                failsWith(expectedValidationResult.getMessages(),
                        expectedValidationResult.getVariableReplacements()));
        }
    }

    private CreateOrUpdateBond createNewCreateOrUpdateBondWithNameAndId() {
        return createNewCreateOrUpdateBond(Guid.newGuid(), "bond1");
    }

    private CreateOrUpdateBond createNewCreateOrUpdateBond(Guid bondId, String bondName, String ... slaveNames) {
        CreateOrUpdateBond createOrUpdateBond = new CreateOrUpdateBond();

        createOrUpdateBond.setId(bondId);
        createOrUpdateBond.setName(bondName);
        createOrUpdateBond.setSlaves(Stream.of(slaveNames).collect(toSet()));

        return createOrUpdateBond;
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveInterfaceDoesNotExist() {
        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBond(Guid.newGuid(), "bond1", "slaveA", "slaveB");

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(createOrUpdateBond))
            .build();
        doTestValidateModifiedBondSlaves(
            spy(validator), new ValidationResult(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST),
            ValidationResult.VALID,
            failsWith(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST));
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveIsNotValid() {
        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBond(Guid.newGuid(), "bond1", "slaveA", "slaveB");

        ValidationResult cannotBeSlaveValidationResult = new ValidationResult(EngineMessage.NETWORK_INTERFACE_BOND_OR_VLAN_CANNOT_BE_SLAVE,
        ReplacementUtils.createSetVariableString(HostInterfaceValidator.VAR_NIC_NAME, createOrUpdateBond.getName()));

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(createOrUpdateBond))
            .build();
        doTestValidateModifiedBondSlaves(
            spy(validator), ValidationResult.VALID,
            cannotBeSlaveValidationResult,
            failsWith(cannotBeSlaveValidationResult));

    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveAlreadySlavesForDifferentBond() {
        Bond bond = createBond("bond1");
        Bond differentBond = createBond("bond2");

        VdsNetworkInterface slaveA = createBondSlave(bond, "slaveA");
        VdsNetworkInterface slaveB = createBondSlave(differentBond, "slaveB");

        setBondSlaves(bond, slaveA, slaveB);

        EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_ALREADY_IN_BOND;
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(CreateOrUpdateBond.fromBond(bond)))
            .addExistingInterfaces(bond, differentBond, slaveA, slaveB)
            .build();

        doTestValidateModifiedBondSlaves(
            spy(validator), ValidationResult.VALID,
            ValidationResult.VALID,
            failsWith(engineMessage,
                    ReplacementUtils.getVariableAssignmentString(engineMessage, slaveB.getName())));
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveAlreadySlavesForDifferentBondWhichGetsRemoved() {
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
    public void testValidateModifiedBondSlavesWhenSlaveAlreadySlavesForDifferentBondButItsDetachedFromItAsAPartOfRequest() {
        Bond bond = createBond("bond1");
        Bond differentBond = createBond("bond2");

        VdsNetworkInterface slaveA = createBondSlave(bond, "slaveA");
        VdsNetworkInterface slaveB = createBondSlave(differentBond, "slaveB");
        VdsNetworkInterface slaveC = createBondSlave(differentBond, "slaveC");
        VdsNetworkInterface slaveD = createBondSlave(differentBond, "slaveD");

        setBondSlaves(bond, slaveA, slaveB);
        setBondSlaves(differentBond, slaveC, slaveD);

        HostSetupNetworksValidator build = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(CreateOrUpdateBond.fromBond(bond), CreateOrUpdateBond.fromBond(differentBond)))
            .addExistingInterfaces(bond, differentBond, slaveA, slaveB, slaveC, slaveD)
            .build();
        doTestValidateModifiedBondSlaves(
            spy(build), ValidationResult.VALID,
            ValidationResult.VALID,
            isValid());
    }

    private Bond createBond(String bondName, String networkName, Guid id) {
        Bond bond = new Bond();
        bond.setId(id);
        bond.setName(bondName);
        bond.setNetworkName(networkName);
        return bond;
    }

    public Bond createBond(String bondName) {
        return createBond(bondName, null, Guid.newGuid());
    }

    private Bond createBond() {
        return createBond("bond1");
    }

    @Test
    public void testValidateModifiedBondSlavesWhenSlaveHasNetworkAssignedWhichIsNotRemovedAsAPartOfRequest() {
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
            .setParams(new ParametersBuilder().addBonds(CreateOrUpdateBond.fromBond(bond)))
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
    public void testValidateModifiedBondSlavesWhenSlaveHasNetworkAssignedWhichIsRemovedAsAPartOfRequest() {
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
        when(hostInterfaceValidatorMock.interfaceExists(anyString())).thenReturn(interfaceExistValidationResult);
        when(hostInterfaceValidatorMock.interfaceIsValidSlave()).thenReturn(interfaceIsValidSlaveValidationResult);

        doReturn(hostInterfaceValidatorMock).when(validator).createHostInterfaceValidator(any());

        assertThat(validator.validNewOrModifiedBonds(), matcher);
    }

    @Test
    public void testValidateCustomPropertiesWhenAttachmentDoesNotHaveCustomProperties() {
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
                        Collections.emptyMap(),
                        Collections.emptyMap()),
                isValid());
    }

    @Test
    public void testValidateCustomPropertiesWhenCustomPropertyValidationFailed() {
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
        doReturn(Collections.emptyList()).when(validator).translateErrorMessages(any());

        EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT;
        assertThat(validator.validateCustomProperties(SimpleCustomPropertiesUtil.getInstance(),
                Collections.emptyMap(),
                Collections.emptyMap()),
            failsWith(engineMessage,
                    ReplacementUtils.getVariableAssignmentStringWithMultipleValues(engineMessage, networkA.getName())));

    }

    @Test
    public void testValidateCustomProperties() {
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

        assertThat(validator.validateCustomProperties(simpleCustomPropertiesUtilMock,
                Collections.emptyMap(),
                Collections.emptyMap()),
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

        CreateOrUpdateBond createOrUpdateBond =
                createNewCreateOrUpdateBond(Guid.newGuid(), "bond1", nicA.getName(), nicB.getName());

        addNetworkIdToNetworkDaoMock(networkA);
        addNetworkToClusterDaoMock(networkA.getId());

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder()
                .addNetworkAttachments(networkAttachment)
                .addBonds(createOrUpdateBond)
                .build())
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
    public void testValidateQosOverriddenInterfacesWhenAttachmentHasQosOverriddenAndRequiredValuesNotPresent() {
        EngineMessage hostNetworkQosValidatorFailure =
            EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_SETUP_NETWORKS_MISSING_VALUES;

        Network network = createNetworkWithName("network");
        HostSetupNetworksValidator validator = createValidatorForTestingValidateQosOverridden(network);

        HostSetupNetworksValidator validatorSpy = spy(validator);
        HostNetworkQosValidator hostNetworkQosValidatorMock = mock(HostNetworkQosValidator.class);

        when(hostNetworkQosValidatorMock.requiredQosValuesPresentForOverriding(eq(network.getName()))).
            thenReturn(new ValidationResult(hostNetworkQosValidatorFailure));

        doReturn(hostNetworkQosValidatorMock).when(validatorSpy)
            .createHostNetworkQosValidator(any());

        assertThat(validatorSpy.validateQosOverriddenInterfaces(),
                ValidationResultMatchers.failsWith(hostNetworkQosValidatorFailure));
        verify(hostNetworkQosValidatorMock).requiredQosValuesPresentForOverriding(eq(network.getName()));
        verifyNoMoreInteractions(hostNetworkQosValidatorMock);
    }

    @Test
    public void testValidateQosOverriddenInterfacesWhenAttachmentHasQosOverriddenAndRequiredValuesPresentButInconsistent() {
        EngineMessage hostNetworkQosValidatorFailure =
            EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_INCONSISTENT_VALUES;

        Network network = createNetworkWithName("network");
        HostSetupNetworksValidator validator = createValidatorForTestingValidateQosOverridden(network);

        HostSetupNetworksValidator validatorSpy = spy(validator);
        HostNetworkQosValidator hostNetworkQosValidatorMock = mock(HostNetworkQosValidator.class);

        when(hostNetworkQosValidatorMock.valuesConsistent(eq(network.getName()))).
            thenReturn(new ValidationResult(hostNetworkQosValidatorFailure));

        doReturn(hostNetworkQosValidatorMock).when(validatorSpy)
            .createHostNetworkQosValidator(any());

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
    public void testValidateBondModesForLabeledVmNetwork() {
        for (BondMode bondMode : BondMode.values()) {
            validateBondModeForLabeledVmNetwork(bondMode);
        }
    }

    private void validateBondModeForLabeledVmNetwork(BondMode bondMode) {
        String bondName = "bondName";
        String networkName = "vmNetwork";
        String label = "label";

        Bond bond = createBond(bondName, networkName, null);
        bond.setLabels(new HashSet<>(Collections.singletonList(label)));
        bond.setBondOptions(bondMode.getConfigurationValue());

        Network vmNetwork = createNetworkWithName(networkName);
        vmNetwork.setVmNetwork(true);
        vmNetwork.setLabel(label);

        NetworkAttachment vmNetworkNetworkAttachment = createNetworkAttachment(vmNetwork, bond);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(CreateOrUpdateBond.fromBond(bond)))
            .addNetworks(vmNetwork)
            .addExistingInterfaces(bond)
            .build();

        List<NetworkAttachment> attachmentsToConfigure = Collections.singletonList(vmNetworkNetworkAttachment);
        ValidationResult result = validator.validateBondModeVsNetworksAttachedToIt(attachmentsToConfigure);
        if (bondMode.isBondModeValidForVmNetwork()) {
            assertThat(result, isValid());
        } else {
            assertThat(result,
                failsWith(EngineMessage.INVALID_BOND_MODE_FOR_BOND_WITH_LABELED_VM_NETWORK,
                        ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_BOND_NAME,
                                bondName),
                        ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME,
                                networkName),
                        ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_LABEL, label)
                        ));
        }
    }

    @Test
    public void testValidateBondModeForVmNetwork() {
        validateBondModes(true);
    }

    @Test
    public void testValidateBondModeForNonVmNetwork() {
        validateBondModes(false);
    }

    private void validateBondModes(boolean isVmNetwork) {
        for (BondMode bondMode : BondMode.values()) {
            validateBondMode(isVmNetwork, bondMode);
        }
    }

    private void validateBondMode(boolean isVmNetwork, BondMode bondMode) {
        String networkName = "networkName";
        String bondName = "bondName";

        Bond bond = createBond(bondName, networkName, null);
        bond.setBondOptions(bondMode.getConfigurationValue());

        Network network = createNetworkWithName(networkName);
        network.setVmNetwork(isVmNetwork);
        NetworkAttachment vmNetworkNetworkAttachment = createNetworkAttachment(network, bond);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addBonds(CreateOrUpdateBond.fromBond(bond)))
            .addNetworks(network)
            .addExistingInterfaces(bond)
            .build();

        List<NetworkAttachment> attachmentsToConfigure = Collections.singletonList(vmNetworkNetworkAttachment);
        ValidationResult result = validator.validateBondModeVsNetworksAttachedToIt(attachmentsToConfigure);
        if (!isVmNetwork || bondMode.isBondModeValidForVmNetwork()) {
            assertThat(result, isValid());
        } else {
            assertThat(result,
                failsWith(EngineMessage.INVALID_BOND_MODE_FOR_BOND_WITH_VM_NETWORK,
                        ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_BOND_NAME, bondName),
                        ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME, networkName)
                ));
        }
    }

    @Test
    public void testValidateBondOptionsForNewAttachementWithVmNetwork() {
        validateValidBondsForAllBondModes(false, true, true, false);
    }

    @Test
    public void testValidateBondOptionsForNewAttachementWithNonVmNetwork() {
        validateValidBondsForAllBondModes(true, false, true, false);
    }

    @Test
    public void validateBondOptionsForNewAttachementWithOutOfSyncVmNetworkNotOverridden() {
        validateValidBondsForAllBondModes(true, true, false, false);
    }

    @Test
    public void validateBondOptionsForNewAttachementWithOutOfSyncVmNetworOverridden() {
        NetworkAttachment modifiedAttachment = createNetworkAttachment(new Network());

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addRemovedNetworkAttachments(modifiedAttachment))
            .build();

        assertThat(validator.modifiedAttachmentNotRemoved(modifiedAttachment),
                failsWith(EngineMessage.NETWORK_ATTACHMENT_IN_BOTH_LISTS,
                        ReplacementUtils.createSetVariableString("NETWORK_ATTACHMENT_IN_BOTH_LISTS_ENTITY",
                                modifiedAttachment.getId().toString())));
    }

    private void validateValidBondsForAllBondModes(boolean isValidForAllModes,
            boolean isVmNetwork,
            boolean isInSync,
            boolean isOverriddenConfiguration) {

        for (BondMode bondMode : BondMode.values()) {
            validateValidBondsForBondMode(isValidForAllModes, isVmNetwork, isInSync, isOverriddenConfiguration, bondMode);
        }
    }

    private void validateValidBondsForBondMode(boolean isValidForAllModes,
            boolean isVmNetwork,
            boolean isInSync,
            boolean isOverriddenConfiguration,
            BondMode bondMode) {
        String networkName = "network";
        String bondName = "bondName";

        Network network = createNetworkWithName(networkName);
        network.setVmNetwork(isVmNetwork);

        NetworkImplementationDetails networkImplementationDetails = new NetworkImplementationDetails(isInSync, true);

        Bond bond = createBond(bondName, networkName, null);
        bond.setBondOptions(bondMode.getConfigurationValue());
        bond.setNetworkImplementationDetails(networkImplementationDetails);

        NetworkAttachment networkAttachment = createNetworkAttachment(network, bond);
        networkAttachment.setOverrideConfiguration(isOverriddenConfiguration);

        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
                .setParams(new ParametersBuilder().addBonds(CreateOrUpdateBond.fromBond(bond)))
                .addNetworks(network)
                .addExistingInterfaces(bond)
                .build();

        boolean expectValidValidationResult = isValidForAllModes || bondMode.isBondModeValidForVmNetwork();
        List<NetworkAttachment> attachmentsToConfigure = Collections.singletonList(networkAttachment);
        ValidationResult result = validator.validateBondModeVsNetworksAttachedToIt(attachmentsToConfigure);
        if (expectValidValidationResult) {
            assertThat(result, isValid());
        } else {
            assertThat(result, failsWith(EngineMessage.INVALID_BOND_MODE_FOR_BOND_WITH_VM_NETWORK,
                    ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_BOND_NAME, bondName),
                    ReplacementUtils.createSetVariableString(HostSetupNetworksValidator.VAR_NETWORK_NAME, networkName)
            ));
        }
    }

    @Test
    public void testBondNotUpdatedAndRemovedSimultaneouslyValid() {
        HostSetupNetworksParameters params = new ParametersBuilder().addBonds(CreateOrUpdateBond.fromBond(bond))
                .addRemovedBonds(Guid.newGuid())
                .build();
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
                .setParams(params)
                .build();

        assertThat(validator.bondNotUpdatedAndRemovedSimultaneously(), isValid());
    }

    @Test
    public void testBondNotUpdatedAndRemovedSimultaneouslyNotValid() {
        HostSetupNetworksParameters params = new ParametersBuilder().addBonds(CreateOrUpdateBond.fromBond(bond))
                .addRemovedBonds(bond.getId())
                .build();
        HostSetupNetworksValidator validator = new HostSetupNetworksValidatorBuilder()
                .setParams(params)
                .build();

        EngineMessage engineMessage = EngineMessage.BONDS_UPDATED_AND_REMOVED_SIMULTANEOUSLY;
        assertThat(validator.bondNotUpdatedAndRemovedSimultaneously(), failsWith(engineMessage,
                ReplacementUtils.getListVariableAssignmentString(engineMessage,
                        Collections.singletonList(bond.getName()))));
    }

    @Test
    public void testBondNameNumPatternValid() {
        host.setClusterCompatibilityVersion(Version.v4_2);
        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBond(null, "bond1", "slaveA", "slaveB");
        doTestValidModifiedBonds(
                createOrUpdateBond,
                ValidationResult.VALID,
                ValidationResult.VALID,
                ValidationResult.VALID);
    }

    @Test
    public void testBondNameNumPatternInvalid() {
        host.setClusterCompatibilityVersion(Version.v4_2);
        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBond(null, "bond_fancy", "slaveA", "slaveB");
        doTestValidModifiedBonds(
                createOrUpdateBond,
                ValidationResult.VALID,
                new ValidationResult(EngineMessage.NETWORK_BOND_NAME_BAD_FORMAT,
                        ReplacementUtils.getVariableAssignmentString(EngineMessage.NETWORK_BOND_NAME_BAD_FORMAT,
                                createOrUpdateBond.getName())),
                ValidationResult.VALID);
    }

    @Test
    public void testBondNamePatternValid() {
        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBond(null, "bond_fancy", "slaveA", "slaveB");
        doTestValidModifiedBonds(
                createOrUpdateBond,
                ValidationResult.VALID,
                ValidationResult.VALID,
                ValidationResult.VALID);
    }

    @Test
    public void testBondNamePatternInvalid() {
        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBond(null, "fancy_bond", "slaveA", "slaveB");
        doTestValidModifiedBonds(
                createOrUpdateBond,
                ValidationResult.VALID,
                new ValidationResult(EngineMessage.NETWORK_BOND_NAME_BAD_FORMAT,
                        ReplacementUtils.getVariableAssignmentString(EngineMessage.NETWORK_BOND_NAME_BAD_FORMAT,
                                createOrUpdateBond.getName())),
                ValidationResult.VALID);
    }

    @Test
    public void testBondNamePatternUnicode() {
        CreateOrUpdateBond createOrUpdateBond = createNewCreateOrUpdateBond(null, "bond_מפואר", "slaveA", "slaveB");
        doTestValidModifiedBonds(
                createOrUpdateBond,
                ValidationResult.VALID,
                new ValidationResult(EngineMessage.NETWORK_BOND_NAME_BAD_FORMAT,
                        ReplacementUtils.getVariableAssignmentString(EngineMessage.NETWORK_BOND_NAME_BAD_FORMAT,
                                createOrUpdateBond.getName())),
                ValidationResult.VALID);
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
        AnonymousHostNetworkQos qos = createHostNetworkQos(10, 10, 10);


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
        networkAttachment.setHostNetworkQos(new AnonymousHostNetworkQos());

        return new HostSetupNetworksValidatorBuilder()
            .setParams(new ParametersBuilder().addNetworkAttachments(networkAttachment))
            .addNetworks(network)
            .build();
    }

    private AnonymousHostNetworkQos createHostNetworkQos(
            int outAverageRealtime,
            int outAverageUpperlimit,
            int outAverageLinkshare) {
        AnonymousHostNetworkQos qos = new AnonymousHostNetworkQos();
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
            EngineMessage engineMessage = EngineMessage.NETWORK_SHOULD_BE_ATTACHED_VIA_LABEL_TO_ANOTHER_NIC;
            assertThat(validator.validateAttachmentAndNicReferenceSameLabelNotConflict(attachment),
                failsWith(engineMessage,
                    ReplacementUtils.getVariableAssignmentString(engineMessage, network.getName()),
                    ReplacementUtils.createSetVariableString("nicName", attachment.getNicName()),
                    ReplacementUtils.createSetVariableString("labeledNicName", nicLabel.getNicName())));
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

        public ParametersBuilder addBonds(CreateOrUpdateBond... createOrUpdateBonds) {
            if (nullParameters(createOrUpdateBonds)) {
                return this;
            }

            if (parameters.getCreateOrUpdateBonds() == null) {
                parameters.setCreateOrUpdateBonds(new ArrayList<>());
            }

            parameters.getCreateOrUpdateBonds().addAll(Arrays.asList(createOrUpdateBonds));
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
                null,
                new BusinessEntityMap<>(networks),
                networkClusterDaoMock,
                networkDaoMock,
                vdsDaoMock,
                findActiveVmsUsingNetwork,
                new HostSetupNetworksValidatorHelper(),
                mockNetworkExclusivenessValidatorResolver,
                mockNetworkAttachmentIpConfigurationValidator,
                new UnmanagedNetworkValidator(),
                backendInternal);
        }
    }
}

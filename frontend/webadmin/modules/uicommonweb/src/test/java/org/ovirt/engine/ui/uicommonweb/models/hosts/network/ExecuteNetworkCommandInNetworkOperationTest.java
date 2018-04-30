package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIMessages;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExecuteNetworkCommandInNetworkOperationTest {

    @Mock
    private LogicalNetworkModel logicalNetworkModelOfNetworkA;

    @Mock
    private LogicalNetworkModel logicalNetworkModelOfNetworkC;

    @Mock
    private BondNetworkInterfaceModel bondNetworkInterfaceModelA;

    @Mock
    private BondNetworkInterfaceModel bondNetworkInterfaceModelB;

    private Network networkA = createNetwork("networkA"); //$NON-NLS-1$
    private Network networkC = createNetwork("networkC"); //$NON-NLS-1$

    @Mock
    private NetworkInterfaceModel networkInterfaceModelOfNicA;

    @Mock
    private NetworkInterfaceModel networkInterfaceModelOfNicB;

    @Mock
    private NetworkInterfaceModel networkInterfaceModelOfNicC;

    @Mock
    private NetworkInterfaceModel networkInterfaceModelOfNicD;

    private VdsNetworkInterface nicA = createNic("nicA"); //$NON-NLS-1$
    private VdsNetworkInterface nicB = createNic("nicB"); //$NON-NLS-1$
    private VdsNetworkInterface nicC = createNic("nicC"); //$NON-NLS-1$
    private VdsNetworkInterface nicD = createNic("nicD"); //$NON-NLS-1$

    private static final Guid existingBondId = Guid.newGuid();
    private static final String existingBondName = "existingBond"; //$NON-NLS-1$
    private Bond existingBond = createBond(existingBondId, existingBondName, Arrays.asList(nicA, nicB)).toBond();
    private CreateOrUpdateBond newlyCreatedBond =
            createBond(null, "newlyCreatedBond", Collections.emptyList()); //$NON-NLS-1$

    @Mock
    private HostSetupNetworksModel setupModel;

    private DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel =
            new DataFromHostSetupNetworksModel();

    @BeforeEach
    public void setUp() {
        when(logicalNetworkModelOfNetworkA.getNetwork()).thenReturn(networkA);
        when(logicalNetworkModelOfNetworkC.getNetwork()).thenReturn(networkC);

        when(logicalNetworkModelOfNetworkA.getSetupModel()).thenReturn(setupModel);
        when(logicalNetworkModelOfNetworkC.getSetupModel()).thenReturn(setupModel);

        when(networkInterfaceModelOfNicA.getOriginalIface()).thenReturn(nicA);
        when(networkInterfaceModelOfNicB.getOriginalIface()).thenReturn(nicB);
        when(networkInterfaceModelOfNicC.getOriginalIface()).thenReturn(nicC);
        when(networkInterfaceModelOfNicD.getOriginalIface()).thenReturn(nicD);

        when(networkInterfaceModelOfNicA.getName()).thenReturn(nicA.getName());
        when(networkInterfaceModelOfNicB.getName()).thenReturn(nicB.getName());
        when(networkInterfaceModelOfNicC.getName()).thenReturn(nicC.getName());
        when(networkInterfaceModelOfNicD.getName()).thenReturn(nicD.getName());

        when(setupModel.getHostSetupNetworksParametersData()).thenReturn(dataFromHostSetupNetworksModel);

        // mock manager/resolver so it's possible to delegate from one NetworkOperation to another.
        ConstantsManager constantsManagerMock = mock(ConstantsManager.class);
        UIMessages uiMessagesMock = mock(UIMessages.class);
        when(constantsManagerMock.getMessages()).thenReturn(uiMessagesMock);
        when(uiMessagesMock.detachNetwork(any())).thenReturn("doh"); //$NON-NLS-1$
        ConstantsManager.setInstance(constantsManagerMock);
        TypeResolver typeResolverMock = mock(TypeResolver.class);
        TypeResolver.setInstance(typeResolverMock);
    }

    /*
     * At the beginning there was a void, then NetworkAttachment was created attaching given network and nic.
     */
    @Test
    public void testCreatingBrandNewNetworkAttachment() {
        initNetworkIdToExistingAttachmentIdMap();

        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(false);

        NetworkOperation.ATTACH_NETWORK.getTarget().executeNetworkCommand(
                logicalNetworkModelOfNetworkA,
                networkInterfaceModelOfNicA,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(1));
        NetworkAttachment networkAttachment = dataFromHostSetupNetworksModel.getNetworkAttachments().iterator().next();
        assertNetworkAttachment(networkAttachment, null, networkA.getId(), nicA.getId());

        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().isEmpty(), is(true));
    }

    /*
     * At the beginning there was a NetworkAttachment. Suddenly network was detached from the nic, but in the end,
     * network was back attached to the nic unchanged.
     */
    @Test
    public void testReattachingPreexistingNetworkAfterItsBeingDetached() {
        NetworkAttachment attachment = createAttachmentOnNetworkModelAndUpdateParams(networkInterfaceModelOfNicA, logicalNetworkModelOfNetworkA);
        initNetworkIdToExistingAttachmentIdMap(attachment);

        NetworkOperation.DETACH_NETWORK.getTarget().executeNetworkCommand(
                logicalNetworkModelOfNetworkA,
                null,
                dataFromHostSetupNetworksModel);

        NetworkOperation.ATTACH_NETWORK.getTarget().executeNetworkCommand(
                logicalNetworkModelOfNetworkA,
                networkInterfaceModelOfNicA,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(1));

        NetworkAttachment updatedNetworkAttachment =
                dataFromHostSetupNetworksModel.getNetworkAttachments().iterator().next();
        assertNetworkAttachment(updatedNetworkAttachment, attachment.getId(), networkA.getId(), nicA.getId());

        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(0));
    }

    /*
     * At the beginning there was a NetworkAttachment. Suddenly network was detached from the nic, and gets finally
     * attached to another nic.
     */
    @Test
    public void testReattachingPreexistingNetworkToDifferentNicAfterItsBeingDetached() {
        NetworkAttachment attachment = createAttachmentOnNetworkModelAndUpdateParams(networkInterfaceModelOfNicA, logicalNetworkModelOfNetworkA);
        initNetworkIdToExistingAttachmentIdMap(attachment);

        NetworkOperation.DETACH_NETWORK.getTarget().executeNetworkCommand(
                logicalNetworkModelOfNetworkA,
                null,
                dataFromHostSetupNetworksModel);

        NetworkOperation.ATTACH_NETWORK.getTarget().executeNetworkCommand(
                logicalNetworkModelOfNetworkA,
                networkInterfaceModelOfNicB,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(1));
        assertNetworkAttachment(dataFromHostSetupNetworksModel.getNetworkAttachments().iterator().next(),
                attachment.getId(),
                networkA.getId(),
                nicB.getId());
    }

    /*
     * At the beginning there was a NetworkAttachment, and network gets detached from the nic.
     */
    @Test
    public void testDetachingPreexistingNetworkAttachment() {
        NetworkAttachment attachment = createAttachmentOnNetworkModelAndUpdateParams(networkInterfaceModelOfNicA,
                logicalNetworkModelOfNetworkA);
        Guid attachmentId = attachment.getId();

        NetworkOperation.DETACH_NETWORK.getTarget().executeNetworkCommand(
                logicalNetworkModelOfNetworkA,
                null,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(1));
        Guid removedNetworkAttachmentId =
                dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().iterator().next();
        assertThat("id mismatch", removedNetworkAttachmentId, is(attachmentId)); //$NON-NLS-1$
    }

    /*
     * At the beginning there was a void, then NetworkAttachment was created attaching given network with nic, and then
     * her was immediately detached from him.
     */
    @Test
    public void testDetachingPreviouslyAddedNetworkAttachment() {
        initNetworkIdToExistingAttachmentIdMap();

        NetworkOperation.ATTACH_NETWORK.getTarget().executeNetworkCommand(
                logicalNetworkModelOfNetworkA,
                networkInterfaceModelOfNicB,
                dataFromHostSetupNetworksModel);

        setAttachmentOnNetworkModel(networkInterfaceModelOfNicB,
                logicalNetworkModelOfNetworkA,
                dataFromHostSetupNetworksModel.getNetworkAttachments().iterator().next());

        NetworkOperation.DETACH_NETWORK.getTarget().executeNetworkCommand(
                logicalNetworkModelOfNetworkA,
                null,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(0));
    }

    /*
     * At the beginning there was a bond, which was then broken.
     */
    @Test
    public void testBreakingExistingBond() {
        CreateOrUpdateBond createOrUpdateBond = CreateOrUpdateBond.fromBond(existingBond);

        dataFromHostSetupNetworksModel.getBonds().add(createOrUpdateBond);

        when(bondNetworkInterfaceModelA.getItems()).thenReturn(Collections.emptyList());
        when(bondNetworkInterfaceModelA.getCreateOrUpdateBond()).thenReturn(createOrUpdateBond);

        NetworkOperation.BREAK_BOND.getTarget().executeNetworkCommand(
                bondNetworkInterfaceModelA,
                null,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.getBonds().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getRemovedBonds().size(), is(1));

        Guid removedBondId = dataFromHostSetupNetworksModel.getRemovedBonds().iterator().next();
        assertThat("id mismatch", removedBondId, is(existingBond.getId())); //$NON-NLS-1$
    }

    /*
     * At the beginning there was a void, then bond was created and was immediately broken.
     */
    @Test
    public void testBreakingNewlyCreatedBond() {
        initOrginalBondNameToIdMap();

        NetworkOperation.BOND_WITH.getTarget().executeNetworkCommand(
                networkInterfaceModelOfNicA,
                networkInterfaceModelOfNicB,
                dataFromHostSetupNetworksModel,
                newlyCreatedBond);

        when(bondNetworkInterfaceModelA.getItems()).thenReturn(Collections.emptyList());
        when(bondNetworkInterfaceModelA.getCreateOrUpdateBond())
                .thenReturn(dataFromHostSetupNetworksModel.getBonds().iterator().next());

        NetworkOperation.BREAK_BOND.getTarget().executeNetworkCommand(
                bondNetworkInterfaceModelA,
                null,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.getBonds().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getRemovedBonds().size(), is(0));
    }

    /*
     * At the beginning there was a bond with one network attachment, which was then broken.
     */
    @Test
    public void testBreakingExistingBondWithNetworkAttached() {
        addBondToParamsAndModel(existingBond,
                bondNetworkInterfaceModelA,
                Collections.singletonList(logicalNetworkModelOfNetworkA));
        NetworkAttachment networkAttachment = createAttachmentOnNetworkModelAndUpdateParams(bondNetworkInterfaceModelA,
                logicalNetworkModelOfNetworkA);

        NetworkOperation.BREAK_BOND.getTarget().executeNetworkCommand(
                bondNetworkInterfaceModelA,
                null,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(1));
        Guid removedNetworkAttachmentId =
                dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().iterator().next();
        assertThat("id mismatch", removedNetworkAttachmentId, is(networkAttachment.getId())); //$NON-NLS-1$

        assertThat(dataFromHostSetupNetworksModel.getBonds().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getRemovedBonds().size(), is(1));
        Guid removedBondId = dataFromHostSetupNetworksModel.getRemovedBonds().iterator().next();

        assertThat("id mismatch", removedBondId, is(existingBond.getId())); //$NON-NLS-1$
    }

    /*
     * At the beginning there were two nics (one with NetworkAttachment), which, after being introduced to each other,
     * formed a firm bond adopting NetworkAttachment as their own.
     */
    @Test
    public void testBondingTwoNicsWithReattachingNetworkAttachmentOnNewlyCreatedBond() {
        NetworkAttachment attachment = createAttachmentOnNetworkModelAndUpdateParams(networkInterfaceModelOfNicA,
                logicalNetworkModelOfNetworkA);
        initNetworkIdToExistingAttachmentIdMap(attachment);
        initOrginalBondNameToIdMap();

        when(networkInterfaceModelOfNicA.getItems())
                .thenReturn(Collections.singletonList(logicalNetworkModelOfNetworkA));

        NetworkOperation.BOND_WITH.getTarget().executeNetworkCommand(
                networkInterfaceModelOfNicA,
                networkInterfaceModelOfNicB,
                dataFromHostSetupNetworksModel,
                newlyCreatedBond);

        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(false);
        when(bondNetworkInterfaceModelA.getOriginalIface()).thenReturn(newlyCreatedBond.toBond());

        // this is not part of BOND_WITH command, it's simply called after it. BOND_WITH is actually: "detach networks
        // and create bond".
        // in production code, probably due to some problems with listeners, this is actually called three times,
        // luckily each time overwriting previous call.
        NetworkOperation.attachNetworks(bondNetworkInterfaceModelA,
                Collections.singletonList(logicalNetworkModelOfNetworkA),
                dataFromHostSetupNetworksModel);

        // related network attachment will be updated, not removed and created new one.
        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(1));
        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(0));
        assertNetworkAttachment(dataFromHostSetupNetworksModel.getNetworkAttachments().iterator().next(),
                attachment.getId(),
                networkA.getId(),
                newlyCreatedBond.getId());

        assertThat(dataFromHostSetupNetworksModel.getBonds().size(), is(1));
        CreateOrUpdateBond newOrModifiedBond = dataFromHostSetupNetworksModel.getBonds().iterator().next();
        assertBond(newOrModifiedBond, null, Arrays.asList(nicA, nicB));
        assertThat(dataFromHostSetupNetworksModel.getRemovedBonds().size(), is(0));
    }

    private void initNetworkIdToExistingAttachmentIdMap(NetworkAttachment... attachments) {
        Map<Guid, Guid> networkIdToExistingAttachmentId = new HashMap<>();
        for (NetworkAttachment attachment : attachments) {
            networkIdToExistingAttachmentId.put(attachment.getNetworkId(), attachment.getId());
        }
        dataFromHostSetupNetworksModel.setNetworkIdToExistingAttachmentId(networkIdToExistingAttachmentId);
    }

    private void initOrginalBondNameToIdMap(CreateOrUpdateBond... bonds) {
        dataFromHostSetupNetworksModel.setOriginalBondsByName(Entities.entitiesByName(Arrays.asList(bonds)));
    }

    /*
     * At the beginning there was bond. It was broken into two nics, but they get back together in the end.
     */
    @Test
    public void testReBondingTwoNicsWithReattachingNetworkAttachmentOnNewlyCreatedBond() {
        addBondToParamsAndModel(existingBond,
                bondNetworkInterfaceModelA,
                Collections.emptyList());

        initOrginalBondNameToIdMap(CreateOrUpdateBond.fromBond(existingBond));

        NetworkOperation.BREAK_BOND.getTarget().executeNetworkCommand(
                bondNetworkInterfaceModelA,
                null,
                dataFromHostSetupNetworksModel);

        NetworkOperation.BOND_WITH.getTarget().executeNetworkCommand(
                networkInterfaceModelOfNicA,
                networkInterfaceModelOfNicB,
                dataFromHostSetupNetworksModel,
                createBond(null, existingBondName, Collections.emptyList()));

        // related network attachment will be updated, not removed and created new one.
        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.getBonds().size(), is(1));
        CreateOrUpdateBond newOrModifiedBond = dataFromHostSetupNetworksModel.getBonds().iterator().next();
        assertBond(newOrModifiedBond, existingBondId, Arrays.asList(nicA, nicB));
        assertThat(dataFromHostSetupNetworksModel.getRemovedBonds().size(), is(0));
    }

    /*
     * At the beginning there was bond without any network attachment. Then another nic showed up joining the family
     * bringing his own network attachment along.
     */
    @Test
    public void testAddingNewNicWithNetworkAttachmentToExistingBondWithoutAnyAttachment() {
        addBondToParamsAndModel(existingBond,
                bondNetworkInterfaceModelA,
                Collections.emptyList());

        NetworkAttachment networkAttachment = createAttachmentOnNetworkModelAndUpdateParams(networkInterfaceModelOfNicC,
                logicalNetworkModelOfNetworkC);
        when(networkInterfaceModelOfNicC.getItems())
                .thenReturn(Collections.singletonList(logicalNetworkModelOfNetworkC));

        initNetworkIdToExistingAttachmentIdMap(networkAttachment);

        NetworkOperation.ADD_TO_BOND.getTarget().executeNetworkCommand(
                networkInterfaceModelOfNicC,
                bondNetworkInterfaceModelA,
                dataFromHostSetupNetworksModel);

        // related network attachment will be updated, not removed and created new one.
        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(1));
        assertNetworkAttachment(dataFromHostSetupNetworksModel.getNetworkAttachments().iterator().next(),
                networkAttachment.getId(),
                networkC.getId(),
                existingBondId);
        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.getBonds().size(), is(1));
        CreateOrUpdateBond newOrModifiedBond = dataFromHostSetupNetworksModel.getBonds().iterator().next();
        assertBond(newOrModifiedBond, existingBondId, Arrays.asList(nicA, nicB, nicC));
        assertThat(dataFromHostSetupNetworksModel.getRemovedBonds().size(), is(0));
    }

    /*
     * At the beginning there was a bond with three slaves, the one of them left others.
     */
    @Test
    public void testRemoveSlaveFromBond() {
        existingBond.getSlaves().add(nicC.getName());
        addBondToParamsAndModel(existingBond,
                bondNetworkInterfaceModelA,
                Collections.emptyList());
        when(bondNetworkInterfaceModelA.getSlaves())
                .thenReturn(Arrays.asList(networkInterfaceModelOfNicA,
                        networkInterfaceModelOfNicB,
                        networkInterfaceModelOfNicC));

        NetworkAttachment networkAttachment = createAttachmentOnNetworkModelAndUpdateParams(bondNetworkInterfaceModelA,
                logicalNetworkModelOfNetworkA);

        when(networkInterfaceModelOfNicA.getBond()).thenReturn(bondNetworkInterfaceModelA);

        NetworkOperation.REMOVE_FROM_BOND.getTarget().executeNetworkCommand(
                networkInterfaceModelOfNicA,
                null,
                dataFromHostSetupNetworksModel);

        // related network attachment will be updated, not removed and created new one.
        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(1));
        Guid networkAttachmentId = dataFromHostSetupNetworksModel.getNetworkAttachments().iterator().next().getId();
        assertThat("id mismatch", networkAttachmentId, is(networkAttachment.getId())); //$NON-NLS-1$

        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.getBonds().size(), is(1));
        CreateOrUpdateBond newOrModifiedBond = dataFromHostSetupNetworksModel.getBonds().iterator().next();
        assertBond(newOrModifiedBond, existingBondId, Arrays.asList(nicB, nicC));
        assertThat(dataFromHostSetupNetworksModel.getRemovedBonds().size(), is(0));
    }

    /*
     * At the beginning there was a bond with two slaves, the one of them left others.
     */
    @Test
    public void testRemoveSlaveFromBondWithTwoSlaves() {
        addBondToParamsAndModel(existingBond,
                bondNetworkInterfaceModelA,
                Collections.emptyList());
        when(bondNetworkInterfaceModelA.getSlaves())
                .thenReturn(Arrays.asList(networkInterfaceModelOfNicA, networkInterfaceModelOfNicB));

        when(bondNetworkInterfaceModelA.getItems()).thenReturn(Collections.singletonList(logicalNetworkModelOfNetworkA));
        NetworkAttachment networkAttachment = createAttachmentOnNetworkModelAndUpdateParams(bondNetworkInterfaceModelA,
                logicalNetworkModelOfNetworkA);

        when(networkInterfaceModelOfNicA.getBond()).thenReturn(bondNetworkInterfaceModelA);

        NetworkOperation.REMOVE_FROM_BOND.getTarget().executeNetworkCommand(
                networkInterfaceModelOfNicA,
                null,
                dataFromHostSetupNetworksModel);

        // related network attachment will be updated, not removed and created new one.
        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(1));
        Guid removedNetworkAttachmentId =
                dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().iterator().next();
        assertThat("id mismatch", removedNetworkAttachmentId, is(networkAttachment.getId())); //$NON-NLS-1$

        assertThat(dataFromHostSetupNetworksModel.getBonds().size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.getRemovedBonds().size(), is(1));
        Guid removedBondId = dataFromHostSetupNetworksModel.getRemovedBonds().iterator().next();
        assertThat("id mismatch", removedBondId, is(existingBond.getId())); //$NON-NLS-1$
    }

    /*
     * Two existing bonds are joined. The newly created bond will have a new name.
     */
    @Test
    public void testJoiningBondsNotReusingName() {
        joinBondsTest(false);
    }

    /*
     * Two existing bonds are joined. The newly created bond will have the same name as one of the bonds if was created
     * from.
     */
    @Test
    public void testJoiningBondsReusingName() {
        joinBondsTest(true);
    }

    /*
     * At the beginning there were two bonds (one with NetworkAttachment), which, after being introduced to each other,
     * formed a firm bond adopting NetworkAttachment as their own.
     */
    private void joinBondsTest(boolean resusingName) {
        addBondToParamsAndModel(existingBond,
                bondNetworkInterfaceModelA,
                Collections.emptyList());
        NetworkAttachment networkAttachment = createAttachmentOnNetworkModelAndUpdateParams(bondNetworkInterfaceModelA,
                logicalNetworkModelOfNetworkA);

        Bond bondB = createBond(Guid.newGuid(), "bondB", Arrays.asList(nicC, nicD)).toBond(); //$NON-NLS-1$
        addBondToParamsAndModel(bondB, bondNetworkInterfaceModelB, Collections.singletonList(logicalNetworkModelOfNetworkA));

        initOrginalBondNameToIdMap(bondNetworkInterfaceModelA.getCreateOrUpdateBond(),
                bondNetworkInterfaceModelB.getCreateOrUpdateBond());

        when(bondNetworkInterfaceModelA.getItems())
                .thenReturn(Collections.singletonList(logicalNetworkModelOfNetworkA));
        when(bondNetworkInterfaceModelA.getSlaves()).thenReturn(Arrays.asList(networkInterfaceModelOfNicA,
                networkInterfaceModelOfNicB));

        when(bondNetworkInterfaceModelB.getItems()).thenReturn(Collections.emptyList());
        when(bondNetworkInterfaceModelB.getSlaves())
                .thenReturn(Arrays.asList(networkInterfaceModelOfNicC, networkInterfaceModelOfNicD));

        if (resusingName) {
            newlyCreatedBond.setName(existingBondName);
        }

        NetworkOperation.JOIN_BONDS.getTarget().executeNetworkCommand(
                bondNetworkInterfaceModelA,
                bondNetworkInterfaceModelB,
                dataFromHostSetupNetworksModel,
                newlyCreatedBond);

        // NetworkOperation.JOIN_BONDS only detaches the attachments, the attachments are being re-attached only in
        // HostSetupNetworksModel (after clicking ok in the new bond dialog)
        assertThat(dataFromHostSetupNetworksModel.getNetworkAttachments().size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().size(), is(1));
        Guid removeNetworkAttachmentId =
                dataFromHostSetupNetworksModel.getRemovedNetworkAttachments().iterator().next();
        assertThat("id mismatch", removeNetworkAttachmentId, is(networkAttachment.getId())); //$NON-NLS-1$

        assertThat(dataFromHostSetupNetworksModel.getBonds().size(), is(1));
        CreateOrUpdateBond newOrModifiedBond = dataFromHostSetupNetworksModel.getBonds().iterator().next();
        assertBond(newOrModifiedBond, resusingName ? existingBondId : null, Arrays.asList(nicA, nicB, nicC, nicD));

        assertThat(dataFromHostSetupNetworksModel.getRemovedBonds().size(), is(resusingName ? 1 : 2));
        assertThat("id mismatch", dataFromHostSetupNetworksModel.getRemovedBonds().contains(existingBondId), is(!resusingName)); //$NON-NLS-1$
        assertThat("id mismatch", dataFromHostSetupNetworksModel.getRemovedBonds().contains(bondB.getId()), is(true)); //$NON-NLS-1$
    }

    private void assertBond(CreateOrUpdateBond bond, Guid bondId, List<VdsNetworkInterface> slaves) {
        assertThat("id mismatch", bond.getId(), is(bondId)); //$NON-NLS-1$

        for (VdsNetworkInterface slave : slaves) {
            assertThat("missing slave", bond.getSlaves().contains(slave.getName()), is(true)); //$NON-NLS-1$
        }
        assertThat("invalid slaves count", bond.getSlaves().size(), is(slaves.size())); //$NON-NLS-1$
    }

    private void assertNetworkAttachment(NetworkAttachment networkAttachment,
            Guid attachmentId,
            Guid networkId,
            Guid nicId) {
        assertThat("id mismatch", networkAttachment.getId(), is(attachmentId)); //$NON-NLS-1$
        assertThat("network id mismatch", networkAttachment.getNetworkId(), equalTo(networkId)); //$NON-NLS-1$
        assertThat("nicId mismatch", networkAttachment.getNicId(), equalTo(nicId)); //$NON-NLS-1$
    }

    private Network createNetwork(String networkA) {
        Network result = new Network();

        result.setId(Guid.newGuid());
        result.setName(networkA);

        return result;
    }

    private VdsNetworkInterface createNic(String nicName) {
        VdsNetworkInterface result = new VdsNetworkInterface();

        result.setId(Guid.newGuid());
        result.setName(nicName);

        return result;
    }

    private CreateOrUpdateBond createBond(Guid id, String bondName, List<VdsNetworkInterface> slaves) {
        CreateOrUpdateBond result = new CreateOrUpdateBond();

        result.setId(id);
        result.setName(bondName);

        for (VdsNetworkInterface slave : slaves) {
            result.getSlaves().add(slave.getName());
        }

        return result;
    }

    private NetworkAttachment createNetworkAttachment(Guid id, VdsNetworkInterface baseNic, Network network) {
        NetworkAttachment networkAttachment =
                new NetworkAttachment(baseNic, network, NetworkCommonUtils.createDefaultIpConfiguration());
        networkAttachment.setId(id);
        return networkAttachment;
    }

    private NetworkAttachment createAttachmentOnNetworkModelAndUpdateParams(NetworkInterfaceModel nicModel,
            LogicalNetworkModel networkModel) {
        NetworkAttachment attachment =
                createNetworkAttachment(Guid.newGuid(), nicModel.getOriginalIface(), networkModel.getNetwork());
        dataFromHostSetupNetworksModel.getNetworkAttachments().add(attachment);

        setAttachmentOnNetworkModel(nicModel, networkModel, attachment);

        return attachment;
    }

    private void setAttachmentOnNetworkModel(NetworkInterfaceModel nicModel,
            LogicalNetworkModel networkModel,
            NetworkAttachment attachment) {
        when(networkModel.getNetworkAttachment()).thenReturn(attachment);
        when(networkModel.getAttachedToNic()).thenReturn(nicModel);
        when(networkModel.isAttached()).thenReturn(true);
    }

    private void addBondToParamsAndModel(Bond bond,
            BondNetworkInterfaceModel bondModel,
            List<LogicalNetworkModel> networks) {
        CreateOrUpdateBond createOrUpdateBond = CreateOrUpdateBond.fromBond(bond);
        dataFromHostSetupNetworksModel.getBonds().add(createOrUpdateBond);

        when(bondModel.getOriginalIface()).thenReturn(bond);
        when(bondModel.getCreateOrUpdateBond()).thenReturn(createOrUpdateBond);
        when(bondModel.getItems()).thenReturn(networks);
    }
}

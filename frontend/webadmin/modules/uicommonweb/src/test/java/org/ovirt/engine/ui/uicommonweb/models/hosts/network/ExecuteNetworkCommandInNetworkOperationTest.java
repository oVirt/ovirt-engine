package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIMessages;

@RunWith(MockitoJUnitRunner.class)
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
    private Bond existingBond = createBond(existingBondId, existingBondName, Arrays.asList(nicA, nicB));
    private Bond newlyCreatedBond = createBond(null, "newlyCreatedBond", Collections.<VdsNetworkInterface>emptyList()); //$NON-NLS-1$

    private List<VdsNetworkInterface> allNics = new ArrayList<>();

    private List<NetworkAttachment> existingNetworkAttachments = new ArrayList<>();

    private Set<String> networksToSync = new HashSet<>();

    private DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel =
            new DataFromHostSetupNetworksModel(allNics, existingNetworkAttachments, networksToSync);

    @Before
    public void setUp() throws Exception {
        when(logicalNetworkModelOfNetworkA.getNetwork()).thenReturn(networkA);
        when(logicalNetworkModelOfNetworkC.getNetwork()).thenReturn(networkC);
        when(networkInterfaceModelOfNicA.getIface()).thenReturn(nicA);
        when(networkInterfaceModelOfNicB.getIface()).thenReturn(nicB);
        when(networkInterfaceModelOfNicC.getIface()).thenReturn(nicC);
        when(networkInterfaceModelOfNicD.getIface()).thenReturn(nicD);

        //mock manager/resolver so it's possible to delegate from one NetworkOperation to another.
        ConstantsManager constantsManagerMock = Mockito.mock(ConstantsManager.class);
        UIMessages uiMessagesMock = Mockito.mock(UIMessages.class);
        when(constantsManagerMock.getMessages()).thenReturn(uiMessagesMock);
        when(uiMessagesMock.detachNetwork(anyString())).thenReturn("doh"); //$NON-NLS-1$
        ConstantsManager.setInstance(constantsManagerMock);
        TypeResolver typeResolverMock = Mockito.mock(TypeResolver.class);
        TypeResolver.setInstance(typeResolverMock);
    }

    /*
     * At the beginning there was a void, then NetworkAttachment was created attaching given network and nic.
     * */
    @Test
    public void testCreatingBrandNewNetworkAttachment() throws Exception {
        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(false);

        NetworkOperation.ATTACH_NETWORK.getTarget().executeNetworkCommand(
                logicalNetworkModelOfNetworkA,
                networkInterfaceModelOfNicA,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(1));
        NetworkAttachment networkAttachment = dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.iterator().next();
        assertNetworkAttachment(networkAttachment, null, networkA.getId(), nicA.getId());

        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.isEmpty(), is(true));
    }

    /*
     * At the beginning there was a NetworkAttachment. Suddenly network was detached from the nic, but in the end,
     * network was back attached to the nic unchanged.
     * */
    @Test
    public void testReattachingPreexistingNetworkAfterItsBeingDetached() throws Exception {
        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(false);

        Guid networkAttachmentId = Guid.newGuid();
        NetworkAttachment networkAttachment =
            NetworkOperation.newNetworkAttachment(networkA,
                nicA,
                null,
                networkAttachmentId,
                dataFromHostSetupNetworksModel.networksToSync,
                null);
        existingNetworkAttachments.add(networkAttachment);
        dataFromHostSetupNetworksModel.removedNetworkAttachments.add(networkAttachment);

        NetworkOperation.ATTACH_NETWORK.getTarget().executeNetworkCommand(
            logicalNetworkModelOfNetworkA,
            networkInterfaceModelOfNicA,
            dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(1));

        NetworkAttachment updatedNetworkAttachment = dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.iterator().next();
        assertNetworkAttachment(updatedNetworkAttachment, networkAttachmentId, networkA.getId(), nicA.getId());

        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(0));
    }

    /*
     * At the beginning there was a NetworkAttachment. Suddenly network was detached from the nic, and gets firmly
     * attached to another nic.
     * */
    @Test
    public void testReattachingPreexistingNetworkToDifferentNicAfterItsBeingDetached() throws Exception {
        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(false);
        when(networkInterfaceModelOfNicA.getIface()).thenReturn(nicB);

        Guid networkAttachmentId = Guid.newGuid();
        NetworkAttachment formerAttachment =
            NetworkOperation.newNetworkAttachment(networkA,
                nicA,
                null,
                networkAttachmentId,
                dataFromHostSetupNetworksModel.networksToSync,
                null);
        existingNetworkAttachments.add(formerAttachment);
        dataFromHostSetupNetworksModel.removedNetworkAttachments.add(formerAttachment);

        NetworkOperation.ATTACH_NETWORK.getTarget().executeNetworkCommand(
            logicalNetworkModelOfNetworkA,
            networkInterfaceModelOfNicA,
            dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(1));
        assertNetworkAttachment(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.iterator().next(),
                networkAttachmentId,
                networkA.getId(),
                nicB.getId());
    }

    /*
     * At the beginning there was a NetworkAttachment, and network gets detached from the nic.
     * */
    @Test
    public void testDetachingPreexistingNetworkAttachment() throws Exception {
        Guid networkAttachmentId = Guid.newGuid();
        NetworkAttachment networkAttachment =
            NetworkOperation.newNetworkAttachment(networkA,
                nicA,
                null,
                networkAttachmentId,
                dataFromHostSetupNetworksModel.networksToSync,
                null);
        existingNetworkAttachments.add(networkAttachment);
        when(logicalNetworkModelOfNetworkA.hasVlan()).thenReturn(false);
        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(true);
        when(logicalNetworkModelOfNetworkA.getAttachedToNic()).thenReturn(networkInterfaceModelOfNicA);


        NetworkOperation.DETACH_NETWORK.getTarget().executeNetworkCommand(
            logicalNetworkModelOfNetworkA,
            null,
            dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(1));
        Guid removedNetworkAttachmentId = dataFromHostSetupNetworksModel.removedNetworkAttachments.iterator().next().getId();
        assertThat("id mismatch", removedNetworkAttachmentId, is(networkAttachmentId)); //$NON-NLS-1$
    }

    /*
     * At the beginning there was a void, then NetworkAttachment was created attaching given network with nic,
     * and then her was immediately detached from him.
     * */
    @Test
    public void testDetachingPreviouslyAddedNetworkAttachment() throws Exception {
        NetworkAttachment networkAttachment =
                NetworkOperation.newNetworkAttachment(networkA,
                        nicA,
                        null,
                        dataFromHostSetupNetworksModel.networksToSync);
        dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.add(networkAttachment);
        when(logicalNetworkModelOfNetworkA.hasVlan()).thenReturn(false);
        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(true);
        when(logicalNetworkModelOfNetworkA.getAttachedToNic()).thenReturn(networkInterfaceModelOfNicA);

        NetworkOperation.DETACH_NETWORK.getTarget().executeNetworkCommand(
                logicalNetworkModelOfNetworkA,
                null,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(0));
    }

    /*
     * At the beginning there was a bond, which was then broken.
     * */
    @Test
    public void testBreakingExistingBond() throws Exception {
        when(bondNetworkInterfaceModelA.getItems()).thenReturn(Collections.<LogicalNetworkModel> emptyList());
        when(bondNetworkInterfaceModelA.getIface()).thenReturn(existingBond);

        NetworkOperation.BREAK_BOND.getTarget().executeNetworkCommand(
                bondNetworkInterfaceModelA,
                null,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedBonds.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedBonds.size(), is(1));

        Guid removedBondId = dataFromHostSetupNetworksModel.removedBonds.iterator().next().getId();
        assertThat("id mismatch", removedBondId, is(existingBond.getId())); //$NON-NLS-1$
    }

    /*
     * At the beginning there was a void, then bond was created and was immediately broken.
     * */
    @Test
    public void testBreakingNewlyCreatedBond() throws Exception {
        when(bondNetworkInterfaceModelA.getItems()).thenReturn(Collections.<LogicalNetworkModel> emptyList());
        when(bondNetworkInterfaceModelA.getIface()).thenReturn(newlyCreatedBond);

        NetworkOperation.BREAK_BOND.getTarget().executeNetworkCommand(
                bondNetworkInterfaceModelA,
                null,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedBonds.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedBonds.size(), is(0));
    }

    /*
     * At the beginning there was a bond, which was then broken.
     * */
    @Test
    public void testBreakingExistingBondWithNetworkAttached() throws Exception {
        NetworkAttachment networkAttachment =
                NetworkOperation.newNetworkAttachment(networkA,
                        existingBond,
                        null,
                        dataFromHostSetupNetworksModel.networksToSync);

        existingNetworkAttachments.add(networkAttachment);
        when(logicalNetworkModelOfNetworkA.hasVlan()).thenReturn(false);
        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(true);
        when(logicalNetworkModelOfNetworkA.getAttachedToNic()).thenReturn(networkInterfaceModelOfNicA);


        when(bondNetworkInterfaceModelA.getItems()).thenReturn(Collections.singletonList(logicalNetworkModelOfNetworkA));
        when(bondNetworkInterfaceModelA.getIface()).thenReturn(existingBond);

        NetworkOperation.BREAK_BOND.getTarget().executeNetworkCommand(
                bondNetworkInterfaceModelA,
                null,
                dataFromHostSetupNetworksModel);

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(1));
        Guid removedNetworkAttachmentId = dataFromHostSetupNetworksModel.removedNetworkAttachments.iterator().next().getId();
        assertThat("id mismatch", removedNetworkAttachmentId, nullValue()); //$NON-NLS-1$

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedBonds.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedBonds.size(), is(1));
        Guid removedBondId = dataFromHostSetupNetworksModel.removedBonds.iterator().next().getId();

        assertThat("id mismatch", removedBondId, is(existingBond.getId())); //$NON-NLS-1$
    }

    /*
     * At the beginning there was two nics (one with NetworkAttachment), which, after being introduced to each other,
     * formed a firm bond adopting NetworkAttachment as their own.
     * */
    @Test
    public void testBondingTwoNicsWithReattachingNetworkAttachmentOnNewlyCreatedBond() throws Exception {
        Guid networkAttachmentId = Guid.newGuid();
        NetworkAttachment networkAttachment =
            NetworkOperation.newNetworkAttachment(networkA,
                nicA,
                null,
                networkAttachmentId,
                dataFromHostSetupNetworksModel.networksToSync,
                null);

        existingNetworkAttachments.add(networkAttachment);
        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(true);
        when(logicalNetworkModelOfNetworkA.getAttachedToNic()).thenReturn(networkInterfaceModelOfNicA);

        when(networkInterfaceModelOfNicA.getItems()).thenReturn(Collections.singletonList(logicalNetworkModelOfNetworkA));

        when(networkInterfaceModelOfNicC.getIface()).thenReturn(newlyCreatedBond);
        when(networkInterfaceModelOfNicC.getItems()).thenReturn(Collections.<LogicalNetworkModel> emptyList());
        when(networkInterfaceModelOfNicC.getName()).thenReturn(newlyCreatedBond.getName());

        NetworkOperation.BOND_WITH.getTarget().executeNetworkCommand(
                networkInterfaceModelOfNicA,
                networkInterfaceModelOfNicB,
                dataFromHostSetupNetworksModel,
                newlyCreatedBond);

        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(false);

        //this is not part of BOND_WITH command, it's simply called after it. BOND_WITH is actually: "detach networks and create bond".
        //in production code, probably due to some problems with listeners, this is actually called three times, luckily each time overwriting previous call.
        NetworkOperation.attachNetworks(networkInterfaceModelOfNicC,
                Collections.singletonList(logicalNetworkModelOfNetworkA),
                dataFromHostSetupNetworksModel);

        //related network attachment will be updated, not removed and created new one.
        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(1));
        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(0));
        assertNetworkAttachment(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.iterator().next(),
                networkAttachmentId,
                networkA.getId(),
                newlyCreatedBond.getId());

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedBonds.size(), is(1));
        assertBond(dataFromHostSetupNetworksModel.newOrModifiedBonds.iterator().next(),
                null,
                Arrays.asList(nicA, nicB));
        assertThat(dataFromHostSetupNetworksModel.removedBonds.size(), is(0));
    }

    /*
     * At the beginning there was bond. It was broken into two nics, but they get back together in the end.
     * */
    @Test
    public void testReBondingTwoNicsWithReattachingNetworkAttachmentOnNewlyCreatedBond() throws Exception {

        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(false);

        dataFromHostSetupNetworksModel.removedBonds.add(existingBond);

        NetworkOperation.BOND_WITH.getTarget().executeNetworkCommand(
                networkInterfaceModelOfNicA,
                networkInterfaceModelOfNicB,
                dataFromHostSetupNetworksModel,
                createBond(existingBondId, existingBondName, Collections.<VdsNetworkInterface> emptyList()));

        //related network attachment will be updated, not removed and created new one.
        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedBonds.size(), is(1));
        assertBond(dataFromHostSetupNetworksModel.newOrModifiedBonds.iterator().next(),
                existingBondId,
                Arrays.asList(nicA, nicB));
        assertThat(dataFromHostSetupNetworksModel.removedBonds.size(), is(0));
    }

    /*
     * At the beginning there was bond without any network attachment. Then another nic showed up joining the family
     * bringing his own network attachment along.
     * */
    @Test
    public void testAddingNewNicWithNetworkAttachmentToExistingBondWithoutAnyAttachment() throws Exception {
        Guid networkAttachmentId = Guid.newGuid();
        NetworkAttachment networkAttachment =
            NetworkOperation.newNetworkAttachment(networkC,
                nicC,
                null,
                networkAttachmentId,
                dataFromHostSetupNetworksModel.networksToSync,
                null);

        existingNetworkAttachments.add(networkAttachment);

        //this can be confusing. network *is* attached but it gets detached as a part of ADD_TO_BOND, so consulting this method, it will be detached.
        when(logicalNetworkModelOfNetworkC.isAttached()).thenReturn(true, false);
        when(logicalNetworkModelOfNetworkC.getAttachedToNic()).thenReturn(networkInterfaceModelOfNicC, (NetworkInterfaceModel)null);

        when(networkInterfaceModelOfNicC.getItems()).thenReturn(Collections.singletonList(logicalNetworkModelOfNetworkC));

        when(bondNetworkInterfaceModelA.getItems()).thenReturn(Collections.<LogicalNetworkModel> emptyList());
        when(bondNetworkInterfaceModelA.getIface()).thenReturn(existingBond);


        NetworkOperation.ADD_TO_BOND.getTarget().executeNetworkCommand(
                networkInterfaceModelOfNicC,
                bondNetworkInterfaceModelA,
                dataFromHostSetupNetworksModel);

        //related network attachment will be updated, not removed and created new one.
        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(1));
        assertNetworkAttachment(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.iterator().next(),
                networkAttachmentId,
                networkC.getId(),
                existingBondId);
        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedBonds.size(), is(1));
        assertBond(dataFromHostSetupNetworksModel.newOrModifiedBonds.iterator().next(),
                existingBondId,
                Arrays.asList(nicA, nicB, nicC));
        assertThat(dataFromHostSetupNetworksModel.removedBonds.size(), is(0));
    }

    /*
    * At the beginning there was a bond with three slaves, the one of them left others.
    * */
    @Test
    public void testRemoveSlaveFromBond() throws Exception {

        Bond bond = createBond(existingBondId, existingBondName, Arrays.asList(nicA, nicB, nicC));


        Guid networkAttachmentId = Guid.newGuid();
        NetworkAttachment networkAttachment =
            NetworkOperation.newNetworkAttachment(networkA,
                bond,
                null,
                networkAttachmentId,
                dataFromHostSetupNetworksModel.networksToSync,
                null);

        existingNetworkAttachments.add(networkAttachment);

        when(bondNetworkInterfaceModelA.getIface()).thenReturn(bond);

        when(networkInterfaceModelOfNicA.getBond()).thenReturn(bondNetworkInterfaceModelA);

        when(bondNetworkInterfaceModelA.getIface()).thenReturn(bond);


        NetworkOperation.REMOVE_FROM_BOND.getTarget().executeNetworkCommand(
                networkInterfaceModelOfNicA,
                null,
                dataFromHostSetupNetworksModel);

        //related network attachment will be updated, not removed and created new one.
        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(0));

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedBonds.size(), is(1));
        assertBond(dataFromHostSetupNetworksModel.newOrModifiedBonds.iterator().next(),
                existingBondId,
                Arrays.asList(nicB, nicC));
        assertThat(dataFromHostSetupNetworksModel.removedBonds.size(), is(0));
    }

    /*
     * At the beginning there was two nics (one with NetworkAttachment), which, after being introduced to each other,
     * formed a firm bond adopting NetworkAttachment as their own.
     * */
    @Test
    public void testJoiningBonds() throws Exception {
        Guid networkAttachmentId = Guid.newGuid();
        NetworkAttachment networkAttachment =
            NetworkOperation.newNetworkAttachment(networkA,
                existingBond,
                null,
                networkAttachmentId, dataFromHostSetupNetworksModel.networksToSync,
                null);

        Guid bondBId = Guid.newGuid();
        Bond bondB = createBond(bondBId, "bondB", Arrays.asList(nicC, nicD)); //$NON-NLS-1$

        existingNetworkAttachments.add(networkAttachment);
        when(logicalNetworkModelOfNetworkA.isAttached()).thenReturn(true);
        when(logicalNetworkModelOfNetworkA.getAttachedToNic()).thenReturn(networkInterfaceModelOfNicA);

        when(bondNetworkInterfaceModelA.getItems()).thenReturn(Collections.singletonList(logicalNetworkModelOfNetworkA));
        when(bondNetworkInterfaceModelA.getIface()).thenReturn(existingBond);
        when(bondNetworkInterfaceModelA.getBonded()).thenReturn(Arrays.asList(networkInterfaceModelOfNicA,
            networkInterfaceModelOfNicB));
        when(bondNetworkInterfaceModelB.getItems()).thenReturn(Collections.<LogicalNetworkModel>emptyList());
        when(bondNetworkInterfaceModelB.getIface()).thenReturn(bondB);
        when(bondNetworkInterfaceModelB.getBonded()).thenReturn(Arrays.asList(networkInterfaceModelOfNicC, networkInterfaceModelOfNicD));

        NetworkOperation.JOIN_BONDS.getTarget().executeNetworkCommand(
                bondNetworkInterfaceModelA,
                bondNetworkInterfaceModelB,
                dataFromHostSetupNetworksModel,
                newlyCreatedBond);

        assertThat(dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.size(), is(0));
        assertThat(dataFromHostSetupNetworksModel.removedNetworkAttachments.size(), is(1));
        Guid removeNetworkAttachmentId = dataFromHostSetupNetworksModel.removedNetworkAttachments.iterator().next().getId();
        assertThat("id mismatch", removeNetworkAttachmentId, is(networkAttachmentId)); //$NON-NLS-1$




        assertThat(dataFromHostSetupNetworksModel.newOrModifiedBonds.size(), is(1));
        assertBond(dataFromHostSetupNetworksModel.newOrModifiedBonds.iterator().next(),
                null,
                Arrays.asList(nicA, nicB, nicC, nicD));
        assertThat(dataFromHostSetupNetworksModel.removedBonds.size(), is(2));

        Iterator<Bond> removedBondsIterator = dataFromHostSetupNetworksModel.removedBonds.iterator();
        Guid firstRemovedBondId = removedBondsIterator.next().getId();
        assertThat("id mismatch", firstRemovedBondId, is(existingBondId)); //$NON-NLS-1$

        Guid secondRemovedBondId = removedBondsIterator.next().getId();
        assertThat("id mismatch", secondRemovedBondId, is(bondBId)); //$NON-NLS-1$
    }

    private void assertBond(Bond bond, Guid bondId, List<VdsNetworkInterface> slaves) {
        List<VdsNetworkInterface> existingNics = new ArrayList<>();
        existingNics.add(bond);
        existingNics.addAll(slaves);

        NetworkCommonUtils.fillBondSlaves(existingNics);
        Matcher attachmentIdMatcher = bondId == null ? nullValue() : is(bondId);
        assertThat("id mismatch", bond.getId(), attachmentIdMatcher); //$NON-NLS-1$

        for (VdsNetworkInterface slave : slaves) {
            assertThat("missing slave", bond.getSlaves().contains(slave.getName()), is(true)); //$NON-NLS-1$
        }
        assertThat("invalid slaves count", bond.getSlaves().size(), is(slaves.size())); //$NON-NLS-1$
    }

    private void assertNetworkAttachment(NetworkAttachment networkAttachment,
            Guid attachmentId,
            Guid networkId,
            Guid nicId) {
        Matcher attachmentIdMatcher = attachmentId == null ? nullValue() : is(attachmentId);
        assertThat("id mismatch", networkAttachment.getId(), attachmentIdMatcher); //$NON-NLS-1$
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

    private Bond createBond(Guid id, String bondName, List<VdsNetworkInterface> slaves) {
        Bond result = new Bond();

        result.setId(id);
        result.setName(bondName);

        for (VdsNetworkInterface slave : slaves) {
            slave.setBondName(bondName);
        }

        return result;
    }
}

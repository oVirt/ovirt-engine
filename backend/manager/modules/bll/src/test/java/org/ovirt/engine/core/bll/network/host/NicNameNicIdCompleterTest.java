package org.ovirt.engine.core.bll.network.host;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.network.host.NicNameNicIdCompleter.NicNameAndNicIdAccessors;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class NicNameNicIdCompleterTest {
    private VdsNetworkInterface nic;
    private NicNameNicIdCompleter completer;

    @BeforeEach
    public void setUp() {
        nic = new VdsNetworkInterface();
        nic.setId(Guid.newGuid());
        nic.setName("existingNic");

        List<VdsNetworkInterface> nics = Collections.singletonList(nic);
        completer = new NicNameNicIdCompleter(nics);
    }

    @Test
    public void testProperNetworkAttachmentBindingToNicNameAndNicIdAccessors() {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNicId(Guid.newGuid());
        networkAttachment.setNicName("nic name");

        NicNameAndNicIdAccessors accessors = new NicNameAndNicIdAccessors.FromNetworkAttachment(networkAttachment);
        assertThat("id should be bound to nicId", accessors.getId(), is(networkAttachment.getNicId()));
        assertThat("name should be bound to nicName", accessors.getName(), is(networkAttachment.getNicName()));

        accessors.setId(Guid.newGuid());
        accessors.setName("another name");
        assertThat("id should be bound to nicId", networkAttachment.getNicId(), is(accessors.getId()));
        assertThat("name should be bound to nicName", networkAttachment.getNicName(), is(accessors.getName()));
    }

    @Test
    public void testProperBondBindingToNicNameAndNicIdAccessors() {
        CreateOrUpdateBond createOrUpdateBond = new CreateOrUpdateBond();
        createOrUpdateBond.setName("nic name");
        createOrUpdateBond.setId(Guid.newGuid());

        NicNameAndNicIdAccessors accessors = new NicNameAndNicIdAccessors.FromCreateOrUpdateBondData(createOrUpdateBond);
        assertThat(accessors.getId(), is(createOrUpdateBond.getId()));
        assertThat(accessors.getName(), is(createOrUpdateBond.getName()));

        accessors.setId(Guid.newGuid());
        accessors.setName("another name");
        assertThat(createOrUpdateBond.getId(), is(accessors.getId()));
        assertThat(createOrUpdateBond.getName(), is(accessors.getName()));
    }

    @Test
    public void testCompleteWhenUnsetIdAndName() {
        NicNameAndNicIdAccessors withoutNameOrIdSet = mock(NicNameAndNicIdAccessors.class);
        completer.complete(withoutNameOrIdSet);
        verify(withoutNameOrIdSet, never()).setName(any());
        verify(withoutNameOrIdSet, never()).setId(any());
    }

    @Test
    public void testCompleteWhenBothIdAndNameDoesNotReferenceExistingNic() {
        NicNameAndNicIdAccessors accessors = mock(NicNameAndNicIdAccessors.class);

        Guid guidOfNotExistingNic = Guid.newGuid();
        when(accessors.getId()).thenReturn(guidOfNotExistingNic);
        when(accessors.getName()).thenReturn("notAExistingNicName");

        completer.complete(accessors);
        verify(accessors, never()).setName(any());
        verify(accessors, never()).setId(any());
    }

    @Test
    public void testCompleteWhenNicIdReferencesExistingNic() {
        TestAccessors withIdSet = new TestAccessors();
        withIdSet.setId(nic.getId());

        completer.complete(withIdSet);
        assertThat(withIdSet.getName(), is(nic.getName()));
        assertThat(withIdSet.getId(), is(nic.getId()));
    }

    @Test
    public void testCompleteWhenNicNameReferencesExistingNic() {
        TestAccessors withNameSet = new TestAccessors();
        withNameSet.setName(nic.getName());

        completer.complete(withNameSet);
        assertThat(withNameSet.getName(), is(nic.getName()));
        assertThat(withNameSet.getId(), is(nic.getId()));
    }

    @Test
    public void testCompleteWhenNicNameAndNicIdAreIncoherent() {
        TestAccessors accessors = new TestAccessors();
        Guid id = Guid.newGuid();
        String name = nic.getName();

        accessors.setId(id);
        accessors.setName(name);

        completer.complete(accessors);

        //no errors, no changes'; inconsistency has to be treated elsewhere.
        assertThat(accessors.getId(), is(id));
        assertThat(accessors.getName(), is(name));
    }

    private static class TestAccessors implements NicNameAndNicIdAccessors {

        private String name;
        private Guid id;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public TestAccessors setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Guid getId() {
            return id;
        }

        @Override
        public TestAccessors setId(Guid id) {
            this.id = id;
            return this;
        }
    }

}

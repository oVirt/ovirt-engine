package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

// this class contains only tests for the isDisplayAddressPartiallyOverridden method
public class IsDisplayAddressConsistentInClusterQueryTest extends
        AbstractUserQueryTest<IdQueryParameters, IsDisplayAddressConsistentInClusterQuery<IdQueryParameters>> {

    // create data point with 1,2,3 and many VDS
    private enum ConsoleTestAddresses {
        ONE(1),
        TWO(2),
        THREE(3),
        MANY(20);

        private int numConsoles;

        ConsoleTestAddresses(int numConsoles) {
            this.numConsoles = numConsoles;
        }

        public int getNumConsoles() {
            return numConsoles;
        }
    }

    @Test
    public void nullHostsAreNotMismatched() {
        assertThat(getQuery().isDisplayAddressPartiallyOverridden(null), is(false));
    }

    @Test
    public void emptyHostsAreNotMismatched() {
        assertThat(getQuery().isDisplayAddressPartiallyOverridden(new ArrayList<>()), is(false));
    }

    @ParameterizedTest
    @EnumSource(ConsoleTestAddresses.class)
    public void whenAllHostsAreDefaultTheyAreNotMismatched(ConsoleTestAddresses c) {
        assertThat(getQuery().isDisplayAddressPartiallyOverridden(new DefaultConsoleAddress(c.getNumConsoles()).getAllVds()),
                is(false));
    }

    @ParameterizedTest
    @EnumSource(ConsoleTestAddresses.class)
    public void whenAllHostsAreOverriddenTheyAreNotMismatched(ConsoleTestAddresses c) {
        assertThat(getQuery().isDisplayAddressPartiallyOverridden(new OverriddenConsoleAddress(c.getNumConsoles()).getAllVds()),
                is(false));
    }

    @ParameterizedTest
    @MethodSource
    public void anyCombinationOfDefaulfAndOverriddenHostsAreMismatched(DefaultConsoleAddress defaultAddress, OverriddenConsoleAddress overriddenAddress) {
        List<VDS> mergedAddresses = new ArrayList<>();
        mergedAddresses.addAll(defaultAddress.getAllVds());
        mergedAddresses.addAll(overriddenAddress.getAllVds());
        assertThat(getQuery().isDisplayAddressPartiallyOverridden(mergedAddresses), is(true));
    }

    public static Stream<Arguments> anyCombinationOfDefaulfAndOverriddenHostsAreMismatched() {
        // Permute every default and overridden addresses:
        return Arrays.stream(ConsoleTestAddresses.values())
                .flatMap(d -> Arrays.stream(ConsoleTestAddresses.values())
                        .map(o -> Arguments.of(
                                new DefaultConsoleAddress(d.getNumConsoles()),
                                new OverriddenConsoleAddress(o.getNumConsoles()))));
    }

    private abstract static class BaseVdsContainer {

        private List<VDS> content;

        public BaseVdsContainer(int numOfVds) {
            for (int i = 0; i < numOfVds; i++) {
                addVds();
            }
        }

        protected abstract void addVds();

        public void addVds(String returnValue) {
            if (content == null) {
                content = new ArrayList<>();
            }

            VDS vds = mock(VDS.class);
            when(vds.getConsoleAddress()).thenReturn(returnValue);
            content.add(vds);
        }

        public List<VDS> getAllVds() {
            return content;
        }
    }

    private static class OverriddenConsoleAddress extends BaseVdsContainer {

        public OverriddenConsoleAddress(int numOfVds) {
            super(numOfVds);
        }

        @Override
        protected void addVds() {
            super.addVds("some overridden value"); //$NON-NLS-1$
        }
    }

    private static class DefaultConsoleAddress extends BaseVdsContainer {

        public DefaultConsoleAddress(int numOfVds) {
            super(numOfVds);
        }

        @Override
        protected void addVds() {
            super.addVds(null);
        }
    }
}

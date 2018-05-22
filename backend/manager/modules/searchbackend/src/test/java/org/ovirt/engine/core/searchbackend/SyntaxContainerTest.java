package org.ovirt.engine.core.searchbackend;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SyntaxContainerTest {

    /**
     * Make sure this searchText produces a search on the related tag table as well
     * For search on users the tag table is also used for user's VMs.
     * @param searchText the search expression
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "Users: type=user and vm.id=12345678-1234-1234-1234-1234-123456789012",
            "Users: type=user and tag = foo",
            "Hosts: tag=foo"
    })
    void searchUsingTags(String searchText) {
        SyntaxChecker sc = new SyntaxChecker();
        assertTrue(sc.analyzeSyntaxState(searchText, false).isSearchUsingTags());
    }

    /**
     * Make sure this searchText DOES NOT use tag, i.e will not produce a join on the related
     * tag table. Those searches, since they are code-generated, are sub-optimal and we wan't to avoid them
     * when they are not needed. Specifically the general default searches for a default UI grids must
     * be snappy and perform well.
     * @param searchText the search expression
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "Users:",
            "Users: type=user",
            "Users: type=group",
            "Hosts:",
            "Vms:",
            "Datacenters:",
            "Clusters:"
    })
    void searchNotUsingTags(String searchText) {
        SyntaxChecker sc = new SyntaxChecker();
        assertFalse(sc.analyzeSyntaxState(searchText, false).isSearchUsingTags());
    }
}

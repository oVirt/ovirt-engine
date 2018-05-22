package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SyntaxContainerTest {

    private static final boolean USING_TAGS = true;
    private static final boolean NOT_USING_TAGS = false;

    private boolean expected;
    private String input;

    public SyntaxContainerTest(boolean expected, String input) {
        this.expected = expected;
        this.input = input;
    }

    @Parameterized.Parameters
    public static Object[] searchTexts() {
        return new Object[][]{
                {USING_TAGS, "Users: type=user and vm.id=12345678-1234-1234-1234-1234-123456789012"},
                {USING_TAGS, "Users: type=user and tag = foo"},
                {USING_TAGS, "Hosts: tag=foo"},
                {NOT_USING_TAGS, "Users: type=user"},
                {NOT_USING_TAGS, "Users: type=group"},
                {NOT_USING_TAGS, "Hosts:"},
                {NOT_USING_TAGS, "Vms:"},
                {NOT_USING_TAGS, "Datacenters:"},
                {NOT_USING_TAGS, "Clusters:"},
        };
    }

    /**
     * Make sure this searchText produces a search on the related tag table as well.
     * For search on users the tag table is also used for user's VMs.
     *
     * On other cases, make sure the searchText DOES NOT use tag, i.e will not produce a join on the related
     * tag table. Those searches, since they are code-generated, are sub-optimal and we wan't to avoid them
     * when they are not needed. Specifically the general default searches for a default UI grids must
     * be snappy and perform well.
     */
    @Test
    public void searchUsingTags() {
        assertEquals(expected, new SyntaxChecker().analyzeSyntaxState(input, false).isSearchUsingTags());
    }

}

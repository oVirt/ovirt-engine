package org.ovirt.engine.ui.common.binding;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ElementIdStatementTest {

    private ElementIdStatement tested;

    @Before
    public void setUp() {
        tested = new ElementIdStatement("owner.a.b.c", "abcId");
    }

    @Test
    public void buildIdSetterStatement() {
        assertThat(tested.buildIdSetterStatement(),
                equalTo("setElementId(owner.a.b.c, \"abcId\")"));
    }

    @Test
    public void buildGuardCondition() {
        assertThat(tested.buildGuardCondition(), equalTo(
                "owner != null && owner.a != null && owner.a.b != null && owner.a.b.c != null"));
    }

    @Test
    public void getSubPaths_singlePathElement() {
        String[] subPaths = tested.getSubPaths("a");
        assertThat(subPaths.length, equalTo(1));
        assertThat(subPaths[0], equalTo("a"));
    }

    @Test
    public void getSubPaths_multiplePathElements() {
        String[] subPaths = tested.getSubPaths("a.b.c");
        assertThat(subPaths.length, equalTo(3));
        assertThat(subPaths[0], equalTo("a"));
        assertThat(subPaths[1], equalTo("a.b"));
        assertThat(subPaths[2], equalTo("a.b.c"));
    }

    @Test(expected = IllegalStateException.class)
    public void getSubPaths_malformedPath() {
        tested.getSubPaths(".a.");
    }

}

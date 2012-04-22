package org.ovirt.engine.ui.common.binding;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ElementIdStatementTest {

    private ElementIdStatement tested;

    @Before
    public void setUp() {
        tested = new ElementIdStatement("owner.a.b.c", "abcId"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void buildIdSetterStatement() {
        assertThat(tested.buildIdSetterStatement(),
                equalTo("setElementId(owner.a.b.c, \"abcId\")")); //$NON-NLS-1$
    }

    @Test
    public void buildGuardCondition() {
        assertThat(tested.buildGuardCondition(), equalTo(
                "owner != null && owner.a != null && owner.a.b != null && owner.a.b.c != null")); //$NON-NLS-1$
    }

    @Test
    public void getSubPaths_singlePathElement() {
        String[] subPaths = tested.getSubPaths("a"); //$NON-NLS-1$
        assertThat(subPaths.length, equalTo(1));
        assertThat(subPaths[0], equalTo("a")); //$NON-NLS-1$
    }

    @Test
    public void getSubPaths_multiplePathElements() {
        String[] subPaths = tested.getSubPaths("a.b.c"); //$NON-NLS-1$
        assertThat(subPaths.length, equalTo(3));
        assertThat(subPaths[0], equalTo("a")); //$NON-NLS-1$
        assertThat(subPaths[1], equalTo("a.b")); //$NON-NLS-1$
        assertThat(subPaths[2], equalTo("a.b.c")); //$NON-NLS-1$
    }

    @Test(expected = IllegalStateException.class)
    public void getSubPaths_malformedPath() {
        tested.getSubPaths(".a."); //$NON-NLS-1$
    }

}

package org.ovirt.engine.ui.common.binding;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ElementIdStatementTest {

    private ElementIdStatement tested;

    @BeforeEach
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

    @Test
    public void getSubPaths_malformedPath() {
        assertThrows(IllegalStateException.class, () -> tested.getSubPaths(".a.")); //$NON-NLS-1$
    }

}

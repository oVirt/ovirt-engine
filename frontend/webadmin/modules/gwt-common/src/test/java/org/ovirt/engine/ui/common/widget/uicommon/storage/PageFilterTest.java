package org.ovirt.engine.ui.common.widget.uicommon.storage;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;

class PageFilterTest {

    private Collection<Integer> fiveElem = Arrays.asList(1, 2, 3, 4, 5);
    private Collection<Integer> fourElem = Arrays.asList(1, 2, 3, 4);

    @Test
    void disabledOnStart() {
        PageFilter pager = PageFilter.create(2);
        assertFalse(pager.canGoBack());
        assertFalse(pager.canGoForward());
        assertThat(pager.getFirstItemOnPage()).isEqualTo(-1);
        assertThat(pager.getLastItemOnPage()).isEqualTo(-1);
        assertThat(pager.getTotalItemsCount()).isEqualTo(0);
    }

    @Test
    void pageSizeZero() {
        assertThrows(IllegalArgumentException.class, () -> PageFilter.create(0));
    }

    @Test
    void nullItems() {
        assertNull(PageFilter.create(2).filter(null));
    }

    @Test
    void emptyItems() {
        assertThat(PageFilter.create(2).filter(emptyList())).isEmpty();
    }

    @Test
    void lastPageIsFull() {
        PageFilter pager = PageFilter.create(2);
        assertThat(pager.filter(fourElem)).containsExactly(1, 2);
        assertFalse(pager.canGoBack());
        assertTrue(pager.canGoForward());
        assertThat(pager.getFirstItemOnPage()).isEqualTo(0);
        assertThat(pager.getLastItemOnPage()).isEqualTo(1);
        assertThat(pager.getTotalItemsCount()).isEqualTo(4);

        pager.goForward();
        assertThat(pager.filter(fourElem)).containsExactly(3, 4);
        assertTrue(pager.canGoBack());
        assertFalse(pager.canGoForward());
        assertThat(pager.getFirstItemOnPage()).isEqualTo(2);
        assertThat(pager.getLastItemOnPage()).isEqualTo(3);
        assertThat(pager.getTotalItemsCount()).isEqualTo(4);
    }

    @Test
    void lastPagePartiallyFilled() {
        PageFilter pager = PageFilter.create(2);
        assertThat(pager.filter(fiveElem)).containsExactly(1, 2);
        assertFalse(pager.canGoBack());
        assertTrue(pager.canGoForward());
        assertThat(pager.getFirstItemOnPage()).isEqualTo(0);
        assertThat(pager.getLastItemOnPage()).isEqualTo(1);
        assertThat(pager.getTotalItemsCount()).isEqualTo(5);

        pager.goForward();
        assertThat(pager.filter(fiveElem)).containsExactly(3, 4);
        assertTrue(pager.canGoBack());
        assertTrue(pager.canGoForward());
        assertThat(pager.getFirstItemOnPage()).isEqualTo(2);
        assertThat(pager.getLastItemOnPage()).isEqualTo(3);
        assertThat(pager.getTotalItemsCount()).isEqualTo(5);

        pager.goForward();
        assertThat(pager.filter(fiveElem)).containsExactly(5);
        assertTrue(pager.canGoBack());
        assertFalse(pager.canGoForward());
        assertThat(pager.getFirstItemOnPage()).isEqualTo(4);
        assertThat(pager.getLastItemOnPage()).isEqualTo(4);
        assertThat(pager.getTotalItemsCount()).isEqualTo(5);
    }

    @Test
    void goForwardAndBack() {
        PageFilter pager = PageFilter.create(2);
        assertThat(pager.filter(fiveElem)).containsExactly(1, 2);

        pager.goForward();
        pager.goBack();

        assertThat(pager.filter(fiveElem)).containsExactly(1, 2);

        assertFalse(pager.canGoBack());
        assertTrue(pager.canGoForward());
        assertThat(pager.getFirstItemOnPage()).isEqualTo(0);
        assertThat(pager.getLastItemOnPage()).isEqualTo(1);
        assertThat(pager.getTotalItemsCount()).isEqualTo(5);
    }

    @Test
    void resetDueToItemsListChange() {
        PageFilter pager = PageFilter.create(2);
        assertThat(pager.filter(fourElem)).containsExactly(1, 2);

        pager.goForward();
        assertThat(pager.filter(fourElem)).containsExactly(3, 4);
        assertThat(pager.getTotalItemsCount()).isEqualTo(4);

        assertThat(pager.filter(fiveElem)).containsExactly(1, 2);
        assertFalse(pager.canGoBack());
        assertTrue(pager.canGoForward());
        assertThat(pager.getFirstItemOnPage()).isEqualTo(0);
        assertThat(pager.getLastItemOnPage()).isEqualTo(1);
        assertThat(pager.getTotalItemsCount()).isEqualTo(5);

    }
}

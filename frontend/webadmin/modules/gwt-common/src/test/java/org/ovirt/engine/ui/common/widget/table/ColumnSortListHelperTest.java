package org.ovirt.engine.ui.common.widget.table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.ui.common.widget.table.AbstractActionTable.ColumnSortListHelper.findInColumnSortList;
import static org.ovirt.engine.ui.common.widget.table.AbstractActionTable.ColumnSortListHelper.moveHeaderSortState;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;

import com.google.gwt.user.cellview.client.ColumnSortList;

class ColumnSortListHelperTest {

    private AbstractTextColumn<String> createSortableMock() {
        AbstractTextColumn<String> column = mock(AbstractTextColumn.class);
        when(column.isSortable()).thenReturn(true);
        return column;
    }

    @Test
    void noColumnFoundWhenNullSortList() {
        AbstractTextColumn<String> column = createSortableMock();
        assertThat(findInColumnSortList(null, column)).isEqualTo(-1);
    }

    @Test
    void noColumnFoundWhenNullColumn() {
        assertThat(findInColumnSortList(new ColumnSortList(), null)).isEqualTo(-1);
    }

    @Test
    void noColumnFoundInEmptyList() {
        ColumnSortList sortList = new ColumnSortList();
        AbstractTextColumn<String> column = createSortableMock();

        assertThat(findInColumnSortList(sortList, column)).isEqualTo(-1);
    }

    @Test
    void columnFound() {
        AbstractTextColumn<String> testColumn = createSortableMock();
        ColumnSortList sortList = new ColumnSortList();
        sortList.push(testColumn);
        sortList.push(createSortableMock());
        sortList.push(createSortableMock());

        assertThat(findInColumnSortList(sortList, testColumn)).isEqualTo(2);
    }

    @Test
    void columnFoundDespiteNullColumns() {
        AbstractTextColumn<String> testColumn = createSortableMock();
        ColumnSortList sortList = new ColumnSortList();
        sortList.push(testColumn);
        sortList.push(new ColumnSortList.ColumnSortInfo(null, true));

        assertThat(findInColumnSortList(sortList, testColumn)).isEqualTo(1);
    }

    @Test
    void nothingToSyncDueToEmptyList() {
        ColumnSortList sortList = new ColumnSortList();
        AbstractTextColumn<String> from = createSortableMock();
        AbstractTextColumn<String> to = createSortableMock();

        moveHeaderSortState(sortList, from, to);

        assertThat(sortList.size()).isZero();
    }

    @Test
    void nothingToSyncDueToBothMissing() {
        AbstractTextColumn<String> other = createSortableMock();
        AbstractTextColumn<String> from = createSortableMock();
        AbstractTextColumn<String> to = createSortableMock();
        ColumnSortList sortList = new ColumnSortList();
        sortList.push(other);

        moveHeaderSortState(sortList, from, to);

        assertThat(sortList.size()).isEqualTo(1);
        assertThat(sortList.get(0).getColumn()).isEqualTo(other);
    }

    @Test
    void nonSortableTargetIsSkipped() {
        AbstractTextColumn<String> from = createSortableMock();
        AbstractTextColumn<String> to = mock(AbstractTextColumn.class);
        when(to.isSortable()).thenReturn(false);
        ColumnSortList sortList = new ColumnSortList();
        sortList.push(from);

        moveHeaderSortState(sortList, from, to);

        assertThat(sortList.size()).isZero();
    }

    @Test
    void nullTargetIsSkipped() {
        AbstractTextColumn<String> from = createSortableMock();
        ColumnSortList sortList = new ColumnSortList();
        sortList.push(from);

        moveHeaderSortState(sortList, from, null);

        assertThat(sortList.size()).isZero();
    }

    @Test
    void nullSourceIsIgnored() {
        AbstractTextColumn<String> to = createSortableMock();
        ColumnSortList sortList = new ColumnSortList();
        sortList.push(to);

        moveHeaderSortState(sortList, null, to);

        assertThat(sortList.size()).isZero();
    }

    @Test
    void nullSortList() {
        AbstractTextColumn<String> from = createSortableMock();
        AbstractTextColumn<String> to = createSortableMock();

        try {
            moveHeaderSortState(null, from, to);
        } catch (Exception e) {
            fail("unexpected exception"); //$NON-NLS-1$
        }
    }

    @Test
    void sortOrderNotChangedAfterReplacingSource() {
        AbstractTextColumn<String> firstSortColumn = createSortableMock();
        AbstractTextColumn<String> from = createSortableMock();
        AbstractTextColumn<String> to = createSortableMock();
        ColumnSortList sortList = new ColumnSortList();
        sortList.push(new ColumnSortList.ColumnSortInfo(from, true));
        sortList.push(firstSortColumn);

        moveHeaderSortState(sortList, from, to);

        assertThat(sortList.size()).isEqualTo(2);
        assertThat(sortList.get(0).getColumn()).isEqualTo(firstSortColumn);
        assertThat(sortList.get(1).getColumn()).isEqualTo(to);
        assertThat(sortList.get(1).isAscending()).isTrue();
    }

    @Test
    void targetRemovedInsteadOfReplacingWhenSourceNotPartOfSortList() {
        AbstractTextColumn<String> secondSortColumn = createSortableMock();
        AbstractTextColumn<String> from = createSortableMock();
        AbstractTextColumn<String> to = createSortableMock();
        ColumnSortList sortList = new ColumnSortList();
        sortList.push(secondSortColumn);
        sortList.push(new ColumnSortList.ColumnSortInfo(to, false));

        moveHeaderSortState(sortList, from, to);

        assertThat(sortList.size()).isEqualTo(1);
        assertThat(sortList.get(0).getColumn()).isEqualTo(secondSortColumn);
    }

}

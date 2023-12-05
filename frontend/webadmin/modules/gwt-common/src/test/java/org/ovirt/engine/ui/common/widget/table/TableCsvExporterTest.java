package org.ovirt.engine.ui.common.widget.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;

@ExtendWith(MockitoExtension.class)
public class TableCsvExporterTest {
    private final List<Header<?>> headers = new ArrayList<>();
    private final List<AbstractColumn<VM, ?>> columns = new ArrayList<>();
    private final List<AbstractColumn<VM, ?>> visibleColumns = new ArrayList<>();
    private final List<List<VM>> pages = new ArrayList<>();

    private final Event<EventArgs> itemsChangedEvent = new Event<>("Name", TableCsvExporterTest.class); //$NON-NLS-1$

    private int currentPage;

    private TableCsvExporter<VM> exporter;

    @BeforeEach
    public void init() {
        GWTMockUtilities.disarm();
        SearchableTableModelProvider<VM, SearchableListModel> modelProvider = mock(SearchableTableModelProvider.class);
        AbstractCellTable<VM> table = mock(AbstractCellTable.class);
        SearchableListModel<?, VM> model = mock(SearchableListModel.class);
        ColumnController<VM> columnController = mock(ColumnController.class);

        AbstractColumn<VM, ?> statusIconCol = mockColumn(null);
        when(statusIconCol.getContextMenuTitle()).thenReturn("Status Icon"); //$NON-NLS-1$
        lenient().when(statusIconCol.getValue(any(VM.class))).thenReturn(null);
        lenient().when(statusIconCol.getTooltip(any(VM.class))).thenAnswer(invocationOnMock -> {
            VM vm = invocationOnMock.getArgument(0, VM.class);
            return mockHtml("<div>" + vm.getStatus().name() + "</div>"); //$NON-NLS-1$ //$NON-NLS-2$
        });
        AbstractColumn<VM, ?> nameCol = mockColumn("Name"); //$NON-NLS-1$
        lenient().when(nameCol.getValue(any(VM.class))).thenAnswer(invocationOnMock -> {
            VM vm = invocationOnMock.getArgument(0, VM.class);
            return vm.getName();
        });
        AbstractColumn<VM, ?> descriptionCol = mockColumn("Description"); //$NON-NLS-1$
        lenient().when(descriptionCol.getValue(any(VM.class))).thenAnswer(invocationOnMock -> {
            VM vm = invocationOnMock.getArgument(0, VM.class);
            return vm.getDescription();
        });
        AbstractColumn<VM, ?> statusCol = mockColumn("Status"); //$NON-NLS-1$
        lenient().when(statusCol.getValue(any(VM.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, VM.class));

        when(table.getColumnCount()).thenReturn(columns.size());
        when(table.getColumn(anyInt())).thenAnswer(invocationOnMock -> {
            int index = invocationOnMock.getArgument(0, Integer.class);
            return columns.get(index);
        });
        when(columnController.isColumnVisible(any())).thenAnswer(invocationOnMock -> {
            Column<?, ?> column = invocationOnMock.getArgument(0, Column.class);
            return visibleColumns.contains(column);
        });
        when(table.getHeader(anyInt())).thenAnswer(invocationOnMock -> {
            int index = invocationOnMock.getArgument(0, Integer.class);
            return headers.get(index);
        });

        when(modelProvider.getModel()).thenReturn(model);
        when(model.getItemsChangedEvent()).thenReturn(itemsChangedEvent);
        when(model.getItems()).thenAnswer(invocationOnMock -> {
            if (currentPage < 0 || currentPage >= pages.size()) {
                return Collections.emptyList();
            }
            return pages.get(currentPage);
        });
        when(modelProvider.canGoBack()).thenAnswer(invocationOnMock -> currentPage > 0);
        when(modelProvider.canGoForward()).thenAnswer(invocationOnMock -> currentPage < pages.size() - 1);
        lenient().doAnswer(invocationOnMock -> {
            currentPage--;
            lenient().when(model.getItems()).thenReturn(pages.get(currentPage));
            itemsChangedEvent.raise(null, null);
            return null;
        }).when(modelProvider).goBack();
        lenient().doAnswer(invocationOnMock -> {
            currentPage++;
            lenient().when(model.getItems()).thenReturn(pages.get(currentPage));
            itemsChangedEvent.raise(null, null);
            return null;
        }).when(modelProvider).goForward();

        exporter = new TableCsvExporter<>("vms", modelProvider, table, columnController); //$NON-NLS-1$
    }

    @AfterEach
    public void tearDown() {
        GWTMockUtilities.restore();
    }

    private AbstractColumn<VM, ?> mockColumn(String header) {
        AbstractColumn<VM, ?> column = mock(AbstractColumn.class);
        columns.add(column);
        visibleColumns.add(column);
        Header<String> hdr = mock(Header.class);
        if (header != null) {
            lenient().when(hdr.getValue()).thenReturn(header);
        }
        headers.add(hdr);
        return column;
    }

    private VM mockItem(VMStatus status, String name, String description) {
        VM item = mock(VM.class);
        lenient().when(item.getStatus()).thenReturn(status);
        lenient().when(item.getName()).thenReturn(name);
        lenient().when(item.getDescription()).thenReturn(description);
        return item;
    }

    private SafeHtml mockHtml(String html) {
        SafeHtml result = mock(SafeHtml.class);
        when(result.asString()).thenReturn(html);
        return result;
    }

    private void mockPage() {
        pages.add(Arrays.asList(
                mockItem(VMStatus.Down, "vm1", " descr1 with \n spec '\" symbols "), //$NON-NLS-1$ //$NON-NLS-2$
                mockItem(VMStatus.Up, "vm2", ""), //$NON-NLS-1$ //$NON-NLS-2$
                mockItem(VMStatus.Paused, "vm3", null)//$NON-NLS-1$
        ));
    }

    @Test
    public void exportEmptyTableTest() {
        exporter.generateCsv();
        assertGeneratedCsv("Status Icon,Name,Description,Status\n"); //$NON-NLS-1$
    }

    @Test
    public void exportOnePageTableTest() {
        mockPage();
        exporter.generateCsv();
        assertGeneratedCsv("Status Icon,Name,Description,Status\n" + //$NON-NLS-1$
                "Down,vm1,\"descr1 with   spec '\"\" symbols\",Down\n" + //$NON-NLS-1$
                "Up,vm2,,Up\n" + //$NON-NLS-1$
                "Paused,vm3,,Paused\n"); //$NON-NLS-1$
        assertEquals(0, currentPage);
    }

    @Test
    public void exportMultiplePagesTableTest() {
        mockPage();
        mockPage();
        exporter.generateCsv();
        assertGeneratedCsv("Status Icon,Name,Description,Status\n" + //$NON-NLS-1$
                "Down,vm1,\"descr1 with   spec '\"\" symbols\",Down\n" + //$NON-NLS-1$
                "Up,vm2,,Up\n" + //$NON-NLS-1$
                "Paused,vm3,,Paused\n" + //$NON-NLS-1$
                "Down,vm1,\"descr1 with   spec '\"\" symbols\",Down\n" + //$NON-NLS-1$
                "Up,vm2,,Up\n" + //$NON-NLS-1$
                "Paused,vm3,,Paused\n"); //$NON-NLS-1$
        assertEquals(0, currentPage);
    }

    @Test
    public void exportMultiplePagesTableFromNonFirtsPageTest() {
        mockPage();
        mockPage();
        mockPage();
        currentPage = 2;
        exporter.generateCsv();
        assertGeneratedCsv("Status Icon,Name,Description,Status\n" + //$NON-NLS-1$
                "Down,vm1,\"descr1 with   spec '\"\" symbols\",Down\n" + //$NON-NLS-1$
                "Up,vm2,,Up\n" + //$NON-NLS-1$
                "Paused,vm3,,Paused\n" + //$NON-NLS-1$
                "Down,vm1,\"descr1 with   spec '\"\" symbols\",Down\n" + //$NON-NLS-1$
                "Up,vm2,,Up\n" + //$NON-NLS-1$
                "Paused,vm3,,Paused\n" + //$NON-NLS-1$
                "Down,vm1,\"descr1 with   spec '\"\" symbols\",Down\n" + //$NON-NLS-1$
                "Up,vm2,,Up\n" + //$NON-NLS-1$
                "Paused,vm3,,Paused\n"); //$NON-NLS-1$
        assertEquals(2, currentPage);
    }

    @Test
    public void exportTableWithInvisibleColumnsTest() {
        visibleColumns.remove(2);
        mockPage();
        exporter.generateCsv();
        assertGeneratedCsv("Status Icon,Name,Status\n" + //$NON-NLS-1$
                "Down,vm1,Down\n" + //$NON-NLS-1$
                "Up,vm2,Up\n" + //$NON-NLS-1$
                "Paused,vm3,Paused\n"); //$NON-NLS-1$
        assertEquals(0, currentPage);
    }

    private void assertGeneratedCsv(String expectedCsv) {
        assertEquals(expectedCsv, exporter.getGeneratedCsv());
    }
}

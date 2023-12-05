package org.ovirt.engine.ui.common.widget.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;

/**
 * A utility class that allows to export content of any {@link ActionCellTable} to CSV
 * <p>
 * This class allows to export content of any {@link ActionCellTable} with {@link SearchableTableModelProvider} to CSV:
 * <ul>
 *     <li>It takes into account current columns visibility. Only visible columns are exported</li>
 *     <li>Current sorting configuration would be applied to the exported content</li>
 *     <li>Current filtering configuration would be applied to the exported content</li>
 *     <li>It generates exported CSV file name in the following way: filenameBase.currentDateAndTime.csv, where the
 *     filenameBase is provided by {@link SearchableTableModelProvider}, see csvExportFilenameBase method;
 *     currentDateAndTime is the current date and time in the yyyy-MM-dd.HH-mm format</li>
 *     <li>It initiates automatic download of the generated CSV file</li>
 *     <li>The generated CSV file is limited by 10000 rows</li>
 * </ul>
 * @param <T>
 *            Table row data type.
 **/
public class TableCsvExporter<T> {
    private static final int LINES_LIMIT = 10000;
    private static final String HTML_TAG_PATTERN = "<[^>]*>"; //$NON-NLS-1$
    private static final String EMPTY = ""; //$NON-NLS-1$
    private static final char SPACE = ' '; //$NON-NLS-1$
    private static final char NEW_LINE = '\n'; //$NON-NLS-1$
    private static final char SEPARATOR = ','; //$NON-NLS-1$
    private static final char DOT = '.'; //$NON-NLS-1$
    private static final char SINGLE_QUOTE = '\''; //$NON-NLS-1$
    private static final char DOUBLE_QUOTE = '"'; //$NON-NLS-1$
    private static final String DOUBLE_QUOTE_STR = "\""; //$NON-NLS-1$
    private static final String DOUBLE_DOUBLE_QUOTE_STR = "\"\""; //$NON-NLS-1$
    private static final String FILE_EXT = ".csv"; //$NON-NLS-1$
    private static final String FILE_CURRENT_DATE_AND_TIME_FORMAT = "yyyy-MM-dd.HH-mm"; //$NON-NLS-1$

    private final String filenameBase;
    private final SearchableTableModelProvider<T, ?> modelProvider;
    private final AbstractCellTable<T> table;
    private final ColumnController columnController;
    private final boolean testMode;
    private final StringBuilder csv;
    private int pageOffset;
    private int linesExported = -1;

    public TableCsvExporter(String filenameBase, SearchableTableModelProvider<T, ?> modelProvider, ActionCellTable<T> table) {
        this(filenameBase, modelProvider, table, table);
    }

    TableCsvExporter(String filenameBase, SearchableTableModelProvider<T, ?> modelProvider, AbstractCellTable<T> table, ColumnController<T> columnController) {
        this.filenameBase = filenameBase;
        this.modelProvider = modelProvider;
        this.table = table;
        this.columnController = columnController;
        this.csv = new StringBuilder();
        this.pageOffset = 0;
        this.testMode = table != columnController; // For unit tests
    }

    public void generateCsv() {
        // Header
        int colCount = table.getColumnCount();
        List<AbstractColumn<T, ?>> columns = new ArrayList<>();
        boolean firstInLine = true;
        for (int i = 0; i < colCount; i++) {
            Column<T, ?> col = table.getColumn(i);
            if (columnController.isColumnVisible(col) &&
                    col instanceof AbstractColumn) {
                String colName = ((AbstractColumn<?, ?>) col).getContextMenuTitle();
                if (colName == null || colName.isEmpty()) {
                    Header<?> header = table.getHeader(i);
                    colName = csvValue(header.getValue());
                }
                if (colName != null && !colName.isEmpty()) {
                    columns.add((AbstractColumn<T, ?>) col);
                    firstInLine = appendItem(firstInLine, colName);
                }
            }
        }
        newLine();

        // Content
        // Note that in order to export content of the table we need to scroll to the first page, then export the content
        // by moving forward page by page till the end (or till the 10000 rows limit is reached). And then return to the
        // page where the export functionality was initiated.
        ListModel<T> model = modelProvider.getModel();
        Event<EventArgs> itemsChangedEvent = model.getItemsChangedEvent();
        if (modelProvider.canGoBack()) {
            // If we are not on the first page then let's move to the first page
            itemsChangedEvent.addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    if (modelProvider.canGoBack()) {
                        // We are still not on the first page
                        pageOffset--;
                        modelProvider.goBack();
                    } else {
                        // The first page was reached. Let's generate the CSV file
                        itemsChangedEvent.removeListener(this);
                        generateContent(columns);
                    }
                }
            });
            pageOffset--;
            modelProvider.goBack();
        } else {
            // We are on the first page already. Let's generate the CSV file
            generateContent(columns);
        }
    }

    private void generateContent(List<AbstractColumn<T, ?>> columns) {
        ListModel<T> model = modelProvider.getModel();
        // Export current page to CSV ...
        generatePage(columns, model.getItems());
        if (hasMoreData()) {
            // ... and then move to the next page if any
            Event<EventArgs> itemsChangedEvent = model.getItemsChangedEvent();
            itemsChangedEvent.addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    // When the next page was loaded continue the export
                    itemsChangedEvent.removeListener(this);
                    generateContent(columns);
                }
            });
            pageOffset++;
            modelProvider.goForward();
        } else {
            // All the content was exported, so move to the initial page and initiate download of the exported CSV file
            restorePageAndFinish();
        }
    }

    private void restorePageAndFinish() {
        // Before initiating the download of the exported content we want to return to the initial page of the table
        if (pageOffset > 0 && modelProvider.canGoBack()) {
            // We still are not on the initial page. Let move towards it
            ListModel<T> model = modelProvider.getModel();
            Event<EventArgs> itemsChangedEvent = model.getItemsChangedEvent();
            itemsChangedEvent.addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    itemsChangedEvent.removeListener(this);
                    restorePageAndFinish();
                }
            });
            pageOffset--;
            modelProvider.goBack();
        } else {
            // We reached the initial page, let's initiate automatic download of the generated CSV file
            if (!testMode) { // disabled for unit tests
                downloadCsv(getFileName(), getGeneratedCsv());
            }
        }
    }

    private void generatePage(List<AbstractColumn<T, ?>> columns, Collection<T> items) {
        boolean firstInLine = true;
        for (T item : items) {
            for (AbstractColumn<T, ?> col : columns) {
                String cellValue = csvValue(col.getValue(item));
                if (cellValue == null || cellValue.isEmpty()) {
                    cellValue = csvValue(col.getTooltip(item));
                }
                firstInLine = appendItem(firstInLine, cellValue);
            }
            firstInLine = newLine();
        }
    }

    private boolean hasMoreData() {
        return modelProvider.canGoForward() && linesExported < LINES_LIMIT;
    }

    private String csvValue(Object tableValue) {
        String result = null;
        if (tableValue instanceof String) {
            result = (String) tableValue;
        } else if (tableValue instanceof SafeHtml) {
            result = ((SafeHtml) tableValue).asString();
        } else if (tableValue instanceof BusinessEntityWithStatus) {
            result = translateEnum(((BusinessEntityWithStatus) tableValue).getStatus());
        }

        if (result != null) {
            // Sometimes content of a cell contains images (encoded in HTML tags). Let's remove the images. Just leave a
            // text of the cell
            result = result.replaceAll(HTML_TAG_PATTERN, EMPTY).trim();
        }

        return result;
    }

    private String translateEnum(Enum<?> key) {
        return testMode ? key.name() : EnumTranslator.getInstance().translate(key);
    }

    private boolean appendItem(boolean firstInLine, String item) {
        if (!firstInLine) {
            csv.append(SEPARATOR);
        }
        if (item != null) {
            csv.append(escapeSpecialCharacters(item));
        }
        return false;
    }

    private boolean newLine() {
        csv.append(NEW_LINE);
        linesExported++;
        return true;
    }

    private String escapeSpecialCharacters(String data) {
        String escapedData = data.replace(NEW_LINE, SPACE);
        if (escapedData.indexOf(SEPARATOR) >= 0 ||
                escapedData.indexOf(SINGLE_QUOTE) >= 0 ||
                escapedData.indexOf(DOUBLE_QUOTE) >= 0) {
            escapedData = DOUBLE_QUOTE + escapedData.replace(DOUBLE_QUOTE_STR, DOUBLE_DOUBLE_QUOTE_STR) + DOUBLE_QUOTE;
        }
        return escapedData;
    }

    String getFileName() {
        String dt = DateTimeFormat.getFormat(FILE_CURRENT_DATE_AND_TIME_FORMAT).format(new Date()); //$NON-NLS-1$
        return filenameBase + DOT + dt + FILE_EXT;
    }
    String getGeneratedCsv() {
        return csv.toString();
    }

    private native void downloadCsv(String filename, String text)/*-{
      var pom = document.createElement('a');
      pom.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
      pom.setAttribute('download', filename);
      document.body.appendChild(pom);
      pom.click();
     document.body.removeChild(pom); }-*/;
}

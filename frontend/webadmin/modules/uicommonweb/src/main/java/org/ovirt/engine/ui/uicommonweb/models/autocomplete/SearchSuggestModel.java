package org.ovirt.engine.ui.uicommonweb.models.autocomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.core.searchbackend.SyntaxChecker;
import org.ovirt.engine.core.searchbackend.SyntaxContainer;
import org.ovirt.engine.core.searchbackend.SyntaxError;
import org.ovirt.engine.core.searchbackend.SyntaxObjectType;
import org.ovirt.engine.ui.uicommonweb.StringConstants;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ObservableCollection;

public class SearchSuggestModel extends SearchableListModel {

    private final String[] itemsToIgnore = { "monitor-desktop", //$NON-NLS-1$
            SyntaxChecker.PAGE, SyntaxChecker.SORTBY, SyntaxChecker.SORTDIR_ASC, SyntaxChecker.SORTDIR_DESC};

    //Exceptions that are potentially matched by items to ignore but are valid.
    private final List<String> exceptions = Arrays.asList("DESCRIPTION"); //$NON-NLS-1$

    @Override
    public List getItems() {
        return (List) super.getItems();
    }

    public void setItems(List value) {
        super.setItems(value);
    }

    private String privatePrefix;

    public String getPrefix() {
        return privatePrefix;
    }

    public void setPrefix(String value) {
        privatePrefix = value;
    }

    /**
     * Gets or sets an array specifying which options will be filtered out from suggestion.
     */
    private String[] privateFilter;

    public String[] getFilter() {
        return privateFilter;
    }

    public void setFilter(String[] value) {
        privateFilter = value;
    }

    private String[] searchObjectFilter;

    public String[] getSearchObjectFilter() {
        return searchObjectFilter;
    }

    public void setSearchObjectFilter(String[] value) {
        searchObjectFilter = value;
    }

    public SearchSuggestModel() {
        setItems(new ObservableCollection<>());
        setIsTimerDisabled(true);
    }

    @Override
    protected void searchStringChanged() {
        super.searchStringChanged();
        getSearchCommand().execute();
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();
        updateOptionsAsync(getPrefix() + getSearchString());
    }

    public void updateOptionsAsync(String search) {
        getItems().clear();

        ISyntaxChecker syntaxChecker = getConfigurator().getSyntaxChecker();
        if (syntaxChecker == null) {
            return;
        }

        SyntaxContainer syntax = syntaxChecker.getCompletion(search);

        int lastHandledIndex = syntax.getLastHandledIndex();
        String pf = search.substring(0, lastHandledIndex);
        String notHandled = search.substring(lastHandledIndex);

        String[] suggestedItems = syntax.getCompletionArray();

        // Ensure that filtered search objects will invalidate the whole search query
        if (getSearchObjectFilter() != null && syntax.getState() != SyntaxObjectType.BEGIN) {
            for (String value : getSearchObjectFilter()) {
                if (pf.toLowerCase().equals(value.toLowerCase())
                        || pf.toLowerCase().startsWith(value.toLowerCase() + ":") || containsWithItemsToIgnore(pf)) { //$NON-NLS-1$
                    addSuggestItem("", SuggestItemPartType.Valid, search, SuggestItemPartType.Erroneous); //$NON-NLS-1$
                    return;
                }
            }
        }

        if (syntax.getError() == SyntaxError.NO_ERROR) {
            List<String> actualItems = new ArrayList<>(Arrays.asList(suggestedItems));

            // Filter search object suggestions
            if (getSearchObjectFilter() != null && syntax.getState() == SyntaxObjectType.BEGIN) {
                for (String value : getSearchObjectFilter()) {
                    for (String item : suggestedItems) {
                        if (item.toLowerCase().equals(value.toLowerCase())) {
                            actualItems.remove(item);
                        }
                    }
                }

                // Ensure that empty search suggestion list invalidates the search query
                if (actualItems.isEmpty()) {
                    addSuggestItem("", SuggestItemPartType.Valid, search, SuggestItemPartType.Erroneous); //$NON-NLS-1$
                    return;
                }
            }

            for (String item : actualItems) {
                // Apply filter
                if (getFilter() != null) {
                    boolean skipItem = false;
                    for (String value : getFilter()) {
                        if (Objects.equals(value.toLowerCase(), item.toLowerCase())) {
                            skipItem = true;
                            break;
                        }
                    }

                    if (skipItem) {
                        continue;
                    }
                }

                String space = ""; //$NON-NLS-1$
                if ((pf.length() > 0) && !pf.substring(pf.length() - 1, pf.length() - 1 + 1).equals(".") //$NON-NLS-1$
                        && !".".equals(item)) { //$NON-NLS-1$
                    space = StringConstants.SPACE;
                }

                // Patch: monitor-desktop
                if (!inItemsToIgnore(item)) {
                    addSuggestItem(StringHelper.trimEnd(pf),
                            SuggestItemPartType.Valid,
                            space + item.trim(),
                            SuggestItemPartType.New);
                }
            }
        } else {
            addSuggestItem(pf, SuggestItemPartType.Valid, notHandled, SuggestItemPartType.Erroneous);
        }
    }

    private boolean containsWithItemsToIgnore(String pf) {
        for (String item: itemsToIgnore) {
            if (pf.toUpperCase().contains(StringConstants.SPACE + item.trim().toUpperCase())) {
                for (String exception: exceptions) {
                    if (pf.toUpperCase().contains(exception)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean inItemsToIgnore(String actualItem) {
        if (actualItem != null) {
            for (String item: itemsToIgnore) {
                if (item.equalsIgnoreCase(actualItem.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addSuggestItem(String firstPart, SuggestItemPartType firstPartType,
            String secondPart, SuggestItemPartType secondPartType) {
        SuggestItemPartModel tempVar = new SuggestItemPartModel();
        tempVar.setPartString(firstPart);
        tempVar.setPartType(firstPartType);
        SuggestItemPartModel tempVar2 = new SuggestItemPartModel();
        tempVar2.setPartString(secondPart);
        tempVar2.setPartType(secondPartType);
        ArrayList<SuggestItemPartModel> parts =
                new ArrayList<>(Arrays.asList(new SuggestItemPartModel[]{tempVar, tempVar2}));
        getItems().add(parts);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();

        List selectedItem = (List) getSelectedItem();
        if (selectedItem != null) {
            ArrayList<String> items = new ArrayList<>();
            for (Object item : selectedItem) {
                SuggestItemPartModel i = (SuggestItemPartModel) item;
                items.add(i.getPartString());
            }

            String searchString = String.join("", items); //$NON-NLS-1$
            // If there prefix exist, don't transfer it back as a part of search string.
            if (getPrefix() != null) {
                searchString = searchString.substring(getPrefix().length());
            }

            setSearchString(searchString);
        }
    }

    @Override
    protected String getListName() {
        return "SearchSuggestModel"; //$NON-NLS-1$
    }
}

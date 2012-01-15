package org.ovirt.engine.ui.uicommonweb.models.autocomplete;

import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.core.searchbackend.SyntaxCheckerFactory;
import org.ovirt.engine.core.searchbackend.SyntaxContainer;
import org.ovirt.engine.core.searchbackend.SyntaxError;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ITaskTarget;
import org.ovirt.engine.ui.uicompat.Task;
import org.ovirt.engine.ui.uicompat.TaskContext;

@SuppressWarnings("unused")
public class SearchSuggestModel extends SearchableListModel implements ITaskTarget
{
    private ISyntaxChecker syntaxChecker;

    @Override
    public java.util.List getItems()
    {
        return (java.util.List) super.getItems();
    }

    public void setItems(java.util.List value)
    {
        super.setItems(value);
    }

    private String privatePrefix;

    public String getPrefix()
    {
        return privatePrefix;
    }

    public void setPrefix(String value)
    {
        privatePrefix = value;
    }

    /**
     * Gets or sets an array specifying which options will be filtered out from suggestion.
     */
    private String[] privateFilter;

    public String[] getFilter()
    {
        return privateFilter;
    }

    public void setFilter(String[] value)
    {
        privateFilter = value;
    }

    public SearchSuggestModel()
    {
        setItems(new ObservableCollection<Object>());

        syntaxChecker = SyntaxCheckerFactory.CreateUISyntaxChecker(DataProvider.GetAuthenticationMethod());

        setIsTimerDisabled(true);
    }

    @Override
    protected void SearchStringChanged()
    {
        super.SearchStringChanged();
        getSearchCommand().Execute();
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        Task.Create(this, null).InvokeUIThread();
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();
        AsyncSearch();
    }

    public void UpdateOptionsAsync(String search)
    {
        getItems().clear();

        SyntaxContainer syntax = syntaxChecker.getCompletion(search);

        int lastHandledIndex = syntax.getLastHandledIndex();
        String pf = search.substring(0, lastHandledIndex);
        String notHandled = search.substring(lastHandledIndex);

        if (syntax.getError() == SyntaxError.NO_ERROR)
        {
            String[] items = syntax.getCompletionArray();

            for (String item : items)
            {
                // Apply filter.
                if (getFilter() != null)
                {
                    boolean skipItem = false;
                    for (String value : getFilter())
                    {
                        if (StringHelper.stringsEqual(value.toLowerCase(), item.toLowerCase()))
                        {
                            skipItem = true;
                            break;
                        }
                    }

                    if (skipItem)
                    {
                        continue;
                    }
                }

                String space = "";
                if ((pf.length() > 0) && (!pf.substring(pf.length() - 1, pf.length() - 1 + 1).equals("."))
                        && (!StringHelper.stringsEqual(item, ".")))
                {
                    space = " ";
                }

                // Patch: monitor-desktop
                if (!item.trim().toLowerCase().startsWith("monitor-desktop"))
                {
                    SuggestItemPartModel tempVar = new SuggestItemPartModel();
                    tempVar.setPartString(StringHelper.trimEnd(pf));
                    tempVar.setPartType(SuggestItemPartType.Valid);
                    SuggestItemPartModel tempVar2 = new SuggestItemPartModel();
                    tempVar2.setPartString(space + item.trim());
                    tempVar2.setPartType(SuggestItemPartType.New);
                    java.util.ArrayList<SuggestItemPartModel> parts =
                            new java.util.ArrayList<SuggestItemPartModel>(java.util.Arrays.asList(new SuggestItemPartModel[] {
                                    tempVar, tempVar2 }));

                    getItems().add(parts);
                }
            }
        }
        else
        {
            SuggestItemPartModel tempVar3 = new SuggestItemPartModel();
            tempVar3.setPartString(pf);
            tempVar3.setPartType(SuggestItemPartType.Valid);
            SuggestItemPartModel tempVar4 = new SuggestItemPartModel();
            tempVar4.setPartString(notHandled);
            tempVar4.setPartType(SuggestItemPartType.Erroneous);
            java.util.ArrayList<SuggestItemPartModel> parts =
                    new java.util.ArrayList<SuggestItemPartModel>(java.util.Arrays.asList(new SuggestItemPartModel[] {
                            tempVar3, tempVar4 }));

            getItems().add(parts);
        }
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();

        java.util.List selectedItem = (java.util.List) getSelectedItem();
        if (selectedItem != null)
        {
            // SearchString = String.Join(String.Empty,
            // selectedItem
            // .Cast<SuggestItemPartModel>()
            // .Select(a => a.PartString)
            // .ToArray()
            // );
            java.util.ArrayList<String> items = new java.util.ArrayList<String>();
            for (Object item : selectedItem)
            {
                SuggestItemPartModel i = (SuggestItemPartModel) item;
                items.add(i.getPartString());
            }

            String searchString = StringHelper.join("", items.toArray(new String[] {}));
            // If there prefix exist, don't transfer it back as a part of search string.
            if (getPrefix() != null)
            {
                searchString = searchString.substring(getPrefix().length());
            }

            setSearchString(searchString);
        }
    }

    @Override
    public void run(TaskContext context)
    {
        UpdateOptionsAsync(getPrefix() + getSearchString());
    }

    @Override
    protected String getListName() {
        return "SearchSuggestModel";
    }
}

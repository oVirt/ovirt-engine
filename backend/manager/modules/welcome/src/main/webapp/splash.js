function localeSelected(selectBox) {
    var selectedLocale = selectBox.options[selectBox.selectedIndex].value;
    var newParam = 'locale=' + encodeURIComponent(selectedLocale);
    var params = '?' + newParam;
    var search = document.location.search;
    // If the "search" string exists, then build params from it
    if (search) {
      // Try to replace an existance instance
      params = search.replace(new RegExp('([\?&])' + 'locale' + '[^&]*'), "$1" + newParam);

      // If nothing was replaced, then add the new param to the end
      if (params === search) {
        params += '&' + newParam;
      }
    }
    document.location.href = document.location.pathname + params;
}

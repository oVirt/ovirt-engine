package org.ovirt.engine.ui.uicommonweb.models.bookmarks;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class BookmarkModel extends Model {

    private boolean privateIsNew;

    public boolean getIsNew() {
        return privateIsNew;
    }

    public void setIsNew(boolean value) {
        privateIsNew = value;
    }

    private EntityModel<String> privateName;

    public EntityModel<String> getName() {
        return privateName;
    }

    public void setName(EntityModel<String> value) {
        privateName = value;
    }

    private EntityModel<String> privateSearchString;

    public EntityModel<String> getSearchString() {
        return privateSearchString;
    }

    public void setSearchString(EntityModel<String> value) {
        privateSearchString = value;
    }

    public BookmarkModel() {
        setName(new EntityModel<String>());
        setSearchString(new EntityModel<String>());
    }

    public boolean validate() {
        LengthValidation tempVar = new LengthValidation();
        tempVar.setMaxLength(40);
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

        LengthValidation tempVar2 = new LengthValidation();
        tempVar2.setMaxLength(300);
        getSearchString().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar2 });

        return getName().getIsValid() && getSearchString().getIsValid();
    }
}

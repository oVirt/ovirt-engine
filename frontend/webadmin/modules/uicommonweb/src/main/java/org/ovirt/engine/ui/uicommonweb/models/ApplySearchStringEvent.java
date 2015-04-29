package org.ovirt.engine.ui.uicommonweb.models;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class ApplySearchStringEvent extends GwtEvent<ApplySearchStringEvent.ApplySearchStringHandler> {

  String searchString;

  protected ApplySearchStringEvent() {
    // Possibly for serialization.
  }

  public ApplySearchStringEvent(String searchString) {
    this.searchString = searchString;
  }

  public static void fire(HasHandlers source, String searchString) {
    ApplySearchStringEvent eventInstance = new ApplySearchStringEvent(searchString);
    source.fireEvent(eventInstance);
  }

  public static void fire(HasHandlers source, ApplySearchStringEvent eventInstance) {
    source.fireEvent(eventInstance);
  }

  public interface HasApplySearchStringHandlers extends HasHandlers {
    HandlerRegistration addApplySearchStringHandler(ApplySearchStringHandler handler);
  }

  public interface ApplySearchStringHandler extends EventHandler {
    public void onApplySearchString(ApplySearchStringEvent event);
  }

  private static final Type<ApplySearchStringHandler> TYPE = new Type<ApplySearchStringHandler>();

  public static Type<ApplySearchStringHandler> getType() {
    return TYPE;
  }

  @Override
  public Type<ApplySearchStringHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(ApplySearchStringHandler handler) {
    handler.onApplySearchString(this);
  }

  public String getSearchString(){
    return searchString;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
        return true;
    if (obj == null)
        return false;
    if (getClass() != obj.getClass())
        return false;
    ApplySearchStringEvent other = (ApplySearchStringEvent) obj;
    if (searchString == null) {
      if (other.searchString != null)
        return false;
    } else if (!searchString.equals(other.searchString))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 23;
    hashCode = (hashCode * 37) + (searchString == null ? 1 : searchString.hashCode());
    return hashCode;
  }

  @Override
  public String toString() {
    return "ApplySearchStringEvent[" //$NON-NLS-1$
                 + searchString
    + "]"; //$NON-NLS-1$
  }
}

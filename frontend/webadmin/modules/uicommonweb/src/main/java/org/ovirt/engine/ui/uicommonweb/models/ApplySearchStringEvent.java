package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Objects;

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

  private static final Type<ApplySearchStringHandler> TYPE = new Type<>();

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
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ApplySearchStringEvent)) {
      return false;
    }
    ApplySearchStringEvent other = (ApplySearchStringEvent) obj;
    return Objects.equals(searchString, other.searchString);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(searchString);
  }

  @Override
  public String toString() {
    return "ApplySearchStringEvent[" //$NON-NLS-1$
                 + searchString
    + "]"; //$NON-NLS-1$
  }
}

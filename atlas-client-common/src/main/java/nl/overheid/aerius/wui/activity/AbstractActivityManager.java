package nl.overheid.aerius.wui.activity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.ResettableEventBus;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import nl.overheid.aerius.wui.command.HasCommandRouter;
import nl.overheid.aerius.wui.command.PlaceChangeCommand;
import nl.overheid.aerius.wui.dev.GWTProd;
import nl.overheid.aerius.wui.place.Place;
import nl.overheid.aerius.wui.place.PlaceController;
import nl.overheid.aerius.wui.widget.HasEventBus;

public abstract class AbstractActivityManager<C> implements ActivityManager<C> {
  private final ActivityManagerImplEventBinder EVENT_BINDER = GWT.create(ActivityManagerImplEventBinder.class);

  @SuppressWarnings("rawtypes")
  interface ActivityManagerImplEventBinder extends EventBinder<AbstractActivityManager> {}

  private final ActivityMapper<C> mapper;
  private final PlaceController placeController;

  private C panel;

  private final ResettableEventBus activityEventBus;

  private Activity<?, ?> currentActivity;

  public AbstractActivityManager(final EventBus globalEventBus, final PlaceController placeController, final ActivityMapper<C> mapper) {
    this.placeController = placeController;
    this.mapper = mapper;

    activityEventBus = new ResettableEventBus(globalEventBus);

    EVENT_BINDER.bindEventHandlers(this, globalEventBus);
  }

  @EventHandler
  public void onPlaceChangeCommand(final PlaceChangeCommand c) {
    final Place previousPlace = placeController.getPreviousPlace();
    final Place place = c.getValue();

    if (previousPlace != null && previousPlace.getClass().equals(place.getClass())) {
      return;
    }

    final boolean delegateSuccess = delegateToActivity(currentActivity, activityEventBus, c);
    if (delegateSuccess) {
      GWTProd.log("ActivityManager", "Delegated to current activity.");
      return;
    }

    if (c.isRedirected()) {
      GWTProd.log("ActivityManager", "Cancelling because place is redirected. (to " + c.getRedirect() + ")");
      c.silence();
      c.cancel();
      return;
    }

    // Suspend previous activity
    final boolean suspendSuccess = suspendActivity(currentActivity);
    if (!suspendSuccess) {
      c.silence();
      c.cancel();
      return;
    }

    // Remove event handlers
    GWTProd.log("ActivityManager", "Removing handlers");
    activityEventBus.removeHandlers();

    // Start next activity
    final Activity<?, C> activity = mapper.getActivity(place);
    if (activity instanceof HasEventBus) {
      ((HasEventBus) activity).setEventBus(activityEventBus);
    }

    currentActivity = activity;

    GWTProd.log("ActivityManager", "Starting activity: " + currentActivity.getClass().getSimpleName());

    // Start and delegate
    activity.onStart(panel);
    if (activity instanceof HasCommandRouter) {
      ((HasCommandRouter) activity).onStart();
    }
    if (activity instanceof DelegableActivity) {
      final DelegableActivity act = (DelegableActivity) activity;

      if (act.isDelegable(place)) {
        act.delegate(activityEventBus, c);
      }
    }
  }

  private boolean delegateToActivity(final Activity<?, ?> activity, final EventBus eventBus, final PlaceChangeCommand c) {
    if (activity instanceof DelegableActivity) {
      final DelegableActivity act = (DelegableActivity) activity;

      if (act.isDelegable(c.getValue())) {
        return act.delegate(eventBus, c);
      } else {
        return false;
      }
    }

    return false;
  }

  @Override
  public void setPanel(final C panel) {
    this.panel = panel;
  }

  private static <C> boolean suspendActivity(final Activity<?, C> currentActivity) {
    if (currentActivity == null) {
      return true;
    }

    final String stop = currentActivity.mayStop();
    if (stop != null) {
      final boolean confirm = Window.confirm(stop);

      if (confirm) {
        currentActivity.onStop();
      } else {
        return false;
      }
    } else {
      currentActivity.onStop();
    }

    return true;
  }
}

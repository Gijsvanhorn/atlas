/*
 * Copyright Dutch Ministry of Agriculture, Nature and Food Quality
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package nl.aerius.shared.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ServiceSelector implements Serializable {
  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new AutoValue_ServiceSelector.Builder()
        .defaultt(false)
        .selectable(true);
  }

  private ServiceSelector parent;

  private boolean enabled;
  private boolean selected;

  public abstract String value();

  public abstract String name();

  public abstract String nameShort();

  public abstract boolean defaultt();

  public abstract boolean selectable();

  public void parent(final ServiceSelector parent) {
    this.parent = parent;
  }

  public ServiceSelector parent() {
    return parent;
  }

  public void enabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public boolean enabled() {
    return enabled;
  }

  public void selected(final boolean selected) {
    this.selected = selected;
  }

  public boolean selected() {
    return selected;
  }

  @Nullable
  public abstract List<String> searchTags();

  @Nullable
  public abstract HashMap<String, String> tags();

  @Nullable
  public abstract List<ServiceSelector> selectors();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder value(String value);

    public abstract Builder name(String value);

    public abstract Builder nameShort(String value);

    public abstract Builder defaultt(boolean value);

    public abstract Builder selectable(boolean value);

    public abstract Builder tags(HashMap<String, String> value);

    public abstract Builder selectors(List<ServiceSelector> value);

    public abstract Builder searchTags(List<String> value);

    public abstract ServiceSelector build();
  }
}

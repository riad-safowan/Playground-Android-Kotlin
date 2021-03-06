// Generated by Dagger (https://dagger.dev).
package com.riadsafowan.to_do.data.local.pref;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import javax.inject.Provider;

@DaggerGenerated
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class PreferencesRepository_Factory implements Factory<PreferencesRepository> {
  private final Provider<Context> contextProvider;

  public PreferencesRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PreferencesRepository get() {
    return newInstance(contextProvider.get());
  }

  public static PreferencesRepository_Factory create(Provider<Context> contextProvider) {
    return new PreferencesRepository_Factory(contextProvider);
  }

  public static PreferencesRepository newInstance(Context context) {
    return new PreferencesRepository(context);
  }
}

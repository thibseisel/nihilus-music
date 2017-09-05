package fr.nihilus.music;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import fr.nihilus.music.di.DaggerAppComponent;
import fr.nihilus.music.settings.PreferenceDao;

/**
 * An Android Application component that can inject dependencies into Activities and Services.
 * This class also performs general configuration tasks.
 */
public class MyApplication extends Application implements HasActivityInjector, HasServiceInjector {
    @Inject DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject DispatchingAndroidInjector<Service> dispatchingServiceInjector;
    @Inject PreferenceDao mPrefs;

    @Override
    public void onCreate() {
        super.onCreate();

        /*if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            );

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            );
        }*/


        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this);

        AppCompatDelegate.setDefaultNightMode(mPrefs.getNightMode());

        // Permet d'inflater des VectorDrawable pour API < 21. Peut causer des problèmes.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }
}
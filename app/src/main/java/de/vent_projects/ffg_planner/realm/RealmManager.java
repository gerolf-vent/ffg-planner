package de.vent_projects.ffg_planner.realm;

import android.content.Context;

import java.io.File;

import de.vent_projects.ffg_planner.BuildConfig;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class RealmManager implements RealmMigration {
    public static long getRealmSchemaVersion() {
        return 0;
    }

    public static RealmMigration getRealmMigration() {
        return new RealmManager();
    }

    public static void init(Context context) {
        Realm.setDefaultConfiguration(getDefaultRealmConfiguration(context));
    }

    public static Realm getRealm(){
        return Realm.getDefaultInstance();
    }

    public static void close() {
        getRealm().close();
    }

    private static RealmConfiguration getDefaultRealmConfiguration(Context context) {
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder();
        builder.name("default.realm");
        builder.schemaVersion(RealmManager.getRealmSchemaVersion());
        if (RealmManager.getRealmSchemaVersion() > 1) {
            if (RealmManager.getRealmMigration() != null) {
                builder.migration(getRealmMigration());
            } else {
                builder.deleteRealmIfMigrationNeeded();
            }
        }
        if (BuildConfig.DEBUG) {
            File externalFilesDir = context.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                builder.directory(externalFilesDir);
            }
        }
        return builder.build();
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        // TODO: Add new migration if schemaVersion++
    }
}

package io.github.sumyabazar1.weather;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The weather now for a launcher header: a periodic job reads the chosen city's current weather every 30 minutes,
 * and a read-only provider hands the last reading out (temp, code, day, sky words, time read). The launcher itself
 * keeps no network access; it only reads this provider.
 */
final class HeaderWeather {
    static final String PREFS = "header";
    private static final int JOB_ID = 4201;
    private static final long PERIOD_MS = 30 * 60_000L;

    private HeaderWeather() { }

    /** Keeps the periodic job scheduled; cheap when it already is. */
    static void schedule(Context context) {
        JobScheduler jobs = context.getSystemService(JobScheduler.class);
        if (jobs == null || jobs.getPendingJob(JOB_ID) != null) return;
        jobs.schedule(new JobInfo.Builder(JOB_ID, new ComponentName(context, Job.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD_MS)
                .setPersisted(true)
                .build());
    }

    /** Asks for one reading soon, for when the stored one is missing or old. */
    static void refreshSoon(Context context) {
        JobScheduler jobs = context.getSystemService(JobScheduler.class);
        if (jobs == null || jobs.getPendingJob(JOB_ID + 1) != null) return;
        jobs.schedule(new JobInfo.Builder(JOB_ID + 1, new ComponentName(context, Job.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setOverrideDeadline(60_000L)
                .build());
    }

    static void store(Context context, Forecast.Report report) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt("temp", (int) Math.round(report.tempC))
                .putInt("code", report.code)
                .putBoolean("day", report.day)
                .putString("sky", context.getString(Forecast.sky(report.code, report.day)))
                .putLong("time", System.currentTimeMillis())
                .apply();
    }

    public static final class Job extends JobService {
        private final ExecutorService io = Executors.newSingleThreadExecutor();

        @Override
        public boolean onStartJob(JobParameters params) {
            SharedPreferences place = getSharedPreferences("place", MODE_PRIVATE);
            if (!place.contains("name")) return false;
            double lat = Double.longBitsToDouble(place.getLong("lat", 0));
            double lon = Double.longBitsToDouble(place.getLong("lon", 0));
            io.execute(() -> {
                boolean retry = false;
                try {
                    store(this, Forecast.fetch(lat, lon, Forecast.MIN_DAYS));
                } catch (Exception e) {
                    retry = true;
                }
                jobFinished(params, retry);
            });
            return true;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return true;
        }
    }

    /** content://io.github.sumyabazar1.weather.header/now: one row, or none before the first reading. */
    public static final class Provider extends ContentProvider {
        private static final String[] COLUMNS = {"temp", "code", "day", "sky", "time"};

        @Override
        public boolean onCreate() {
            return true;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
            Context context = getContext();
            MatrixCursor cursor = new MatrixCursor(COLUMNS);
            if (context == null) return cursor;
            schedule(context);
            SharedPreferences header = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            long time = header.getLong("time", 0);
            if (System.currentTimeMillis() - time > PERIOD_MS + 10 * 60_000L) refreshSoon(context);
            if (time > 0) {
                cursor.addRow(new Object[]{header.getInt("temp", 0), header.getInt("code", 3),
                        header.getBoolean("day", true) ? 1 : 0, header.getString("sky", ""), time});
            }
            return cursor;
        }

        @Override
        public String getType(Uri uri) {
            return null;
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            return null;
        }

        @Override
        public int delete(Uri uri, String selection, String[] args) {
            return 0;
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] args) {
            return 0;
        }
    }
}

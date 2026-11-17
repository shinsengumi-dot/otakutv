package com.gathin.tvhome;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * header_weather: the Weather add-on's last reading under the date ("☁️ 12° Nuageux"). The add-on does the network
 * work; Home only reads its provider, off the UI thread, on start and on the minute tick. Hidden when the add-on is
 * missing, has no city yet, or its reading is over three hours old.
 */
final class HeaderWeatherLine {
    private static final Uri URI = Uri.parse("content://io.github.sumyabazar1.weather.header/now");
    private static final long STALE_MS = 3 * 60 * 60_000L;
    private final Context context;
    private final TextView view;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    HeaderWeatherLine(Context context, TextView view) {
        this.context = context.getApplicationContext();
        this.view = view;
    }

    void update() {
        io.execute(() -> {
            String text = null;
            try (Cursor c = context.getContentResolver().query(URI, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    long time = c.getLong(c.getColumnIndexOrThrow("time"));
                    if (System.currentTimeMillis() - time < STALE_MS) {
                        int code = c.getInt(c.getColumnIndexOrThrow("code"));
                        boolean day = c.getInt(c.getColumnIndexOrThrow("day")) == 1;
                        text = icon(code, day) + "  " + c.getInt(c.getColumnIndexOrThrow("temp")) + "°   "
                                + c.getString(c.getColumnIndexOrThrow("sky"));
                    }
                }
            } catch (Exception ignored) {
                // add-on not installed or not answering: no line
            }
            String shown = text;
            view.post(() -> {
                view.setText(shown);
                view.setVisibility(shown == null ? View.GONE : View.VISIBLE);
            });
        });
    }

    void close() {
        io.shutdownNow();
    }

    /** WMO weather code to a colour emoji. */
    static String icon(int code, boolean day) {
        if (code == 0 || code == 1 && !day) return day ? "☀️" : "🌙";
        if (code == 1 || code == 2) return day ? "⛅" : "☁️";
        if (code == 3) return "☁️";
        if (code == 45 || code == 48) return "🌫️";
        if (code >= 95) return "⛈️";
        if (code >= 71 && code <= 77 || code == 85 || code == 86) return "❄️";
        if (code >= 80 && code <= 82) return "🌦️";
        if (code >= 51 && code <= 67) return "🌧️";
        return "☁️";
    }
}

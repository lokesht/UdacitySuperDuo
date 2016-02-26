package barqsoft.footballscores.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.MainScreenFragment;
import barqsoft.footballscores.R;
import barqsoft.footballscores.widget.TodaysMatchProvider;

/**
 * Created by Lokesh on 25/02/2016.
 */
public class WidgetUpdateService extends IntentService {

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.TIME_COL
    };

    // these indices must match the projection
    private static final int INDEX_MATCH_ID = 0;
    private static final int INDEX_HOME_COL = 1;
    private static final int INDEX_AWAY_COL = 2;
    private static final int INDEX_HOME_GOAL_COL = 3;
    private static final int INDEX_AWAY_GOAL_COL = 4;
    private static final int INDEX_LEAGUE_COL = 5;
    private static final int INDEX_TIME_COL = 6;

    public WidgetUpdateService() {
        super("Football Score");
    }

    public WidgetUpdateService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TodaysMatchProvider.class));

        Date fragmentdate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = mformat.format(fragmentdate);

        Uri todaysUri = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor data = getContentResolver().query(todaysUri, SCORE_COLUMNS, null, new String[]{todayDate}, null);

        if (data == null) {
            return;
        }

        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_today;

            RemoteViews remoteViews = new RemoteViews(getPackageName(), layoutId);
            remoteViews.setTextViewText(R.id.widget_home_team, data.getString(INDEX_HOME_COL));
            remoteViews.setTextViewText(R.id.widget_away_team, data.getString(INDEX_AWAY_COL));
            remoteViews.setTextViewText(R.id.widget_home_team_score, data.getString(INDEX_HOME_GOAL_COL));
            remoteViews.setTextViewText(R.id.widget_away_team_score, data.getString(INDEX_AWAY_GOAL_COL));

            String time = data.getString(INDEX_TIME_COL);
            time = getString(R.string.msg_match_start_time, time);
            remoteViews.setTextViewText(R.id.tv_widget_match_start_time, time);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

    }
}

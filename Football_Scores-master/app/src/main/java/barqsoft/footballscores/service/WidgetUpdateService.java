package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.widget.TodaysMatchProvider;

/**
 * Created by Lokesh on 25/02/2016.
 */
public class WidgetUpdateService extends IntentService {

    private static final String TAG = WidgetUpdateService.class.getName();

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
        Cursor data = getContentResolver().query(todaysUri, SCORE_COLUMNS, null, new String[]{todayDate}, DatabaseContract.scores_table.TIME_COL);

        if (data == null) {
            return;
        }

        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        /*Match which is most closet to current time*/
        String strTimeNow = Calendar.getInstance().get(Calendar.HOUR) + ":" + Calendar.getInstance().get(Calendar.HOUR);
        long timeNow = Utilies.getTimeInSecond(strTimeNow);
        /*Start with one day difference*/
        long closet = 24 * 60 * 60;
        int counter = -1;
        int mark = 0;
        do {
            counter++;

            String strTime = data.getString(INDEX_TIME_COL);
            long timeInSec = Utilies.getTimeInSecond(strTime);
            if ((Math.abs(timeInSec - timeNow)) < closet) {
                closet = Math.abs(timeInSec - timeNow);
                mark = counter;
            }
        } while (data.moveToNext());

        /*Now move to exact location where time very close to current time*/
        data.moveToPosition(mark);

        for (int appWidgetId : appWidgetIds) {

            String time = data.getString(INDEX_TIME_COL);

            String strHomeTeam = data.getString(INDEX_HOME_COL);
            String strAwayTeam = data.getString(INDEX_AWAY_COL);
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
            int layoutId;
            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.widget_today_large;

                time = getString(R.string.msg_match_start_time, time);
            } else {
                layoutId = R.layout.widget_today;

                /*Define short string for small widget*/
                time = getString(R.string.msg_match_start_time_short, time);

                /*Lets make name abbreviated due to space constraints*/
                String[] arrHomeTeam = strHomeTeam.split(" ");
                String temp = "";
                for (String s : arrHomeTeam) {
                    temp = temp + s.substring(0, 1);
                }
                Log.i(TAG, "onHandleIntent: HOME-TEAM " + strHomeTeam + "(" + temp + ")");
                strHomeTeam = temp;


                /*Clean the temp variable*/
                temp = "";
                String[] arrAwayTeam = strAwayTeam.split(" ");
                for (String s : arrAwayTeam) {
                    temp = temp + s.substring(0, 1);
                }
                Log.i(TAG, "onHandleIntent: HOME-TEAM " + strAwayTeam + "(" + temp + ")");
                strAwayTeam = temp;
            }

            RemoteViews remoteViews = new RemoteViews(getPackageName(), layoutId);
            remoteViews.setTextViewText(R.id.widget_home_team, strHomeTeam);

            remoteViews.setTextViewText(R.id.widget_away_team, strAwayTeam);

            String home_goal = data.getString(INDEX_HOME_GOAL_COL);
            String away_goal = data.getString(INDEX_AWAY_GOAL_COL);
            if (home_goal.equalsIgnoreCase("-1")) {
                home_goal = "-";
            }

            if (away_goal.equalsIgnoreCase("-1")) {
                away_goal = "-";
            }

            remoteViews.setTextViewText(R.id.tv_widget_match_start_time, time);
            remoteViews.setTextViewText(R.id.widget_home_team_score, home_goal);
            remoteViews.setTextViewText(R.id.widget_away_team_score, away_goal);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(remoteViews, R.id.tv_widget_match_start_time, time);
                setRemoteContentDescription(remoteViews, R.id.widget_home_team, data.getString(INDEX_HOME_COL));
                setRemoteContentDescription(remoteViews, R.id.widget_away_team, data.getString(INDEX_AWAY_COL));

                String msgHomeGoal = getString(R.string.msg_goal_stat, data.getString(INDEX_HOME_COL), data.getString(INDEX_HOME_GOAL_COL));
                String msgAwayGoal = getString(R.string.msg_goal_stat, data.getString(INDEX_AWAY_COL), data.getString(INDEX_AWAY_GOAL_COL));
                setRemoteContentDescription(remoteViews, R.id.widget_home_team_score, msgHomeGoal);
                setRemoteContentDescription(remoteViews, R.id.widget_away_team_score, msgAwayGoal);
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, int id, String description) {
        views.setContentDescription(id, description);
    }
}

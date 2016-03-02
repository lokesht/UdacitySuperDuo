package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.MATCH_DAY
    };

    // these indices must match the projection
    public static final int COL_DATE = 0;
    public static final int COL_MATCHTIME = 1;
    public static final int COL_HOME = 2;
    public static final int COL_AWAY = 3;
    public static final int COL_LEAGUE = 4;
    public static final int COL_HOME_GOALS = 5;
    public static final int COL_AWAY_GOALS = 6;
    public static final int COL_MATCH_ID = 7;
    public static final int COL_MATCHDAY = 8;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Date fragmentdate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                String todayDate = mformat.format(fragmentdate);

                Uri todaysUri = DatabaseContract.scores_table.buildScoreWithDate();
                data = getContentResolver().query(todaysUri, SCORE_COLUMNS, null, new String[]{todayDate}, DatabaseContract.scores_table.TIME_COL);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.scores_list_item);

                views.setTextViewText(R.id.home_name,data.getString(COL_HOME));
                views.setTextViewText(R.id.away_name,data.getString(COL_AWAY));
                views.setTextViewText(R.id.data_textview,data.getString(COL_DATE));

                int home_goal = data.getInt(COL_HOME_GOALS);
                int away_goal = data.getInt(COL_AWAY_GOALS);
                if (home_goal > away_goal) {
                    views.setTextColor(R.id.home_name,getResources().getColor(R.color.green));
                    views.setTextColor(R.id.away_name,getResources().getColor(R.color.red));
                } else if (home_goal < away_goal) {
                    views.setTextColor(R.id.home_name,getResources().getColor(R.color.red));
                    views.setTextColor(R.id.away_name,getResources().getColor(R.color.green));
                }else
                {
                    views.setTextColor(R.id.home_name,getResources().getColor(R.color.primary_text));
                    views.setTextColor(R.id.away_name,getResources().getColor(R.color.primary_text));
                }

                String goal = Utilies.getScores(home_goal, away_goal);
                views.setTextViewText(R.id.score_textview,goal);
                String time = data.getString(COL_MATCHTIME);
                views.setTextViewText(R.id.data_textview,time);

                views.setTextColor(R.id.score_textview,getResources().getColor(R.color.primary_text));
                views.setTextColor(R.id.data_textview,getResources().getColor(R.color.primary_text));

                views.setImageViewResource(R.id.home_crest,Utilies.getTeamCrestByTeamName(data.getString(COL_HOME)));
                views.setImageViewResource(R.id.away_crest,Utilies.getTeamCrestByTeamName(data.getString(COL_AWAY)));
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(COL_MATCH_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

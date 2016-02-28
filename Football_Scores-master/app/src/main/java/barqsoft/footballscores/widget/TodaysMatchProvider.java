package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import barqsoft.footballscores.ScoresProvider;

/**
 * Created by Lokesh on 25/02/2016.
 */
public class TodaysMatchProvider extends AppWidgetProvider {

    //TODO Empty view of Widget
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, TodaysWidgetUpdateService.class));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (ScoresProvider.ACTION_DATA_UPDATE.equals(intent.getAction())) {
            context.startService(new Intent(context, TodaysWidgetUpdateService.class));
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, TodaysWidgetUpdateService.class));
    }
}

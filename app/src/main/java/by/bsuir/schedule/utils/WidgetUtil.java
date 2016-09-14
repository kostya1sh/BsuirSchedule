package by.bsuir.schedule.utils;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.example.myapplication.R;
import by.bsuir.schedule.widget.ScheduleWidgetProvider;

/**
 * Created by iChrome on 23.09.2015.
 */
public class WidgetUtil {
    private WidgetUtil(){
    }

    /**
     * Метод служит для обновления всех виджетов данного приложения
     * @param activity Активити вызывавшая метод
     */
    public static void updateWidgets(Activity activity){
        Context context = activity.getApplicationContext();
        ComponentName name = new ComponentName(context, ScheduleWidgetProvider.class);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);

        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, activity, ScheduleWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        activity.sendBroadcast(intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, ScheduleWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listViewWidget);
    }
}

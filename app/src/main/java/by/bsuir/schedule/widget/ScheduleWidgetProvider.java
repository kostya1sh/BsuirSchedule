package by.bsuir.schedule.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.example.myapplication.R;
import by.bsuir.schedule.utils.DateUtil;
import by.bsuir.schedule.utils.FileUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of App Widget functionality.
 */
public class ScheduleWidgetProvider extends AppWidgetProvider {
    private static final String NEXT_DAY = "nextDay";
    private static final String PREVIOUS_DAY = "previousDay";
    private static final String GO_TO_TODAY = "goToToday";
    private static final String PATTERN = "^[а-яА-ЯёЁ]+";
    private static Integer offset = 0;

    /**
     * Метод обновляет все виджеты id которые переданы в массиве appWidgetIds
     * @param context контекст
     * @param appWidgetManager manager
     * @param appWidgetIds id виджетов которые нужно обновить
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listViewWidget);
    }

    /**
     * Вызывается при изменении в виджете
     * @param context контекст который вызывает метод
     * @param intent intent, из которого достается какое действие было совершено с виджетом
     */
    @Override
    public synchronized void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        updateOffsetValue(context, intent);
    }

    /**
     * Метод для обновления текущего смещения показываемого дня в виджете относительно текущего дня
     * по календарю
     * @param context контекст вызвавший метод
     * @param intent intent, из которого достается какое действие было совершено с виджетом
     */
    private static void updateOffsetValue(@NonNull Context context, @NonNull Intent intent){
        if(NEXT_DAY.equals(intent.getAction())){
            offset++;
            updateWidgetSecondMethod(context);
        } else if (PREVIOUS_DAY.equals(intent.getAction())) {
            offset--;
            updateWidgetSecondMethod(context);
        } else if(GO_TO_TODAY.equals(intent.getAction())){
            offset = 0;
            updateWidgetSecondMethod(context);
        }
    }

    /**
     * Метод для обновления всех виджетов данного приложения
     * @param context контекст
     */
    private static void updateWidgetSecondMethod(Context context){
        ComponentName name = new ComponentName(context, ScheduleWidgetProvider.class);
        int [] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);

        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, context, ScheduleWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, ScheduleWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listViewWidget);
    }

    /**
     * Вызывается когда виджет создается
     * @param context контекст
     */
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    /**
     * Enter relevant functionality for when the last widget is disabled
     * @param context контекст
     */
    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    /**
     * В методе происходит настройка виджета. Устанавливается обработчики на нажатия кнопок.
     * Устанавливается adapter для списка занятий
     * @param context контекст
     * @param appWidgetManager appWidgetManager
     * @param appWidgetId id виджета, который обрабатывается
     */
    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent = new Intent(context, ScheduleWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(ListRemoteViewsFactory.OFFSET_EXTRA_NAME, offset);
        intent.putExtra(ListRemoteViewsFactory.DEFAULT_SUBGROUP, FileUtil.getDefaultSubgroup(context));

        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        // Construct the RemoteViews object
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.schedule_widget);
        rv.setRemoteAdapter(appWidgetId, R.id.listViewWidget, intent);
        rv.setEmptyView(R.id.listViewWidget, R.id.empty_view_widget);

        rv.setTextViewText(R.id.scheduleWidgetTitle, getFirstTitle(context));
        rv.setTextViewText(R.id.secondWidgetTitle, getSecondTitle());
        rv.setTextViewText(R.id.secondWidgetSubTitle, getSecondSubTitle(context));

        rv.setOnClickPendingIntent(R.id.previousWidgetButton, getPendingSelfIntent(context, PREVIOUS_DAY));
        rv.setOnClickPendingIntent(R.id.nextWidgetButton, getPendingSelfIntent(context, NEXT_DAY));
        rv.setOnClickPendingIntent(R.id.todayWidgetButton, getPendingSelfIntent(context, GO_TO_TODAY));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, rv);

    }

    /**
     * В методе создается первый заголовок для виджета
     * @param context контектс
     * @return Возвращает заголовок
     */
    private static String getFirstTitle(Context context){
        String result = "";
        String defaultSchedule = FileUtil.getDefaultSchedule(context);
        if(defaultSchedule != null) {
            if (FileUtil.isDefaultStudentGroup(context)) {
                result = defaultSchedule.substring(0, 6);
                Integer defaultSubGroup = FileUtil.getDefaultSubgroup(context);
                if(defaultSubGroup != null) {
                    result += " - " + defaultSubGroup + " подгр.";
                }
            } else {
                Pattern pattern = Pattern.compile(PATTERN);
                Matcher matcher = pattern.matcher(defaultSchedule);
                result = findMatches(matcher);
            }
        }
        return result;
    }

    /**
     * Возвращает ФИО преподавателя
     * @param matcher матчер, в котором должна быть строка "ФИО преподавателя + id преподавателя"
     * @return Возвращает ФИО
     */
    private static String findMatches(Matcher matcher){
        String result;
        if (matcher.find()) {
            result = matcher.group(0);
            String initials = result.substring(result.length() - 2, result.length());
            if(initials.length() == 2) {
                result = result.substring(0, result.length() - 2) + " " + initials.charAt(0) + ". " + initials.charAt(1) + ".";
            }
        } else {
            result = "not found";
        }
        return result;
    }

    /**
     * Создает второй заголовок для виджета
     * @return Возвращает заголовок
     */
    private static String getSecondTitle(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, offset);
        DateFormat df = new SimpleDateFormat("dd/MM", Locale.getDefault());
        String result = df.format(calendar.getTime());

        Integer currentWeekNumber = DateUtil.getWeek(calendar.getTime());
        if(currentWeekNumber != null){
            result += " (" + currentWeekNumber + " нед)";
        }
        return result;
    }

    /**
     * Создает подЗаголовок для второго заголовка виджета
     * @param context контекст
     * @return возвращает подЗаголовок
     */
    private static String getSecondSubTitle(Context context){
        String result = "";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, offset);
        int indexDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if(indexDayOfWeek == 1){
            indexDayOfWeek = 8;
        }
        String[] dayOfWeekAbbrev = context.getResources().getStringArray(R.array.day_of_week_for_widget);
        result += dayOfWeekAbbrev[indexDayOfWeek - 1];
        return  result;
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}


package by.bsuir.schedule.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.myapplication.R;
import by.bsuir.schedule.dao.DBHelper;
import by.bsuir.schedule.dao.SchoolDayDao;
import by.bsuir.schedule.model.Schedule;
import by.bsuir.schedule.model.SchoolDay;
import by.bsuir.schedule.utils.DateUtil;
import by.bsuir.schedule.utils.FileUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by iChrome on 04.09.2015.
 */
public class ScheduleWidgetService extends RemoteViewsService {

    /**
     * Создает адаптер для списка занятий на виджете
     * @param intent intent
     * @return возвращает адаптер
     */
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{
    private static final String SET_BACKGROUND_RESOURCE = "setBackgroundResource";
    public static final String OFFSET_EXTRA_NAME = "scheduleWidgetOffset";
    public static final String DEFAULT_SUBGROUP = "defaultSubgroup";
    private String[] weekDays = new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
    private List<SchoolDay> weekSchedules;
    private Integer dayOffset;
    private Context savedContext;
    private List<Schedule> items;
    private Integer defaultSubGroup;
    private boolean isLastUsingDailySchedule;

    /**
     * Адаптер для виджета
     * @param context контекст
     * @param intent intent
     */
    public ListRemoteViewsFactory(Context context, Intent intent){
        savedContext = context;
        dayOffset = intent.getIntExtra(OFFSET_EXTRA_NAME, 0);
        defaultSubGroup = intent.getIntExtra(DEFAULT_SUBGROUP, 0);
    }

    @Override
    public void onCreate(){
        updateScheduleList();
    }

    /**
     * Обновляет список всех занятий. Сначале из SharedPreference достается дефолтная группа, потом
     * для нее достается список всех занятий
     */
    public void updateScheduleList(){
        String defaultSchedule = FileUtil.getDefaultSchedule(savedContext);
        if(defaultSchedule != null) {
            isLastUsingDailySchedule = FileUtil.isLastUsingDailySchedule(savedContext);
            if (!isLastUsingDailySchedule) {
                defaultSchedule = defaultSchedule.replace(".xml", "exam.xml");
            }
            SchoolDayDao sdd =new SchoolDayDao(DBHelper.getInstance(savedContext));
            weekSchedules = sdd.getSchedule(defaultSchedule, false);
            updateCurrentDaySchedules();
        }
    }

    /**
     * Обновляется список занятий для текущего выбранного дня на виджете. Если дефолтное расписание -
     * это расписание для группы, то список занятий фильтруется по дефолтной подгруппе, иначе показываются
     * все занятия на выбранный день
     */
    private void updateCurrentDaySchedules(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, dayOffset);
        List<Schedule> schedules;
        if(isLastUsingDailySchedule) {
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
            if (currentDay == Calendar.SUNDAY) {
                currentDay = 8;
            }
            schedules = getSchedulesByDayOfWeek(currentDay - 2, weekSchedules);
        } else{
            schedules = getSchedulesByDate(weekSchedules, dayOffset);
        }
        Integer currentWeekNumber = DateUtil.getWeek(calendar.getTime());
        boolean scheduleForGroup = FileUtil.isDefaultStudentGroup(savedContext);
        items = new ArrayList<>();
        if (currentWeekNumber != null) {
            String weekNumberAsString = currentWeekNumber.toString();
            for (Schedule schedule : schedules) {
                boolean matchSubgroupNumber = isMatchSubGroup(scheduleForGroup, defaultSubGroup, schedule);
                boolean matchWeekNumber = isMatchWeekNumber(isLastUsingDailySchedule, schedule, weekNumberAsString);

                if (matchSubgroupNumber && matchWeekNumber) {
                    items.add(schedule);
                }
            }
        }
    }

    /**
     * Проверяет проводится ли занятия для выбранной подгруппы. Если отображается расписание для
     * преподавателя, а не для группы, то мы отображаем занятия для всех подгрупп
     * @param scheduleForGroup переменная указывающая показываем расписание для группы, или для преподавателя
     * @param defaultSubGroup Дефолтная подгруппа
     * @param schedule Занятие которое нужно проверить
     * @return Возвращает true, если занятие нужно покзаать, иначе возвращает false
     */
    private static boolean isMatchSubGroup(boolean scheduleForGroup, Integer defaultSubGroup, Schedule schedule){
        boolean result = false;
        if (scheduleForGroup) {
            if (defaultSubGroup == 0) {
                result = true;
            } else if (schedule.getSubGroup().isEmpty()) {
                result = true;
            } else {
                if (defaultSubGroup.toString().equalsIgnoreCase(schedule.getSubGroup())) {
                    result = true;
                }
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Проверяет проводится ли занятие на выбранной неделе
     * @param isLastUsingDailySchedule Переменная указывающая что пользователь использовал в
     *                                 последний раз: расписание занятий или расписание экзаменов
     * @param schedule занятие которое нужно проверить
     * @param weekNumberAsString номер недели
     * @return возвращает true, если занятие нужно показывать, иначе возвращает false
     */
    private static boolean isMatchWeekNumber(boolean isLastUsingDailySchedule, Schedule schedule, String weekNumberAsString){
        boolean result = false;
        if(isLastUsingDailySchedule) {
            for (String weekNumber : schedule.getWeekNumbers()) {
                if (weekNumberAsString.equalsIgnoreCase(weekNumber)) {
                    result = true;
                }
            }
        } else{
            result = true;
        }
        return result;
    }

    /**
     * Формирует список занятий для выбранного пользователем дня. Метод используется если на
     * виджете отображается расписание занятий
     * @param dayOfWeek номер дня недели, который выбрал пользователь
     * @param weekSchedules список занятий для всех недели
     * @return Возвращает список занятий для выбранного дня
     */
    private List<Schedule> getSchedulesByDayOfWeek(Integer dayOfWeek, List<SchoolDay> weekSchedules){
        if(dayOfWeek >= 0 && dayOfWeek < weekDays.length) {
            String dayAsString = weekDays[dayOfWeek];
            for (SchoolDay schoolDay : weekSchedules) {
                if (schoolDay.getDayName().equalsIgnoreCase(dayAsString)) {
                    return schoolDay.getSchedules();
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Метод формирует список занятий для выбранного пользователем дня. Метод используется если
     * на виджете отображается расписание экзаменов
     * @param dateSchedules Список всех экзаменов
     * @param dayOffset номер дня, занятия которого нужно отобразить
     * @return Возвращает список экзаменов для выбранного пользователем дня
     */
    private List<Schedule> getSchedulesByDate(List<SchoolDay> dateSchedules, Integer dayOffset){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, dayOffset);
        String currentDateAsString = calendar.get(Calendar.DATE) + ".";
        //add 1 because month is zero-based
        Integer monthOrder = calendar.get(Calendar.MONTH) + 1;
        if(monthOrder < 10){
            currentDateAsString += "0" + monthOrder + ".";
        } else {
            currentDateAsString += monthOrder + ".";
        }
        currentDateAsString += Integer.toString(calendar.get(Calendar.YEAR));
        for(SchoolDay schoolDay : dateSchedules){
            if(schoolDay.getDayName().equalsIgnoreCase(currentDateAsString)){
                return schoolDay.getSchedules();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void onDestroy(){
        items.clear();
    }

    @Override
    public int getCount(){
        return items.size();
    }

    /**
     * Создает View для отображение одного занятия, которое потом отобразится в listView на виджете.
     * Занятия выбираются из списка занятий на текущий день
     * @param position номер занятия для которого нужно создать View
     * @return Возвращает созданное View
     */
    @Override
    public RemoteViews getViewAt(int position){
        updateCurrentDaySchedules();
        Schedule currentSchedule;
        RemoteViews result = new RemoteViews(savedContext.getPackageName(), R.layout.schedule_widget_item_layout);
        if(items.size() > position) {
            currentSchedule = items.get(position);

            String lessonTime = currentSchedule.getLessonTime();
            String[] times = lessonTime.split("-");
            if (times.length == 2) {
                result.setTextViewText(R.id.scheduleWidgetStartTime, times[0]);
                result.setTextViewText(R.id.scheduleWidgetEndTime, times[1]);
            }
            updateLessonTypeViewBackground(currentSchedule, result);

            String subject = currentSchedule.getSubject();
            if(!currentSchedule.getNote().isEmpty()){
                subject += " " + currentSchedule.getNote();
            }
            result.setTextViewText(R.id.scheduleWidgetSubjectName, subject);
            result.setTextViewText(R.id.scheduleWidgetAuditory, convertListString(currentSchedule.getAuditories(), ""));
        }
        return result;
    }

    /**
     * На основе типа занятия переданного schedule окрашивает view в определенный цвет
     * @param currentSchedule занятие из которого выбирается тип занятия
     * @param remoteViews Переменная с помощью которой окрашивает view в цвет
     */
    private void updateLessonTypeViewBackground(Schedule currentSchedule, RemoteViews remoteViews){
        switch(currentSchedule.getLessonType()){
            case "ПЗ":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, SET_BACKGROUND_RESOURCE, R.color.yellow);
                break;
            case "УПз":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, SET_BACKGROUND_RESOURCE, R.color.yellow);
                break;
            case "ЛК":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, SET_BACKGROUND_RESOURCE, R.color.green);
                break;
            case "УЛк":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, SET_BACKGROUND_RESOURCE, R.color.green);
                break;
            case "ЛР":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, SET_BACKGROUND_RESOURCE, R.color.red);
                break;
            default:
                remoteViews.setInt(R.id.lessonTypeViewInWidget, SET_BACKGROUND_RESOURCE, R.color.blue);
                break;
        }
    }

    /**
     * Метод конвертит лист значений в одну строку
     * @param values список значений
     * @param addition дополнителье сообщение
     * @return Возвращает строку
     */
    private static String convertListString(List<String> values, String addition){
        StringBuilder resultString = new StringBuilder();
        for(String value : values){
            resultString.append(value);
            resultString.append(", ");
        }
        if(resultString.length() > 2) {
            resultString.delete(resultString.length() - 2, resultString.length());
            resultString.append(addition);
        }
        return resultString.toString();
    }

    @Override
    public RemoteViews getLoadingView(){
        return null;
    }

    @Override
    public int getViewTypeCount(){
        return 1;
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public boolean hasStableIds(){
        return true;
    }

    @Override
    public void onDataSetChanged(){
        updateScheduleList();
    }
}

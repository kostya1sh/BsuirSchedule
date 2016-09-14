package by.bsuir.schedule.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by iChrome on 20.08.2015.
 */
public class DateUtil {
    public static final int COUNT_WEEK = 4;
    private static final int DAYS_IN_WEEK = 7;
    private static final int LAST_DAY_IN_DECEMBER = 31;
    private static final int IS_SUNDAY_APPEND_TO_WEEK_DEY = 6;
    private static final int APPEND_TO_WEEK_DEY = 2;

    private DateUtil(){
    }

    /**
     * Возвращает текущую дату ввиде строки
     * @return Возвращает текущую дату
     */
    public static String getCurrentDateAsString(){
        DateFormat df = DateFormat.getDateInstance();
        Date today = Calendar.getInstance().getTime();

        return df.format(today);
    }

    /**
     * Получаем текущую дату в секундах
     * @return текущая дата
     */
    public static Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    /**
     * Преобразуем строку в дату
     * @param date строка для преобразования
     * @return дата в секундах
     */
    public static Date getDateFromString(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e("DateUtil", ex.getMessage(), ex);
            return Calendar.getInstance().getTime();
        }
    }

    /**
     * Метод рассчитывает номер учебной недели для переданной даты
     * @param date Дата для которой нужно расчитать номер учебной недели
     * @return Возвращает номер недели
     */
    public static Integer getWeek(Date date) {
        Calendar today = new GregorianCalendar();
        today.setTime(date);
        int todayMonth = today.get(Calendar.MONTH);
        if (todayMonth == Calendar.AUGUST) {
            return null;
        }
        GregorianCalendar september = new GregorianCalendar();
        september.set(Calendar.MONTH, Calendar.SEPTEMBER);
        september.set(Calendar.DAY_OF_MONTH, 1);
        september.set(Calendar.YEAR, today.get(Calendar.YEAR));
        GregorianCalendar lastDayInYear = new GregorianCalendar();
        lastDayInYear.set(Calendar.MONTH, Calendar.DECEMBER);
        lastDayInYear.set(Calendar.DAY_OF_MONTH, LAST_DAY_IN_DECEMBER);
        lastDayInYear.set(Calendar.YEAR, today.get(Calendar.YEAR));
        if (todayMonth < Calendar.SEPTEMBER) {
            september.add(Calendar.YEAR, -1);
        }
        boolean isSunday = september.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        int controlDay = september.get(Calendar.DAY_OF_YEAR);
        int checkDay = today.get(Calendar.DAY_OF_YEAR);
        int dayDiff;
        if (checkDay >= controlDay) {
            dayDiff = checkDay - controlDay;
        } else {
            int totalDays = lastDayInYear.get(Calendar.DAY_OF_YEAR);
            dayDiff = totalDays - controlDay + checkDay;
        }
        if (isSunday) {
            dayDiff += IS_SUNDAY_APPEND_TO_WEEK_DEY;
        } else {
            dayDiff += (september.get(Calendar.DAY_OF_WEEK) - APPEND_TO_WEEK_DEY);
        }
        int weekDiff = dayDiff / DAYS_IN_WEEK;
        return weekDiff % COUNT_WEEK + 1;
    }
}

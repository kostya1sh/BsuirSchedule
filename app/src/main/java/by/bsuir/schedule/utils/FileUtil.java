package by.bsuir.schedule.utils;

import android.content.Context;
import android.content.SharedPreferences;

import by.bsuir.schedule.MainActivity;
import com.example.myapplication.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iChrome on 20.08.2015.
 */
public class FileUtil {
    private static final String DEFAULT_GROUP = "defaultGroup";
    private static final String DEFAULT_EMPLOYEE = "defaultEmployee";

    private FileUtil(){
    }

    /**
     * Формирует список всех скачанных ранее расписаний
     * @param context контекст
     * @param schedulesForGroups переменная указывающая нужно достать скачанные расписания для
     *                           групп, или для преподавателей
     * @return Возвращает список расписаний
     */
    public static List<String> getAllDownloadedSchedules(Context context, boolean schedulesForGroups){
        List<String> result = new ArrayList<>();
        for (File f : context.getFilesDir().listFiles()) {
            if(f.isFile()){
                String fileName = f.getName();
                addFileName(fileName, schedulesForGroups, result);
            }
        }
        return result;
    }

    /**
     * Метод проверяет нужно ли добавлять текущий файл в список расписаний
     * @param fileName имя файла которое проверяется
     * @param schedulesForGroups переменная указывающая формируем мы список расписаний для групп,
     *                           или для преподавателя
     * @param fileNameList список с уже добавленными расписаниями. В этом список добавляем fileName
     *                     если определим что он нужен
     */
    private static void addFileName(String fileName, boolean schedulesForGroups, List<String> fileNameList){
        if(".xml".equalsIgnoreCase(fileName.substring(fileName.length() - 4)) && !fileName.contains("exam")){
            if (schedulesForGroups && isDigit(fileName.charAt(0))) {
                fileNameList.add(fileName);
            } else if (!schedulesForGroups && !isDigit(fileName.charAt(0))) {
                fileNameList.add(fileName);
            }
        }
    }

    /**
     * Проверяет является ли переданный символ цифрой
     * @param symbol переданный символ
     * @return возвращает true если это символ, иначе false
     */
    public static boolean isDigit(char symbol){
        char[] digits = new char[] {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
        for(char tempSymbol : digits){
            if(tempSymbol == symbol){
                return true;
            }
        }
        return false;
    }

    /**
     * Возвращает текущее дефолтное расписание.
     * @param context контекст
     * @return Возвращает строку содержащую номер группы, или имя преподавателя чье расписание
     * теперь дефолтное
     */
    public static String getDefaultSchedule(Context context){
        String settingFileName = context.getString(R.string.setting_file_name);
        SharedPreferences preferences = context.getSharedPreferences(settingFileName, 0);
        String defaultGroup = preferences.getString(DEFAULT_GROUP, "none");
        if(!"none".equalsIgnoreCase(defaultGroup)){
            return defaultGroup + ".xml";
        } else{
            String defaultEmployee = preferences.getString(DEFAULT_EMPLOYEE, "none");
            if(!"none".equalsIgnoreCase(defaultEmployee)){
                return defaultEmployee + ".xml";
            }
        }
        return null;
    }

    /**
     * Возвращает дефолтное подгруппу. Эта подгруппа используется в виджете
     * @param context контекс
     * @return Возвращает null, если подгруппа не установлена, иначе возвращает номер подгруппы
     */
    public static Integer getDefaultSubgroup(Context context){
        String settingFileName = context.getString(R.string.setting_file_name);
        final SharedPreferences preferences = context.getSharedPreferences(settingFileName, 0);
        Integer defaultSubgroup = preferences.getInt(context.getString(R.string.default_subgroup), 0);
        if(defaultSubgroup != 0){
            return defaultSubgroup;
        } else {
            return null;
        }
    }

    /**
     * Функция определяет является ли дефолтное расписание расписание для группы
     * @param context контекст
     * @return Возвращает true если дефолтное расписание является расписанием для группы, иначе
     * возвращает false
     */
    public static boolean isDefaultStudentGroup(Context context){
        String settingFileName = context.getString(R.string.setting_file_name);
        SharedPreferences preferences = context.getSharedPreferences(settingFileName, 0);
        String defaultGroup = preferences.getString(context.getResources().getString(R.string.default_group_field_in_settings), "none");
        return !"none".equals(defaultGroup);
    }

    /**
     * Определяет какое расписание использователь пользователь в последний раз
     * @param context контест
     * @return Возвращает true, если пользователь в последний раз использовал расписание занятий,
     * иначе, если пользователь использовал расписание экзаменов, то возвращается false
     */
    public static boolean isLastUsingDailySchedule(Context context){
        String settingFileName = context.getString(R.string.setting_file_name);
        final SharedPreferences preferences = context.getSharedPreferences(settingFileName, 0);
        String lastUsingSchedule = preferences.getString(MainActivity.LAST_USING_SCHEDULE, "none");
        switch (lastUsingSchedule){
            case MainActivity.LAST_USING_DAILY_SCHEDULE_TAG:
                return true;
            case MainActivity.LAST_USING_EXAM_SCHEDULE_TAG:
                return false;
            default:
                return true;
        }
    }
}

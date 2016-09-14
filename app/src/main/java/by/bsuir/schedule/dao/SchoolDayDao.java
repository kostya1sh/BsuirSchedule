package by.bsuir.schedule.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import by.bsuir.schedule.model.Employee;
import by.bsuir.schedule.model.Schedule;
import by.bsuir.schedule.model.SchoolDay;
import by.bsuir.schedule.model.WeekDayEnum;
import by.bsuir.schedule.utils.DateUtil;
import by.bsuir.schedule.utils.EmployeeUtil;
import by.bsuir.schedule.utils.FileUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by iChrome on 29.12.2015.
 */
public class SchoolDayDao {
    private DBHelper dbHelper;
    static final Integer WEEK_WORKDAY_NUM = 6;
    static final Integer STUDENT_GROUP_LENGTH = 6;
    static final Integer TIME_HOUR_LENGTH = 2;
    static final Integer GR_SCHEDULE_AVAILABLE = 2;
    static final Integer GROUP_NAME = 1;
    static final Integer EMP_SCHEDULE_AVAILABLE = 5;
    static final Integer LAST_NAME = 2;
    static final Integer FIRST_NAME = 1;
    static final Integer MIDDLE_NAME = 3;


    /**
     * Класс для работы с базой данных
     * @param helper база данных
     */
    public SchoolDayDao(DBHelper helper){
        setDbHelper(helper);
    }

    /**
     * Метод который сохраняет расписание в базу данных.
     * @param week расписание, которое надо сохранить
     */
    public void saveSchoolWeekToDataBase(List<SchoolDay> week){
        for(SchoolDay schoolDay : week){
            for(Schedule schedule : schoolDay.getSchedules()){
                WeekDayEnum weekDay = WeekDayEnum.getDayByName(schoolDay.getDayName());

                //if weekDay == date
                //weekDay == date if schedule is for exam
                if (schoolDay.getDayName().contains(".")) {
                    schedule.setDate(schoolDay.getDayName());
                }

                if(weekDay != null) {

                    schedule.setWeekDay((long) weekDay.getOrder());
                }
                getDbHelper().addScheduleToDataBase(schedule);
            }
        }
    }


    /**
     * Метод который удаляет расписание из базы данных
     * если нужно удалить расписание для обновления то не удаляем записи в которых колонка IS_MANUAL == true.
     * @param fileName название расписания, которое надо удалить
     * @param isForRefresh флаг, который показывает удаляем или обновляем расписание
     * @return возвращает количество удаленных строк или -1 если случилась ошибка
     */
    public Integer deleteSchedule(String fileName, boolean isForRefresh) {
        if (FileUtil.isDigit(fileName.charAt(0))) {
            return deleteGroupSchedule(fileName, isForRefresh);
        } else {
            return deleteTeacherSchedule(fileName, isForRefresh);
        }
    }

    private Integer deleteGroupSchedule(String fileName, boolean isForRefresh) {
        List<Cursor> tableCursor;
        String studentGroupName = fileName.substring(0, STUDENT_GROUP_LENGTH);
        String query = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName);
        tableCursor = getDbHelper().getData(query);
        Integer deletedRowNum = 0;

        if (tableCursor.get(0) != null) {
            tableCursor.get(0).moveToFirst();
        } else {
            Log.d("DB", "schedule table doesn't contain this group!");
            return -1;
        }

        do {
            if (!isAvailableTeacherId(tableCursor.get(0).getString(0))) {
               deletedRowNum = checkForRefresh(tableCursor, deletedRowNum, isForRefresh);
            }
        } while (tableCursor.get(0).moveToNext());

        tableCursor.get(0).close();
        tableCursor.get(1).close();

        return deletedRowNum;
    }

    private Integer checkForRefresh(List<Cursor> tableCursor, Integer deletedRowNum, boolean isForRefresh) {
        int dels = deletedRowNum;
        if (isForRefresh) {
            if (tableCursor.get(0) == null) {
                deleteScheduleTableRow(tableCursor.get(0).getString(0));
                dels++;
            }
            if (!"true".equalsIgnoreCase(tableCursor.get(0).getString(tableCursor.get(0)
                    .getColumnIndex(DBColumns.IS_MANUAL)))) {
                deleteScheduleTableRow(tableCursor.get(0).getString(0));
                dels++;
            }
        } else {
            deleteScheduleTableRow(tableCursor.get(0).getString(0));
            dels++;
        }
        return dels;
    }

    private boolean isAvailableTeacherId(String id) {
        List<Cursor> teacherTableCursor;
        String query = "select * from employee where " +
                DBColumns.EMP_SCHEDULE_AVAILABLE + " = 'true'";
        teacherTableCursor = getDbHelper().getData(query);

        if (teacherTableCursor.get(0) != null) {
            teacherTableCursor.get(0).moveToFirst();
        } else {
            Log.d("DB ", "All teachers is unavailable!");
            return false;
        }

        do {
            if (teacherTableCursor.get(0).getString(0).equals(id)) {
                teacherTableCursor.get(0).close();
                teacherTableCursor.get(1).close();
                return true;
            }
        } while(teacherTableCursor.get(0).moveToNext());

        teacherTableCursor.get(0).close();
        teacherTableCursor.get(1).close();
        return false;
    }

    private boolean isAvailableGroupId(String id) {
        List<Cursor> groupTableCursor;
        String query = "select * from student_group where " +
                BaseColumns._ID + " = " + id;
        groupTableCursor = getDbHelper().getData(query);

        if (groupTableCursor.get(0) != null) {
            groupTableCursor.get(0).moveToFirst();
        } else {
            Log.d("DB ", "All groups is unavailable!");
            return false;
        }


        if (groupTableCursor.get(0).getString(
                groupTableCursor.get(0).getColumnIndex(
                        DBColumns.GR_SCHEDULE_AVAILABLE)) != null) {
            if ("true".equals(groupTableCursor.get(0).getString(
                    groupTableCursor.get(0).getColumnIndex(
                            DBColumns.GR_SCHEDULE_AVAILABLE)))) {
                Log.d("Data Base", groupTableCursor.get(0).getString(groupTableCursor.get(0).getColumnIndex(
                        DBColumns.GR_SCHEDULE_AVAILABLE)));
                groupTableCursor.get(0).close();
                groupTableCursor.get(1).close();
                return true;
            }
            else {
                groupTableCursor.get(0).close();
                groupTableCursor.get(1).close();
                return false;
            }
        }
        else {
            groupTableCursor.get(0).close();
            groupTableCursor.get(1).close();
            return false;
        }
    }

    private Integer deleteTeacherSchedule(String fileName,  boolean isForRefresh) {
        List<Cursor> tableCursor;
        String lastName = EmployeeUtil.getEmployeeLastNameFromFile(fileName);
        String query = "select * from schedule";
        tableCursor = getDbHelper().getData(query);
        Integer deletedRowNum = 0;
        List<String> scheduleId = new ArrayList<>();

        if (tableCursor.get(0) != null) {
            tableCursor.get(0).moveToFirst();
        } else {
            Log.d("DB", "schedule table doesn't contain this group!");
            return -1;
        }

        List<Cursor> cursor;
        query = "select se_id_schedule from schedule_employee where se_id_employee = "
                + getEmployeeId(lastName);

        cursor = getDbHelper().getData(query);

        cursor.get(0).moveToFirst();
        do {
            scheduleId.add(cursor.get(0).getString(0));
        } while (cursor.get(0).moveToNext());

        do {
            if (isTeacherId(scheduleId, tableCursor.get(0).getString(0)) && !isAvailableGroupId(tableCursor.get(0).getString(
                    tableCursor.get(0).getColumnIndex(DBColumns.STUDENT_GROUP_ID_COLUMN)))) {
                deletedRowNum = checkForRefresh(tableCursor, deletedRowNum, isForRefresh);
            }
        } while (tableCursor.get(0).moveToNext());

        tableCursor.get(0).close();
        tableCursor.get(1).close();

        cursor.get(0).close();
        cursor.get(1).close();

        return deletedRowNum;
    }

    /**
     * Метод который удаляет строку из базы данных
     * @param id - id строки для удаления
     * @return возвращает количество удаленных строк или -1 если случилась ошибка
     */
    public Integer deleteScheduleTableRow(String id) {
        return getDbHelper().getReadableDatabase().delete("schedule", "_id = " + id, null);
    }

    /**
     * Метод который получает расписание из базы данных
     * @param fileName - название расписания
     * @param isForExam - расписание экзаменов или занятий
     * @return расписание для выбранной группы или преподавателя
     */
    public List<SchoolDay> getSchedule(String fileName, boolean isForExam) {
        Log.d("Current date", DateUtil.getCurrentDateAsString());
        if (FileUtil.isDigit(fileName.charAt(0))) {
            if (isForExam) {
                return getExamScheduleForStudentGroup(fileName);
            } else {
                return getScheduleForStudentGroup(fileName);
            }
        } else {
            return getScheduleForTeacher(fileName, isForExam);
        }
    }

    //расписание для преподавателей
    private List<SchoolDay> getScheduleForTeacher(String  fileName, boolean isForExam) {
        List<SchoolDay> weekSchedule = new ArrayList<>();
        List<String> scheduleId = new ArrayList<>();
        String lastName = EmployeeUtil.getEmployeeLastNameFromFile(fileName);

        List<Cursor> cursor;
        String query = "select se_id_schedule from schedule_employee where se_id_employee = "
                + getEmployeeId(lastName);

        cursor = getDbHelper().getData(query);
        if (cursor.get(0) == null)
            return new ArrayList<>();

        cursor.get(0).moveToFirst();
        do {
            scheduleId.add(cursor.get(0).getString(0));
        } while (cursor.get(0).moveToNext());

        String[] buf = scheduleId.toArray(new String[scheduleId.size()]);
        Log.d("sch id = ", Integer.toString(buf.length));

        if (isForExam) {
            return fillExamScheduleForTeacher(scheduleId, lastName);
        } else {
            for (int i = 0; i < WEEK_WORKDAY_NUM; i++) {
                SchoolDay tmp;
                if ((tmp = fillSchoolDayForTeacher(scheduleId, (long) (i + 1))) != null) {
                    weekSchedule.add(tmp);
                }
            }
        }

        for (SchoolDay sd : weekSchedule) {
            Log.d("SCHEDULE DAY = ", sd.getDayName());
            for (Schedule schedule : sd.getSchedules()) {
                Log.d("schedule = ", schedule.getLessonTime() + " " +
                        schedule.getLessonType() + " " +
                        schedule.getStudentGroup() + " " +
                        schedule.getSubject() + " " +
                        schedule.getSubGroup() + " " +
                        schedule.getWeekNumbers() + " " +
                        schedule.getWeekDay() + " ");
            }
        }

        cursor.get(0).close();
        cursor.get(1).close();

        return weekSchedule;
    }

    private List<SchoolDay> getExamScheduleForStudentGroup(String fileName) {
        SchoolDay currentSchoolDay;
        Schedule currentSchedule;
        List<Schedule> currentScheduleList;
        String studentGroupName = fileName.substring(0, STUDENT_GROUP_LENGTH);
        List<SchoolDay> sdList = new ArrayList<>();


        List<Cursor> scheduleTableCursor;
        String scheduleTableQuery = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName) + " and " + "week_day IS NULL";
        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        String lessonTime;
        String lessonType;
        String subject;
        String subGroup;
        String groupName;
        String note;
        List<Employee> employees;
        List<String> auds;
        String isHidden;
        String date;
        List<String> dateList = new ArrayList<>();

        if (scheduleTableCursor.get(0) == null)
            return new ArrayList<>();

        scheduleTableCursor.get(0).moveToFirst();

        //fill SchoolDay
        do {
            currentSchedule = new Schedule();
            currentScheduleList = new ArrayList<>();
            currentSchoolDay = new SchoolDay();


            if ((date = scheduleTableCursor.get(0).getString(scheduleTableCursor.get(0)
                    .getColumnIndex(DBColumns.DATE_COLUMN))) != null) {
                currentSchedule.setDate(date);
            }

            // если эта дата уже обрабатывалась
            if (dateList.contains(date)) {
                continue;
            }

            dateList.add(date);

            List<Cursor> dateCursor;
            dateCursor = getDbHelper().getData("select * from schedule where date = '" + date + "'"
                    + " and " + DBColumns.STUDENT_GROUP_ID_COLUMN + " = '" + getGroupId(studentGroupName) + "'");

            if (dateCursor.get(0) != null) {
                dateCursor.get(0).moveToFirst();
            }

            //получаем все расписания для этой даты
            int dateRowNum = 0;
            do {
                currentSchedule = new Schedule();

                if (dateCursor.get(0) == null)
                    continue;

                if ((isHidden = dateCursor.get(0).getString(dateCursor.get(0)
                        .getColumnIndex(DBColumns.IS_HIDDEN))) != null
                        && "true".equals(isHidden)) {
                    currentSchedule.setHidden(true);
                } else currentSchedule.setHidden(false);

                currentSchedule.setDate(date);

                if ((lessonTime = getLessonTimeById(getLessonTimeIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                    currentSchedule.setLessonTime(lessonTime);
                } else currentSchedule.setLessonTime(null);

                if ((lessonType = getLessonTypeFromScheduleTable(dateRowNum, dateCursor)) != null) {
                    currentSchedule.setLessonType(lessonType);
                } else currentSchedule.setLessonType(null);

                if ((subject = getSubjectById(getSubjectIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                    currentSchedule.setSubject(subject);
                } else currentSchedule.setSubject(null);

                if ((subGroup = getSubGroupFromScheduleTable(dateRowNum, dateCursor)) != null && !"0".equals(subGroup)) {
                    currentSchedule.setSubGroup(subGroup);
                } else currentSchedule.setSubGroup("");

                if ((groupName = getGroupNameById(getGroupIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                    currentSchedule.setStudentGroup(groupName);
                } else currentSchedule.setStudentGroup("");

                if ((employees = getEmployeeForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                    currentSchedule.setEmployeeList(employees);
                } else currentSchedule.setEmployeeList(null);

                if ((auds = getAudsForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                    currentSchedule.setAuditories(auds);
                } else currentSchedule.setAuditories(Arrays.asList(""));

                if ((note = getNoteForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                    currentSchedule.setNote(note);
                } else currentSchedule.setNote("");

                currentSchedule.setScheduleTableRowId(dateCursor.get(0).getString(
                        dateCursor.get(0).getColumnIndex(BaseColumns._ID)));

                currentScheduleList.add(currentSchedule);
                dateRowNum++;
            } while (dateCursor.get(0).moveToNext());


            currentSchoolDay.setSchedules(currentScheduleList);
            currentSchoolDay.setDayName(date);

            sdList.add(currentSchoolDay);
        } while (scheduleTableCursor.get(0).moveToNext());

        scheduleTableCursor.get(0).close();
        scheduleTableCursor.get(1).close();

        sdList = sortExamScheduleForCurrentDay(sdList);
        sdList = sortDatesLowerThenCurrent(sdList);

        return sdList;
    }

    /**
     * Метод сортирует расписание экзаменов так чтобы на первом месте был ближайщий экзамен
     * @param examSchedule не отсортированное расписание
     * @return отсортированное расписание
     */
    private List<SchoolDay> sortExamScheduleForCurrentDay(List<SchoolDay> examSchedule) {
        Date currentDate = DateUtil.getCurrentDate();
        int position = 0;
        int iter = 0;
        Date bufDate = currentDate;

        //Ищем дату наиболее близкую к сегодняшней
        //и запоминаем ее позицию
        for (SchoolDay sd : examSchedule) {
            if (DateUtil.getDateFromString(sd.getDayName()) != null) {

                if (DateUtil.getDateFromString(sd.getDayName()).getDate() == currentDate.getDate()) {
                    position = iter;
                    break;
                }
                if (DateUtil.getDateFromString(sd.getDayName()).getTime() >= currentDate.getTime() && ((bufDate.getTime() - currentDate.getTime()) >= (DateUtil.getDateFromString(sd.getDayName()).getTime() - currentDate.getTime()) || (bufDate.getTime() - currentDate.getTime()) <= 0)) {
                    bufDate = DateUtil.getDateFromString(sd.getDayName());
                    position = iter;
                }
            }

            iter++;
        }


        examSchedule = sortExamScheduleRelativeToThePosition(examSchedule, position);

        return examSchedule;
    }

    /**
     * Метод ставит элемент с номекром = position на первое место
     * и смещает сотальные элементы относительно его
     * @param examSchedule расписание для сортировки
     * @param position пзиция элемента который надо поставить первым
     * @return отсортированное относительно элемнта[position] расписание
     */
    private List<SchoolDay> sortExamScheduleRelativeToThePosition(List<SchoolDay> examSchedule, int position) {
        //если найдена наиболее близкая дата
        if (position > 0) {
            //ставим наиболее близкую дату на первое место и смещаем остальные относительно ее
            for (int i = 0; i < examSchedule.size(); i++) {

                if (position + i >= examSchedule.size())
                    break;

                SchoolDay sdBuf;
                sdBuf = examSchedule.get(i);
                examSchedule.set(i, examSchedule.get(position + i));
                examSchedule.set(position + i, sdBuf);
            }
        }

        return examSchedule;
    }

    /**
     * Сортируем даты меньше текущей в порядке возрастания
     * @param examSchedule расписание
     * @return отсортированное расписание
     */
    private List<SchoolDay> sortDatesLowerThenCurrent(List<SchoolDay> examSchedule) {
        Date currentDate = DateUtil.getCurrentDate();
        int i = 0;

        //находим начало дат которые меньше чем текущая
        while (true) {
            if (DateUtil.getDateFromString(examSchedule.get(i).getDayName()).getDate() < currentDate.getDate()) {
                break;
            }
            i++;
        }

        //сортировка дат которые меньше чем текущая
        for (int k = i; k < examSchedule.size(); k++) {
            for (int j = i; j < examSchedule.size(); j++) {
                if (DateUtil.getDateFromString(examSchedule.get(k).getDayName()).getDate() <
                        DateUtil.getDateFromString(examSchedule.get(j).getDayName()).getDate()) {
                    SchoolDay sdBuf;
                    sdBuf = examSchedule.get(k);
                    examSchedule.set(k, examSchedule.get(j));
                    examSchedule.set(j, sdBuf);
                }
            }
        }

        return examSchedule;
    }

    private List<SchoolDay> fillExamScheduleForTeacher(List<String> scheduleId, String lastName) {
        SchoolDay currentSchoolDay;
        Schedule currentSchedule;
        List<Schedule> currentScheduleList;
        List<SchoolDay> sdList = new ArrayList<>();

        List<Cursor> scheduleTableCursor;
        String scheduleTableQuery = "select * from schedule where week_day IS NULL";
        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        String lessonTime;
        String lessonType;
        String subject;
        String subGroup;
        String groupName;
        String note;
        List<Employee> employees;
        List<String> auds;
        String isHidden;
        String date;
        List<String> dateList = new ArrayList<>();

        if (scheduleTableCursor.isEmpty()) {
            Log.d("cursor", " is empty");
        }
        else if (scheduleTableCursor.get(0) != null) {
                scheduleTableCursor.get(0).moveToFirst();

        } else {
            return new ArrayList<>();
        }

        //fill SchoolDay
        do {
            if (isTeacherId(scheduleId, scheduleTableCursor.get(0).getString(0))) {
                currentSchedule = new Schedule();
                currentScheduleList = new ArrayList<>();
                currentSchoolDay = new SchoolDay();


                if ((date = scheduleTableCursor.get(0).getString(scheduleTableCursor.get(0)
                        .getColumnIndex(DBColumns.DATE_COLUMN))) != null) {
                    currentSchedule.setDate(date);
                }

                // if schedule for this date was processed continue
                if (dateList.contains(date)) {
                    continue;
                }

                dateList.add(date);

                List<Cursor> dateCursor;
                dateCursor = getDbHelper().getData("select * from schedule where date = '" + date + "'");

                dateCursor.get(0).moveToFirst();

                int dateRowNum = 0;
                do {
                    currentSchedule = new Schedule();
                    if (!isTeacherId(scheduleId, dateCursor.get(0).getString(0))) {
                        Log.d("SDD", dateCursor.get(0).getString(0));
                        continue;
                    }

                    if (dateCursor.get(0) == null)
                        continue;

                    if ((isHidden = dateCursor.get(0).getString(dateCursor.get(0)
                            .getColumnIndex(DBColumns.IS_HIDDEN))) != null
                            && "true".equals(isHidden)) {
                        currentSchedule.setHidden(true);
                    } else currentSchedule.setHidden(false);


                    if ((lessonTime = getLessonTimeById(getLessonTimeIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                        currentSchedule.setLessonTime(lessonTime);
                    } else currentSchedule.setLessonTime(null);

                    if ((lessonType = getLessonTypeFromScheduleTable(dateRowNum, dateCursor)) != null) {
                        currentSchedule.setLessonType(lessonType);
                    } else currentSchedule.setLessonType(null);

                    if ((subject = getSubjectById(getSubjectIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                        currentSchedule.setSubject(subject);
                    } else currentSchedule.setSubject(null);

                    if ((subGroup = getSubGroupFromScheduleTable(dateRowNum, dateCursor)) != null && !"0".equals(subGroup)) {
                        currentSchedule.setSubGroup(subGroup);
                    } else currentSchedule.setSubGroup("");

                    if ((groupName = getGroupNameById(getGroupIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                        currentSchedule.setStudentGroup(groupName);
                    } else currentSchedule.setStudentGroup("");

                    if ((employees = getEmployeeForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                        currentSchedule.setEmployeeList(employees);
                    } else currentSchedule.setEmployeeList(null);

                    if ((auds = getAudsForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                        currentSchedule.setAuditories(auds);
                    } else currentSchedule.setAuditories(Arrays.asList(""));

                    if ((note = getNoteForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                        currentSchedule.setNote(note);
                    } else currentSchedule.setNote("");

                    currentSchedule.setScheduleTableRowId(dateCursor.get(0).getString(
                            dateCursor.get(0).getColumnIndex(BaseColumns._ID)));

                    if (employees != null && employees.get(0) != null) {
                        if (employees.get(0).getLastName().equals(lastName)) {
                            currentScheduleList.add(currentSchedule);
                        }
                    }
                    dateRowNum++;
                } while (dateCursor.get(0).moveToNext());


                currentSchoolDay.setSchedules(currentScheduleList);
                currentSchoolDay.setDayName(date);

                sdList.add(currentSchoolDay);
            }
        } while (scheduleTableCursor.get(0).moveToNext());

        scheduleTableCursor.get(0).close();
        scheduleTableCursor.get(1).close();

        sdList = sortExamScheduleForCurrentDay(sdList);

        return sdList;
    }

    //расписание для группы
    private List<SchoolDay> getScheduleForStudentGroup(String groupFileName) {
        List<SchoolDay> weekSchedule = new ArrayList<>();
        Log.d("file name = ", groupFileName);
        String studentGroupName = groupFileName.substring(0, STUDENT_GROUP_LENGTH);

        List<Cursor> scheduleTableCursor;
        String scheduleTableQuery = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName);

        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        if (scheduleTableCursor.get(0) == null)
            return new ArrayList<>();

        scheduleTableCursor.get(0).moveToLast();

        for (int i = 0; i < WEEK_WORKDAY_NUM; i++) {
            SchoolDay tmp;
            if ((tmp = fillSchoolDayForGroup(studentGroupName, (long) (i + 1))) != null) {
                weekSchedule.add(tmp);
            }
        }

        for (SchoolDay sd: weekSchedule) {
            Log.d("SCHEDULE DAY = ", sd.getDayName());
            for (Schedule schedule: sd.getSchedules()) {
                Log.d("schedule = ", schedule.getLessonTime() + " " +
                        schedule.getLessonType() + " " +
                        schedule.getStudentGroup() + " " +
                        schedule.getSubject() + " " +
                        schedule.getSubGroup() + " " +
                        schedule.getWeekNumbers() + " " +
                        schedule.getWeekDay() + " ");
            }
        }

        scheduleTableCursor.get(0).close();
        scheduleTableCursor.get(1).close();

        return weekSchedule;
    }

    //распимание для преподавателя на один день
    private SchoolDay fillSchoolDayForTeacher(List<String> scheduleId,@NonNull Long weekDay) {
        SchoolDay currentSchoolDay = new SchoolDay();
        Schedule currentSchedule;
        List<Schedule> currentScheduleList = new ArrayList<>();

        List<Cursor> scheduleTableCursor;
        String scheduleTableQuery = "select * from schedule where " + "week_day = " + weekDay
                + " order by id_lesson_time";
        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        String lessonTime;
        String lessonType;
        String subject;
        String subGroup;
        String groupName;
        String[] weekNumbers;
        List<Employee> employees;
        List<String> auds;
        String isHidden;
        Long wd;


        if (scheduleTableCursor.get(0) == null)
            return null;

        scheduleTableCursor.get(0).moveToFirst();



        int rowNum = 0;

        //fill SchoolDay
        do {
            if (isTeacherId(scheduleId, scheduleTableCursor.get(0).getString(0))) {
                currentSchedule = new Schedule();

                if ((isHidden = scheduleTableCursor.get(0).getString(scheduleTableCursor.get(0)
                        .getColumnIndex(DBColumns.IS_HIDDEN))) != null
                        && "true".equals(isHidden)) {

                    currentSchedule.setHidden(true);
                } else currentSchedule.setHidden(false);

                if ((lessonTime = getLessonTimeById(getLessonTimeIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                    currentSchedule.setLessonTime(lessonTime);
                } else currentSchedule.setLessonTime(null);

                if ((lessonType = getLessonTypeFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                    currentSchedule.setLessonType(lessonType);
                } else currentSchedule.setLessonType(null);

                if ((subject = getSubjectById(getSubjectIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                    currentSchedule.setSubject(subject);
                } else currentSchedule.setSubject(null);

                if ((groupName = getGroupNameById(getGroupIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                    currentSchedule.setStudentGroup(groupName);
                } else currentSchedule.setStudentGroup("");

                if ((subGroup = getSubGroupFromScheduleTable(rowNum, scheduleTableCursor)) != null && !"0".equals(subGroup)) {
                    currentSchedule.setSubGroup(subGroup);
                } else currentSchedule.setSubGroup("");

                if ((wd = getWeekDayFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                    currentSchedule.setWeekDay(wd);
                } else currentSchedule.setWeekDay(null);

                if ((weekNumbers = getWeekNumsFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                    currentSchedule.setWeekNumbers(Arrays.asList(weekNumbers));
                } else currentSchedule.setWeekNumbers(null);

                Log.d("day num = ", Long.toString(weekDay));
                if ((employees = getEmployeeForScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                    currentSchedule.setEmployeeList(employees);
                } else currentSchedule.setEmployeeList(null);

                if ((auds = getAudsForScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                    currentSchedule.setAuditories(auds);
                } else currentSchedule.setAuditories(Arrays.asList(""));

                currentSchedule.setScheduleTableRowId(scheduleTableCursor.get(0).getString(
                        scheduleTableCursor.get(0).getColumnIndex(BaseColumns._ID)));

                currentScheduleList.add(currentSchedule);
            }
            rowNum++;
        } while (scheduleTableCursor.get(0).moveToNext());

        for (Schedule s: currentScheduleList) {
            Log.d("full list ", s.getLessonTime() + s.getLessonType() +
                    s.getSubject() + s.getWeekNumbers());
        }

        currentScheduleList = sortScheduleListByTime(currentScheduleList);

        currentSchoolDay.setSchedules(currentScheduleList);

        if (weekDay == null)
            currentSchoolDay.setDayName(null);
        else {
            switch (weekDay.toString()) {
                case "1":
                    currentSchoolDay.setDayName("Понедельник");
                    break;
                case "2":
                    currentSchoolDay.setDayName("Вторник");
                    break;
                case "3":
                    currentSchoolDay.setDayName("Среда");
                    break;
                case "4":
                    currentSchoolDay.setDayName("Четверг");
                    break;
                case "5":
                    currentSchoolDay.setDayName("Пятница");
                    break;
                case "6":
                    currentSchoolDay.setDayName("Суббота");
                    break;
                default:
                    currentSchoolDay.setDayName("Ошибка");
                    break;
            }
        }

        scheduleTableCursor.get(0).close();
        scheduleTableCursor.get(1).close();

        return currentSchoolDay;
    }

    //проверяет принадлежит ли запись в таблице преподавателю
    private static boolean isTeacherId(List<String> scheduleId, String value) {
        for(String str: scheduleId) {
            if (str.equals(value))
                return true;
        }
        return false;
    }

    //заполняет расписание на один день для группы
    private SchoolDay fillSchoolDayForGroup(String studentGroupName,@NonNull Long weekDay) {
        SchoolDay currentSchoolDay = new SchoolDay();
        Schedule currentSchedule;
        List<Schedule> currentScheduleList = new ArrayList<>();

        List<Cursor> scheduleTableCursor;
        String scheduleTableQuery = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName) + " and " + "week_day = " + weekDay + " order by id_lesson_time";
        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        String lessonTime;
        String lessonType;
        String subject;
        String subGroup;
        String groupName;
        String[] weekNumbers;
        String note;
        List<Employee> employees;
        List<String> auds;
        String isHidden;
        String date;
        Long wd;

        if (scheduleTableCursor.get(0) != null) {
            scheduleTableCursor.get(0).moveToFirst();
        } else {
            return null;
        }
        int rowNum = 0;

        //fill SchoolDay
        do {
            currentSchedule = new Schedule();

            if ((isHidden = scheduleTableCursor.get(0).getString(scheduleTableCursor.get(0)
                    .getColumnIndex(DBColumns.IS_HIDDEN))) != null
                    && "true".equals(isHidden)) {

                currentSchedule.setHidden(true);
            }
            else currentSchedule.setHidden(false);


            if ((lessonTime = getLessonTimeById(getLessonTimeIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                currentSchedule.setLessonTime(lessonTime);
            }
            else currentSchedule.setLessonTime(null);

            if ((lessonType = getLessonTypeFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setLessonType(lessonType);
            }
            else currentSchedule.setLessonType(null);

            if ((subject = getSubjectById(getSubjectIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                currentSchedule.setSubject(subject);
            }
            else currentSchedule.setSubject(null);

            if ((subGroup = getSubGroupFromScheduleTable(rowNum, scheduleTableCursor)) != null)  {
                if (!"0".equals(subGroup)) {
                    currentSchedule.setSubGroup(subGroup);
                }
                else currentSchedule.setSubGroup("");
            }
            else currentSchedule.setSubGroup("");

            if ((groupName = getGroupNameById(getGroupIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                currentSchedule.setStudentGroup(groupName);
            } else currentSchedule.setStudentGroup("");

            if ((wd = getWeekDayFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setWeekDay(wd);
            }
            else currentSchedule.setWeekDay(null);

            if ((weekNumbers = getWeekNumsFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setWeekNumbers(Arrays.asList(weekNumbers));
            }
            else currentSchedule.setWeekNumbers(null);

            Log.d("day num = ", Long.toString(weekDay));
            if ((employees = getEmployeeForScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setEmployeeList(employees);
            }
            else currentSchedule.setEmployeeList(null);

            if ((auds = getAudsForScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setAuditories(auds);
            }
            else currentSchedule.setAuditories(Arrays.asList(""));

            if ((note = getNoteForScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setNote(note);
            } else currentSchedule.setNote("");

            currentSchedule.setScheduleTableRowId(scheduleTableCursor.get(0).getString(
                    scheduleTableCursor.get(0).getColumnIndex(BaseColumns._ID)));

            if ((date = scheduleTableCursor.get(0).getString(scheduleTableCursor.get(0)
                    .getColumnIndex(DBColumns.DATE_COLUMN))) != null) {
                currentSchedule.setDate(date);
            } else currentSchedule.setDate("");


            currentScheduleList.add(currentSchedule);
            rowNum++;
        } while (scheduleTableCursor.get(0).moveToNext());

        currentScheduleList = sortScheduleListByTime(currentScheduleList);

        currentSchoolDay.setSchedules(currentScheduleList);

        if (weekDay == null)
            currentSchoolDay.setDayName(null);
        else {
            switch (weekDay.toString()) {
                case "1":
                    currentSchoolDay.setDayName("Понедельник");
                    break;
                case "2":
                    currentSchoolDay.setDayName("Вторник");
                    break;
                case "3":
                    currentSchoolDay.setDayName("Среда");
                    break;
                case "4":
                    currentSchoolDay.setDayName("Четверг");
                    break;
                case "5":
                    currentSchoolDay.setDayName("Пятница");
                    break;
                case "6":
                    currentSchoolDay.setDayName("Суббота");
                    break;
                default:
                    currentSchoolDay.setDayName("Ошибка");
                    break;
            }
        }

        scheduleTableCursor.get(0).close();
        scheduleTableCursor.get(1).close();

        return currentSchoolDay;
    }

    /**
     * Метод который сортирует расписание по времени, от меньшего к большему
     * @param schedules - расписание
     * @return возвращает отсортированное расписание
     */
    public List<Schedule> sortScheduleListByTime(List<Schedule> schedules) {
        for (int i = 0; i < schedules.size(); i++) {
            for (int j = i; j < schedules.size(); j++) {
                if (!isTimeLess(schedules.get(i).getLessonTime(), schedules.get(j).getLessonTime())) {
                    Schedule buf;
                    buf = schedules.get(i);
                    schedules.set(i, schedules.get(j));
                    schedules.set(j, buf);
                }
            }
        }

        return schedules;
    }

    private static boolean isTimeLess(String firstTime, String secondTime) {
        if (firstTime == null || secondTime == null)
            return true;

        if (firstTime.length() <= 0 || secondTime.length() <= 0) {
            return true;
        }
        try {
                Integer first = Integer.valueOf(firstTime.substring(0, TIME_HOUR_LENGTH));
                Integer second = Integer.valueOf(secondTime.substring(0, TIME_HOUR_LENGTH));
                return first < second;
        }
        catch (NumberFormatException ex) {
            Integer first = Integer.valueOf(firstTime.substring(0, 1));
            Integer second = Integer.valueOf(secondTime.substring(0, 1));
            return first < second;
        }


    }

    //получить id группы по навзанию группы
    private String getGroupId(String studentGroupName) {
        String id;
        List<Cursor> groupNameCursor;
        String getGroupIdQuery = "select * from student_group where student_group_name = "
                + studentGroupName;
        groupNameCursor = getDbHelper().getData(getGroupIdQuery);
        Log.d("STUDENT GROUP NAME = ", studentGroupName);
        if (groupNameCursor.get(0) != null) {
            id = groupNameCursor.get(0).getString(0);
            groupNameCursor.get(0).close();
        } else {
            id = "2";
        }
        Log.d("selected group id = ", id);


        groupNameCursor.get(1).close();
        return id;
    }

    //получает номера недель из строки в таблицы расписании
    private String[] getWeekNumsFromScheduleTable(Integer rowNum, List<Cursor> c) {
        String[] weekNums = {"1", "2", "3", "4"};

        if (c.get(0) == null)
            return weekNums;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);


        return c.get(0).getString(c.get(0).getColumnIndex(DBColumns.WEEK_NUMBER_COLUMN)).split("\\, ");

    }

    //получае день недели из строки в таблицы расписании
    private Long getWeekDayFromScheduleTable(Integer rowNum, List<Cursor> c) {

        if (c.get(0) == null)
            return null;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        return c.get(0).getLong(c.get(0).getColumnIndex(DBColumns.WEEK_DAY_COLUMN));
    }

    //получает подгруппу из строки в таблице расписании
    private String getSubGroupFromScheduleTable(Integer rowNum, List<Cursor> c) {

        if (c.get(0) == null)
            return null;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        return c.get(0).getString(c.get(0).getColumnIndex(DBColumns.SUBGROUP_COLUMN));
    }

    private String getNoteForScheduleTableRow(Integer rowNum, List<Cursor> c) {

        if (c.get(0) == null)
            return null;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        String rowId = c.get(0).getString(c.get(0).getColumnIndex(BaseColumns._ID));
        String query = "select " + DBColumns.NOTE_TEXT_COLUMN + " from note"  + " where " +
                DBColumns.NOTE_SCHEDULE_ID_COLUMN + " = " + rowId;

        List<Cursor> noteCursor = getDbHelper().getData(query);

        if (noteCursor.get(0) != null) {
            Log.d("Date Base", " Row with id " + rowId + " have note = " + noteCursor.get(0).getString(0));

            String retValue = noteCursor.get(0).getString(0);
            noteCursor.get(0).close();
            noteCursor.get(1).close();

            return retValue;
        } else {
            Log.d("Data Base", " Row with id " + rowId + " haven't got note." );
            noteCursor.get(0).close();
            noteCursor.get(1).close();
            return null;
        }

    }

    //тип занятия
    private String getLessonTypeFromScheduleTable(Integer rowNum, List<Cursor> c) {

        if (c.get(0) == null)
            return null;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        return c.get(0).getString(c.get(0).getColumnIndex(DBColumns.LESSON_TYPE_COLUMN));
    }

    private String getSubjectIdFromScheduleTable(Integer rowNum, List<Cursor> c) {

        if (c.get(0) == null)
            return null;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        return c.get(0).getString(c.get(0).getColumnIndex(DBColumns.SUBJECT_ID_COLUMN));
    }

    private String getLessonTimeIdFromScheduleTable(Integer rowNum, List<Cursor> c) {

        if (c.get(0) == null)
            return null;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        return c.get(0).getString(c.get(0).getColumnIndex(DBColumns.LESSON_TIME_ID_COLUMN));
    }

    private String getGroupIdFromScheduleTable(Integer rowNum, List<Cursor> c) {

        if (c.get(0) == null)
            return null;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        return c.get(0).getString(c.get(0).getColumnIndex(DBColumns.STUDENT_GROUP_ID_COLUMN));
    }

    private String getGroupNameById(String id) {
        List<Cursor> groupNameCursor;
        String getGroupNameQuery = "select * from student_group where _id = " + id;

        groupNameCursor = getDbHelper().getData(getGroupNameQuery);

        if (groupNameCursor.get(0) == null)
            return null;

        String retValue = groupNameCursor.get(0).getString(groupNameCursor.get(0).getColumnIndex(DBColumns.STUDENT_GROUP_NAME_COLUMN));
        groupNameCursor.get(0).close();
        groupNameCursor.get(1).close();

        return retValue;
    }

    private Employee getEmployeeById(String id) {
        List<Cursor> getEmployeeCursor;
        Employee employee = new Employee();
        String getEmployeeQuery = "select * from employee where _id = " + id;

        getEmployeeCursor = getDbHelper().getData(getEmployeeQuery);
        if (getEmployeeCursor.get(0) == null)
            return null;

        for (int i = 0; i < getEmployeeCursor.get(0).getColumnCount(); i++) {
            switch (getEmployeeCursor.get(0).getColumnName(i)) {
                case DBColumns.FIRST_NAME_COLUMN:
                    if (getEmployeeCursor.get(0).getString(i) != null) {
                        employee.setFirstName(getEmployeeCursor.get(0).getString(i));
                    } else employee.setFirstName(null);
                    break;
                case DBColumns.LAST_NAME_COLUMN:
                    if (getEmployeeCursor.get(0).getString(i) != null) {
                        employee.setLastName(getEmployeeCursor.get(0).getString(i));
                    } else employee.setLastName(null);
                    break;
                case DBColumns.MIDDLE_NAME_COLUMN:
                    if (getEmployeeCursor.get(0).getString(i) != null) {
                        employee.setMiddleName(getEmployeeCursor.get(0).getString(i));
                    } else employee.setMiddleName(null);
                    break;
                case DBColumns.DEPARTMENT_COLUMN:
                    if (getEmployeeCursor.get(0).getString(i) != null) {
                        employee.setDepartment(getEmployeeCursor.get(0).getString(i));
                    } else employee.setDepartment(null);
                    break;
                case DBColumns.EMP_PHOTO:
                    if (getEmployeeCursor.get(0).getBlob(i) != null) {
                        employee.setPhoto(BitmapFactory.decodeByteArray(getEmployeeCursor.get(0).getBlob(i), 0,
                                getEmployeeCursor.get(0).getBlob(i).length));
                    }
                    break;
                case BaseColumns._ID:
                    if (getEmployeeCursor.get(0).getString(i) != null) {
                        employee.setId(getEmployeeCursor.get(0).getLong(i));
                    } else employee.setId(null);
                    break;
                default:
                    return employee;

            }
        }
        Log.d("employee string = ", employee.getId() + employee.getDepartment() +
                employee.getFirstName() + employee.getLastName() + employee.getMiddleName());

        getEmployeeCursor.get(0).close();
        getEmployeeCursor.get(1).close();

        return employee;


    }

    private List<Employee> getEmployeeForScheduleTableRow(Integer rowNum, List<Cursor> c) {
        Log.d("rowNum = ", rowNum + "");
        List<Employee> employee = new ArrayList<>();
        String scheduleId;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        scheduleId = c.get(0).getString(0);


        List<Cursor> empCursor;
        String query = "select * from schedule_employee where se_id_schedule = " + scheduleId;
        empCursor = getDbHelper().getData(query);
        if (empCursor.get(0) == null)
            return new ArrayList<>();

        empCursor.get(0).moveToFirst();
        do {
            employee.add(getEmployeeById(empCursor.get(0).getString(empCursor.get(0).getColumnIndex(
                    DBColumns.SE_EMPLOYEE_ID_COLUMN))));
        } while (empCursor.get(0).moveToNext());

        empCursor.get(0).close();
        empCursor.get(1).close();

        return employee;
    }

    private String getSubjectById(String id) {
        List<Cursor> getSubjCursor;
        String getSubjQuery = "select * from subject where _id = " + id;

        getSubjCursor = getDbHelper().getData(getSubjQuery);

        if (getSubjCursor.get(0) == null)
            return null;

        String retValue = getSubjCursor.get(0).getString(getSubjCursor.get(0).getColumnIndex(DBColumns.SUBJECT_NAME_COLUMN));
        getSubjCursor.get(0).close();
        getSubjCursor.get(1).close();

        return retValue;
    }

    private String getLessonTimeById(String id) {
        List<Cursor> getTimeCursor;
        String getEmployeeQuery = "select * from lesson_time where _id = " + id;

        getTimeCursor = getDbHelper().getData(getEmployeeQuery);

        if (getTimeCursor.get(0) == null)
            return null;

        String retValue = getTimeCursor.get(0).getString(getTimeCursor.get(0).getColumnIndex(DBColumns.LESSON_TIME_COLUMN));
        getTimeCursor.get(0).close();
        getTimeCursor.get(1).close();
        return retValue;
    }

    private String getAudById(String id) {
        List<Cursor> cursor;
        String query = "select * from auditory where _id = " + id;

        cursor = getDbHelper().getData(query);

        if (cursor.get(0) == null)
            return null;

        String retValue = cursor.get(0).getString(cursor.get(0).getColumnIndex(DBColumns.AUDITORY_NAME_COLUMN));
        cursor.get(0).close();
        cursor.get(1).close();
        return retValue;
    }

    private List<String> getAudsForScheduleTableRow(Integer rowNum, List<Cursor> c) {
        List<String> auds = new ArrayList<>();
        String scheduleId;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        scheduleId = c.get(0).getString(0);

        List<Cursor> audCursor;
        String query = "select * from schedule_auditory where sa_id_schedule = " + scheduleId;
        audCursor = getDbHelper().getData(query);
        if (audCursor.get(0) == null)
            return new ArrayList<>();

        audCursor.get(0).moveToFirst();
        do {
            auds.add(getAudById(audCursor.get(0).getString(audCursor.get(0).getColumnIndex(
                    DBColumns.SA_AUDITORY_ID_COLUMN))));
        } while(audCursor.get(0).moveToNext());


        audCursor.get(0).close();
        audCursor.get(1).close();
        return auds;
    }

    private String getEmployeeId(String lastName) {
        List<Cursor> cursor;


        String query = "select last_name from employee";

        // get employee id from table
        // sqlite not match unicode symbols
        cursor = getDbHelper().getData(query);
        cursor.get(0).moveToFirst();
        int id = 0;
        do {
            if (cursor.get(0).getString(0).equals(lastName)) {
                cursor.get(0).close();
                cursor.get(1).close();
                return String.valueOf(id + 1);
            }
            id++;
        } while (cursor.get(0).moveToNext());


        cursor.get(0).close();
        cursor.get(1).close();
        return null;
    }

    //сделать расписание группы доступным для просмотра
    private void setGroupAsAvailable(String fileName) {
        String groupName = fileName.substring(0, STUDENT_GROUP_LENGTH);

        ContentValues values = new ContentValues();
        values.put(DBColumns.GR_SCHEDULE_AVAILABLE, "true");

        String selection = DBColumns.STUDENT_GROUP_NAME_COLUMN + " = " + groupName;


        getDbHelper().getReadableDatabase().update(
                "student_group",
                values,
                selection,
                null);
    }

    //делает группу недоступной для просмотра
    private void setGroupAsUnavailable(String fileName) {
        String groupName = fileName.substring(0, STUDENT_GROUP_LENGTH);

        ContentValues values = new ContentValues();
        values.put(DBColumns.GR_SCHEDULE_AVAILABLE, "");

        String selection = DBColumns.STUDENT_GROUP_NAME_COLUMN + " = " + groupName;


        getDbHelper().getReadableDatabase().update(
                "student_group",
                values,
                selection,
                null);
    }

    //сделать расписание преподавателя доступным для просмотра
    private void setTeacherAsAvailable(String fileName) {
        String lastName = EmployeeUtil.getEmployeeLastNameFromFile(fileName);

        ContentValues values = new ContentValues();
        values.put(DBColumns.EMP_SCHEDULE_AVAILABLE, "true");

        String selection = BaseColumns._ID + " = " + getEmployeeId(lastName);;


        getDbHelper().getReadableDatabase().update(
                "employee",
                values,
                selection,
                null);
    }

    private void setTeacherAsUnavailable(String fileName) {
        String lastName = EmployeeUtil.getEmployeeLastNameFromFile(fileName);

        ContentValues values = new ContentValues();
        values.put(DBColumns.EMP_SCHEDULE_AVAILABLE, "");

        String selection = BaseColumns._ID + " = " + getEmployeeId(lastName);;


        getDbHelper().getReadableDatabase().update(
                "employee",
                values,
                selection,
                null);
    }

    /**
     * Метод который делает строку из базы данных скрытой
     * @param rowId - id строки в базе данных
     */
    public void hideScheduleRow(String rowId) {
        ContentValues values = new ContentValues();
        values.put(DBColumns.IS_HIDDEN, "true");

        String selection = BaseColumns._ID + " = " + rowId;


        getDbHelper().getReadableDatabase().update(
                "schedule",
                values,
                selection,
                null);
    }

    /**
     * Метод который показывет что строка создавалась пользователем
     * и, что ее не надо удалять при обновлении
     * @param rowId - id строки в базе данных
     */
    public void setAsManual(String rowId) {
        ContentValues values = new ContentValues();
        values.put(DBColumns.IS_MANUAL, "true");

        String selection = BaseColumns._ID + " = " + rowId;


        getDbHelper().getReadableDatabase().update(
                "schedule",
                values,
                selection,
                null);
    }

    /**
     * Метод который делает строку из базы данных видимой
     * @param rowId - id строки в базе данных
     */
    public void showScheduleRow(String rowId) {
        ContentValues values = new ContentValues();
        values.put(DBColumns.IS_HIDDEN, "false");

        String selection = BaseColumns._ID + " = " + rowId;


        getDbHelper().getReadableDatabase().update(
                "schedule",
                values,
                selection,
                null);
    }

    /**
     * Метод который возвращает доступны для просмотра расписания групп
     * @return список групп
     */
    public List<String> getAvailableGroups() {
        List<Cursor> cursors;
        List<String> groups = new ArrayList<>();

        String query = "select * from student_group";


        cursors = getDbHelper().getData(query);
        if (cursors.get(0) == null)
            return groups;
        cursors.get(0).moveToFirst();


        do {
            // 2 = GR_SCHEDULE_AVAILABLE column
            if (cursors.get(0).getString(GR_SCHEDULE_AVAILABLE) != null
                    && "true".equals(cursors.get(0).getString(GR_SCHEDULE_AVAILABLE))) {

                //1 = GROUP_NAME column
                groups.add(cursors.get(0).getString(GROUP_NAME));
                Log.d("available group = ", groups.get(0));
            }
        } while (cursors.get(0).moveToNext());

        cursors.get(0).close();
        cursors.get(1).close();

        return groups;

    }

    /**
     * Метод который возвращает доступны для просмотра расписания преподавателей
     * @return список преподавателей
     */
    public List<String> getAvailableTeachers() {
        List<Cursor> cursors;
        List<String> teachers = new ArrayList<>();

        String query = "select * from employee";


        cursors = getDbHelper().getData(query);
        if (cursors.get(0) == null)
            return teachers;
        cursors.get(0).moveToFirst();


        do {
            //5 = EMP_SCHEDULE_AVAILABLE column
            if (cursors.get(0).getString(cursors.get(0).getColumnIndex(DBColumns.EMP_SCHEDULE_AVAILABLE)) != null
                    && "true".equals(cursors.get(0).getString(cursors.get(0).getColumnIndex(DBColumns.EMP_SCHEDULE_AVAILABLE)))) {
                //2, 1, 3 = LAST_NAME, FIRST_NAME, MIDDLE_NAME columns
                teachers.add(cursors.get(0).getString(cursors.get(0).getColumnIndex(DBColumns.LAST_NAME_COLUMN)) +
                        cursors.get(0).getString(cursors.get(0).getColumnIndex(DBColumns.FIRST_NAME_COLUMN)).substring(0, 1) +
                        cursors.get(0).getString(cursors.get(0).getColumnIndex(DBColumns.MIDDLE_NAME_COLUMN)).substring(0, 1));
                Log.d("available teacher = ", teachers.get(0));

            }

        } while (cursors.get(0).moveToNext());

        cursors.get(0).close();
        cursors.get(1).close();

        return teachers;

    }

    /**
     * Метод который делает расписание доступным для просмотра
     */
    public void setAsAvailable(String fileName) {
        if (FileUtil.isDigit(fileName.charAt(0))) {
            setGroupAsAvailable(fileName);
        } else {
            setTeacherAsAvailable(fileName);
        }
    }

    /**
     * Метод который делает расписание не доступным для просмотра
     */
    public void setAsUnavailable(String fileName) {
        if (FileUtil.isDigit(fileName.charAt(0))) {
            setGroupAsUnavailable(fileName);
        } else {
            setTeacherAsUnavailable(fileName);
        }
    }


    /**
     * Метод возвращает фотографию преподавателя из базы данных
     * @param emp преподаватель
     * @return битмап
     */
    public Bitmap getEmployeePhoto(Employee emp) {
        Bitmap photo;

        List<Cursor> empCursor;
        String query = "select emp_photo from employee where _id = " +  getEmployeeId(emp.getLastName());
        empCursor = getDbHelper().getData(query);

        if (empCursor.get(0) == null)
            return null;

        if (empCursor.get(0).getBlob(0) == null)
            return null;

        photo = BitmapFactory.decodeByteArray(empCursor.get(0).getBlob(0), 0, empCursor.get(0).getBlob(0).length);

        return photo;
    }


    public DBHelper getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
}

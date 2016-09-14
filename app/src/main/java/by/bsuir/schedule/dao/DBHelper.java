package by.bsuir.schedule.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.provider.BaseColumns;
import android.util.Log;

import by.bsuir.schedule.model.Employee;
import by.bsuir.schedule.model.Schedule;
import by.bsuir.schedule.utils.ListUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iChrome on 24.12.2015.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper sInstance;

    private static final Integer DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "scheduleDB";
    private static final String SUBJECT_TABLE_NAME = "subject";
    private static final String EMPLOYEE_TABLE_NAME = "employee";
    private static final String AUDITORY_TABLE_NAME = "auditory";
    private static final String STUDENT_GROUP_TABLE_NAME = "student_group";
    private static final String LESSON_TIME_TABLE_NAME = "lesson_time";
    private static final String SCHEDULE_TABLE_NAME = "schedule";
    private static final String SCHEDULE_EMPLOYEE_TABLE_NAME = "schedule_employee";
    private static final String SCHEDULE_AUDITORY_TABLE_NAME = "schedule_auditory";
    private static final String NOTE_TABLE_NAME = "note";
    private static final String SCHEDULE_VIEW_NAME = "scheduleView";


    private DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Метод возвращает текущий объект класса DBHelper
     */
    public static DBHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * метод вызывается при создании базы данных
     * и создает нужные таблицы
     * @param db - база данных
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");

        //creating subject table
        String sql = "CREATE TABLE " + SUBJECT_TABLE_NAME + " (" + BaseColumns._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBColumns.SUBJECT_NAME_COLUMN + " TEXT);";
        db.execSQL(sql);

        //creating employee table
        sql = "CREATE TABLE " + EMPLOYEE_TABLE_NAME + " (" + BaseColumns._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBColumns.FIRST_NAME_COLUMN + " TEXT, " +
                DBColumns.LAST_NAME_COLUMN + " TEXT, " + DBColumns.MIDDLE_NAME_COLUMN + " TEXT, " +
                DBColumns.EMP_PHOTO + " BLOB, " + DBColumns.EMP_PHOTO_LINK + " TEXT, " +
                DBColumns.DEPARTMENT_COLUMN + " TEXT, " + DBColumns.EMP_SCHEDULE_AVAILABLE + " TEXT);";
        db.execSQL(sql);

        //creating auditory table
        sql = "CREATE TABLE " + AUDITORY_TABLE_NAME + " (" + BaseColumns._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBColumns.AUDITORY_NAME_COLUMN + " TEXT);";
        db.execSQL(sql);

        sql = "CREATE TABLE " + STUDENT_GROUP_TABLE_NAME + " (" + BaseColumns._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBColumns.STUDENT_GROUP_NAME_COLUMN + " TEXT, "
                + DBColumns.GR_SCHEDULE_AVAILABLE + " TEXT);";
        db.execSQL(sql);

        //creating lessonTime table
        sql = "CREATE TABLE " + LESSON_TIME_TABLE_NAME + " (" + BaseColumns._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBColumns.LESSON_TIME_COLUMN + " TEXT);";
        db.execSQL(sql);

        //creating schedule table
        sql = "CREATE TABLE " + SCHEDULE_TABLE_NAME + " (" + BaseColumns._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBColumns.LESSON_TYPE_COLUMN + " TEXT, " +
                DBColumns.SUBJECT_ID_COLUMN + " INTEGER, " + DBColumns.LESSON_TIME_ID_COLUMN + " INTEGER, " +
                DBColumns.SUBGROUP_COLUMN + " INTEGER DEFAULT 0, " +
                DBColumns.WEEK_NUMBER_COLUMN + " TEXT, " + DBColumns.WEEK_DAY_COLUMN + " TEXT, " +
                DBColumns.DATE_COLUMN + " DATE, " + DBColumns.STUDENT_GROUP_ID_COLUMN + " INTEGER, " + DBColumns.IS_HIDDEN + " TEXT, " +
                DBColumns.IS_MANUAL + " TEXT," +
                "FOREIGN KEY (" + DBColumns.SUBJECT_ID_COLUMN + ") REFERENCES " + SUBJECT_TABLE_NAME + "(" + BaseColumns._ID + "), " +
                "FOREIGN KEY (" + DBColumns.STUDENT_GROUP_ID_COLUMN + ") REFERENCES " + STUDENT_GROUP_TABLE_NAME + "(" + BaseColumns._ID + "), " +
                "FOREIGN KEY (" + DBColumns.LESSON_TIME_ID_COLUMN + ") REFERENCES " + LESSON_TIME_TABLE_NAME + "(" + BaseColumns._ID + "));";
        db.execSQL(sql);

        //creating note table
        sql = "CREATE TABLE " + NOTE_TABLE_NAME + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBColumns.NOTE_SCHEDULE_ID_COLUMN + " INTEGER, " + DBColumns.NOTE_TEXT_COLUMN + " TEXT, " + DBColumns.DATE_COLUMN + " TEXT,"
                + "FOREIGN KEY(" + DBColumns.NOTE_SCHEDULE_ID_COLUMN + ") REFERENCES " + SCHEDULE_TABLE_NAME + "("
                + BaseColumns._ID + "));";
        db.execSQL(sql);

        //creating schedule_employee table (manyToMany)
        sql = "CREATE TABLE " + SCHEDULE_EMPLOYEE_TABLE_NAME + " (" + DBColumns.SE_EMPLOYEE_ID_COLUMN +
                " INTEGER, " + DBColumns.SE_SCHEDULE_ID_COLUMN + " INTEGER, PRIMARY KEY (" + DBColumns.SE_EMPLOYEE_ID_COLUMN + ", " + DBColumns.SE_SCHEDULE_ID_COLUMN + "), " +
                "FOREIGN KEY (" + DBColumns.SE_EMPLOYEE_ID_COLUMN + ") REFERENCES " + EMPLOYEE_TABLE_NAME + "(" + BaseColumns._ID + "), " +
                "FOREIGN KEY (" + DBColumns.SE_SCHEDULE_ID_COLUMN + ") REFERENCES " + SCHEDULE_TABLE_NAME + "(" + BaseColumns._ID + "));";
        db.execSQL(sql);

        //creating schedule_auditory table (manyToMany)
        sql = "CREATE TABLE " + SCHEDULE_AUDITORY_TABLE_NAME + " (" + DBColumns.SA_SCHEDULE_ID_COLUMN +
                " INTEGER, " + DBColumns.SA_AUDITORY_ID_COLUMN + " INTEGER, PRIMARY KEY (" + DBColumns.SA_SCHEDULE_ID_COLUMN + ", " + DBColumns.SA_AUDITORY_ID_COLUMN + "), " +
                "FOREIGN KEY (" + DBColumns.SA_AUDITORY_ID_COLUMN + ") REFERENCES " + AUDITORY_TABLE_NAME + "(" + BaseColumns._ID + "), " +
                "FOREIGN KEY (" + DBColumns.SA_SCHEDULE_ID_COLUMN + ") REFERENCES " + SCHEDULE_TABLE_NAME + "(" + BaseColumns._ID + "));";
        db.execSQL(sql);

        //creating schedule view
        sql = "CREATE VIEW " + SCHEDULE_VIEW_NAME + " AS SELECT " + SCHEDULE_TABLE_NAME + "." + BaseColumns._ID + ", " +
                SUBJECT_TABLE_NAME + "." + DBColumns.SUBJECT_NAME_COLUMN + ", " + LESSON_TIME_TABLE_NAME + "." + DBColumns.LESSON_TIME_COLUMN + ", " +
                SCHEDULE_TABLE_NAME + "." + DBColumns.SUBGROUP_COLUMN + ", " + SCHEDULE_TABLE_NAME + "." + DBColumns.WEEK_NUMBER_COLUMN + ", " +
                SCHEDULE_TABLE_NAME + "." + DBColumns.WEEK_DAY_COLUMN + ", " + SCHEDULE_TABLE_NAME + "." + DBColumns.DATE_COLUMN + ", " +
                SCHEDULE_TABLE_NAME + "." + DBColumns.LESSON_TYPE_COLUMN + ", " + STUDENT_GROUP_TABLE_NAME + "." + DBColumns.STUDENT_GROUP_NAME_COLUMN + ", " +
                "GROUP_CONCAT(DISTINCT( " + EMPLOYEE_TABLE_NAME + "." + DBColumns.MIDDLE_NAME_COLUMN + " || ' ' || substr(" +
                EMPLOYEE_TABLE_NAME + "." + DBColumns.FIRST_NAME_COLUMN + ", 1, 1) || '. ' || substr(" +
                EMPLOYEE_TABLE_NAME + "." + DBColumns.LAST_NAME_COLUMN + ", 1, 1) || '. ')), " +
                "GROUP_CONCAT(DISTINCT " + AUDITORY_TABLE_NAME + "." + DBColumns.AUDITORY_NAME_COLUMN + ")" +
                " FROM " + SCHEDULE_TABLE_NAME +
                " left join " + LESSON_TIME_TABLE_NAME + " on " + SCHEDULE_TABLE_NAME + "." + DBColumns.LESSON_TIME_ID_COLUMN + " = " +
                LESSON_TIME_TABLE_NAME + "." + BaseColumns._ID +
                " left join " + SUBJECT_TABLE_NAME + " on " + SCHEDULE_TABLE_NAME + "." + DBColumns.SUBJECT_ID_COLUMN + " = " +
                SUBJECT_TABLE_NAME + "." + BaseColumns._ID +
                " left join " + STUDENT_GROUP_TABLE_NAME + " on " + SCHEDULE_TABLE_NAME + "." + DBColumns.STUDENT_GROUP_ID_COLUMN + " = " +
                STUDENT_GROUP_TABLE_NAME + "." + BaseColumns._ID +
                " left join " + SCHEDULE_AUDITORY_TABLE_NAME + " on " + SCHEDULE_AUDITORY_TABLE_NAME + "." + DBColumns.SA_SCHEDULE_ID_COLUMN + " = " +
                SCHEDULE_TABLE_NAME + "." + BaseColumns._ID +
                " left join " + AUDITORY_TABLE_NAME + " on " + AUDITORY_TABLE_NAME + "." + BaseColumns._ID + " = " +
                SCHEDULE_AUDITORY_TABLE_NAME + "." + DBColumns.SA_AUDITORY_ID_COLUMN +
                " left join " + SCHEDULE_EMPLOYEE_TABLE_NAME + " on " + SCHEDULE_EMPLOYEE_TABLE_NAME + "." + DBColumns.SE_SCHEDULE_ID_COLUMN + " = " +
                SCHEDULE_TABLE_NAME + "." + BaseColumns._ID +
                " left join " + EMPLOYEE_TABLE_NAME + " on " + SCHEDULE_EMPLOYEE_TABLE_NAME + "." + DBColumns.SE_EMPLOYEE_ID_COLUMN + " = " +
                EMPLOYEE_TABLE_NAME + "." + BaseColumns._ID +
                " group by " + SCHEDULE_TABLE_NAME + "." + BaseColumns._ID + ";" ;

        db.execSQL(sql);
    }

    /**
     * Метод вызывается при обновлении базы данных
     * @param db - id строки для удаления
     * @param oldVersion - номер версии до обновления
     * @param newVersion - после
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion) {
            return;
        }

        recreateTables(db);
    }

    /**
     * Метод удаляет все таблицы из базы данных
     * @param db - база дынных
     */
    public void recreateTables(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS " + SCHEDULE_VIEW_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_EMPLOYEE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_AUDITORY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AUDITORY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NOTE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EMPLOYEE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SUBJECT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LESSON_TIME_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + STUDENT_GROUP_TABLE_NAME);
        onCreate(db);
    }

    /**
     * Метод получает ID строки с искомым значением
     * @param tableName - название таблицы
     * @param column -  название столбца
     * @param value - искомое значение
     * @return возвращает ID строки с искомым значением
     */
    public long getItemWithNameFromDataBase(String tableName, String column, String value){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.query(tableName, new String[]{ BaseColumns._ID, column}, column + " = ?", new String[]{value}, null, null, null);
        cursor.moveToFirst();
        if(cursor.getCount() == 0){
            cursor.close();
            return -1;
        } else{
            long result = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            cursor.close();
            return result;
        }
    }

    /**
     * Метод получает ID строки с искомым преподавателем
     * @param employee - преподаватель
     * @return возвращает ID строки с искомым значением
     */
    public long getEmployeeFromDataBase(Employee employee){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.query(EMPLOYEE_TABLE_NAME, new String[]{ BaseColumns._ID, DBColumns.FIRST_NAME_COLUMN, DBColumns.MIDDLE_NAME_COLUMN, DBColumns.LAST_NAME_COLUMN},
                DBColumns.FIRST_NAME_COLUMN + " = ? and " + DBColumns.MIDDLE_NAME_COLUMN + " = ? and " + DBColumns.LAST_NAME_COLUMN + " = ? ",
                new String[]{employee.getFirstName(), employee.getMiddleName(), employee.getLastName()}, null, null, null);
        cursor.moveToFirst();
        if(cursor.getCount() == 0){
            cursor.close();
            return -1;
        } else{
            long result = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            cursor.close();
            return result;
        }
    }

    /**
     * Метод получает ID строки с искомой записью
     * @param schedule - искомая запись
     * @param contentValues - искомые значения
     * @return возвращает ID строки с искомым значением
     */
    public long getScheduleFromDataBase(Schedule schedule, ContentValues contentValues){
        SQLiteDatabase database = this.getWritableDatabase();
        List<String> selection = new ArrayList<>();
        List<String> args = new ArrayList<>();
        StringBuilder selectionArgs = new StringBuilder();
        selection.add(BaseColumns._ID);

        selection.add(DBColumns.SUBJECT_ID_COLUMN);
        args.add(String.valueOf(contentValues.get(DBColumns.SUBJECT_ID_COLUMN)));
        selectionArgs.append(DBColumns.SUBJECT_ID_COLUMN + " = ? and ");

        selection.add(DBColumns.LESSON_TIME_ID_COLUMN);
        args.add(String.valueOf(contentValues.get(DBColumns.LESSON_TIME_ID_COLUMN)));
        selectionArgs.append(DBColumns.LESSON_TIME_ID_COLUMN + " = ? and ");

        selection.add(DBColumns.STUDENT_GROUP_ID_COLUMN);
        args.add(String.valueOf(contentValues.get(DBColumns.STUDENT_GROUP_ID_COLUMN)));
        selectionArgs.append(DBColumns.STUDENT_GROUP_ID_COLUMN + " = ? and ");

        if(schedule.getSubGroup() != null && !schedule.getSubGroup().isEmpty()){
            selection.add(DBColumns.SUBGROUP_COLUMN);
            args.add(String.valueOf(contentValues.get(DBColumns.SUBGROUP_COLUMN)));
            selectionArgs.append(DBColumns.SUBGROUP_COLUMN + " = ? and ");
        }
        if(schedule.getWeekNumbers() != null){
            selection.add(DBColumns.WEEK_NUMBER_COLUMN);
            args.add(String.valueOf(contentValues.get(DBColumns.WEEK_NUMBER_COLUMN)));
            selectionArgs.append(DBColumns.WEEK_NUMBER_COLUMN + " = ? and ");
        }
        if(schedule.getWeekDay() != null){
            selection.add(DBColumns.WEEK_DAY_COLUMN);
            args.add(String.valueOf(contentValues.get(DBColumns.WEEK_DAY_COLUMN)));
            selectionArgs.append(DBColumns.WEEK_DAY_COLUMN + " = ? and ");
        }
        if(schedule.getDate() != null){
            selection.add(DBColumns.DATE_COLUMN);
            args.add(String.valueOf(contentValues.get(DBColumns.DATE_COLUMN)));
            selectionArgs.append(DBColumns.DATE_COLUMN + " = ? and ");
        }
        if(schedule.getLessonType() != null){
            selection.add(DBColumns.LESSON_TYPE_COLUMN);
            args.add(String.valueOf(contentValues.get(DBColumns.LESSON_TYPE_COLUMN)));
            selectionArgs.append(DBColumns.LESSON_TYPE_COLUMN + " = ?");
        }

        Cursor cursor = database.query(SCHEDULE_TABLE_NAME, selection.toArray(new String[selection.size()]), selectionArgs.toString(), args.toArray(new String[args.size()]), null, null, null);
        cursor.moveToFirst();
        if(cursor.getCount() == 0){
            cursor.close();
            return -1;
        } else{
            long result = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            cursor.close();
            return result;
        }
    }

    /**
     * Метод добавляет предмет в базу данных
     * @param subjectName - название предмета
     * @return возвращает ID строки с этим предметом
     */
    public long addSubjectToDataBase(String subjectName){
        long itemId = getItemWithNameFromDataBase(SUBJECT_TABLE_NAME, DBColumns.SUBJECT_NAME_COLUMN, subjectName);
        if(itemId < 0){
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBColumns.SUBJECT_NAME_COLUMN, subjectName);
            itemId = database.insert(SUBJECT_TABLE_NAME, null, contentValues);
            return itemId;
        } else{
            //item already exists
            return itemId;
        }
    }

    /**
     * Метод добавляет преподавателя в базу данных
     * @param employee - преподаватель
     * @return возвращает ID строки с этим преподавателем
     */
    public long addEmployeeToDataBase(Employee employee){
        long resultId = getEmployeeFromDataBase(employee);
        byte[] data = null;
        if (employee.getPhoto() != null) {
            data = getBitmapAsByteArray(employee.getPhoto());
        }
        if(resultId < 0){
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBColumns.FIRST_NAME_COLUMN, employee.getFirstName());
            contentValues.put(DBColumns.MIDDLE_NAME_COLUMN, employee.getMiddleName());
            contentValues.put(DBColumns.LAST_NAME_COLUMN, employee.getLastName());
            contentValues.put(DBColumns.EMP_PHOTO_LINK, employee.getPhotoURL());
            contentValues.put(DBColumns.EMP_PHOTO, data);
            resultId = database.insert(EMPLOYEE_TABLE_NAME, null, contentValues);
            return resultId;
        } else{
            //employee already exists
            return resultId;
        }
    }

    /**
     * Метод добавляет время начала и конца пары в базу данных
     * @param lessonTime - время
     * @return возвращает ID строки с этим временем
     */
    public long addLessonTimeToDataBase(String lessonTime){
        long resultId = getItemWithNameFromDataBase(LESSON_TIME_TABLE_NAME, DBColumns.LESSON_TIME_COLUMN, lessonTime);
        if(resultId < 0){
            SQLiteDatabase database = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBColumns.LESSON_TIME_COLUMN, lessonTime);
            resultId = database.insert(LESSON_TIME_TABLE_NAME, null, contentValues);
            return resultId;
        } else{
            //lessonTime already exists
            return resultId;
        }
    }

    /**
     * Метод добавляет номер группы в базу данных
     * @param studentGroupName - номер группы
     * @return возвращает ID строки с этой группой
     */
    public long addStudentGroupToDataBase(String studentGroupName){
        long resultId = getItemWithNameFromDataBase(STUDENT_GROUP_TABLE_NAME, DBColumns.STUDENT_GROUP_NAME_COLUMN, studentGroupName);
        if(resultId < 0){
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBColumns.STUDENT_GROUP_NAME_COLUMN, studentGroupName);
            resultId = database.insert(STUDENT_GROUP_TABLE_NAME, null, contentValues);
            return resultId;
        } else{
            //studentGroup already exists
            return resultId;
        }
    }

    /**
     * Метод добавляет номер аудитории в базу данных
     * @param auditoryName - номер ааудитории
     * @return возвращает ID строки с этой аудиторией
     */
    public long addAuditoryToDataBase(String auditoryName){
        long resultId = getItemWithNameFromDataBase(AUDITORY_TABLE_NAME, DBColumns.AUDITORY_NAME_COLUMN, auditoryName);
        if(resultId < 0){
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBColumns.AUDITORY_NAME_COLUMN, auditoryName);
            resultId = database.insert(AUDITORY_TABLE_NAME, null, contentValues);
            return resultId;
        } else{
            //auditory already exists
            return resultId;
        }
    }

    /**
     * Метод добавляет номер аудитории в таблицу с расписанием
     * @param scheduleID - id строки в расписании
     * @param auditoryID - id строки в таблице аудитории
     */
    public void addScheduleAuditoryToDataBase(Long scheduleID, Long auditoryID){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.query(SCHEDULE_AUDITORY_TABLE_NAME, new String[]{DBColumns.SA_AUDITORY_ID_COLUMN, DBColumns.SA_SCHEDULE_ID_COLUMN},
                DBColumns.SA_AUDITORY_ID_COLUMN + " = ? and " + DBColumns.SA_SCHEDULE_ID_COLUMN + " = ?",
                new String[]{auditoryID.toString(), scheduleID.toString()}, null, null, null);
        if (cursor.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBColumns.SA_AUDITORY_ID_COLUMN, auditoryID);
            contentValues.put(DBColumns.SA_SCHEDULE_ID_COLUMN, scheduleID);
            database.insert(SCHEDULE_AUDITORY_TABLE_NAME, null, contentValues);
            cursor.close();
        }
    }

    /**
     * Метод добавляет id преподавателя в таблицу с расписанием
     * @param scheduleID - id строки в расписании
     * @param employeeID - id строки в таблице преподавателей
     */
    public void addScheduleEmployeeToDataBase(Long scheduleID, Long employeeID){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.query(SCHEDULE_EMPLOYEE_TABLE_NAME, new String[]{DBColumns.SE_EMPLOYEE_ID_COLUMN, DBColumns.SE_SCHEDULE_ID_COLUMN},
                DBColumns.SE_EMPLOYEE_ID_COLUMN + " = ? and " + DBColumns.SE_SCHEDULE_ID_COLUMN + " = ?",
                new String[]{employeeID.toString(), scheduleID.toString()}, null, null, null);
        if(cursor.getCount() == 0){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBColumns.SE_EMPLOYEE_ID_COLUMN, employeeID);
            contentValues.put(DBColumns.SE_SCHEDULE_ID_COLUMN, scheduleID);
            database.insert(SCHEDULE_EMPLOYEE_TABLE_NAME, null, contentValues);
            cursor.close();
        }
    }

    /**
     * Метод добавляет примечание в базу данных
     * @param scheduleID - id строки в расписании
     * @param text - текст примечания
     */
    public void addScheduleNoteToDataBase(Long scheduleID, String text) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.NOTE_SCHEDULE_ID_COLUMN, scheduleID);
        cv.put(DBColumns.NOTE_TEXT_COLUMN, text);

        database.insert("note", null, cv);
    }


    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }


    /**
     * Метод добавляет расписание в базу данных
     * @param schedule - расписание
     * @return возвращает ID добавленного расписания
     */
    public long addScheduleToDataBase(Schedule schedule){

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBColumns.SUBJECT_ID_COLUMN, addSubjectToDataBase(schedule.getSubject()));
        contentValues.put(DBColumns.LESSON_TIME_ID_COLUMN, addLessonTimeToDataBase(schedule.getLessonTime()));
        contentValues.put(DBColumns.STUDENT_GROUP_ID_COLUMN, addStudentGroupToDataBase(schedule.getStudentGroup()));
        if(schedule.getSubGroup() != null && !schedule.getSubGroup().isEmpty()){
            contentValues.put(DBColumns.SUBGROUP_COLUMN, schedule.getSubGroup());
        }
        if(schedule.getWeekNumbers() != null && schedule.getWeekNumbers().size() > 0){
            contentValues.put(DBColumns.WEEK_NUMBER_COLUMN, ListUtil.convertListToString(schedule.getWeekNumbers()));
        }
        if(schedule.getWeekDay() != null){
            contentValues.put(DBColumns.WEEK_DAY_COLUMN, schedule.getWeekDay());
        }
        if(schedule.getDate() != null){
            contentValues.put(DBColumns.DATE_COLUMN, schedule.getDate());
        }
        if(schedule.getLessonType() != null){
            contentValues.put(DBColumns.LESSON_TYPE_COLUMN, schedule.getLessonType());
        }

        long scheduleResultId = getScheduleFromDataBase(schedule, contentValues);
        if(scheduleResultId < 0){
            SQLiteDatabase database = this.getWritableDatabase();
            scheduleResultId = database.insert(SCHEDULE_TABLE_NAME, null, contentValues);
        }

        for(Employee employee : schedule.getEmployeeList()){
            Long employeeID = addEmployeeToDataBase(employee);
            addScheduleEmployeeToDataBase(scheduleResultId, employeeID);
        }
        for(String auditory : schedule.getAuditories()){
            Long auditoryID = addAuditoryToDataBase(auditory);
            addScheduleAuditoryToDataBase(scheduleResultId, auditoryID);
        }

        addScheduleNoteToDataBase(scheduleResultId, schedule.getNote());

        return scheduleResultId;
    }

    /**
     * Метод получает данные из бвзы данных
     * @param query - sql запрос
     * @return возвращает курсор с выбранными строками
     */
    public List<Cursor> getData(String query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<>(2);
        MatrixCursor cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            cursor2.addRow(new Object[]{"Success"});

            alc.set(1,cursor2);

                if (null != c && c.getCount() > 0) {


                    alc.set(0, c);
                    c.moveToFirst();

                    return alc;
                }

            return alc;
        } catch(SQLException sqlEx){
            Log.e("db exception", sqlEx.getMessage(), sqlEx);
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, cursor2);
            return alc;
        } catch (Exception ex) {
            Log.e("db exception", ex.getMessage(), ex);
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, cursor2);
            return alc;
        }


    }
}

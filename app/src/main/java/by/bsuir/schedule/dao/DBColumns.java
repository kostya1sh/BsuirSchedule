package by.bsuir.schedule.dao;

/**
 * Created by iChrome on 24.12.2015.
 */
public class DBColumns {

    //employee table
    public static final String FIRST_NAME_COLUMN = "FIRST_NAME";
    public static final String LAST_NAME_COLUMN = "LAST_NAME";
    public static final String MIDDLE_NAME_COLUMN = "MIDDLE_NAME";
    public static final String DEPARTMENT_COLUMN = "DEPARTMENT";
    public static final String EMP_SCHEDULE_AVAILABLE = "EMP_SCHEDULE_AVAILABLE";
    public static final String EMP_PHOTO_LINK = "EMP_PHOTO_LINK";
    public static final String EMP_PHOTO = "EMP_PHOTO";

    //subject
    public static final String SUBJECT_NAME_COLUMN = "SUBJECT_NAME";

    //studentGroup
    public static final String STUDENT_GROUP_NAME_COLUMN = "STUDENT_GROUP_NAME";
    public static final String GR_SCHEDULE_AVAILABLE = "GR_SCHEDULE_AVAILABLE";

    //lessonTime
    public static final String LESSON_TIME_COLUMN = "TIME";

    //auditory
    public static final String AUDITORY_NAME_COLUMN = "AUDITORY_NAME";

    //schedule
    public static final String SUBJECT_ID_COLUMN = "ID_SUBJECT";
    public static final String LESSON_TIME_ID_COLUMN = "ID_LESSON_TIME";
    public static final String SUBGROUP_COLUMN = "SUBGROUP";
    public static final String WEEK_NUMBER_COLUMN = "WEEK_NUMBER";
    public static final String WEEK_DAY_COLUMN = "WEEK_DAY";
    public static final String DATE_COLUMN = "DATE";
    public static final String LESSON_TYPE_COLUMN = "LESSON_TYPE";
    public static final String STUDENT_GROUP_ID_COLUMN = "ID_STUDENT_GROUP";
    public static final String IS_HIDDEN = "IS_HIDDEN";
    public static final String IS_MANUAL = "IS_MANUAL";

    //note
    public static final String NOTE_TEXT_COLUMN = "NOTE_TEXT";
    public static final String NOTE_SCHEDULE_ID_COLUMN = "NOTE_SCHEDULE_ID";

    //schedule_employee
    public static final String SE_SCHEDULE_ID_COLUMN = "SE_ID_SCHEDULE";
    public static final String SE_EMPLOYEE_ID_COLUMN = "SE_ID_EMPLOYEE";

    //schedule_auditory
    public static final String SA_SCHEDULE_ID_COLUMN = "SA_ID_SCHEDULE";
    public static final String SA_AUDITORY_ID_COLUMN = "SA_ID_AUDITORY";


    private DBColumns() {

    }

}

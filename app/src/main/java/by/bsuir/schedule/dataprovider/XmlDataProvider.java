package by.bsuir.schedule.dataprovider;

import android.util.Log;

import by.bsuir.schedule.model.Employee;
import by.bsuir.schedule.model.Schedule;
import by.bsuir.schedule.model.SchoolDay;
import by.bsuir.schedule.model.StudentGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iChrome on 05.08.2015.
 */
public class XmlDataProvider {
    private static final String TAG = "xmlLog";
    private static final String EMPLOYEE_TAG = "employee";
    private static final String ERROR_WHILE_PARSING_XML = "Ошибка при парсинге xml";

    /**
     * Класс предоставляющий методы для парсинга xml файлов
     */
    private XmlDataProvider(){
    }

    /**
     * Метод парсит строку в список преподавателей
     * @param content Входная строка для парсинга
     * @return Возвращает список преподавателей
     */
    public static List<Employee> parseListEmployeeXml(String content){
        try {
            return parseEmployeeList(content);
        } catch(XmlPullParserException e){
            Log.v(TAG, ERROR_WHILE_PARSING_XML, e);
        } catch (IOException e){
            Log.v(TAG, "IO exception", e);
        }
        return new ArrayList<>();
    }

    /**
     * Метод непосредство парсящий список преподавателей
     * @param content входная строка для парсинга
     * @return возвращает результирующий список преподавателей
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static List<Employee> parseEmployeeList(String content) throws XmlPullParserException, IOException{
        List<Employee> resultList = new ArrayList<>();
        Employee currentReadingEmployee = null;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(content));

        int xmlEventType = parser.getEventType();
        String currentTag = null;

        while (xmlEventType != XmlPullParser.END_DOCUMENT) {

            switch (xmlEventType) {
                case XmlPullParser.START_DOCUMENT:
                    Log.v(TAG, "Start xml document");
                    break;
                case XmlPullParser.START_TAG:
                    currentTag = parser.getName();
                    currentReadingEmployee = handleEmployeeStartTag(currentTag, parser, currentReadingEmployee, resultList);
                    break;
                case XmlPullParser.END_TAG:
                    currentTag = parser.getName();
                    break;
                case XmlPullParser.TEXT:
                    handleEmployeeTextNode(currentTag, currentReadingEmployee, parser);
                    break;
                default:
                    break;
            }
            xmlEventType = parser.next();
        }
        return resultList;
    }

    /**
     * Метод обрабатывает тег "START_TAG" при парсинге списка преподавателей
     * @param currentTag текущий тег
     * @param parser парсер
     * @param passedReadingEmployee текущий считываемый преподаватель
     * @param resultList результирующий список
     * @return Возвращает преподавателя.
     */
    private static Employee handleEmployeeStartTag(String currentTag, XmlPullParser parser, Employee passedReadingEmployee, List<Employee> resultList){
        Employee currentReadingEmployee = passedReadingEmployee;
        if(currentTag.equals(EMPLOYEE_TAG) && parser.getDepth() == 2){
            if(currentReadingEmployee != null){
                resultList.add(currentReadingEmployee);
            }
            currentReadingEmployee = new Employee();
        }
        return currentReadingEmployee;
    }

    /**
     * Метод обрабатывающий тег "TEXT" при парсинге списка преподавателей
     * @param currentTag текущий тег
     * @param currentReadingEmployee текущий считываемый преподаватель
     * @param parser парсер
     */
    private static void handleEmployeeTextNode(String currentTag, Employee currentReadingEmployee, XmlPullParser parser){
        assert currentTag != null;
        if("academicDepartment".equalsIgnoreCase(currentTag)){
            assert currentReadingEmployee != null;
            currentReadingEmployee.setDepartment(parser.getText());
        } else if("firstName".equalsIgnoreCase(currentTag)){
            assert currentReadingEmployee != null;
            currentReadingEmployee.setFirstName(parser.getText());
        } else if("id".equalsIgnoreCase(currentTag)){
            assert currentReadingEmployee != null;
            currentReadingEmployee.setId(Long.parseLong(parser.getText()));
        } else if("lastName".equalsIgnoreCase(currentTag)){
            assert currentReadingEmployee != null;
            currentReadingEmployee.setLastName(parser.getText());
        } else if("middleName".equalsIgnoreCase(currentTag)){
            assert currentReadingEmployee != null;
            currentReadingEmployee.setMiddleName(parser.getText());
        }
    }

    /**
     * Метод для парсинга списка студенченских групп
     * @param content контекст
     * @return возвращает результирующий список студенченских групп
     */
    public static List<StudentGroup> parseListStudentGroupXml(String content){
        try {
            return parseStudentGroupList(content);
        } catch(XmlPullParserException e){
            Log.v(TAG, ERROR_WHILE_PARSING_XML, e);
        } catch (IOException e){
            Log.v(TAG, "IO exception", e);
        } catch (Exception e){
            Log.v(TAG, "unknown exception", e);
        }
        return new ArrayList<>();
    }

    /**
     * Метод непосредственно парсит список студенченских групп
     * @param content строка для парсинга
     * @return возвращает полученный список студенченских групп
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static List<StudentGroup> parseStudentGroupList(String content) throws XmlPullParserException, IOException{
        List<StudentGroup> resultList = new ArrayList<>();
        StudentGroup currentReadingStudentGroup = null;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(content));

        int eventType = parser.getEventType();
        String currentTag = null;

        while(eventType != XmlPullParser.END_DOCUMENT){
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    Log.v(TAG, "Start document");
                    break;
                case XmlPullParser.START_TAG:
                    currentTag = parser.getName();
                    currentReadingStudentGroup = handleStudentGroupStartTag(currentTag, currentReadingStudentGroup, parser, resultList);
                    break;
                case XmlPullParser.END_TAG:
                    currentTag = parser.getName();
                    break;
                case XmlPullParser.TEXT:
                    handleStudentGroupTextTag(currentTag, currentReadingStudentGroup, parser);
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return resultList;
    }

    /**
     * Метод обрабатывает тег "START_TAG" при парсинге списка студенченских групп
     * @param currentTag текущий тег
     * @param passedStudentGroup текущая считываемая студенченская группа
     * @param parser парсер
     * @param resultList результирующий список
     * @return Возвращает группу
     */
    private static StudentGroup handleStudentGroupStartTag(String currentTag, StudentGroup passedStudentGroup, XmlPullParser parser, List<StudentGroup> resultList){
        StudentGroup currentReadingStudentGroup = passedStudentGroup;
        if("studentGroup".equalsIgnoreCase(currentTag) && parser.getDepth() == 2){
            if(currentReadingStudentGroup != null){
                resultList.add(currentReadingStudentGroup);
            }
            currentReadingStudentGroup = new StudentGroup();
        }
        return currentReadingStudentGroup;
    }

    /**
     * Метод обрабатывает тег "TEXT" при парсинге списка студенченских групп
     * @param currentTag текущий тег
     * @param currentReadingStudentGroup текущая считываемая группа
     * @param parser парсер
     */
    private static void handleStudentGroupTextTag(String currentTag, StudentGroup currentReadingStudentGroup, XmlPullParser parser){
        assert currentTag != null;
        if("name".equalsIgnoreCase(currentTag)){
            assert currentReadingStudentGroup != null;
            currentReadingStudentGroup.setStudentGroupName(parser.getText());
        } else if("id".equalsIgnoreCase(currentTag)) {
            assert currentReadingStudentGroup != null;
            currentReadingStudentGroup.setStudentGroupId(Long.parseLong(parser.getText()));
        }
    }

    /**
     * Метод служит для парсинга списка занятий
     * @param directory папка в которой сохранен файл с занятиями
     * @param fileName файл с занятиями
     * @return результирующий список занятий
     */
    public static List<SchoolDay> parseScheduleXml(File directory, String fileName){
        try {
            return parseScheduleFromXML(directory, fileName);
        } catch (FileNotFoundException e){
            Log.v(TAG, "Файл не найден: " + fileName, e);
        } catch(XmlPullParserException e){
            Log.v(TAG, ERROR_WHILE_PARSING_XML, e);
        } catch (IOException e){
            Log.v(TAG, "Ошибка при считывании данных", e);
        }
        return new ArrayList<>();
    }

    /**
     * Метод непосредственно парсящий файл с занятиями
     * @param directory папка в которой сохранен файл с занятиями
     * @param fileName имя файла
     * @return возвращает список занятий
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static List<SchoolDay> parseScheduleFromXML(File directory, String fileName)throws XmlPullParserException, IOException{
        List<SchoolDay> weekSchedule = new ArrayList<>();
        File file = new File(directory, fileName);
        FileReader reader = new FileReader(file);

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(reader);

        int eventType = parser.getEventType();
        String currentTag = null;
        SchoolDay currentSchoolDay;
        Schedule currentSchedule = null;
        Employee currentEmployee = new Employee();
        List<Schedule> currentScheduleList = new ArrayList<>();

        while(eventType != XmlPullParser.END_DOCUMENT){
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    Log.v(TAG, "start xml document");
                    break;
                case XmlPullParser.START_TAG:
                    currentTag = parser.getName();
                    currentSchedule = handleScheduleStartTag(currentTag, parser, currentSchedule, currentScheduleList);
                    break;
                case XmlPullParser.END_TAG:
                    currentTag = parser.getName();
                    break;
                case XmlPullParser.TEXT:
                    if (currentTag != null) {
                        switch (currentTag) {
                            case "auditory":
                                if (currentSchedule != null)
                                    currentSchedule.getAuditories().add(parser.getText());
                                break;
                            case "firstName":
                                currentEmployee.setFirstName(parser.getText());
                                break;
                            case "lastName":
                                currentEmployee.setLastName(parser.getText());
                                break;
                            case "middleName":
                                currentEmployee.setMiddleName(parser.getText());
                                if (currentSchedule != null)
                                    currentSchedule.getEmployeeList().add(currentEmployee);
                                currentEmployee = new Employee();
                                break;
                            case "photoLink":
                                if (currentSchedule != null) {
                                    currentSchedule.getEmployeeList().get(0).setPhotoURL(parser.getText());
                                }
                                break;
                            case "studentGroup":
                                if (currentSchedule != null)
                                    currentSchedule.setStudentGroup(parser.getText());
                                break;
                            case "lessonTime":
                                if (currentSchedule != null)
                                    currentSchedule.setLessonTime(parser.getText());
                                break;
                            case "lessonType":
                                if (currentSchedule != null)
                                    currentSchedule.setLessonType(parser.getText());
                                break;
                            case "note":
                                if (currentSchedule != null)
                                    currentSchedule.setNote(parser.getText());
                                break;
                            case "numSubgroup":
                                determineNumbSubGroup(parser, currentSchedule);
                                break;
                            case "subject":
                                if (currentSchedule != null)
                                    currentSchedule.setSubject(parser.getText());
                                break;
                            case "weekNumber":
                                determineWeekNumber(parser, currentSchedule);
                                break;
                            case "weekDay":
                                addToScheduleList(currentSchedule, currentScheduleList);
                                currentSchoolDay = new SchoolDay();
                                currentSchoolDay.setDayName(parser.getText());
                                currentSchoolDay.setSchedules(currentScheduleList);
                                weekSchedule.add(currentSchoolDay);

                                currentSchedule = null;
                                currentScheduleList = new ArrayList<>();
                                break;
                            default:
                                break;
                        }
                    } else {
                        return new ArrayList<>();
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return weekSchedule;
    }

    /**
     * Добавляет занятие в список занятий, если оно не равно null
     * @param currentSchedule занятие
     * @param scheduleList список занятий
     */
    private static void addToScheduleList(Schedule currentSchedule, List<Schedule> scheduleList){
        if (currentSchedule != null) {
            scheduleList.add(currentSchedule);
        }
    }

    private static void determineWeekNumber(XmlPullParser parser, Schedule currentSchedule){
        String weekNum = parser.getText();
        if (!"0".equals(weekNum)) {
            currentSchedule.getWeekNumbers().add(weekNum);
        }
    }

    private static void determineNumbSubGroup(XmlPullParser parser, Schedule currentSchedule){
        if (!"0".equals(parser.getText())) {
            currentSchedule.setSubGroup(parser.getText());
        } else{
            currentSchedule.setSubGroup("");
        }
    }

    /**
     * Метод обрабатывает тег "START_TAG" при парсинге списка занятий
     * @param currentTag текущий тег
     * @param parser парсер
     * @param passedCurrentSchedule текущее считываемое занятие
     * @param currentScheduleList результирующий список занятий
     * @return возвращает занятие
     */
    private static Schedule handleScheduleStartTag(String currentTag, XmlPullParser parser, Schedule passedCurrentSchedule, List<Schedule> currentScheduleList){
        Schedule currentSchedule = passedCurrentSchedule;
        if("schedule".equalsIgnoreCase(currentTag) && parser.getDepth() == 3)
        {
            if(currentSchedule != null) {
                currentScheduleList.add(currentSchedule);
            }
            currentSchedule = new Schedule();
        }
        return currentSchedule;
    }
}

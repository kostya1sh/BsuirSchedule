package by.bsuir.schedule.dataprovider;

import android.util.Log;

import by.bsuir.schedule.model.Employee;
import by.bsuir.schedule.model.StudentGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Класс предсотавляющий методы для загрузки расписаний
 */
public class LoadSchedule {
    private static final String STUDENT_GROUP_SCHEDULE_BY_ID_REST = "http://www.bsuir.by/schedule/rest/schedule/android/";
    private static final String EXAM_SCHEDULE = "http://www.bsuir.by/schedule/rest/examSchedule/android/";
    private static final String ACTUAL_APPLICATION_VERSION_URL = "http://www.bsuir.by/schedule/rest/android/actualAndroidVersion";
    private static final String EMPLOYEE_LIST_REST = "http://www.bsuir.by/schedule/rest/employee";
    private static final String SCHEDULE_EMPLOYEE_REST = "http://www.bsuir.by/schedule/rest/employee/android/";
    private static final String EXAM_SCHEDULE_EMPLOYEE = "http://www.bsuir.by/schedule/rest/examSchedule/employee/";
    private static final String STUDENT_GROUP_REST = "http://www.bsuir.by/schedule/rest/studentGroup/";
    private static final String LAST_UPDATE_DATE_EMPLOYEE_REST = "http://www.bsuir.by/schedule/rest/lastUpdateDate/employee/";
    private static final String LAST_UPDATE_DATE_STUDENT_GROUP_REST = "http://www.bsuir.by/schedule/rest/lastUpdateDate/studentGroup/";
    private static final String TAG = "Load";

    /**
     * Класс предоставляющий методы для загрузки расписаний
     */
    private LoadSchedule(){
    }

    /**
     * Метод загружает расписание занятий для группы
     * @param sg группа для которой нужно загрузить расписание занятий
     * @param fileDir файл в который нужно сохранить скачанное расписание
     * @return Возвращает null если скачивание прошло успешно, иначе возвращает сообщение об ошибке
     */
    public static String loadScheduleForStudentGroupById(StudentGroup sg, File fileDir){
        try{
            URL url = new URL(STUDENT_GROUP_SCHEDULE_BY_ID_REST + sg.getStudentGroupId().toString());
            loadSchedule(url, fileDir, sg.getStudentGroupName());

            url = new URL(EXAM_SCHEDULE + sg.getStudentGroupId().toString());
            loadSchedule(url, fileDir, sg.getStudentGroupName()+ "exam");

            return null;
        } catch (SocketTimeoutException e) {
            Log.v(TAG, e.getMessage(), e);
            return "Ошибка подключения. Сервер не отвечает.";
        } catch (IOException e) {
            Log.v("logs", e.toString(), e);
            return "Группа " + sg.getStudentGroupName() + " не найдена. Проверьте соединение с интернетом." + e.toString();
        }
    }

    /**
     * Скачивает расписание занятий и расписание экзаменов для преподавателя
     * @param employeeName Имя преподавателя для которого нужно скачать расписание
     * @param filesDir имя файла в который нужно сохранить скачанное расписание
     * @return Возвращает null если скачивание прошло успешно, иначе возвращает сообщение об ошибке
     */
    public static String loadScheduleForEmployeeById(Employee employeeName, File filesDir){
        try {
            //employeeName contains last name and id
            //get all digits from passed employeeName
            //it will be employeeId. Construct URL with this id

            URL url = new URL(SCHEDULE_EMPLOYEE_REST + employeeName.getId());
            loadSchedule(url, filesDir, employeeName.getLastName() + employeeName.getFirstName().charAt(0) +
                                        employeeName.getMiddleName().charAt(0));

            url = new URL(EXAM_SCHEDULE_EMPLOYEE + employeeName.getId());
            loadSchedule(url, filesDir, employeeName.getLastName() + employeeName.getFirstName().charAt(0) +
                    employeeName.getMiddleName().charAt(0) + "exam");

            return null;
        } catch (SocketTimeoutException e) {
            Log.v(TAG, e.getMessage(), e);
            return "Ошибка подключения. Сервер не отвечает.";
        } catch (IOException e) {
            Log.v("logs", e.toString(), e);
            return "Расписание для " + employeeName + " не найдено." + e.toString();
        }
    }

    /**
     * Метод скачивает расписание и сохраняет его в файл
     * @param filesDir папка в которой сохраняется скачанное расписание
     * @param employeeName имя преподавателя для которого надо загрузить расписание
     * @return null - если загружено, строку с описанием ошибки если произошла ошибка
     */
    public static String loadScheduleForEmployee(String employeeName, File filesDir){
        try {
            //employeeName contains last name and id
            //get all digits from passed employeeName
            //it will be employeeId. Construct URL with this id
            String employeeId = employeeName.replaceAll("\\D+","");
            URL url = new URL(SCHEDULE_EMPLOYEE_REST + employeeId);
            loadSchedule(url, filesDir, employeeName);

            url = new URL(EXAM_SCHEDULE_EMPLOYEE + employeeId);
            loadSchedule(url, filesDir, employeeName + "exam");
            return null;
        } catch (SocketTimeoutException e) {
            Log.v(TAG, e.getMessage(), e);
            return "Ошибка подключения. Сервер не отвечает.";
        } catch (IOException e) {
            Log.v("logs", e.toString(), e);
            return "Расписание для " + employeeName + " не найдено." + e.toString();
        }
    }

    /**
     * Метод скачивает расписание и сохраняет его в файл
     * @param url url  по которому скачивается расписание
     * @param fileDir папка в которой сохраняется скачанное расписание
     * @param fileName файл в который сохранятеся скачанное расписание
     * @throws IOException
     */
    private static void loadSchedule(URL url, File fileDir, String fileName) throws IOException{
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        urlConnection.setConnectTimeout(5000);
        urlConnection.connect();

        InputStream inputStream = urlConnection.getInputStream();
        File file = new File(fileDir, fileName + ".xml");
        FileOutputStream fileOutput = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int bufferLength;
        while ((bufferLength = inputStream.read(buffer)) > 0) {
            fileOutput.write(buffer, 0, bufferLength);
        }
        inputStream.close();
        fileOutput.close();
        urlConnection.disconnect();
        Log.v(TAG, "Расписание успешно загружено!");
    }

    /**
     * Скачивает список всех студенченских групп у которых есть расписание
     * @return Возвращает список групп у которых есть расписание
     */
    public static List<StudentGroup> loadAvailableStudentGroups(){
        BufferedReader reader = null;
        try{
            URL url = new URL(STUDENT_GROUP_REST);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();

            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null){
                result.append(line);
            }
            return XmlDataProvider.parseListStudentGroupXml(result.toString());
        } catch (Exception e){
            Log.v(TAG, e.toString(), e);
        } finally {
            try{
                if(reader != null){
                    reader.close();
                }
            } catch (IOException e){
                Log.v(TAG, e.toString(), e);
            }
        }
        return new ArrayList<>();
    }

    /**
     * Скачивает список всех преподавателей у которых есть расписание
     * @return Возвращает список преподавателей у которых есть расписание
     */
    public static List<Employee> loadListEmployee() {
        BufferedReader reader = null;
        try {
            URL url = new URL(EMPLOYEE_LIST_REST);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();

            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return XmlDataProvider.parseListEmployeeXml(result.toString());
        } catch (Exception e) {
            Log.v(TAG, e.toString(), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }

            } catch (IOException e){
                Log.v(TAG, e.toString(), e);
            }
        }
        return new ArrayList<>();
    }

    /**
     * Получает через веб сервис дату последнего обновления расписания для студенченской группы
     * @param passedStudentGroupName группа для которой нужно скачать дату обновления
     * @return возвращает дату последнего обновления
     */
    public static Date loadLastUpdateDateForStudentGroup(String passedStudentGroupName, String dateFormatTemplate){
        String studentGroupName = passedStudentGroupName;
        if(".xml".equalsIgnoreCase(studentGroupName.substring(studentGroupName.length() - 4, studentGroupName.length()))){
            studentGroupName = studentGroupName.substring(0, studentGroupName.length() - 4);
        }
        String studentGroupId =  studentGroupName.substring(6, studentGroupName.length());
        if(!studentGroupId.isEmpty()){
            String url = LAST_UPDATE_DATE_STUDENT_GROUP_REST;
            url += studentGroupId;
            String lastUpdateDateAsString = loadLastUpdateDate(url);
            if(lastUpdateDateAsString != null) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatTemplate, Locale.getDefault());
                    return dateFormat.parse(lastUpdateDateAsString);
                } catch (ParseException e) {
                    Log.e(TAG, "error while parsing date", e);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Получает через веб сервис дату последнего обновления расписания для студенченской группы
     * @param studentGroupName группа для которой нужно скачать дату обновления
     * @return возвращает дату последнего обновления
     */
    public static Date loadLastUpdateDateForStudentGroup(StudentGroup studentGroupName, String dateFormatTemplate){

        if(!studentGroupName.getStudentGroupId().toString().isEmpty()){
            String url = LAST_UPDATE_DATE_STUDENT_GROUP_REST;
            url += studentGroupName.getStudentGroupId().toString();
            String lastUpdateDateAsString = loadLastUpdateDate(url);
            if(lastUpdateDateAsString != null) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatTemplate, Locale.getDefault());
                    return dateFormat.parse(lastUpdateDateAsString);
                } catch (ParseException e) {
                    Log.e(TAG, "error while parsing date", e);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Получает через веб сервис дату последнего обновления расписания для преподавателя
     * @param employeeName Имя преподавателя для которого нужно скачать дату последнего обновления расписания
     * @return Возвращает дату последнего обновления расписания для преподавателя
     */
    public static Date loadLastUpdateDateForEmployee(String employeeName, String dateFormatTemplate){
        String employeeId = employeeName.replaceAll("\\D+", "");
        if(employeeId != null && !employeeId.isEmpty()){
            String url = LAST_UPDATE_DATE_EMPLOYEE_REST;
            url += employeeId;
            String lastUpdateDateAsString = loadLastUpdateDate(url);
            if(lastUpdateDateAsString != null) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatTemplate, Locale.getDefault());
                    return dateFormat.parse(lastUpdateDateAsString);
                } catch (ParseException e) {
                    Log.e(TAG, "error while parsing date", e);
                    return null;
                }
            }
        }
        return null;
    }


    /**
     * Получает через веб сервис дату последнего обновления расписания для преподавателя
     * @param employeeName Имя преподавателя для которого нужно скачать дату последнего обновления расписания
     * @return Возвращает дату последнего обновления расписания для преподавателя
     */
    public static Date loadLastUpdateDateForEmployee(Employee employeeName, String dateFormatTemplate){

        if(employeeName.getId() != null && !employeeName.getId().toString().isEmpty()){
            String url = LAST_UPDATE_DATE_EMPLOYEE_REST;
            url += employeeName.getId().toString();
            String lastUpdateDateAsString = loadLastUpdateDate(url);
            if(lastUpdateDateAsString != null) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatTemplate, Locale.getDefault());
                    return dateFormat.parse(lastUpdateDateAsString);
                } catch (ParseException e) {
                    Log.e(TAG, "error while parsing date", e);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Использует веб сервис для получения даты последнего обновления расписания
     * @param urlForDownload url веб сервиса
     * @return возвращает дату последнего обновления ввиде строки
     */
    public static String loadLastUpdateDate(String urlForDownload){
        return loadStringFromWebService(urlForDownload);
    }

    /**
     * Скачивает актуальную версию приложения
     * @return возвращает актуальную версию андроид приложения
     */
    public static String loadActualApplicationVersion(){
        return loadStringFromWebService(ACTUAL_APPLICATION_VERSION_URL);
    }

    /**
     * Базовый метод для загрузки информации через веб сервис
     * @param serviceURL url веб сервиса
     * @return возвращает скачанную строку
     */
    public static String loadStringFromWebService(String serviceURL){
        BufferedReader reader = null;
        try{
            URL url = new URL(serviceURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.isEmpty()){
                return line;
            }
        } catch (IOException e){
            Log.v(TAG, e.toString(), e);
        } finally {
            try {
                if(reader != null){
                    reader.close();
                }
            } catch (IOException e){
                Log.v(TAG, e.toString(), e);
            }
        }
        return null;
    }
}

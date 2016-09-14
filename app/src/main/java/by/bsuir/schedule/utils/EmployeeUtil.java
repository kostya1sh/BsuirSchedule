package by.bsuir.schedule.utils;

import by.bsuir.schedule.model.Employee;

/**
 * Created by iChrome on 18.08.2015.
 */
public class EmployeeUtil {
    private EmployeeUtil(){
    }

    /**
     * Метод формирует ФИО для переданного преподавателя
     * @param employee Объект преподавателя из которого нужно сформировать ФИО
     * @return Возвращает ФИО
     */
    public static String getEmployeeFIO(Employee employee) {
        String fio = employee.getLastName();
        if (employee.getFirstName() != null && (employee.getFirstName().length() > 0)) {
            fio += " " + employee.getFirstName().substring(0, 1) + ".";
            if (employee.getMiddleName() != null && (employee.getMiddleName().length() > 0)) {
                fio += " " + employee.getMiddleName().substring(0, 1) + ".";
            }
        }
        return fio;
    }

    /**
     * Метод получет фамилию преподавателя из имени файла
     * @param fileName имя файла
     * @return фамилия преподавателя
     */
    public static String getEmployeeLastNameFromFile(String fileName) {
        int lastCharPos;

        //4 symbols = file extension , 8 symbols = teacher id (file name contain id only after download),
        // 2 symbols = first and middle name (after first save)
        //if first symbol after extension is digit then file name contains teachers id
        if (FileUtil.isDigit(fileName.charAt(fileName.length() - 4 - 1))) {
            lastCharPos = fileName.length() - 8 - 4; // delete all except last name
        }
        else {
            if (!fileName.contains(".xml")) {
                lastCharPos = fileName.length() - 2; // delete firs and middle name
            } else {
                lastCharPos = fileName.length() - 2 - 4; // ... and ".xml"
            }
        }
        return fileName.substring(0, lastCharPos);
    }
}

package by.bsuir.schedule.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Лол on 05.08.2015.
 */
public class Schedule implements Serializable{
    private List<String> employees = new ArrayList<>();
    private List<Employee> employeeList = new ArrayList<>();
    private List<String> auditories = new ArrayList<>();
    private List<String> weekNumbers = new ArrayList<>();
    private Long weekDay;
    private String date;
    private String lessonTime = "";
    private String lessonType = "";
    private String subject = "";
    private String subGroup = "";
    private String studentGroup = "";
    private String note = "";
    private String scheduleTableRowId = "";
    private boolean isHidden = false;

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public String getScheduleTableRowId() {
        return scheduleTableRowId;
    }

    public void setScheduleTableRowId(String rowId) {
        this.scheduleTableRowId = rowId;
    }

    public List<String> getEmployees() {
        return employees;
    }

    public void setEmployees(List<String> employees) {
        this.employees = employees;
    }

    public List<String> getAuditories() {
        return auditories;
    }

    public void setAuditories(List<String> auditories) {
        this.auditories = auditories;
    }

    public List<String> getWeekNumbers() {
        return weekNumbers;
    }

    public void setWeekNumbers(List<String> weekNumbers) {
        this.weekNumbers = weekNumbers;
    }

    public String getLessonTime() {
        return lessonTime;
    }

    public void setLessonTime(String lessonTime) {
        this.lessonTime = lessonTime;
    }

    public String getLessonType() {
        return lessonType;
    }

    public void setLessonType(String lessonType) {
        this.lessonType = lessonType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }

    public String getStudentGroup() {
        return studentGroup;
    }

    public void setStudentGroup(String studentGroup) {
        this.studentGroup = studentGroup;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(Long weekDay) {
        this.weekDay = weekDay;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

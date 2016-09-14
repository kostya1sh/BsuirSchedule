package by.bsuir.schedule.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Лол on 05.08.2015.
 */
public class SchoolDay implements Serializable{
    private String dayName;
    private List<Schedule> schedules;

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }
}

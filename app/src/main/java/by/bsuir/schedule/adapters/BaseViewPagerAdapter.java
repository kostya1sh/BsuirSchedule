package by.bsuir.schedule.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import by.bsuir.schedule.model.SchoolDay;
import by.bsuir.schedule.model.SubGroupEnum;
import by.bsuir.schedule.model.WeekNumberEnum;

import java.util.List;

/**
 * Created by iChrome on 23.09.2015.
 */
public abstract class BaseViewPagerAdapter extends FragmentStatePagerAdapter {
    protected static final int LEFT = 0;
    protected static final int RIGHT = 2;
    protected Integer selectedDayPosition;
    protected WeekNumberEnum selectedWeekNumber;
    protected SubGroupEnum selectedSubGroupNumber;
    protected boolean showHidden;

    protected List<SchoolDay> allSchedules;

    /**
     * Базовый адаптер для ViewPager
     * @param fm переменная для хранения списка фрагментов
     */
    public BaseViewPagerAdapter(FragmentManager fm){
        super(fm);
    }

    /**
     * Метод для получения выбранного дня
     * @return Возвращает выбранный день
     */
    public Integer getSelectedDayPosition() {
        return selectedDayPosition;
    }

    /**
     * Устанавливает текущий день
     * @param selectedDayPosition Выбранный пользователь день недели
     */
    public void setSelectedDayPosition(Integer selectedDayPosition) {
        this.selectedDayPosition = selectedDayPosition;
    }

    /**
     * Устанавливает флаг показа скрытых запмсей
     * @param show показывать или нет
     */
    public void setShowHidden(boolean show) {
        this.showHidden = show;
    }

    /**
     * Метод для получения выбранной учебной недели
     * @return Возвращает выбранную неделю
     */
    public WeekNumberEnum getSelectedWeekNumber() {
        return selectedWeekNumber;
    }

    /**
     * Возвращает флаг показа скрытых запмсей
     */
    public boolean getShowHidden() {
        return showHidden;
    }

    /**
     * Устанавливает текущую учебную неделю
     * @param selectedWeekNumber Выбранная пользователем учебная неделя
     */
    public void setSelectedWeekNumber(WeekNumberEnum selectedWeekNumber) {
        this.selectedWeekNumber = selectedWeekNumber;
    }

    /**
     * Метод для получения выбранной подгруппы
     * @return Возвращает текущую выбранную подгруппу
     */
    public SubGroupEnum getSelectedSubGroupNumber() {
        return selectedSubGroupNumber;
    }

    /**
     * Устанавливает текущую подгруппу
     * @param selectedSubGroupNumber выбранная пользователем
     */
    public void setSelectedSubGroupNumber(SubGroupEnum selectedSubGroupNumber) {
        this.selectedSubGroupNumber = selectedSubGroupNumber;
    }

    /**
     * Метод для получения списка всех занятий
     * @return Возвращает список занятий
     */
    public List<SchoolDay> getAllSchedules() {
        return allSchedules;
    }

    /**
     * Устанавливает список всех занятий
     * @param allSchedules Список занятий для выбранной пользовтаелем группы или преподавателя
     */
    public void setAllSchedules(List<SchoolDay> allSchedules) {
        this.allSchedules = allSchedules;
    }
}

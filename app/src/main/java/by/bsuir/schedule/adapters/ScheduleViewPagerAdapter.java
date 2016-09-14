package by.bsuir.schedule.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import by.bsuir.schedule.ScheduleFragmentForGroup;

/**
 * Created by iChrome on 01.09.2015.
 */
public class ScheduleViewPagerAdapter extends BaseViewPagerAdapter {

    /**
     * адаптер для ViewPager  в расписании занятий
     * @param fm переменная для хранения списка фрагментов
     */
    public ScheduleViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Метод создает фрагмент по переданному номеру
     * @param i позиция во ViewPager для которой нужно создать фрагмент
     * @return возвращает созданный фрагмент
     */
    @Override
    public Fragment getItem(int i) {
        Integer calculatedDayPosition;
        switch(i){
            case LEFT:
                calculatedDayPosition = calculateDayPositionForLeftScreen(selectedDayPosition);
                break;
            case RIGHT:
                calculatedDayPosition = calculateDayPositionForRightScreen(selectedDayPosition);
                break;
            default:
                calculatedDayPosition = selectedDayPosition;
        }
        return ScheduleFragmentForGroup.newInstance(getAllSchedules(), calculatedDayPosition, getSelectedWeekNumber(), getSelectedSubGroupNumber(), getShowHidden());
    }

    /**
     * Высчитываем день недели который будет отображаться во ViewPager если пользователь сделал свап влево.
     * Если день недели равен 0, то возвращаем последний день недели. Это сделано для того чтобы
     * ViewPager был зациклен
     * @param selectedDayPosition текущий день недели
     * @return возвращает будущий день недели
     */
    protected static Integer calculateDayPositionForLeftScreen(Integer selectedDayPosition){
        Integer result;
        if(selectedDayPosition == 0){
            result = 6;
        } else {
            result = selectedDayPosition - 1;
        }
        return result;
    }

    /**
     * Высчитываем день недели который будет отображаться во ViewPager если пользователь сделал свап вправо.
     * Если день недели равен последнему дню недели, то возвращаем ноль. Это сделано для того чтобы
     * ViewPager был зациклен
     * @param selectedDayPosition текущий день недели
     * @return возвращает будущий день недели
     */
    protected static Integer calculateDayPositionForRightScreen(Integer selectedDayPosition){
        Integer result;
        if(selectedDayPosition == 6){
            result = 0;
        } else{
            result = selectedDayPosition + 1;
        }
        return result;
    }

    /**
     * Количество элементов во ViewPager
     * @return количетсво доступных страниц во ViewPager
     */
    @Override
    public int getCount() {
        return 3;
    }
}

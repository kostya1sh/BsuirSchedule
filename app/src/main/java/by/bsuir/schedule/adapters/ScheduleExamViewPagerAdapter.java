package by.bsuir.schedule.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import by.bsuir.schedule.ExamScheduleFragment;

/**
 * Created by iChrome on 01.09.2015.
 */
public class ScheduleExamViewPagerAdapter extends BaseViewPagerAdapter {

    /**
     * Адаптер для ViewPager в расписании экзаменов
     * @param fm переменная служащая для хранения фрагментов
     */
    public ScheduleExamViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    /**
     * Метод создает фрагмент по переданной позиции
     * @param i позиция для которой нужно создать и вернуть фрагмент
     * @return возвращаем созданный фрагмент
     */
    @Override
    public Fragment getItem(int i) {
        Integer calculatedDayPosition;
        switch(i){
            case LEFT:
                calculatedDayPosition = calculateDayPositionForLeftScreen();
                break;
            case RIGHT:
                calculatedDayPosition = calculateDayPositionForRightScreen();
                break;
            default:
                calculatedDayPosition = selectedDayPosition;
        }
        return ExamScheduleFragment.newInstance(getAllSchedules(), calculatedDayPosition, showHidden);
    }

    /**
     * Метод высчитывает номер дня, если пользователь делает свап влево. Если текущий выбранный день
     * с нулевым индексом, то возвращаем последний день. Это сделано для того чтобы ViewPager был
     * зацикленным
     * @return возвращает номер дня для которого потом создается фрагмент
     */
    private Integer calculateDayPositionForLeftScreen(){
        Integer result;
        if(selectedDayPosition == 0){
            result = allSchedules.size() - 1;
        } else {
            result = selectedDayPosition - 1;
        }
        return result;
    }

    /**
     * Метод высчитывает номер дня, если пользователь делает свап вправо. Если текущий выбранный день
     * с последний индексом, то возвращаем первый день.
     * @return возвращает номер дня для которого создастся фрагмент
     */
    private Integer calculateDayPositionForRightScreen(){
        Integer result;
        if(selectedDayPosition == allSchedules.size() - 1){
            result = 0;
        } else {
            result = selectedDayPosition + 1;
        }
        return result;
    }

    /**
     * Возвращает количество элементов во ViewPager
     * @return количество элементов во ViewPager
     */
    @Override
    public int getCount() {
        if (!ExamScheduleFragment.refreshed()) {
            ExamScheduleFragment.setRefreshed(true);
            this.notifyDataSetChanged();
        }

        if (allSchedules != null)
            return allSchedules.size();

        return 0;
    }
}

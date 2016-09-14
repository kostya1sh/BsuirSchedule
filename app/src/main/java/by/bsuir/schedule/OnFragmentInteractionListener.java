package by.bsuir.schedule;

import by.bsuir.schedule.model.AvailableFragments;

/**
 * Created by iChrome on 13.08.2015.
 */
public interface OnFragmentInteractionListener {
    /**
     * Метод вызывается при изменении текущего активного фрагмента
     * @param passedFragment новый активный фрагмент
     */
    void onChangeFragment(AvailableFragments passedFragment);

    /**
     * Изменение активного дня в расписании занятий
     * @param dayPosition новый выбранный день недели
     */
    void onChangeDay(Integer dayPosition);

    /**
     * Изменение активного дня в расписании экзаменов
     * @param dayPosition новый выбранный день
     */
    void onChangeExamDay(Integer dayPosition);
}

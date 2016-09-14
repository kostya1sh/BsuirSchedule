package by.bsuir.schedule;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;

import by.bsuir.schedule.adapters.ScheduleViewPagerAdapter;
import by.bsuir.schedule.model.SchoolDay;
import by.bsuir.schedule.model.SubGroupEnum;
import by.bsuir.schedule.model.WeekNumberEnum;

import java.io.Serializable;
import java.util.List;

/**
 * ViewPager для отображения списка занятий
 */
public class ScheduleViewPagerFragment extends Fragment {
    private static final String TAG = "schedViewPager";
    private static final int PAGE_LEFT = 0;
    private static final int PAGE_MIDDLE = 1;
    private static final int PAGE_RIGHT = 2;
    private static final String SCHEDULES_PARCELABLE_KEY = "scheduleParcelableKey";
    private static final String CURRENT_MIDDLE_INDEX_KEY = "currentMiddleIndexKey";
    private static final String CURRENT_WEEK_NUMBER_KEY = "currentWeekNumberKey";
    private static final String CURRENT_SUB_GROUP_KEY = "currentSubGroupKey";
    private static final String CURRENT_SHOW_HIDDEN_KEY = "currentShowHiddenKey";

    private ViewPager scheduleViewPager;
    private List<SchoolDay> allWeekSchedules;
    private OnFragmentInteractionListener baseActivity;
    private Integer currentMiddleIndex;
    private Integer currentSelectedIndex;
    private WeekNumberEnum selectedWeekNumber = WeekNumberEnum.ALL;
    private SubGroupEnum selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;
    private boolean selectedShowHidden;

    /**
     * ViewPager для отображения списка занятий
     */
    public ScheduleViewPagerFragment() {
        // Required empty public constructor
    }

    /**
     * Статический метод для создания экземпляра фрагмента
     * @return
     */
    public static ScheduleViewPagerFragment newInstance(List<SchoolDay> schedules, int currentMiddleIndex, WeekNumberEnum currentWeekNumber,
                                                        SubGroupEnum currentSubGroup, boolean showHidden) {
        ScheduleViewPagerFragment result = new ScheduleViewPagerFragment();
        Bundle args = new Bundle();
        args.putSerializable(SCHEDULES_PARCELABLE_KEY, (Serializable) schedules);
        args.putInt(CURRENT_MIDDLE_INDEX_KEY, currentMiddleIndex);
        args.putSerializable(CURRENT_WEEK_NUMBER_KEY, currentWeekNumber);
        args.putSerializable(CURRENT_SUB_GROUP_KEY, currentSubGroup);
        args.putBoolean(CURRENT_SHOW_HIDDEN_KEY, showHidden);
        result.setArguments(args);
        result.setAllWeekSchedules(schedules);
        result.setCurrentMiddleIndex(currentMiddleIndex);
        result.setSelectedWeekNumber(currentWeekNumber);
        result.setSelectedSubGroup(currentSubGroup);
        result.setShowHidden(showHidden);
        return result;
    }

    /**
     * Метод вызывается при первоначальном создании фрагмента.
     * Также метод вызывается андроидом при пересоздании фрагмента. Метод вызывается, например, если приложение было в фоне,
     * и андроид удалил фрагмент из памяти. При попытке пользователем сново открыть приложение
     * фрагмент будет пересоздан
     * @param args бандл с параметрами, которые мы засетили в метод newInstance()
     */
    @Override
    public void onCreate(Bundle args){
        super.onCreate(args);
        if(args != null) {
            List<SchoolDay> schedules = (List<SchoolDay>) args.getSerializable(SCHEDULES_PARCELABLE_KEY);
            setAllWeekSchedules(schedules);
            setCurrentMiddleIndex(args.getInt(CURRENT_MIDDLE_INDEX_KEY, 0));
            setSelectedWeekNumber((WeekNumberEnum) args.getSerializable(CURRENT_WEEK_NUMBER_KEY));
            setSelectedSubGroup((SubGroupEnum) args.getSerializable(CURRENT_SUB_GROUP_KEY));
            setShowHidden(args.getBoolean(CURRENT_SHOW_HIDDEN_KEY));
        }
    }

    /**
     * Метод создает View
     * @param inflater Объект с помощью которого создается View
     * @param container Родительское View
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return Возвращает созданное View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule_view_pager, container, false);
        scheduleViewPager = (ViewPager) view.findViewById(R.id.scheduleViewPager);
        scheduleViewPager.addOnPageChangeListener(new ViewPagerChangeListener());

        updateFiltersForViewPager(getCurrentMiddleIndex(), getSelectedWeekNumber(), getSelectedSubGroup(), getShowHidden());
        return view;
    }


    /**
     * Метод настраивает ViewPager для отображения списка занятий используя переданные параметры
     * @param dayPosition выбранный день недели
     * @param weekNumber выбранная учебная неделя
     * @param subGroup выбранная подгруппа
     * @return null
     */
    public Void updateFiltersForViewPager(Integer dayPosition, WeekNumberEnum weekNumber, SubGroupEnum subGroup,
                                          boolean showHidden) {
        if(getActivity() != null) {
            selectedWeekNumber = weekNumber;
            selectedSubGroup = subGroup;
            selectedShowHidden = showHidden;
            ScheduleViewPagerAdapter adapter = new ScheduleViewPagerAdapter(getActivity().getSupportFragmentManager());
            adapter.setAllSchedules(getAllWeekSchedules());
            currentMiddleIndex = dayPosition;
            adapter.setSelectedDayPosition(dayPosition);
            adapter.setSelectedWeekNumber(weekNumber);
            adapter.setSelectedSubGroupNumber(subGroup);
            adapter.setShowHidden(showHidden);
            scheduleViewPager.setAdapter(adapter);
            scheduleViewPager.setCurrentItem(PAGE_MIDDLE);
        }
        return null;
    }

    /**
     * Метод вызывается при присоединении фрагмента к активити
     * @param activity Активити к которой присоединяется фрагмент
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            baseActivity = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            Log.v(TAG, e.getMessage(), e);
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Метод вызывается при отсоединении фрагмента от активити
     */
    @Override
    public void onDetach() {
        super.onDetach();
        baseActivity = null;
    }

    public List<SchoolDay> getAllWeekSchedules() {
        return allWeekSchedules;
    }

    public void setAllWeekSchedules(List<SchoolDay> allWeekSchedules) {
        this.allWeekSchedules = allWeekSchedules;
    }

    public void setShowHidden(boolean show) {
        this.selectedShowHidden = show;
    }

    public Integer getCurrentMiddleIndex() {
        return currentMiddleIndex;
    }

    public void setCurrentMiddleIndex(Integer currentMiddleIndex) {
        this.currentMiddleIndex = currentMiddleIndex;
    }

    public SubGroupEnum getSelectedSubGroup() {
        return selectedSubGroup;
    }

    public boolean getShowHidden() {
        return selectedShowHidden;
    }

    public void setSelectedSubGroup(SubGroupEnum selectedSubGroup) {
        this.selectedSubGroup = selectedSubGroup;
    }

    public WeekNumberEnum getSelectedWeekNumber() {
        return selectedWeekNumber;
    }

    public void setSelectedWeekNumber(WeekNumberEnum selectedWeekNumber) {
        this.selectedWeekNumber = selectedWeekNumber;
    }

    /**
     * Обработчик событий во ViewPager
     */
    private class ViewPagerChangeListener implements ViewPager.OnPageChangeListener {

        /**
         * Метод вызывается когда текущая страница скролится
         * @param position позиция текущей отображаемой страницы
         * @param positionOffset значение от [0, 1) отображающее смещение
         * @param positionOffsetPixels смещение в пикселях
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //nothing to do
        }

        /**
         * Вызывается когда пользователь делает свап влево или вправо
         * @param position позиция новой выбранной страницы во ViewPager
         */
        @Override
        public void onPageSelected(int position) {
            currentSelectedIndex = position;
            if (currentSelectedIndex == PAGE_LEFT) {
                if(currentMiddleIndex == 0){
                    currentMiddleIndex = 6;
                } else {
                    currentMiddleIndex--;
                }
                // user swiped to right direction
            } else if (currentSelectedIndex == PAGE_RIGHT) {

                if(currentMiddleIndex == 6){
                    currentMiddleIndex = 0;
                } else{
                    currentMiddleIndex++;
                }

            }
            baseActivity.onChangeDay(currentMiddleIndex);
        }

        /**
         * Вызывается при изменении состояния ViewPager
         * @param state новое состояние
         */
        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE && currentSelectedIndex != null) {
                updateFiltersForViewPager(currentMiddleIndex, selectedWeekNumber, selectedSubGroup, selectedShowHidden);
            }
        }
    }
}

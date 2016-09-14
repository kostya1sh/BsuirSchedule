package by.bsuir.schedule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.myapplication.R;

import by.bsuir.schedule.adapters.ArrayAdapterEmployeeSchedule;
import by.bsuir.schedule.adapters.ArrayAdapterGroupSchedule;
import by.bsuir.schedule.dao.DBHelper;
import by.bsuir.schedule.dao.SchoolDayDao;
import by.bsuir.schedule.model.Employee;
import by.bsuir.schedule.model.Schedule;
import by.bsuir.schedule.model.SchoolDay;
import by.bsuir.schedule.model.SubGroupEnum;
import by.bsuir.schedule.model.WeekNumberEnum;
import by.bsuir.schedule.utils.DateUtil;
import by.bsuir.schedule.utils.FileUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ScheduleFragmentForGroup extends Fragment {
    private static final String ARG_ALL_SCHEDULES = "groupAllSchedules";
    private static final String ARG_CURRENT_POSITION = "currentPosition";
    private static final String ARG_SELECTED_WEEK_NUMBER = "selectedWeekNumber";
    private static final String ARG_SELECTED_SUB_GROUP = "selectedSubGroup";
    private static final String ARG_SHOW_HIDDEN_SCHEDULE = "showHidden";
    private static final Integer SUNDAY = 6;
    private Schedule[] schedulesForShow;
    private List<SchoolDay> allScheduleForGroup;
    private View currentView;
    private String[] weekDays = new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
    private Context context;
    private Integer currentPosition;
    private WeekNumberEnum selectedWeekNumber;
    private SubGroupEnum selectedSubGroup;
    private boolean showHidden;

    private ArrayAdapterGroupSchedule groupAdapter;
    private ArrayAdapterEmployeeSchedule empAdapter;

    /**
     * Фрагмент для отображения списка занятий для студенченской группы
     */
    public ScheduleFragmentForGroup() {
         // Mandatory empty constructor for the fragment manager to instantiate the
         // fragment (e.g. upon screen orientation changes).
    }

    /**
     * Статический метод для создания экземпляра фрагмента с заданными параметрами
     * @param allSchedules лист всех занятий на неделю
     * @param position текущий выбранный день недели
     * @param weekNumber текущая выбранная учебная неделя
     * @param subGroup текущая выбранная подгруппа
     * @return возвращает созданный фрагмент
     */
    public static ScheduleFragmentForGroup newInstance(List<SchoolDay> allSchedules,int position, WeekNumberEnum weekNumber,
                                                       SubGroupEnum subGroup, boolean showHidden) {
        ScheduleFragmentForGroup fragment = new ScheduleFragmentForGroup();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALL_SCHEDULES, (Serializable) allSchedules);
        args.putInt(ARG_CURRENT_POSITION, position);
        args.putSerializable(ARG_SELECTED_WEEK_NUMBER, weekNumber);
        args.putSerializable(ARG_SELECTED_SUB_GROUP, subGroup);
        args.putBoolean(ARG_SHOW_HIDDEN_SCHEDULE, showHidden);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Метод вызывается при создании фрагмента
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            List<SchoolDay> schedules = (List<SchoolDay>) args.getSerializable(ARG_ALL_SCHEDULES);
            setAllScheduleForGroup(schedules);
            setCurrentPosition(args.getInt(ARG_CURRENT_POSITION));
            setSelectedWeekNumber((WeekNumberEnum) args.getSerializable(ARG_SELECTED_WEEK_NUMBER));
            setSelectedSubGroup((SubGroupEnum) args.getSerializable(ARG_SELECTED_SUB_GROUP));
            setShowHidden(args.getBoolean(ARG_SHOW_HIDDEN_SCHEDULE));
        }
    }


    /**
     * Метод для создания view которую будет отбражаться пользователю
     * @param inflater Объект служащий создания view
     * @param container Родительское view
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return Возвращает view для отображения пользователю
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.show_schedule_fragment_layout, container, false);
        if(currentPosition != null) {
            filterScheduleList(currentPosition, selectedWeekNumber, selectedSubGroup, showHidden);
        }
        return currentView;
    }

    /**
     * Обновляет listView со списком занятий на выбранный день недели
     */
    public void updateListView(){
        if(currentView != null && getActivity() != null) {
            if (currentPosition == SUNDAY) {
                TextView tvAddSchedule = (TextView) currentView.findViewById(R.id.addNewSchedule);
                tvAddSchedule.setVisibility(View.INVISIBLE);
            }
            Integer currentWeekNumber = DateUtil.getWeek(Calendar.getInstance().getTime());
            TextView currentWeekTextView = (TextView) currentView.findViewById(R.id.currentWeekNumber);
            currentWeekTextView.setText("Сейчас " + currentWeekNumber + "-я уч. неделя");

            groupAdapter = new ArrayAdapterGroupSchedule(getActivity(),
                    R.layout.schedule_fragment_item_layout, schedulesForShow);

            empAdapter = new ArrayAdapterEmployeeSchedule(getActivity(),
                    R.layout.schedule_fragment_item_layout, schedulesForShow);

            ListView mainListView = (ListView) currentView.findViewById(R.id.showScheduleListView);
            //save adapter for getting item position in context menu

            if (FileUtil.isDefaultStudentGroup(getActivity())) {
                mainListView.setAdapter(groupAdapter);
                mainListView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Schedule s = groupAdapter.getSchedule(position);
                        Log.d("List View", s.getLessonTime() + " " + s.getLessonType() + " " + s.getSubGroup()
                                + " " + s.getSubject() + " " + s.getSubGroup() + " " + s.getStudentGroup());
                        createChooseActionDialog(s, false);
                    }
                });
                final TextView tvAddSchedule = (TextView) currentView.findViewById(R.id.addNewSchedule);
                final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.onclick_anim);
                tvAddSchedule.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvAddSchedule.startAnimation(anim);
                        Schedule s = allScheduleForGroup.get(0).getSchedules().get(0);
                        createAddDialog(s, false);
                    }
                });
            } else {
                mainListView.setAdapter(empAdapter);
                mainListView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Schedule s = empAdapter.getSchedule(position);
                        Log.d("List View", s.getLessonTime() + " " + s.getLessonType() + " " + s.getSubGroup()
                                + " " + s.getSubject() + " " + s.getSubGroup() + " " + s.getStudentGroup());
                        createChooseActionDialog(s, true);
                    }
                });
                final TextView tvAddSchedule = (TextView) currentView.findViewById(R.id.addNewSchedule);
                final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.onclick_anim);
                tvAddSchedule.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvAddSchedule.startAnimation(anim);
                        Schedule s;
                        try {
                            s = empAdapter.getSchedule(0);
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            Log.e("emp adapter", ex.getMessage(), ex);
                            int  i = 0;
                            while (getListSchedules(i).isEmpty()) {
                                i++;
                            }

                            s = getListSchedules(i).get(0);
                        }
                        createAddDialog(s, true);
                    }
                });
            }
            TextView emptyTextView = (TextView) currentView.findViewById(R.id.emptyResults);
            mainListView.setEmptyView(emptyTextView);
        }
    }

    /**
     * Обновляет лист со списком занятий на основе выбранных пользователем фильтров
     * @param dayPosition номере дня недели
     * @param weekNumber номер учебной недели
     * @param subGroupEnum номер подгруппы
     */
    public void filterScheduleList(Integer dayPosition, WeekNumberEnum weekNumber, SubGroupEnum subGroupEnum, boolean showHidden){
        List<Schedule> result = new ArrayList<>();
        List<Schedule> selectedSchoolDay = getListSchedules(dayPosition);
        for (Schedule schedule : selectedSchoolDay) {
            boolean matchWeekNumber = isMatchWeekNumber(weekNumber, schedule);
            boolean matchSubGroup = isMatchSubGroup(subGroupEnum, schedule);

            if (matchSubGroup && matchWeekNumber) {
                //если нужно показывать скрытые записи то показываем все для текущих настроек
                if (showHidden) {
                    result.add(schedule);
                } else if (!schedule.isHidden()) {
                    //не показывать скрытые записи
                    result.add(schedule);
                }
            }
        }
        schedulesForShow = result.toArray(new Schedule[result.size()]);
        updateListView();
    }

    /**
     * Фильтруем занятие на основе введенной пользователем подгруппы
     * @param subGroupEnum подгруппа введенная пользователем
     * @param schedule занятие которое нужно проверить
     * @return Возвращает false, если занятие не для выбранной подгруппы, иначе возвращает true
     */
    private static boolean isMatchSubGroup(SubGroupEnum subGroupEnum, Schedule schedule){
        boolean result = false;
        if (subGroupEnum.getOrder().equals(SubGroupEnum.ENTIRE_GROUP.getOrder())) {
            result = true;
        } else if (schedule.getSubGroup().isEmpty()) {
            result = true;
        } else {
            if (schedule.getSubGroup().equalsIgnoreCase(subGroupEnum.getOrder().toString())) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Проверяет занятие на соответствие введенной пользователем учебной недели
     * @param weekNumber Номер учебной недели введенной пользователем
     * @param schedule Занятие для проверки
     * @return Возвращает false, если занятие не проводится на выбранной неделе, иначе возвращает true
     */
    private static boolean isMatchWeekNumber(WeekNumberEnum weekNumber, Schedule schedule){
        boolean result = false;
        if (weekNumber.getOrder().equals(WeekNumberEnum.ALL.getOrder())) {
            result = true;
        } else {
            for (String weekNumberFromSchedule : schedule.getWeekNumbers()) {
                if (weekNumberFromSchedule.equalsIgnoreCase(weekNumber.getOrder().toString())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Возвращает список занятий для определенного дня недели
     * @param position номер дня недели
     * @return Возвращает список занятий в переданный день недели
     */
    public List<Schedule> getListSchedules(int position){
        if(position >= 0 && position < weekDays.length) {
            String dayAsString = weekDays[position];
            for (SchoolDay schoolDay : getAllScheduleForGroup()) {
                if (schoolDay.getDayName().equalsIgnoreCase(dayAsString)) {
                    return schoolDay.getSchedules();
                }
            }
        }
        return new ArrayList<>();
    }


    /**
     * Получает расписание которое будет показано
     */
    public Schedule[] getSchedulesForShow() {
        return schedulesForShow;
    }

    /**
     * Устанавливает расписание которое будет показано
     */
    public void setSchedulesForShow(Schedule[] schedulesForShow) {
        this.schedulesForShow = schedulesForShow;
    }

    /**
     * Получает все расписание
     */
    public List<SchoolDay> getAllScheduleForGroup() {
        return allScheduleForGroup;
    }

    /**
     * Устанавливает все расписание
     */
    public void setAllScheduleForGroup(List<SchoolDay> allScheduleForGroup) {
        this.allScheduleForGroup = allScheduleForGroup;
    }

    /**
     * Получает контекст текущего активити
     */
    public Context getContext() {
        return context;
    }

    /**
     * Устанавливает контекст
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Устанавливает текущий день
     */
    public void setCurrentPosition(Integer currentPosition) {
        this.currentPosition = currentPosition;
    }

    /**
     * Устанавливаетфлаг показа скрытых записей
     */
    public void setShowHidden(boolean show) {
        this.showHidden = show;
    }

    /**
     * Устанавливет номер недели
     */
    public void setSelectedWeekNumber(WeekNumberEnum selectedWeekNumber) {
        this.selectedWeekNumber = selectedWeekNumber;
    }

    /**
     * Возвращает текущий фрагмент
     */
    public ScheduleFragmentForGroup getThisFragment() {
        return this;
    }


    /**
     * Устанавливает номер подгруппы
     */
    public void setSelectedSubGroup(SubGroupEnum selectedSubGroup) {
        this.selectedSubGroup = selectedSubGroup;
    }

    //create view for action dialog
    private View getEmptyView(Schedule scheduleForFill, boolean isForGroup) {
        LayoutInflater ltInflater = getActivity().getLayoutInflater();
        View view = ltInflater.inflate(R.layout.edit_schedule_dialog, null, false);

        EditText etEmpLastName;
        EditText etEmpFirstName;
        EditText etEmpMiddleName;
        EditText etGroup;


        etGroup = (EditText) view.findViewById(R.id.etSetGroup);
        etGroup.setText(scheduleForFill.getStudentGroup());
        if (!isForGroup) {
            etGroup.setEnabled(false);
        }

        List<Employee> emps;
        emps = scheduleForFill.getEmployeeList();
        etEmpLastName = (EditText) view.findViewById(R.id.etSetEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetEmpMiddleName);
        String lastNameBuf = "";
        String firstNameBuf = "";
        String middleNameBuf = "";

        for (Employee e: emps) {
            lastNameBuf += e.getLastName();
            lastNameBuf += ",";

            firstNameBuf += e.getFirstName();
            firstNameBuf += ",";

            middleNameBuf += e.getMiddleName();
            middleNameBuf += ",";
        }
        if (lastNameBuf.length() > 0) {
            lastNameBuf = lastNameBuf.substring(0, lastNameBuf.length() - 1);
            firstNameBuf = firstNameBuf.substring(0, firstNameBuf.length() - 1);
            middleNameBuf = middleNameBuf.substring(0, middleNameBuf.length() - 1);
        }



        if (isForGroup) {
            etEmpFirstName.setText(firstNameBuf);
            etEmpLastName.setText(lastNameBuf);
            etEmpMiddleName.setText(middleNameBuf);
            etEmpFirstName.setEnabled(false);
            etEmpLastName.setEnabled(false);
            etEmpMiddleName.setEnabled(false);
        }

        return view;
    }

    //create view for action dialog
    private View getEditView(Schedule scheduleForFill, boolean isForGroup) {
        LayoutInflater ltInflater = getActivity().getLayoutInflater();
        View view = ltInflater.inflate(R.layout.edit_schedule_dialog, null, false);

        EditText etAud;
        EditText etSubj;
        EditText etEmpLastName;
        EditText etEmpFirstName;
        EditText etEmpMiddleName;
        EditText etNote;
        EditText etGroup;
        EditText etTime;
        EditText etLType;
        Spinner subGroupSpinner;
        CheckBox w1CheckBox;
        CheckBox w2CheckBox;
        CheckBox w3CheckBox;
        CheckBox w4CheckBox;

        etAud = (EditText) view.findViewById(R.id.etSetAud);
        String[] auds = scheduleForFill.getAuditories().toArray(new String[scheduleForFill.getAuditories().size()]);
        String buf = "";
        for (String str: auds) {
            buf += str;
        }
        etAud.setText(buf);

        etSubj = (EditText) view.findViewById(R.id.etSetSubj);
        etSubj.setText(scheduleForFill.getSubject());

        etGroup = (EditText) view.findViewById(R.id.etSetGroup);
        etGroup.setText(scheduleForFill.getStudentGroup());
        if (!isForGroup) {
            etGroup.setEnabled(false);
        }

        List<Employee> emps;
        emps = scheduleForFill.getEmployeeList();
        etEmpLastName = (EditText) view.findViewById(R.id.etSetEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetEmpMiddleName);
        String lastNameBuf = "";
        String firstNameBuf = "";
        String middleNameBuf = "";

        for (Employee e: emps) {
            lastNameBuf += e.getLastName();
            lastNameBuf += ",";

            firstNameBuf += e.getFirstName();
            firstNameBuf += ",";

            middleNameBuf += e.getMiddleName();
            middleNameBuf += ",";
        }
        if (lastNameBuf.length() > 0) {
            lastNameBuf = lastNameBuf.substring(0, lastNameBuf.length() - 1);
            firstNameBuf = firstNameBuf.substring(0, firstNameBuf.length() - 1);
            middleNameBuf = middleNameBuf.substring(0, middleNameBuf.length() - 1);
        }

        etEmpFirstName.setText(firstNameBuf);
        etEmpLastName.setText(lastNameBuf);
        etEmpMiddleName.setText(middleNameBuf);

        if (isForGroup) {
            etEmpFirstName.setEnabled(false);
            etEmpLastName.setEnabled(false);
            etEmpMiddleName.setEnabled(false);
        }

        etNote = (EditText) view.findViewById(R.id.etSetNote);
        etNote.setText(scheduleForFill.getNote());

        etTime = (EditText) view.findViewById(R.id.etSetTime);
        etTime.setText(scheduleForFill.getLessonTime());

        subGroupSpinner = (Spinner) view.findViewById(R.id.setSubGroupSpinner);
        for (int i = 0; i < subGroupSpinner.getAdapter().getCount(); i++) {
            if (subGroupSpinner.getSelectedItem().toString().equals(scheduleForFill.getSubGroup())
                    || "".equals(scheduleForFill.getSubGroup())) {
                break;
            } else {
                subGroupSpinner.setSelection(i);
            }
        }

        etLType = (EditText) view.findViewById(R.id.etSetLessonType);
        etLType.setText(scheduleForFill.getLessonType());

        w1CheckBox = (CheckBox) view.findViewById(R.id.firstWeek);
        if (scheduleForFill.getWeekNumbers().contains("1")) {
            w1CheckBox.setChecked(true);
        }
        w2CheckBox = (CheckBox) view.findViewById(R.id.secondWeek);
        if (scheduleForFill.getWeekNumbers().contains("2")) {
            w2CheckBox.setChecked(true);
        }
        w3CheckBox = (CheckBox) view.findViewById(R.id.thirdWeek);
        if (scheduleForFill.getWeekNumbers().contains("3")) {
            w3CheckBox.setChecked(true);
        }
        w4CheckBox = (CheckBox) view.findViewById(R.id.fourthWeek);
        if (scheduleForFill.getWeekNumbers().contains("4")) {
            w4CheckBox.setChecked(true);
        }

        return view;
    }


    //get schedule params from edit dialog
    private Schedule fillScheduleFromEditDialog(View view) {
        EditText etAud;
        EditText etSubj;
        EditText etEmpLastName;
        EditText etEmpFirstName;
        EditText etEmpMiddleName;
        EditText etNote;
        EditText etTime;
        EditText etLType;
        EditText etGroupName;
        Spinner subGroupSpinner;
        CheckBox w1CheckBox;
        CheckBox w2CheckBox;
        CheckBox w3CheckBox;
        CheckBox w4CheckBox;

        Schedule newSchedule = new Schedule();

        etGroupName = (EditText) view.findViewById(R.id.etSetGroup);
        newSchedule.setStudentGroup(etGroupName.getText().toString());

        etAud = (EditText) view.findViewById(R.id.etSetAud);
        newSchedule.setAuditories(Arrays.asList(etAud.getText().toString().split("\\,")));

        etSubj = (EditText) view.findViewById(R.id.etSetSubj);
        newSchedule.setSubject(etSubj.getText().toString());

        List<Employee> emps = new ArrayList<>();


        etEmpLastName = (EditText) view.findViewById(R.id.etSetEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetEmpMiddleName);

        String[] firstNameBuf;
        String[] lastNameBuf;
        String[] middleNameBuf;
        firstNameBuf = etEmpFirstName.getText().toString().split("\\,");
        lastNameBuf = etEmpLastName.getText().toString().split("\\,");
        middleNameBuf = etEmpMiddleName.getText().toString().split("\\,");

        for (int i = 0; i < firstNameBuf.length; i++) {
            Employee emp = new Employee();
            emp.setFirstName(firstNameBuf[i]);
            emp.setLastName(lastNameBuf[i]);
            emp.setMiddleName(middleNameBuf[i]);
            emps.add(emp);
        }

        newSchedule.setEmployeeList(emps);

        etNote = (EditText) view.findViewById(R.id.etSetNote);
        newSchedule.setNote(etNote.getText().toString());

        etTime = (EditText) view.findViewById(R.id.etSetTime);
        newSchedule.setLessonTime(etTime.getText().toString());

        subGroupSpinner = (Spinner) view.findViewById(R.id.setSubGroupSpinner);
        if ("0".equals(subGroupSpinner.getSelectedItem().toString())) {
            newSchedule.setSubGroup("");
        } else {
            newSchedule.setSubGroup(subGroupSpinner.getSelectedItem().toString());
        }

        etLType = (EditText) view.findViewById(R.id.etSetLessonType);
        newSchedule.setLessonType(etLType.getText().toString());


        List<String> weekNums = new ArrayList<>();
        w1CheckBox = (CheckBox) view.findViewById(R.id.firstWeek);
        if (w1CheckBox.isChecked())
            weekNums.add("1");
        w2CheckBox = (CheckBox) view.findViewById(R.id.secondWeek);
        if (w2CheckBox.isChecked())
            weekNums.add("2");
        w3CheckBox = (CheckBox) view.findViewById(R.id.thirdWeek);
        if (w3CheckBox.isChecked())
            weekNums.add("3");
        w4CheckBox = (CheckBox) view.findViewById(R.id.fourthWeek);
        if (w4CheckBox.isChecked())
            weekNums.add("4");

        newSchedule.setWeekNumbers(weekNums);

        return newSchedule;
    }

    //get schedule params from add dialog
    private Schedule fillScheduleFromAddDialog(View view) {
        EditText etAud;
        EditText etSubj;
        EditText etEmpLastName;
        EditText etEmpFirstName;
        EditText etEmpMiddleName;
        EditText etNote;
        EditText etTime;
        EditText etLType;
        EditText etGroupName;
        Spinner subGroupSpinner;
        CheckBox w1CheckBox;
        CheckBox w2CheckBox;
        CheckBox w3CheckBox;
        CheckBox w4CheckBox;

        Schedule newSchedule = new Schedule();

        etGroupName = (EditText) view.findViewById(R.id.etSetGroup);
        newSchedule.setStudentGroup(etGroupName.getText().toString());

        etAud = (EditText) view.findViewById(R.id.etSetAud);
        newSchedule.setAuditories(Arrays.asList(etAud.getText().toString().split("\\,")));

        etSubj = (EditText) view.findViewById(R.id.etSetSubj);
        newSchedule.setSubject(etSubj.getText().toString());

        List<Employee> emps = new ArrayList<>();


        etEmpLastName = (EditText) view.findViewById(R.id.etSetEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetEmpMiddleName);

        String[] firstNameBuf;
        String[] lastNameBuf;
        String[] middleNameBuf;
        firstNameBuf = etEmpFirstName.getText().toString().split("\\,");
        lastNameBuf = etEmpLastName.getText().toString().split("\\,");
        middleNameBuf = etEmpMiddleName.getText().toString().split("\\,");

        for (int i = 0; i < firstNameBuf.length; i++) {
            Employee emp = new Employee();
            emp.setFirstName(firstNameBuf[i]);
            emp.setLastName(lastNameBuf[i]);
            emp.setMiddleName(middleNameBuf[i]);
            emps.add(emp);
        }

        newSchedule.setEmployeeList(emps);

        etNote = (EditText) view.findViewById(R.id.etSetNote);
        newSchedule.setNote(etNote.getText().toString());

        etTime = (EditText) view.findViewById(R.id.etSetTime);
        newSchedule.setLessonTime(etTime.getText().toString());

        subGroupSpinner = (Spinner) view.findViewById(R.id.setSubGroupSpinner);
        if ("0".equals(subGroupSpinner.getSelectedItem().toString())) {
            newSchedule.setSubGroup("");
        } else {
            newSchedule.setSubGroup(subGroupSpinner.getSelectedItem().toString());
        }

        etLType = (EditText) view.findViewById(R.id.etSetLessonType);
        newSchedule.setLessonType(etLType.getText().toString());


        List<String> weekNums = new ArrayList<>();
        w1CheckBox = (CheckBox) view.findViewById(R.id.firstWeek);
        if (w1CheckBox.isChecked())
            weekNums.add("1");
        w2CheckBox = (CheckBox) view.findViewById(R.id.secondWeek);
        if (w2CheckBox.isChecked())
            weekNums.add("2");
        w3CheckBox = (CheckBox) view.findViewById(R.id.thirdWeek);
        if (w3CheckBox.isChecked())
            weekNums.add("3");
        w4CheckBox = (CheckBox) view.findViewById(R.id.fourthWeek);
        if (w4CheckBox.isChecked())
            weekNums.add("4");

        newSchedule.setWeekNumbers(weekNums);

        return newSchedule;
    }

    private void addScheduleRowAction(View view) {
        Schedule record = fillScheduleFromAddDialog(view);
        record.setWeekDay((long) currentPosition + 1);
        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        String buf = String.valueOf(sdd.getDbHelper().addScheduleToDataBase(record));
        record.setScheduleTableRowId(buf);
        sdd.setAsManual(record.getScheduleTableRowId());

        Integer currDay = currentPosition;
        allScheduleForGroup.get(currDay).getSchedules().add(record);
        allScheduleForGroup.get(currDay).setSchedules(sdd.sortScheduleListByTime(
                allScheduleForGroup.get(currDay).getSchedules()));
        getThisFragment().onResume();
        filterScheduleList(currDay, selectedWeekNumber,
                selectedSubGroup, showHidden);
    }

    private void editScheduleRowAction(Schedule schedule, View view) {
        Schedule record = fillScheduleFromEditDialog(view);
        record.setWeekDay(schedule.getWeekDay());
        record.getEmployeeList().get(0).setPhoto(schedule.getEmployeeList().get(0).getPhoto());
        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        sdd.deleteScheduleTableRow(schedule.getScheduleTableRowId());
        String buf = String.valueOf(sdd.getDbHelper().addScheduleToDataBase(record));
        record.setScheduleTableRowId(buf);
        sdd.setAsManual(record.getScheduleTableRowId());

        Integer currDay = Integer.valueOf(schedule.getWeekDay().toString()) - 1;
        allScheduleForGroup.get(currDay).getSchedules().remove(schedule);
        allScheduleForGroup.get(currDay).getSchedules().add(record);
        allScheduleForGroup.get(currDay).setSchedules(sdd.sortScheduleListByTime(
                allScheduleForGroup.get(currDay).getSchedules()));
        filterScheduleList(currDay, selectedWeekNumber,
                selectedSubGroup, showHidden);
    }

    private void deleteScheduleRowAction(Schedule schedule) {
        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        sdd.deleteScheduleTableRow(schedule.getScheduleTableRowId());

        Integer currDay = Integer.valueOf(schedule.getWeekDay().toString()) - 1;
        allScheduleForGroup.get(currDay).getSchedules().remove(schedule);
        allScheduleForGroup.get(currDay).setSchedules(sdd.sortScheduleListByTime(
                allScheduleForGroup.get(currDay).getSchedules()));
        filterScheduleList(currDay, selectedWeekNumber,
                selectedSubGroup, showHidden);
    }

    private void hideScheduleRowAction(Schedule schedule) {
        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));

        if (schedule.isHidden()) {
            sdd.showScheduleRow(schedule.getScheduleTableRowId());
            schedule.setHidden(false);
        } else {
            sdd.hideScheduleRow(schedule.getScheduleTableRowId());
            schedule.setHidden(true);
        }

        Integer currDay = Integer.valueOf(schedule.getWeekDay().toString()) - 1;
        allScheduleForGroup.get(currDay).getSchedules().remove(schedule);
        allScheduleForGroup.get(currDay).getSchedules().add(schedule);
        allScheduleForGroup.get(currDay).setSchedules(sdd.sortScheduleListByTime(
                allScheduleForGroup.get(currDay).getSchedules()));
        filterScheduleList(currDay, selectedWeekNumber,
                selectedSubGroup, showHidden);
    }

    private Integer executeAction(int which, Schedule schedule, boolean isForEmp, DialogInterface dialog) {
        switch (which) {
            case 0:
                createConfirmDeletingDialog(schedule);
                dialog.dismiss();
                return 0;

            case 1:
                createEditDialog(schedule, isForEmp);
                dialog.dismiss();
                return 0;

            case 2:
                hideScheduleRowAction(schedule);
                dialog.dismiss();
                return 0;

            default:
                return -1;
        }
    }

    /**
     * Метод создает диалоговое окно для добавления новой записи в расписание
     * @param isForEmp - показывет чье это расписание, преподавателя или группы
     * @param s - расписание для которого добавляется запись
     */
    public void createAddDialog(Schedule s, boolean isForEmp) {
        final View view = getEmptyView(s, isForEmp);
        final String title = currentView.getResources().getString(R.string.title_add);
        final String buttonAdd = currentView.getResources().getString(R.string.button_add);
        final String buttonCancel = currentView.getResources().getString(R.string.button_cancel);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(title)
                            .setCancelable(false)
                            .setView(view)
                            .setPositiveButton(buttonAdd, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    addScheduleRowAction(view);
                                }
                            })
                            .setNegativeButton(buttonCancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }
            }
        });
    }

    /**
     * Метод создает диалоговое окно для редактирования записи в расписание
     * @param isForEmp - показывет чье это расписание, преподавателя или группы
     * @param s - расписание для которого добавляется запись
     */
    public void createEditDialog(Schedule s, boolean isForEmp) {
        final Schedule schedule = s;
        final View view = getEditView(s, isForEmp);
        final String title = currentView.getResources().getString(R.string.title_edit);
        final String buttonEdit = currentView.getResources().getString(R.string.button_edit);
        final String buttonCancel = currentView.getResources().getString(R.string.button_cancel);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    new AlertDialog.Builder(getActivity()).setTitle(title).setCancelable(false).setView(view)
                            .setPositiveButton(buttonEdit, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    editScheduleRowAction(schedule, view);
                                }
                            })
                            .setNegativeButton(buttonCancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }
            }
        });
    }

    /**
     * Метод удаляет запись из расписание
     * @param s - расписание для удаления
     */
    public void createConfirmDeletingDialog(Schedule s) {
        final Schedule schedule = s;
        final String title = currentView.getResources().getString(R.string.title_confirm_deleting);
        final String msg = currentView.getResources().getString(R.string.message_confirm_deleting);
        final String buttonYes = currentView.getResources().getString(R.string.button_yes);
        final String buttonNo = currentView.getResources().getString(R.string.button_no);
        TextView tv = new TextView(getActivity());
        tv.setTextSize(18);
        tv.setText(msg);
        final View view = tv;


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    new AlertDialog.Builder(getActivity()).setTitle(title).setCancelable(false).setView(view)
                            .setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteScheduleRowAction(schedule);
                                }
                            })
                            .setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }
            }
        });
    }

    /**
     * Метод создает диалоговое окно для выбора действия над расписанием
     * @param isForEmp - показывет чье это расписание, преподавателя или группы
     * @param s - расписание для которого создается диалоговое окно
     */
    public void createChooseActionDialog(Schedule s, final boolean isForEmp) {
        final Schedule schedule = s;

        final String[] actionsIfHidden = currentView.getResources().getStringArray(R.array.choose_action_if_hidden);
        final String[] actionsIfNotHidden = currentView.getResources().getStringArray(R.array.choose_action_if_not_hidden);
        final String[] items;
        if (!schedule.isHidden()) {
            items = actionsIfHidden;
        } else {
            items = actionsIfNotHidden;
        }

        final String title = currentView.getResources().getString(R.string.title_choose_action);
        final String buttonCancel = currentView.getResources().getString(R.string.button_cancel);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    new AlertDialog.Builder(getActivity()).setTitle(title).setCancelable(false)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (executeAction(which, schedule, isForEmp, dialog) < 0) {
                                        Log.d("Action Dialog", "Can't find handler for this action.");
                                    } else
                                        Log.d("Action Dialog", "Action executed.");
                                }
                            })
                            .setNegativeButton(buttonCancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
            }
        });
    }

}

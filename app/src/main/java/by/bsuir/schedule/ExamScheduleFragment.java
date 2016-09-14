package by.bsuir.schedule;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.R;

import by.bsuir.schedule.adapters.ArrayAdapterEmployeeSchedule;
import by.bsuir.schedule.adapters.ArrayAdapterGroupSchedule;
import by.bsuir.schedule.dao.DBHelper;
import by.bsuir.schedule.dao.SchoolDayDao;
import by.bsuir.schedule.model.Employee;
import by.bsuir.schedule.model.Schedule;
import by.bsuir.schedule.model.SchoolDay;
import by.bsuir.schedule.utils.FileUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Фрагмент для скачивания расписания экзаменов
 */
public class ExamScheduleFragment extends Fragment {
    private static final String ARG_ALL_SCHEDULE = "examAllSchedule";
    private static final String ARG_SELECTED_POSITION = "examSelectedPosition";
    private static final String ARG_SHOW_HIDDEN_SCHEDULE = "showHidden";
    private View currentView;
    private List<SchoolDay> allSchedules;
    private Schedule[] schedulesForShow;
    private Integer currentSelectedPosition;
    private boolean showHidden;

    private static boolean isRefreshed = true;

    private ArrayAdapterGroupSchedule groupAdapter;
    private ArrayAdapterEmployeeSchedule empAdapter;

    /**
     * Фрагмент для скачивания расписания экзаменов
     */
    public ExamScheduleFragment() {
        // Required empty public constructor
    }

    /**
     * Статический метод для создания экземпляра данного фрагмента.
     * Параметры сетятся в экземпляр фрагмента через метод "setArguments", это позволит фрагменту
     * восстановить параметры после пересоздания
     * @param allSchedules список всех экзаменов
     * @param position номер текущего дня
     * @return созданный фрагмент
     */
    public static ExamScheduleFragment newInstance(List<SchoolDay> allSchedules, int position, boolean showHidden) {
        ExamScheduleFragment fragment = new ExamScheduleFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALL_SCHEDULE, (Serializable) allSchedules);
        args.putInt(ARG_SELECTED_POSITION, position);
        args.putBoolean(ARG_SHOW_HIDDEN_SCHEDULE, showHidden);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Метод вызывется при создании фрагмента
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null){
            List<SchoolDay> schedules = (List<SchoolDay>) args.getSerializable(ARG_ALL_SCHEDULE);
            setAllSchedules(schedules);
            setCurrentSelectedPosition(args.getInt(ARG_SELECTED_POSITION));
            setShowHidden(args.getBoolean(ARG_SHOW_HIDDEN_SCHEDULE));
        }
    }

    /**
     * Вызывается для того чтобы фрагмент создал свое представление
     * @param inflater Объект служащий для создания view
     * @param container Ссылка указывающая на родительское view
     * @param savedInstanceState Если фрагмент пересоздается, то здесь будут хранится сохраненные
     *                           значения
     * @return Возвращает созданное view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_exam_schedule, container, false);
        updateSchedule(currentSelectedPosition, showHidden);
        return currentView;
    }

    /**
     * Обновляет ListView со списком занятий
     */
    public void updateListView(){
        if(currentView != null && getActivity() != null) {
            ListView mainListView = (ListView) currentView.findViewById(R.id.showExamScheduleView);
            groupAdapter = new ArrayAdapterGroupSchedule(getActivity(),
                    R.layout.schedule_fragment_item_layout, schedulesForShow);
            empAdapter = new ArrayAdapterEmployeeSchedule(getActivity(),
                    R.layout.schedule_fragment_item_layout, schedulesForShow);

            if (FileUtil.isDefaultStudentGroup(getActivity())) {
                mainListView.setAdapter(groupAdapter);
                mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Schedule s = groupAdapter.getSchedule(position);
                        Log.d("List View", s.getLessonTime() + " " + s.getLessonType() + " " + s.getSubGroup()
                                + " " + s.getSubject() + " " + s.getSubGroup() + " " + s.getStudentGroup());
                        createChooseActionDialog(s, false);
                    }
                });
                final TextView tvAddSchedule = (TextView) currentView.findViewById(R.id.addNewExamSchedule);
                final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.onclick_anim);
                tvAddSchedule.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvAddSchedule.startAnimation(anim);
                        Schedule s = allSchedules.get(currentSelectedPosition).getSchedules().get(0);
                        createAddDialog(s, false);
                    }
                });
            } else {
                mainListView.setAdapter(empAdapter);
                mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Schedule s = empAdapter.getSchedule(position);
                        Log.d("List View", s.getLessonTime() + " " + s.getLessonType() + " " + s.getSubGroup()
                                + " " + s.getSubject() + " " + s.getSubGroup() + " " + s.getStudentGroup());
                        createChooseActionDialog(s, true);
                    }
                });
                final TextView tvAddSchedule = (TextView) currentView.findViewById(R.id.addNewExamSchedule);
                final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.onclick_anim);
                tvAddSchedule.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvAddSchedule.startAnimation(anim);
                        Schedule s = empAdapter.getSchedule(0);
                        createAddDialog(s, true);
                    }
                });
            }

            TextView emptyTextView = (TextView) currentView.findViewById(R.id.emptyExamList);
            mainListView.setEmptyView(emptyTextView);

        }
    }

    /**
     * Метод вызывается после выбора пользователем дня для показа расписания. В методе достается
     * список занятий для введенного дня, и обновление ListView с отображаемым списком занятий
     * @param position номер дня, для которого нужно показать расписание
     */
    public void updateSchedule(int position, boolean showHidden){

        List<Schedule> scheduleList = new ArrayList<>();
        if (getAllSchedules().size() > position)
            for (Schedule schedule : getAllSchedules().get(position).getSchedules()) {
                if (showHidden) {
                    scheduleList.add(schedule);
                } else if (!schedule.isHidden()) {
                    scheduleList.add(schedule);
                }
            }
        else for (Schedule schedule : getAllSchedules().get(0).getSchedules()) {
            if (showHidden) {
                scheduleList.add(schedule);
            } else if (!schedule.isHidden()) {
                scheduleList.add(schedule);
            }

        }

        Schedule[] schedules = scheduleList.toArray(new Schedule[scheduleList.size()]);
        setSchedulesForShow(schedules);
        updateListView();

    }

    /**
     * Получает все расписание
     */
    public List<SchoolDay> getAllSchedules() {
        return allSchedules;
    }

    /**
     * Устанавливает все расписание
     */
    public void setAllSchedules(List<SchoolDay> allSchedules) {
        this.allSchedules = allSchedules;
    }

    /**
     * Получает текущий фрагмент
     */
    public ExamScheduleFragment getThisFragment() {
        return this;
    }

    /**
     * Устанавливает флаг показа скрытых записей
     */
    public void setShowHidden(boolean show) {
        this.showHidden = show;
    }

    /**
     * Устанвливает расписания которые буду показаны на текущей странице
     */
    public void setSchedulesForShow(Schedule[] schedulesForShow) {
        this.schedulesForShow = schedulesForShow;
    }

    /**
     * Устанавливает текущую страницу
     */
    public void setCurrentSelectedPosition(Integer currentSelectedPosition) {
        this.currentSelectedPosition = currentSelectedPosition;
    }

    //create view for action dialog
    private View getEmptyView(Schedule scheduleForFill, boolean isForGroup) {
        LayoutInflater ltInflater = getActivity().getLayoutInflater();
        View view = ltInflater.inflate(R.layout.edit_exam_dialog, null, false);

        EditText etEmpLastName;
        EditText etEmpFirstName;
        EditText etEmpMiddleName;
        EditText etGroup;

        etGroup = (EditText) view.findViewById(R.id.etSetExamGroup);
        etGroup.setText(scheduleForFill.getStudentGroup());
        if (!isForGroup) {
            etGroup.setEnabled(false);
        }

        List<Employee> emps;
        emps = scheduleForFill.getEmployeeList();
        etEmpLastName = (EditText) view.findViewById(R.id.etSetExamEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetExamEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetExamEmpMiddleName);
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
        View view = ltInflater.inflate(R.layout.edit_exam_dialog, null, false);

        EditText etAud;
        EditText etSubj;
        EditText etEmpLastName;
        EditText etEmpFirstName;
        EditText etDate;
        EditText etEmpMiddleName;
        EditText etNote;
        EditText etTime;
        EditText etLType;
        EditText etGroup;

        etDate = (EditText) view.findViewById(R.id.etSetExamDate);
        etDate.setText(scheduleForFill.getDate());
        etDate.setEnabled(false);

        etAud = (EditText) view.findViewById(R.id.etSetExamAud);
        String[] auds = scheduleForFill.getAuditories().toArray(new String[scheduleForFill.getAuditories().size()]);
        String buf = "";
        for (String str: auds) {
            buf += str;
        }
        etAud.setText(buf);

        etSubj = (EditText) view.findViewById(R.id.etSetExamSubj);
        etSubj.setText(scheduleForFill.getSubject());

        etGroup = (EditText) view.findViewById(R.id.etSetExamGroup);
        etGroup.setText(scheduleForFill.getStudentGroup());
        if (!isForGroup) {
            etGroup.setEnabled(false);
        }

        List<Employee> emps;
        emps = scheduleForFill.getEmployeeList();
        etEmpLastName = (EditText) view.findViewById(R.id.etSetExamEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetExamEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetExamEmpMiddleName);
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

        etNote = (EditText) view.findViewById(R.id.etSetExamNote);
        etNote.setText(scheduleForFill.getNote());

        etTime = (EditText) view.findViewById(R.id.etSetExamTime);
        etTime.setText(scheduleForFill.getLessonTime());


        etLType = (EditText) view.findViewById(R.id.etSetExamLessonType);
        etLType.setText(scheduleForFill.getLessonType());

        return view;
    }

    //get schedule params from edit dialog
    private Schedule fillScheduleFromEditDialog(View view) {
        EditText etAud;
        EditText etSubj;
        EditText etEmpLastName;
        EditText etEmpFirstName;
        EditText etDate;
        EditText etEmpMiddleName;
        EditText etNote;
        EditText etTime;
        EditText etLType;

        Schedule newSchedule = new Schedule();

        etDate = (EditText) view.findViewById(R.id.etSetExamDate);
        newSchedule.setDate(etDate.getText().toString());

        etAud = (EditText) view.findViewById(R.id.etSetExamAud);
        newSchedule.setAuditories(Arrays.asList(etAud.getText().toString().split("\\,")));

        etSubj = (EditText) view.findViewById(R.id.etSetExamSubj);
        newSchedule.setSubject(etSubj.getText().toString());

        List<Employee> emps = new ArrayList<>();

        etEmpLastName = (EditText) view.findViewById(R.id.etSetExamEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetExamEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetExamEmpMiddleName);

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

        etNote = (EditText) view.findViewById(R.id.etSetExamNote);
        newSchedule.setNote(etNote.getText().toString());

        etTime = (EditText) view.findViewById(R.id.etSetExamTime);
        newSchedule.setLessonTime(etTime.getText().toString());

        etLType = (EditText) view.findViewById(R.id.etSetExamLessonType);
        newSchedule.setLessonType(etLType.getText().toString());

        return newSchedule;
    }

    //get schedule params from add dialog
    private Schedule fillScheduleFromAddDialog(View view) {
        EditText etAud;
        EditText etSubj;
        EditText etEmpLastName;
        EditText etEmpFirstName;
        EditText etDate;
        EditText etEmpMiddleName;
        EditText etNote;
        EditText etTime;
        EditText etLType;
        EditText etGroupName;

        Schedule newSchedule = new Schedule();

        etDate = (EditText) view.findViewById(R.id.etSetExamDate);
        newSchedule.setDate(etDate.getText().toString());

        etGroupName = (EditText) view.findViewById(R.id.etSetExamGroup);
        newSchedule.setStudentGroup(etGroupName.getText().toString());

        etAud = (EditText) view.findViewById(R.id.etSetExamAud);
        newSchedule.setAuditories(Arrays.asList(etAud.getText().toString().split("\\,")));

        etSubj = (EditText) view.findViewById(R.id.etSetExamSubj);
        newSchedule.setSubject(etSubj.getText().toString());


        List<Employee> emps = new ArrayList<>();

        etEmpLastName = (EditText) view.findViewById(R.id.etSetExamEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetExamEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetExamEmpMiddleName);

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

        etNote = (EditText) view.findViewById(R.id.etSetExamNote);
        newSchedule.setNote(etNote.getText().toString());

        etTime = (EditText) view.findViewById(R.id.etSetExamTime);
        newSchedule.setLessonTime(etTime.getText().toString());

        etLType = (EditText) view.findViewById(R.id.etSetExamLessonType);
        newSchedule.setLessonType(etLType.getText().toString());


        return newSchedule;
    }

    private void addScheduleRowAction(View view) {
        boolean isDateExist = false;
        Schedule record = fillScheduleFromAddDialog(view);
        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        String buf = String.valueOf(sdd.getDbHelper().addScheduleToDataBase(record));
        record.setScheduleTableRowId(buf);
        sdd.setAsManual(record.getScheduleTableRowId());

        for (SchoolDay sd : allSchedules) {
            if (sd.getDayName().equals(record.getDate())) {
                sd.getSchedules().add(record);
                isDateExist = true;
                break;
            }
        }
        if (!isDateExist) {
            SchoolDay newSd = new SchoolDay();
            newSd.setDayName(record.getDate());
            List<Schedule> newSl = new ArrayList<>();
            newSl.add(record);
            newSd.setSchedules(newSl);
            isRefreshed = false;
            allSchedules.add(newSd);
            Intent intent = new Intent(getActivity(), MainActivity.class);
            getThisFragment().startActivity(intent);
        }

        updateListView();
        updateSchedule(currentSelectedPosition, showHidden);
    }

    private void editScheduleRowAction(Schedule schedule, View view) {
        Schedule record = fillScheduleFromEditDialog(view);
        record.setStudentGroup(schedule.getStudentGroup());

        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        sdd.deleteScheduleTableRow(schedule.getScheduleTableRowId());

        String buf = String.valueOf(sdd.getDbHelper().addScheduleToDataBase(record));
        record.setScheduleTableRowId(buf);
        sdd.setAsManual(record.getScheduleTableRowId());


        allSchedules.get(currentSelectedPosition).getSchedules().remove(schedule);
        allSchedules.get(currentSelectedPosition).getSchedules().add(record);
        allSchedules.get(currentSelectedPosition).setSchedules(sdd.sortScheduleListByTime(
                allSchedules.get(currentSelectedPosition).getSchedules()));
        updateListView();
        updateSchedule(currentSelectedPosition, showHidden);
    }

    private void deleteScheduleRowAction(Schedule schedule) {
        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        sdd.deleteScheduleTableRow(schedule.getScheduleTableRowId());

        allSchedules.get(currentSelectedPosition).getSchedules().remove(schedule);
        allSchedules.get(currentSelectedPosition).setSchedules(sdd.sortScheduleListByTime(
                allSchedules.get(currentSelectedPosition).getSchedules()));
        updateListView();
        updateSchedule(currentSelectedPosition, showHidden);
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

        allSchedules.get(currentSelectedPosition).getSchedules().remove(schedule);
        allSchedules.get(currentSelectedPosition).getSchedules().add(schedule);
        allSchedules.get(currentSelectedPosition).setSchedules(sdd.sortScheduleListByTime(
                allSchedules.get(currentSelectedPosition).getSchedules()));
        updateListView();
        updateSchedule(currentSelectedPosition, showHidden);
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

            default: return -1;
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
                    new AlertDialog.Builder(getActivity()).setTitle(title).setCancelable(false).setView(view)
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

    /**
     * Метод показывает нужно ли обновлять количество данных для адаптера
     * @return нужно - false, не нужно - true
     */
    public static boolean refreshed() {
        return isRefreshed;
    }

    /**
     * Метод устанавливает нужно ли обновлять количество данных для адаптера
     */
    public static void setRefreshed(boolean value) {
        isRefreshed = value;
    }

}

package by.bsuir.schedule;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import by.bsuir.schedule.dao.DBHelper;
import by.bsuir.schedule.dao.SchoolDayDao;
import by.bsuir.schedule.dataprovider.LoadSchedule;
import by.bsuir.schedule.model.AvailableFragments;
import by.bsuir.schedule.model.StudentGroup;
import by.bsuir.schedule.utils.DateUtil;
import by.bsuir.schedule.utils.WidgetUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Фрагмент для скачивания расписания студенченской группы
 */
public class DownloadScheduleForGroup extends Fragment {
    private static final String TAG = "downScheForGroupTAG";
    private static final Integer COUNT_DIGITS_IN_STUDENT_GROUP = 6;
    private static final Integer XML_EXTENSION_LENGTH = 4;

    private OnFragmentInteractionListener parentActivity;
    private List<String> downloadedSchedulesForGroup;
    private boolean isDownloadingNewSchedule;
    private View currentView;
    private TableLayout tableLayoutForDownloadedSchedules;
    private List<StudentGroup> availableStudentGroups;
    ProgressDialog mProgressDialog;

    /**
     * Фрагмент для скачивания расписания студенченской группы
     */
    public DownloadScheduleForGroup() {
        // Required empty public constructor
    }

    /**
     * Метод вызывается для того чтобы fragment создал view которую будет показано пользователю.
     * @param inflater объект служащий для создания view
     * @param container ссылка на view, к которому фрагмент будет присоединен
     * @param savedInstanceState Если фрагмент был пересоздан, то в savedInstanceState будут
     *                           хранится его сохраненные параметры
     * @return Возвращает View которое будет показано пользователю
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_download_schedule_for_group, container, false);


        ConnectivityManager connectMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadStudentGroupXML().execute();
        } else {
            Toast.makeText(getActivity(), R.string.can_not_load_list_of_student_groups, Toast.LENGTH_LONG).show();
        }

        Button button = (Button) currentView.findViewById(R.id.buttonForDownloadSchedule);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsDownloadingNewSchedule(true);
                AutoCompleteTextView editText = (AutoCompleteTextView) currentView.findViewById(R.id.editTextForEnterGroup);
                String studentGroup = editText.getText().toString();
                StudentGroup selectedStudentGroup = isAppropriateStudentGroup(studentGroup);
                if (selectedStudentGroup == null) {
                    Toast toast = Toast.makeText(getActivity(), R.string.not_schedule_for_your_group, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 30);
                    toast.show();
                } else {
                    downloadOrUpdateSchedule(selectedStudentGroup);
                }
            }
        });

        TableLayout tableLayout = (TableLayout) currentView.findViewById(R.id.tableLayoutForGroup);
        setTableLayoutForDownloadedSchedules(tableLayout);
        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        setDownloadedSchedulesForGroup(sdd.getAvailableGroups());
        populateTableLayout(tableLayout, getDownloadedSchedulesForGroup());

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.downloading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        return currentView;
    }

    /**
     * Метод для скачивания или обновления расписания для группы
     * @param sg группа для которой необходимо скачать или обновить расписание. Представляется
     *           объектом StudentGroup
     */
    private void downloadOrUpdateSchedule(StudentGroup sg){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            //убираем виртуальную клавиатуру перед началом загрузки
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (getActivity().getCurrentFocus() != null && imm != null)
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            DownloadStudentGroupScheduleTask downloadTask = new DownloadStudentGroupScheduleTask(getActivity());
            downloadTask.fileDir = getActivity().getFilesDir();
            downloadTask.execute(sg);
            updateDefaultGroup(sg.getStudentGroupName(), true);
        } else{
            Toast.makeText(getActivity(), getResources().getString(R.string.no_connection_to_network), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Метод наполняет таблицу списком групп для которых скачано расписание
     * @param tableLayout таблица которую нужно заполнить
     * @param schedulesForGroup список групп, расписание для которых было ранее скачано
     */
    public void populateTableLayout(final TableLayout tableLayout, final List<String> schedulesForGroup){
        Integer currentRowNumber = 0;
        tableLayout.removeAllViews();
        tableLayout.setPadding(5, 0, 5, 0);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TableRow headerRow = new TableRow(getActivity());
        TextView groupHeaderTextView = new TextView(getActivity());
        groupHeaderTextView.setText(getResources().getString(R.string.group_name));
        groupHeaderTextView.setGravity(Gravity.START);
        groupHeaderTextView.setLayoutParams(params);
        groupHeaderTextView.setTypeface(null, Typeface.BOLD);
        headerRow.addView(groupHeaderTextView);

        TextView lastUpdateHeader = new TextView(getActivity());
        lastUpdateHeader.setText(getResources().getString(R.string.last_updated));
        lastUpdateHeader.setTypeface(null, Typeface.BOLD);
        lastUpdateHeader.setPadding(5, 0, 5, 0);
        headerRow.addView(lastUpdateHeader);

        TextView deleteHeader = new TextView(getActivity());
        deleteHeader.setText(getResources().getString(R.string.delete));
        deleteHeader.setTypeface(null, Typeface.BOLD);
        deleteHeader.setGravity(Gravity.CENTER);
        deleteHeader.setPadding(5, 0, 5, 0);
        headerRow.addView(deleteHeader);

        TextView refreshHeader = new TextView(getActivity());
        refreshHeader.setText(getResources().getString(R.string.refresh));
        refreshHeader.setTypeface(null, Typeface.BOLD);
        refreshHeader.setGravity(Gravity.CENTER);
        refreshHeader.setPadding(5,0,5,0);
        headerRow.addView(refreshHeader);

        headerRow.setPadding(0,0,0,18);
        tableLayout.addView(headerRow);

        for(String currentGroupSchedule : schedulesForGroup) {
            TableRow rowForGroupSchedule = new TableRow(getActivity());
            TextView textViewForGroupName = new TextView(getActivity());
            textViewForGroupName.setGravity(Gravity.START);
            //currentGroupSchedule contains group name, group id and "exam" at end if it exam schedule
            //group name always contains six symbols.
            textViewForGroupName.setText(currentGroupSchedule.substring(0, COUNT_DIGITS_IN_STUDENT_GROUP));
            textViewForGroupName.setLayoutParams(params);
            rowForGroupSchedule.addView(textViewForGroupName);

            TextView lastUpdatedTextView = new TextView(getActivity());
            lastUpdatedTextView.setText(getLastUpdateFromPreference(currentGroupSchedule));
            lastUpdatedTextView.setPadding(5, 0, 5, 0);
            rowForGroupSchedule.addView(lastUpdatedTextView);

            ImageView deleteImageView = new ImageView(getActivity());
            deleteImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_remove));
            deleteImageView.setOnClickListener(new ViewClickListener(tableLayout, schedulesForGroup));
            rowForGroupSchedule.addView(deleteImageView);

            ImageView refreshImageView = new ImageView(getActivity());
            refreshImageView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_refresh));
            refreshImageView.setOnClickListener(new RefreshButtonListener(schedulesForGroup));
            rowForGroupSchedule.addView(refreshImageView);

            rowForGroupSchedule.setOnClickListener(new GroupNameClickListener(schedulesForGroup));

            rowForGroupSchedule.setTag(currentRowNumber);
            currentRowNumber++;
            tableLayout.addView(rowForGroupSchedule);
        }
    }


    private class RefreshButtonListener implements View.OnClickListener {
        List<String> schedulesForGroup;

        /**
         * Конструктор класса которы обрабатывает нажатие на кнопку обновления расписания
         * @param schedules расписание
         */
        public RefreshButtonListener(List<String> schedules) {
            this.schedulesForGroup = schedules;
        }

        /**
         * Обрабтчик на нажатию кнопку обновления расписания
         * @param v Ссылка на нажатую кнопку
         */
        @Override
        public void onClick(View v) {

            TableRow selectedRow = (TableRow) v.getParent();
            Integer rowNumber = (Integer) selectedRow.getTag();
            String fileNameForRefresh = schedulesForGroup.get(rowNumber);
            SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(null));
            sdd.deleteSchedule(fileNameForRefresh, true);

            setIsDownloadingNewSchedule(false);

            UpdateScheduleForGroup updateTask = new UpdateScheduleForGroup(getActivity());
            updateTask.fileDir = getActivity().getFilesDir();
            updateTask.execute(fileNameForRefresh);
            updateDefaultGroup(fileNameForRefresh, true);

        }
    }

    private class GroupNameClickListener implements View.OnClickListener {
        List<String> schedulesForGroup;

        /**
         * Конструктор класса которы обрабатывает нажатие на номер группы
         * @param schedules расписание
         */
        public GroupNameClickListener(List<String> schedules) {
            this.schedulesForGroup = schedules;
        }

        /**
         * Обработчик нажатий на row в таблице со списком групп, расписание для которых было
         * ранее скачано. При нажатии делаем выбранное расписание дефолтным, и открываем
         * расписание занятий для выбранной группы
         * @param v Ссылка на выбранный ряд
         */
        @Override
        public void onClick(View v) {
            TableRow selectedTableRow = (TableRow) v;
            Integer rowNumber = (Integer) selectedTableRow.getTag();
            String selectedGroup = schedulesForGroup.get(rowNumber);
            updateDefaultGroup(selectedGroup, false);
            parentActivity.onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
        }
    }



    /**
     * Метод обновляет дефолтное расписание переданной группой.
     * @param passedStudentGroup Группа которую нужно сделать дефолтной
     * @param isDownloadedSchedule Переменная указывает расписание было скачано или же просто
     *                             выбрано из списка уже скачанных
     */
    private void updateDefaultGroup(String passedStudentGroup, boolean isDownloadedSchedule){
        String studentGroup = passedStudentGroup;
        if(".xml".equalsIgnoreCase(studentGroup.substring(studentGroup.length() - XML_EXTENSION_LENGTH))){
            studentGroup = studentGroup.substring(0, studentGroup.length() - XML_EXTENSION_LENGTH);
        }
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = this.getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        String employeeFieldInSettings = getActivity().getString(R.string.default_employee_field_in_settings);
        String groupFieldInSettings = getActivity().getString(R.string.default_group_field_in_settings);
        editor.putString(groupFieldInSettings, studentGroup);
        editor.putString(employeeFieldInSettings, "none");
        if(isDownloadedSchedule) {
            Log.d("Current date", DateUtil.getCurrentDateAsString());
            editor.putString(studentGroup, DateUtil.getCurrentDateAsString());
        }
        editor.apply();
        WidgetUtil.updateWidgets(getActivity());
    }



    /**
     * Возвращает дату обновления расписания для переданной группы
     * @param passedSchedulesName Группа для которой нужно вернуть дату последнего обновления
     * @return Возвращает дату последнего обновления
     */
    private String getLastUpdateFromPreference(String passedSchedulesName){
        String schedulesName = passedSchedulesName.substring(0, passedSchedulesName.length() - XML_EXTENSION_LENGTH);
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        return preferences.getString(schedulesName, "-");
    }

    /**
     * Проверяет введенную группу на валидность
     * @param studentGroup Введенная пользователем группа
     * @return Возвращает группу, если валидация прошла успешно, иначе возвращается null
     */
    private StudentGroup isAppropriateStudentGroup(String studentGroup){
        Integer countDigits = studentGroup.length();
        if (!studentGroup.isEmpty() && countDigits.equals(COUNT_DIGITS_IN_STUDENT_GROUP)) {
            for (StudentGroup sg : getAvailableStudentGroups()) {
                if (studentGroup.equalsIgnoreCase(sg.getStudentGroupName())) {
                    return sg;
                }
            }
        }
        return null;
    }

    /**
     * Метод вызывается когда фрагмент присоединяется к активити
     * @param activity activity к которой присоединяется фрагмент
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            parentActivity = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            Log.v(TAG, e.getMessage(), e);
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Метод вызывается при отсоединении фрагмента от активити.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        parentActivity = null;
    }

    private class UpdateScheduleForGroup extends AsyncTask<String, Integer, String> {
        private File fileDir;
        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String groupName;

        /**
         * Конструктор класса который обновляет расписание
         * @param context - контекст текущего активити
         */
        public UpdateScheduleForGroup(Context context) {
            this.context = context;
        }

        /**
         * Метод выполняет загрузку расписания в отдельном потоке
         */
        @Override
        protected String doInBackground(String... parameters) {

            groupName = parameters[0];
            List<StudentGroup> loadedStudentGroups = LoadSchedule.loadAvailableStudentGroups();
            for (StudentGroup sg : loadedStudentGroups) {
                if (sg.getStudentGroupName().equals(parameters[0])) {
                    return LoadSchedule.loadScheduleForStudentGroupById(sg, fileDir);
                }
            }

            return "not found";
        }

        /**
         * Метод вызывается перед началом загрузки
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        /**
         * Метод обновляет процент уже скачанного расписания
         * @param progress Прогресс скачивания расписания
         */
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgress(progress[0]);
        }

        /**
         * Метод вызывается после завершения скачивания. Метод открывает фрагмент для просмотра
         * расписания, или показывает сообщение об ошибке, если расписание не было скачано.
         * @param result Результат скачивания
         */
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if(result != null) {
                if ("not found".equals(result)) {
                    Toast.makeText(getActivity(), "Schedule not found!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), getString(R.string.error_while_downloading_schedule), Toast.LENGTH_LONG).show();
                }
            } else {
                if (isDownloadingNewSchedule()) {
                    parentActivity.onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
                } else {
                    Toast.makeText(getActivity(), "Расписание для группы " + groupName +" обновлено.", Toast.LENGTH_SHORT).show();
                    populateTableLayout(getTableLayoutForDownloadedSchedules(), getDownloadedSchedulesForGroup());
                }

            }
        }

    }

    /**
     * Асинхронный таск для загрузки всех студенченских групп у которых есть расписание
     */
    private class DownloadStudentGroupXML extends AsyncTask<Void, Void, String> {
        static final String ERROR = "Some ERROR while downloading";

        /**
         * Конвертит лист студенченских групп в массив групп
         * @param studentGroups Лист групп которые нужно сконвертировать
         * @return возвращает массив групп
         */
        private String[] convertEmployeeToArray(List<StudentGroup> studentGroups){
            List<String> resultList = new ArrayList<>();
            for(StudentGroup sg : studentGroups){
                resultList.add(sg.getStudentGroupName());
            }
            String[] resultArray = new String[resultList.size()];
            resultArray = resultList.toArray(resultArray);
            return resultArray;
        }

        /**
         * Метод в фоне скачивает список всех студенченских расписаний у которых есть расписание
         * @param parameters null
         * @return Возвращает null если скачивание прошло успешно, иначе возвращает сообщение
         * об ошибке
         */
        @Override
        protected String doInBackground(Void... parameters) {

            List<StudentGroup> loadedStudentGroups = LoadSchedule.loadAvailableStudentGroups();
            if (loadedStudentGroups == null)
                return ERROR;
            setAvailableStudentGroups(loadedStudentGroups);
            return null;
        }


        /**
         * Метод вызывается после скачивания всех доступных групп. В методе конфигурируется
         * AutoCompleteTextView, для того чтобы пользователю показывались доступные группы
         * @param result Результат скачивания
         */
        @Override
        protected void onPostExecute(String result) {

            if (result == null) {
                try {
                    String[] employeesAsArray = convertEmployeeToArray(getAvailableStudentGroups());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, employeesAsArray);
                    AutoCompleteTextView textView = (AutoCompleteTextView) currentView.findViewById(R.id.editTextForEnterGroup);
                    textView.setAdapter(adapter);
                } catch (NullPointerException ex) {
                    Log.d(TAG, "error while downloading available groups", ex);
                }
            }

        }
    }

    private class DownloadStudentGroupScheduleTask extends AsyncTask<StudentGroup, Integer, String> {
        private File fileDir;
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        /**
         * Асинхронный таск для скачивания расписания для введенной пользователем группы
         * @param context контекст
         */
        public DownloadStudentGroupScheduleTask(Context context) {
            this.context = context;
        }

        /**
         * Метод в фоне скачивает расписание для введенной пользователем группы
         * @param urls Группа для которой необходимо скачать расписание
         * @return Возвращает null если скачивание прошло успешно, иначе возвращает сообщение
         * об ошибке
         */
        protected String doInBackground(StudentGroup... urls) {
            return LoadSchedule.loadScheduleForStudentGroupById(urls[0], fileDir);
        }

        /**
         * Метод вызывается перед началом скачивания. Здесь создается прогресс бар отображающий
         * процесс скачивания расписания
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        /**
         * Метод обновляет процент уже скачанного расписания
         *
         * @param progress Прогресс скачивания расписания
         */
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgress(progress[0]);
        }

        /**
         * Метод вызывается после завершения скачивания. Метод открывает фрагмент для просмотра
         * расписания, или показывает сообщение об ошибке, если расписание не было скачано.
         * @param result Результат скачивания
         */
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if(result != null) {
                Toast.makeText(getActivity(), getString(R.string.error_while_downloading_schedule), Toast.LENGTH_LONG).show();
            } else {
                if(isDownloadingNewSchedule()) {
                    parentActivity.onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
                } else {
                    populateTableLayout(getTableLayoutForDownloadedSchedules(), getDownloadedSchedulesForGroup());
                }
            }
        }

    }


    /**
     * Метод для получения списка групп для которых было скачано расписание
     * @return Возвращает список групп
     */
    public List<String> getDownloadedSchedulesForGroup() {
        return downloadedSchedulesForGroup;
    }

    /**
     * Устанавливает список групп для которых было скачано расписание
     * @param downloadedSchedulesForGroup Список групп для которых скачано расписание
     */
    public void setDownloadedSchedulesForGroup(List<String> downloadedSchedulesForGroup) {
        this.downloadedSchedulesForGroup = downloadedSchedulesForGroup;
    }

    /**
     * Метод для получения ссылки на таблицу со списком групп для которых было скачано расписание
     * @return Возвращает ссылку на таблицу
     */
    public TableLayout getTableLayoutForDownloadedSchedules() {
        return tableLayoutForDownloadedSchedules;
    }

    /**
     * Устанавливает ссылку на таблицу со списком групп для которых было скачано расписание
     * @param tableLayoutForDownloadedSchedules Ссылка на таблицу со списком групп
     */
    public void setTableLayoutForDownloadedSchedules(TableLayout tableLayoutForDownloadedSchedules) {
        this.tableLayoutForDownloadedSchedules = tableLayoutForDownloadedSchedules;
    }

    /**
     * Метод возвращает переменную указывающую скачиваем мы новое расписание, или же обновляем
     * ранее скачанное
     * @return Возвращает true, если скачиваем новое, а если обновляем ранее скачанное то вернет false
     */
    public boolean isDownloadingNewSchedule() {
        return isDownloadingNewSchedule;
    }

    /**
     * Устанавливает флаг, скачиваемый мы новое расписание или обновляем ранее скачанное
     * @param isDownloadingNewSchedule флаг определяющий скачиваем новое или обновляем ранее скачанное расписание
     */
    public void setIsDownloadingNewSchedule(boolean isDownloadingNewSchedule) {
        this.isDownloadingNewSchedule = isDownloadingNewSchedule;
    }

    /**
     * Метод возвращает список всех групп у которых есть расписание
     * @return Возвращает список групп
     */
    public List<StudentGroup> getAvailableStudentGroups() {
        return availableStudentGroups;
    }

    /**
     * Устанавливает список групп у которых есть расписание
     * @param availableStudentGroups список групп у которых есть расписание
     */
    public void setAvailableStudentGroups(List<StudentGroup> availableStudentGroups) {
        this.availableStudentGroups = availableStudentGroups;
    }

    private class ViewClickListener implements View.OnClickListener {
        private TableLayout tableLayout;
        private List<String> schedulesForGroup;

        /**
         * Обработчик нажатий на кнопку
         * @param passedTableLayout таблицу которую нужно обновить после удаления расписания
         * @param passedSchedulesForGroup список групп расписание для которых уже было скачано
         */
        public ViewClickListener(final TableLayout passedTableLayout, final List<String> passedSchedulesForGroup){
            tableLayout = passedTableLayout;
            schedulesForGroup = passedSchedulesForGroup;
        }

        /**
         * Обработчик нажатия на кнопку удаления расписания.
         * @param v Ссылка на нажатый View
         */
        @Override
        public void onClick(final View v) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.delete))
                    .setMessage(getResources().getString(R.string.confirm_delete_message))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new PositiveButtonClickListener(v))
                    .setNegativeButton(android.R.string.no, null).show();
        }

        private class PositiveButtonClickListener implements DialogInterface.OnClickListener {
            View v;

            /**
             * Конструктор класса который обрабатывает подтверждение удаления
             * @param v - ткущее view
             */
            public PositiveButtonClickListener(View v) {
                this.v = v;
            }

            /**
             * Метод вызывается при удалении расписания группы. В методе проверяется является ли
             * переданная группа дефолтной, и если так то устанавливает дефолтное расписание в null
             *
             * @param passedGroupName група для удаления
             */
            private void deleteDefaultGroupIfNeed(String passedGroupName) {
                String settingFileName = getActivity().getString(R.string.setting_file_name);
                final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
                final SharedPreferences.Editor editor = preferences.edit();
                String groupFiledInSettings = getActivity().getString(R.string.default_group_field_in_settings);
                String defaultGroupName = preferences.getString(groupFiledInSettings, "none");

                if (defaultGroupName.length() >= COUNT_DIGITS_IN_STUDENT_GROUP
                        && passedGroupName.equalsIgnoreCase(defaultGroupName.substring(0, COUNT_DIGITS_IN_STUDENT_GROUP))) {

                    editor.remove(groupFiledInSettings);

                }
                editor.remove(defaultGroupName);
                editor.apply();
            }

            /**
             * Метод вызывается при подтверждении удаления расписания
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(null));
                TableRow selectedRow = (TableRow) v.getParent();
                Integer rowNumber = (Integer) selectedRow.getTag();
                StringBuilder fileNameForDelete = new StringBuilder(schedulesForGroup.get(rowNumber));

                //set group as unavailable
                sdd.setAsUnavailable(fileNameForDelete.toString());

                //delete group
                Integer dClausesNum;
                if ((dClausesNum = sdd.deleteSchedule(fileNameForDelete.toString(), false)) > 0) {
                    Toast.makeText(getActivity(), "Расписание для группы " +
                            fileNameForDelete + " было удалено.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Ошибка при удалении расписания для группы " +
                            fileNameForDelete + ".", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "deleted clauses number = " + dClausesNum);


                deleteDefaultGroupIfNeed(fileNameForDelete.toString());


                setDownloadedSchedulesForGroup(sdd.getAvailableGroups());

                populateTableLayout(tableLayout, getDownloadedSchedulesForGroup());
            }
        }
    }
}

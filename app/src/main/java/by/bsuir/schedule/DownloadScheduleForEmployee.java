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
import android.support.annotation.Nullable;
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
import by.bsuir.schedule.model.Employee;
import by.bsuir.schedule.utils.DateUtil;
import by.bsuir.schedule.utils.WidgetUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Фрагмент для скачивания расписание преподавателей
 */
public class DownloadScheduleForEmployee extends Fragment {
    private static final String TAG = "employeeDownloadTAG";
    private static final String PATTERN = "^[а-яА-ЯёЁ]+";
    private static final Integer XML_EXTENSION_LENGTH = 4;
    private static final Integer INITIALS_LENGTH = 2;

    private List<Employee> availableEmployeeList;
    private List<String> downloadedSchedules;
    private  View currentView;
    private boolean isDownloadingNewSchedule;
    private TableLayout tableLayoutForDownloadedSchedules;
    ProgressDialog mProgressDialog;

    private OnFragmentInteractionListener mListener;

    /**
     * Фрагмент для скачивания расписание преподавателей
     */
    public DownloadScheduleForEmployee() {
        // Required empty public constructor
    }

    /**
     * Метод вызывается для того чтобы fragment создал view которую будет показано пользователю.
     * @param inflater объект служащий для создания view
     * @param container container это родительский view, к которому фрагмент будет присоединен
     * @param savedInstanceState Если фрагмент был пересоздан, то в savedInstanceState будут
     *                           хранится его сохраненные параметры
     * @return Возвращает View которое будет показано пользователю
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_download_schedule_for_employee, container, false);


        ConnectivityManager connectMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadEmployeeXML().execute();
        } else {
            Toast.makeText(getActivity(), R.string.can_not_load_list_of_employees, Toast.LENGTH_LONG).show();
        }


        Button downloadButton = (Button) currentView.findViewById(R.id.buttonForDownloadEmployeeSchedule);
        downloadButton.setOnClickListener(new DownloadButtonClickListener());

        setTableLayoutForDownloadedSchedules((TableLayout) currentView.findViewById(R.id.tableLayoutForEmployee));

        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        setDownloadedSchedules(sdd.getAvailableTeachers());
        populateTableLayout(getTableLayoutForDownloadedSchedules(), downloadedSchedules);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.downloading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        return currentView;
    }

    /**
     * Метод вызывает асинхронный метод для скачивания или обновления расписания.
     * @param employeeNameForDownload Имя преподавателя для которого необходимо скачать или
     *                                обновить расписание
     */
    public void downloadOrUpdateScheduleForEmployee(String employeeNameForDownload){
        //убираем виртуальную клавиатуру перед началом загрузки
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getActivity().getCurrentFocus() != null && imm != null)
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        DownloadFilesTask task = new DownloadFilesTask(getActivity());
        task.filesDir = getActivity().getFilesDir();
        task.execute(employeeNameForDownload);
    }

    /**
     * Метод заполняет таблицу списком уже скачанных и сохраненных на устройстве
     * расписаний для преподавателя
     * @param tableLayout ссылка на таблицу в которую помещается список скачанных расписаний
     * @param schedulesForEmployee Список преподавателей для которых скачано расписание
     */
    public void populateTableLayout(final TableLayout tableLayout, final List<String> schedulesForEmployee){
        Integer currentRowNumber = 0;
        tableLayout.removeAllViews();
        tableLayout.setPadding(5, 0, 5, 0);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TableRow headerRow = new TableRow(getActivity());
        TextView employeeHeaderTextView = new TextView(getActivity());
        employeeHeaderTextView.setText(getResources().getString(R.string.employee_shortcut));
        employeeHeaderTextView.setGravity(Gravity.START);
        employeeHeaderTextView.setLayoutParams(params);
        employeeHeaderTextView.setTypeface(null, Typeface.BOLD);
        headerRow.addView(employeeHeaderTextView);

        TextView lastUpdateHeader = new TextView(getActivity());
        lastUpdateHeader.setText(getResources().getString(R.string.last_updated));
        lastUpdateHeader.setTypeface(null, Typeface.BOLD);
        headerRow.addView(lastUpdateHeader);

        TextView deleteHeader = new TextView(getActivity());
        deleteHeader.setText(getResources().getString(R.string.delete));
        deleteHeader.setTypeface(null, Typeface.BOLD);
        deleteHeader.setGravity(Gravity.CENTER);
        deleteHeader.setPadding(5, 0, 5, 0);
        headerRow.addView(deleteHeader);

        TextView refreshHeader = new TextView(getActivity());
        refreshHeader.setText(R.string.refresh);
        refreshHeader.setTypeface(null, Typeface.BOLD);
        refreshHeader.setGravity(Gravity.CENTER);
        refreshHeader.setPadding(5,0,5,0);
        headerRow.addView(refreshHeader);

        headerRow.setPadding(0, 0, 0, 18);
        tableLayout.addView(headerRow);

        for(String currentEmployeeSchedule : schedulesForEmployee) {
            TableRow rowForEmployeeSchedule = new TableRow(getActivity());
            TextView textViewForEmployeeName = new TextView(getActivity());
            textViewForEmployeeName.setGravity(Gravity.START);
            textViewForEmployeeName.setText(getEmployeeNameFromString(currentEmployeeSchedule));
            textViewForEmployeeName.setLayoutParams(params);
            rowForEmployeeSchedule.addView(textViewForEmployeeName);

            TextView lastUpdatedTextView = new TextView(getActivity());
            lastUpdatedTextView.setText(getLastUpdateFromPreference(currentEmployeeSchedule));
            rowForEmployeeSchedule.addView(lastUpdatedTextView);


            ImageView deleteImageView = new ImageView(getActivity());
            deleteImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_remove));
            deleteImageView.setOnClickListener(new ButtonClickListener(tableLayout, schedulesForEmployee));
            rowForEmployeeSchedule.addView(deleteImageView);

            ImageView refreshImageView = new ImageView(getActivity());
            refreshImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_refresh));

            refreshImageView.setOnClickListener(new RefreshScheduleListener(schedulesForEmployee, getActivity()));
            rowForEmployeeSchedule.addView(refreshImageView);

            rowForEmployeeSchedule.setOnClickListener(new EmployeeNameClickListener(schedulesForEmployee, getActivity()));

            rowForEmployeeSchedule.setTag(currentRowNumber);
            currentRowNumber++;
            tableLayout.addView(rowForEmployeeSchedule);
        }
    }

    private class EmployeeNameClickListener implements View.OnClickListener {
        private List<String> schedulesForEmployee;
        Activity activity;

        /**
         * Конструктор класса который обрабатывает нажатие на имя преподавателя
         * @param schedules - расписание
         * @param activity - текущее активити
         */
        public EmployeeNameClickListener(List<String> schedules, Activity activity) {
            this.schedulesForEmployee = schedules;
            this.activity = activity;
        }

        /**
         * Метод обновляет название дефолтного расписания. Метод взывается после скачивания нового
         * расписания, или если пользователь выбирает расписание из списка скачанных ранее расписаний.
         * @param passedDefaultEmployee Имя удаляемого преподавателя
         * @param isDownloadedSchedule Переменная указывает было ли расписание скачано, или же
         *                             оно было выбрано из списка ранее скачанных
         */
        private void updateDefaultEmployee(String passedDefaultEmployee, boolean isDownloadedSchedule){
            String defaultEmployee = passedDefaultEmployee;
            if(".xml".equalsIgnoreCase(defaultEmployee.substring(defaultEmployee.length() - XML_EXTENSION_LENGTH))) {
                defaultEmployee = defaultEmployee.substring(0, defaultEmployee.length() - XML_EXTENSION_LENGTH);
            }
            String settingFileName = activity.getString(R.string.setting_file_name);
            final SharedPreferences preferences = activity.getSharedPreferences(settingFileName, 0);
            final SharedPreferences.Editor editor = preferences.edit();
            String employeeFieldInSettings = activity.getString(R.string.default_employee_field_in_settings);
            String currentDefaultEmployee = preferences.getString(employeeFieldInSettings, "none");
            if(!defaultEmployee.equalsIgnoreCase(currentDefaultEmployee)){
                editor.putString(employeeFieldInSettings, defaultEmployee);
                String groupFiledInSettings = activity.getString(R.string.default_group_field_in_settings);
                editor.putString(groupFiledInSettings, "none");
            }
            if(isDownloadedSchedule) {
                editor.putString(defaultEmployee, DateUtil.getCurrentDateAsString());
            }
            editor.apply();
            WidgetUtil.updateWidgets(activity);
        }

        /**
         * Метод который обрабатывает нажатие
         */
        @Override
        public void onClick(View v) {
            TableRow selectedTableRow = (TableRow) v;
            Integer rowNumber = (Integer) selectedTableRow.getTag();
            String selectedEmployee = schedulesForEmployee.get(rowNumber);
            updateDefaultEmployee(selectedEmployee, false);
            mListener.onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
        }
    }

    private class RefreshScheduleListener implements View.OnClickListener {
        private List<String> schedulesForEmployee;
        Activity activity;

        /**
         * Конструктор класса который обрабатывает нажатие иконук обновления расписания
         * @param schedules - расписание
         * @param act- текущее активити
         */
        public RefreshScheduleListener(List<String> schedules, Activity act) {
            this.schedulesForEmployee = schedules;
            this.activity = act;
        }

        /**
         * Метод обновляет название дефолтного расписания. Метод взывается после скачивания нового
         * расписания, или если пользователь выбирает расписание из списка скачанных ранее расписаний.
         * @param passedDefaultEmployee Имя удаляемого преподавателя
         * @param isDownloadedSchedule Переменная указывает было ли расписание скачано, или же
         *                             оно было выбрано из списка ранее скачанных
         */
        private void updateDefaultEmployee(String passedDefaultEmployee, boolean isDownloadedSchedule){
            String defaultEmployee = passedDefaultEmployee;
            if(".xml".equalsIgnoreCase(defaultEmployee.substring(defaultEmployee.length() - XML_EXTENSION_LENGTH))) {
                defaultEmployee = defaultEmployee.substring(0, defaultEmployee.length() - XML_EXTENSION_LENGTH);
            }
            String settingFileName = activity.getString(R.string.setting_file_name);
            final SharedPreferences preferences = activity.getSharedPreferences(settingFileName, 0);
            final SharedPreferences.Editor editor = preferences.edit();
            String employeeFieldInSettings = activity.getString(R.string.default_employee_field_in_settings);
            String currentDefaultEmployee = preferences.getString(employeeFieldInSettings, "none");
            if(!defaultEmployee.equalsIgnoreCase(currentDefaultEmployee)){
                editor.putString(employeeFieldInSettings, defaultEmployee);
                String groupFiledInSettings = activity.getString(R.string.default_group_field_in_settings);
                editor.putString(groupFiledInSettings, "none");
            }
            if(isDownloadedSchedule) {
                editor.putString(defaultEmployee, DateUtil.getCurrentDateAsString());
            }
            editor.apply();
            WidgetUtil.updateWidgets(activity);
        }

        /**
         * Метод который обрабатывает нажатие
         */
        @Override
        public void onClick(View v) {
            ConnectivityManager connectMan = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                TableRow selectedRow = (TableRow) v.getParent();
                Integer rowNumber = (Integer) selectedRow.getTag();
                SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(activity));
                String fileNameForRefresh = schedulesForEmployee.get(rowNumber);
                sdd.deleteSchedule(fileNameForRefresh, true);

                setIsDownloadingNewSchedule(false);

                UpdateScheduleForEmployee updateTask = new UpdateScheduleForEmployee(activity);
                updateTask.fileDir = activity.getFilesDir();
                updateTask.execute(fileNameForRefresh);
                updateDefaultEmployee(fileNameForRefresh, true);
            } else {
                Toast.makeText(activity, activity.getResources().getString(R.string.no_connection_to_network), Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Получает даты последнего обновления для расписания
     * @param passedSchedulesName Расписание для которого необходимо получить дату последнего
     *                            обновления
     * @return возвращает дату последнего обновления
     */
    private String getLastUpdateFromPreference(String passedSchedulesName){
        String schedulesName = passedSchedulesName.substring(0, passedSchedulesName.length() - XML_EXTENSION_LENGTH);
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        return preferences.getString(schedulesName, "-");
    }

    /**
     * Метод получает строку в которой соединены имя преподавателя и его id. Используя регулярное
     * выражения достается только имя преподавателя
     * @param passedString строка состоящая из имени преподавателя и его id
     * @return Возвращает имя преподавателя
     */
    private static String getEmployeeNameFromString(String passedString){
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(passedString);
        if(matcher.find()){
            return matcher.group(0);
        }
        return "Not found matches";
    }


    /**
     * Конвертит объкт Employee в строку
     * @param employee Преподаватель
     * @return Возаращает ФИО полученного преподавателя
     */
    private String employeeToString(Employee employee){
        String employeeFIO = employee.getLastName();
        if(employee.getFirstName() != null && employee.getFirstName().length() > 0){
            employeeFIO += " " + employee.getFirstName();
            if(employee.getMiddleName() != null && employee.getMiddleName().length() > 0){
                employeeFIO += " " + employee.getMiddleName();
            }
        }
        return employeeFIO;
    }

    /**
     * Метод вызывается после присоединения фрагмента к активити
     * @param activity ссылка на активити к которой присоединен фрагмент
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            Log.v(TAG, e.getMessage(), e);
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Метод вызывается при отсоединении фрагмента от активити
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Метод возвращает список преподавателей для которых доступно расписание
     */
    public List<Employee> getAvailableEmployeeList() {
        return availableEmployeeList;
    }

    /**
     * Метод устанавливает список преподавателей для которых доступно расписание
     */
    public void setAvailableEmployeeList(List<Employee> availableEmployeeList) {
        this.availableEmployeeList = availableEmployeeList;
    }

    /**
     * Асинхронный класс для скачивания списка преподавателей у которых есть расписание
     */
    private class DownloadEmployeeXML extends AsyncTask<Void, Void, String> {
        static final String ERROR = "Some ERROR while downloading";
        /**
         * Конвертит лист преподавателей в массив имен преподавателей
         * @param employees лист преподавателй
         * @return возвращает массив имен преподавателей
         */
        private String[] convertEmployeeToArray(List<Employee> employees){
            List<String> resultList = new ArrayList<>();
            for(Employee employee : employees){
                resultList.add(employeeToString(employee));
            }
            String[] resultArray = new String[resultList.size()];
            resultArray = resultList.toArray(resultArray);
            return resultArray;
        }


        /**
         * Метод который в фоне скачивает список преподавателей у которых есть расписание
         * @param parameters null
         * @return Возвращает null если скачивание прошло успешно, иначе возвращает сообщение об
         * ошибке
         */
        @Override
        protected String doInBackground(Void... parameters) {

            List<Employee> loadedEmployees = LoadSchedule.loadListEmployee();
            if (loadedEmployees == null)
                return ERROR;
            setAvailableEmployeeList(loadedEmployees);
            return null;
        }

        /**
         * Метод вызывается после скачивания списка доступных преподавателей. В  методе
         * происходит конфигурация AutoCompleteTextView
         * @param result null
         */
        @Override
        protected void onPostExecute(String result) {

            if (result == null) {
                try {
                    String[] employeesAsArray = convertEmployeeToArray(availableEmployeeList);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, employeesAsArray);
                    AutoCompleteTextView textView = (AutoCompleteTextView) currentView.findViewById(R.id.autoCompleteForEmployee);
                    textView.setAdapter(adapter);
                } catch (NullPointerException ex) {
                    Log.v(TAG, ERROR, ex);
                }

            }

        }
    }


    private class DownloadFilesTask extends AsyncTask<String, Integer, String> {
        private File filesDir;
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        /**
         * Класс для скачивания расписания для выбранного пользователем преподавателя
         * @param context контекст
         */
        public DownloadFilesTask(Context context) {
            this.context = context;
        }

        /**
         * Метод в фоне скачивает расписание для преподавателя
         * @param employeeName Имя преподавателя для которого нужно скачать расписание
         * @return Возвращает null если скачивание прошло успешно, иначе возвращает сообщение об
         * ошибке
         */
        protected String doInBackground(String... employeeName) {
            return LoadSchedule.loadScheduleForEmployee(employeeName[0], filesDir);
        }

        /**
         * Метод показывает диалоговое окно, с информацией об скачивании расписания
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        /**
         * Обновляе диалое окно, в котором отображается процесс скачивания
         * @param progress процент скачанного
         */
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgress(progress[0]);
        }

        /**
         * Метод вызывается после скачивания расписания для преподавателя. Выводит сообщение об успешном
         * скачивании расписания, или сообщение об ошибке
         * @param finalResult Результат скачивания. null если скачивание прошло успешно, иначе
         *                    сообщение об ошибке
         */
        protected void onPostExecute(String finalResult) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if(finalResult != null) {
                Toast.makeText(getActivity(), getString(R.string.error_while_downloading_schedule), Toast.LENGTH_LONG).show();
            } else {
                if(isDownloadingNewSchedule()) {
                    mListener.onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
                } else{
                    populateTableLayout(getTableLayoutForDownloadedSchedules(), getDownloadedSchedules());
                }
            }
        }
    }

    private class UpdateScheduleForEmployee extends AsyncTask<String, Integer, String> {
        private File fileDir;
        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String teacherName;
        static final String ERROR = "Some ERROR while downloading";

        /**
         * Конструктор класса который обновляет расписание
         * @param context - контекст текущего активити
         */
        public UpdateScheduleForEmployee(Context context) {
            this.context = context;
        }

        /**
         * Метод выполняет загрузку расписания в отдельном потоке
         */
        @Override
        protected String doInBackground(String... parameters) {

            teacherName = parameters[0];
            List<Employee> loadedEmployeeList = LoadSchedule.loadListEmployee();
            if (loadedEmployeeList == null)
                return ERROR;
            for (Employee emp : loadedEmployeeList) {
                if (emp.getLastName().equals(
                        parameters[0].substring(0, parameters[0].length() - INITIALS_LENGTH))) {
                    return LoadSchedule.loadScheduleForEmployeeById(emp, fileDir);
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
            if (result != null) {
                Toast.makeText(getActivity(), getString(R.string.error_while_downloading_schedule), Toast.LENGTH_LONG).show();
            } else if (isDownloadingNewSchedule()) {
                mListener.onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
            } else {
                Toast.makeText(getActivity(), "Расписание для преподавателя " + teacherName + " обновлено.",
                        Toast.LENGTH_LONG).show();
                populateTableLayout(getTableLayoutForDownloadedSchedules(), getDownloadedSchedules());
            }
        }


    }

    /**
     * Метод возвращает загруженное расписание
     */
    public List<String> getDownloadedSchedules() {
        return downloadedSchedules;
    }

    public void setDownloadedSchedules(List<String> downloadedSchedules) {
        this.downloadedSchedules = downloadedSchedules;
    }

    /**
     * Метод показывет загружается ли сейчас новое расписание
     */
    public boolean isDownloadingNewSchedule() {
        return isDownloadingNewSchedule;
    }

    /**
     * Метод устанавливет флаг загрузки нового расписания
     */
    public void setIsDownloadingNewSchedule(boolean isDownloadingNewSchedule) {
        this.isDownloadingNewSchedule = isDownloadingNewSchedule;
    }

    /**
     * Метод возвращает таблицу в которой показываются загруженные расписания
     */
    public TableLayout getTableLayoutForDownloadedSchedules() {
        return tableLayoutForDownloadedSchedules;
    }

    /**
     * Метод устанавливает таблицу в которой показываются загруженные расписания
     */
    public void setTableLayoutForDownloadedSchedules(TableLayout tableLayoutForDownloadedSchedules) {
        this.tableLayoutForDownloadedSchedules = tableLayoutForDownloadedSchedules;
    }

    private class ButtonClickListener implements View.OnClickListener {
        private TableLayout tableLayout;
        private List<String> schedulesForEmployee;

        /**
         * listener для кнопки удаления преподавателя
         * @param passedTableLayout таблица, которую нужно обновить после удаления преподавателя
         * @param passedSchedulesForEmployee список преподаватель для которых скачано расписание
         */
        public ButtonClickListener(final TableLayout passedTableLayout, final List<String> passedSchedulesForEmployee){
            tableLayout = passedTableLayout;
            schedulesForEmployee = passedSchedulesForEmployee;
        }

        /**
         * Обработчик нажатия на кнопку удаления расписания преподавателя
         * @param v Ссылка на нажатую кнопку
         */
        @Override
        public void onClick(final View v) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.delete))
                    .setMessage(getResources().getString(R.string.confirm_delete_message))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new PositiveButtonListener(v, getActivity()))
                    .setNegativeButton(android.R.string.no, null).show();
        }

        private class  PositiveButtonListener implements DialogInterface.OnClickListener {
            View v;
            Activity activity;

            /**
             * Конструктор класса который обрабатывает подтверждение удаления
             * @param view - ткущее view
             * @param activity - текущее activity
             */
            public PositiveButtonListener(View view, Activity activity) {
                this.v = view;
                this.activity = activity;
            }

            /**
             * Метод вызывается при удалении расписания преподавателя. Метод проверяет является
             * ли дефолтное расписание расписанием удаляемого преподавателя. Если это так, то устанавливаем
             * дефолтное расписание в null
             * @param passedEmployeeName Имя удаляемого преподавателя
             */
            private void deleteDefaultEmployeeIfNeed(String passedEmployeeName){
                String settingFileName = getActivity().getString(R.string.setting_file_name);
                final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
                final SharedPreferences.Editor editor = preferences.edit();
                String employeeFieldInSettings = getActivity().getString(R.string.default_employee_field_in_settings);
                String defaultEmployeeName = preferences.getString(employeeFieldInSettings, "none");

                if(passedEmployeeName.equalsIgnoreCase(defaultEmployeeName)){
                    editor.remove(employeeFieldInSettings);

                }
                editor.remove(passedEmployeeName);
                editor.apply();
            }

            /**
             * Метод вызывается при подтверждении удаления расписания
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(activity));
                TableRow selectedRow = (TableRow) v.getParent();
                Integer rowNumber = (Integer) selectedRow.getTag();
                String fileNameForDelete = schedulesForEmployee.get(rowNumber);
                sdd.setAsUnavailable(fileNameForDelete);
                Integer dClausesNum;
                if ((dClausesNum = sdd.deleteSchedule(fileNameForDelete, false)) > 0) {
                    Toast.makeText(activity, "Расписание для преподавателя " +
                            fileNameForDelete + " было удалено.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Ошибка при удалении расписания для преподавателя " +
                            fileNameForDelete + ".", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "deleted clauses number = " + dClausesNum);

                deleteDefaultEmployeeIfNeed(fileNameForDelete);

                setDownloadedSchedules(sdd.getAvailableTeachers());
                populateTableLayout(tableLayout, getDownloadedSchedules());
            }
        }
    }


    /**
     * Обработчик на нажатие кнопки скачивания расписания
     */
    private class DownloadButtonClickListener implements View.OnClickListener {

        /**
         * Возвращает объект Employee по полученному имени преподавателя
         * @param selectedEmployee Введенное пользователем имя преподавателя
         * @return возвращает объект Employee
         */
        @Nullable
        private Employee getEmployeeByName(String selectedEmployee){
            if(getAvailableEmployeeList() != null && !getAvailableEmployeeList().isEmpty()) {
                for (Employee employee : getAvailableEmployeeList()) {
                    String employeeAsString = employeeToString(employee);
                    if (employeeAsString.equalsIgnoreCase(selectedEmployee)) {
                        return employee;
                    }
                }
            }
            return null;
        }


        /**
         * Метод обновляет название дефолтного расписания. Метод взывается после скачивания нового
         * расписания, или если пользователь выбирает расписание из списка скачанных ранее расписаний.
         * @param employee Объект указыающий на удаляемого преподавателя
         * @param isDownloadedSchedule Переменная указывает было ли расписание скачано, или же
         *                             оно было выбрано из списка ранее скачанных
         */
        private void updateDefaultEmployee(Employee employee, boolean isDownloadedSchedule){
            String settingFileName = getActivity().getString(R.string.setting_file_name);
            final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
            final SharedPreferences.Editor editor = preferences.edit();
            String employeeFieldInSettings = getActivity().getString(R.string.default_employee_field_in_settings);
            String groupFiledInSettings = getActivity().getString(R.string.default_group_field_in_settings);
            editor.putString(groupFiledInSettings, "none");
            String employeeForPreferences = getFileNameForEmployeeSchedule(employee);
            editor.putString(employeeFieldInSettings, employeeForPreferences);
            if(isDownloadedSchedule) {
                editor.putString(employeeForPreferences, DateUtil.getCurrentDateAsString());
            }
            editor.apply();
            WidgetUtil.updateWidgets(getActivity());
        }

        /**
         * Метод возвращает ФИО преподавтеля
         * @param employee Преподаватель для которого нужно вернуть ФИО
         * @return возвращает ФИО
         */
        private String getFileNameForEmployeeSchedule(Employee employee){
            return employee.getLastName() + employee.getFirstName().charAt(0) + employee.getMiddleName().charAt(0) + employee.getId();
        }


        /**
         * Метод вызывается когда пользователь нажимает на кнопку скачивания расписания для преподавателя
         * @param v Ссылка на кнопку скачивания расписания
         */
        @Override
        public void onClick(View v) {
            ConnectivityManager connectMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()) {
                AutoCompleteTextView textView = (AutoCompleteTextView) currentView.findViewById(R.id.autoCompleteForEmployee);
                String selectedEmployeeName = textView.getText().toString();
                Employee selectedEmployee = getEmployeeByName(selectedEmployeeName);
                if(selectedEmployee != null) {
                    String parameterForDownloadSchedule = getFileNameForEmployeeSchedule(selectedEmployee);
                    setIsDownloadingNewSchedule(true);
                    downloadOrUpdateScheduleForEmployee(parameterForDownloadSchedule);
                    updateDefaultEmployee(selectedEmployee, true);

                } else{
                    Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.not_found_schedule_for_you), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 30);
                    toast.show();
                }
            } else{
                Toast.makeText(getActivity(), getResources().getString(R.string.no_connection_to_network), Toast.LENGTH_LONG).show();
            }
        }
    }
}

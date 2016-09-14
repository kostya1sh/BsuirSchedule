package by.bsuir.schedule.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

import by.bsuir.schedule.dao.DBHelper;
import by.bsuir.schedule.dao.SchoolDayDao;
import by.bsuir.schedule.dataprovider.RoundedImageView;
import by.bsuir.schedule.model.Employee;
import by.bsuir.schedule.model.Schedule;
import by.bsuir.schedule.utils.EmployeeUtil;

import java.util.List;

/**
 * Created by iChrome on 13.08.2015.
 */
public class ArrayAdapterGroupSchedule extends BaseArrayAdapterSchedule {
    Context ctx;

    /**
     * Адаптер для отображения расписания занятий группы
     *
     * @param context          контекст
     * @param layoutResourceId id ListView в котором отображается список занятий группы
     * @param data             список занятий которые нужно отобразить в listView
     */
    public ArrayAdapterGroupSchedule(Context context, int layoutResourceId, Schedule[] data) {
        super(context, layoutResourceId, data);
        ctx = context;
    }

    /**
     * Метод возвращает расписание для заданной позиции
     *
     * @param position номер занятия для которого нужно создать view
     * @return возвращает schedule
     */
    public Schedule getSchedule(int position) {
        return data[position];
    }

    /**
     * Метод возвращает view для выбранного занятия из списка занятий группы для выбранного дня
     *
     * @param position          номер занятия для которого нужно создать view
     * @param passedConvertView View которое нужно заполнить дабнными выбранного занятия группы
     * @param parent            родительское View
     * @return возвращает результирующее view
     */
    @Override
    public View getView(int position, View passedConvertView, ViewGroup parent) {
        View convertView = passedConvertView;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutID, parent, false);
        }
        Schedule currentSchedule = data[position];

        View lessonTypeView = convertView.findViewById(R.id.lessonTypeView);
        updateLessonTypeView(lessonTypeView, currentSchedule.getLessonType());

        TextView scheduleTimeTextView = (TextView) convertView.findViewById(R.id.scheduleTimeTextView);
        scheduleTimeTextView.setText(currentSchedule.getLessonTime());

        TextView subjectName = (TextView) convertView.findViewById(R.id.subjectNameListItem);
        subjectName.setMaxWidth(200);
        String textForSubjectTextView = currentSchedule.getSubject();
        if (!currentSchedule.getLessonType().isEmpty()) {
            textForSubjectTextView += " (" + currentSchedule.getLessonType() + ")";
        }
        subjectName.setText(textForSubjectTextView);

        TextView noteTextView = (TextView) convertView.findViewById(R.id.scheduleNoteTextView);
        noteTextView.setText(currentSchedule.getNote());

        TextView employeeName = (TextView) convertView.findViewById(R.id.employeeNameListItem);
        employeeName.setText(convertEmployeeListToString(currentSchedule.getEmployeeList()));

        TextView subGroup = (TextView) convertView.findViewById(R.id.subGroupTextView);
        if (!currentSchedule.getSubGroup().isEmpty()) {
            subGroup.setText(currentSchedule.getSubGroup().concat(" подгр."));
        } else subGroup.setText("");

        TextView weekNumber = (TextView) convertView.findViewById(R.id.weekNumberTextView);
        if (currentSchedule.getWeekNumbers().size() != 4 && currentSchedule.getWeekNumbers() != null) {
            weekNumber.setText(convertListString(currentSchedule.getWeekNumbers(), " неделя"));
        } else weekNumber.setText("");

        TextView auditoryName = (TextView) convertView.findViewById(R.id.auditoryNameListItem);
        auditoryName.setText(convertListString(currentSchedule.getAuditories(), ""));

        ImageView empPhoto = (ImageView) convertView.findViewById(R.id.employeePhoto);
        RoundedImageView roundedPhoto = new RoundedImageView(ctx);

        if (currentSchedule.getEmployeeList().size() > 0) {
            if (currentSchedule.getEmployeeList().get(0).getPhoto() != null) {
                Bitmap roundedBitmap = roundedPhoto.getCroppedBitmap(currentSchedule.getEmployeeList().get(0).getPhoto(), 110);
                empPhoto.setImageBitmap(roundedBitmap);
            } else {
                SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(ctx));
                Bitmap rawPhoto = sdd.getEmployeePhoto(currentSchedule.getEmployeeList().get(0));
                if (rawPhoto != null) {
                    Bitmap roundedBitmap = roundedPhoto.getCroppedBitmap(rawPhoto, 110);
                    empPhoto.setImageBitmap(roundedBitmap);
                } else {
                    empPhoto.setImageBitmap(null);
                }
            }
        }

        return convertView;
    }



    /**
     * Метод конвертит список преподавателей в одну строку
     * @param employeeList список преподавателей
     * @return Возвращает строку состоящую из всех переданных преподавателей
     */
    @NonNull
    private String convertEmployeeListToString(List<Employee> employeeList){
        StringBuilder builder = new StringBuilder();
        for(Employee employee : employeeList){
            builder.append(EmployeeUtil.getEmployeeFIO(employee));
            builder.append(", ");
        }
        if(builder.length() > 2) {
            builder.delete(builder.length() - 2, builder.length());
        }
        return builder.toString();
    }
}

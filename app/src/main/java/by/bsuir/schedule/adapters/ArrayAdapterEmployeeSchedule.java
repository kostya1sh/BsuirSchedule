package by.bsuir.schedule.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

import by.bsuir.schedule.dao.DBHelper;
import by.bsuir.schedule.dao.SchoolDayDao;
import by.bsuir.schedule.dataprovider.RoundedImageView;
import by.bsuir.schedule.model.Schedule;

/**
 * Created by iChrome on 14.08.2015.
 */
public class ArrayAdapterEmployeeSchedule extends BaseArrayAdapterSchedule {
    Context ctx;

    /**
     * Адаптер для списка расписания преподавателя
     * @param context контекст
     * @param layoutResourceId id listView в котором отображаются занятия преподавателя
     * @param data данные которые необходимо отобразить в listView
     */
    public ArrayAdapterEmployeeSchedule(Context context, int layoutResourceId, Schedule[] data){
        super(context, layoutResourceId, data);
        ctx = context;
    }

    /**
     * Получает расписание для записи на которую нажали
     * @param position номер записи
     * @return расписание
     */
    public Schedule getSchedule(int position) {
        return data[position];
    }

    /**
     * Метод возвращает view для выбранного занятия из списка занятий преподавателя выбранного пользователем дня
     * @param position номер занятия для которого нужно создать view
     * @param passedConvertView View которое нужно заполнить дабнными выбранного занятия
     * @param parent родительское View
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

        TextView scheduleTimeTextView = (TextView) convertView.findViewById(R.id.scheduleTimeTextView);
        scheduleTimeTextView.setText(currentSchedule.getLessonTime());

        View lessonTypeView = convertView.findViewById(R.id.lessonTypeView);
        updateLessonTypeView(lessonTypeView, currentSchedule.getLessonType());

        TextView subjectName = (TextView) convertView.findViewById(R.id.subjectNameListItem);
        String textForSubjectTextView = currentSchedule.getSubject();
        if (!currentSchedule.getLessonType().isEmpty()) {
            textForSubjectTextView += " (" + currentSchedule.getLessonType() + ")";
        }
        subjectName.setText(textForSubjectTextView);
        TextView noteTextView = (TextView) convertView.findViewById(R.id.scheduleNoteTextView);
        noteTextView.setText(currentSchedule.getNote());
        TextView employeeName = (TextView) convertView.findViewById(R.id.employeeNameListItem);
        employeeName.setText(currentSchedule.getStudentGroup());

        TextView weekNumber = (TextView) convertView.findViewById(R.id.weekNumberTextView);
        if (currentSchedule.getWeekNumbers().size() != 4) {
            weekNumber.setText(convertListString(currentSchedule.getWeekNumbers(), " неделя"));
        } else weekNumber.setText("");

        TextView subGroup = (TextView) convertView.findViewById(R.id.subGroupTextView);
        if (!currentSchedule.getSubGroup().isEmpty()) {
            subGroup.setText(currentSchedule.getSubGroup().concat(" подгр."));
        } else subGroup.setText("");

        TextView auditoryName = (TextView) convertView.findViewById(R.id.auditoryNameListItem);
        auditoryName.setText(convertListString(currentSchedule.getAuditories(), ""));

        ImageView empPhoto = (ImageView) convertView.findViewById(R.id.employeePhoto);
        RoundedImageView roundedPhoto = new RoundedImageView(ctx);

        if (currentSchedule.getEmployeeList().size() > 0) {
            if (currentSchedule.getEmployeeList().get(0).getPhoto() != null) {
                Bitmap roundedBitmap = roundedPhoto.getCroppedBitmap(currentSchedule.getEmployeeList().get(0).getPhoto(), 150);
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
}

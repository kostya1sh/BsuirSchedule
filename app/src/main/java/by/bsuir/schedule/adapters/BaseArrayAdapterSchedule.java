package by.bsuir.schedule.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.myapplication.R;
import by.bsuir.schedule.model.Schedule;

import java.util.List;

/**
 * Created by iChrome on 23.09.2015.
 */
public abstract class BaseArrayAdapterSchedule extends ArrayAdapter<Schedule> {
    Context context;
    int layoutID;
    Schedule[] data = null;


    /**
     * Базовый адаптер для отображения расписания занятий
     * @param context контекст
     * @param layoutResourceId id listView в котором необходимо отобразить список занятий
     * @param data список занятий для отображения
     */
    public BaseArrayAdapterSchedule(Context context, int layoutResourceId, Schedule[] data){
        super(context, layoutResourceId,data);
        this.context = context;
        this.layoutID = layoutResourceId;
        this.data = data;
    }

    /**
     * Метод окрашивает переданное view в определенный цвет в зависимости от типа занятия
     * @param view view которое нужно окрасить
     * @param lessonType тип занятия, на основе которого определяется цвет фона
     */
    public void updateLessonTypeView(View view, String lessonType){
        switch(lessonType){
            case "ПЗ":
                view.setBackgroundResource(R.color.yellow);
                break;
            case "УПз":
                view.setBackgroundResource(R.color.yellow);
                break;
            case "ЛК":
                view.setBackgroundResource(R.color.green);
                break;
            case "УЛк":
                view.setBackgroundResource(R.color.green);
                break;
            case "ЛР":
                view.setBackgroundResource(R.color.red);
                break;
            default:
                view.setBackgroundResource(R.color.blue);
                break;
        }
    }

    /**
     * Метод конвертит список значений в одну строку
     * @param values список значений
     * @param addition дополнение
     * @return Возвращает строку состоящую из всех значений в списке + переданное дополнение
     */
    @NonNull
    protected static String convertListString(List<String> values, String addition){
        StringBuilder result = new StringBuilder();
        for(String value : values){
            result.append(value);
            result.append(", ");
        }
        if(result.length() > 2) {
            result.delete(result.length() - 2, result.length());
            result.append(addition);
        }
        return result.toString();
    }

    /**
     * Метод возвращает доступность элементов в ListView
     * @return возвращаем false, для того чтобы нельзя было нажимать на занятия в ListView
     */
    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    /**
     * Метод возвращает возможность выбора элементов из ListView
     * @return возвращаем false, для того чтобы нельзя было нажимать на занятия в ListView
     */
    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}

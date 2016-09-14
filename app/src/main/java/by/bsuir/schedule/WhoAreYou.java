package by.bsuir.schedule;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.myapplication.R;

import by.bsuir.schedule.model.AvailableFragments;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WhoAreYou#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WhoAreYou extends Fragment {
    private static final String TAG = "whoAreYouTag";

    private OnFragmentInteractionListener listener;

    /**
     * Фрагмент предоставляющий пользователю выбор какое расписание скачивать: расписание группы
     * или преподавтеля
     */
    public WhoAreYou() {
        // Required empty public constructor
    }

    /**
     * Метод для создания экземпляра фрагмента
     * @return возвращает созданный фрагмент
     */
    public static WhoAreYou newInstance() {
        return new WhoAreYou();
    }

    /**
     * Метод для создания View которое отображается пользователю
     * @param inflater Объект для создания View
     * @param container родительское View
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return Возвращает созданное view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_who_are_you, null);

        ImageButton buttonForStudentSchedule = (ImageButton) view.findViewById(R.id.studentImageButton);
        ImageButton buttonForEmployeeSchedule = (ImageButton) view.findViewById(R.id.employeeImageButton);

        buttonForStudentSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onChangeFragment(AvailableFragments.DOWNLOAD_SCHEDULE_FOR_GROUP);
            }
        });

        buttonForEmployeeSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onChangeFragment(AvailableFragments.DOWNLOAD_SCHEDULE_FOR_EMPLOYEE);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            Log.v(TAG, e.getMessage(), e);
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}

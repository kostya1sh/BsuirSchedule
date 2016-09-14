package by.bsuir.schedule.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.myapplication.R;

/**
 * Конфигурационная активити. Вызывается при добавлении виджета
 */
public class ScheduleWidgetConfigureActivity extends AppCompatActivity {
    private static final Integer FIRST_SUBGROUP = 1;
    private static final Integer SECOND_SUBGROUP = 2;
    private int mAppWidgetId;
    private RadioButton firstSubgroupRadioButton;
    private RadioButton secondSubgroupRadioButton;

    /**
     * Метод вызывается при создании активити. Здесь настраиваются необходимые для работы данные
     * @param savedInstanceState Сохраненное состояние. В этом объекте хранятся данные, если
     *                           активити была пересоздана
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_widget_configure);
        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }


        View.OnClickListener radioButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                switch (rb.getId()) {
                    case R.id.firstSubGroupRadioButton:
                        updateDefaultSubGroup(FIRST_SUBGROUP);
                        break;
                    case R.id.secondSubGroupRadioButton:
                        updateDefaultSubGroup(SECOND_SUBGROUP);
                        break;
                    default:
                        break;
                }
            }
        };

        firstSubgroupRadioButton = (RadioButton) findViewById(R.id.firstSubGroupRadioButton);
        firstSubgroupRadioButton.setOnClickListener(radioButtonListener);

        secondSubgroupRadioButton = (RadioButton) findViewById(R.id.secondSubGroupRadioButton);
        secondSubgroupRadioButton.setOnClickListener(radioButtonListener);

        Button finishButton = (Button) findViewById(R.id.widgetOKButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSubGroupSetted()){

                    Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, ScheduleWidgetConfigureActivity.this, ScheduleWidgetProvider.class);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {mAppWidgetId});
                    sendBroadcast(intent);

                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                } else {
                    Toast.makeText(ScheduleWidgetConfigureActivity.this, getString(R.string.should_select_subgroup), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    /**
     * Проверяет выбрал ли пользователь подгруппу.
     * @return Возвращает true если пользователь выбрал подгруппу, иначе возвращает false
     */
    private boolean isSubGroupSetted(){
        //return true if at least one radioButton is checked
        return !(!firstSubgroupRadioButton.isChecked() && !secondSubgroupRadioButton.isChecked());
    }

    /**
     * Обновляет в SharedPreference дефолтную подгруппу. Это знаение используется потом в виджете
     * @param subGroup Выбранная пользователь подгруппа
     */
    private void updateDefaultSubGroup(@NonNull Integer subGroup) {
        final SharedPreferences preferences = getSharedPreferences(getResources().getString(R.string.setting_file_name), 0);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getResources().getString(R.string.default_subgroup), subGroup);
        editor.apply();
    }

    /**
     * Метод инициализирует option menu
     * @param menu в menu можно добавлять свои элементы
     * @return возвращает true для того чтобы menu  было отображалось
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_schedule_widget_configure, menu);
        setTitle(getResources().getString(R.string.configuration_for_widget));
        return true;
    }
}

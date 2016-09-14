package by.bsuir.schedule.dataprovider;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import by.bsuir.schedule.dao.DBColumns;
import by.bsuir.schedule.dao.DBHelper;

/**
 * Created by kostya on 12.04.2016.
 */

/**
 * Класс для фоновой загрузки фотографии
 */
public class PhotoDownloader extends AsyncTask<Void, Void, Integer>{
    private static final String LOG_TAG = "PhotoDownloader";
    private DBHelper dbHelper;


    /**
     * Класс для фоновой загрузки фотографии и сохранения их в базц данных
     * @param helper база данных
     */
    public PhotoDownloader(Activity a, DBHelper helper) {
        setDbHelper(helper);
    }

    /**
     * Получаем доступ к базе данных
     * @return хелпер текущей бд
     */
    public DBHelper getDbHelper() {
        return dbHelper;
    }

    /**
     * Устанавливаем базу данных с которой будем работать
     * @param dbHelper хелпер бд
     */
    public void setDbHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Загружает фотографию из интернета
     * @param url - адресс по которому хранится фотография
     * @return возвращает bitmap если фотография загружена
     * или null если произошла ошибка
     */
    private static Bitmap downloadEmployeePhoto(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(2000);
            connection.connect();
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            return BitmapFactory.decodeStream(bis);
        } catch (SocketTimeoutException ex) {
            Log.v("Photo Downloader", "Connection timeout", ex);
            return null;
        } catch (IOException ex) {
            Log.v("Photo Downloader", "Exception while reading input stream", ex);
            return null;
        }
    }

    /**
     * Метод преобразует битмап в массив байт
     * @param bitmap битмап для преобразования
     * @return массив байт
     */
    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }


    /**
     * Метод выполняется перед стартом нового потока
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(LOG_TAG, "Photo downloader running now.");
    }

    /**
     * Метод выполняется в новом потоке
     * @param voids параметры
     * @return возвращает количество загруженных фотографии
     */
    @Override
    protected Integer doInBackground(Void... voids) {
        Integer retValue = 0;
        List<Cursor> tableEmpCursor;
        String query = "select * from employee";
        tableEmpCursor = getDbHelper().getData(query);
        if (tableEmpCursor.get(0) == null) {
            tableEmpCursor.get(1).close();
            return null;
        }

        Cursor emp = tableEmpCursor.get(0);
        Log.d(LOG_TAG, "Employee table found.");

        emp.moveToFirst();

        do {
            Log.d(LOG_TAG, "Performing search...");
            if (emp.getColumnIndex(DBColumns.EMP_PHOTO_LINK) < 0
                    || emp.getColumnIndex(DBColumns.EMP_PHOTO) < 0) {
                Log.d(LOG_TAG, "Haven't got photo link or photo column in this row.");
                continue;
            }

            retValue += ProceedPhoto(emp);

        } while (emp.moveToNext());

        return retValue;
    }

    private Integer ProceedPhoto(Cursor emp) {
        if (emp.getString(emp.getColumnIndex(DBColumns.EMP_PHOTO_LINK)) != null
                && emp.getBlob(emp.getColumnIndex(DBColumns.EMP_PHOTO)) == null) {
            Log.d(LOG_TAG, "Empty photo found for " + emp.getString(emp.getColumnIndex(DBColumns.LAST_NAME_COLUMN)));
            try {
                URL url = new URL(emp.getString(emp.getColumnIndex(DBColumns.EMP_PHOTO_LINK)));
                Bitmap empPhoto = downloadEmployeePhoto(url);

                byte[] data = null;
                if (empPhoto != null) {
                    data = getBitmapAsByteArray(empPhoto);
                    Log.d(LOG_TAG, "Photo was downloaded.");
                } else {
                    Log.d(LOG_TAG, "Photo wasn't downloaded.");
                }

                if (data != null) {
                    ContentValues values = new ContentValues();
                    values.put(DBColumns.EMP_PHOTO, data);

                    String selection = DBColumns.EMP_PHOTO_LINK + " = '"
                            + emp.getString(emp.getColumnIndex(DBColumns.EMP_PHOTO_LINK)) + "'";

                    getDbHelper().getReadableDatabase().update("employee", values, selection, null);
                    Log.d(LOG_TAG, "Photo saved into data base!");
                } else {
                    Log.d(LOG_TAG, "Conversion bitmap to byte[] failed");
                    return 0;
                }


            } catch (MalformedURLException ex) {
                Log.e(LOG_TAG, ex.getMessage(), ex);
                return 0;
            }

        } else {
            Log.d(LOG_TAG, "Photo already exists or employee haven't got it.");
            return 0;
        }

        return 1;
    }

    /**
     * Метод выполняется после завершения потока
     * @param retValue значения которые возвратил метод doInBackground()
     */
    @Override
    protected void onPostExecute(Integer retValue) {
        super.onPostExecute(retValue);
        Log.d(LOG_TAG, "Work done!");
    }
}

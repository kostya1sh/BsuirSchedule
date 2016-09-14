package by.bsuir.schedule.utils;


import java.util.List;

/**
 * Created by iChrome on 28.12.2015.
 */
public class ListUtil {
    private ListUtil(){
    }

    /**
     * Преоборазует список в строку
     * @param strings список для преобразования
     */
    public static String convertListToString(List<String> strings){
        StringBuilder result = new StringBuilder();
        for(String tempString : strings){
            result.append(tempString);
            result.append(", ");
        }
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }
}

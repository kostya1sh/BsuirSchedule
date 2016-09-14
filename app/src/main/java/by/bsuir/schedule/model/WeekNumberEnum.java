package by.bsuir.schedule.model;

/**
 * Created by iChrome on 18.08.2015.
 */
public enum WeekNumberEnum {
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    ALL(5);

    private Integer order;

    WeekNumberEnum(Integer passedOrder){
        order = passedOrder;
    }

    public Integer getOrder(){
        return order;
    }

    /**
     * Получаем WeekNumberEnum по переданному номеру учебной недели
     * @param order номер учебной недели для которой нужно получить WeekNumberEnum
     * @return возвращает WeekNumberEnum
     */
    public static WeekNumberEnum getByOrder(int order){
        for(WeekNumberEnum item : WeekNumberEnum.values()){
            if(item.getOrder().equals(order)){
                return item;
            }
        }
        return WeekNumberEnum.ALL;
    }
}

package by.bsuir.schedule.model;

/**
 * Created by iChrome on 18.08.2015.
 */
public enum SubGroupEnum {
    FIRST_SUB_GROUP(1),
    SECOND_SUB_GROUP(2),
    ENTIRE_GROUP(3);

    private Integer order;

    SubGroupEnum(Integer passedOrder){
        order = passedOrder;
    }

    public Integer getOrder(){
        return order;
    }
}

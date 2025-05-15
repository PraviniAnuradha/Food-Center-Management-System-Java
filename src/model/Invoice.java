package model;

import java.time.LocalDateTime;

public class Invoice {
    private String customer_id;
    private LocalDateTime date_time;
    private String user_id;
    private String uniq_id;
    private int order_through_id;
    private int order_type_id;
    private String code;
    private String remarks;

    public Invoice(String customer_id, LocalDateTime date_time, String user_id, String uniq_id,
                   int order_through_id, int order_type_id, String code, String remarks) {
        this.customer_id = customer_id;
        this.date_time = date_time;
        this.user_id = user_id;
        this.uniq_id = uniq_id;
        this.order_through_id = order_through_id;
        this.order_type_id = order_type_id;
        this.code = code;
        this.remarks = remarks;
    }

    public String toInsertQuery() {
        return "('" + customer_id + "','" + date_time + "','" + user_id + "','" + uniq_id + "','" +
                order_through_id + "','" + order_type_id + "','" + code + "','" + remarks + "','Pending')";
    }
}


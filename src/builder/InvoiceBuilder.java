package builder;

import model.Invoice;
import java.time.LocalDateTime;

/**
 *
 * @author Bihanga
 */
public class InvoiceBuilder {
    private String customer_id;
    private LocalDateTime date_time;
    private String user_id;
    private String uniq_id;
    private int order_through_id;
    private int order_type_id;
    private String code;
    private String remarks;

    public InvoiceBuilder setCustomerID(String customer_id) {
        this.customer_id = customer_id;
        return this;
    }

    public InvoiceBuilder setDateTime(LocalDateTime date_time) {
        this.date_time = date_time;
        return this;
    }

    public InvoiceBuilder setUserID(String user_id) {
        this.user_id = user_id;
        return this;
    }

    public InvoiceBuilder setUniqID(String uniq_id) {
        this.uniq_id = uniq_id;
        return this;
    }

    public InvoiceBuilder setOrderThroughID(int order_through_id) {
        this.order_through_id = order_through_id;
        return this;
    }

    public InvoiceBuilder setOrderTypeID(int order_type_id) {
        this.order_type_id = order_type_id;
        return this;
    }

    public InvoiceBuilder setCode(String code) {
        this.code = code;
        return this;
    }

    public InvoiceBuilder setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public Invoice build() {
        return new Invoice(customer_id, date_time, user_id, uniq_id, order_through_id, order_type_id, code, remarks);
    }
}


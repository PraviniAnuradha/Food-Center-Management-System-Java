/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package builder;

import model.InvoicePayment;

/**
 *
 * @author CHAMA COMPUTERS
 */
public class InvoicePaymentBuilder {
    private String invoice_id;
    private String payment_type_id;
    private String payment;

    public InvoicePaymentBuilder setInvoiceID(String invoice_id) {
        this.invoice_id = invoice_id;
        return this;
    }

    public InvoicePaymentBuilder setPaymentTypeID(String payment_type_id) {
        this.payment_type_id = payment_type_id;
        return this;
    }

    public InvoicePaymentBuilder setPayment(String payment) {
        this.payment = payment;
        return this;
    }

    public InvoicePayment build() {
        return new InvoicePayment(invoice_id, payment_type_id, payment);
    }
}


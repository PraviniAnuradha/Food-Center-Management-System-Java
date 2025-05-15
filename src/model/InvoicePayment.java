/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author CHAMA COMPUTERS
 */
public class InvoicePayment {
    private String invoice_id;
    private String payment_type_id;
    private String payment;

    public InvoicePayment(String invoice_id, String payment_type_id, String payment) {
        this.invoice_id = invoice_id;
        this.payment_type_id = payment_type_id;
        this.payment = payment;
    }

    public String toInsertQuery() {
        return "('" + invoice_id + "','" + payment_type_id + "','" + payment + "')";
    }
}

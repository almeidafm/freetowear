package com.freetowear.freetowear.dto.request.order;

import com.freetowear.freetowear.entity.Payment;
import jakarta.validation.constraints.NotNull;

public class FinishOrderRequest {

    @NotNull
    private Payment.PaymentMethod method;

    private Integer installments;

    public FinishOrderRequest() {}

    public FinishOrderRequest(Payment.PaymentMethod method, Integer installments) {
        this.method = method;
        this.installments = installments;
    }

    public Payment.PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(Payment.PaymentMethod method) {
        this.method = method;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }
}
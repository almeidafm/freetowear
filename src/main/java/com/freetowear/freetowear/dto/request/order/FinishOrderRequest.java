package com.freetowear.freetowear.dto.request.order;

import com.freetowear.freetowear.enums.PaymentMethod;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class FinishOrderRequest {

    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentMethod method;

    @Min(1)
    @Max(12)
    private Integer installments;

    public FinishOrderRequest() {}

    public FinishOrderRequest(PaymentMethod method, Integer installments) {
        this.method = method;
        this.installments = installments;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }
}
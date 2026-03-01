package com.legalbureau.entity.enums;

import lombok.Getter;

@Getter
public enum CaseStatus {
    NEW("Очікує розгляду"),
    ACCEPTED("Прийнято до розгляду"),
    IN_PROGRESS("В роботі"),
    COMPLETED("Завершено"),
    DECLINED("Відхилено");

    private final String displayValue;

    CaseStatus(String displayValue) {
        this.displayValue = displayValue;
    }
}

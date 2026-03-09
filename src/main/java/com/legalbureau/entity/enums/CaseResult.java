package com.legalbureau.entity.enums;

public enum CaseResult {
    WON("Виграно"),
    LOST("Програно"),
    SETTLED("Мирова угода");

    private final String displayValue;

    CaseResult(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
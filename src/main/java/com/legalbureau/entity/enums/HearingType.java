package com.legalbureau.entity.enums;

public enum HearingType {
    CONSULTATION("Консультація (1 год)"),
    COURT("Судове засідання (Весь день)");

    private final String displayValue;

    HearingType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
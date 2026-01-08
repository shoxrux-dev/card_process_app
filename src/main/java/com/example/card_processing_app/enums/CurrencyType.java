package com.example.card_processing_app.enums;

import lombok.Getter;

@Getter
public enum CurrencyType {
    UZS(0),
    USD(2);

    private final int scale;

    CurrencyType(int scale) {
        this.scale = scale;
    }

}

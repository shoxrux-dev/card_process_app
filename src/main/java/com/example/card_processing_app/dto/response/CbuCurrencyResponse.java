package com.example.card_processing_app.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CbuCurrencyResponse(
        @JsonProperty("Code")
        String code,

        @JsonProperty("Ccy")
        String ccy,

        @JsonProperty("CcyNm_EN")
        String ccyNameEn,

        @JsonProperty("Nominal")
        Integer nominal,

        @JsonProperty("Rate")
        BigDecimal rate,

        @JsonProperty("Date")
        String date
) {}

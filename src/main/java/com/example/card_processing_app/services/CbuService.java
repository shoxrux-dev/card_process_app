package com.example.card_processing_app.services;

import com.example.card_processing_app.dto.response.CbuCurrencyResponse;
import com.example.card_processing_app.enums.CurrencyType;
import com.example.card_processing_app.exception.InvalidCurrencyTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Log4j2
@Service
@RequiredArgsConstructor
public class CbuService {
    private final RestTemplate restTemplate;

    private static final String CBU_URL = "https://cbu.uz/uz/arkhiv-kursov-valyut/json/";

    public BigDecimal getExchangeRate(CurrencyType fromCurrency, CurrencyType toCurrency) {
        if (fromCurrency == toCurrency) return BigDecimal.ONE;

        BigDecimal fromRateInUzs = (fromCurrency == CurrencyType.UZS)
                ? BigDecimal.ONE
                : getRateInUzs(fromCurrency);

        BigDecimal toRateInUzs = (toCurrency == CurrencyType.UZS)
                ? BigDecimal.ONE
                : getRateInUzs(toCurrency);

        BigDecimal rate = fromRateInUzs.divide(toRateInUzs, 10, RoundingMode.HALF_UP);

        log.info("Cross-rate calculation: 1 {} = {} {}", fromCurrency, rate, toCurrency);
        return rate;
    }

    private BigDecimal getRateInUzs(CurrencyType currency) {
        log.info("Fetching {} rate in UZS from CBU", currency);

        try {
            ResponseEntity<CbuCurrencyResponse[]> response =
                    restTemplate.getForEntity(CBU_URL, CbuCurrencyResponse[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Failed to fetch currency rates from CBU");
                throw new IllegalStateException("Unable to fetch currency rates from CBU");
            }

            CbuCurrencyResponse[] rates = response.getBody();

            for (CbuCurrencyResponse rate : rates) {
                if (rate.ccy() != null && rate.ccy().equals(currency.name())) {
                    BigDecimal result = rate.rate()
                            .divide(BigDecimal.valueOf(rate.nominal()), 10, RoundingMode.HALF_UP);
                    log.info("CBU rate for {}: {}", currency, result);
                    return result;
                }
            }

            log.warn("Currency {} not found in CBU rates", currency);
            throw new InvalidCurrencyTypeException("Currency not found in CBU rates: " + currency);

        } catch (RestClientException e) {
            log.error("CBU service unavailable", e);
            throw new IllegalStateException("Currency service temporarily unavailable", e);
        }
    }
}

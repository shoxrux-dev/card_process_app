package com.example.card_processing_app.components;

import com.example.card_processing_app.services.TransactionPartitioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionPartitioningManager {
    private final TransactionPartitioningService transactionPartitioningService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        transactionPartitioningService.createPartitionIfNotExist(LocalDateTime.now());
        transactionPartitioningService.createNextMonthPartitionIfNotExist();
    }

    @Scheduled(cron = "0 0 5 * * *")
    public void dailyMaintenance() {
        transactionPartitioningService.createNextMonthPartitionIfNotExist();
    }
}

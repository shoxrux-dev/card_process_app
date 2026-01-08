package com.example.card_processing_app.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class TransactionPartitioningService {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createPartitionIfNotExist(LocalDateTime date) {
        String partitionName = String.format("transactions_%d_%02d",
                date.getYear(), date.getMonthValue());

        LocalDateTime startOfMonth = date.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        String sql = String.format(
                "CREATE TABLE IF NOT EXISTS %s PARTITION OF transactions " +
                        "FOR VALUES FROM ('%s') TO ('%s')",
                partitionName, startOfMonth, startOfNextMonth
        );

        try {
            jdbcTemplate.execute(sql);
            log.info("Partition created: {}", partitionName);
        } catch (Exception e) {
            log.error("Error creating partition: {}", partitionName, e);
        }
    }

    public void createNextMonthPartitionIfNotExist() {
        LocalDate nextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1);

        String partitionName = String.format("transactions_%d_%02d",
                nextMonth.getYear(), nextMonth.getMonthValue());

        LocalDate endOfNextMonth = nextMonth.plusMonths(1);

        String sql = String.format(
                "CREATE TABLE IF NOT EXISTS %s PARTITION OF transactions " +
                        "FOR VALUES FROM ('%s') TO ('%s')",
                partitionName, nextMonth, endOfNextMonth
        );

        try {
            jdbcTemplate.execute(sql);
            log.info("Partitioning successfully checked/created for: {}", partitionName);
        } catch (Exception e) {
            log.error("Critical error during partitioning: ", e);
        }
    }
}

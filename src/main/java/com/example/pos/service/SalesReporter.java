package com.example.pos.service;

import com.example.pos.model.SaleRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

public class SalesReporter {

    private final SaleFileHandler saleFileHandler;

    public SalesReporter(SaleFileHandler saleFileHandler) {
        this.saleFileHandler = saleFileHandler;
    }

    public List<SaleRecord> getAllSales() {
        return saleFileHandler.loadSales();
    }

    public List<SaleRecord> getSalesForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return getAllSales().stream()
                .filter(sale -> !sale.getTimestamp().isBefore(startOfDay) && !sale.getTimestamp().isAfter(endOfDay))
                .collect(Collectors.toList());
    }

    public List<SaleRecord> getSalesForCurrentDay() {
        return getSalesForDate(LocalDate.now());
    }

    public List<SaleRecord> getSalesForCurrentWeek() {
        LocalDate today = LocalDate.now();
        // Assuming week starts on Monday and ends on Sunday
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end = endOfWeek.atTime(LocalTime.MAX);

        return getAllSales().stream()
                .filter(sale -> !sale.getTimestamp().isBefore(start) && !sale.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<SaleRecord> getSalesForCurrentMonth() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = endOfMonth.atTime(LocalTime.MAX);

        return getAllSales().stream()
                .filter(sale -> !sale.getTimestamp().isBefore(start) && !sale.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    public SalesSummary generateSummary(List<SaleRecord> sales) {
        if (sales == null || sales.isEmpty()) {
            return new SalesSummary(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        int numberOfTransactions = sales.size();
        BigDecimal totalGrossSales = sales.stream()
                                       .map(SaleRecord::getTotalAmount)
                                       .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscounts = sales.stream()
                                      .map(SaleRecord::getDiscountAmount)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNetSales = sales.stream()
                                     .map(SaleRecord::getFinalAmount)
                                     .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SalesSummary(numberOfTransactions, totalGrossSales, totalDiscounts, totalNetSales);
    }

    // Inner class for Sales Summary structure
    public static class SalesSummary {
        private final int numberOfTransactions;
        private final BigDecimal totalGrossSales;
        private final BigDecimal totalDiscounts;
        private final BigDecimal totalNetSales;

        public SalesSummary(int numberOfTransactions, BigDecimal totalGrossSales, BigDecimal totalDiscounts, BigDecimal totalNetSales) {
            this.numberOfTransactions = numberOfTransactions;
            this.totalGrossSales = totalGrossSales;
            this.totalDiscounts = totalDiscounts;
            this.totalNetSales = totalNetSales;
        }

        public int getNumberOfTransactions() {
            return numberOfTransactions;
        }

        public BigDecimal getTotalGrossSales() {
            return totalGrossSales;
        }

        public BigDecimal getTotalDiscounts() {
            return totalDiscounts;
        }

        public BigDecimal getTotalNetSales() {
            return totalNetSales;
        }

        @Override
        public String toString() {
            return String.format(
                "Sales Summary:\n" +
                "  Number of Transactions: %d\n" +
                "  Total Gross Sales:    %.2f\n" +
                "  Total Discounts:      %.2f\n" +
                "  Total Net Sales:      %.2f",
                numberOfTransactions, totalGrossSales, totalDiscounts, totalNetSales
            );
        }
    }
}

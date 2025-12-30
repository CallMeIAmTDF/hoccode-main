package com.example.hoccode.service.template;

import com.example.hoccode.dto.payment.PaidPaymentReportRow;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
public class MonthlyRevenueReportTemplate {

    public String buildHtml(
            YearMonth month,
            Long totalRevenue,
            int totalTransactions,
            List<PaidPaymentReportRow> rows
    ) {

        String tableRows = rows.stream()
                .map(r -> """
                            <tr>
                                <td>%d</td>
                                <td>%s</td>
                                <td class="right">%s</td>
                                <td>%s</td>
                                <td>%s</td>
                            </tr>
                        """.formatted(
                        r.stt(),
                        r.email(),
                        formatMoney(r.amount()),
                        r.orderCode(),
                        r.paidAt().toLocalDate()
                ))
                .reduce("", String::concat);

        return """ 
                
                """.formatted(
                month,
                LocalDate.now(),
                formatMoney(totalRevenue),
                totalTransactions,
                formatMoney(totalTransactions == 0 ? 0 : totalRevenue / totalTransactions),
                tableRows,
                LocalDate.now()
        );
    }

    private String formatMoney(Long amount) {
        return String.format("%,d", amount);
    }
}
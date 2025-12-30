package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.dto.payment.MonthlyRevenueDTO;
import com.example.hoccode.dto.payment.PaidPaymentReportRow;
import com.example.hoccode.dto.payment.projection.MonthlyRevenueProjection;
import com.example.hoccode.dto.response.pdf.PdfFileInfo;
import com.example.hoccode.entity.Payment;
import com.example.hoccode.entity.PremiumUser;
import com.example.hoccode.repository.PaymentRepository;
import com.example.hoccode.repository.PremiumUserRepository;
import com.example.hoccode.service.IPaymentService;
import com.example.hoccode.service.IPdfRenderService;
import com.example.hoccode.service.template.MonthlyRevenueReportTemplate;
import com.example.hoccode.util.MoneyFormatter;
import com.example.hoccode.util.RandomStringUtil;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.PaymentStatus;
import com.example.hoccode.util.constant.SocketType;
import com.lowagie.text.pdf.BaseFont;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService implements IPaymentService {
    final PayOS payOS;
    final SecurityUtil securityUtil;
    final PaymentRepository paymentRepository;
    final PremiumUserRepository premiumUserRepository;
    final SimpMessagingTemplate webSocketTemplate;
    final MonthlyRevenueReportTemplate reportTemplate;
    final IPdfRenderService pdfRenderService;
    final TemplateEngine templateEngine;
    static final String PDF_DIRECTORY = "src/main/resources/assets/pdf_report/";

    @Value("${api.base-url}")
    String baseUrl;

    @Value("${api.path.payos}")
    String pathPayos;

    final static Integer AMOUNT = 10000;

    @Override
    @Transactional
    @Cacheable(
            value = "shortCache",
            key = "T(String).format('createPayment:%s', @securityUtil.getCurrentUserLogin().get())"
    )
    public CreatePaymentLinkResponse createPayment() throws Exception {
        PremiumUser premiumUser = premiumUserRepository.findFirstByEmail(securityUtil.getCurrentUserLogin().get());

        if (premiumUser != null) {
            throw new BadRequestException("Đã là tài khoản cao cấp");
        }

        long orderCode = System.currentTimeMillis();
        long expiredAt = Instant.now().getEpochSecond() + 20 * 60;

        String code = RandomStringUtil.generateRandomString(6);

        Payment payment = Payment.builder()
                .status(PaymentStatus.PENDING)
                .amount(AMOUNT)
                .orderCode(orderCode)
                .code(code)
                .build();

        CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .description("HocCode")
                .amount(Long.valueOf(AMOUNT))
                .expiredAt(expiredAt)
                .cancelUrl(baseUrl + pathPayos)
                .returnUrl(baseUrl + pathPayos)
                .build();

        paymentRepository.save(payment);
        return payOS.paymentRequests().create(paymentRequest);
    }

    @Override
    public WebhookData webhook(Webhook webhook) throws Exception {
        try {
            var data = payOS.webhooks().verify(webhook);
            Payment payment = paymentRepository.findFirstByOrderCode(data.getOrderCode()).orElseThrow(
                    () -> new BadRequestException("Mã không hợp lệ")
            );
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            premiumUserRepository.save(
                    PremiumUser.builder()
                            .email(payment.getCreatedBy())
                            .build()
            );

            webSocketTemplate.convertAndSendToUser(
                    payment.getCreatedBy(),
                    "/topic/payment",
                    new SocketMessage(SocketType.PREMIUM_USER, "Nâng cấp tài khoản thành công",
                            true)
            );

            return data;
        } catch (Exception e) {
            throw new BadRequestException("Webhook không hợp lệ");
        }
    }


    @Override
    public Map<String, Object> stats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate now = LocalDate.now();
        Long revenueThisMonth = paymentRepository.getRevenueByMonth(
                now.getYear(),
                now.getMonthValue()
        );
        stats.put("revenueThisMonth", revenueThisMonth);
        stats.put("totalRevenue", paymentRepository.getTotalRevenue());
        stats.put("last12MonthsRevenue", getLast12MonthsRevenue());
        stats.put("report", getReportFile());
        return stats;
    }

    private List<MonthlyRevenueDTO> getLast12MonthsRevenue() {

        List<MonthlyRevenueProjection> data =
                paymentRepository.getRevenueLast12Months();

        Map<YearMonth, Long> revenueMap = data.stream()
                .collect(Collectors.toMap(
                        p -> YearMonth.parse(p.getMonth()),
                        MonthlyRevenueProjection::getRevenue
                ));

        List<MonthlyRevenueDTO> result = new ArrayList<>();

        YearMonth current = YearMonth.now();

        for (int i = 11; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            result.add(new MonthlyRevenueDTO(
                    ym.toString(),
                    revenueMap.getOrDefault(ym, 0L)
            ));
        }

        return result;
    }


    private List<PaidPaymentReportRow> getPaidPaymentsLastMonth() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDateTime start = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime end = lastMonth.atEndOfMonth().atTime(23, 59, 59);
        List<Payment> payments = paymentRepository.findPaidBetween(start, end);
        AtomicInteger counter = new AtomicInteger(1);
        return payments.stream()
                .map(p -> new PaidPaymentReportRow(
                        counter.getAndIncrement(),
                        p.getCreatedBy(),
                        p.getAmount().longValue(),
                        String.valueOf(p.getOrderCode()),
                        p.getCreatedAt()
                ))
                .toList();
    }

        @Scheduled(cron = "0 0 0 1 * ?")
//    @Scheduled(cron = "0 56 23 * * ?")
    public void generateMonthlyReport() throws Exception {

        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        List<PaidPaymentReportRow> rows = getPaidPaymentsLastMonth();

        long totalRevenue = rows.stream()
                .mapToLong(PaidPaymentReportRow::amount)
                .sum();

        Map<String, Object> variables = new HashMap<>();

        variables.put("month", lastMonth.format(formatter));
        variables.put("createdDate", LocalDate.now());
        variables.put("totalRevenue", MoneyFormatter.format(totalRevenue));
        variables.put("totalTransactions", rows.size());
        variables.put(
                "avgRevenue",
                rows.isEmpty()
                        ? "0"
                        : MoneyFormatter.format(totalRevenue / rows.size())
        );
        variables.put("rows", rows);
        String html = renderHtml(
                "monthly-revenue-report",
                variables
        );
//        String outputFilePath = PDF_DIRECTORY + "reports/revenue-" + lastMonth + ".pdf";
        String outputFilePath = String.format(
                "%sreports/revenue-%02d-%d.pdf",
                PDF_DIRECTORY,
                lastMonth.getMonth().getValue(),
                lastMonth.getYear()
        );

        generatePdf(
                html,
                outputFilePath
        );
    }

    private String renderHtml(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    private void generatePdf(String htmlContent, String outputFilePath) throws Exception {
        File outputFile = new File(outputFilePath);
        File parentFolder = outputFile.getParentFile();

        if (parentFolder != null && !parentFolder.exists()) {
            parentFolder.mkdirs();
        }

        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            ITextRenderer renderer = new ITextRenderer();
            String fontPath = new ClassPathResource("/assets/font/DejaVuSans.ttf").getURL().toString();
            renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
        }
    }

    @Override
    public List<PdfFileInfo> getReportFile() {
        List<PdfFileInfo> pdfFiles = new ArrayList<>();
        try {
            Path path = Path.of(PDF_DIRECTORY + "reports/");
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".pdf"))
                    .forEach(p -> {
                        try {
                            long fileSize = Files.size(p);
                            String fileName = p.getFileName().toString();
                            String lastModified = fileName.replace("pvp_rank_report_", "").replace(".pdf", "");
                            pdfFiles.add(new PdfFileInfo(
                                    fileName, fileSize, lastModified));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfFiles;
    }

    @Override
    public byte[] downloadReports(String fileName) {
        Path filePath = Paths.get(PDF_DIRECTORY, "reports", fileName);

        if (!Files.exists(filePath)) {
            return null;
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file report: " + fileName, e);
        }
    }


    public void generateMonthlyReportForMonth(int month) throws Exception {

        int currentYear = Year.now().getValue();

        // Validate: chỉ cho phép tháng 7 -> 12
        if (month < 7 || month > 12) {
            throw new IllegalArgumentException("Month must be between 7 and 12");
        }

        YearMonth yearMonth = YearMonth.of(currentYear, month);

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Payment> payments = paymentRepository.findPaidBetween(start, end);

        AtomicInteger counter = new AtomicInteger(1);

        List<PaidPaymentReportRow> rows = payments.stream()
                .map(p -> new PaidPaymentReportRow(
                        counter.getAndIncrement(),
                        p.getCreatedBy(),
                        p.getAmount().longValue(),
                        String.valueOf(p.getOrderCode()),
                        p.getCreatedAt()
                ))
                .toList();

        long totalRevenue = rows.stream()
                .mapToLong(PaidPaymentReportRow::amount)
                .sum();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        Map<String, Object> variables = new HashMap<>();
        variables.put("month", yearMonth.format(formatter));
        variables.put(
                "createdDate",
                YearMonth.of(currentYear, month)
                        .plusMonths(1)
                        .atDay(1)
        );
        variables.put("totalRevenue", MoneyFormatter.format(totalRevenue));
        variables.put("totalTransactions", rows.size());
        variables.put(
                "avgRevenue",
                rows.isEmpty()
                        ? "0"
                        : MoneyFormatter.format(totalRevenue / rows.size())
        );
        variables.put("rows", rows);

        String html = renderHtml(
                "monthly-revenue-report",
                variables
        );

        String outputFilePath = String.format(
                "%sreports/revenue-%02d-%d.pdf",
                PDF_DIRECTORY,
                month,
                currentYear
        );

        generatePdf(html, outputFilePath);
    }

    @Override
    public void genPdf() throws Exception {
        for (int month = 7; month <= 12; month++) {
            generateMonthlyReportForMonth(month);
        }
    }
}

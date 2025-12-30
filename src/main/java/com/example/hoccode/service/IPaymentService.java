package com.example.hoccode.service;

import com.example.hoccode.dto.response.pdf.PdfFileInfo;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IPaymentService {
    CreatePaymentLinkResponse createPayment() throws Exception;

    WebhookData webhook(Webhook webhook) throws Exception;

    Map<String, Object> stats();

    List<PdfFileInfo> getReportFile();

    byte[] downloadReports(String fileName);

    void genPdf() throws Exception;
}

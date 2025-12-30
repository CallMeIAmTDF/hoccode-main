package com.example.hoccode.service.impl;

import com.example.hoccode.service.IPdfRenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class PdfRenderService implements IPdfRenderService {

    @Override
    public void renderPdf(String html, Path outputPath) {

        try (OutputStream os = Files.newOutputStream(outputPath)) {

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);

        } catch (Exception e) {
            throw new RuntimeException("Render PDF failed", e);
        }
    }
}
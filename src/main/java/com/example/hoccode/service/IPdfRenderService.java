package com.example.hoccode.service;

import java.nio.file.Path;

public interface IPdfRenderService {
    void renderPdf(String html, Path outputPath);
}

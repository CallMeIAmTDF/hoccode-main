package com.example.hoccode.service;

public interface ISensitiveWordService {
    boolean contains(String text);
    String findMatchWord(String text);
}
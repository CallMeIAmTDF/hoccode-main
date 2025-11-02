package com.example.hoccode.service.impl;

import com.example.hoccode.service.ISensitiveWordService;
import com.example.hoccode.util.SensitiveWordUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@Service
public class SensitiveWordServiceImpl implements ISensitiveWordService {
    private List<String> sensitiveWords;
    private SensitiveWordUtil util;

    @PostConstruct
    private void loadData() {
        Gson gson = new Gson();
        try (Reader reader = new FileReader("src/main/resources/assets/blacklist.json")) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            sensitiveWords = gson.fromJson(reader, listType);
            util = new SensitiveWordUtil(sensitiveWords);
        } catch (IOException e) {
            e.printStackTrace();
            sensitiveWords = Collections.emptyList();
            util = new SensitiveWordUtil(sensitiveWords);
        }
    }

    @Override
    public boolean contains(String text) {
        return util.containsSensitive(text);
    }

    @Override
    public String findMatchWord(String text){
        return util.findFirstSensitiveWord(text);
    }

}
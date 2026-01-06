package com.example.hoccode.service.impl;

import com.example.hoccode.service.ISensitiveWordService;
import com.example.hoccode.util.SensitiveWordUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class SensitiveWordServiceImpl implements ISensitiveWordService {
    private List<String> sensitiveWords;
    private SensitiveWordUtil util;

    final ResourceLoader resourceLoader;

    public SensitiveWordServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void loadData() {
        Gson gson = new Gson();
        try{
            Resource resource = resourceLoader.getResource("classpath:assets/blacklist.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);

            Type listType = new TypeToken<List<String>>() {}.getType();
            sensitiveWords = gson.fromJson(reader, listType);
            System.out.println("Load Sensitive Words: " + sensitiveWords.size() + " words.");
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
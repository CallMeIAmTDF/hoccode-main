package com.example.hoccode.service;

import com.example.hoccode.dto.response.SimpInfoUserResponse;

import java.util.List;


public interface IAuthService {
	public List<SimpInfoUserResponse> getUserByIds(List<String> ids);
}
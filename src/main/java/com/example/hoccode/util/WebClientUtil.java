package com.example.hoccode.util;

import com.example.hoccode.advice.exception.BadRequestException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebClientUtil {
    public static <T> T safeCall(Supplier<Mono<T>> call, String errorMessage) {
        try {
            return call.get().block();
        } catch (WebClientResponseException e) {
            log.error("WebClient error: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadRequestException("Lỗi hệ thống, vui lòng thử lại sau");
        } catch (Exception e) {
            log.error("Unexpected error during WebClient call", e);
            throw new BadRequestException("Lỗi hệ thống, vui lòng thử lại sau");
        }
    }
}
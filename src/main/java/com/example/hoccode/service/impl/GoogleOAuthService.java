package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.google.ExchangeTokenResponse;
import com.example.hoccode.dto.google.GoogleUserResponse;
import com.example.hoccode.dto.response.AuthenticationResponse;
import com.example.hoccode.entity.User;
import com.example.hoccode.repository.UserRepository;
import com.example.hoccode.service.AuthenticationService;
import com.example.hoccode.service.IGoogleOAuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoogleOAuthService implements IGoogleOAuthService {

    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;
    private final AuthenticationService authenticationService;

    @Value("${auth.outbound.identity.client-id}")
    private String clientId;

    @Value("${auth.outbound.identity.client-secret}")
    private String clientSecret;

    @Value("${auth.outbound.identity.redirect-uri}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";

    @Override
    public ExchangeTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        return webClientBuilder.build()
                .post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(ExchangeTokenResponse.class)
                .block();
    }

    @Override
    public GoogleUserResponse getUserInfo(String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri(uri -> uri
                        .path(USERINFO_URL)
                        .queryParam("alt", "json")
                        .queryParam("access_token", accessToken)
                        .build()
                )
                .retrieve()
                .bodyToMono(GoogleUserResponse.class)
                .block();
    }

    @Override
    public User getOrCreateUser(GoogleUserResponse googleUser) {
        return userRepository.findByEmail(googleUser.getEmail())
                .orElseGet(() -> {
                    User u = User.builder()
                            .name(googleUser.getName())
                            .email(googleUser.getEmail())
                            .avatar(googleUser.getPicture())
                            .password(null)
//                            .avatar(googleUser.getPicture())
//                            .provider("GOOGLE")
                            .active(true)
                            .isLocked(false)
                            .role("USER")
                            .build();
                    return userRepository.save(u);
                });
    }

//    @Override
//    public AuthenticationResponse handleGoogleLogin(String code) {
//        System.out.println("code 1: " + code);
//
//        var tokenResponse = exchangeCodeForToken(code);
//        System.out.println("code 2: " + code);
//        var googleUser = getUserInfo(tokenResponse.getAccessToken());
//        System.out.println("code 3: " + code);
//        var user = getOrCreateUser(googleUser);
//
//        Authentication auth = buildAuthentication(user);
//
//        SecurityContextHolder.getContext().setAuthentication(auth);
//        return authenticationService.createAuthenticationResponse(user);
//    }

    private Authentication buildAuthentication(User user) {
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        return new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                authorities
        );
    }

    @Override
    public AuthenticationResponse handleGoogleLogin(String idTokenString) {

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new BadRequestException("Google token không hợp lệ");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // tạo hoặc lấy user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .email(email)
                                    .name(name)
                                    .avatar(picture)
                                    .active(true)
                                    .isLocked(false)
                                    .role("USER")
                                    .build()
                    ));

            // tạo authentication
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            // trả JWT cho FE
            return authenticationService.createAuthenticationResponse(user);

        } catch (Exception e) {
            throw new BadRequestException("Không verify được Google token: " + e.getMessage());
        }
    }
}

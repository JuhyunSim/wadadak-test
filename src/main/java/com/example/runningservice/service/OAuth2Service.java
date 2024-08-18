package com.example.runningservice.service;


import com.example.runningservice.dto.googleToken.GoogleAccessTokenRequestDto;
import com.example.runningservice.dto.googleToken.GoogleAccessTokenResponseDto;
import com.example.runningservice.dto.googleToken.GoogleAccountProfileResponseDto;
import com.example.runningservice.util.AESUtil;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.security.auth.login.LoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OAuth2Service {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
    private String authorizationCode;
    @Value("${url.access-token}")
    private String accessTokenUrl;
    @Value("${url.profile}")
    private String profileUrl;

    private final RestTemplate restTemplate;
    private final AESUtil aesUtil;

    public GoogleAccountProfileResponseDto getGoogleAccountProfile(final String code) throws LoginException, Exception {
        final String accessToken = requestGoogleAccessToken(code);
        return requestGoogleAccountProfile(accessToken);
    }

    private String requestGoogleAccessToken(final String code) throws LoginException, Exception {
        final String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        final HttpEntity<GoogleAccessTokenRequestDto> httpEntity = new HttpEntity<>(
            GoogleAccessTokenRequestDto.builder()
                .client_id(aesUtil.decrypt(clientId))
                .client_secret(aesUtil.decrypt(clientSecret))
                .code(decodedCode)
                .redirect_uri(redirectUri)
                .grant_type(authorizationCode)
                .build(),
            headers
        );
        final GoogleAccessTokenResponseDto response = restTemplate.exchange(
            accessTokenUrl, HttpMethod.POST, httpEntity, GoogleAccessTokenResponseDto.class
        ).getBody();
        return Optional.ofNullable(response)
            .orElseThrow(() -> new LoginException("구글로그인을 찾을 수 없습니다."))
            .getAccess_token();
    }

    private GoogleAccountProfileResponseDto requestGoogleAccountProfile(final String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        final HttpEntity<GoogleAccessTokenRequestDto> httpEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(profileUrl, HttpMethod.GET, httpEntity, GoogleAccountProfileResponseDto.class)
            .getBody();
    }
}

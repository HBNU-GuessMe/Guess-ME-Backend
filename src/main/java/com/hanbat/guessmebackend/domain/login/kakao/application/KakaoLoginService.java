package com.hanbat.guessmebackend.domain.login.kakao.application;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanbat.guessmebackend.domain.login.kakao.dto.KakaoToken;
import com.hanbat.guessmebackend.domain.login.kakao.dto.KakaoUserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoLoginService {

	private final String reqURL = "https://kauth.kakao.com/oauth/token";
	private final String userInfoURL = "https://kapi.kakao.com/v2/user/me";

	@Value("${oauth2.provider.kakao.grant-type}")
	private String grantType;

	@Value("${oauth2.provider.kakao.client-id}")
	private String clientId;

	@Value("${oauth2.provider.kakao.redirect-url}")
	private String redirectUrl;

	/**
	 * 인가코드를 카카오에 요청해 엑세스 토큰을 받는 함수
	 */
	public KakaoToken getAccessToken(String code) throws IOException {
		// 파라미터 세팅
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", clientId);
		params.add("grant_type", grantType);
		params.add("code", code);
		params.add("redirect_uri", redirectUrl);
		params.add("client_secret", clientId);

		// AccessToken POST 요청
		WebClient webClient = WebClient.create(reqURL);
		String response = webClient.post()
			.uri(reqURL)
			.body(BodyInserters.fromFormData(params))
			.header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
			.retrieve()
			.bodyToMono(String.class)
			.block();

		log.info(response);

		// json 형태로 변환
		ObjectMapper mapper = new ObjectMapper();
		KakaoToken kakaoToken = null;

		try {

			kakaoToken = mapper.readValue(response, KakaoToken.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return kakaoToken;

	}

}
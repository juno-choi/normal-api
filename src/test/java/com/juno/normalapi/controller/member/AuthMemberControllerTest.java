package com.juno.normalapi.controller.member;

import com.fasterxml.jackson.core.type.TypeReference;
import com.juno.normalapi.api.Response;
import com.juno.normalapi.config.TestSupport;
import com.juno.normalapi.domain.dto.member.JoinMemberDto;
import com.juno.normalapi.domain.entity.member.Member;
import com.juno.normalapi.domain.enums.JoinType;
import com.juno.normalapi.domain.vo.member.LoginMemberVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class AuthMemberControllerTest extends TestSupport {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final String URL = "/auth/member";

    @Test
    @DisplayName("회원 가입 성공")
    void joinMemberSuccess() throws Exception {
        JoinMemberDto joinMemberDto = JoinMemberDto.builder()
                .email("test@naver.com")
                .password("test123!")
                .name("테스터")
                .nickname("닉네임")
                .tel("01012341234")
                .zipCode("12345")
                .address("경기도 성남시 중원구 자혜로17번길 16")
                .addressDetail("상세 주소")
                .build();

        ResultActions perform = mock.perform(
                post(URL + "/join").contentType(MediaType.APPLICATION_JSON)
                .content(convertToString(joinMemberDto))
        );
        perform.andDo(print());
    }

    @Test
    @DisplayName("로그인 성공")
    void loginMemberSuccess1() throws Exception {
        String email = "test@test.com";
        String password = "password";
        Map<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("password", password);

        Member member = Member.builder()
                .email(email)
                .password(password)
                .name("tester")
                .nickname("테스터")
                .tel("01012341234")
                .role("USER")
                .build();
        member.encryptPassword(member, passwordEncoder);
        Member saveMember = memberRepository.save(member);

        ResultActions perform = mock.perform(
                post(URL + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content(convertToString(map))
        ).andDo(print());

        String contentAsString = perform.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Response<LoginMemberVo> response = objectMapper.readValue(contentAsString, new TypeReference<Response<LoginMemberVo>>() {});
        LoginMemberVo loginMemberVo = response.getData();

        String accessToken = (String) redisTemplate.opsForHash().get(loginMemberVo.getAccessToken(), "access_token");
        String refreshToken = (String) redisTemplate.opsForHash().get(loginMemberVo.getRefreshToken(), "refresh_token");

        assertThat(saveMember.getId()).isEqualTo(Long.valueOf(accessToken));
        assertThat(saveMember.getId()).isEqualTo(Long.valueOf(refreshToken));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void refreshSuccess() throws Exception {
        //given
        JoinMemberDto joinMemberDto = JoinMemberDto.builder()
                .email("refresh_temp@mail.com")
                .password("test123!")
                .name("테스터")
                .nickname("닉네임")
                .tel("01012341234")
                .zipCode("12345")
                .address("경기도 성남시 중원구 자혜로17번길 16")
                .addressDetail("상세 주소")
                .build();
        Member member = memberRepository.save(Member.of(joinMemberDto, JoinType.EMAIL));

        String token = "refresh_temp_token";
        redisTemplate.opsForHash().put(token, "refresh_token", String.valueOf(member.getId()));

        //when
        ResultActions perform = mock.perform(
                get(URL + "/refresh/{token}", token).contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());
        //then
        String contentAsString = perform.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(contentAsString).contains("토큰 재발급 성공");
    }

    @Test
    @DisplayName("토큰 재발급 실패")
    void refreshFail1() throws Exception {
        //given
        //when
        ResultActions perform = mock.perform(
                get(URL + "/refresh/{token}", "invalid_token").contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());
        //then
        String contentAsString = perform.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(contentAsString).contains("토큰 값이 유효하지 않습니다.");
    }

}
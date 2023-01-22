package com.juno.normalapi.controller;

import com.juno.normalapi.docs.DocsSupport;
import com.juno.normalapi.domain.dto.RequestJoinMember;
import com.juno.normalapi.domain.dto.RequestLoginMember;
import com.juno.normalapi.domain.entity.Member;
import com.juno.normalapi.domain.enums.JoinType;
import com.juno.normalapi.repository.MemberRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberControllerDocs extends DocsSupport {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final String URL = "/v1/member";
    private final String EMAIL = "docs@email.com";
    private final String PASSWORD = "test123!";

    @BeforeAll
    void setUp(){
        RequestJoinMember requestJoinMember = RequestJoinMember.builder()
                .email(EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .name("테스터")
                .nickname("닉네임")
                .tel("01012341234")
                .zipCode("12345")
                .address("경기도 성남시 중원구")
                .addressDetail("상세 주소")
                .build();
        memberRepository.save(Member.of(requestJoinMember, JoinType.EMAIL));
    }

    @Test
    @DisplayName(URL+"/join")
    void joinMember() throws Exception {
        RequestJoinMember requestJoinMember = RequestJoinMember.builder()
                .email("docs1@email.com")
                .password(PASSWORD)
                .name("테스터")
                .nickname("닉네임")
                .tel("01012341234")
                .zipCode("12345")
                .address("경기도 성남시 중원구")
                .addressDetail("상세 주소")
                .build();

        ResultActions perform = mock.perform(
                post(URL + "/join").contentType(MediaType.APPLICATION_JSON)
                .content(convertToString(requestJoinMember))
        );

        perform.andDo(docs.document(
            requestFields(
                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                fieldWithPath("tel").type(JsonFieldType.STRING).description("전화번호"),
                fieldWithPath("zip_code").type(JsonFieldType.STRING).description("우편번호"),
                fieldWithPath("address").type(JsonFieldType.STRING).description("주소"),
                fieldWithPath("address_detail").type(JsonFieldType.STRING).description("상세주소")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.member_id").type(JsonFieldType.NUMBER).description("회원 번호"),
                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("이름"),
                fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("닉네임"),
                fieldWithPath("data.tel").type(JsonFieldType.STRING).description("전화번호"),
                fieldWithPath("data.zip_code").type(JsonFieldType.STRING).description("우편번호"),
                fieldWithPath("data.address").type(JsonFieldType.STRING).description("주소"),
                fieldWithPath("data.address_detail").type(JsonFieldType.STRING).description("상세주소"),
                fieldWithPath("data.role").type(JsonFieldType.STRING).description("회원 권한")
            )
        ));
    }

    @Test
    @DisplayName(URL+"/login")
    void loginMember() throws Exception {
        RequestLoginMember requestLoginMember = RequestLoginMember.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        ResultActions perform = mock.perform(
                post(URL + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content(convertToString(requestLoginMember))
        );

        String contentAsString = perform.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(contentAsString);

        perform.andDo(docs.document(
                requestFields(
                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.access_token").type(JsonFieldType.STRING).description("access token 1시간"),
                        fieldWithPath("data.refresh_token").type(JsonFieldType.STRING).description("refresh token 30일")
                )
        ));
    }
}
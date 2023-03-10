package com.juno.normalapi.controller.member;

import com.juno.normalapi.docs.DocsSupport;
import com.juno.normalapi.domain.dto.RequestJoinMember;
import com.juno.normalapi.domain.dto.RequestLoginMember;
import com.juno.normalapi.domain.entity.Member;
import com.juno.normalapi.domain.enums.JoinType;
import com.juno.normalapi.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberControllerDocs extends DocsSupport {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisTemplate<String, ?> redisTemplate;

    private final String URL = "/auth/member";
    private final String EMAIL = "docs@email.com";
    private final String PASSWORD = "test123!";

    @BeforeAll
    void setUp(){
        RequestJoinMember requestJoinMember = RequestJoinMember.builder()
                .email(EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .name("?????????")
                .nickname("?????????")
                .tel("01012341234")
                .zipCode("12345")
                .address("????????? ????????? ?????????")
                .addressDetail("?????? ??????")
                .build();
        memberRepository.save(Member.of(requestJoinMember, JoinType.EMAIL));
    }

    @Test
    @DisplayName(URL+"/join")
    void join() throws Exception {
        RequestJoinMember requestJoinMember = RequestJoinMember.builder()
                .email("docs1@email.com")
                .password(PASSWORD)
                .name("?????????")
                .nickname("?????????")
                .tel("01012341234")
                .zipCode("12345")
                .address("????????? ????????? ?????????")
                .addressDetail("?????? ??????")
                .build();

        ResultActions perform = mock.perform(
                post(URL + "/join").contentType(MediaType.APPLICATION_JSON)
                .content(convertToString(requestJoinMember))
        );

        perform.andDo(docs.document(
            requestFields(
                fieldWithPath("email").type(JsonFieldType.STRING).description("?????????"),
                fieldWithPath("password").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("name").type(JsonFieldType.STRING).description("??????"),
                fieldWithPath("nickname").type(JsonFieldType.STRING).description("?????????"),
                fieldWithPath("tel").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("zip_code").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("address").type(JsonFieldType.STRING).description("??????"),
                fieldWithPath("address_detail").type(JsonFieldType.STRING).description("????????????")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("?????? ??????"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                fieldWithPath("data.member_id").type(JsonFieldType.NUMBER).description("?????? ??????"),
                fieldWithPath("data.email").type(JsonFieldType.STRING).description("?????????"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("??????"),
                fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("?????????"),
                fieldWithPath("data.tel").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("data.zip_code").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("data.address").type(JsonFieldType.STRING).description("??????"),
                fieldWithPath("data.address_detail").type(JsonFieldType.STRING).description("????????????"),
                fieldWithPath("data.role").type(JsonFieldType.STRING).description("?????? ??????")
            )
        ));
    }

    @Test
    @DisplayName(URL+"/login")
    void login() throws Exception {
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
                        fieldWithPath("email").type(JsonFieldType.STRING).description("?????????"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("????????????")
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("?????? ??????"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                        fieldWithPath("data.access_token").type(JsonFieldType.STRING).description("access token 1??????"),
                        fieldWithPath("data.refresh_token").type(JsonFieldType.STRING).description("refresh token 30???"),
                        fieldWithPath("data.access_token_expiration").type(JsonFieldType.NUMBER).description("access token ????????? 1??????"),
                        fieldWithPath("data.refresh_token_expiration").type(JsonFieldType.NUMBER).description("refresh token ????????? 30???")
                )
        ));
    }


    @Test
    @DisplayName(URL+"/refresh")
    void refresh() throws Exception {
        //given
        RequestJoinMember requestJoinMember = RequestJoinMember.builder()
                .email("refresh@mail.com")
                .password("test123!")
                .name("?????????")
                .nickname("?????????")
                .tel("01012341234")
                .zipCode("12345")
                .address("????????? ????????? ????????? ?????????17?????? 16")
                .addressDetail("?????? ??????")
                .build();
        Member member = memberRepository.save(Member.of(requestJoinMember, JoinType.EMAIL));

        String token = "refresh_token";
        redisTemplate.opsForHash().put(token, "refresh_token", String.valueOf(member.getMemberId()));

        //when
        ResultActions perform = mock.perform(
                RestDocumentationRequestBuilders.get(URL + "/refresh/{token}", token).contentType(MediaType.APPLICATION_JSON)
        );
        //then
        perform.andDo(docs.document(
            pathParameters(
                parameterWithName("token").description("refresh token")
            ),
            responseFields(
                    fieldWithPath("code").type(JsonFieldType.STRING).description("?????? ??????"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                    fieldWithPath("data.access_token").type(JsonFieldType.STRING).description("access token 1?????? (?????????)"),
                    fieldWithPath("data.access_token_expiration").type(JsonFieldType.NUMBER).description("access token ????????? 1??????")
            )
        ));
    }
}
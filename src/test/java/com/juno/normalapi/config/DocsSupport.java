package com.juno.normalapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juno.normalapi.security.AuthFilter;
import com.juno.normalapi.security.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@Disabled
@Import(RestdocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
public class DocsSupport extends TestSupport{
    @Autowired
    protected RestDocumentationResultHandler docs;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthUtil authUtil;

    @BeforeEach
    void setUp(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) throws Exception {
        this.mock = MockMvcBuilders.webAppContextSetup(context)
                .apply(
                        MockMvcRestDocumentation.documentationConfiguration(provider)
                                .uris()
                                .withScheme("http")
                                .withHost("127.0.0.1")
                                .withPort(80)
                )  // rest docs 설정 주입
                .alwaysDo(MockMvcResultHandlers.print()) // andDo(print()) 코드 포함 -> 3번 문제 해결
                .alwaysDo(docs) // pretty 패턴과 문서 디렉토리 명 정해준것 적용
                .addFilters(
                        new CharacterEncodingFilter("UTF-8", true), // 한글 깨짐 방지
                        getAuthFilter() // 로그인 인증
                )
                .build();
    }

    private AuthFilter getAuthFilter() throws Exception {
        AuthFilter authFilter = new AuthFilter(objectMapper, authUtil);
        authFilter.setAuthenticationManager(authenticationManager());
        authFilter.setFilterProcessesUrl("/auth/member/login");
        return authFilter;
    }
}

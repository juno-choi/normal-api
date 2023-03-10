package com.juno.normalapi.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juno.normalapi.security.AuthFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
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
    private Environment env;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, ?> redisTemplate;

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
                )  // rest docs ?????? ??????
                .alwaysDo(MockMvcResultHandlers.print()) // andDo(print()) ?????? ?????? -> 3??? ?????? ??????
                .alwaysDo(docs) // pretty ????????? ?????? ???????????? ??? ???????????? ??????
                .addFilters(
                        new CharacterEncodingFilter("UTF-8", true), // ?????? ?????? ??????
                        getAuthFilter() // ????????? ??????
                )
                .build();
    }

    private AuthFilter getAuthFilter() throws Exception {
        AuthFilter authFilter = new AuthFilter(env, objectMapper, redisTemplate);
        authFilter.setAuthenticationManager(authenticationManager());
        authFilter.setFilterProcessesUrl("/auth/member/login");
        return authFilter;
    }
}

package com.juno.normalapi.config;

import com.juno.normalapi.annotation.V1;
import com.juno.normalapi.interceptor.TokenInterceptor;
import com.juno.normalapi.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final Environment env;
    private final RedisTemplate redisTemplate;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenInterceptor(env, redisTemplate, memberRepository, passwordEncoder))
                .order(1)
                .addPathPatterns("/v1/**")
                //.excludePathPatterns()
        ;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true)   // /v1/test == /v1/test/
                .addPathPrefix("/v1", HandlerTypePredicate.forAnnotation(V1.class));
    }
}

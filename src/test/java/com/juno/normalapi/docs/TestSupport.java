package com.juno.normalapi.docs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juno.normalapi.domain.dto.RequestJoinMember;
import com.juno.normalapi.domain.entity.Member;
import com.juno.normalapi.domain.enums.JoinType;
import com.juno.normalapi.repository.member.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Disabled
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestSupport extends WebSecurityConfigurerAdapter {
    protected String accessToken = "Bearer ";

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mock;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

    @Autowired
    protected Environment env;

    @BeforeAll
    @Transactional
    void beforeAll(){
        String email = env.getProperty("normal.test.email");
        if(memberRepository.findByEmail(email).isPresent()){
            return ;
        }

        RequestJoinMember requestJoinMember = RequestJoinMember.builder()
                .email(email)
                .password("test123!")
                .name("?????????")
                .nickname("?????????")
                .tel("01012341234")
                .zipCode("12345")
                .address("????????? ????????? ????????? ?????????17?????? 16")
                .addressDetail("?????? ??????")
                .build();

        Member member = Member.of(requestJoinMember, JoinType.EMAIL);
        Member saveMember = memberRepository.save(member);

        String memberId = String.valueOf(saveMember.getMemberId());

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        authorities.add(new SimpleGrantedAuthority("MANAGER"));
        authorities.add(new SimpleGrantedAuthority("ADMIN"));

        // jwt ?????? ??????
        String accessToken = Jwts.builder()
                .setSubject(memberId)
                .claim("roles", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))    //????????? ??????????????? ????????? ??????
                .compact();

        this.accessToken += accessToken;

        HashOperations<String, Object, Object> opsHash = redisTemplate.opsForHash();
        opsHash.put(accessToken, "access_token", memberId);
    }

    protected String convertToString(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }
}

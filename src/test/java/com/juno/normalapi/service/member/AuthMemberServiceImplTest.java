package com.juno.normalapi.service.member;

import com.juno.normalapi.domain.dto.member.JoinMemberDto;
import com.juno.normalapi.domain.entity.member.Member;
import com.juno.normalapi.domain.vo.member.JoinMemberVo;
import com.juno.normalapi.domain.vo.member.LoginMemberVo;
import com.juno.normalapi.repository.member.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
class AuthMemberServiceImplTest {
    @Autowired
    private AuthMemberService authMemberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RedisTemplate<String, ?> redisTemplate;

    @Test
    @DisplayName("회원가입 성공")
    void joinSuccess1(){
        // given
        JoinMemberDto joinMemberDto = JoinMemberDto.builder()
                .email("test2@naver.com")
                .password("test123!")
                .name("테스터")
                .nickname("닉네임")
                .tel("01012341234")
                .zipCode("12345")
                .address("경기도 성남시 중원구 자혜로17번길 16")
                .addressDetail("상세 주소")
               .build();
        // when
        JoinMemberVo joinMemberVo = authMemberService.join(joinMemberDto);

        // then
        List<Member> all = memberRepository.findAll();
        Assertions.assertThat(all.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("유효하지 않은 토큰은 실패한다.")
    void refreshFail1(){
        // given & when
        Throwable throwable = catchThrowable(() -> authMemberService.refresh("invalid_token"));

        // then
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("토큰 값이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("유효하지 않은 회원은 실패한다.")
    void refreshFail2(){
        // given
        String token = "temp_token";
        redisTemplate.opsForHash().put(token, "refresh_token", "0");

        // when
        Throwable throwable = catchThrowable(() -> authMemberService.refresh(token));

        // then
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 회원입니다. 관리자에게 문의해주세요!");
    }

    @Test
    @DisplayName("토큰 재발급에 성공한다.")
    void refreshSuccess1(){
        // given
        JoinMemberDto joinMemberDto = JoinMemberDto.builder()
                .email("refresh@naver.com")
                .password("test123!")
                .name("테스터")
                .nickname("닉네임")
                .tel("01012341234")
                .zipCode("12345")
                .address("경기도 성남시 중원구 자혜로17번길 16")
                .addressDetail("상세 주소")
                .build();
        JoinMemberVo joinMemberVo = authMemberService.join(joinMemberDto);

        String token = "token";
        redisTemplate.opsForHash().put(token, "refresh_token", String.valueOf(joinMemberVo.getMemberId()));

        // when
        LoginMemberVo loginMemberVo = authMemberService.refresh(token);

        // then
        assertThat(loginMemberVo.getAccessToken()).isNotNull();
    }
}
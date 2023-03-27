package com.juno.normalapi.service.board;

import com.juno.normalapi.domain.dto.RequestBoard;
import com.juno.normalapi.domain.dto.RequestReply;
import com.juno.normalapi.domain.entity.Member;
import com.juno.normalapi.exception.UnauthorizedException;
import com.juno.normalapi.repository.board.BoardRepository;
import com.juno.normalapi.repository.board.ReplyRepository;
import com.juno.normalapi.repository.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplUnitTest {
    @InjectMocks
    private BoardServiceImpl boardService;

    @Mock
    private Environment env;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ReplyRepository replyRepository;

    private MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    @DisplayName("게시글 등록에 실패한다.")
    void postBoardFail1() {
        // given
        RequestBoard requestBoard = RequestBoard.builder()
                .title("제목")
                .content("내용")
                .build();

        given(env.getProperty(anyString())).willReturn("loginMemberId");

        // when
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> boardService.postBoard(requestBoard, request));

        // then
        assertEquals("잘못된 접근입니다.", ex.getMessage());
    }
    
    @Test
    @DisplayName("유효하지 않은 회원은 댓글 등록에 실패한다")
    void postReplyFail1(){
        // given
        given(env.getProperty(anyString())).willReturn("loginMemberId");
        // when
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> boardService.postReply(
                RequestReply.builder()
                        .boardId(1L)
                        .content("테스트")
                        .build(),
                request
        ));
        // then
        assertEquals("잘못된 접근입니다.", ex.getMessage());
    }

    @Test
    @DisplayName("유효하지 게시판은 댓글 등록에 실패한다")
    void postReplyFail2(){
        // given
        given(env.getProperty(anyString())).willReturn("loginMemberId");
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(Member.builder().build()));
        request.setAttribute("loginMemberId", 1L);

        // when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> boardService.postReply(
                RequestReply.builder()
                        .boardId(1L)
                        .content("테스트")
                        .build(),
                request
        ));

        // then
        assertEquals("유효하지 않은 게시판입니다.", ex.getMessage());
    }
}
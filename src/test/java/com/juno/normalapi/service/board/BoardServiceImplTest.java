package com.juno.normalapi.service.board;

import com.juno.normalapi.domain.dto.RequestBoard;
import com.juno.normalapi.domain.entity.Board;
import com.juno.normalapi.domain.entity.Reply;
import com.juno.normalapi.domain.vo.BoardListVo;
import com.juno.normalapi.domain.vo.BoardVo;
import com.juno.normalapi.repository.board.BoardRepository;
import com.juno.normalapi.repository.board.ReplyRepository;
import com.juno.normalapi.service.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BoardServiceImplTest extends ServiceTestSupport {
    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ReplyRepository replyRepository;

    private MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    @DisplayName("게시글 등록에 성공한다.")
    void postBoardSuccess() {
        // given
        RequestBoard requestBoard = RequestBoard.builder()
                .title("제목")
                .content("내용")
                .build();

        request.setAttribute("loginMemberId", member.getMemberId());

        // when
        BoardVo saveBoard = boardService.postBoard(requestBoard, request);

        // then
        Long boardId = saveBoard.getBoardId();
        assertNotNull(boardRepository.findById(boardId));
    }

    @Test
    @DisplayName("게시글 리스트 불러오기에 성공한다.")
    void getBoardListSuccess() {
        //given
        for(int i=0; i<20; i++){
            LocalDateTime now = LocalDateTime.now();
            boardRepository.save(
                    Board.builder()
                            .title("테스트 "+i)
                            .content("내용 "+i)
                            .member(member)
                            .createdAt(now)
                            .modifiedAt(now)
                            .build()
            );
        }

        Pageable pageable = Pageable.ofSize(5);
        pageable = pageable.next();
        request.setAttribute("loginMemberId", member.getMemberId());

        //when
        BoardListVo boardList = boardService.getBoardList(pageable, request);

        //then
        assertNotNull(boardList);
    }

    @Test
    @DisplayName("유효하지 않은 게시물은 불러오는데 실패한다.")
    void getBoardFail1() throws Exception {
        //given
        //when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> boardService.getBoard(0L, request));

        //then
        assertEquals("유효하지 않은 게시판 번호입니다.", ex.getMessage());
    }

    @Test
    @DisplayName("게시물 불러오는데 성공한다.")
    void getBoardSuccess1() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        Board saveBoard = boardRepository.save(
                Board.builder()
                        .title("테스트")
                        .content("내용")
                        .member(member)
                        .createdAt(now)
                        .modifiedAt(now)
                        .build()
        );
        replyRepository.save(Reply.of(member, saveBoard, "댓글 달아보자"));

        //when
        BoardVo board = boardService.getBoard(saveBoard.getId(), request);

        //then
        assertNotNull(board);
    }
}
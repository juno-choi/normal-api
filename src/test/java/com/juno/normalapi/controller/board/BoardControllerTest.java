package com.juno.normalapi.controller.board;

import com.juno.normalapi.docs.TestSupport;
import com.juno.normalapi.domain.dto.RequestBoard;
import com.juno.normalapi.domain.entity.Board;
import com.juno.normalapi.domain.entity.Member;
import com.juno.normalapi.repository.board.BoardRepository;
import com.juno.normalapi.repository.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest extends TestSupport {
    private final String URL = "/v1/board";

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private Environment env;

    @Test
    @DisplayName("게시글 등록에 성공한다.")
    void postBoardSuccess() throws Exception {
        //given
        RequestBoard requestBoard = RequestBoard.builder().title("테스트 글").content("테스트 내용").build();
        //when
        ResultActions perform = mock.perform(post(URL).header(AUTHORIZATION, accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertToString(requestBoard))
        ).andDo(print());
        //then
        assertTrue(perform.andReturn().getResponse().getContentAsString().contains("SUCCESS"));
    }

    @Test
    @DisplayName("게시글 불러오기에 성공한다.")
    void getBoardSuccess() throws Exception {
        //given
        Member member = memberRepository.findByEmail(env.getProperty("normal.test.email")).get();
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

        //when
        ResultActions perform = mock.perform(
                get(URL + "?page=0&size=5").header(AUTHORIZATION, accessToken)
        ).andDo(print());
        //then
        perform.andReturn().getResponse().getContentAsString().contains("SUCCESS");
    }
}
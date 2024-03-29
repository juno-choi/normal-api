package com.juno.normalapi.domain.dto.board;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReplyDto {
    @NotNull(message = "board_id는 필수 값입니다.")
    private Long boardId;
    @NotNull(message = "content는 필수 값 입니다.")
    private String content;
}

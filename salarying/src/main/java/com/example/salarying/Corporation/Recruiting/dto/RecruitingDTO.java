package com.example.salarying.Corporation.Recruiting.dto;


import com.example.salarying.Corporation.Recruiting.entity.Progress;
import com.example.salarying.Corporation.Recruiting.entity.Recruiting;
import com.example.salarying.Corporation.User.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

public class RecruitingDTO {

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "채용공고 응답 DTO",description = "채용공고 정보 출력")
    public static class RecruitingResponse {


        private Long id;

        private String title;

        private Date postDate;

        private String task;

        private String status;

        public RecruitingResponse(Recruiting recruiting) {
            this.id = recruiting.getId();
            this.title = recruiting.getTitle();
            this.postDate = recruiting.getPostDate();
            this.task = recruiting.getTask();
            this.status = recruiting.getStatus();
        }
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "채용공고 요청 DTO",description = "채용공고 등록")
    public static class RecruitingRequest {


        private String title;

        private String task;

        private Boolean document;

        private Boolean firstRound;

        private Boolean secondRound;

        private Boolean finalRound;

        public void setTitle(String title) {
            this.title = normalize(title);
        }

        public void setTask(String task) {
            this.task = normalize(task);
        }


        public Recruiting toRecruitingEntity(Member member) {
            return Recruiting.builder()
                    .member(member)
                    .title(title)
                    .postDate(new Date())
                    .task(task)
                    .status("서류전형").build();
        }

        public Progress toProgressEntity(Recruiting recruiting) {
            return Progress.builder()
                    .recruiting(recruiting)
                    .hasDocument(document)
                    .hasFirstRound(firstRound)
                    .hasSecondRound(secondRound)
                    .hasFinalRound(finalRound)
                    .build();
        }

        }
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "전형 요청 DTO",description = "채용공고 전형단계 수정")
    public static class StatusRequest {


        @NotNull(message = "채용공고 ID는 필수입니다.")
        private Long recruitingId;

        @NotBlank(message = "채용 상태는 필수입니다.")
        @Pattern(regexp = "1차전형|2차전형|최종전형|채용완료", message = "채용 상태 값이 올바르지 않습니다.")
        private String status;

        public void setStatus(String status) {
            this.status = normalize(status);
        }

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "채용공고 상세 응답 DTO",description = "채용공고 상세 출력")
    public static class DetailResponse {

        private String title;

        private Date postDate;

        private String task;

        private String status;

        public DetailResponse(Recruiting recruiting) {
            this.title = recruiting.getTitle();
            this.postDate = recruiting.getPostDate();
            this.task = recruiting.getTask();
            this.status = recruiting.getStatus();
        }
    }
    }


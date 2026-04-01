package com.example.salarying.Admin.Terms.dto;

import com.example.salarying.Admin.Terms.entity.Terms;
import com.example.salarying.Admin.User.entity.Admin;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class TermsDTO {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "약관 등록 DTO", description = "새로운 약관 생성 시 필요한 데이터")
    public static class AddTermRequest{

        @Schema(name = "type", example = "서비스 이용 약관")
        private String type;
        @Schema(name = "title", example = "서비스 이용 약관 임시 버전")
        private String title;
        @Schema(name = "version", example = "1.1")
        private String version;
        @Schema(name = "content")
        private String content;

        public Terms toEntity(Admin admin){
            return Terms.builder()
                    .admin(admin)
                    .status("비공개")
                    .type(this.type)
                    .version(this.version)
                    .agreementTitle(this.title)
                    .agreementContent(this.content)
                    .date(new Date())
                    .build();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "약관 리스트 응답 DTO", description = "타입 별 약관 리스트")
    public static class TermsListResponse {

        @Schema(name = "terms_id", example = "1")
        private Long Id;
        @Schema(name = "status", example = "비공개")
        private String status;
        @Schema(name = "title", example = "서비스 이용 약관 임시 버전")
        private String title;
        @Schema(name = "version", example = "1.1")
        private String version;
        @Schema(name = "writer", example = "박혁거세")
        private String name;

        public TermsListResponse(Terms terms) {
            this.Id = terms.getId();
            this.status = terms.getStatus();
            this.title = terms.getAgreementTitle();
            this.version = terms.getVersion();
            this.name = terms.getAdmin().getAdminName();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "약관 상태 변경 DTO", description = "약관의 공개 여부를 설정하는 status 값 변경")
    public static class StatusRequest {

        @Schema(name = "force", example = "false")
        private Boolean force;
        @Schema(name = "status", example = "공개")
        private String status;
        @Schema(name = "terms_id", example = "1")
        private Long Id;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "약관 상세 출력 응답 DTO", description = "약관 id값으로 해당 정보 모두 출력")
    public static class DetailResponse {

        @Schema(name = "status", example = "공개")
        private String status;
        @Schema(name = "type", example = "서비스 이용약관")
        private String type;
        @Schema(name = "version", example = "공개")
        private String version;
        @Schema(name = "writer", example = "박혁거세")
        private String name;
        @Schema(name = "title", example = "제목")
        private String title;
        @Schema(name = "content", example = "내용")
        private String content;

        public DetailResponse(Terms terms) {
            this.status = terms.getStatus();
            this.type = terms.getType();
            this.version = terms.getVersion();
            this.name = terms.getAdmin().getAdminName();
            this.title = terms.getAgreementTitle();
            this.content = terms.getAgreementContent();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "약관 수정 요청 DTO", description = "약관 수정")
    public static class UpdateRequest {

        @Schema(name = "Id", example = "1")
        @NotNull(message = "약관 ID는 필수입니다.")
        private Long Id;
        @Schema(name = "version", example = "1.0")
        @NotBlank(message = "버전은 필수입니다.")
        private String version;
        @Schema(name = "title", example = "제목")
        @NotBlank(message = "제목은 필수입니다.")
        private String title;
        @Schema(name = "content", example = "내용")
        @NotBlank(message = "내용은 필수입니다.")
        private String content;
    }
}

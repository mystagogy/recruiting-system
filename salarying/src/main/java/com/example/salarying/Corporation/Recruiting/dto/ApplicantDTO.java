package com.example.salarying.Corporation.Recruiting.dto;


import com.example.salarying.Corporation.Recruiting.entity.Applicant;
import com.example.salarying.Corporation.Recruiting.entity.Recruiting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ApplicantDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "지원자 정보 응답 DTO", description = "지원자 정보 출력")
    public static class ApplicantResponse{

        private String applicantNm;

        private String applicantTel;

        private String applicantEmail;

        private String progress;

        private String status;

        public ApplicantResponse(Applicant applicant){
            this.applicantNm = applicant.getApplicantName();
            this.applicantTel = applicant.getApplicantPhoneNumber();
            this.applicantEmail = applicant.getApplicantEmail();
            this.progress = applicant.getProgress();
            this.status = applicant.getStatus();
        }


    }
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "지원자 선택하기 위한 조건 요청 DTO", description = "지원자의 progress,status에 따른 지원자 정보 출력 ")
    public static class SelectionRequest {


        private Long id;

        private String progress;

        private String status;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "지원자 요청 DTO", description = "지원자 정보 등록")
    public static class ApplicantRequest {


        private Long recruitingId;

        private String email;

        private String name;

        private String number;

        public Applicant toEntity(Recruiting recruiting){
            return Applicant.builder()
                    .recruiting(recruiting)
                    .applicantName(name)
                    .applicantPhoneNumber(number)
                    .applicantEmail(email)
                    .progress("서류전형")
                    .status("불합격")
                    .build();
        }
    }
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "지원자 progress,status변경을 위한 DTO",description = "지원자 상태 변경")
    public static class ResultRequest {


        @NotNull(message = "채용공고 ID는 필수입니다.")
        private Long recruitingId;

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "전형 단계는 필수입니다.")
        @Pattern(regexp = "서류전형|1차전형|2차전형|최종전형", message = "전형 단계 값이 올바르지 않습니다.")
        private String progress;

        @NotBlank(message = "합격 여부는 필수입니다.")
        @Pattern(regexp = "합격|불합격", message = "합격 여부 값이 올바르지 않습니다.")
        private String status;



    }
}

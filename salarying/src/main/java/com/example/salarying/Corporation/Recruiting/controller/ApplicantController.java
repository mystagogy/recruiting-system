package com.example.salarying.Corporation.Recruiting.controller;


import com.example.salarying.Corporation.Recruiting.dto.ApplicantDTO;
import com.example.salarying.Corporation.Recruiting.service.ApplicantService;
import com.example.salarying.global.dto.ResponseDTO;
import com.example.salarying.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ApplicantController {


    private final ApplicantService applicantService;

    /**
     * 채용공고별 지원자 리스트 출력하는 API
     * @param customUserDetails: 로그인 기업회원 id
     * @param id: 채용공고 id
     * @return: 기업의 채용공고별 지원자 리스트 출력
     */
    @Operation(summary = "지원자 리스트 출력", description = "지원자 리스트 출력 FOR USER")
    @GetMapping("/applicants")
    public ResponseDTO<?> showApplicantList(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestParam("recruiting_id") Long id){
        List<ApplicantDTO.ApplicantResponse> applicantResponseList = applicantService.findApplicantByRecruitingId(customUserDetails.getUserId(),id);
        return new ResponseDTO<>().ok(applicantResponseList,"정상출력 데이터");
    }

    /**
     * 채용전형과 합격여부와 일치하는 지원자 리스트 출력하는 API
     * @param customUserDetails: 로그인 기업회원 id
     * @param request: 채용공고id,채용전형,합격여부 정보를 가진 DTO
     * @return: 조건에 해당하는 지원자 리스트
     */
    @Operation(summary = "채용전형과 합격여부와 일치하는 지원자 리스트 출력", description = "채용전형과 합격여부와 일치하는 지원자 리스트 출력 FOR USER")
    @PostMapping("/applicants/selection")
    public ResponseDTO<?> showSelectedApplicantList(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody ApplicantDTO.SelectionRequest request){
        List<ApplicantDTO.ApplicantResponse> applicantResponseList = applicantService.findApplicantByRecruitingIdAndProgressAndStatus(customUserDetails.getUserId(), request);
        return new ResponseDTO<>().ok(applicantResponseList,"정상출력 데이터");
    }

    /**
     * 채용공고에 지원자 등록
     * @param customUserDetails: 로그인 기업회원
     * @param request: 지원자 정보 담은 DTO
     * @return 등록된 지원자 정보
     */
    @Operation(summary = "채용공고에 지원자 등록", description = "지원자 등록 FOR USER")
    @PostMapping("/applicants")
    public ResponseDTO<?> insertApplicant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody ApplicantDTO.ApplicantRequest request){
        ApplicantDTO.ApplicantResponse applicantResponse = applicantService.insertApplicant(customUserDetails.getUserId(), request);
        return new ResponseDTO<>().ok(applicantResponse,"정상출력 데이터");
    }

    /**
     * 채용단계에 따른 지원자 progress, status값 수정
     * @param customUserDetails: 로그인 기업회원
     * @param request: 수정할 내용 담은 DTO
     * @return: 수정된 지원자 정보
     */
    @Operation(summary = "지원자 progress,status값 수정",description = "전형단계, 합격여부 값 변경 FOR USER")
    @PutMapping("/applicants")
    public ResponseDTO<?> updateStatus(@AuthenticationPrincipal CustomUserDetails customUserDetails, @Valid @RequestBody ApplicantDTO.ResultRequest request){
        ApplicantDTO.ApplicantResponse applicantResponse = applicantService.updateApplicant(customUserDetails.getUserId(),request);
        return new ResponseDTO<>().ok(applicantResponse,"수정완료");
    }

}

package com.example.salarying.Corporation.Recruiting.controller;


import com.example.salarying.Corporation.Recruiting.dto.RecruitingDTO;
import com.example.salarying.Corporation.Recruiting.service.RecruitingService;
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
public class RecruitingController {


    private final RecruitingService recruitingService;

    /**
     * 기업id로 채용공고 리스트를 출력하는 API
     * @param customUserDetails: 로그인한 기업회원
     * @return: 기업별 채용공고 리스트 출력
     */
    @Operation(summary = "채용공고 리스트 출력",description = "채용공고 리스트 출력 FOR USER")
    @GetMapping("/recruiting")
    public ResponseDTO<?> showRecruitingList(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        List<RecruitingDTO.RecruitingResponse> recruitingResponseList = recruitingService.findRecruitingByMemberId(customUserDetails.getUserId());
        return new ResponseDTO<>().ok(recruitingResponseList,"정상출력 데이터");
    }

    /**
     * 기업id로 채용공고를 등록하는 API
     * @param customUserDetails: 로그인한 기업회원
     * @param request: 등록하고자 하는 채용공고 정보 DTO
     * @return: 등록된 채용공고 DTO
     */
    @Operation(summary = "기업별 채용공고 등록",description = "채용공고 등록 FOR USER")
    @PostMapping("/recruiting")
    public ResponseDTO<?> insertRecruiting(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody RecruitingDTO.RecruitingRequest request){
        RecruitingDTO.RecruitingResponse responseDTO = recruitingService.insertRecruiting(customUserDetails.getUserId(),request);
        return new ResponseDTO<>().ok(responseDTO,"정상출력 데이터");
    }

    /**
     * 채용공고 status 변경하는 API
     * @param customUserDetails: 로그인한 기업회원
     * @param request: 변경하고자하는 status 정보 DTO
     * @return: 수정된 채용공고 DTO
     */
    @Operation(summary = "채용공고 status 변경", description = "채용공고 전형 변경 FOR USER")
    @PutMapping("/recruiting")
    public ResponseDTO<?> updateStatus(@AuthenticationPrincipal CustomUserDetails customUserDetails, @Valid @RequestBody RecruitingDTO.StatusRequest request){
        RecruitingDTO.RecruitingResponse responseDTO = recruitingService.updateStatus(customUserDetails.getUserId(),request);
        return new ResponseDTO<>().ok(responseDTO,"수정완료");
    }

    /**
     * 채용공고 상세 출력하는 API
     * @param customUserDetails: 로그인한 기업회원
     * @param id: 채용공고id
     * @return: 해당 채용공고 정보가진 DTO
     */
    @Operation(summary = "채용공고 상세",description = "채용공고 상세 FOR USER")
    @GetMapping("/recruiting/{id}")
    public ResponseDTO<?> showRecruiting(@AuthenticationPrincipal CustomUserDetails customUserDetails,@PathVariable Long id){
        RecruitingDTO.DetailResponse detailResponse = recruitingService.detailRecruiting(customUserDetails.getUserId(), id);
        return new ResponseDTO<>().ok(detailResponse,"정상출력 데이터");
    }
}

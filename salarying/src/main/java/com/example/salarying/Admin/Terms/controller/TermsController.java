package com.example.salarying.Admin.Terms.controller;

import com.example.salarying.Admin.Terms.dto.TermsDTO;
import com.example.salarying.Admin.Terms.service.TermsService;
import com.example.salarying.global.dto.ResponseDTO;
import com.example.salarying.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
public class TermsController {

    private final TermsService termsService;

    /**
     * 약관 등록 API
     * @param userDetails : 작성자 이메일
     * @param request : 제목, 내용, 버전, 타입
     * @return : 약관 등록 성공 여부
     */
    @Operation(summary = "약관 등록 API", description = "새로운 약관 생성 FOR ADMIN, SUPERADMIN")
    @PostMapping("/terms")
    public ResponseDTO<?> AddTerms(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody TermsDTO.AddTermRequest request){
        termsService.insertTerm(userDetails.getUserId(), request);
        return new ResponseDTO<>().ok(null, "약관 등록 완료");

    }

    /**
     * 약관의 타입별로 등록된 약관 리스트를 출력하는 API
     * @param type : 약관 타입(service, privacy, information, marketing)
     * @return : 약관 리스트 출력
     */
    @Operation(summary = "약관 리스트 출력 API", description = "약관별 리스트 출력 FOR ADMIN, SUPERADMIN")
    @GetMapping("/terms")
    public ResponseDTO<?> ListTerms(@RequestParam String type){
        return new ResponseDTO<>().ok(termsService.termsList(type), "리스트 출력 완료");

    }

    /**
     * 약관의 공개 상태를 변경하는 API
     * 해당 약관 공개 설정 요청 시 이미 공개된 해당 타입의 약관이 존재하는 경우 변경 불가
     * @param request : 상태 변경 요청 DTO(약관 Id, status)
     * @return : 변경완료
     */
    @Operation(summary = "약관 상태 변경 API", description = "약관 공개 여부 상태 변경 FOR ADMIN, SUPERADMIN")
    @PostMapping("/terms/status")
    public ResponseDTO<?> changeStatus(@RequestBody TermsDTO.StatusRequest request){
        return new ResponseDTO<>().ok(null, termsService.changeStatus(request));

    }

    /**
     * 약관 내용 상세 출력 API
     * @param Id : 약관 Id
     * @return : 약관 상세 내용 출력
     */
    @Operation(summary = "약관 상세보기 API", description = "약관 상세 보기 FOR ADMIN, SUPERADMIN")
    @GetMapping("/terms/detail/{Id}")
    public ResponseDTO<?> showTerm(@PathVariable Long Id){
        return new ResponseDTO<>().ok(termsService.showDetail(Id), "약관 출력 완료");

    }

    /**
     * 약관 내용 수정 API
     * @param userDetails : 작성자 입력을 위한 사용자 정보
     * @param request : 변경 요청 DTO
     * @return : 변경완료
     */
    @Operation(summary = "약관 수정 API", description = "약관 내용 수정 FOR ADMIN, SUPERADMIN")
    @PutMapping("/terms")
    public ResponseDTO<?> updateTerm(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody TermsDTO.UpdateRequest request){
        termsService.updateTerm(userDetails.getUserId(), request);
        return new ResponseDTO<>().ok(null, "변경 완료");

    }

    /**
     * 약관 삭제 API
     * @param userDetails : 사용자 확인을 위한 userDetails
     * @param Id : 약관 Id
     * @return : 삭제 완료
     */
    @Operation(summary = "약관 삭제 API", description = "약관 삭제 FOR ADMIN, SUPERADMIN")
    @DeleteMapping("/terms/{Id}")
    public ResponseDTO<?> deleteTerm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long Id){
        return new ResponseDTO<>().ok(null,termsService.deleteTerm(userDetails.getUserId(), Id));
    }
}

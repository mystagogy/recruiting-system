package com.example.salarying.Corporation.Recruiting.service.impl;


import com.example.salarying.Corporation.Recruiting.dto.ApplicantDTO;
import com.example.salarying.Corporation.Recruiting.entity.Applicant;
import com.example.salarying.Corporation.Recruiting.entity.Recruiting;
import com.example.salarying.Corporation.Recruiting.exception.*;
import com.example.salarying.Corporation.Recruiting.repository.ApplicantRepository;
import com.example.salarying.Corporation.Recruiting.repository.RecruitingRepository;
import com.example.salarying.Corporation.Recruiting.service.ApplicantService;
import com.example.salarying.Corporation.Recruiting.service.RecruitingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ApplicantServiceImpl implements ApplicantService {

    private static final Set<String> VALID_PROGRESS = Set.of("서류전형", "1차전형", "2차전형", "최종전형");
    private static final Set<String> VALID_RESULT_STATUS = Set.of("합격", "불합격");

    private final ApplicantRepository applicantRepository;

    private final RecruitingService recruitingService;

    private final RecruitingRepository recruitingRepository;

    /**
     * 기업id와 채용공고id가 일치하는 지원자리스트 출력
     * @param userId: 기업회원 id
     * @param recruitingId: 채용공고 id
     * @return: 기업의 채용공고별 지원자 리스트
     */
    @Override
    public List<ApplicantDTO.ApplicantResponse> findApplicantByRecruitingId(Long userId, Long recruitingId) {
        Optional<Recruiting> recruitingOptional = recruitingRepository.findById(recruitingId);
        if(recruitingOptional.isPresent()){
            List<ApplicantDTO.ApplicantResponse> applicantResponseList = applicantRepository.findApplicantByRecruitingId(recruitingId)
                    .stream().filter(en -> en.getRecruiting().getMember().getId() == userId)
                    .map(entity -> new ApplicantDTO.ApplicantResponse(entity))
                    .collect(Collectors.toList());
            return applicantResponseList;
        }else{
            throw new RecruitingException(RecruitingExceptionType.NOT_EXIST);
        }
    }


    /**
     * 기업회원 id,채용공고id,progress,status가 일치하는 지원자리스트 출력
     * @param userId: 기업회원 id
     * @param request: 채용공고id,채용전형,합격여부 정보를 가진 DTO
     * @return 조건에 해당하는 지원자 리스트
     */
    @Override
    public List<ApplicantDTO.ApplicantResponse> findApplicantByRecruitingIdAndProgressAndStatus(Long userId,ApplicantDTO.SelectionRequest request) {
        List<ApplicantDTO.ApplicantResponse> applicantResponseList = findApplicantByRecruitingId(userId, request.getId())
                                                                    .stream()
                                                                    .filter(e->e.getStatus().equals(request.getStatus())&&e.getProgress().equals(request.getProgress()))
                                                                    .collect(Collectors.toList());

        return applicantResponseList;
    }

    /**
     * 지원자 등록하는 기능
     * @param userId: 기업회원 id
     * @param request: 지원자 정보를 담은 DTO
     * @return: 등록된 지원자 정보
     */
    @Override
    public ApplicantDTO.ApplicantResponse insertApplicant(Long userId, ApplicantDTO.ApplicantRequest request) {

        Recruiting recruiting = recruitingRepository.findRecruitingByIdAndMember_IdAndStatus(request.getRecruitingId(), userId,"서류전형")
                .orElseThrow(() -> new RecruitingException(RecruitingExceptionType.NOT_EXIST));

        if (checkApplicantRequestDTO(request)&&!applicantRepository.existsApplicantByApplicantEmailAndRecruiting_Id(request.getEmail(), request.getRecruitingId())) {
            return new ApplicantDTO.ApplicantResponse(applicantRepository.save(request.toEntity(recruiting)));
        }else {
            throw new ApplicantException(ApplicantExceptionType.ALREADY_EXIST_APPLICANT);
        }
    }

    /**
     * 지원자 요청 DTO 형식 체크
     * @param request: 지원자 정보를 담은 DTO
     * @return: 올바른 형식-true/ 아니면 예외처리
     */
    @Override
    public Boolean checkApplicantRequestDTO(ApplicantDTO.ApplicantRequest request) {
        if (request.getRecruitingId()==null) {
            throw new ApplicantException(ApplicantExceptionType.NOT_EXIST_RECRUITING);
        } else if (request.getName() == null || request.getName().isBlank()) {
            throw new ApplicantException(ApplicantExceptionType.NOT_EXIST_NAME);
        } else if (request.getNumber() == null || request.getNumber().isBlank()) {
            throw new ApplicantException(ApplicantExceptionType.NOT_EXIST_PHONE);
        } else if(!request.getNumber().matches("\\d{3}-?\\d{4}-?\\d{4}")){
            throw new ApplicantException(ApplicantExceptionType.NOT_PHONE_FORMAT);
        } else if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ApplicantException(ApplicantExceptionType.NOT_EXIST_EMAIL);
        } else if (!request.getEmail().matches("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")) {
            throw new ApplicantException(ApplicantExceptionType.NOT_EMAIL_FORMAT);
        } else {
            return true;
        }
    }

    /**
     * 지원자 progress,status 수정
     * @param userId: 기업 회원 id
     * @param request: 수정 요청 DTO
     * @return: 수정된 지원자 정보
     */
    @Transactional
    @Override
    public ApplicantDTO.ApplicantResponse updateApplicant(Long userId, ApplicantDTO.ResultRequest request) {
        Recruiting recruiting = recruitingService.findRecruitingByIdAndAndMember_Id(request.getRecruitingId(),userId);

        Applicant applicant = applicantRepository.findApplicantByApplicantEmailAndRecruiting(request.getEmail(),recruiting)
                                                    .orElseThrow(()->new ApplicantException(ApplicantExceptionType.NOT_EXIST));

        if(checkResultRequestDTO(request)&&recruiting.getStatus().equals(request.getProgress())){
            applicant.update(request.getProgress(),request.getStatus());
            return new ApplicantDTO.ApplicantResponse(applicantRepository.save(applicant));
        }else{
            throw new ApplicantException(ApplicantExceptionType.NOT_MATCH_PROGRESS);
        }
    }

    /**
     * 수정요청 DTO 형식 체크
     * @param request: 수정 요청 DTO
     * @return: 올바른 형식-true/ 아니면 예외처리
     */
    @Override
    public Boolean checkResultRequestDTO(ApplicantDTO.ResultRequest request) {
        if (request.getRecruitingId()==null) {
            throw new ApplicantException(ApplicantExceptionType.NOT_EXIST_RECRUITING);
        } else if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ApplicantException(ApplicantExceptionType.NOT_EXIST_EMAIL);
        } else if (!request.getEmail().matches("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")) {
            throw new ApplicantException(ApplicantExceptionType.NOT_EMAIL_FORMAT);
        } else if (request.getProgress() == null || request.getProgress().isBlank() || !VALID_PROGRESS.contains(request.getProgress())){
            throw new ApplicantException(ApplicantExceptionType.NOT_PROGRESS);
        } else if (request.getStatus() == null || request.getStatus().isBlank() || !VALID_RESULT_STATUS.contains(request.getStatus())) {
            throw new ApplicantException(ApplicantExceptionType.NOT_MATCH_STATUS);
        } else {
            return true;
        }
    }


}

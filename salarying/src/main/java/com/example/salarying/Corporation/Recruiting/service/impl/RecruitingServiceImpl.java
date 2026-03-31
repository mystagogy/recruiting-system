package com.example.salarying.Corporation.Recruiting.service.impl;


import com.example.salarying.Corporation.Recruiting.dto.RecruitingDTO;
import com.example.salarying.Corporation.Recruiting.entity.Progress;
import com.example.salarying.Corporation.Recruiting.entity.Recruiting;
import com.example.salarying.Corporation.Recruiting.exception.RecruitingException;
import com.example.salarying.Corporation.Recruiting.exception.RecruitingExceptionType;
import com.example.salarying.Corporation.Recruiting.repository.ProgressRepository;
import com.example.salarying.Corporation.Recruiting.repository.RecruitingRepository;
import com.example.salarying.Corporation.Recruiting.service.RecruitingService;
import com.example.salarying.Corporation.User.dto.MemberDTO;
import com.example.salarying.Corporation.User.entity.Member;
import com.example.salarying.Corporation.User.exception.UserException;
import com.example.salarying.Corporation.User.exception.UserExceptionType;
import com.example.salarying.Corporation.User.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RecruitingServiceImpl implements RecruitingService {

    private static final Set<String> VALID_RECRUITING_STATUSES = Set.of("1차전형", "2차전형", "최종전형", "채용완료");

    private final RecruitingRepository recruitingRepository;
    private final ProgressRepository progressRepository;
    private final MemberRepository memberRepository;

    /**
     * 기업id로 채용공고 리스트 출력
     * @param userId : 기업회원 id
     * @return: 해당기업 채용공고 리스트
     */
    @Override
    public List<RecruitingDTO.RecruitingResponse> findRecruitingByMemberId(Long userId) {
        List<RecruitingDTO.RecruitingResponse> recruitingResponseList = recruitingRepository.findRecruitingByMemberId(userId)
                .stream().map(entity->new RecruitingDTO.RecruitingResponse(entity))
                .collect(Collectors.toList());
        return recruitingResponseList;
    }

    /**
     * 기업id로 채용공고 등록
     * @param userId: 기업회원 id
     * @param request: 등록하고자 하는 채용공고 정보 DTO
     * @return: 등록한 채용절차 정보
     */
    @Override
    @Transactional
    public RecruitingDTO.RecruitingResponse insertRecruiting(Long userId,RecruitingDTO.RecruitingRequest request) {

        Optional<Member> member = memberRepository.findById(userId);

        if (member.isPresent()&&checkRecruitingDTO(request)) {
            Recruiting recruiting = request.toRecruitingEntity(member.get());

            Progress progress = request.toProgressEntity(recruitingRepository.save(recruiting));

            progressRepository.save(progress);

            return new RecruitingDTO.RecruitingResponse(recruiting);
        }else{
            throw new UserException(UserExceptionType.NOT_EXIST_ACCOUNT);
        }
    }


    /**
     * DTO형식 체크 기능
     * @param request: 등록하고자 하는 채용공고 정보 DTO
     * @return: 공고명,직무 작성안되어 있거나, 채용절차 없을시 false/ 잘 작성되어 있으면  true
     */
    public Boolean checkRecruitingDTO(RecruitingDTO.RecruitingRequest request){
        if(request.getTitle() == null || request.getTitle().isBlank()){
            throw new RecruitingException(RecruitingExceptionType.NOT_EXIST_TITLE);
        } else if (request.getTask() == null || request.getTask().isBlank()) {
            throw new RecruitingException(RecruitingExceptionType.NOT_EXIST_TASK);
        } else if (!request.getDocument()&& !request.getFirstRound()&& !request.getSecondRound()&& !request.getFinalRound()){
            throw new RecruitingException(RecruitingExceptionType.NOT_EXIST_PROGRESS);
        }else{
            return true;
        }
    }

    /**
     * 채용공고 id로 채용공고 정보 찾기
     * @param recruitingId: 채용공고 id
     * @return: 채용공고 정보
     */
    @Override
    public Recruiting findById(Long recruitingId) {
        Optional<Recruiting> recruitingOptional = recruitingRepository.findById(recruitingId);
        if(recruitingOptional.isPresent()){
            return recruitingOptional.get();
        }else{
            throw new RecruitingException(RecruitingExceptionType.NOT_EXIST);
        }
    }

    /**
     * 채용공고 전형 수정
     * @param userId: 기업회원 id
     * @param request: 수정 요청 DTO
     * @return: 수정된 채용공고 정보 DTO
     */
    @Transactional
    @Override
    public RecruitingDTO.RecruitingResponse updateStatus(Long userId, RecruitingDTO.StatusRequest request) {
        checkStatusDTO(request);
        Recruiting recruiting = findRecruitingByIdAndAndMember_Id(request.getRecruitingId(),userId);
        recruiting.update(request.getStatus());
        return new RecruitingDTO.RecruitingResponse(recruitingRepository.save(recruiting));

    }

    /**
     * status 요청 DTO 형식체크
     * @param request: 채용공고id와 status가 담긴 DTO
     * @return 수정된 채용공고 정보
     */
    public Boolean checkStatusDTO(RecruitingDTO.StatusRequest request){
        if(request.getRecruitingId()==null){
            throw new RecruitingException(RecruitingExceptionType.NOT_EXIST_ID);
        } else if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new RecruitingException(RecruitingExceptionType.NOT_EXIST_PROGRESS);
        } else if (!VALID_RECRUITING_STATUSES.contains(request.getStatus())){
            throw new RecruitingException(RecruitingExceptionType.NOT_STATUS_FORMAT);
        }else{
            return true;
        }
    }

    /**
     * 채용공고 상세 출력
     * @param userId: 로그인 기업 id
     * @param recruitingId: 채용공고 id
     * @return 해당 채용공고 정보가진 DTO
     */
    @Override
    public RecruitingDTO.DetailResponse detailRecruiting(Long userId, Long recruitingId) {
        Recruiting recruiting = findRecruitingByIdAndAndMember_Id(recruitingId,userId);
        return new RecruitingDTO.DetailResponse(recruiting);
    }

    /**
     *
     * @param recruitingId
     * @param userId
     * @return
     */
    @Override
    public Recruiting findRecruitingByIdAndAndMember_Id(Long recruitingId, Long userId) {

        Optional<Recruiting> recruiting = recruitingRepository.findRecruitingByIdAndAndMember_Id(recruitingId,userId);

        if(recruiting.isPresent()){
            return recruiting.get();
        }else{
            throw  new RecruitingException(RecruitingExceptionType.NOT_EXIST);
        }

    }

}

package com.example.salarying.Corporation.Recruiting.service.impl;

import com.example.salarying.Corporation.Recruiting.dto.EmailDTO;
import com.example.salarying.Corporation.Recruiting.entity.Applicant;
import com.example.salarying.Corporation.Recruiting.entity.Email;
import com.example.salarying.Corporation.Recruiting.entity.Recruiting;
import com.example.salarying.Corporation.Recruiting.exception.*;
import com.example.salarying.Corporation.Recruiting.repository.ApplicantRepository;
import com.example.salarying.Corporation.Recruiting.repository.EmailRepository;
import com.example.salarying.Corporation.Recruiting.service.EmailService;
import com.example.salarying.Corporation.Recruiting.service.RecruitingService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {

    private static final Set<String> VALID_PROGRESS = Set.of("서류전형", "1차전형", "2차전형", "최종전형");
    private static final Set<String> VALID_RESULT_STATUS = Set.of("합격", "불합격");


    private final EmailRepository emailRepository;

    private final JavaMailSender javaMailSender;

    private final ApplicantRepository applicantRepository;

    private final RecruitingService recruitingService;


    /**
     * 이메일 발송 및 내용 저장
     * @param userId: 기업회원 id
     * @param requestList: 이메일요청 DTO 리스트
     */
    @Transactional
    public void sendAndSaveEmail(Long userId, List<EmailDTO.EmailRequest> requestList) {

        for (EmailDTO.EmailRequest request : requestList) {
            Recruiting recruiting = recruitingService.findById(request.getRecruitingId());

            if (checkEmailDTO(request)&&recruiting.getMember().getId().equals(userId)) {
                Applicant applicant = applicantRepository.findApplicantByApplicantEmailAndRecruitingAndProgressAndStatus(request.getApplicantEmail(),recruiting, request.getProgress(), request.getStatus())
                        .orElseThrow(()->new ApplicantException(ApplicantExceptionType.NOT_EXIST));
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setSubject(request.getTitle());
                mailMessage.setText(request.getContent());
                mailMessage.setFrom(recruiting.getMember().getEmail());
                mailMessage.setTo(applicant.getApplicantEmail());
                javaMailSender.send(mailMessage);
                Email email = Email.builder().applicant(applicant).recruiting(recruiting).progress(request.getProgress())
                        .sendDate(new Date()).status(request.getStatus()).build();
                emailRepository.save(email);
            }else{
                throw new EmailException(EmailExceptionType.NOT_EXIST_APPLICANT);
            }

        }


    }

    /**
     * 이메일 요청 DTO 형식 체크
     * @param request: 이메일 요청 DTO
     * @return: 올바른 형식-true/ 아니면 예외처리
     */
    public Boolean checkEmailDTO(EmailDTO.EmailRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new EmailException(EmailExceptionType.NOT_EXIST_SUBJECT);
        } else if (request.getContent() == null || request.getContent().isBlank()) {
            throw new EmailException(EmailExceptionType.NOT_EXIST_CONTENT);
        } else if (request.getApplicantEmail() == null || request.getApplicantEmail().isBlank()) {
            throw new EmailException(EmailExceptionType.NOT_EXIST_EMAIL);
        } else if (!request.getApplicantEmail().matches("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")) {
            throw new EmailException(EmailExceptionType.NOT_EMAIL_FORMAT);
        } else if (request.getProgress() == null || request.getProgress().isBlank() || !VALID_PROGRESS.contains(request.getProgress())){
            throw new EmailException(EmailExceptionType.NOT_MATCH_PROGRESS);
        } else if (request.getStatus() == null || request.getStatus().isBlank() || !VALID_RESULT_STATUS.contains(request.getStatus())){
            throw new EmailException(EmailExceptionType.NOT_MATCH_STATUS);
        }else {
            return true;
        }
    }

    /**
     * 기업회원 id에 따른 메일 전송 내역 리스트 출력
     * @param userId: 로그인 기업회원 id
     * @return: 메일 전송 내역 리스트
     */
    @Override
    public List<EmailDTO.EmailResponse> findEmailByMemberId(Long userId) {
        List<EmailDTO.EmailResponse> emailResponseList = emailRepository.findEmailByRecruiting_Member_Id(userId)
                                                                        .stream().map(en->new EmailDTO.EmailResponse(en))
                                                                        .collect(Collectors.toList());
        return emailResponseList;
    }


}



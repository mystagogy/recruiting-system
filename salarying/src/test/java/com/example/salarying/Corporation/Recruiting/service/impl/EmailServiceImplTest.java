package com.example.salarying.Corporation.Recruiting.service.impl;

import com.example.salarying.Corporation.Recruiting.dto.EmailDTO;
import com.example.salarying.Corporation.Recruiting.entity.Applicant;
import com.example.salarying.Corporation.Recruiting.entity.Email;
import com.example.salarying.Corporation.Recruiting.entity.Recruiting;
import com.example.salarying.Corporation.Recruiting.exception.EmailException;
import com.example.salarying.Corporation.Recruiting.exception.EmailExceptionType;
import com.example.salarying.Corporation.Recruiting.repository.ApplicantRepository;
import com.example.salarying.Corporation.Recruiting.repository.EmailRepository;
import com.example.salarying.Corporation.Recruiting.service.RecruitingService;
import com.example.salarying.Corporation.User.entity.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private RecruitingService recruitingService;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendAndSaveEmail_sendsMailAndStoresHistory_whenRequestIsValid() {
        Recruiting recruiting = createRecruiting(1L, "corp@test.com");
        Applicant applicant = Applicant.builder()
                .recruiting(recruiting)
                .applicantName("지원자")
                .applicantPhoneNumber("010-1234-5678")
                .applicantEmail("applicant@test.com")
                .progress("서류전형")
                .status("합격")
                .build();
        EmailDTO.EmailRequest request = EmailDTO.EmailRequest.builder()
                .recruitingId(1L)
                .applicantEmail("applicant@test.com")
                .title("합격 안내")
                .content("다음 전형에 참여해주세요.")
                .progress("서류전형")
                .status("합격")
                .build();

        when(recruitingService.findById(1L)).thenReturn(recruiting);
        when(applicantRepository.findApplicantByApplicantEmailAndRecruitingAndProgressAndStatus(
                "applicant@test.com", recruiting, "서류전형", "합격"))
                .thenReturn(Optional.of(applicant));
        when(emailRepository.save(any(Email.class))).thenAnswer(invocation -> invocation.getArgument(0));

        emailService.sendAndSaveEmail(1L, List.of(request));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

        verify(javaMailSender).send(messageCaptor.capture());
        verify(emailRepository).save(emailCaptor.capture());

        assertThat(messageCaptor.getValue().getSubject()).isEqualTo("합격 안내");
        assertThat(messageCaptor.getValue().getText()).isEqualTo("다음 전형에 참여해주세요.");
        assertThat(messageCaptor.getValue().getFrom()).isEqualTo("corp@test.com");
        assertThat(messageCaptor.getValue().getTo()).containsExactly("applicant@test.com");

        assertThat(emailCaptor.getValue().getApplicant()).isEqualTo(applicant);
        assertThat(emailCaptor.getValue().getRecruiting()).isEqualTo(recruiting);
        assertThat(emailCaptor.getValue().getProgress()).isEqualTo("서류전형");
        assertThat(emailCaptor.getValue().getStatus()).isEqualTo("합격");
    }

    @Test
    void checkEmailDTO_throwsEmailException_whenStatusIsInvalid() {
        EmailDTO.EmailRequest request = EmailDTO.EmailRequest.builder()
                .recruitingId(1L)
                .applicantEmail("applicant@test.com")
                .title("안내")
                .content("내용")
                .progress("서류전형")
                .status("대기")
                .build();

        EmailException exception = assertThrows(EmailException.class, () -> emailService.checkEmailDTO(request));

        assertThat(exception.getExceptionType()).isEqualTo(EmailExceptionType.NOT_MATCH_STATUS);
    }

    @Test
    void sendAndSaveEmail_throwsEmailException_whenRecruitingOwnerDoesNotMatch() {
        Recruiting recruiting = createRecruiting(1L, "corp@test.com");
        EmailDTO.EmailRequest request = EmailDTO.EmailRequest.builder()
                .recruitingId(1L)
                .applicantEmail("applicant@test.com")
                .title("합격 안내")
                .content("다음 전형에 참여해주세요.")
                .progress("서류전형")
                .status("합격")
                .build();

        when(recruitingService.findById(1L)).thenReturn(recruiting);

        EmailException exception = assertThrows(EmailException.class,
                () -> emailService.sendAndSaveEmail(999L, List.of(request)));

        assertThat(exception.getExceptionType()).isEqualTo(EmailExceptionType.NOT_EXIST_APPLICANT);
        verifyNoInteractions(applicantRepository, javaMailSender, emailRepository);
    }

    private Recruiting createRecruiting(Long userId, String email) {
        Member member = Member.builder()
                .companyName("테스트 회사")
                .companyPhoneNumber("02-1234-5678")
                .email(email)
                .password("password")
                .role("USER")
                .build();
        ReflectionTestUtils.setField(member, "id", userId);

        Recruiting recruiting = Recruiting.builder()
                .member(member)
                .title("백엔드 개발자")
                .task("서버 개발")
                .status("서류전형")
                .build();
        ReflectionTestUtils.setField(recruiting, "id", 1L);
        return recruiting;
    }
}

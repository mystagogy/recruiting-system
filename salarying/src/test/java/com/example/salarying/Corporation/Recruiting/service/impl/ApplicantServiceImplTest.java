package com.example.salarying.Corporation.Recruiting.service.impl;

import com.example.salarying.Corporation.Recruiting.dto.ApplicantDTO;
import com.example.salarying.Corporation.Recruiting.entity.Applicant;
import com.example.salarying.Corporation.Recruiting.entity.Recruiting;
import com.example.salarying.Corporation.Recruiting.exception.ApplicantException;
import com.example.salarying.Corporation.Recruiting.exception.ApplicantExceptionType;
import com.example.salarying.Corporation.Recruiting.repository.ApplicantRepository;
import com.example.salarying.Corporation.Recruiting.repository.RecruitingRepository;
import com.example.salarying.Corporation.Recruiting.service.RecruitingService;
import com.example.salarying.Corporation.User.entity.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicantServiceImplTest {

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private RecruitingService recruitingService;

    @Mock
    private RecruitingRepository recruitingRepository;

    @InjectMocks
    private ApplicantServiceImpl applicantService;

    @Test
    void insertApplicant_savesApplicant_whenRequestIsValid() {
        Recruiting recruiting = createRecruiting(1L, "서류전형");
        ApplicantDTO.ApplicantRequest request = ApplicantDTO.ApplicantRequest.builder()
                .recruitingId(1L)
                .email("applicant@test.com")
                .name("지원자")
                .number("010-1234-5678")
                .build();

        when(recruitingRepository.findRecruitingByIdAndMember_IdAndStatus(1L, 1L, "서류전형"))
                .thenReturn(Optional.of(recruiting));
        when(applicantRepository.existsApplicantByApplicantEmailAndRecruiting_Id("applicant@test.com", 1L))
                .thenReturn(false);
        when(applicantRepository.save(any(Applicant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicantDTO.ApplicantResponse response = applicantService.insertApplicant(1L, request);

        assertThat(response.getApplicantNm()).isEqualTo("지원자");
        assertThat(response.getApplicantEmail()).isEqualTo("applicant@test.com");
        assertThat(response.getProgress()).isEqualTo("서류전형");
        assertThat(response.getStatus()).isEqualTo("불합격");
    }

    @Test
    void checkApplicantRequestDTO_throwsApplicantException_whenPhoneFormatIsInvalid() {
        ApplicantDTO.ApplicantRequest request = ApplicantDTO.ApplicantRequest.builder()
                .recruitingId(1L)
                .email("applicant@test.com")
                .name("지원자")
                .number("010123")
                .build();

        ApplicantException exception = assertThrows(ApplicantException.class,
                () -> applicantService.checkApplicantRequestDTO(request));

        assertThat(exception.getExceptionType()).isEqualTo(ApplicantExceptionType.NOT_PHONE_FORMAT);
    }

    @Test
    void updateApplicant_updatesApplicantStatus_whenProgressMatchesRecruitingStatus() {
        Recruiting recruiting = createRecruiting(1L, "1차전형");
        Applicant applicant = Applicant.builder()
                .recruiting(recruiting)
                .applicantName("지원자")
                .applicantPhoneNumber("010-1234-5678")
                .applicantEmail("applicant@test.com")
                .progress("서류전형")
                .status("불합격")
                .build();
        ApplicantDTO.ResultRequest request = ApplicantDTO.ResultRequest.builder()
                .recruitingId(1L)
                .email("applicant@test.com")
                .progress("1차전형")
                .status("합격")
                .build();

        when(recruitingService.findRecruitingByIdAndAndMember_Id(1L, 1L)).thenReturn(recruiting);
        when(applicantRepository.findApplicantByApplicantEmailAndRecruiting("applicant@test.com", recruiting))
                .thenReturn(Optional.of(applicant));
        when(applicantRepository.save(applicant)).thenReturn(applicant);

        ApplicantDTO.ApplicantResponse response = applicantService.updateApplicant(1L, request);

        assertThat(response.getProgress()).isEqualTo("1차전형");
        assertThat(response.getStatus()).isEqualTo("합격");
        assertThat(applicant.getProgress()).isEqualTo("1차전형");
        assertThat(applicant.getStatus()).isEqualTo("합격");
    }

    @Test
    void updateApplicant_throwsApplicantException_whenProgressDoesNotMatchRecruitingStatus() {
        Recruiting recruiting = createRecruiting(1L, "서류전형");
        Applicant applicant = Applicant.builder()
                .recruiting(recruiting)
                .applicantName("지원자")
                .applicantPhoneNumber("010-1234-5678")
                .applicantEmail("applicant@test.com")
                .progress("서류전형")
                .status("불합격")
                .build();
        ApplicantDTO.ResultRequest request = ApplicantDTO.ResultRequest.builder()
                .recruitingId(1L)
                .email("applicant@test.com")
                .progress("1차전형")
                .status("합격")
                .build();

        when(recruitingService.findRecruitingByIdAndAndMember_Id(1L, 1L)).thenReturn(recruiting);
        when(applicantRepository.findApplicantByApplicantEmailAndRecruiting("applicant@test.com", recruiting))
                .thenReturn(Optional.of(applicant));

        ApplicantException exception = assertThrows(ApplicantException.class,
                () -> applicantService.updateApplicant(1L, request));

        assertThat(exception.getExceptionType()).isEqualTo(ApplicantExceptionType.NOT_MATCH_PROGRESS);
        verify(applicantRepository, never()).save(any(Applicant.class));
    }

    private Recruiting createRecruiting(Long userId, String status) {
        Member member = Member.builder()
                .companyName("테스트 회사")
                .companyPhoneNumber("02-1234-5678")
                .email("corp@test.com")
                .password("password")
                .role("USER")
                .build();
        ReflectionTestUtils.setField(member, "id", userId);

        Recruiting recruiting = Recruiting.builder()
                .member(member)
                .title("백엔드 개발자")
                .task("서버 개발")
                .status(status)
                .build();
        ReflectionTestUtils.setField(recruiting, "id", 1L);
        return recruiting;
    }
}

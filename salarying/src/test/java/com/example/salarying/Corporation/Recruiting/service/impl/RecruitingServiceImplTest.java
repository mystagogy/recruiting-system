package com.example.salarying.Corporation.Recruiting.service.impl;

import com.example.salarying.Corporation.Recruiting.dto.RecruitingDTO;
import com.example.salarying.Corporation.Recruiting.entity.Progress;
import com.example.salarying.Corporation.Recruiting.entity.Recruiting;
import com.example.salarying.Corporation.Recruiting.exception.RecruitingException;
import com.example.salarying.Corporation.Recruiting.exception.RecruitingExceptionType;
import com.example.salarying.Corporation.Recruiting.repository.ProgressRepository;
import com.example.salarying.Corporation.Recruiting.repository.RecruitingRepository;
import com.example.salarying.Corporation.User.entity.Member;
import com.example.salarying.Corporation.User.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitingServiceImplTest {

    @Mock
    private RecruitingRepository recruitingRepository;

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RecruitingServiceImpl recruitingService;

    @Test
    void insertRecruiting_savesRecruitingAndProgress_whenRequestIsValid() {
        Member member = createMember(1L, "corp@test.com");
        RecruitingDTO.RecruitingRequest request = RecruitingDTO.RecruitingRequest.builder()
                .title("백엔드 개발자")
                .task("서버 개발")
                .document(true)
                .firstRound(true)
                .secondRound(false)
                .finalRound(true)
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(recruitingRepository.save(any(Recruiting.class))).thenAnswer(invocation -> {
            Recruiting recruiting = invocation.getArgument(0);
            ReflectionTestUtils.setField(recruiting, "id", 10L);
            return recruiting;
        });

        RecruitingDTO.RecruitingResponse response = recruitingService.insertRecruiting(1L, request);

        ArgumentCaptor<Progress> progressCaptor = ArgumentCaptor.forClass(Progress.class);
        verify(progressRepository).save(progressCaptor.capture());

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("백엔드 개발자");
        assertThat(response.getTask()).isEqualTo("서버 개발");
        assertThat(response.getStatus()).isEqualTo("서류전형");
        assertThat(progressCaptor.getValue().getHasDocument()).isTrue();
        assertThat(progressCaptor.getValue().getHasFirstRound()).isTrue();
        assertThat(progressCaptor.getValue().getHasSecondRound()).isFalse();
        assertThat(progressCaptor.getValue().getHasFinalRound()).isTrue();
    }

    @Test
    void updateStatus_updatesRecruitingStatus_whenRequestIsValid() {
        Recruiting recruiting = Recruiting.builder()
                .member(createMember(1L, "corp@test.com"))
                .title("백엔드 개발자")
                .task("서버 개발")
                .status("서류전형")
                .build();
        ReflectionTestUtils.setField(recruiting, "id", 3L);

        RecruitingDTO.StatusRequest request = RecruitingDTO.StatusRequest.builder()
                .recruitingId(3L)
                .status("1차전형")
                .build();

        when(recruitingRepository.findRecruitingByIdAndAndMember_Id(3L, 1L)).thenReturn(Optional.of(recruiting));
        when(recruitingRepository.save(recruiting)).thenReturn(recruiting);

        RecruitingDTO.RecruitingResponse response = recruitingService.updateStatus(1L, request);

        assertThat(response.getStatus()).isEqualTo("1차전형");
        assertThat(recruiting.getStatus()).isEqualTo("1차전형");
    }

    @Test
    void checkStatusDTO_throwsRecruitingException_whenStatusIsInvalid() {
        RecruitingDTO.StatusRequest request = RecruitingDTO.StatusRequest.builder()
                .recruitingId(1L)
                .status("면접대기")
                .build();

        RecruitingException exception = assertThrows(RecruitingException.class,
                () -> recruitingService.checkStatusDTO(request));

        assertThat(exception.getExceptionType()).isEqualTo(RecruitingExceptionType.NOT_STATUS_FORMAT);
    }

    private Member createMember(Long id, String email) {
        Member member = Member.builder()
                .companyName("테스트 회사")
                .companyPhoneNumber("02-1234-5678")
                .email(email)
                .password("password")
                .role("USER")
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}

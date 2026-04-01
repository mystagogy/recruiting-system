package com.example.salarying.Admin.Terms.service;

import com.example.salarying.Admin.Terms.dto.TermsDTO;
import com.example.salarying.Admin.Terms.entity.Terms;
import com.example.salarying.Admin.Terms.exception.TermsException;
import com.example.salarying.Admin.Terms.exception.TermsExceptionType;
import com.example.salarying.Admin.Terms.repository.TermsRepository;
import com.example.salarying.Admin.User.entity.Admin;
import com.example.salarying.Admin.User.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TermsServiceImplTest {

    @Mock
    private TermsRepository termsRepository;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private TermsServiceImpl termsService;

    @Test
    void insertTerm_savesTermWithMappedType_whenVersionIsUnique() {
        Admin admin = Admin.builder()
                .id(1L)
                .adminName("관리자")
                .adminEmail("admin@test.com")
                .adminPassword("password")
                .role("ADMIN")
                .build();
        TermsDTO.AddTermRequest request = new TermsDTO.AddTermRequest("service", "서비스 이용약관", "1.0", "본문");

        when(termsRepository.existsTermsByTypeAndVersion("서비스 이용약관", "1.0")).thenReturn(false);
        when(adminService.findAdminById(1L)).thenReturn(admin);

        String result = termsService.insertTerm(1L, request);

        ArgumentCaptor<Terms> termsCaptor = ArgumentCaptor.forClass(Terms.class);
        verify(termsRepository).save(termsCaptor.capture());

        assertThat(result).isEqualTo("약관 등록 완료");
        assertThat(request.getType()).isEqualTo("서비스 이용약관");
        assertThat(termsCaptor.getValue().getType()).isEqualTo("서비스 이용약관");
        assertThat(termsCaptor.getValue().getStatus()).isEqualTo("비공개");
        assertThat(termsCaptor.getValue().getAdmin()).isEqualTo(admin);
    }

    @Test
    void changeStatus_switchesOpenedTerm_whenForceIsTrue() {
        Terms current = Terms.builder()
                .id(2L)
                .admin(Admin.builder().id(1L).adminName("관리자").adminEmail("admin@test.com").adminPassword("password").role("ADMIN").build())
                .type("서비스 이용약관")
                .agreementTitle("신규 약관")
                .agreementContent("본문")
                .version("1.1")
                .status("비공개")
                .build();
        Terms oldOpened = Terms.builder()
                .id(1L)
                .admin(Admin.builder().id(1L).adminName("관리자").adminEmail("admin@test.com").adminPassword("password").role("ADMIN").build())
                .type("서비스 이용약관")
                .agreementTitle("기존 약관")
                .agreementContent("본문")
                .version("1.0")
                .status("공개")
                .build();
        TermsDTO.StatusRequest request = TermsDTO.StatusRequest.builder()
                .Id(2L)
                .status("공개")
                .force(true)
                .build();

        when(termsRepository.findById(2L)).thenReturn(Optional.of(current));
        when(termsRepository.existsByTypeAndStatus("서비스 이용약관", "공개")).thenReturn(true);
        when(termsRepository.findByTypeAndStatus("서비스 이용약관", "공개")).thenReturn(oldOpened);

        String result = termsService.changeStatus(request);

        assertThat(result).isEqualTo("변경 완료");
        assertThat(current.getStatus()).isEqualTo("공개");
        assertThat(oldOpened.getStatus()).isEqualTo("비공개");
        verify(termsRepository).save(oldOpened);
        verify(termsRepository).save(current);
    }

    @Test
    void updateTerm_updatesWriterAndContent_whenTermExists() {
        Admin updater = Admin.builder()
                .id(2L)
                .adminName("수정자")
                .adminEmail("editor@test.com")
                .adminPassword("password")
                .role("ADMIN")
                .build();
        Terms terms = Terms.builder()
                .id(1L)
                .admin(Admin.builder().id(1L).adminName("기존 작성자").adminEmail("old@test.com").adminPassword("password").role("ADMIN").build())
                .type("서비스 이용약관")
                .agreementTitle("기존 제목")
                .agreementContent("기존 본문")
                .version("1.0")
                .status("비공개")
                .build();
        TermsDTO.UpdateRequest request = TermsDTO.UpdateRequest.builder()
                .Id(1L)
                .version("1.1")
                .title("수정된 제목")
                .content("수정된 본문")
                .build();

        when(adminService.findAdminById(2L)).thenReturn(updater);
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        String result = termsService.updateTerm(2L, request);

        assertThat(result).isEqualTo("변경 완료");
        assertThat(terms.getAdmin()).isEqualTo(updater);
        assertThat(terms.getAgreementTitle()).isEqualTo("수정된 제목");
        assertThat(terms.getAgreementContent()).isEqualTo("수정된 본문");
        assertThat(terms.getVersion()).isEqualTo("1.1");
        verify(termsRepository).save(terms);
    }

    @Test
    void findType_throwsTermsException_whenKeywordIsInvalid() {
        TermsException exception = assertThrows(TermsException.class, () -> termsService.findType("invalid"));

        assertThat(exception.getExceptionType()).isEqualTo(TermsExceptionType.INVALID_TYPE);
    }
}

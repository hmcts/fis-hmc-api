package uk.gov.hmcts.reform.hmc.api.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.Categories;
import uk.gov.hmcts.reform.hmc.api.model.response.Category;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService1;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class HearingsControllerTest {
    @Mock private HearingsService1 hearingsService;
    @Mock private HearingsService hearingsServiceTest;

    @InjectMocks private HearingsController hearingsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void hearingsDataTest() throws JsonProcessingException {
        Categories categories = new Categories();
        List<Category> categoryList = new ArrayList<>();
        Category category = new Category();
        categoryList.add(category);
        categories.setListOfCategory(categoryList);
        when(hearingsService.getRefData(HearingsRequest.builder().build(), "authorisation"))
                .thenReturn(categories);
        ResponseEntity<Categories> hearingsData =
                hearingsController.getHearingsData("authorisation", new HearingsRequest());
        Categories categoryListActual = hearingsData.getBody();
        Assertions.assertEquals(1, categoryListActual.getListOfCategory().size());
    }

    @Test
    void shouldReturnHearingsTest() {
        Hearings caseHearingsData =
                Hearings.caseHearingsWith().caseRef("123").hmctsServiceCode("BBA3").build();
        when(hearingsServiceTest.getHearingsByCaseRefNo(anyString(), anyString(), anyString()))
                .thenReturn(caseHearingsData);
        Hearings caseHearings =
                hearingsController.getHearingsByCaseRefNo("authorisation", "serviceAuth", "123");
        Assertions.assertEquals("BBA3", caseHearings.getHmctsServiceCode());
    }
}

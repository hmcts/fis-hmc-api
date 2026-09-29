package uk.gov.hmcts.reform.hmc.api.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.hmc.api.utils.PublicCaseNameUtils.buildFl401PublicCaseName;

class PublicCaseNameUtilsTest {

    @Test
    void shouldBuildPublicCaseNameFromCaseReferenceAndSurnameInitials() {
        assertThat(buildFl401PublicCaseName("12345", "Smith", "Jones"))
            .isEqualTo("12345 S v J");
    }

    @Test
    void shouldTrimSurnamesAndSupportUnicodeInitials() {
        assertThat(buildFl401PublicCaseName("12345", "  éclair", "Łukasz"))
            .isEqualTo("12345 É v Ł");
    }

    @Test
    void shouldReturnEmptyNameForMissingOrInvalidValues() {
        assertThat(buildFl401PublicCaseName(null, "Smith", "Jones")).isEmpty();
        assertThat(buildFl401PublicCaseName(" ", "Smith", "Jones")).isEmpty();
        assertThat(buildFl401PublicCaseName("12345", null, "Jones")).isEmpty();
        assertThat(buildFl401PublicCaseName("12345", "", "Jones")).isEmpty();
        assertThat(buildFl401PublicCaseName("12345", "1Smith", "Jones")).isEmpty();
        assertThat(buildFl401PublicCaseName("12345", "Smith", "-Jones")).isEmpty();
    }
}

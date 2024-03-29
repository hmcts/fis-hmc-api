package uk.gov.hmcts.reform.hmc.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CustomEnumSerializer;

@RequiredArgsConstructor
@Getter
@JsonSerialize(using = CustomEnumSerializer.class)
public enum State {
    AWAITING_SUBMISSION_TO_HMCTS("AWAITING_SUBMISSION_TO_HMCTS", "Draft"),
    AWAITING_FL401_SUBMISSION_TO_HMCTS("AWAITING_FL401_SUBMISSION_TO_HMCTS", "Draft"),
    SUBMITTED_NOT_PAID("SUBMITTED_NOT_PAID", "Pending"),
    SUBMITTED_PAID("SUBMITTED_PAID", "Submitted"),
    AWAITING_RESUBMISSION_TO_HMCTS("AWAITING_RESUBMISSION_TO_HMCTS", "Returned"),
    CASE_ISSUE("CASE_ISSUE", "Case Issued"),
    CASE_WITHDRAWN("CASE_WITHDRAWN", "Withdrawn"),
    GATE_KEEPING("GATE_KEEPING", "Gatekeeping"),
    ALL_FINAL_ORDERS_ISSUED("ALL_FINAL_ORDERS_ISSUED", "Closed"),
    PREPARE_FOR_HEARING_CONDUCT_HEARING(
            "PREPARE_FOR_HEARING_CONDUCT_HEARING", "Prepare for hearing"),
    DELETED("DELETED", "Deleted"),
    DECISION_OUTCOME("DECISION_OUTCOME", "Hearing outcome");
    private final String value;
    private final String label;

    State(String value) {
        this.value = value;
        this.label = value;
    }

    public static State fromValue(final String value) {
        return tryFromValue(value)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Unable to map " + value + " to a case state"));
    }

    public static Optional<State> tryFromValue(final String value) {
        return Stream.of(values()).filter(state -> state.value.equalsIgnoreCase(value)).findFirst();
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static State getValue(String key) {
        return State.valueOf(key);
    }
}

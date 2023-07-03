package uk.gov.hmcts.reform.hmc.api.utils;

public final class Constants {

    public static final String EMPTY_STRING = " ";
    public static final Boolean TRUE = true;
    public static final Boolean FALSE = false;

    public static final String LISTED = "LISTED";

    public static final String COMPLETED = "COMPLETED";

    public static final String ADJOURNED = "ADJOURNED";

    public static final String CANCELLED = "CANCELLED";
    public static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    public static final String FL401_APPLICANT_TABLE = "fl401ApplicantTable";
    public static final String FL401_RESPONDENT_TABLE = "fl401RespondentTable";
    public static final String APPLICANT_CASE_NAME = "applicantCaseName";

    public static final String APPLICANTS_FL401 = "applicantsFL401";

    public static final String RESPONDENTS_FL401 = "respondentsFL401";

    public static final String CASE_MNGEMNT_LOC = "caseManagementLocation";

    public static final String APPLICANT_CASE_NAME_TEST_VALUE = "Test Case 1 DA 31";

    public static final String ISSUE_DATE = "issueDate";
    public static final String BBA3 = "BBA3";
    public static final String FL401 = "FL401";
    public static final String C100 = "C100";
    public static final String RE_MINOR = "Re-Minor";
    public static final String EMPTY = "";
    public static final String PRIVATE_LAW = "PrivateLaw";
    public static final String UNDERSCORE = "_";
    public static final String SCREEN_FLOW = "screenFlow";
    public static final String CASE_MANAGEMENT_LOCATION = "20262";

    public static final String AND = " and ";

    public static final String COURT = "court";

    public static final String CASE_TYPE = "caseType";
    public static final String CATEGORY_VALUE = "ABA5-PRL";
    public static final String CASE_SUB_TYPE = "caseSubType";
    public static final String ABA5 = "ABA5";
    public static final String APPLICANT = "APPL";

    public static final String APPLICANTS = "applicants";

    public static final String ORGANISATION = "LGRP";
    public static final String RESPONDENT = "RESP";

    public static final String RESPONDENTS = "respondents";
    public static final String HEARING_TYPE = "";

    public static final String TEST = "test";

    public static final String HEARING_PRIORITY = "Standard";

    public static final String LAST_NAME = "lastName";

    public static final String CASE_LINKS = "caseLinks";

    public static final String VALUE = "value";

    public static final String REASON_FOR_LINK = "ReasonForLink";

    public static final String REASON = "Reason";

    public static final String REASON_TEST_VALUE = "CLRC017";

    public static final String ORGANISATION_TEST_ID = "orgTestId";

    public static final String ORGANISATION_TEST_NAME = "orgTestName";

    public static final String REP_FIRST_NAME_TEST_VALUE = "rep first name test";

    public static final String REP_LAST_NAME_TEST_VALUE = "rep last name test";

    public static final int ONE = 1;

    public static final int TWO = 2;

    public static final String LISTING_COMMENTS =
            "Applicant Name/Respondent Name: Multiple Interpreters. See case flags.";
    public static final String PF0002 = "PF0002";
    public static final String PF0007 = "PF0007";
    public static final String PF0013 = "PF0013";
    public static final String PF0018 = "PF0018";

    public static final String PF0020 = "PF0020";

    public static final String SM0002 = "SM0002";

    public static final String RA0042 = "RA0042"; // Sign  Language Interpreter

    public static final String PF0015 = "PF0015"; // Language Interpreter

    public static final String RA = "RA";

    public static final String SM = "SM";

    public static final String PLUS_SIGN = " + ";

    public static final String PROCESSING_REQUEST_AFTER_AUTHORIZATION =
            "processing request after authorization";

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    public static final String AUTHORIZATION = "Authorization";

    public static final String CCD_DEMO_LINK =
            "https://manage-case-hearings-int.demo.platform.hmcts.net/cases/case-details/";
    public static final String DEMO = "demo";

    public static final String CASE_FILE_VIEW = "#Case File View";

    public static final String FAMILY_COURT_TYPE_ID = "18";

    public static final String OPEN = "Open";

    public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";

    public static final String ROLE_ASSIGNMENT_HEARING_VIEWER = "hearing-viewer";

    public static final String ROLE_ASSIGNMENT_HEARING_MANAGER = "hearing-manager";

    public static final String ROLE_ASSIGNMENT_ROLE_REQUEST_PROCESS = "private-law-system-users";

    public static final String ROLE_ASSIGNMENT_ROLE_REQUEST_REFERENCE = "private-law-hearings-system-user";

    public static final String ROLE_ASSIGNMENT_ROLE_TYPE = "ORGANISATION";

    public static final String ROLE_ASSIGNMENT_CLASSIFICATION = "PUBLIC";

    public static final String ROLE_ASSIGNMENT_ROLE_CATEGORY = "SYSTEM";

    public static final String ROLE_ASSIGNMENT_GRANT_TYPE = "STANDARD";

    public static final String ROLE_ASSIGNMENT_ATTRIBUTE_JURISDICTION = "PRIVATELAW";

    public static final String ROLE_ASSIGNMENT_ATTRIBUTE_CASE_TYPE = "PRLAPPS";

    public static final String ROLE_ASSIGNMENT_ACTOR_ID_TYPE = "IDAM";

    public static final String AUTH_TOKEN_AAT = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3Z0lPVEFGb2pFNHJDL2ZiZUt1M0k9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJ4dWlDYXNld29ya2VyQUFUMDVAbWFpbGluYXRvci5jb20iLCJjdHMiOiJPQVVUSDJfU1RBVEVMRVNTX0dSQU5UIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiYmE5OWViMTktYmI0Yy00NTE2LWJmZjktN2NmNDA4ZTM0ZDAyLTIyNjIyNjk0Iiwic3VibmFtZSI6Inh1aUNhc2V3b3JrZXJBQVQwNUBtYWlsaW5hdG9yLmNvbSIsImlzcyI6Imh0dHBzOi8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tYWF0Mi5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvcmVhbG1zL3Jvb3QvcmVhbG1zL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoibG5jcFlkbUFab295eHJHNDcybXdLRkdZd1E0Iiwibm9uY2UiOiJTS29ldjlncFp0TUVaOG5MamRmdEtJeGFlYTNvT080SkdUaU9LY1NwMjVBIiwiYXVkIjoieHVpd2ViYXBwIiwibmJmIjoxNjg4MzgxNTE5LCJncmFudF90eXBlIjoiYXV0aG9yaXphdGlvbl9jb2RlIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIiwiY3JlYXRlLXVzZXIiLCJtYW5hZ2UtdXNlciIsInNlYXJjaC11c2VyIl0sImF1dGhfdGltZSI6MTY4ODM4MTUxMSwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE2ODg0MTAzMTksImlhdCI6MTY4ODM4MTUxOSwiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6IjkzQ1ozcDV6bnRpN2w1dUExZk5fV3JtUGhwbyJ9.thxKZ21gp-_iqf_T3-0PiTKVh3uleF5NdV70HZHVMqviZ0bHUKWCTgAaKbLq0aVsiSjPy15GiepgPXg5yOdahWv5sHlrfUO0o7lerp7-AWtEF1b8mrVHZsgD1ZEYxXXbf0_bFHJaMz25v0bcMCd0X0F_lA2PYeruwp-drHxtcYDWyyrOXGdzHR6Fl5hdzkuXrYqa3pdT4ZwbKE8ttWmEquVgeL2xrrgl4d4HOvYuydYOP5aq9mv8J3LpE-CLljXOT_oFEG-PjlcyYNwLRIB4tfG4WhZBth6JuKN-TRnUCp0ENjevtZo32ENLqcMUrqY77V_K30CgjPm8oDolkaKrhQ";

    public static final String S2S_TOKEN_AAT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmaXNfaG1jX2FwaSIsImV4cCI6MTY4ODQwNzU2MH0.JcMT1BLjqoVnAUg9uPvXCFwX0FNfCHQW2V73l0fBij5ggBDRtW5Iz_-mtUuKkAgN15BfXk3GThUazzUh4FlYHg";

    private Constants() {}
}

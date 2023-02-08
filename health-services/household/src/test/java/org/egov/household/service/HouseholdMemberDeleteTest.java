package org.egov.household.service;

import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.validator.Validator;
import org.egov.household.config.HouseholdMemberConfiguration;
import org.egov.household.helper.HouseholdMemberRequestTestBuilder;
import org.egov.household.household.member.validators.HmHouseholdHeadValidator;
import org.egov.household.household.member.validators.HmIndividualValidator;
import org.egov.household.household.member.validators.HmIsDeletedValidator;
import org.egov.household.household.member.validators.HmNonExistentEntityValidator;
import org.egov.household.household.member.validators.HmNullIdValidator;
import org.egov.household.household.member.validators.HmRowVersionValidator;
import org.egov.household.household.member.validators.HmUniqueEntityValidator;
import org.egov.household.repository.HouseholdMemberRepository;
import org.egov.household.web.models.HouseholdMember;
import org.egov.household.web.models.HouseholdMemberBulkRequest;
import org.egov.household.web.models.HouseholdMemberRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HouseholdMemberDeleteTest {

    @InjectMocks
    HouseholdMemberService householdMemberService;

    @Mock
    HouseholdMemberRepository householdMemberRepository;

    @Mock
    HouseholdMemberConfiguration householdMemberConfiguration;

    @Mock
    HouseholdService householdService;

    @Mock
    private ServiceRequestClient serviceRequestClient;

    @Mock
    private HmNullIdValidator hmNullIdValidator;

    @Mock
    private HmNonExistentEntityValidator hmNonExistentEntityValidator;


    @Mock
    private HmUniqueEntityValidator hmUniqueEntityValidator;

    @Mock
    private HmIsDeletedValidator hmIsDeletedValidator;

    @Mock
    private HmRowVersionValidator hmRowVersionValidator;

    @Mock
    private HmIndividualValidator hmIndividualValidator;

    @Mock
    private HmHouseholdHeadValidator hmHouseholdHeadValidator;

    @Mock
    private HouseholdMemberEnrichmentService householdMemberEnrichmentService;

    private List<Validator<HouseholdMemberBulkRequest, HouseholdMember>> validators;


    @BeforeEach
    void setUp() {
        validators = Arrays.asList(
                hmNullIdValidator,
                hmNonExistentEntityValidator,
                hmUniqueEntityValidator,
                hmRowVersionValidator,
                hmIsDeletedValidator,
                hmIndividualValidator,
                hmHouseholdHeadValidator);
        ReflectionTestUtils.setField(householdMemberService, "validators", validators);
        lenient().when(householdMemberConfiguration.getCreateTopic()).thenReturn("create-topic");
        lenient().when(householdMemberConfiguration.getUpdateTopic()).thenReturn("update-topic");
        lenient().when(householdMemberConfiguration.getDeleteTopic()).thenReturn("delete-topic");

    }

    @Test
    @DisplayName("should delete the individual and related entities")
    void shouldDeleteTheIndividualAndRelatedEntities() {
        HouseholdMemberRequest request = HouseholdMemberRequestTestBuilder.builder()
                .withRequestInfo()
                .withDeletedHouseholdMember()
                .build();

        householdMemberService.delete(request);
        verify(householdMemberRepository, times(1)).save(anyList(), anyString());

    }


}

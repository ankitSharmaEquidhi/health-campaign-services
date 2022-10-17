package org.digit.health.sync.orchestrator.client;

import org.digit.health.sync.context.metric.SyncStepMetric;
import org.digit.health.sync.context.step.DeliverySyncStep;
import org.digit.health.sync.context.step.RegistrationSyncStep;
import org.digit.health.sync.context.step.SyncStep;
import org.digit.health.sync.helper.SyncStepMetricTestBuilder;
import org.digit.health.sync.helper.SyncUpDataListTestBuilder;
import org.digit.health.sync.orchestrator.SyncOrchestrator;
import org.digit.health.sync.orchestrator.client.enums.SyncLogStatus;
import org.digit.health.sync.orchestrator.client.metric.SyncLogMetric;
import org.digit.health.sync.repository.SyncErrorDetailsLogRepository;
import org.digit.health.sync.web.models.SyncUpDataList;
import org.digit.health.sync.web.models.dao.SyncErrorDetailsLogData;
import org.digit.health.sync.web.models.request.DeliveryMapper;
import org.digit.health.sync.web.models.request.HouseholdRegistrationMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCampaignSyncOrchestratorClientTest {

    @Mock
    private SyncOrchestrator<Map<Class<? extends SyncStep>, Object>,
            List<SyncStepMetric>> syncOrchestrator;

    @Mock
    private SyncErrorDetailsLogRepository syncErrorDetailsLogRepository;

    @Test
    @DisplayName("health camp sync orchestrator client should call health camp sync orchestrator to orchestrate")
    void testThatHealthCampSyncOrchestratorClientCallsHealthCampSyncOrchestratorToOrchestrate() {
        HealthCampaignSyncOrchestratorClient syncOrchestratorClient =
                new HealthCampaignSyncOrchestratorClient(syncOrchestrator, syncErrorDetailsLogRepository);
        SyncUpDataList syncUpDataList = SyncUpDataListTestBuilder.builder()
                .withOneHouseholdRegistrationAndDelivery()
                .build();
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("syncUpDataList", syncUpDataList);
        Map<Class<? extends SyncStep>, Object> stepToPayloadMap = getStepToPayloadMap();
        when(syncOrchestrator.orchestrate(stepToPayloadMap)).thenReturn(Collections.emptyList());

        syncOrchestratorClient.orchestrate(payloadMap);

        verify(syncOrchestrator, times(1)).orchestrate(stepToPayloadMap);
    }

    @Test
    @DisplayName("health camp sync orchestrator client should orchestrate and return aggregate metrics")
    void testThatHealthCampSyncOrchestratorClientOrchestratesAndReturnsAggregateMetrics() {
        HealthCampaignSyncOrchestratorClient syncOrchestratorClient =
                new HealthCampaignSyncOrchestratorClient(syncOrchestrator, syncErrorDetailsLogRepository);
        SyncUpDataList syncUpDataList = SyncUpDataListTestBuilder.builder()
                .withOneHouseholdRegistrationAndDelivery()
                .build();
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("syncUpDataList", syncUpDataList);
        Map<Class<? extends SyncStep>, Object> stepToPayloadMap = getStepToPayloadMap();
        List<SyncStepMetric> syncStepMetrics = new ArrayList<>();
        syncStepMetrics.add(SyncStepMetricTestBuilder.builder()
                .withCompletedRegistrationStep().build());
        syncStepMetrics.add(SyncStepMetricTestBuilder.builder()
                .withCompletedDeliveryStep().build());
        SyncLogMetric syncLogMetricExpected = SyncLogMetric.builder()
                .syncLogStatus(SyncLogStatus.COMPLETE)
                .errorCount(0)
                .successCount(2)
                .totalCount(2)
                .build();
        when(syncOrchestrator.orchestrate(stepToPayloadMap)).thenReturn(syncStepMetrics);

        SyncLogMetric syncLogMetric = syncOrchestratorClient
                .orchestrate(payloadMap);

        assertEquals(syncLogMetricExpected, syncLogMetric);
        verify(syncErrorDetailsLogRepository, times(0))
                .save(any(SyncErrorDetailsLogData.class));
    }

    @Test
    @DisplayName("should orchestrate and persist error details metrics")
    void testThatHealthCampSyncOrchestratorClientOrchestrateAndPersistErrorDetailsMetrics() {
        HealthCampaignSyncOrchestratorClient syncOrchestratorClient =
                new HealthCampaignSyncOrchestratorClient(syncOrchestrator, syncErrorDetailsLogRepository);
        SyncUpDataList syncUpDataList = SyncUpDataListTestBuilder.builder()
                .withOneHouseholdRegistrationAndDelivery()
                .build();
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("syncUpDataList", syncUpDataList);
        payloadMap.put("requestInfo", RequestInfo.builder()
                        .userInfo(User.builder()
                                .uuid("some-uuid")
                                .build())
                .build());
        Map<Class<? extends SyncStep>, Object> stepToPayloadMap = getStepToPayloadMap();
        List<SyncStepMetric> syncStepMetrics = new ArrayList<>();
        syncStepMetrics.add(SyncStepMetricTestBuilder.builder()
                .withCompletedRegistrationStep().build());
        syncStepMetrics.add(SyncStepMetricTestBuilder.builder()
                .withFailedDeliveryStep().build());
        SyncLogMetric syncLogMetricExpected = SyncLogMetric.builder()
                .syncLogStatus(SyncLogStatus.PARTIALLY_COMPLETE)
                .errorCount(1)
                .successCount(1)
                .totalCount(2)
                .build();
        when(syncOrchestrator.orchestrate(stepToPayloadMap)).thenReturn(syncStepMetrics);

        SyncLogMetric syncLogMetric = syncOrchestratorClient
                .orchestrate(payloadMap);

        assertEquals(syncLogMetricExpected, syncLogMetric);
        verify(syncErrorDetailsLogRepository, times(1))
                .save(any(SyncErrorDetailsLogData.class));
    }

    @Test
    @DisplayName("health camp sync orchestrator client should orchestrate a single unrelated item")
    void testThatHealthCampSyncOrchestratorClientOrchestratesSingleUnrelatedItem() {
        HealthCampaignSyncOrchestratorClient syncOrchestratorClient =
                new HealthCampaignSyncOrchestratorClient(syncOrchestrator, syncErrorDetailsLogRepository);
        SyncUpDataList syncUpDataList = SyncUpDataListTestBuilder.builder()
                .withOneHouseholdRegistration()
                .build();
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("syncUpDataList", syncUpDataList);
        Map<Class<? extends SyncStep>, Object> stepToPayloadMap = getSingleItemStepToPayloadMap();
        List<SyncStepMetric> syncStepMetrics = new ArrayList<>();
        syncStepMetrics.add(SyncStepMetricTestBuilder.builder()
                .withCompletedRegistrationStep().build());
        SyncLogMetric syncLogMetricExpected = SyncLogMetric.builder()
                .syncLogStatus(SyncLogStatus.COMPLETE)
                .errorCount(0)
                .successCount(1)
                .totalCount(1)
                .build();
        when(syncOrchestrator.orchestrate(stepToPayloadMap)).thenReturn(syncStepMetrics);

        SyncLogMetric syncLogMetric = syncOrchestratorClient
                .orchestrate(payloadMap);

        assertEquals(syncLogMetricExpected, syncLogMetric);
    }

    @Test
    @DisplayName("health camp sync orchestrator client should orchestrate a different single unrelated item")
    void testThatHealthCampSyncOrchestratorClientOrchestratesSingleUnrelatedItemUseCase2() {
        HealthCampaignSyncOrchestratorClient syncOrchestratorClient =
                new HealthCampaignSyncOrchestratorClient(syncOrchestrator, syncErrorDetailsLogRepository);
        SyncUpDataList syncUpDataList = SyncUpDataListTestBuilder.builder()
                .withOneDelivery()
                .build();
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("syncUpDataList", syncUpDataList);
        Map<Class<? extends SyncStep>, Object> stepToPayloadMap =
                getSingleItemStepToPayloadMapDifferent();
        List<SyncStepMetric> syncStepMetrics = new ArrayList<>();
        syncStepMetrics.add(SyncStepMetricTestBuilder.builder()
                .withCompletedDeliveryStep().build());
        SyncLogMetric syncLogMetricExpected = SyncLogMetric.builder()
                .syncLogStatus(SyncLogStatus.COMPLETE)
                .errorCount(0)
                .successCount(1)
                .totalCount(1)
                .build();
        when(syncOrchestrator.orchestrate(stepToPayloadMap)).thenReturn(syncStepMetrics);

        SyncLogMetric syncLogMetric = syncOrchestratorClient
                .orchestrate(payloadMap);

        assertEquals(syncLogMetricExpected, syncLogMetric);
    }

    @Test
    @DisplayName("health camp sync orchestrator client should orchestrate for multiple payloads and return aggregate metrics")
    void testThatHealthCampSyncOrchestratorClientOrchestratesForMultiplePayloadsAndReturnsAggregateMetrics() {
        HealthCampaignSyncOrchestratorClient syncOrchestratorClient =
                new HealthCampaignSyncOrchestratorClient(syncOrchestrator, syncErrorDetailsLogRepository);
        SyncUpDataList syncUpDataList = SyncUpDataListTestBuilder.builder()
                .withTwoHouseholdRegistrationAndDelivery()
                .build();
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("syncUpDataList", syncUpDataList);
        payloadMap.put("requestInfo", RequestInfo.builder()
                .userInfo(User.builder()
                        .uuid("some-uuid")
                        .build())
                .build());
        Map<Class<? extends SyncStep>, Object> stepToPayloadMap = getStepToPayloadMap();
        Map<Class<? extends SyncStep>, Object> secondStepToPayloadMap = getTwoItemStepToPayloadMap();
        List<SyncStepMetric> firstSyncStepMetrics = new ArrayList<>();
        firstSyncStepMetrics.add(SyncStepMetricTestBuilder.builder()
                .withCompletedRegistrationStep().build());
        firstSyncStepMetrics.add(SyncStepMetricTestBuilder.builder()
                .withCompletedDeliveryStep().build());
        List<SyncStepMetric> secondSyncStepMetrics = new ArrayList<>();
        secondSyncStepMetrics.add(SyncStepMetricTestBuilder.builder()
                .withCompletedRegistrationStep().build());
        secondSyncStepMetrics.add(SyncStepMetricTestBuilder.builder()
                .withFailedDeliveryStep().build());
        SyncLogMetric syncLogMetricExpected = SyncLogMetric.builder()
                .syncLogStatus(SyncLogStatus.PARTIALLY_COMPLETE)
                .errorCount(1)
                .successCount(3)
                .totalCount(4)
                .build();
        when(syncOrchestrator.orchestrate(stepToPayloadMap)).thenReturn(firstSyncStepMetrics);
        when(syncOrchestrator.orchestrate(secondStepToPayloadMap)).thenReturn(secondSyncStepMetrics);

        SyncLogMetric syncLogMetric = syncOrchestratorClient
                .orchestrate(payloadMap);

        assertEquals(syncLogMetricExpected, syncLogMetric);
        verify(syncErrorDetailsLogRepository, times(1))
                .save(any(SyncErrorDetailsLogData.class));
    }

    private static Map<Class<? extends SyncStep>, Object> getStepToPayloadMap() {
        Map<Class<? extends SyncStep>, Object> stepToPayloadMap = new HashMap<>();
        stepToPayloadMap.put(RegistrationSyncStep.class,
                HouseholdRegistrationMapper.INSTANCE.toRequest(SyncUpDataListTestBuilder
                        .getHouseholdRegistration()));
        stepToPayloadMap.put(DeliverySyncStep.class,
                DeliveryMapper.INSTANCE.toRequest(SyncUpDataListTestBuilder.getDelivery()));
        return stepToPayloadMap;
    }

    private static Map<Class<? extends SyncStep>, Object> getSingleItemStepToPayloadMap() {
        Map<Class<? extends SyncStep>, Object> stepToPayloadMap = new HashMap<>();
        stepToPayloadMap.put(RegistrationSyncStep.class,
                HouseholdRegistrationMapper.INSTANCE.toRequest(SyncUpDataListTestBuilder
                        .getHouseholdRegistration()));
        return stepToPayloadMap;
    }

    private static Map<Class<? extends SyncStep>, Object> getSingleItemStepToPayloadMapDifferent() {
        Map<Class<? extends SyncStep>, Object> stepToPayloadMap = new HashMap<>();
        stepToPayloadMap.put(DeliverySyncStep.class,
                DeliveryMapper.INSTANCE.toRequest(SyncUpDataListTestBuilder.getDelivery()));
        return stepToPayloadMap;
    }

    private static Map<Class<? extends SyncStep>, Object> getTwoItemStepToPayloadMap() {
        Map<Class<? extends SyncStep>, Object> stepToPayloadMap = new HashMap<>();
        stepToPayloadMap.put(RegistrationSyncStep.class,
                HouseholdRegistrationMapper.INSTANCE.toRequest(SyncUpDataListTestBuilder
                        .getHouseholdRegistration("some-different-id")));
        stepToPayloadMap.put(DeliverySyncStep.class,
                DeliveryMapper.INSTANCE.toRequest(SyncUpDataListTestBuilder
                        .getDelivery("some-different-id")));
        return stepToPayloadMap;
    }
}

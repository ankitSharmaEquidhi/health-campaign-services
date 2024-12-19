package org.egov.processor.util;

import org.apache.poi.ss.usermodel.*;
import org.egov.processor.web.models.Locale;
import org.egov.processor.web.models.LocaleResponse;
import org.egov.processor.web.models.PlanConfigurationRequest;
import org.egov.processor.web.models.ResourceMapping;
import org.egov.processor.web.models.census.Census;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.egov.tracer.model.CustomException;

import static org.egov.processor.config.ServiceConstants.FACILITY_NAME;
import static org.egov.processor.config.ServiceConstants.HCM_MICROPLAN_SERVING_FACILITY;

@Component
public class OutputEstimationGenerationUtil {

    private LocaleUtil localeUtil;

    private ParsingUtil parsingUtil;

    private EnrichmentUtil enrichmentUtil;

    public OutputEstimationGenerationUtil(LocaleUtil localeUtil, ParsingUtil parsingUtil, EnrichmentUtil enrichmentUtil) {
        this.localeUtil = localeUtil;
        this.parsingUtil = parsingUtil;
        this.enrichmentUtil = enrichmentUtil;
    }

    public void processOutputFile(Workbook workbook, PlanConfigurationRequest request) {
        LocaleResponse localeResponse = localeUtil.searchLocale(request);
        //removing readme sheet
        for (int i = workbook.getNumberOfSheets() - 1; i >= 0; i--) {
            Sheet sheet = workbook.getSheetAt(i);
            if (!parsingUtil.isSheetAllowedToProcess(request, sheet.getSheetName(), localeResponse)) {
                workbook.removeSheetAt(i);
            }
        }

        //
        Map<String, String> localizationCodeAndMessageMap = localeResponse.getMessages().stream().collect(Collectors.toMap(Locale::getCode, Locale::getMessage));

        for(Sheet sheet: workbook) {
            processSheetForHeaderLocalization(sheet, localizationCodeAndMessageMap);
        }
    }

    public void processSheetForHeaderLocalization(Sheet sheet, Map<String, String> localizationCodeAndMessageMap) {
        // Fetch the header row from sheet
        Row row = sheet.getRow(0);
        if (parsingUtil.isRowEmpty(row)) throw new CustomException();


        //Iterate from the end, for every cell localize the header value
        for (int i = row.getLastCellNum() - 1; i >= 0; i--) {
            Cell headerColumn = row.getCell(i);

            if (headerColumn == null || headerColumn.getCellType() != CellType.STRING) {
                continue;
            }
            String headerColumnValue = headerColumn.getStringCellValue();

            // Exit the loop if the header column value is not in the localization map
            if (!localizationCodeAndMessageMap.containsKey(headerColumnValue)) {
                break;
            }

            // Update the cell value with the localized message
            headerColumn.setCellValue(localizationCodeAndMessageMap.get(headerColumnValue));
        }

    }

    /**
     * For each boundary code in the sheet being processed, adds the name of the facility mapped to that boundary code.
     *
     * @param workbook    the workbook.
     * @param request     the plan configuration request.
     * @param fileStoreId the associated file store ID.
     */
    public void addAssignedFacility(Workbook workbook, PlanConfigurationRequest request, String fileStoreId) {
        LocaleResponse localeResponse = localeUtil.searchLocale(request);

        String assignedFacilityColHeader = localeUtil.localeSearch(localeResponse.getMessages(), HCM_MICROPLAN_SERVING_FACILITY);

        assignedFacilityColHeader = assignedFacilityColHeader != null ? assignedFacilityColHeader : HCM_MICROPLAN_SERVING_FACILITY;

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (parsingUtil.isSheetAllowedToProcess(request, sheet.getSheetName(), localeResponse)) {

                // Get column index of assigned facility name in the sheet being processed
                Integer indexOfFacility = sheet.getRow(0).getLastCellNum() + 1;

                // Create a new column for assigned facility name
                Cell cell = sheet.getRow(0).createCell(indexOfFacility, CellType.STRING);
                cell.setCellValue(assignedFacilityColHeader);

                // Creating a map of MappedTo and MappedFrom values from resource mapping
                Map<String, String> mappedValues = request.getPlanConfiguration().getResourceMapping().stream()
                        .filter(f -> f.getFilestoreId().equals(fileStoreId))
                        .collect(Collectors.toMap(
                                ResourceMapping::getMappedTo,
                                ResourceMapping::getMappedFrom,
                                (existing, replacement) -> existing,
                                LinkedHashMap::new
                        ));

                // Get column index of boundary code in the sheet being processed
                Integer indexOfBoundaryCode = parsingUtil.getIndexOfBoundaryCode(0, sheet, mappedValues);

                // Get a list of boundary codes
                List<String> boundaryCodes = enrichmentUtil.getBoundaryCodesFromTheSheet(sheet, request, fileStoreId);

                //Getting census records for the list of boundaryCodes
                List<Census> censusList = enrichmentUtil.getCensusRecordsForEnrichment(request, boundaryCodes);

                // Create a map of boundary code to facility assigned for the boundary
                Map<String, String> boundaryCodeToFacility = censusList.stream()
                        .collect(Collectors.toMap(Census::getBoundaryCode, census -> (String) parsingUtil.extractFieldsFromJsonObject(census.getAdditionalDetails(), FACILITY_NAME)));

                // for each boundary code in the sheet add the name of the facility assigned to it.
                for (Row row : sheet) {

                    // Skip the header row and empty rows
                    if (row.getRowNum() == 0 || parsingUtil.isRowEmpty(row)) {
                        continue;
                    }

                    // Get the boundaryCode in the current row
                    Cell boundaryCodeCell = row.getCell(indexOfBoundaryCode);
                    String boundaryCode = boundaryCodeCell.getStringCellValue();

                    String facility = boundaryCodeToFacility.get(boundaryCode);

                    Cell facilityCell = row.getCell(indexOfFacility);
                    if (facilityCell == null) {
                        facilityCell = row.createCell(indexOfFacility, CellType.STRING);
                    }
                    facilityCell.setCellValue(facility);
                }
            }
        }

    }
}

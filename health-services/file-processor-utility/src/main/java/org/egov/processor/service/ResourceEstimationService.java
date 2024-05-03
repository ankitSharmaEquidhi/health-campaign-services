package org.egov.processor.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.processor.util.PlanConfigurationUtil;
import org.egov.processor.web.models.File;
import org.egov.processor.web.models.PlanConfiguration;
import org.egov.processor.web.models.PlanConfigurationRequest;
import org.egov.processor.web.models.PlanConfigurationSearchCriteria;
import org.egov.processor.web.models.PlanConfigurationSearchRequest;
import org.egov.processor.web.models.PlanRequest;
import org.springframework.stereotype.Service;

import static org.egov.processor.web.models.File.InputFileTypeEnum.EXCEL;
import static org.egov.processor.web.models.File.InputFileTypeEnum.GEOJSON;
import static org.egov.processor.web.models.File.InputFileTypeEnum.SHAPEFILE;

@Service
@Slf4j
public class ResourceEstimationService {

    private final PlanConfigurationUtil planConfigurationUtil;
    private final FileParser excelParser;
    private final FileParser geoJsonParser;
    private final FileParser shapeFileParser;

    public ResourceEstimationService(PlanConfigurationUtil planConfigurationUtil, FileParser excelParser, FileParser geoJsonParser, FileParser shapeFileParser) {
        this.planConfigurationUtil = planConfigurationUtil;
        this.excelParser = excelParser;
        this.geoJsonParser = geoJsonParser;
        this.shapeFileParser = shapeFileParser;
    }

    public void estimateResources(PlanConfigurationRequest planConfigurationRequest) {
        PlanConfiguration planConfiguration = planConfigurationRequest.getPlanConfiguration();
        // filter by templateIdentifier as pop
        File.InputFileTypeEnum fileType = planConfiguration.getFiles().get(0).getInputFileType();

        Map<File.InputFileTypeEnum, FileParser> parserMap = getInputFileTypeMap();
        FileParser parser = parserMap.computeIfAbsent(fileType, ft -> {
            throw new IllegalArgumentException("Unsupported file type: " + ft);
        });

        parser.parseFileData(planConfiguration, planConfiguration.getFiles().get(0).getFilestoreId());
    }

    public Map<File.InputFileTypeEnum, FileParser> getInputFileTypeMap()
    {
        Map<File.InputFileTypeEnum, FileParser> parserMap = new HashMap<>();
        parserMap.put(File.InputFileTypeEnum.EXCEL, excelParser);
        parserMap.put(File.InputFileTypeEnum.SHAPEFILE, shapeFileParser);
        parserMap.put(File.InputFileTypeEnum.GEOJSON, geoJsonParser);

        return parserMap;
    }
}


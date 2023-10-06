package org.egov.project.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.egov.project.web.models.MultiRoundConstants;

public class ProjectConstants {
    public static final String MASTER_TENANTS = "tenants";
    public static final String MDMS_TENANT_MODULE_NAME = "tenant";
    public static final String MDMS_COMMON_MASTERS_MODULE_NAME = "common-masters";
    public static final String MASTER_DEPARTMENT = "Department";
    public static final String MASTER_PROJECTTYPE = "ProjectType";
    //location
    public static final String MASTER_NATUREOFWORK = "NatureOfWork";
    public static final String CODE = "code";
    //General
    public static final String SEMICOLON = ":";
    public static final String DOT = ".";
    public static final String PROJECT_PARENT_HIERARCHY_SEPERATOR = ".";
    public static final String TASK_NOT_ALLOWED = "TASK_NOT_ALLOWED";

    public enum TaskStatus {
        BENEFICIARY_REFUSED("BENEFICIARY_REFUSED");
        private String value;

        TaskStatus(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static TaskStatus fromValue(String text) {
            for (TaskStatus status : TaskStatus.values()) {
                if (String.valueOf(status.value).equals(text)) {
                    return status;
                }
            }
            return null;
        }
    }

}

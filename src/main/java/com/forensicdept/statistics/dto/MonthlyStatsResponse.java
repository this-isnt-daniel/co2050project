package com.forensicdept.statistics.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MonthlyStatsResponse {
    private Summary summary;
    private MonthlyVolumes monthlyVolumes;
    private Distribution mlefBodyHarmDistribution;
    private Distribution mlefHurtCategoryDistribution;

    @Data
    @Builder
    public static class Summary {
        private long patients;
        private long mlef;
        private long mlr;
        private long labRequests;
        private long pmr;
    }

    @Data
    @Builder
    public static class MonthlyVolumes {
        private List<String> labels;
        private List<Dataset> datasets;
    }

    @Data
    @Builder
    public static class Dataset {
        private String label;
        private List<Long> data;
    }

    @Data
    @Builder
    public static class Distribution {
        private List<String> labels;
        private List<Long> data;
    }
}

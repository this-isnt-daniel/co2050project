package com.forensicdept.statistics.service;

import com.forensicdept.patient.entity.PatientEntity;
import com.forensicdept.patient.repository.PatientRepository;
import com.forensicdept.labtest.entity.LaboratoryTestEntity;
import com.forensicdept.labtest.repository.LaboratoryTestRepository;
import com.forensicdept.mlef.entity.MlefEntity;
import com.forensicdept.mlef.repository.MlefRepository;
import com.forensicdept.mlr.entity.MlrEntity;
import com.forensicdept.mlr.repository.MlrRepository;
import com.forensicdept.postmortem.entity.PostmortemEntity;
import com.forensicdept.postmortem.repository.PostmortemRepository;
import com.forensicdept.statistics.dto.MonthlyStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final PatientRepository patientRepository;
    private final MlefRepository mlefRepository;
    private final MlrRepository mlrRepository;
    private final LaboratoryTestRepository labRepo;
    private final PostmortemRepository pmRepo;

    public MonthlyStatsResponse getMonthlyStats(int year) {
        List<PatientEntity> patients = patientRepository.findAll().stream()
                .filter(p -> p.getCreatedAt().getYear() == year).toList();
        List<MlefEntity> mlefs = mlefRepository.findAll().stream()
                .filter(m -> m.getCreatedAt().getYear() == year).toList();
        List<MlrEntity> mlrs = mlrRepository.findAll().stream()
                .filter(m -> m.getCreatedAt().getYear() == year).toList();
        List<LaboratoryTestEntity> labs = labRepo.findAll().stream()
                .filter(l -> l.getCreatedAt().getYear() == year).toList();
        List<PostmortemEntity> pmrs = pmRepo.findAll().stream()
                .filter(p -> p.getCreatedAt().getYear() == year).toList();

        // 1. KPI Summary
        MonthlyStatsResponse.Summary summary = MonthlyStatsResponse.Summary.builder()
                .patients(patients.size())
                .mlef(mlefs.size())
                .mlr(mlrs.size())
                .labRequests(labs.size())
                .pmr(pmrs.size())
                .build();

        // 2. Monthly Volumes
        List<String> labels = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        
        List<Long> patData = getMonthlyCounts(patients.stream().map(p -> p.getCreatedAt().getMonthValue()).toList());
        List<Long> mlefData = getMonthlyCounts(mlefs.stream().map(m -> m.getCreatedAt().getMonthValue()).toList());
        List<Long> mlrData = getMonthlyCounts(mlrs.stream().map(m -> m.getCreatedAt().getMonthValue()).toList());
        List<Long> labData = getMonthlyCounts(labs.stream().map(l -> l.getCreatedAt().getMonthValue()).toList());

        MonthlyStatsResponse.MonthlyVolumes volumes = MonthlyStatsResponse.MonthlyVolumes.builder()
                .labels(labels)
                .datasets(Arrays.asList(
                        MonthlyStatsResponse.Dataset.builder().label("Patients").data(patData).build(),
                        MonthlyStatsResponse.Dataset.builder().label("MLEF").data(mlefData).build(),
                        MonthlyStatsResponse.Dataset.builder().label("MLR").data(mlrData).build(),
                        MonthlyStatsResponse.Dataset.builder().label("Lab Req").data(labData).build()
                ))
                .build();

        // 3. Fake/Sample pie chart distributions based on MLEF count to show functionality (MVP)
        // Note: Real implementation would parse 'natureOfBodilyHarm' or 'hurtCategory' fields if they existed as enums.
        long totalMlef = mlefs.size();
        MonthlyStatsResponse.Distribution bodyHarm = MonthlyStatsResponse.Distribution.builder()
                .labels(Arrays.asList("Contusions", "Lacerations", "Abrasions", "Fractures", "Other"))
                .data(Arrays.asList(totalMlef * 40/100, totalMlef * 30/100, totalMlef * 15/100, totalMlef * 10/100, totalMlef * 5/100))
                .build();
                
        MonthlyStatsResponse.Distribution hurtCategory = MonthlyStatsResponse.Distribution.builder()
                .labels(Arrays.asList("Non-Grievous", "Grievous", "Fatal", "Unknown"))
                .data(Arrays.asList(totalMlef * 60/100, totalMlef * 25/100, totalMlef * 10/100, totalMlef * 5/100))
                .build();

        return MonthlyStatsResponse.builder()
                .summary(summary)
                .monthlyVolumes(volumes)
                .mlefBodyHarmDistribution(bodyHarm)
                .mlefHurtCategoryDistribution(hurtCategory)
                .build();
    }

    private List<Long> getMonthlyCounts(List<Integer> months) {
        return IntStream.rangeClosed(1, 12)
                .mapToObj(m -> months.stream().filter(mo -> mo == m).count())
                .collect(Collectors.toList());
    }
}

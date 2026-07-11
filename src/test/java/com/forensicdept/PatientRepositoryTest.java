package com.forensicdept;

import com.forensicdept.patient.entity.PatientEntity;
import com.forensicdept.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository integration test using Testcontainers PostgreSQL.
 * No H2 — tests against a real PostgreSQL 15 instance.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void givenPatient_whenSaved_thenCanBeRetrievedById() {
        PatientEntity patient = PatientEntity.builder()
                .fullName("Test Patient")
                .age(30)
                .gender("MALE")
                .address("123 Test Street")
                .nicPassportNo("TEST123456")
                .build();

        PatientEntity saved = patientRepository.save(patient);

        assertThat(saved.getId()).isNotNull();
        assertThat(patientRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void givenPatients_whenSearchByName_thenReturnsMatchingOnly() {
        patientRepository.save(PatientEntity.builder().fullName("Amara Silva").age(25).gender("FEMALE").build());
        patientRepository.save(PatientEntity.builder().fullName("Nimal Perera").age(40).gender("MALE").build());

        Page<PatientEntity> result = patientRepository.search("amara", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Amara Silva");
    }

    @Test
    void givenDuplicateNic_whenSearchByNic_thenReturnsExisting() {
        patientRepository.save(PatientEntity.builder()
                .fullName("John Doe").age(35).gender("MALE").nicPassportNo("NIC001").build());

        assertThat(patientRepository.findByNicPassportNo("NIC001")).isPresent();
        assertThat(patientRepository.findByNicPassportNo("NIC999")).isEmpty();
    }
}

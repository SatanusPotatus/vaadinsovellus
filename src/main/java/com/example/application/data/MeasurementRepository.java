package com.example.application.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MeasurementRepository
        extends
            JpaRepository<Measurement, Long>,
            JpaSpecificationExecutor<Measurement> {

    List<Measurement> findBySamplePerson(SamplePerson person);

}

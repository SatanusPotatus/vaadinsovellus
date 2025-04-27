package com.example.application.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.application.data.Measurement;
import com.example.application.data.MeasurementRepository;
import com.example.application.data.SamplePerson;
import com.example.application.data.SamplePersonRepository;

import jakarta.transaction.Transactional;

@Service
public class MeasurementService {
    private final MeasurementRepository measurementRepository;
    private final SamplePersonRepository samplePersonRepository;

    // Constructor injection
    public MeasurementService(MeasurementRepository measurementRepository, SamplePersonRepository samplePersonRepository) {
        this.measurementRepository = measurementRepository;
        this.samplePersonRepository = samplePersonRepository;
    }

    // Save or update a measurement
    public Measurement save(Measurement measurement) {
        return measurementRepository.save(measurement);
    }

    // Find a measurement by its ID
    public Optional<Measurement> getMeasurementById(Long id) {
        return measurementRepository.findById(id);
    }

    // Get all measurements for a specific person
    public List<Measurement> getMeasurementsByPerson(SamplePerson person) {
        return measurementRepository.findBySamplePerson(person);
    }

    // Delete a measurement by its ID
    public void deleteMeasurement(Long id) {
        measurementRepository.deleteById(id);
    }

    // Get all measurements (optional)
    public List<Measurement> getAllMeasurements() {
        return measurementRepository.findAll();
    }
    @Transactional
    public void addTestData() {
        // Fetch a sample person
        SamplePerson person = samplePersonRepository.findById(1L).orElse(new SamplePerson()); // Sample person with ID = 1

        // Create and save mock measurements
        for (int i = 0; i < 5; i++) {
            Measurement measurement = new Measurement();
            measurement.setSystolicPressure(120 + i); // Random systolic pressure
            measurement.setDiastolicPressure(80 + i); // Random diastolic pressure
            measurement.setMeasurementDate(LocalDate.now().minusDays(i)); // Last 5 days

            measurement.setSamplePerson(person); // Link measurement to person
            measurementRepository.save(measurement); // Save to DB
        }
    }
}

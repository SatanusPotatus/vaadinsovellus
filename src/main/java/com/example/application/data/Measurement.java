package com.example.application.data;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Measurement extends AbstractEntity {

    private Integer systolicPressure;  // Systolic pressure (e.g., 120)
    private Integer diastolicPressure;  // Diastolic pressure (e.g., 80)
    private LocalDate measurementDate;  // Date of the measurement

    @ManyToOne
    @JoinColumn(name = "sample_person_id")  // Foreign key linking to SamplePerson
    private SamplePerson samplePerson;  // The person for whom the measurement was taken

    public Integer getSystolicPressure() {
        return systolicPressure;
    }
    public void setSystolicPressure(Integer systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    public Integer getDiastolicPressure() {
        return diastolicPressure;
    }
    public void setDiastolicPressure(Integer diastolicPressure) {
        this.diastolicPressure = diastolicPressure;
    }

    public LocalDate getMeasurementDate() {
        return measurementDate;
    }
    public void setMeasurementDate(LocalDate measurementDate) {
        this.measurementDate = measurementDate;
    }

    public SamplePerson getSamplePerson() {
        return samplePerson;
    }
    public void setSamplePerson(SamplePerson samplePerson) {
        this.samplePerson = samplePerson;
    }
}

package com.example.application;

import com.example.application.data.Measurement;
import com.example.application.data.MeasurementRepository;
import com.example.application.data.SamplePerson;
import com.example.application.data.SamplePersonRepository;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.time.LocalDate;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "vaadin-app")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);  // This line runs the application

        // Get the repositories or services from the application context
        SamplePersonRepository samplePersonRepository = context.getBean(SamplePersonRepository.class);
        MeasurementRepository measurementRepository = context.getBean(MeasurementRepository.class);

        // Add test data
        addTestData(samplePersonRepository, measurementRepository);
    }

    private static void addTestData(SamplePersonRepository samplePersonRepository, MeasurementRepository measurementRepository) {
        // Fetch all existing persons
        Iterable<SamplePerson> persons = samplePersonRepository.findAll();
    
        // Loop through each person and add measurements
        persons.forEach(person -> {
            // Add 5 measurements to each person
            for (int i = 0; i < 5; i++) {
                Measurement measurement = new Measurement();
                measurement.setSystolicPressure(120 + i);
                measurement.setDiastolicPressure(80 + i);
                measurement.setMeasurementDate(LocalDate.now().minusDays(i));
                measurement.setSamplePerson(person);  // associate measurement with the person
                measurementRepository.save(measurement);  // save the measurement
            }
        });
    
        System.out.println("Measurements added to existing persons.");
    }
    

    @Bean
    SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource,
            SqlInitializationProperties properties, SamplePersonRepository repository) {
        // This bean ensures the database is only initialized when empty
        return new SqlDataSourceScriptDatabaseInitializer(dataSource, properties) {
            @Override
            public boolean initializeDatabase() {
                if (repository.count() == 0L) {
                    return super.initializeDatabase();
                }
                return false;
            }
        };
    }
}

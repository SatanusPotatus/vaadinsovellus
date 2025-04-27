package com.example.application.views.components;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.example.application.data.SamplePerson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class Filters extends Div implements Specification<SamplePerson> {

    private TextField firstName = new TextField("First name");
    private TextField lastName = new TextField("Last name");
    private DatePicker birthDateStart = new DatePicker("Birthday start");
    private DatePicker birthDateEnd = new DatePicker("Birthday end");
    private Button searchButton = new Button("Search");
    private Button clearButton = new Button("Clear");
    
    public Filters(Runnable onSearch) {

        clearButton.addClickListener(e -> {
            firstName.clear();
            lastName.clear();
            birthDateStart.clear();
            birthDateEnd.clear();
            onSearch.run();
        });
        searchButton.addClickListener(e -> {
            onSearch.run();
        });
        
        HorizontalLayout layout = new HorizontalLayout();
        layout.add(firstName, lastName, birthDateStart, birthDateEnd, searchButton, clearButton);
        layout.setAlignItems(Alignment.BASELINE);
        add(layout);
    }

    @Override
    public Predicate toPredicate(Root<SamplePerson> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        
        // firstName like '%[firstName]%'
        if(!firstName.isEmpty()){
            Predicate firstNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.getValue().toLowerCase() + "%");
            predicates.add(firstNamePredicate);
        }
        if(!lastName.isEmpty()){
            Predicate lastNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.getValue().toLowerCase() + "%");
            predicates.add(lastNamePredicate);
        }
        if(!birthDateStart.isEmpty()){
            Predicate birthDateStartPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("dateOfBirth"), criteriaBuilder.literal(birthDateStart.getValue()));
            predicates.add(birthDateStartPredicate);
        }
        if(!birthDateEnd.isEmpty()){
            Predicate birthDateEndPredicate = criteriaBuilder.lessThanOrEqualTo(root.get("dateOfBirth"), criteriaBuilder.literal(birthDateEnd.getValue()));
            predicates.add(birthDateEndPredicate);
        }
        
        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }
}

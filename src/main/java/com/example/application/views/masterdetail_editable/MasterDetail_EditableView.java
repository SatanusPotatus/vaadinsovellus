package com.example.application.views.masterdetail_editable;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.checkerframework.checker.units.qual.s;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.example.application.data.Measurement;
import com.example.application.data.SamplePerson;
import com.example.application.services.MeasurementService;
import com.example.application.services.SamplePersonService;
import com.example.application.views.components.Filters;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import jakarta.annotation.security.RolesAllowed;
import javassist.tools.reflect.Sample;

@PageTitle("Admin Persons View")
@Route("admin-persons_view/:samplePersonID?/:action?(edit)")
@Menu(order = 2, icon = LineAwesomeIconUrl.COLUMNS_SOLID)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class MasterDetail_EditableView extends Div implements BeforeEnterObserver {

    private final String SAMPLEPERSON_ID = "samplePersonID";
    private final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "admin-persons_view/%s/edit";

    private final Grid<SamplePerson> grid = new Grid<>(SamplePerson.class, false);
    private Filters filters;

    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField phone;
    private DatePicker dateOfBirth;
    private TextField occupation;
    private TextField role;
    private Checkbox important;
    private MultiSelectComboBox<Measurement> measurements;
    private IntegerField systolicPressure;
    private IntegerField diastolicPressure;
    private DatePicker measurementDate;
    private Button addMeasurement = new Button("Add Measurement");

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<SamplePerson> binder;

    private SamplePerson samplePerson;

    private final SamplePersonService samplePersonService;
    private final MeasurementService measurementService;

    public MasterDetail_EditableView(SamplePersonService samplePersonService, MeasurementService measurementService) {
        this.measurementService = measurementService;
        this.samplePersonService = samplePersonService;
        addClassNames("master-detail-editable-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("phone").setAutoWidth(true);
        grid.addColumn("dateOfBirth").setAutoWidth(true);
        grid.addColumn("occupation").setAutoWidth(true);
        grid.addColumn("role").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>((SamplePerson p) -> {
            if (p.getMeasurements() == null || p.getMeasurements().isEmpty()) {
                return new Span("No measurements");
            }
            Button button = new Button(p.getMeasurements().size() + " Measurements");
            Popover popover = new Popover();
            popover.setTarget(button);
            popover.setWidth("400px");
            popover.addThemeVariants(PopoverVariant.ARROW, PopoverVariant.LUMO_NO_PADDING);
            popover.setPosition(PopoverPosition.BOTTOM);
            popover.setModal(true);
            popover.setAriaLabel("blood-pressure-readings");
            VerticalLayout verticalLayout = new VerticalLayout();
            for (Measurement measurement : p.getMeasurements()) {
                Span span = new Span("BP: " + measurement.getSystolicPressure() + "/" 
                    + measurement.getDiastolicPressure() 
                    + " on " + measurement.getMeasurementDate());
                verticalLayout.add(span);
            }
            
            popover.add(verticalLayout);
            popover.setCloseOnOutsideClick(true);
        
            return button;
        })).setAutoWidth(true).setHeader("Blood Pressure Measurements");
        LitRenderer<SamplePerson> importantRenderer = LitRenderer.<SamplePerson>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", important -> important.isImportant() ? "check" : "minus").withProperty("color",
                        important -> important.isImportant()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(importantRenderer).setHeader("Important").setAutoWidth(true);

        grid.setItems(query -> samplePersonService.list(VaadinSpringDataHelpers.toSpringPageRequest(query), filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                SamplePerson selectedPerson = event.getValue();

                measurements.setItems(selectedPerson.getMeasurements());
                measurements.setItemLabelGenerator(measurement -> measurement.getMeasurementDate().toString());

                UI.getCurrent().navigate(String.format(SAMPLEPERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(MasterDetail_EditableView.class);
            }
        });
        
        // Configure Form
        binder = new BeanValidationBinder<>(SamplePerson.class);
        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(measurements).bind(
                            // missä muodossa entiteetin tieto tulee komponentille
                            samplePerson -> new HashSet<>(samplePerson.getMeasurements()), 
                            // miten komponentin tieto menee entiteettiin
                            (samplePerson, formSet) -> samplePerson.setMeasurements(new ArrayList<>(formSet)));

        // Tarkistin puhelinnumerolle
        binder.forField(phone)
                .withValidator(number -> number.length() > 5, "Phone number too short")
                .bind("phone");
        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.samplePerson == null) {
                    this.samplePerson = new SamplePerson();
                }
                this.samplePerson.setMeasurements(new ArrayList<>((measurements.getValue())));
                binder.writeBean(this.samplePerson);
                samplePersonService.save(this.samplePerson);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(MasterDetail_EditableView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> samplePersonId = event.getRouteParameters().get(SAMPLEPERSON_ID).map(Long::parseLong);
        if (samplePersonId.isPresent()) {
            Optional<SamplePerson> samplePersonFromBackend = samplePersonService.get(samplePersonId.get());
            if (samplePersonFromBackend.isPresent()) {
                populateForm(samplePersonFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested samplePerson was not found, ID = %s", samplePersonId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(MasterDetail_EditableView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        email = new TextField("Email");
        phone = new TextField("Phone");
        dateOfBirth = new DatePicker("Date Of Birth");
        occupation = new TextField("Occupation");
        role = new TextField("Role");
        important = new Checkbox("Important");
        measurements = new MultiSelectComboBox<>("Measurements");
        systolicPressure = new IntegerField("Systolic Pressure");
        diastolicPressure = new IntegerField("Diastolic Pressure");
        measurementDate = new DatePicker("Measurement Date");
        addMeasurement = new Button("Add Measurement");
        addMeasurement.addClickListener(e -> {
            if (systolicPressure.isEmpty() || diastolicPressure.isEmpty() || measurementDate.isEmpty()) {
                Notification.show("Please fill in all measurement fields");
                return;
            }
            if (systolicPressure.getValue() < 0 || diastolicPressure.getValue() < 0) {
                Notification.show("Blood pressure values must be positive");
                return;
            }
            if (measurementDate.getValue() == null) {
                Notification.show("Please select a measurement date");
                return;
            }
            if (samplePerson == null) {
                Notification.show("Please select a sample person first");
                return;
            }
            if (samplePerson.getMeasurements() == null) {
                samplePerson.setMeasurements(new ArrayList<>());
            }
            Set<Measurement> currentMeasurements = new HashSet<>(measurements.getValue());
            Measurement measurement = new Measurement();
            measurement.setSystolicPressure(systolicPressure.getValue());
            measurement.setDiastolicPressure(diastolicPressure.getValue());
            measurement.setMeasurementDate(measurementDate.getValue());
            measurement.setSamplePerson(samplePerson);
            measurementService.save(measurement);
            samplePerson.getMeasurements().add(measurement);
            samplePersonService.save(samplePerson);
            currentMeasurements.add(measurement);
            systolicPressure.clear();
            diastolicPressure.clear();
            measurementDate.clear();
            measurements.setItems(samplePerson.getMeasurements());
            measurements.setItemLabelGenerator(measurement1 -> measurement1.getMeasurementDate().toString());
            measurements.select(currentMeasurements);
        });
        formLayout.add(firstName, lastName, email, phone, dateOfBirth, occupation, role, important, measurements, systolicPressure, diastolicPressure, measurementDate, addMeasurement);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }


    private void createGridLayout(SplitLayout splitLayout) {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        filters = new Filters(this::refreshGrid);
        Div wrapper = new Div();
        wrapper.setHeightFull();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(verticalLayout);
        verticalLayout.add(filters, wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SamplePerson value) {
        this.samplePerson = value;
        binder.readBean(this.samplePerson);

    }
}

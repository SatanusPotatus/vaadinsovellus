package com.example.application.views.masterdetail;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.example.application.data.SamplePerson;
import com.example.application.services.SamplePersonService;
import com.example.application.views.components.Filters;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import jakarta.annotation.security.RolesAllowed;

@PageTitle("Persons View")
@Route("persons_view")
@Menu(order = 1, icon = LineAwesomeIconUrl.COLUMNS_SOLID)
@RolesAllowed("USER")
@Uses(Icon.class)
public class MasterDetailView extends Div implements BeforeEnterObserver {

    private final String SAMPLEPERSON_ID = "samplePersonID";
    private final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "/%s/edit";

    private final Grid<SamplePerson> grid = new Grid<>(SamplePerson.class, false);
    private Filters filters;
    private SamplePerson samplePerson;
    private final SamplePersonService samplePersonService;

    public MasterDetailView(SamplePersonService samplePersonService) {
        this.samplePersonService = samplePersonService;
        addClassNames("master-detail-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("phone").setAutoWidth(true);
        grid.addColumn("dateOfBirth").setAutoWidth(true);
        grid.addColumn("occupation").setAutoWidth(true);
        grid.addColumn("role").setAutoWidth(true);
        LitRenderer<SamplePerson> importantRenderer = LitRenderer.<SamplePerson>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", important -> important.isImportant() ? "check" : "minus").withProperty("color",
                        important -> important.isImportant()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(importantRenderer).setHeader("Important").setAutoWidth(true);

        grid.setItems(query -> samplePersonService.list(VaadinSpringDataHelpers.toSpringPageRequest(query), filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
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
}

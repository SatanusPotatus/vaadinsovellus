package com.example.application.views.landingpage;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Landing Page")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.GLOBE_SOLID)
@AnonymousAllowed
public class LandingPageView extends HorizontalLayout {

    public LandingPageView() {
        Text nameField = new Text("Hello, here's the landing page!");
        add(nameField);
        setMargin(true);
        setVerticalComponentAlignment(Alignment.END);
    }

}

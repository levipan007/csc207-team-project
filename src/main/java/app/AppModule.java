package app;

import javax.swing.JTabbedPane;

/** One area's wiring. Each area owns exactly one module file and adds its own tabs. */
public interface AppModule {
    void addTabs(JTabbedPane tabs);
}

package contractmanager.presentation;

import contractmanager.presentation.applicationtab.ComparatorApplicationTab;
import contractmanager.presentation.applicationtab.ExtractorApplicationTab;

public class ApplicationData {

    private Settings settings;

    private ExtractorApplicationTab extractorApplicationTab;

    private ComparatorApplicationTab comparatorApplicationTab;

    public ApplicationData() {
        this.settings = new Settings();
        this.extractorApplicationTab = new ExtractorApplicationTab();
        this.comparatorApplicationTab = new ComparatorApplicationTab();
    }

    public Settings getSettings() {
        return settings;
    }

    public ExtractorApplicationTab getExtractorApplicationTab() {
        return extractorApplicationTab;
    }

    public ComparatorApplicationTab getComparatorApplicationTab() {
        return comparatorApplicationTab;
    }
}

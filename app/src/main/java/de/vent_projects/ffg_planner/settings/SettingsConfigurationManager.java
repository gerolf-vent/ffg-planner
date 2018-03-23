package de.vent_projects.ffg_planner.settings;

import de.vent_projects.ffg_planner.settings.objects.SchoolClass;

public interface SettingsConfigurationManager {

    SchoolClass getSchoolClass();
    void setSchoolClass(SchoolClass schoolClass);

    String getLanguage();
    void setLanguage(String language);

    String getBelieve();
    void setBelieve(String believe);

    /* boolean isSpanish();
    void setSpanish(boolean isSpanish);*/

    String getThirdLanguage();
    void setThirdLanguage(String thirdLanguage);
}

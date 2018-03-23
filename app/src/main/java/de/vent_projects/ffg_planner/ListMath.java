package de.vent_projects.ffg_planner;

public class ListMath {
    public static int getResourceBreakPosition(int position){
        return position / 3;
    }
    public static int getResourceLessonPosition(int position){
        return position - (position / 3);
    }
    public static int getAbsoluteLessonPosition(int position){
        return position + (position / 2);
    }
    public static int getCountOfHiddenItems(int countOfLessons){
        return getAbsoluteLessonPosition(countOfLessons);
    }
    public static int getCountOfBreaks(int countOfLessons){
        int count = (countOfLessons-1)/2;
        return (count < 0) ? 0 : count;
    }
}
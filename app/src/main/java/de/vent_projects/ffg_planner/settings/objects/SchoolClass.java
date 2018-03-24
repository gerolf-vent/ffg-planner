package de.vent_projects.ffg_planner.settings.objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchoolClass{
    public static final int MIN_GRADE = 7;
    public static final int MAX_GRADE = 12;

    private int grade;
    private Character name;

    public SchoolClass(int grade, Character name){
        if (grade < MIN_GRADE) {
            this.grade = MIN_GRADE;
        } else if (grade > MAX_GRADE) {
            this.grade = MAX_GRADE;
        }else {
            this.grade = grade;
        }
        this.name = name;
    }

    public SchoolClass(String schoolClass){
        Matcher gradeMatcher = Pattern.compile("[0-9]+").matcher(schoolClass);
        if (gradeMatcher.find()){
            Integer match = Integer.parseInt(gradeMatcher.group());
            if (match < MIN_GRADE) {
                this.grade = MIN_GRADE;
            } else if (match > MAX_GRADE) {
                this.grade = MAX_GRADE;
            }else {
                this.grade = match;
            }
            Matcher nameMatcher = Pattern.compile("[a-zA-Z]").matcher(schoolClass);
            if (nameMatcher.find()){
                this.name = nameMatcher.group().charAt(0);
            }else{
                this.name = 'a';
            }
        }else{
            this.grade = MIN_GRADE;
            this.name = 'a';
        }
    }

    // GETTER

    public int getGrade() {
        return this.grade;
    }

    public boolean isUpperLevel(){
        return this.grade > 10;
    }

    public Character getName() {
        return this.name;
    }

    // TO STRING

    public String toString(){
        return gradeToString()+this.name.toString().toUpperCase();
    }

    public String gradeToString(){
        return Integer.toString(this.grade);
    }
}


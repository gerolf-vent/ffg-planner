package de.vent_projects.ffg_planner.settings.objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchoolClass{
    public static final int MINGRADE = 7;
    public static final int MAXGRADE = 12;

    private int grade;
    private Character name;

    public SchoolClass(int grade, Character name){
        if (grade < MINGRADE){
            this.grade = MINGRADE;
        } else if (grade > MAXGRADE){
            this.grade = MAXGRADE;
        }else {
            this.grade = grade;
        }
        this.name = name;
    }

    public SchoolClass(String schoolClass){
        Matcher gradeMatcher = Pattern.compile("[0-9]+").matcher(schoolClass);
        if (gradeMatcher.find()){
            Integer match = Integer.parseInt(gradeMatcher.group());
            if (match < MINGRADE){
                this.grade = MINGRADE;
            } else if (match > MAXGRADE){
                this.grade = MAXGRADE;
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
            this.grade = MINGRADE;
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


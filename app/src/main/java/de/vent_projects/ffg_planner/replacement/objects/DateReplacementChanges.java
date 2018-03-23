package de.vent_projects.ffg_planner.replacement.objects;

import java.util.ArrayList;

public class DateReplacementChanges {
    private ArrayList<Replacement> added;
    private ArrayList<Replacement> changed;
    private ArrayList<Replacement> removed;
    private boolean hasInfoChanged;

    public DateReplacementChanges() {
        this.added = new ArrayList<>();
        this.changed = new ArrayList<>();
        this.removed = new ArrayList<>();
    }

    public ArrayList<Replacement> getAddedReplacements() {
        return this.added;
    }

    public void addedReplacement(Replacement replacement) {
        this.added.add(replacement);
    }

    public void addedReplacement(ArrayList<Replacement> replacements) {
        this.added.addAll(replacements);
    }

    public ArrayList<Replacement> getChangedReplacements() {
        return this.changed;
    }

    public void changedReplacement(Replacement replacement) {
        this.changed.add(replacement);
    }

    public ArrayList<Replacement> getRemovedReplacements() {
        return this.removed;
    }

    public void removedReplacement(Replacement replacement) {
        this.removed.add(replacement);
    }

    public void removedReplacement(ArrayList<Replacement> replacementa) {
        this.removed.addAll(replacementa);
    }

    public boolean hasInfoChanged() {
        return this.hasInfoChanged;
    }

    public void setInfoChanged(boolean hasInfoChanged) {
        this.hasInfoChanged = hasInfoChanged;
    }

    public boolean isSomethingChanged() {
        return this.hasInfoChanged || this.added.size() > 0 || this.changed.size() > 0 || this.removed.size() > 0;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Added: ");
        if (this.added.size() > 0) stringBuilder.append(this.added);
        stringBuilder.append("\nChanged: ");
        if (this.changed.size() > 0) stringBuilder.append(this.changed);
        stringBuilder.append("\nRemoved: ");
        if (this.removed.size() > 0) stringBuilder.append(this.removed);
        stringBuilder.append("\nHas info changed: ").append(this.hasInfoChanged);
        return stringBuilder.toString();
    }
}

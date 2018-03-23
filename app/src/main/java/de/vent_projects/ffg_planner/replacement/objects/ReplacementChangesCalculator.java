package de.vent_projects.ffg_planner.replacement.objects;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ReplacementChangesCalculator {
    private ArrayList<DateReplacement> added;
    private ArrayList<DateReplacementChanges> changed;
    private ArrayList<DateReplacement> removed;

    /* PLEASE, PLEASE CONSTRUCT ONLY IN ASYNC TASKS OR SERVICES */
    public ReplacementChangesCalculator(List<DateReplacement> oldDateReplacements, List<DateReplacement> newDateReplacements) {
        Log.d("ReplacementChanges", oldDateReplacements.toString() + "\n" + newDateReplacements.toString());

        this.added = new ArrayList<>();
        this.changed = new ArrayList<>();
        this.removed = new ArrayList<>();

        ArrayList<DateReplacement> cachedOldDateReplacements = new ArrayList<>(oldDateReplacements);
        ArrayList<DateReplacement> cachedNewDateReplacements = new ArrayList<>(newDateReplacements);
        for (int i = cachedNewDateReplacements.size() - 1; i >= 0 ; i--) {
            for (int j = cachedOldDateReplacements.size() - 1; j >= 0 ; j--) {
                if (cachedNewDateReplacements.get(i).equals(cachedOldDateReplacements.get(j))) {
                    DateReplacementChanges dateReplacementChanges = getDateReplacementChanges(cachedNewDateReplacements.get(i), cachedOldDateReplacements.get(j));
                    if (dateReplacementChanges.isSomethingChanged()) {
                        Log.e("ReplacementChanges", "It's a match and not equal");
                        this.changed.add(dateReplacementChanges);
                    } else {
                        Log.e("ReplacementChanges", "It's a match and equal");
                    }
                    cachedNewDateReplacements.remove(i);
                    cachedOldDateReplacements.remove(j);
                    break;
                }
            }
        }

        this.added.addAll(cachedNewDateReplacements);
        this.removed.addAll(cachedOldDateReplacements);
    }

    private DateReplacementChanges getDateReplacementChanges(DateReplacement oldDateReplacement, DateReplacement newDateReplacement) {
        DateReplacementChanges dateReplacementChanges = new DateReplacementChanges();
        if (!oldDateReplacement.getInfo().equals(newDateReplacement.getInfo()))
            dateReplacementChanges.setInfoChanged(true);
        ArrayList<Replacement> oldReplacements = new ArrayList<>(oldDateReplacement.getReplacements());
        ArrayList<Replacement> newReplacements = new ArrayList<>(newDateReplacement.getReplacements());

        for (int i = newReplacements.size() - 1; i >= 0; i--) {
            for (int j = oldReplacements.size() - 1; j >= 0 ; j--) {
                if (newReplacements.get(i).getPeriod() == oldReplacements.get(j).getPeriod()) {
                    if (!newReplacements.get(i).equals(oldReplacements.get(j))) {
                        dateReplacementChanges.changedReplacement(newReplacements.get(i));
                    }
                    newReplacements.remove(i);
                    oldReplacements.remove(j);
                    break;
                }
            }
        }

        dateReplacementChanges.addedReplacement(newReplacements);
        dateReplacementChanges.removedReplacement(oldReplacements);
        return dateReplacementChanges;
    }

    public ArrayList<DateReplacement> getAddedDateReplacements() {
        return this.added;
    }

    public ArrayList<DateReplacementChanges> getChangedDateReplacements() {
        return this.changed;
    }

    public ArrayList<DateReplacement> getRemovedDateReplacements() {
        return this.removed;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Added: ");
        if (this.added.size() > 0) {
            stringBuilder.append(this.added);
        } else {
            stringBuilder.append("none");
        }
        stringBuilder.append("\n\n\nChanged: ");
        if (this.added.size() > 0) {
            stringBuilder.append(this.changed);
        } else {
            stringBuilder.append("none");
        }
        stringBuilder.append("\n\n\nRemoved: ");
        if (this.added.size() > 0) {
            stringBuilder.append(this.removed);
        } else {
            stringBuilder.append("none");
        }
        return stringBuilder.toString();
    }
}

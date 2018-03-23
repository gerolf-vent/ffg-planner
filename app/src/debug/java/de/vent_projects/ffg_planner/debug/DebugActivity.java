package de.vent_projects.ffg_planner.debug;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.replacement.objects.DateReplacement;
import de.vent_projects.ffg_planner.replacement.objects.Replacement;
import de.vent_projects.ffg_planner.replacement.objects.ReplacementChangesCalculator;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button buttonCalc = findViewById(R.id.button_calc);
        buttonCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ReplacementChangesCalculatorAsyncTask((TextView) findViewById(R.id.text_view_changes)).execute();
            }
        });

    }

    private static class ReplacementChangesCalculatorAsyncTask extends AsyncTask<Void, Void, ReplacementChangesCalculator> {
        TextView textView;

        ReplacementChangesCalculatorAsyncTask(TextView textView) {
            this.textView = textView;
        }

        @Override
        protected ReplacementChangesCalculator doInBackground(Void... voids) {
            return calcReplacementChanges();
        }

        private ReplacementChangesCalculator calcReplacementChanges() {
            Log.d("ReplacementChanges", "Start build");
            ArrayList<DateReplacement> oldDateReplacements = new ArrayList<>();
            DateReplacement oldDateReplacement = new DateReplacement("23.02.2018");
            oldDateReplacement.addReplacement(new Replacement("23.02.2018", 1, "D", false, "dx", false, "103", false, "", ""));
            oldDateReplacement.addReplacement(new Replacement("23.02.2018", 2, "D", false, "dx", false, "103", false, "", ""));
            oldDateReplacement.setInfo("Changed");
            oldDateReplacements.add(oldDateReplacement);
            oldDateReplacements.add(new DateReplacement("22.02.2018", new ArrayList<Replacement>(), "12e", "", "", "Old"));
            ArrayList<DateReplacement> newDateReplacements = new ArrayList<>();
            DateReplacement newDateReplacement = new DateReplacement("23.02.2018");
            newDateReplacement.addReplacement(new Replacement("23.02.2018", 1, "Ma", false, "bg", false, "303", false, "", ""));
            newDateReplacement.addReplacement(new Replacement("23.02.2018", 3, "D", false, "dx", false, "103", false, "", ""));
            newDateReplacement.setInfo("Changed");
            newDateReplacements.add(newDateReplacement);
            newDateReplacements.add(new DateReplacement("24.02.2018", new ArrayList<Replacement>(), "12e", "", "", "New"));
            Log.d("ReplacementChanges", "Start calc");
            return new ReplacementChangesCalculator(oldDateReplacements, newDateReplacements);
        }

        @Override
        protected void onPostExecute(ReplacementChangesCalculator replacementChangesCalculator) {
            Log.d("ReplacementChanges", "Finished calc");
            this.textView.setText(replacementChangesCalculator.toString());
        }
    }

}

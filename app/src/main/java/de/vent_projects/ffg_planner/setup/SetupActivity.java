package de.vent_projects.ffg_planner.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashMap;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.setup.ui.SetupActivityInterface;
import de.vent_projects.ffg_planner.setup.ui.SetupFragment;
import de.vent_projects.ffg_planner.setup.ui.SetupFragmentInterface;
import de.vent_projects.ffg_planner.setup.ui.SlideDirection;
import de.vent_projects.ffg_planner.setup.ui.common.SetupCommonFragment;
import de.vent_projects.ffg_planner.setup.ui.schedule.SetupScheduleFragment;

import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

public class SetupActivity extends AppCompatActivity implements SetupActivityInterface {
    private static final String TAG = SetupActivity.class.getSimpleName();

    private boolean isCancelAllowed;

    private ImageButton buttonBack;
    private ImageButton buttonNext;

    private SetupFragmentInterface setupFragmentInterface;

    private HashMap<SetupSteps, SetupFragment> setupFragments;
    private ArrayList<SetupSteps> setupSteps;
    private int currentStepPosition;

    public SetupActivity() {
        this.setupFragments = new HashMap<>();
        this.setupSteps = new ArrayList<>();
    }

    public static ArrayList<String> getFirstSetupPreset() {
        ArrayList<String> setupSteps = new ArrayList<>();
        setupSteps.add(SetupSteps.COMMON.name());
        setupSteps.add(SetupSteps.SCHEDULE.name());
        return setupSteps;
    }

    public static ArrayList<String> getNormalSetupPreset() {
        ArrayList<String> setupSteps = new ArrayList<>();
        setupSteps.add(SetupSteps.COMMON.name());
        setupSteps.add(SetupSteps.SCHEDULE.name());
        return setupSteps;
    }

    public static void startForFirstSetup(Context context) {
        Intent intent = new Intent(context, SetupActivity.class);
        intent.putExtra("setup_steps", getFirstSetupPreset());
        intent.putExtra("is_cancel_allowed", false);
        context.startActivity(intent);
    }

    public static void startForNormalSetup(Context context) {
        Intent intent = new Intent(context, SetupActivity.class);
        intent.putExtra("setup_steps", getNormalSetupPreset());
        intent.putExtra("is_cancel_allowed", true);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Bundle extras = getIntent().getExtras();
        this.isCancelAllowed = (extras != null && extras.getBoolean("is_cancel_allowed", false));

        if (extras != null && extras.containsKey("setup_steps")) {
            ArrayList<String> setupSteps = extras.getStringArrayList("setup_steps");
            if (setupSteps != null) {
                for (int i = 0; i < setupSteps.size(); i++) {
                    this.setupSteps.add(SetupSteps.valueOf(setupSteps.get(i)));
                }
            }
        }

        ImageButton buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        this.buttonBack = buttonBack;

        ImageButton buttonNext = findViewById(R.id.button_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });
        this.buttonNext = buttonNext;

        if (getSupportActionBar() != null){
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        this.showFirstStep();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    // MENU

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_setup, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (setupFragmentInterface != null && setupFragmentInterface.isResetable()) {
            menu.getItem(0).setVisible(true);
        } else {
            menu.getItem(0).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reset) {
            if (setupFragmentInterface != null) {
                setupFragmentInterface.reset();
            }
            return true;
        } else if (id == android.R.id.home){
            goBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // STEP MANAGEMENT

    private int getStepCount() {
        return this.setupSteps.size();
    }

    private boolean isFirstStep() {
        return this.currentStepPosition <= 0;
    }

    private boolean isFirstSubstep() {
        return this.setupFragmentInterface == null || this.setupFragmentInterface.getSubstepPosition() <= 0;
    }

    private boolean isLastStep() {
        return this.currentStepPosition >= this.getStepCount() - 1;
    }

    private boolean isLastSubstep() {
        return this.setupFragmentInterface == null || this.setupFragmentInterface.getSubstepPosition() >= this.setupFragmentInterface.getSubstepCount() - 1;
    }

    private SetupFragment getSetupFragmentForStep(SetupSteps step) {
        /*if (this.setupFragments.containsKey(step)) {
            return this.setupFragments.get(step);
        }*/
        return createNewSetupFragmentForStep(step);
    }

    private static SetupFragment createNewSetupFragmentForStep(SetupSteps step) {
        switch (step) {
            case COMMON: return new SetupCommonFragment();
            case SCHEDULE: return new SetupScheduleFragment();
        }
        return null;
    }

    private void goStepBack() {
        if (this.isFirstStep()) {
            if (this.isCancelAllowed) finish();
        } else {
            showStep(this.setupSteps.get(this.currentStepPosition - 1), SlideDirection.BACK);
        }
    }

    private void goStepNext() {
        if (this.isLastStep()) {
            finish();
        } else {
            showStep(this.setupSteps.get(this.currentStepPosition + 1), SlideDirection.NEXT);
        }
    }

    private void showFirstStep() {
        if (this.getStepCount() == 0) {
            this.requestNavigationRefresh();
            return;
        }
        showStep(this.setupSteps.get(0), SlideDirection.NOWHERE);
    }

    private void showStep(SetupSteps step, SlideDirection slideDirection) {
        SetupFragment oldSetupFragment = getSetupFragmentForStep(this.setupSteps.get(this.currentStepPosition));
        oldSetupFragment.unbindFromSetupActivity();

        SetupFragment newSetupFragment = getSetupFragmentForStep(step);
        if (newSetupFragment == null) {
            // TODO: Throw error
            return;
        }
        this.setupFragmentInterface = newSetupFragment.bindToSetupActivity(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (slideDirection == SlideDirection.NEXT) {
            transaction.setCustomAnimations(R.anim.slide_to_left, R.anim.exit_to_left);
        } else if (slideDirection == SlideDirection.BACK) {
            transaction.setCustomAnimations(R.anim.slide_to_right, R.anim.exit_to_right);
        } else {
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        }
        transaction.replace(R.id.setup_fragment, newSetupFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        this.currentStepPosition = this.setupSteps.indexOf(step);

        this.requestTitleRefresh();
        this.requestNavigationRefresh();
        this.invalidateOptionsMenu();
    }

    // SETUP ACTIVITY INTERFACE IMPLEMENTATION

    @Override
    public void notifyStepFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                goStepNext();
            }
        });
    }

    @Override
    public void notifyStepAborted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                goStepBack();
            }
        });
    }

    @Override
    public void requestTitleRefresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getSupportActionBar() != null) {
                    if (setupFragmentInterface == null || isStringBlank(setupFragmentInterface.getTitle())) {
                        getSupportActionBar().setTitle(getString(R.string.title_activity_setup));
                    } else {
                        getSupportActionBar().setTitle(setupFragmentInterface.getTitle());
                    }
                }
            }
        });
    }

    // SUBSTEP MANAGEMENT

    private void goBack() {
        if (setupFragmentInterface != null) {
            if (setupFragmentInterface.canGoBack()) {
                if (this.isFirstSubstep()) {
                    goStepBack();
                } else {
                    setupFragmentInterface.goBack();
                }
            }
        } else {
            // TODO: Throw error
            finish();
        }
    }

    private void goNext() {
        if (setupFragmentInterface != null) {
            if (setupFragmentInterface.canGoNext()) {
                if (this.isLastSubstep()) {
                    setupFragmentInterface.onFinish();
                    goStepNext();
                } else {
                    setupFragmentInterface.goNext();
                }
            }
        } else {
            // TODO: Throw error
            finish();
        }
    }

    // NAVIGATION MANAGEMENT + SETUP ACTIVITY INTERFACE IMPLEMENTATION

    @Override
    public void requestNavigationRefresh() {
        if (this.buttonBack == null || this.buttonNext == null) return;
        buttonBack.setImageResource(this.isFirstStep() && this.isFirstSubstep() ? R.drawable.ic_close : R.drawable.ic_back);
        buttonBack.setContentDescription(getString(this.isFirstStep() && this.isFirstSubstep() ? R.string.button_cancel : R.string.button_back));
        buttonNext.setImageResource(this.isLastStep() && this.isLastSubstep() ? R.drawable.ic_done : R.drawable.ic_next);
        buttonNext.setContentDescription(getString(this.isLastStep() && this.isLastSubstep() ? R.string.button_done : R.string.button_next));
        if (setupFragmentInterface != null) {
            buttonBack.setVisibility((this.isFirstStep() && !this.isCancelAllowed) ? View.INVISIBLE : View.VISIBLE);
            this.setButtonBackEnabled(setupFragmentInterface == null || setupFragmentInterface.canGoBack());
            this.setButtonNextEnabled(setupFragmentInterface == null || setupFragmentInterface.canGoNext());
        } else {
            buttonBack.setVisibility(View.VISIBLE);
            this.setButtonBackEnabled(true);
            this.setButtonNextEnabled(false);
        }
    }

    private void setButtonBackEnabled(boolean enabled) {
        if (this.buttonBack == null) return;
        this.buttonBack.setAlpha(enabled ? 0xFF : 0x3F);
        this.buttonBack.setEnabled(enabled);
    }

    private void setButtonNextEnabled(boolean enabled) {
        if (this.buttonNext == null) return;
        this.buttonNext.setAlpha(enabled ? 0xFF : 0x3F);
        this.buttonNext.setEnabled(enabled);
    }
}

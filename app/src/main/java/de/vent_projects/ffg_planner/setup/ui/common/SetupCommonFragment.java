package de.vent_projects.ffg_planner.setup.ui.common;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.download.schedule.DownloadScheduleListener;
import de.vent_projects.ffg_planner.download.schedule.DownloadScheduleManager;
import de.vent_projects.ffg_planner.settings.objects.SchoolClass;
import de.vent_projects.ffg_planner.setup.settings.SetupSettingsManager;
import de.vent_projects.ffg_planner.setup.ui.SetupFragment;
import de.vent_projects.ffg_planner.setup.ui.SetupFragmentInterface;

import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

public class SetupCommonFragment extends SetupFragment implements SetupFragmentInterface {
    private static final String TAG = SetupCommonFragment.class.getSimpleName();

    private TextInputEditText editTextSchoolClass;
    private TextInputLayout textInputLayoutSchoolClass;
    private Spinner spinnerLanguage;
    private Spinner spinnerBelieve;
    // private CheckBox checkBoxIsSpanish;
    private Spinner spinnerThirdLanguage;

    private SetupSettingsManager settingsManager;

    public SetupCommonFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup_common, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.settingsManager = new SetupSettingsManager(getContext());

        this.requestNavigationRefresh();

        this.editTextSchoolClass = view.findViewById(R.id.edit_text_school_class);
        this.textInputLayoutSchoolClass = view.findViewById(R.id.text_input_layout_school_class);
        this.spinnerLanguage = view.findViewById(R.id.spinner_language);
        this.spinnerBelieve = view.findViewById(R.id.spinner_believe);
        // this.checkBoxIsSpanish = view.findViewById(R.id.checkbox_is_spanish);
        this.spinnerThirdLanguage = view.findViewById(R.id.spinner_third_language);

        // EDIT SCHOOL CLASS

        this.editTextSchoolClass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                SetupCommonFragment.this.onSchoolClassChange(editable);
            }
        });

        this.textInputLayoutSchoolClass.setErrorEnabled(true);

        // CHOOSE LANGUAGE

        ArrayAdapter<CharSequence> language_adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.options_languages, android.R.layout.simple_spinner_dropdown_item);
        language_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spinnerLanguage.setAdapter(language_adapter);
        this.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settingsManager.setLanguage(view.getContext().getResources().getStringArray(R.array.options_languages_values)[position]);
                requestNavigationRefresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                requestNavigationRefresh();
            }
        });

        // CHOOSE BELIEVE

        ArrayAdapter<CharSequence> believes_adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.options_believes, android.R.layout.simple_spinner_dropdown_item);
        believes_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spinnerBelieve.setAdapter(believes_adapter);
        this.spinnerBelieve.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settingsManager.setBelieve(view.getContext().getResources().getStringArray(R.array.options_believes_values)[position]);
                requestNavigationRefresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                requestNavigationRefresh();
            }
        });

        // CHOOSE SPANISH OR NOT

        /* this.checkBoxIsSpanish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsManager.setSpanish(isChecked);
                requestNavigationRefresh();
            }
        }); */

        // CHOOSE THIRD LANGUAGE

        ArrayAdapter<CharSequence> third_languages_adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.options_third_languages, android.R.layout.simple_spinner_dropdown_item);
        third_languages_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spinnerThirdLanguage.setAdapter(third_languages_adapter);
        this.spinnerThirdLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settingsManager.setThirdLanguage(view.getContext().getResources().getStringArray(R.array.options_third_languages_values)[position]);
                requestNavigationRefresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                requestNavigationRefresh();
            }
        });

        // INSERT AVAILABLE SETUP CONFIGURATION

        SchoolClass schoolClass = settingsManager.getSchoolClass();
        if (schoolClass != null && !schoolClass.toString().equals("")) {
            this.editTextSchoolClass.setText(schoolClass.toString().toLowerCase());
        }
        String language = settingsManager.getLanguage();
        if (!isStringBlank(language)) {
            for (int i = 0; i < getResources().getStringArray(R.array.options_languages_values).length; i++) {
                if (getResources().getStringArray(R.array.options_languages_values)[i].equals(language)) {
                    this.spinnerLanguage.setSelection(i);
                }
            }
        }
        String believe = settingsManager.getBelieve();
        if (!isStringBlank(believe)) {
            for (int i = 0; i < getResources().getStringArray(R.array.options_believes_values).length; i++) {
                if (getResources().getStringArray(R.array.options_believes_values)[i].equals(believe)) {
                    this.spinnerBelieve.setSelection(i);
                }
            }
        }
        // this.checkBoxIsSpanish.setChecked(settingsManager.isSpanish());
        String thirdLanguage = settingsManager.getThirdLanguage();
        if (!isStringBlank(thirdLanguage)) {
            for (int i = 0; i < getResources().getStringArray(R.array.options_third_languages_values).length; i++) {
                if (getResources().getStringArray(R.array.options_third_languages_values)[i].equals(thirdLanguage)) {
                    this.spinnerThirdLanguage.setSelection(i);
                }
            }
        }

        super.onViewCreated(view, savedInstanceState);
    }

    private void onSchoolClassChange(Editable editable) {
        Integer grade;
        Character name;
        Matcher gradeMatcher = Pattern.compile("[0-9]+").matcher(editable.toString());
        this.spinnerLanguage.setVisibility(View.GONE);
        this.spinnerBelieve.setVisibility(View.GONE);
        //this.checkBoxIsSpanish.setVisibility(View.GONE);
        this.spinnerThirdLanguage.setVisibility(View.GONE);
        if (gradeMatcher.find()) {
            Integer match = Integer.parseInt(gradeMatcher.group());
            if (match < SchoolClass.MINGRADE) {
                this.textInputLayoutSchoolClass.setError(String.format(getString(R.string.errors_message_school_class_under_min), SchoolClass.MINGRADE));
                settingsManager.setSchoolClass(null);
            } else if (match > SchoolClass.MAXGRADE) {
                this.textInputLayoutSchoolClass.setError(String.format(getString(R.string.errors_message_school_class_over_max), SchoolClass.MAXGRADE));
                settingsManager.setSchoolClass(null);
            } else {
                grade = match;
                Matcher nameMatcher = Pattern.compile("[a-zA-Z]").matcher(editable.toString());
                if (nameMatcher.find()) {
                    this.textInputLayoutSchoolClass.setError(null);
                    name = nameMatcher.group().charAt(0);
                    settingsManager.setSchoolClass(new SchoolClass(grade, name));
                    if (grade <= 10) {
                        if (grade == 10) {
                            //this.checkBoxIsSpanish.setVisibility(View.VISIBLE);
                            this.spinnerThirdLanguage.setVisibility(View.VISIBLE);
                        }
                        this.spinnerLanguage.setVisibility(View.VISIBLE);
                        this.spinnerBelieve.setVisibility(View.VISIBLE);
                    }
                } else {
                    this.textInputLayoutSchoolClass.setError(getString(R.string.errors_message_school_class_no_name));
                    settingsManager.setSchoolClass(null);
                }
            }
        } else {
            this.textInputLayoutSchoolClass.setError(getString(R.string.errors_message_school_class_unparseble));
            settingsManager.setSchoolClass(null);
        }

        this.requestNavigationRefresh();
    }

    // CHECK IF SETUP CONFIGURATION IS COMPLETE

    private boolean isConfigurationComplete() {
        if (settingsManager == null) return false;
        SchoolClass schoolClass = settingsManager.getSchoolClass();
        return (schoolClass != null) && (schoolClass.isUpperLevel() || (!isStringBlank(settingsManager.getLanguage()) && !isStringBlank(settingsManager.getBelieve())));
    }

    // SETUP FRAGMENT INTERFACE

    @Override
    public SetupFragmentInterface getSetupFragmentInterface() {
        return this;
    }

    @Override
    public boolean canGoBack() {
        return true;
    }

    @Override
    public boolean canGoNext() {
        return this.isConfigurationComplete();
    }

    @Override
    public void goBack() {}

    @Override
    public void goNext() {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getContext().getString(R.string.progress_download_schedule));
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
        if (this.isConfigurationComplete()) {
            DownloadScheduleManager downloadScheduleManager = new DownloadScheduleManager(getContext(), new SetupSettingsManager(getContext()));
            downloadScheduleManager.downloadScheduleFromServerAsync(new DownloadScheduleListener() {
                @Override
                public void onFinished() {
                    if (dialog.isShowing()) dialog.dismiss();
                    notifyStepFinished();
                }

                @Override
                public void onNetworkError(int errorCode, String errorMessage) {
                    if (dialog.isShowing()) dialog.dismiss();
                    if (errorCode == 404) {
                        notifyPerDialog(R.string.dialog_title_data_not_found, R.string.dialog_text_school_class_not_found, new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (editTextSchoolClass != null) {
                                            Matcher grade_matcher = Pattern.compile("[0-9]+").matcher(editTextSchoolClass.getText());
                                            if (grade_matcher.find()) {
                                                editTextSchoolClass.setText("");
                                                editTextSchoolClass.append(grade_matcher.group());
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        notifyPerDialog(getString(R.string.dialog_title_sorry), String.format(getString(R.string.dialog_text_connection_x_response), errorCode));
                    }
                }

                @Override
                public void onNoNetworkAvailable() {
                    if (dialog.isShowing()) dialog.dismiss();
                    notifyPerDialog(R.string.dialog_title_no_connection, R.string.dialog_text_check_connection);
                }

                @Override
                public void onBadNetwork() {
                    if (dialog.isShowing()) dialog.dismiss();
                    notifyPerDialog(R.string.dialog_title_bad_connection, R.string.dialog_text_bad_connection);
                }

                @Override
                public void onParseError() {
                    if (dialog.isShowing()) dialog.dismiss();
                    notifyPerDialog(R.string.dialog_title_sorry, R.string.dialog_text_error_common);
                }

                @Override
                public void onUnknownError() {
                    if (dialog.isShowing()) dialog.dismiss();
                    notifyPerDialog(R.string.dialog_title_sorry, R.string.dialog_text_error_common);
                }
            });
        }
    }

    @Override
    public int getSubstepCount() {
        return 2;
    }

    @Override
    public int getSubstepPosition() {
        return 0;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public boolean isResetable() {
        return true;
    }

    @Override
    public void reset() {
        if (this.editTextSchoolClass != null) {
            this.editTextSchoolClass.setText("");
        }
    }

    @Override
    public void onFinish() {

    }
}

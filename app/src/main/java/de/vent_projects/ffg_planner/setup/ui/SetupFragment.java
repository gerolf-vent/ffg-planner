package de.vent_projects.ffg_planner.setup.ui;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import de.vent_projects.ffg_planner.R;

public abstract class SetupFragment extends Fragment implements SetupActivityInterface {
    private static final String TAG = SetupFragment.class.getSimpleName();
    private SetupActivityInterface setupActivityInterface;

    public SetupFragment() {}

    // BINDING TO SETUP ACTIVITY

    public SetupFragmentInterface bindToSetupActivity(SetupActivityInterface setupActivityInterface) {
         if (setupActivityInterface == null) {
            Log.e(TAG, "SetupFragment is not binded to SetupActivity SetupActivityInterface is null");
        }
        this.setupActivityInterface = setupActivityInterface;
        return getSetupFragmentInterface();
    }

    public void unbindFromSetupActivity() {
        this.setupActivityInterface = null;
    }

    public abstract SetupFragmentInterface getSetupFragmentInterface();

    // SETUP ACTIVITY INTERFACE

    @Override
    public void requestNavigationRefresh() {
        if (setupActivityInterface != null) {
            setupActivityInterface.requestNavigationRefresh();
        } else {
            Log.e(TAG, "SetupFragment is not binded to SetupActivity: SetupActivityInterface is null");
        }
    }

    @Override
    public void notifyStepFinished() {
        if (setupActivityInterface != null) {
            setupActivityInterface.notifyStepFinished();
        } else {
            Log.e(TAG, "SetupFragment is not binded to SetupActivity: SetupActivityInterface is null");
        }
    }

    @Override
    public void notifyStepAborted() {
        if (setupActivityInterface != null) {
            setupActivityInterface.notifyStepAborted();
        } else {
            Log.e(TAG, "SetupFragment is not binded to SetupActivity: SetupActivityInterface is null");
        }
    }

    @Override
    public void requestTitleRefresh() {
        if (setupActivityInterface != null) {
            setupActivityInterface.requestTitleRefresh();
        } else {
            Log.e(TAG, "SetupFragment is not binded to SetupActivity: SetupActivityInterface is null");
        }
    }

    // COMMON FUNCTIONS

    public String makeFragmentName(int containerViewID, int id) {
        return "android:switcher:" + Integer.toString(containerViewID) + ":" + Integer.toString(id);
    }

    // NOTIFY BY DIALOG

    public void notifyPerDialog(int title, int message){
        notifyPerDialog(title, message, null);
    }
    public void notifyPerDialog(String title, String message) {
        notifyPerDialog(title, message, null);
    }
    public void notifyPerDialog(int title, int message, DialogInterface.OnDismissListener listener){
        notifyPerDialog(getContext().getString(title), getContext().getString(message), listener);
    }
    public void notifyPerDialog(final String title, final String message, final DialogInterface.OnDismissListener listener) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                if (title != null) {
                    alertDialog.setTitle(title);
                }
                alertDialog.setMessage(message);
                alertDialog.setCancelable(false);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.setOnDismissListener(listener);
                alertDialog.show();
            }
        });
    }
}

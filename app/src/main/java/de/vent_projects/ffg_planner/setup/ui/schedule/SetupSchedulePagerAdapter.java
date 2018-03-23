package de.vent_projects.ffg_planner.setup.ui.schedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class SetupSchedulePagerAdapter extends FragmentStatePagerAdapter {

    public SetupSchedulePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        SetupSchedulePageFragment fragment = new SetupSchedulePageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("day", position + 2);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 5;
    }
}

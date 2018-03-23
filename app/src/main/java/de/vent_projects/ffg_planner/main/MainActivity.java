package de.vent_projects.ffg_planner.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.main.schedule.MainScheduleManager;
import de.vent_projects.ffg_planner.main.ui.MainCurrentPagerAdapter;
import de.vent_projects.ffg_planner.main.ui.MainReplacementContainerListAdapter;
import de.vent_projects.ffg_planner.main.ui.MainReplacementRecyclerView;
import de.vent_projects.ffg_planner.main.ui.MainSchedulePagerAdapter;
import de.vent_projects.ffg_planner.main.ui.MainSwipeRefreshLayout;
import de.vent_projects.ffg_planner.realm.RealmManager;
import de.vent_projects.ffg_planner.replacement.DownloadReplacementListener;
import de.vent_projects.ffg_planner.replacement.ReplacementManager;
import de.vent_projects.ffg_planner.services.NotificationManager;
import de.vent_projects.ffg_planner.settings.SettingsActivity;
import de.vent_projects.ffg_planner.setup.SetupActivity;
import de.vent_projects.ffg_planner.theming.ThemeManager;
import io.realm.Realm;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private MainSwipeRefreshLayout swipeRefreshLayout;
    private ViewPager viewPagerCurrent;
    private Spinner spinnerCurrent;
    private View containerReplacement;
    private MainReplacementRecyclerView recyclerViewReplacement;
    private ViewPager viewPagerSchedule;
    private Spinner spinnerSchedule;

    private MainSections activeSection;
    private MainScheduleManager scheduleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme((new ThemeManager(this)).getActivityWithToolbarThemeID());

        super.onCreate(savedInstanceState);

        if (savedInstanceState == null && getIntent() != null && getIntent().getStringExtra("active_section") != null) {
            savedInstanceState = new Bundle();
            savedInstanceState.putString("active_section", getIntent().getStringExtra("active_section"));
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        Realm.init(this);
        RealmManager.init(this);

        this.scheduleManager = new MainScheduleManager(this);

        if (this.scheduleManager.needConfiguration()) {
            SetupActivity.startForFirstSetup(this);
        }

        initializeSectionViews();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (SDK_INT >= 21) {
            Menu navigationMenu = navigationView.getMenu();
            MenuItem scheduleItem = navigationMenu.findItem(R.id.nav_schedule);
            View scheduleActionView = scheduleItem.getActionView();
            ImageButton imageButton = scheduleActionView.findViewById(R.id.action_button);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetupActivity.startForNormalSetup(v.getContext());
                }
            });
        }

        this.restoreSectionSelection(savedInstanceState);
    }

    private void initializeSectionViews() {
        this.swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        this.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        this.viewPagerSchedule = findViewById(R.id.viewpager_schedule);
        this.spinnerSchedule = findViewById(R.id.toolbar_spinner_schedule);

        this.viewPagerSchedule.setOffscreenPageLimit(5);
        this.viewPagerSchedule.setAdapter(new MainSchedulePagerAdapter(getSupportFragmentManager()));
        this.viewPagerSchedule.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                spinnerSchedule.setSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                swipeRefreshLayout.setScrollSchedule(state != ViewPager.SCROLL_STATE_IDLE);
            }
        });

        this.spinnerSchedule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewPagerSchedule.setCurrentItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        this.viewPagerCurrent = findViewById(R.id.viewpager_current);
        this.spinnerCurrent = findViewById(R.id.toolbar_spinner_current);

        this.viewPagerCurrent.setAdapter(new MainCurrentPagerAdapter(getSupportFragmentManager()));
        this.viewPagerCurrent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                spinnerCurrent.setSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                swipeRefreshLayout.setScrollCurrent(state != ViewPager.SCROLL_STATE_IDLE);
            }
        });
        this.viewPagerCurrent.setOffscreenPageLimit(2);

        this.spinnerCurrent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewPagerCurrent.setCurrentItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        this.recyclerViewReplacement = findViewById(R.id.recyclerview_replacement);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.recyclerViewReplacement.setLayoutManager(layoutManager);
        this.recyclerViewReplacement.addEmptyView(findViewById(R.id.nothing_text));
        this.recyclerViewReplacement.addEmptyView(findViewById(R.id.nothing_icon));
        this.recyclerViewReplacement.setAdapter(new MainReplacementContainerListAdapter(this));

        this.containerReplacement = findViewById(R.id.container_replacement);

        this.swipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(SwipeRefreshLayout parent, @Nullable View child) {
                ScrollView scrollView = null;
                if (getActiveSection() == MainSections.SCHEDULE && getScheduleView() != null) {
                    scrollView = getScheduleView().findViewWithTag("scrollview_schedule_" + getScheduleView().getCurrentItem());
                } else if (getActiveSection() == MainSections.CURRENT && getCurrentView() != null) {
                    scrollView = getCurrentView().findViewWithTag("scrollview_current_" + getCurrentView().getCurrentItem());
                } else if (getActiveSection() == MainSections.REPLACEMENT && getReplacementView() != null) {
                    RecyclerView recyclerView = getReplacementView();
                    return recyclerView != null && recyclerView.getScrollY() > 0;
                }
                return scrollView == null || scrollView.getScrollY() > 0;
            }
        });
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateReplacementData();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("active_section", getActiveSection().name());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        this.restoreSectionSelection(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void restoreSectionSelection(Bundle savedInstanceState) {
        String activeSection = (savedInstanceState != null && savedInstanceState.containsKey("active_section"))
                ? savedInstanceState.getString("active_section", MainSections.CURRENT.name())
                : MainSections.CURRENT.name();

        NavigationView navigationView = findViewById(R.id.nav_view);

        switch (activeSection) {
            case "CURRENT": showCurrentView();
                navigationView.setCheckedItem(R.id.nav_current);
                break;
            case "REPLACEMENT": showReplacementView();
                navigationView.setCheckedItem(R.id.nav_replacement);
                break;
            case "SCHEDULE": showScheduleView();
                navigationView.setCheckedItem(R.id.nav_schedule);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_update) {
            /*swipeRefreshLayout.setRefreshing(true);
            updateReplacementData();*/
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "replacements");
            notificationBuilder.setSmallIcon(R.drawable.ic_school);
            notificationBuilder.setContentTitle(this.getString(R.string.notification_title_replacement_changed));
            notificationBuilder.setContentText(this.getString(R.string.notification_text_replacement_changed));
            Intent replacementIntent = new Intent(this, MainActivity.class);
            replacementIntent.putExtra("active_section", MainSections.REPLACEMENT.name());
            PendingIntent clickedReplacementIntent = PendingIntent.getActivities(this, 0, new Intent[]{replacementIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(clickedReplacementIntent);
            new NotificationManager(this).notify(123, notificationBuilder);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_current) {
            showCurrentView();
        } else if (id == R.id.nav_replacement) {
            showReplacementView();
        } else if (id == R.id.nav_schedule) {
            showScheduleView();
        } else if (id == R.id.nav_settings_schedule) {
            SetupActivity.startForNormalSetup(this);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        RealmManager.close();
        super.onDestroy();
    }

    private void updateReplacementData() {
        new ReplacementManager(getApplicationContext()).downloadReplacementFromServerAsyc(new DownloadReplacementListener() {
            @Override
            public void onFinished() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onNoNetworkAvailable() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onError() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    private MainSections getActiveSection() {
        return this.activeSection;
    }
    public void setActiveSection(MainSections activeSection) {
        this.activeSection = activeSection;
        this.swipeRefreshLayout.setCurrentMode(activeSection);
    }

    private View getActiveSectionView() {
        if (getActiveSection() == null) return null;
        switch (getActiveSection().name()) {
            case "CURRENT": return getCurrentView();
            case "REPLACEMENT": return getReplacementContainerView();
            case "SCHEDULE": return getScheduleView();
            default: return null;
        }
    }

    private ViewPager getCurrentView(){
        return this.viewPagerCurrent;
    }
    private void showCurrentView() {
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.spinnerSchedule.setVisibility(View.GONE);
        this.spinnerCurrent.setVisibility(View.VISIBLE);
        if (getScheduleView() != null) {
            crossfadeViews(getActiveSectionView(), getCurrentView());
        }
        setActiveSection(MainSections.CURRENT);
    }

    private View getReplacementContainerView() {
        return this.containerReplacement;
    }
    private MainReplacementRecyclerView getReplacementView() {
        return this.recyclerViewReplacement;
    }
    private void showReplacementView() {
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(true);
        spinnerCurrent.setVisibility(View.GONE);
        spinnerSchedule.setVisibility(View.GONE);
        if (getReplacementView() != null) {
            crossfadeViews(getActiveSectionView(), getReplacementContainerView());
        }
        setActiveSection(MainSections.REPLACEMENT);
    }

    private ViewPager getScheduleView(){
        return this.viewPagerSchedule;
    }
    private void showScheduleView() {
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.spinnerCurrent.setVisibility(View.GONE);
        this.spinnerSchedule.setVisibility(View.VISIBLE);
        if (getScheduleView() != null) {
            crossfadeViews(getActiveSectionView(), getScheduleView());
        }
        setActiveSection(MainSections.SCHEDULE);
    }

    private void crossfadeViews(final View hide, View show) {
        if (show != null && !show.equals(hide)){
            show.setAlpha(0f);
            show.setVisibility(View.VISIBLE);
            show.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .setListener(null);

            if (hide != null) {
                hide.animate()
                        .alpha(0f)
                        .setDuration(150)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                hide.setVisibility(View.INVISIBLE);
                            }
                        });
            }
        }
    }
}

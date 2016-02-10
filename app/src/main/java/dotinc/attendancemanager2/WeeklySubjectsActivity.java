package dotinc.attendancemanager2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import dotinc.attendancemanager2.Adapters.WeeklySubjectsAdapter;
import dotinc.attendancemanager2.Fragements.WeeklySubjectsFragment;
import dotinc.attendancemanager2.Objects.SubjectsList;
import dotinc.attendancemanager2.Objects.TimeTableList;
import dotinc.attendancemanager2.Utils.Helper;
import dotinc.attendancemanager2.Utils.SubjectDatabase;
import dotinc.attendancemanager2.Utils.TimeTableDatabase;

public class WeeklySubjectsActivity extends AppCompatActivity {

    private Context context;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton fab;
    private String[] tabTitles;

    private ArrayList<Fragment> fragments;
    private ArrayList<TimeTableList> arrayList;
    private ArrayList<SubjectsList> subjectsNameList;
    private ArrayList<String> subjects;

    private SubjectDatabase subjectDatabase;
    private TimeTableDatabase database;
    private WeeklySubjectsAdapter pagerAdapter;
    private WeeklySubjectsFragment mon, tue, wed, thu, fri, sat;


    private int timetableFlag;
    private int view_timetable = 0;
    private static int pageNumber = 1;

    void instantiate() {
        context = WeeklySubjectsActivity.this;
        Intent intent = getIntent();
        view_timetable = intent.getIntExtra("view_timetable", 0);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (view_timetable == 1)
            getSupportActionBar().setTitle("Time Table");
        else
            getSupportActionBar().setTitle("Weekly Subjects");

        timetableFlag = intent.getIntExtra("timetableFlag", 0);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.pager);
        fab = (FloatingActionButton) findViewById(R.id.add_subjects);

        mon = new WeeklySubjectsFragment();
        tue = new WeeklySubjectsFragment();
        wed = new WeeklySubjectsFragment();
        thu = new WeeklySubjectsFragment();
        fri = new WeeklySubjectsFragment();
        sat = new WeeklySubjectsFragment();

        fragments = new ArrayList<>();
        fragments.add(mon);
        fragments.add(tue);
        fragments.add(wed);
        fragments.add(thu);
        fragments.add(fri);
        fragments.add(sat);

        tabTitles = getResources().getStringArray(R.array.tabs);

        pagerAdapter =
                new WeeklySubjectsAdapter(getSupportFragmentManager(), fragments, tabTitles, view_timetable);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new PageListener());

        subjectsNameList = new ArrayList<>();
        subjectDatabase = new SubjectDatabase(this);
        database = new TimeTableDatabase(this);
        subjects = new ArrayList<>();
        arrayList = new ArrayList<>();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_subjects);
        instantiate();
        if (view_timetable == 1)
            fab.setVisibility(View.INVISIBLE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTimetable();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subject_name, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();
        else if (item.getItemId() == R.id.done)
            if (view_timetable != 1)
                Helper.saveToPref(context, Helper.COMPLETED, "completed");
        startActivity(new Intent(WeeklySubjectsActivity.this,
                MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void addTimetable() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WeeklySubjectsActivity.this);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.subjects_list, null);
        ListView subjects_view = (ListView) view.findViewById(R.id.subjectList);
        subjectsNameList.clear();
        subjectsNameList = subjectDatabase.getAllSubjects();
        subjects.clear();
        for (int i = 0; i < subjectsNameList.size(); i++)
            subjects.add(subjectsNameList.get(i).getSubjectName().toString());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(WeeklySubjectsActivity.this, android.R.layout.simple_list_item_1, subjects);
        subjects_view.setAdapter(arrayAdapter);
        builder.setView(view);
        subjects_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                addSubjectToTimetable(position);
            }
        });
        builder.create().show();
    }

    private void addSubjectToTimetable(int position) {
        String subjectSelected = subjects.get(position).toString();
        int id = subjectsNameList.get(position).getId();
        TimeTableList timeTableList = new TimeTableList();
        timeTableList.setId(id);
        timeTableList.setDayCode(pageNumber);
        timeTableList.setSubjectName(subjectSelected);
        arrayList = database.getSubjects(timeTableList);
        timeTableList.setPosition(arrayList.size());
        database.addTimeTable(timeTableList);
        database.toast();

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(pageNumber - 1);
    }

    private class PageListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            pageNumber = position + 1;
            arrayList.clear();
        }
    }
}

package com.example.todo_app.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.todo_app.R;
import com.example.todo_app.model.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    public static String currentDate;
    public static boolean showCompleted = true;
    public static final String REST_URL = "http://10.0.2.2:8080/todo_list/"; // Because Android emulator runs in a Virtual Machine therefore here 127.0.0.1 or localhost will be emulator's own loopback address
    private LinearLayout linearLayout;
    private RequestQueue queue;
    private Switch showCompletedTasks;

    private Calendar calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setCurrDateTextLabel();
            linearLayout.removeAllViews();
            getAllTasks();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        showCompletedTasks = (Switch) findViewById(R.id.showCompletedTasks);
        queue = Volley.newRequestQueue(this);

        showCompletedTasks.setChecked(true);

        addChooseDateButtonListener();
        addAddNewTaskButtonListener();
        addShowCompletedTasksSwitchListener();
        setCurrDateTextLabel();

        getAllTasks();
    }

    public void addChooseDateButtonListener() {
        Button chooseDateButton = (Button) findViewById(R.id.chooseDateButton);
        chooseDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(MainActivity.this, date,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    public void addAddNewTaskButtonListener() {
        Button addNewTaskButton = (Button) findViewById(R.id.addNewTaskButton);
        addNewTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildAddNewTaskAlert();
            }
        });
    }

    public void addShowCompletedTasksSwitchListener() {
        showCompletedTasks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                showCompleted = !showCompleted;
                linearLayout.removeAllViews();
                getAllTasks();
            }
        });
    }

    private void buildAddNewTaskAlert() {
        final String[] inputTask = {""};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add new task");
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputTask[0] = input.getText().toString();
                Task task = new Task();
                task.setTask(inputTask[0]);
                task.setCreationDate(getRequestFormattedDate());
                addTask(task);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @SuppressLint("DefaultLocale")
    private void setCurrDateTextLabel() {
        currentDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) +
                "-" +  String.format("%02d", calendar.get(Calendar.MONTH) + 1) +
                "-" + calendar.get(Calendar.YEAR);
        TextView currDate = (TextView) findViewById(R.id.dateTextLabel);
        currDate.setText(currentDate);
    }

    private String getRequestFormattedDate() {
        return  calendar.get(Calendar.YEAR) +
                "-" +  String.format("%02d", calendar.get(Calendar.MONTH) + 1) +
                "-" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
    }

    // http requests

    private void getAllTasks() {
        String url = REST_URL + "tasks/" + LoginActivity.userId;

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+hh:mm").create();
                        Task[] tasks = gson.fromJson(response, Task[].class);
                        populateCheckboxes(tasks);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(stringRequest);
    }

    private void updateTask(Task task) {
        String url = REST_URL + "task/" + task.getId();

        JSONObject object = new JSONObject();
        try {
            object.put("task", task.getTask());
            object.put("active", String.valueOf(task.isActive()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.PUT, url, object,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }

    private void addTask(Task task) {
        String url = REST_URL + "task/" + LoginActivity.userId;

        JSONObject object = new JSONObject();
        try {
            object.put("task", task.getTask());
            object.put("creationDate", task.getCreationDate());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        linearLayout.removeAllViews();
                        getAllTasks();
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }

    private void populateCheckboxes(Task[] tasks) {
        for (Task task : tasks) {
            if (!task.getCreationDate().equals(currentDate))
                continue;

            final CheckBox checkBox = new CheckBox(MainActivity.this);
            checkBox.setText(task.getTask());
            if (!showCompleted && !task.isActive())
                continue;
            if (!task.isActive()) {
                checkBox.setChecked(true);
                checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            addCheckboxListener(checkBox, task);
            linearLayout.addView(checkBox);
        }
    }

    private void addCheckboxListener(final CheckBox checkBox, final Task task) {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checkBox.getPaint().isStrikeThruText())
                    checkBox.setPaintFlags(checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                else
                    checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                task.setActive(!task.isActive());
                updateTask(task);
            }
        });
    }
}
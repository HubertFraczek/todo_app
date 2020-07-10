package com.example.todo_app.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.todo_app.R;
import com.example.todo_app.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.todo_app.activity.MainActivity.REST_URL;

public class LoginActivity extends AppCompatActivity {
    public static int userId;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        queue = Volley.newRequestQueue(this);

        addLoginButtonListener();
        addRegisterButtonListener();
    }

    public void addRegisterButtonListener() {
        Button button = (Button) findViewById(R.id.registerButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildRegisterAlert();
            }
        });
    }

    private void buildRegisterAlert() {
        final String[] input = {"", ""};

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Add new task");
        final EditText username = new EditText(LoginActivity.this);
        final EditText password = new EditText(LoginActivity.this);

        LinearLayout linearLayout = new LinearLayout(LoginActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        username.setInputType(InputType.TYPE_CLASS_TEXT);
        username.setHint("Username");
        linearLayout.addView(username);

        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint("Password");
        linearLayout.addView(password);

        builder.setView(linearLayout);

        builder.setPositiveButton("Sign up", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                input[0] = username.getText().toString();
                input[1] = password.getText().toString();
                signUp(username.getText().toString(), password.getText().toString());
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

    public void addLoginButtonListener() {
        Button button = (Button) findViewById(R.id.loginButton);
        final TextView username = (TextView) findViewById(R.id.usernameTextField);
        final TextView password = (TextView) findViewById(R.id.passwordTextField);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    login(username.getText().toString(), password.getText().toString());
                } catch (AuthFailureError authFailureError) {
                    authFailureError.printStackTrace();
                }
            }
        });
    }

    public void login(String username, String password) throws AuthFailureError {
        String url = REST_URL + "user/" + username + "/" + password;

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            Gson gson = new GsonBuilder().create();
                            User user = gson.fromJson(response, User.class);
                            userId = user.getId();

                            Toast.makeText(LoginActivity.this, "You successfully logged in", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(".MainActivity");
                            startActivity(intent);
                            finish();
                        } else
                            Toast.makeText(LoginActivity.this, "Wrong username or password", Toast.LENGTH_LONG).show();
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        queue.add(stringRequest);
    }

    public void signUp(String username, String password) {
        String url = REST_URL + "user";

        JSONObject object = new JSONObject();
        try {
            object.put("username", username);
            object.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(LoginActivity.this, "You signed up!", Toast.LENGTH_LONG).show();
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "This username is taken", Toast.LENGTH_LONG).show();
                System.out.println(error.toString());
            }
        });
        queue.add(jsonObjectRequest);
    }
}
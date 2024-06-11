package com.weedshop;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Constant;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.webservices.CommonTask;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by MTPC-86 on 3/6/2017.
 */

public class ForgotActivity extends BaseActivity implements OnTaskCompleted {
    private Button btn_forgot;
    private EditText edtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        btn_forgot = (Button) findViewById(R.id.btn_forgot);
        edtEmail = (EditText) findViewById(R.id.edtEmail);

        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.txtBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                if (checkData(email)) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("email", email);
                    CommonTask task = new CommonTask(ForgotActivity.this, Constant.forgot_pass, map, true);
                    task.executeAsync();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private boolean checkData(String email) {
        if (!CommonUtils.checkNetworkConnection(this, R.string.internet_error)) {
            return false;
        }
        if (email.length() == 0) {
            CommonUtils.toastShort(this, R.string.email_valid_error);
            return false;
        }
        if (email.length() != 0 && !CommonUtils.isValidEmail(email)) {
            CommonUtils.toastShort(this, R.string.email_valid_error);
            return false;
        }
        return true;
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                Toast.makeText(ForgotActivity.this, msg, Toast.LENGTH_LONG).show();
                if (response) {
                  /*  Intent m_intent = new Intent(ForgotActivity.this, SignInActivity.class);
                    startActivity(m_intent);*/
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

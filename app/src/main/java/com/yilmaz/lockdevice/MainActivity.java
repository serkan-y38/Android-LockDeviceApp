package com.yilmaz.lockdevice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button adminButton;
    private static final int RESULT_ENABLE = 1;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkDrawOverlays();

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(MainActivity.this, Controller.class);

        adminButton = findViewById(R.id.adminButton);
        if (devicePolicyManager.isAdminActive(componentName))
            adminButton.setText("Disable");
        else
            adminButton.setText("Enable");

        action();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_ENABLE) {
            if (resultCode == Activity.RESULT_OK) {
                adminButton.setText("Disable");
                startService();
            } else {
                Toast.makeText(MainActivity.this, "Oops, something went wrong.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkDrawOverlays();
    }

    @SuppressLint("SetTextI18n")
    private void action() {
        adminButton.setOnClickListener(view -> {
            if (devicePolicyManager.isAdminActive(componentName)) {
                devicePolicyManager.removeActiveAdmin(componentName);
                adminButton.setText("Enable");
                stopService();

            } else {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Please enable to use app.");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_ENABLE);
            }
        });
    }

    private void checkDrawOverlays() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );
            //noinspection deprecation
            startActivityForResult(intent, 0);
        }
    }

    private void startService() {
        Intent i = new Intent(MainActivity.this, FloatingViewService.class);
        i.putExtra(FloatingViewService.SERVICE_ACTION, FloatingViewService.ACTION_START);
        startService(i);
    }

    private void stopService() {
        Intent i = new Intent(MainActivity.this, FloatingViewService.class);
        i.putExtra(FloatingViewService.SERVICE_ACTION, FloatingViewService.ACTION_STOP);
        startService(i);
    }

}
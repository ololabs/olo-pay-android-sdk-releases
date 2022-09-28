// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.olo.olopay.controls.callbacks.CardInputListener;
import com.olo.olopay.controls.callbacks.FormValidCallback;
import com.olo.olopay.data.CardField;
import com.olo.olopay.data.ICardFieldState;
import com.olo.olopay.googlepay.GooglePayContext;
import com.olo.olopay.googlepay.IGooglePayContext;
import com.olo.olopaytestharness.R;
import com.olo.olopaytestharness.fragments.SettingsFragment;
import com.olo.olopaytestharness.databinding.ActivityOloPayTestBinding;
import com.olo.olopaytestharness.olopaysdk.JavaSDKImplementation;
import com.olo.olopaytestharness.util.AppUtils;
import com.olo.olopaytestharness.viewmodels.ActivityViewModel;
import com.olo.olopaytestharness.viewmodels.SettingsViewModel;

import java.util.Map;
import java.util.Set;

public class JavaOloPayTestActivity extends AppCompatActivity implements CardInputListener, FormValidCallback, DialogInterface.OnDismissListener {
    private static final String SettingsTag = "SettingsDialog";
    private ActivityOloPayTestBinding _binding;
    private IGooglePayContext _googlePayContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_olo_pay_test);
        _binding.setLifecycleOwner(this);

        ViewModelProvider provider = new ViewModelProvider(this);
        _binding.setSettings(provider.get(SettingsViewModel.class));
        _binding.setViewModel(provider.get(ActivityViewModel.class));
        _binding.setSdkImpl(new JavaSDKImplementation(_binding.getViewModel(), _binding.getSettings()));

        _googlePayContext = new GooglePayContext(
                this,
                isReady -> _binding.getViewModel().getGooglePayReady().postValue(isReady),
                result -> _binding.getSdkImpl().onGooglePayResult(result));

        _binding.setGooglePayContext(_googlePayContext);

        Toolbar toolbar = _binding.toolbarLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getSupportActionBar().getTitle() + " : Java");

        _binding.cardSingleLineView.setCardInputListener(this);
        _binding.cardMultiLineView.setCardInputListener(this);
        _binding.cardDetailsForm.setFormValidCallback(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings)
            showSettings();
        else if (id == R.id.restart)
            AppUtils.Companion.restartApp(this);

        return true;
    }

    public void showSettings() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SettingsFragment settingsFragment = getSettingsFragment(fragmentManager);

        if (!settingsFragment.isAdded())
            settingsFragment.show(fragmentManager, SettingsTag);
    }

    public SettingsFragment getSettingsFragment(FragmentManager fragmentManager) {
        Fragment settingsFragment = fragmentManager.findFragmentByTag(SettingsTag);
        if (!(settingsFragment instanceof SettingsFragment))
            return new SettingsFragment();

        return (SettingsFragment) settingsFragment;
    }

    @Override
    public void onFocusChange(@NonNull CardField field) {
        logCardInputChange("Card Field Focus Changed: " + field);
    }

    @Override
    public void onFieldComplete(@NonNull CardField field) {
        logCardInputChange("Card Field Complete: " + field);
    }

    @Override
    public void onInputChanged(boolean isValid, @NonNull Set<? extends CardField> invalidFields) {
        logCardInputChange("Input Changed: IsValid: " + isValid);
    }

    @Override
    public void onInputChanged(boolean isValid, @NonNull Map<CardField, ? extends ICardFieldState> fieldStates) {
        logCardInputChange("Input Changed: IsValid: " + isValid);
    }

    private void logCardInputChange(String message) {
        if (!isBound())
            return;

        Boolean logChanges = _binding.getSettings().getLogCardInputChanges().getValue();
        if (logChanges != null && !logChanges)
            return;

        _binding.getViewModel().logText(message);
    }

    private boolean isBound() {
        return settingsBound() && viewModelBound();
    }

    private boolean settingsBound() {
        return _binding.getSettings() != null;
    }

    private boolean viewModelBound() {
        return _binding.getViewModel() != null;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        //No need to check dialog type... settings is the only dialog used in the app
        logSettings();
    }

    private void logSettings() {
        if (!isBound())
            return;

        _binding.getViewModel().logSettings(_binding.getSettings());
    }
}
package com.smsmurilo.inspection.ui.form;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

import com.google.android.material.snackbar.Snackbar;
import com.smsmurilo.inspection.R;
import com.smsmurilo.inspection.data.AppDatabase;
import com.smsmurilo.inspection.data.RoomInspectionRepository;
import com.smsmurilo.inspection.databinding.ActivityFormBinding;
import com.smsmurilo.inspection.domain.Inspection;
import com.smsmurilo.inspection.domain.InspectionValidator;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class InspectionFormActivity extends AppCompatActivity implements InspectionFormContract.View {

    private static final int LOCATION_REQUEST = 7;
    private ActivityFormBinding binding;
    private InspectionFormContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        presenter = new InspectionFormPresenter(new RoomInspectionRepository(AppDatabase.get(this).inspectionDao()),
                Schedulers.io(), AndroidSchedulers.mainThread(), System::currentTimeMillis);
        presenter.attach(this);

        binding.save.setOnClickListener(v -> presenter.onSave(
                text(binding.establishment.getText()), text(binding.cnpj.getText()), selectedResult(), text(binding.notes.getText())));
        requestLocation();
    }

    @Override
    protected void onDestroy() {
        presenter.detach();
        super.onDestroy();
    }

    private Inspection.Result selectedResult() {
        int id = binding.resultGroup.getCheckedRadioButtonId();
        if (id == R.id.resultRemarks) return Inspection.Result.APPROVED_WITH_REMARKS;
        if (id == R.id.resultInterdicted) return Inspection.Result.INTERDICTED;
        return Inspection.Result.APPROVED;
    }

    private static String text(CharSequence c) {
        return c == null ? "" : c.toString();
    }

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        readLocation();
    }

    @SuppressLint("MissingPermission")
    private void readLocation() {
        LocationManager lm = getSystemService(LocationManager.class);
        // Framework LocationManager: works on devices without Google Play Services (common in public-sector fleets).
        LocationManagerCompat.getCurrentLocation(lm, LocationManager.GPS_PROVIDER, (android.os.CancellationSignal) null, ContextCompat.getMainExecutor(this),
                (Location location) -> {
                    if (location != null) presenter.onLocation(location.getLatitude(), location.getLongitude());
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == LOCATION_REQUEST && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) readLocation();
    }

    @Override
    public void showErrors(List<InspectionValidator.Error> errors) {
        binding.establishmentLayout.setError(errors.contains(InspectionValidator.Error.ESTABLISHMENT_REQUIRED) ? "Informe o estabelecimento" : null);
        binding.cnpjLayout.setError(errors.contains(InspectionValidator.Error.INVALID_CNPJ) ? "CNPJ inválido" : null);
        binding.notesLayout.setError(errors.contains(InspectionValidator.Error.NOTES_REQUIRED_FOR_REMARKS) ? "Descreva as irregularidades encontradas" : null);
        if (errors.contains(InspectionValidator.Error.LOCATION_REQUIRED)) showError("Aguardando localização GPS");
    }

    @Override
    public void showLocation(double latitude, double longitude) {
        binding.location.setText(getString(R.string.location_value, latitude, longitude));
    }

    @Override
    public void showSaving(boolean saving) {
        binding.save.setEnabled(!saving);
        binding.progress.setVisibility(saving ? View.VISIBLE : View.GONE);
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }
}

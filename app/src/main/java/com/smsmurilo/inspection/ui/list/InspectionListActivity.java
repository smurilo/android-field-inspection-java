package com.smsmurilo.inspection.ui.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.smsmurilo.inspection.R;
import com.smsmurilo.inspection.data.AppDatabase;
import com.smsmurilo.inspection.data.RoomInspectionRepository;
import com.smsmurilo.inspection.databinding.ActivityListBinding;
import com.smsmurilo.inspection.domain.Inspection;
import com.smsmurilo.inspection.sync.FlakyFakeApi;
import com.smsmurilo.inspection.sync.SyncManager;
import com.smsmurilo.inspection.ui.form.InspectionFormActivity;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class InspectionListActivity extends AppCompatActivity implements InspectionListContract.View {

    private ActivityListBinding binding;
    private InspectionListContract.Presenter presenter;
    private final InspectionAdapter adapter = new InspectionAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        var repository = new RoomInspectionRepository(AppDatabase.get(this).inspectionDao());
        presenter = new InspectionListPresenter(repository,
                new SyncManager(repository, new FlakyFakeApi(), Schedulers.computation()),
                Schedulers.io(), AndroidSchedulers.mainThread());

        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(adapter);
        binding.fab.setOnClickListener(v -> presenter.onNewInspectionClicked());
        binding.syncButton.setOnClickListener(v -> presenter.onSyncClicked());
    }

    @Override protected void onStart() { super.onStart(); presenter.attach(this); }
    @Override protected void onStop() { presenter.detach(); super.onStop(); }

    @Override
    public void showInspections(List<Inspection> inspections, int pendingCount) {
        binding.empty.setVisibility(View.GONE);
        binding.list.setVisibility(View.VISIBLE);
        adapter.submitList(inspections);
        binding.syncButton.setEnabled(pendingCount > 0);
        binding.syncButton.setText(getString(R.string.sync_pending, pendingCount));
    }

    @Override
    public void showEmpty() {
        binding.empty.setVisibility(View.VISIBLE);
        binding.list.setVisibility(View.GONE);
        binding.syncButton.setEnabled(false);
    }

    @Override
    public void showSyncing(boolean syncing) {
        binding.progress.setVisibility(syncing ? View.VISIBLE : View.GONE);
        binding.syncButton.setEnabled(!syncing);
    }

    @Override
    public void showSyncReport(int synced, int failed) {
        Snackbar.make(binding.getRoot(), getString(R.string.sync_report, synced, failed), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void openForm() {
        startActivity(new Intent(this, InspectionFormActivity.class));
    }
}

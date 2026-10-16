package com.smsmurilo.inspection.ui.list;

import com.smsmurilo.inspection.data.InspectionRepository;
import com.smsmurilo.inspection.domain.Inspection;
import com.smsmurilo.inspection.sync.SyncManager;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class InspectionListPresenter implements InspectionListContract.Presenter {

    private final InspectionRepository repository;
    private final SyncManager syncManager;
    private final Scheduler io;
    private final Scheduler ui;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private InspectionListContract.View view;
    private boolean syncing;

    public InspectionListPresenter(InspectionRepository repository, SyncManager syncManager, Scheduler io, Scheduler ui) {
        this.repository = repository;
        this.syncManager = syncManager;
        this.io = io;
        this.ui = ui;
    }

    @Override
    public void attach(InspectionListContract.View view) {
        this.view = view;
        disposables.add(repository.observeAll()
                .subscribeOn(io)
                .observeOn(ui)
                .subscribe(list -> {
                    if (list.isEmpty()) {
                        view.showEmpty();
                    } else {
                        long pending = list.stream().filter(i -> i.syncStatusEnum() != Inspection.SyncStatus.SYNCED).count();
                        view.showInspections(list, (int) pending);
                    }
                }, error -> view.showError("Falha ao carregar fiscalizações")));
    }

    @Override
    public void detach() {
        // Prevents leaking the Activity and delivering results to a destroyed view.
        disposables.clear();
        view = null;
    }

    @Override
    public void onSyncClicked() {
        if (syncing || view == null) return;
        syncing = true;
        view.showSyncing(true);
        disposables.add(syncManager.syncPending()
                .subscribeOn(io)
                .observeOn(ui)
                .doFinally(() -> {
                    syncing = false;
                    if (view != null) view.showSyncing(false);
                })
                .subscribe(
                        report -> { if (view != null) view.showSyncReport(report.synced(), report.failed()); },
                        error -> { if (view != null) view.showError("Sincronização interrompida"); }));
    }

    @Override
    public void onNewInspectionClicked() {
        if (view != null) view.openForm();
    }
}

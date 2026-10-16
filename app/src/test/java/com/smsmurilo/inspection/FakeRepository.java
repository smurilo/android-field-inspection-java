package com.smsmurilo.inspection;

import com.smsmurilo.inspection.data.InspectionRepository;
import com.smsmurilo.inspection.domain.Inspection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

class FakeRepository implements InspectionRepository {

    final List<Inspection> items = new ArrayList<>();
    private final BehaviorProcessor<List<Inspection>> stream = BehaviorProcessor.createDefault(List.of());

    static Inspection inspection(String id) {
        Inspection i = new Inspection();
        i.id = id;
        i.establishment = "Padaria " + id;
        i.cnpj = "11222333000181";
        return i;
    }

    private void publish() { stream.onNext(new ArrayList<>(items)); }

    @Override public Flowable<List<Inspection>> observeAll() { return stream; }

    @Override public Completable save(Inspection inspection) {
        return Completable.fromAction(() -> { items.add(inspection); publish(); });
    }

    @Override public Single<List<Inspection>> pendingSync() {
        return Single.fromCallable(() -> items.stream()
                .filter(i -> i.syncStatusEnum() != Inspection.SyncStatus.SYNCED).collect(Collectors.toList()));
    }

    @Override public Completable markSynced(String id) { return mark(id, Inspection.SyncStatus.SYNCED); }
    @Override public Completable markFailed(String id) { return mark(id, Inspection.SyncStatus.FAILED); }

    private Completable mark(String id, Inspection.SyncStatus status) {
        return Completable.fromAction(() -> {
            items.stream().filter(i -> i.id.equals(id)).forEach(i -> { i.syncStatus = status.name(); i.syncAttempts++; });
            publish();
        });
    }
}

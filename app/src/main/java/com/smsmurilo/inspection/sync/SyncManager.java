package com.smsmurilo.inspection.sync;

import com.smsmurilo.inspection.data.InspectionRepository;
import com.smsmurilo.inspection.domain.Inspection;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;

/**
 * Uploads pending inspections one by one. Transient network errors are retried with
 * exponential backoff (1s, 2s, 4s); a record that still fails is marked FAILED and the
 * batch continues, so one bad record never blocks the others.
 */
public class SyncManager {

    public static final int MAX_RETRIES = 3;

    private final InspectionRepository repository;
    private final InspectionApi api;
    private final Scheduler timer;

    public SyncManager(InspectionRepository repository, InspectionApi api, Scheduler timer) {
        this.repository = repository;
        this.api = api;
        this.timer = timer;
    }

    public record Report(int synced, int failed) {}

    public Single<Report> syncPending() {
        AtomicInteger synced = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        return repository.pendingSync()
                .flatMapObservable(Observable::fromIterable)
                .concatMapCompletable(inspection -> upload(inspection)
                        .andThen(repository.markSynced(inspection.id))
                        .doOnComplete(synced::incrementAndGet)
                        .onErrorResumeNext(error -> repository.markFailed(inspection.id).doOnComplete(failed::incrementAndGet)))
                .toSingle(() -> new Report(synced.get(), failed.get()));
    }

    private io.reactivex.rxjava3.core.Completable upload(Inspection inspection) {
        return api.upload(inspection).retryWhen(errors -> errors
                .zipWith(Flowable.range(1, MAX_RETRIES + 1), (error, attempt) -> {
                    if (!(error instanceof IOException) || attempt > MAX_RETRIES) throw error;
                    return attempt;
                })
                .flatMap(attempt -> Flowable.timer(1L << (attempt - 1), TimeUnit.SECONDS, timer)));
    }
}

package com.smsmurilo.inspection;

import com.smsmurilo.inspection.domain.Inspection;
import com.smsmurilo.inspection.sync.InspectionApi;
import com.smsmurilo.inspection.sync.SyncManager;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.TestScheduler;

import static org.junit.Assert.assertEquals;

public class SyncManagerTest {

    /** Fails each record a configured number of times, then succeeds. */
    private static class ScriptedApi implements InspectionApi {
        final Map<String, Integer> failuresLeft = new HashMap<>();
        final Map<String, Integer> calls = new HashMap<>();
        RuntimeException permanent;

        @Override
        public Completable upload(Inspection inspection) {
            return Completable.defer(() -> {
                calls.merge(inspection.id, 1, Integer::sum);
                if (permanent != null && inspection.id.equals("bad")) return Completable.error(permanent);
                int left = failuresLeft.getOrDefault(inspection.id, 0);
                if (left > 0) {
                    failuresLeft.put(inspection.id, left - 1);
                    return Completable.error(new IOException("timeout"));
                }
                return Completable.complete();
            });
        }
    }

    @Test
    public void retriesTransientErrorsWithExponentialBackoff() {
        FakeRepository repo = new FakeRepository();
        repo.items.add(FakeRepository.inspection("a"));
        ScriptedApi api = new ScriptedApi();
        api.failuresLeft.put("a", 2);
        TestScheduler time = new TestScheduler();

        TestObserver<SyncManager.Report> result = new SyncManager(repo, api, time).syncPending().test();

        time.advanceTimeBy(1, TimeUnit.SECONDS);   // 1st retry after 1s
        assertEquals(2, (int) api.calls.get("a"));
        time.advanceTimeBy(2, TimeUnit.SECONDS);   // 2nd retry after 2s
        result.assertValue(new SyncManager.Report(1, 0));
        assertEquals(Inspection.SyncStatus.SYNCED, repo.items.get(0).syncStatusEnum());
    }

    @Test
    public void oneFailingRecordDoesNotBlockTheBatch() {
        FakeRepository repo = new FakeRepository();
        repo.items.add(FakeRepository.inspection("bad"));
        repo.items.add(FakeRepository.inspection("ok"));
        ScriptedApi api = new ScriptedApi();
        api.permanent = new IllegalStateException("422 dados inválidos");
        TestScheduler time = new TestScheduler();

        TestObserver<SyncManager.Report> result = new SyncManager(repo, api, time).syncPending().test();
        time.advanceTimeBy(1, TimeUnit.MINUTES);

        result.assertValue(new SyncManager.Report(1, 1));
        assertEquals(1, (int) api.calls.get("bad")); // non-network errors are not retried
        assertEquals(Inspection.SyncStatus.FAILED, repo.items.get(0).syncStatusEnum());
    }

    @Test
    public void givesUpAfterMaxRetries() {
        FakeRepository repo = new FakeRepository();
        repo.items.add(FakeRepository.inspection("a"));
        ScriptedApi api = new ScriptedApi();
        api.failuresLeft.put("a", 99);
        TestScheduler time = new TestScheduler();

        TestObserver<SyncManager.Report> result = new SyncManager(repo, api, time).syncPending().test();
        time.advanceTimeBy(1, TimeUnit.MINUTES);

        result.assertValue(new SyncManager.Report(0, 1));
        assertEquals(SyncManager.MAX_RETRIES + 1, (int) api.calls.get("a"));
    }
}

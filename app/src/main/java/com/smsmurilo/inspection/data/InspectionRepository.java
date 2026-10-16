package com.smsmurilo.inspection.data;

import com.smsmurilo.inspection.domain.Inspection;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface InspectionRepository {
    Flowable<List<Inspection>> observeAll();

    Completable save(Inspection inspection);

    Single<List<Inspection>> pendingSync();

    Completable markSynced(String id);

    Completable markFailed(String id);
}

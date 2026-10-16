package com.smsmurilo.inspection.data;

import com.smsmurilo.inspection.domain.Inspection;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class RoomInspectionRepository implements InspectionRepository {

    private final InspectionDao dao;

    public RoomInspectionRepository(InspectionDao dao) {
        this.dao = dao;
    }

    @Override public Flowable<List<Inspection>> observeAll() { return dao.observeAll(); }
    @Override public Completable save(Inspection inspection) { return dao.upsert(inspection); }
    @Override public Single<List<Inspection>> pendingSync() { return dao.pendingSync(); }
    @Override public Completable markSynced(String id) { return dao.updateSyncStatus(id, Inspection.SyncStatus.SYNCED.name()); }
    @Override public Completable markFailed(String id) { return dao.updateSyncStatus(id, Inspection.SyncStatus.FAILED.name()); }
}

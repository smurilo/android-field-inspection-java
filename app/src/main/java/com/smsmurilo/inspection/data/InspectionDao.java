package com.smsmurilo.inspection.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.smsmurilo.inspection.domain.Inspection;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface InspectionDao {

    @Query("SELECT * FROM inspections ORDER BY createdAt DESC")
    Flowable<List<Inspection>> observeAll();

    @Query("SELECT * FROM inspections WHERE syncStatus != 'SYNCED' ORDER BY createdAt")
    Single<List<Inspection>> pendingSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable upsert(Inspection inspection);

    @Query("UPDATE inspections SET syncStatus = :status, syncAttempts = syncAttempts + 1 WHERE id = :id")
    Completable updateSyncStatus(String id, String status);
}

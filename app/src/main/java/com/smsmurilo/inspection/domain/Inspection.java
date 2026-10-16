package com.smsmurilo.inspection.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A sanitary inspection performed in the field, often without connectivity.
 * Created locally with a client-side UUID so it can be synced later without duplicates.
 */
@Entity(tableName = "inspections", indices = {@Index("syncStatus")})
public class Inspection {

    public enum Result { APPROVED, APPROVED_WITH_REMARKS, INTERDICTED }

    public enum SyncStatus { PENDING, SYNCED, FAILED }

    @PrimaryKey @NonNull public String id = "";
    @NonNull public String establishment = "";
    @NonNull public String cnpj = "";
    @NonNull public String result = Result.APPROVED.name();
    @Nullable public String notes;
    public double latitude;
    public double longitude;
    public long createdAt;
    @NonNull public String syncStatus = SyncStatus.PENDING.name();
    public int syncAttempts;

    public Result resultEnum() {
        return Result.valueOf(result);
    }

    public SyncStatus syncStatusEnum() {
        return SyncStatus.valueOf(syncStatus);
    }
}

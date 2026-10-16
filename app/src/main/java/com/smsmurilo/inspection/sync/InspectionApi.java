package com.smsmurilo.inspection.sync;

import com.smsmurilo.inspection.domain.Inspection;

import io.reactivex.rxjava3.core.Completable;

/** Remote endpoint. Must be idempotent on {@code inspection.id} because the client may resend. */
public interface InspectionApi {
    Completable upload(Inspection inspection);
}

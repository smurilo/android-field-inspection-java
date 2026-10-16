package com.smsmurilo.inspection.sync;

import com.smsmurilo.inspection.domain.Inspection;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Completable;

/** Simulates a 3G connection in the countryside: slow and failing ~40% of the time. */
public class FlakyFakeApi implements InspectionApi {

    private final Random random = new Random();

    @Override
    public Completable upload(Inspection inspection) {
        return Completable.defer(() -> random.nextInt(10) < 4
                        ? Completable.error(new IOException("timeout"))
                        : Completable.complete())
                .delay(400, TimeUnit.MILLISECONDS);
    }
}

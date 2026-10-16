package com.smsmurilo.inspection.ui.form;

import com.smsmurilo.inspection.data.InspectionRepository;
import com.smsmurilo.inspection.domain.Inspection;
import com.smsmurilo.inspection.domain.InspectionValidator;

import java.util.List;
import java.util.UUID;
import java.util.function.LongSupplier;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class InspectionFormPresenter implements InspectionFormContract.Presenter {

    private final InspectionRepository repository;
    private final Scheduler io;
    private final Scheduler ui;
    private final LongSupplier clock;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private InspectionFormContract.View view;
    private Double latitude;
    private Double longitude;

    public InspectionFormPresenter(InspectionRepository repository, Scheduler io, Scheduler ui, LongSupplier clock) {
        this.repository = repository;
        this.io = io;
        this.ui = ui;
        this.clock = clock;
    }

    @Override
    public void attach(InspectionFormContract.View view) {
        this.view = view;
        if (latitude != null) view.showLocation(latitude, longitude);
    }

    @Override
    public void detach() {
        disposables.clear();
        view = null;
    }

    @Override
    public void onLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        if (view != null) view.showLocation(latitude, longitude);
    }

    @Override
    public void onSave(String establishment, String cnpj, Inspection.Result result, String notes) {
        List<InspectionValidator.Error> errors = InspectionValidator.validate(establishment, cnpj, result, notes, latitude, longitude);
        view.showErrors(errors);
        if (!errors.isEmpty()) return;

        Inspection inspection = new Inspection();
        inspection.id = UUID.randomUUID().toString();
        inspection.establishment = establishment.trim();
        inspection.cnpj = cnpj.replaceAll("\\D", "");
        inspection.result = result.name();
        inspection.notes = notes == null || notes.isBlank() ? null : notes.trim();
        inspection.latitude = latitude;
        inspection.longitude = longitude;
        inspection.createdAt = clock.getAsLong();

        view.showSaving(true);
        // Saved locally first: works with no signal; the sync queue uploads it later.
        disposables.add(repository.save(inspection)
                .subscribeOn(io)
                .observeOn(ui)
                .subscribe(() -> { if (view != null) view.close(); },
                        e -> { if (view != null) { view.showSaving(false); view.showError("Não foi possível salvar"); } }));
    }
}

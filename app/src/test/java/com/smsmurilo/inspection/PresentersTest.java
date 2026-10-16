package com.smsmurilo.inspection;

import com.smsmurilo.inspection.domain.Inspection;
import com.smsmurilo.inspection.domain.InspectionValidator;
import com.smsmurilo.inspection.sync.SyncManager;
import com.smsmurilo.inspection.ui.form.InspectionFormContract;
import com.smsmurilo.inspection.ui.form.InspectionFormPresenter;
import com.smsmurilo.inspection.ui.list.InspectionListContract;
import com.smsmurilo.inspection.ui.list.InspectionListPresenter;

import org.junit.Test;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PresentersTest {

    @Test
    public void formRejectsInvalidDataAndRequiresNotesForFindings() {
        FakeRepository repo = new FakeRepository();
        InspectionFormContract.View view = mock(InspectionFormContract.View.class);
        InspectionFormPresenter presenter = new InspectionFormPresenter(repo, Schedulers.trampoline(), Schedulers.trampoline(), () -> 1L);
        presenter.attach(view);

        presenter.onSave("Pa", "11.222.333/0001-80", Inspection.Result.INTERDICTED, "");

        verify(view).showErrors(List.of(
                InspectionValidator.Error.ESTABLISHMENT_REQUIRED,
                InspectionValidator.Error.INVALID_CNPJ,
                InspectionValidator.Error.NOTES_REQUIRED_FOR_REMARKS,
                InspectionValidator.Error.LOCATION_REQUIRED));
        verify(view, never()).close();
    }

    @Test
    public void formSavesOfflineAsPendingAndCloses() {
        FakeRepository repo = new FakeRepository();
        InspectionFormContract.View view = mock(InspectionFormContract.View.class);
        InspectionFormPresenter presenter = new InspectionFormPresenter(repo, Schedulers.trampoline(), Schedulers.trampoline(), () -> 42L);
        presenter.attach(view);
        presenter.onLocation(-9.66, -35.73);

        presenter.onSave("Restaurante Sabor", "11.222.333/0001-81", Inspection.Result.APPROVED_WITH_REMARKS, "Extintor vencido na cozinha");

        verify(view).close();
        Inspection saved = repo.items.get(0);
        assertEquals("11222333000181", saved.cnpj);
        assertEquals(Inspection.SyncStatus.PENDING, saved.syncStatusEnum());
        assertEquals(42L, saved.createdAt);
    }

    @Test
    public void listShowsPendingCountAndIgnoresSyncAfterDetach() {
        FakeRepository repo = new FakeRepository();
        repo.save(FakeRepository.inspection("1")).blockingAwait();
        InspectionListContract.View view = mock(InspectionListContract.View.class);
        SyncManager sync = new SyncManager(repo, i -> Completable.complete(), Schedulers.trampoline());
        InspectionListPresenter presenter = new InspectionListPresenter(repo, sync, Schedulers.trampoline(), Schedulers.trampoline());

        presenter.attach(view);
        verify(view).showInspections(anyList(), org.mockito.ArgumentMatchers.eq(1));

        presenter.onSyncClicked();
        verify(view).showSyncReport(1, 0);

        presenter.detach();
        presenter.onSyncClicked(); // must not touch a detached view
        assertFalse(repo.items.isEmpty());
    }
}

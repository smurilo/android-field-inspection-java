package com.smsmurilo.inspection.ui.list;

import com.smsmurilo.inspection.domain.Inspection;

import java.util.List;

/** MVP contract: the View is passive; the Presenter holds all presentation logic and is plain Java. */
public interface InspectionListContract {

    interface View {
        void showInspections(List<Inspection> inspections, int pendingCount);
        void showEmpty();
        void showSyncing(boolean syncing);
        void showSyncReport(int synced, int failed);
        void showError(String message);
        void openForm();
    }

    interface Presenter {
        void attach(View view);
        void detach();
        void onSyncClicked();
        void onNewInspectionClicked();
    }
}

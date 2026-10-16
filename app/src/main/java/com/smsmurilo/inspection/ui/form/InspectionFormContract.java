package com.smsmurilo.inspection.ui.form;

import com.smsmurilo.inspection.domain.Inspection;
import com.smsmurilo.inspection.domain.InspectionValidator;

import java.util.List;

public interface InspectionFormContract {

    interface View {
        void showErrors(List<InspectionValidator.Error> errors);
        void showLocation(double latitude, double longitude);
        void showSaving(boolean saving);
        void close();
        void showError(String message);
    }

    interface Presenter {
        void attach(View view);
        void detach();
        void onLocation(double latitude, double longitude);
        void onSave(String establishment, String cnpj, Inspection.Result result, String notes);
    }
}

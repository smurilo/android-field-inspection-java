package com.smsmurilo.inspection.domain;

import java.util.ArrayList;
import java.util.List;

public final class InspectionValidator {

    public enum Error { ESTABLISHMENT_REQUIRED, INVALID_CNPJ, NOTES_REQUIRED_FOR_REMARKS, LOCATION_REQUIRED }

    private InspectionValidator() {}

    public static List<Error> validate(String establishment, String cnpj, Inspection.Result result, String notes,
                                       Double latitude, Double longitude) {
        List<Error> errors = new ArrayList<>();
        if (establishment == null || establishment.trim().length() < 3) errors.add(Error.ESTABLISHMENT_REQUIRED);
        if (!isValidCnpj(cnpj)) errors.add(Error.INVALID_CNPJ);
        // Legal requirement: any finding other than a clean approval must be justified in writing.
        if (result != Inspection.Result.APPROVED && (notes == null || notes.trim().length() < 10)) {
            errors.add(Error.NOTES_REQUIRED_FOR_REMARKS);
        }
        if (latitude == null || longitude == null) errors.add(Error.LOCATION_REQUIRED);
        return errors;
    }

    public static boolean isValidCnpj(String raw) {
        if (raw == null) return false;
        String cnpj = raw.replaceAll("\\D", "");
        if (cnpj.length() != 14 || cnpj.chars().distinct().count() == 1) return false;
        int[] w1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] w2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return digit(cnpj, w1) == cnpj.charAt(12) - '0' && digit(cnpj, w2) == cnpj.charAt(13) - '0';
    }

    private static int digit(String cnpj, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) sum += (cnpj.charAt(i) - '0') * weights[i];
        int rest = sum % 11;
        return rest < 2 ? 0 : 11 - rest;
    }
}

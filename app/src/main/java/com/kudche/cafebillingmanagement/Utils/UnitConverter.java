package com.kudche.cafebillingmanagement.Utils;

public class UnitConverter {

    public static final String UNIT_KG = "KG";
    public static final String UNIT_GRAM = "GRAM";
    public static final String UNIT_LITER = "LITRE";
    public static final String UNIT_ML = "ML";
    public static final String UNIT_PCS = "QUANTITY";

    /**
     * Converts any unit to its base unit (KG, LITER, or PCS) for storage.
     */
    public static double convertToBaseUnit(double value, String unit) {
        switch (unit) {
            case UNIT_GRAM:
                return value / 1000.0;
            case UNIT_ML:
                return value / 1000.0;
            case UNIT_KG:
            case UNIT_LITER:
            case UNIT_PCS:
            default:
                return value;
        }
    }

    /**
     * Converts from base unit to a specific display unit.
     */
    public static double convertFromBaseUnit(double value, String targetUnit) {
        switch (targetUnit) {
            case UNIT_GRAM:
                return value * 1000.0;
            case UNIT_ML:
                return value * 1000.0;
            case UNIT_KG:
            case UNIT_LITER:
            case UNIT_PCS:
            default:
                return value;
        }
    }

    public static String getBaseUnit(String unit) {
        if (unit.equals(UNIT_GRAM) || unit.equals(UNIT_KG)) return UNIT_KG;
        if (unit.equals(UNIT_ML) || unit.equals(UNIT_LITER)) return UNIT_LITER;
        return UNIT_PCS;
    }

    public static String[] getRelatedUnits(String baseUnit) {
        if (baseUnit.equals(UNIT_KG) || baseUnit.equals(UNIT_GRAM)) {
            return new String[]{UNIT_GRAM, UNIT_KG};
        } else if (baseUnit.equals(UNIT_LITER) || baseUnit.equals(UNIT_ML)) {
            return new String[]{UNIT_ML, UNIT_LITER};
        } else {
            return new String[]{UNIT_PCS};
        }
    }
}
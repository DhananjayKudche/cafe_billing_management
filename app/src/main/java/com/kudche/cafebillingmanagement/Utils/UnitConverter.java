package com.kudche.cafebillingmanagement.Utils;

import java.util.HashMap;
import java.util.Map;

public class UnitConverter {

    private static final Map<String, Double> TO_BASE_UNIT = new HashMap<>();

    static {
        // Base units are GRAM for weight and ML for volume
        TO_BASE_UNIT.put("GRAM", 1.0);
        TO_BASE_UNIT.put("KG", 1000.0);
        TO_BASE_UNIT.put("ML", 1.0);
        TO_BASE_UNIT.put("LITRE", 1000.0);
        TO_BASE_UNIT.put("QUANTITY", 1.0);
    }

    public static double convertToBase(double value, String unit) {
        Double factor = TO_BASE_UNIT.get(unit.toUpperCase());
        return factor != null ? value * factor : value;
    }

    public static double convertFromBase(double baseValue, String targetUnit) {
        Double factor = TO_BASE_UNIT.get(targetUnit.toUpperCase());
        return factor != null ? baseValue / factor : baseValue;
    }

    public static String getBaseUnit(String unit) {
        String u = unit.toUpperCase();
        if (u.equals("KG") || u.equals("GRAM")) return "GRAM";
        if (u.equals("LITRE") || u.equals("ML")) return "ML";
        return "QUANTITY";
    }
}
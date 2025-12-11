package com.unam.mvmsys.ui;

import javafx.scene.control.Button;

/**
 * Factory helpers for consistent table action buttons.
 */
public final class TableActionButtons {

    private TableActionButtons() { }

    public static Button iconButton(String iconText, Runnable action, String... styleClasses) {
        Button btn = new Button(iconText);
        btn.getStyleClass().clear();
        btn.getStyleClass().add("table-action");
        if (styleClasses != null) {
            for (String cls : styleClasses) {
                if (cls != null && !cls.isBlank()) btn.getStyleClass().add(cls);
            }
        }
        btn.setOnAction(e -> action.run());
        return btn;
    }

    public static void setDanger(Button btn) {
        applyVariant(btn, "danger");
    }

    public static void setSuccess(Button btn) {
        applyVariant(btn, "success");
    }

    public static void setNeutral(Button btn) {
        applyVariant(btn, null);
    }

    private static void applyVariant(Button btn, String variant) {
        btn.getStyleClass().clear();
        btn.getStyleClass().add("table-action");
        if (variant != null) btn.getStyleClass().add(variant);
    }
}

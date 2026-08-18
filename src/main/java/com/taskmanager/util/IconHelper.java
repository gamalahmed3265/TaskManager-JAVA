package com.taskmanager.util;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * مُساعد لإنشاء أيقونات SVG Vectorية خفيفة وعصرية لجميع عناصر الواجهة
 */
public class IconHelper {

    // مسارات SVG بنمط Outline (Stroke) عصري
    public static final String PLUS = "M12 5v14M5 12h14";
    public static final String EDIT = "M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z";
    public static final String TRASH = "M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M10 11v6M14 11v6";
    public static final String PLAY = "M5 3l14 9-14 9V3z";
    public static final String PAUSE = "M6 4h4v16H6zM14 4h4v16h-4z";
    public static final String CLOCK = "M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z M12 6v6l4 2";
    public static final String CHECK = "M20 6L9 17l-5-5";
    public static final String SPARKLES = "M12 2L9.5 8.5 3 11l6.5 2.5L12 20l2.5-6.5L21 11l-6.5-2.5z";
    public static final String CALENDAR = "M19 4H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2zM16 2v4M8 2v4M3 10h18";
    public static final String TARGET = "M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10zm0-4a6 6 0 1 0 0-12 6 6 0 0 0 0 12zm0-4a2 2 0 1 0 0-4 2 2 0 0 0 0 4z";
    public static final String EXPORT = "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5-5 5 5M12 5v12";
    public static final String MOON = "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z";
    public static final String SEARCH = "M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16zm10 2l-4.35-4.35";
    public static final String TAG = "M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z";
    public static final String FLAG = "M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1zm0 0v7";

    /**
     * ينشئ أيقونة SVG Stroke
     */
    public static Node createStrokeIcon(String svgContent, double size, String colorHex) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Paint.valueOf(colorHex));
        path.setStrokeWidth(2.0);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);

        StackPane container = new StackPane(path);
        container.setMaxSize(size, size);
        container.setMinSize(size, size);
        container.setPrefSize(size, size);

        double scale = size / 24.0;
        path.setScaleX(scale);
        path.setScaleY(scale);

        return container;
    }

    /**
     * ينشئ أيقونة SVG Fill
     */
    public static Node createFillIcon(String svgContent, double size, String colorHex) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.setFill(Paint.valueOf(colorHex));
        path.setStroke(Color.TRANSPARENT);

        StackPane container = new StackPane(path);
        container.setMaxSize(size, size);
        container.setMinSize(size, size);
        container.setPrefSize(size, size);

        double scale = size / 24.0;
        path.setScaleX(scale);
        path.setScaleY(scale);

        return container;
    }

    public static Node plus(double size, String colorHex) { return createStrokeIcon(PLUS, size, colorHex); }
    public static Node edit(double size, String colorHex) { return createStrokeIcon(EDIT, size, colorHex); }
    public static Node trash(double size, String colorHex) { return createStrokeIcon(TRASH, size, colorHex); }
    public static Node play(double size, String colorHex) { return createFillIcon(PLAY, size, colorHex); }
    public static Node pause(double size, String colorHex) { return createFillIcon(PAUSE, size, colorHex); }
    public static Node clock(double size, String colorHex) { return createStrokeIcon(CLOCK, size, colorHex); }
    public static Node check(double size, String colorHex) { return createStrokeIcon(CHECK, size, colorHex); }
    public static Node sparkles(double size, String colorHex) { return createStrokeIcon(SPARKLES, size, colorHex); }
    public static Node calendar(double size, String colorHex) { return createStrokeIcon(CALENDAR, size, colorHex); }
    public static Node target(double size, String colorHex) { return createStrokeIcon(TARGET, size, colorHex); }
    public static Node export(double size, String colorHex) { return createStrokeIcon(EXPORT, size, colorHex); }
    public static Node moon(double size, String colorHex) { return createStrokeIcon(MOON, size, colorHex); }
    public static Node search(double size, String colorHex) { return createStrokeIcon(SEARCH, size, colorHex); }
    public static Node tag(double size, String colorHex) { return createStrokeIcon(TAG, size, colorHex); }
    public static Node flag(double size, String colorHex) { return createStrokeIcon(FLAG, size, colorHex); }
}

package app.ui;

import javafx.geometry.Orientation;
import javafx.scene.layout.StackPane;

/**
 * A pane whose height is its width times a fixed ratio, reported through content
 * bias so layout parents (grids) size it in the same pass instead of lagging a
 * frame on resize.
 */
final class AspectPane extends StackPane {

    private final double aspect;

    AspectPane(double aspect) {
        this.aspect = aspect;
        setMaxWidth(Double.MAX_VALUE);
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computeMinHeight(double width) {
        return height(width);
    }

    @Override
    protected double computePrefHeight(double width) {
        return height(width);
    }

    @Override
    protected double computeMaxHeight(double width) {
        return height(width);
    }

    private double height(double width) {
        return (width >= 0 ? width : getWidth()) * aspect;
    }
}

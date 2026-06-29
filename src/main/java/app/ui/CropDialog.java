package app.ui;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A simple dialog to crop uploaded images to a given aspect ratio.
 * A user can drag the selection rectangle to move it, or drag the corner handle to resize it.
 */
public final class CropDialog {

    /** Largest side of the preview the user drags the selection over. */
    private static final double PREVIEW_MAX = 560;
    /** Largest side of the produced crop, to keep stored images a sane size. */
    private static final double OUTPUT_MAX = 1600;

    private CropDialog() {
    }

    static byte[] crop(byte[] data, double targetRatio) {
        Image src = new Image(new ByteArrayInputStream(data));
        if (src.isError() || src.getWidth() == 0 || src.getHeight() == 0) {
            return data;
        }

        double srcW = src.getWidth();
        double srcH = src.getHeight();
        final double scale = Math.min(Math.min(PREVIEW_MAX / srcW, PREVIEW_MAX / srcH), 2);
        double dispW = srcW * scale;
        double dispH = srcH * scale;

        ImageView preview = new ImageView(src);
        preview.setFitWidth(dispW);
        preview.setFitHeight(dispH);
        preview.setPreserveRatio(true);

        Pane canvas = new Pane(preview);
        canvas.setMinSize(dispW, dispH);
        canvas.setPrefSize(dispW, dispH);
        canvas.setMaxSize(dispW, dispH);

        double selW;
        double selH;
        if (dispH / dispW > targetRatio) {
            selW = dispW;
            selH = selW * targetRatio;
        } else {
            selH = dispH;
            selW = selH / targetRatio;
        }
        Rectangle sel = new Rectangle((dispW - selW) / 2, (dispH - selH) / 2, selW, selH);
        sel.setFill(Color.color(1, 1, 1, 0.15));
        sel.setStroke(Color.web("#ffd000"));
        sel.setStrokeWidth(2);
        sel.setCursor(Cursor.MOVE);

        Rectangle handle = new Rectangle(14, 14, Color.web("#ffd000"));
        handle.setCursor(Cursor.SE_RESIZE);
        Runnable placeHandle = () -> {
            handle.setX(sel.getX() + sel.getWidth() - 7);
            handle.setY(sel.getY() + sel.getHeight() - 7);
        };
        placeHandle.run();

        double[] dragOffset = new double[2];
        sel.setOnMousePressed(e -> {
            dragOffset[0] = e.getX() - sel.getX();
            dragOffset[1] = e.getY() - sel.getY();
        });
        sel.setOnMouseDragged(e -> {
            sel.setX(clamp(e.getX() - dragOffset[0], 0, dispW - sel.getWidth()));
            sel.setY(clamp(e.getY() - dragOffset[1], 0, dispH - sel.getHeight()));
            placeHandle.run();
        });
        handle.setOnMouseDragged(e -> {
            double nw = clamp(e.getX() - sel.getX(), 40, dispW - sel.getX());
            double nh = nw * targetRatio;
            if (sel.getY() + nh > dispH) {
                nh = dispH - sel.getY();
                nw = nh / targetRatio;
            }
            sel.setWidth(nw);
            sel.setHeight(nh);
            placeHandle.run();
        });

        canvas.getChildren().addAll(sel, handle);

        byte[][] result = {null};
        Stage dialog = new Stage();

        Button use = Ui.button("Use crop", 13, "-fx-background-color: #ffd000;");
        use.setOnAction(e -> {
            result[0] = render(src, scale, sel, data);
            dialog.close();
        });
        Button cancel = Ui.button("Cancel", 13, "-fx-background-color: #eeeeee;");
        cancel.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, Ui.spacer(), cancel, use);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(14, Ui.light("Drag to move, drag the corner to resize.", 12), canvas, buttons);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        dialog.initOwner(ShareS.primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Crop image");
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
        return result[0];
    }

    /** Extracts the selected region at source resolution and encodes it to PNG. */
    private static byte[] render(Image src, double scale, Rectangle sel, byte[] fallback) {
        double sx = sel.getX() / scale;
        double sy = sel.getY() / scale;
        double sw = sel.getWidth() / scale;
        double sh = sel.getHeight() / scale;

        ImageView cropView = new ImageView(src);
        cropView.setViewport(new Rectangle2D(sx, sy, sw, sh));

        SnapshotParameters params = new SnapshotParameters();
        double outScale = Math.min(1.0, OUTPUT_MAX / sw);
        if (outScale < 1.0) {
            params.setTransform(new Scale(outScale, outScale));
        }

        WritableImage out = cropView.snapshot(params, null);
        try {
            // flatten onto white and encode as JPEG so stored photos stay small
            BufferedImage argb = SwingFXUtils.fromFXImage(out, null);
            BufferedImage rgb = new BufferedImage(argb.getWidth(), argb.getHeight(), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = rgb.createGraphics();
            g.drawImage(argb, 0, 0, java.awt.Color.WHITE, null);
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(rgb, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            return fallback;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

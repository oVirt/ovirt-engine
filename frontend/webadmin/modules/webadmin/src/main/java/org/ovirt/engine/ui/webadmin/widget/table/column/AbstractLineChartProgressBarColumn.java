package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.UriUtils;

/**
 * An implementation of the SafeHtmlColumn which renders a list of integers as a line chart to a HTML5 canvas. If the
 * browser does not support the HTML5 canvas, it falls back to PercentColumn rendering the last value only.
 *
 * The widget's specific behavior (in case the browser supports HTML5 canvas):
 * - the widget draws a line chart ending with a green triangle (if the last value is smaller than 70), orange square (if smaller than 95) and a red dot (if bigger than 95)
 * - next to the chart is a the last value of the chart written in form of N% (e.g. 75%). If the value is >= 95, it is red and bold
 * - if the num of progress values is smaller than the amount fitting into the drawing area but does not start with 0, the values are prepended by '0' e.g. the chart will start from 0
 * - if the num of progress values is bigger than the amount fitting into the drawing area, beginning of the values are cut
 * - the distance between two points has to be between the stepMin and stepMax (calculated according to the width of the drawing area and num of of points in the chart) -
 *   e.g. the bigger the drawing area the more points can fit in
 * - if no points fit into the drawing area, only the number is shown
 *
 * @param <T> The entity type
 */
public abstract class AbstractLineChartProgressBarColumn<T> extends AbstractSafeHtmlColumn<T> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    private static final int stepMax = 15;

    private static final int stepMin = 4;

    private static final int leftMargin = 2;

    private static final int bottomPadding = 2;

    private static final int lineEndShapeSize = 6;

    private static final int lineWidth = 2;

    private static final int textWidth = 40;

    private static final int rightMargin = textWidth + lineEndShapeSize;

    private static final int canvasHeight = 20;

    public static final String chartLineColor = "#629766"; //$NON-NLS-1$

    public static final String lineEndTriangleColor = "#629766"; //$NON-NLS-1$

    public static final String lineEndSquareColor = "#ff8000"; //$NON-NLS-1$

    public static final String lineEndDotColor = "#ff0000"; //$NON-NLS-1$

    public static final String textColorNormal = "inherit"; //$NON-NLS-1$

    public static final String textColorRed = "#ff0000"; //$NON-NLS-1$

    private FallbackProgressColumn fallbackProgressColumn;

    private static final Canvas canvas = Canvas.createIfSupported();

    @Override
    public SafeHtml getValue(T object) {
        if (canvas == null) {
            // lazily - normally the browser should support it
            if (fallbackProgressColumn == null) {
                fallbackProgressColumn = new FallbackProgressColumn();
            }

            return fallbackProgressColumn.getValue(object);
        }

        List<Integer> progressValues = getProgressValues(object);
        FontWeight fontWeight = FontWeight.NORMAL;
        String textColor = textColorNormal;
        if (getLastPoint(progressValues) >= 95) {
            textColor = textColorRed;
            fontWeight = FontWeight.BOLD;
        }

        int chartWidth = getChartWidth();

        SafeStylesBuilder styleBuilder = new SafeStylesBuilder();
        styleBuilder.width(textWidth, Unit.PX);
        styleBuilder.trustedColor(textColor);
        styleBuilder.fontWeight(fontWeight);

        if (chartWidth > 0 && progressValues != null && progressValues.size() > 0) {

            List<Integer> normalizedPoints = normalizePoints(progressValues, chartWidth);
            if (normalizedPoints.size() > 0) {
                // at least some part of the chart fits in, lets draw it
                canvas.setCoordinateSpaceWidth(chartWidth + lineEndShapeSize + leftMargin);
                canvas.setCoordinateSpaceHeight(canvasHeight);
                canvas.setWidth((chartWidth + lineEndShapeSize + leftMargin) + "px"); //$NON-NLS-1$
                canvas.setHeight(canvasHeight + "px"); //$NON-NLS-1$

                drawChart(canvas, normalizedPoints);

                String dataUrl = canvas.toDataUrl();

                return templates.lineChart(
                        UriUtils.fromTrustedString(dataUrl),
                        styleBuilder.toSafeStyles(),
                        getLastPoint(progressValues)
                );
            }

        }

        // if the chart does not fit in, show at least the text
        return templates.lineChartWithoutImage(styleBuilder.toSafeStyles());
    }

    private int getChartWidth() {
        String actualWidth = getActualWidth();

        if (actualWidth.indexOf("px") == -1) { //$NON-NLS-1$
            // this is a developer mistake - fail with an exception to simplify debugging
            throw new IllegalArgumentException("The size is expected to be in PX in a format: 100px but it was: '"  + actualWidth + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        String withoutPx = actualWidth.substring(0, actualWidth.indexOf("px")); //$NON-NLS-1$
        int intSize = Integer.parseInt(withoutPx);
        int realSize = intSize - rightMargin - leftMargin;
        if (realSize < 0) {
            return 0;
        }

        return realSize;
    }

    private int getLastPoint(List<Integer> points) {
        if (points == null || points.size() == 0) {
            return 0;
        }

        return points.get(points.size() - 1);
    }

    private void drawChart(Canvas canvas, List<Integer> normalizedPoints) {
        Context2d context2d = canvas.getContext2d();

        context2d.clearRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());

        context2d.beginPath();

        int actualX = leftMargin;
        int stepSize = calculateStepSize(normalizedPoints, getChartWidth());

        int lastX = leftMargin;
        int lastY = calculateHeight(normalizedPoints.get(0), canvas.getCoordinateSpaceHeight());
        int lastPoint = normalizedPoints.get(0);

        context2d.moveTo(lastX, lastY);

        for (int point : normalizedPoints.subList(1, normalizedPoints.size())) {
            actualX += stepSize;
            lastX = actualX;
            lastY = calculateHeight(point, canvas.getCoordinateSpaceHeight());
            lastPoint = point;

            context2d.lineTo(lastX, lastY);
        }

        context2d.setLineWidth(lineWidth);
        context2d.setStrokeStyle(chartLineColor); //$NON-NLS-1$
        context2d.stroke();

        drawLineDecoration(context2d, lastX, lastY, lastPoint);
    }

    private void drawLineDecoration(Context2d context2d, int x, int y, int point) {
        context2d.beginPath();

        int yOfLineCenter = y + (lineWidth / 2);
        int yOfLineBottom = y + lineWidth;

        if (point < 70) {
            // green triangle

            context2d.moveTo(x - lineEndShapeSize / 2, yOfLineBottom);
            context2d.lineTo(x + lineEndShapeSize / 2, yOfLineBottom);
            context2d.lineTo(x, yOfLineBottom - lineEndShapeSize);
            context2d.lineTo(x - lineEndShapeSize / 2, yOfLineBottom);
            context2d.setFillStyle(lineEndTriangleColor);
        } else if (point < 95) {
            // orange square
            context2d.rect(x, yOfLineCenter - lineEndShapeSize / 2, lineEndShapeSize, lineEndShapeSize);
            context2d.setFillStyle(lineEndSquareColor);
        } else {
            // red dot
            context2d.arc(x + lineEndShapeSize / 2, yOfLineCenter, lineEndShapeSize / 2, 0, 2 * Math.PI, false);
            context2d.setFillStyle(lineEndDotColor);
        }

        context2d.fill();
    }

    private int calculateHeight(int point, int drawingAreaHeight) {
        // add a padding so the line decorations will fit in
        int topPadding = lineEndShapeSize / 2;
        // "drawingAreaHeight - " is there because the canvas coordinates start at top left corner and the chart at bottom left
        return drawingAreaHeight - (point * (drawingAreaHeight - bottomPadding - topPadding) / 100 + bottomPadding);
    }

    private List<Integer> normalizePoints(List<Integer> original, int drawingAreaWidth) {
        if (drawingAreaWidth <= 0) {
            return Collections.emptyList();
        }

        if (drawingAreaWidth / original.size() < stepMin) {
            // too many points in the list - cut to fit into the drawing area
            int correctSize = drawingAreaWidth / stepMin;

            // if it still does not fit in, retuen an empty list
            if (correctSize <= 0) {
                return Collections.emptyList();
            }
            return original.subList(original.size() - correctSize, original.size());
        }

        if (drawingAreaWidth / original.size() > stepMax) {
            // too few points - try adding a 0 so it will not be just a line in the air but will start somewhere
            if (original.size() > 1 && original.get(0) == 0) {
                // already starts on 0 and has some points, return it
                return original;
            }

            List<Integer> res = new ArrayList<>();
            res.add(0);
            res.addAll(original);

            if (drawingAreaWidth / res.size() < stepMin) {
                // it does not fit in after adding a 0, fall back to the original
                return original;
            }

            return res;
        }

        return original;
    }

    /**
     * Expects that the points will be not null and not empty
     */
    private int calculateStepSize(List<Integer> points, int drawingAreaWidth) {
        if (drawingAreaWidth / points.size() > stepMax) {
            return stepMax;
        }

        return drawingAreaWidth / points.size();
    }

    class FallbackProgressColumn extends AbstractPercentColumn<T> {

        @Override
        protected Integer getProgressValue(T object) {
            return getLastPoint(getProgressValues(object));
        }
    }

    /**
     * List of values to draw the line chart from
     */
    protected abstract List<Integer> getProgressValues(T object);

    /**
     * Return the size this widget can occupy (line chart + number at the end) in a form of Npx (e.g. 200px)
     *
     * @return the size in form Npx (e.g. 200px)
     */
    protected abstract String getActualWidth();
}

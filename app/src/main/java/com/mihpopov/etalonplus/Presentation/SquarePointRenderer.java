package com.mihpopov.etalonplus.Presentation;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.github.mikephil.charting.utils.Transformer;

/**
 * Кастомный рендерер для отображения точек на графике выполнения заданий в виде квадратов.
 */
public class SquarePointRenderer extends LineChartRenderer {
    private final Paint mSquarePaint;
    private float mSquareSize = 16f;

    public SquarePointRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
        mSquarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSquarePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void drawCircles(Canvas c) {
        for (ILineDataSet dataSet : mChart.getLineData().getDataSets()) {
            if (!dataSet.getLabel().equals("Процент выполнения заданий")) continue;
            drawSquareCircles(c, dataSet);
        }
    }

    private void drawSquareCircles(Canvas c, ILineDataSet dataSet) {
        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
        float phaseY = mAnimator.getPhaseY();
        mXBounds.set(mChart, dataSet);
        for (int i = mXBounds.min; i <= mXBounds.max; i++) {
            Entry e = dataSet.getEntryForIndex(i);
            if (e == null) continue;
            float[] pts = new float[2];
            pts[0] = e.getX();
            pts[1] = e.getY() * phaseY;
            trans.pointValuesToPixel(pts);
            if (!mViewPortHandler.isInBoundsRight(pts[0])) break;
            if (!mViewPortHandler.isInBoundsLeft(pts[0]) || !mViewPortHandler.isInBoundsY(pts[1])) continue;
            mSquarePaint.setColor(dataSet.getColor());
            float half = mSquareSize / 2f;
            c.drawRect(pts[0] - half, pts[1] - half, pts[0] + half, pts[1] + half, mSquarePaint);
        }
    }

    public void setSquareSize(float size) {
        this.mSquareSize = size;
    }
}
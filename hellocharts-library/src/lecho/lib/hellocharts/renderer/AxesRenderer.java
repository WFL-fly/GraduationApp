package lecho.lib.hellocharts.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

import java.util.logging.Logger;
import lecho.lib.hellocharts.computator.ChartComputator;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.AxisAutoValues;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.util.FloatUtils;
import lecho.lib.hellocharts.view.Chart;

/**
 * Default axes renderer. Can draw maximum four axes - two horizontal(top/bottom) and two vertical(left/right).
 */
public class AxesRenderer {
    //private Logger log=Logger.getLogger("AxesRenderer");//= LogFactory.getLog(Mysql.class);

    private static final int DEFAULT_AXIS_MARGIN_DP = 1;

    /**
     * Axis positions indexes, used for indexing tabs that holds axes parameters, see below.
     */
    private static final int TOP = 0;
    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int BOTTOM = 3;

    /**
     * Used to measure label width. If label has mas 5 characters only 5 first characters of this array are used to
     * measure text width.
     */
    private static final char[] labelWidthChars = new char[]{
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};

    private Chart chart;
    private ChartComputator computator;
    private int axisMargin;
    private float density;
    private float scaledDensity;//系统指定 勿该
    private Paint[] labelPaintTab = new Paint[]{new Paint(), new Paint(), new Paint(), new Paint()};
    private Paint[] namePaintTab = new Paint[]{new Paint(), new Paint(), new Paint(), new Paint()};
    private Paint[] linePaintTab = new Paint[]{new Paint(), new Paint(), new Paint(), new Paint()};
    private float[] nameBaselineTab = new float[4];
    private float[] labelBaselineTab = new float[4];
    private float[] separationLineTab = new float[4];
    private int[] labelWidthTab = new int[4];//控制间隔线数目因素之一
    private int[] labelTextAscentTab = new int[4];//控制间隔线数目因素之2
    private int[] labelTextDescentTab = new int[4];
    private int[] labelDimensionForMarginsTab = new int[4];
    private int[] labelDimensionForStepsTab = new int[4];
    private int[] tiltedLabelXTranslation = new int[4];
    private int[] tiltedLabelYTranslation = new int[4];
    private FontMetricsInt[] fontMetricsTab = new FontMetricsInt[]{new FontMetricsInt(), new FontMetricsInt(),
            new FontMetricsInt(), new FontMetricsInt()};//字体基线的数据
    /**
     * Holds formatted axis value label.
     */
    private char[] labelBuffer = new char[64];

    /**
     * Holds number of values that should be drown for each axis.
     */
    private int[] valuesToDrawNumTab = new int[4];

    /**
     * Holds raw values to draw for each axis.
     */
    private float[][] rawValuesTab = new float[4][0];

    /**
     * Holds auto-generated values that should be drawn, i.e if axis is inside not all auto-generated values should be
     * drawn to avoid overdrawing. Used only for auto axes.
     */
    private float[][] autoValuesToDrawTab = new float[4][0];

    /**
     * Holds custom values that should be drawn, used only for custom axes.
     */
    private AxisValue[][] valuesToDrawTab = new AxisValue[4][0];

    /**
     * Buffers for axes lines coordinates(to draw grid in the background).
     */
    private float[][] linesDrawBufferTab = new float[4][0];

    /**
     * Buffers for auto-generated values for each axis, used only if there are auto axes.
     */
    private AxisAutoValues[] autoValuesBufferTab = new AxisAutoValues[]{new AxisAutoValues(),
            new AxisAutoValues(), new AxisAutoValues(), new AxisAutoValues()};

    public AxesRenderer(Context context, Chart chart) {
        this.chart = chart;
        computator = chart.getChartComputator();
        density = context.getResources().getDisplayMetrics().density;
        scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        axisMargin = ChartUtils.dp2px(density, DEFAULT_AXIS_MARGIN_DP);
        for (int position = 0; position < 4; ++position) {
            labelPaintTab[position].setStyle(Paint.Style.FILL);
            labelPaintTab[position].setAntiAlias(true);
            namePaintTab[position].setStyle(Paint.Style.FILL);
            namePaintTab[position].setAntiAlias(true);
            linePaintTab[position].setStyle(Paint.Style.STROKE);
            linePaintTab[position].setAntiAlias(true);
        }
    }

    public void onChartSizeChanged() {
        onChartDataOrSizeChanged();
    }

    public void onChartDataChanged() {
        onChartDataOrSizeChanged();
    }

    private void onChartDataOrSizeChanged() {
        initAxis(chart.getChartData().getAxisXTop(), TOP);
        initAxis(chart.getChartData().getAxisXBottom(), BOTTOM);
        initAxis(chart.getChartData().getAxisYLeft(), LEFT);
        initAxis(chart.getChartData().getAxisYRight(), RIGHT);
    }

    public void resetRenderer() {
        this.computator = chart.getChartComputator();
    }

    /**
     * Initialize attributes and measurement for axes(left, right, top, bottom);
     */
    private void initAxis(Axis axis, int position) {
        if (null == axis) {
            return;
        }
        initAxisAttributes(axis, position);//设置属性
        initAxisMargin(axis, position);//设置空白 边缘
        initAxisMeasurements(axis, position);//设置尺寸
    }
    //设置属性
    private void initAxisAttributes(Axis axis, int position) {
        initAxisPaints(axis, position);//画笔
        initAxisTextAlignment(axis, position);//设置文本对齐
        if (axis.hasTiltedLabels()) //是否倾斜显示
        {
            initAxisDimensionForTiltedLabels(position);
            intiTiltedLabelsTranslation(axis, position);
        }
        else
        {
            initAxisDimension(position);
        }
    }
    //初始化画笔
    private void initAxisPaints(Axis axis, int position) {
        Typeface typeface = axis.getTypeface();
        if (null != typeface) {
            labelPaintTab[position].setTypeface(typeface);
            namePaintTab[position].setTypeface(typeface);
        }
        labelPaintTab[position].setColor(axis.getTextColor());
        labelPaintTab[position].setTextSize(ChartUtils.sp2px(scaledDensity, axis.getTextSize()));//设置画笔文字大小
        labelPaintTab[position].getFontMetricsInt(fontMetricsTab[position]);//从Paint 初始化fontMetricsTab
        namePaintTab[position].setColor(axis.getTextColor());
        namePaintTab[position].setTextSize(ChartUtils.sp2px(scaledDensity, axis.getTextSize()));
        linePaintTab[position].setColor(axis.getLineColor());

        labelTextAscentTab[position] = Math.abs(fontMetricsTab[position].ascent);//eee
        labelTextDescentTab[position] = Math.abs(fontMetricsTab[position].descent);
        labelWidthTab[position] = (int) labelPaintTab[position].measureText(labelWidthChars, 0,
                axis.getMaxLabelChars());//绘制文本占用长度 设置 axis.getMaxLabelChars() 显示的文本字符最大长度
    }
    //初始化文本对齐
    private void initAxisTextAlignment(Axis axis, int position) {
        namePaintTab[position].setTextAlign(Align.CENTER);
        if (TOP == position || BOTTOM == position) {
            labelPaintTab[position].setTextAlign(Align.CENTER);
        } else if (LEFT == position) {
            if (axis.isInside()) {
                labelPaintTab[position].setTextAlign(Align.LEFT);
            } else {
                labelPaintTab[position].setTextAlign(Align.RIGHT);
            }
        } else if (RIGHT == position) {
            if (axis.isInside()) {
                labelPaintTab[position].setTextAlign(Align.RIGHT);
            } else {
                labelPaintTab[position].setTextAlign(Align.LEFT);
            }
        }
    }
    //初始化 step 决定显示多少条间隔线
    private void initAxisDimensionForTiltedLabels(int position)
    {
        //int pythagoreanFromLabelWidth = (int) Math.sqrt(Math.pow(labelWidthTab[position], 2) / 2);
       // int pythagoreanFromAscent = (int) Math.sqrt(Math.pow(labelTextAscentTab[position], 2) / 2);
        int pythagoreanFromLabelWidth = (int) Math.sqrt(labelWidthTab[position]/2);
        int pythagoreanFromAscent = (int) Math.sqrt(labelTextAscentTab[position]/2);
        labelDimensionForMarginsTab[position] = pythagoreanFromAscent + pythagoreanFromLabelWidth;
        //log.info("修改此处");
        labelDimensionForStepsTab[position] = Math.round(labelDimensionForMarginsTab[position] * 0.05f);//修改 0.75f
        //labelDimensionForStepsTab[position] = labelDimensionForMarginsTab[position]+2;
    }
    //初始化尺寸 un
    private void initAxisDimension(int position) {
        if (LEFT == position || RIGHT == position)
        {
            labelDimensionForMarginsTab[position] = labelWidthTab[position];//文本长度
            labelDimensionForStepsTab[position] = labelTextAscentTab[position];//
        }
        else if (TOP == position || BOTTOM == position)
        {
            labelDimensionForMarginsTab[position] = labelTextAscentTab[position] + labelTextDescentTab[position];
            labelDimensionForStepsTab[position] = labelWidthTab[position];
        }
    }

    private void intiTiltedLabelsTranslation(Axis axis, int position) {
        int pythagoreanFromLabelWidth = (int) Math.sqrt(Math.pow(labelWidthTab[position], 2) / 2);
        int pythagoreanFromAscent = (int) Math.sqrt(Math.pow(labelTextAscentTab[position], 2) / 2);
        int dx = 0;
        int dy = 0;
        if (axis.isInside())
        {
            if (LEFT == position) {
                dx = pythagoreanFromAscent;
            } else if (RIGHT == position) {
                dy = -pythagoreanFromLabelWidth / 2;
            } else if (TOP == position) {
                dy = (pythagoreanFromAscent + pythagoreanFromLabelWidth / 2) - labelTextAscentTab[position];
            } else if (BOTTOM == position) {
                dy = -pythagoreanFromLabelWidth / 2;
            }
        }
        else
        {
            if (LEFT == position) {
                dy = -pythagoreanFromLabelWidth / 2;
            } else if (RIGHT == position) {
                dx = pythagoreanFromAscent;
            } else if (TOP == position) {
                dy = -pythagoreanFromLabelWidth / 2;
            } else if (BOTTOM == position) {
                dy = (pythagoreanFromAscent + pythagoreanFromLabelWidth / 2) - labelTextAscentTab[position];
            }
        }
        tiltedLabelXTranslation[position] = dx;
        tiltedLabelYTranslation[position] = dy;
    }

    private void initAxisMargin(Axis axis, int position) {
        int margin = 0;
        if (!axis.isInside() && (axis.isAutoGenerated() || !axis.getValues().isEmpty())) {
            margin += axisMargin + labelDimensionForMarginsTab[position];
        }
        margin += getAxisNameMargin(axis, position);
        insetContentRectWithAxesMargins(margin, position);
    }

    private int getAxisNameMargin(Axis axis, int position) {
        int margin = 0;
        if (!TextUtils.isEmpty(axis.getName())) {
            margin += labelTextAscentTab[position];
            margin += labelTextDescentTab[position];
            margin += axisMargin;
        }
        return margin;
    }

    private void insetContentRectWithAxesMargins(int axisMargin, int position) {
        if (LEFT == position) {
            chart.getChartComputator().insetContentRect(axisMargin, 0, 0, 0);
        } else if (RIGHT == position) {
            chart.getChartComputator().insetContentRect(0, 0, axisMargin, 0);
        } else if (TOP == position) {
            chart.getChartComputator().insetContentRect(0, axisMargin, 0, 0);
        } else if (BOTTOM == position) {
            chart.getChartComputator().insetContentRect(0, 0, 0, axisMargin);
        }
    }
    //设置尺寸
    private void initAxisMeasurements(Axis axis, int position) {
        if (LEFT == position) {
            if (axis.isInside()) {
                labelBaselineTab[position] = computator.getContentRectMinusAllMargins().left + axisMargin;
                nameBaselineTab[position] = computator.getContentRectMinusAxesMargins().left - axisMargin
                        - labelTextDescentTab[position];
            } else {
                labelBaselineTab[position] = computator.getContentRectMinusAxesMargins().left - axisMargin;
                nameBaselineTab[position] = labelBaselineTab[position] - axisMargin
                        - labelTextDescentTab[position] - labelDimensionForMarginsTab[position];
            }
            separationLineTab[position] = computator.getContentRectMinusAllMargins().left;
        } else if (RIGHT == position) {
            if (axis.isInside()) {
                labelBaselineTab[position] = computator.getContentRectMinusAllMargins().right - axisMargin;
                nameBaselineTab[position] = computator.getContentRectMinusAxesMargins().right + axisMargin
                        + labelTextAscentTab[position];
            } else {
                labelBaselineTab[position] = computator.getContentRectMinusAxesMargins().right + axisMargin;
                nameBaselineTab[position] = labelBaselineTab[position] + axisMargin
                        + labelTextAscentTab[position] + labelDimensionForMarginsTab[position];
            }
            separationLineTab[position] = computator.getContentRectMinusAllMargins().right;
        } else if (BOTTOM == position)
        {
            if (axis.isInside())
            {
                labelBaselineTab[position] = computator.getContentRectMinusAllMargins().bottom- labelTextDescentTab[position];
                //labelBaselineTab[position] = computator.getContentRectMinusAxesMargins().bottom+ axisMargin;//fly 修改- axisMargin - labelTextDescentTab[position]
                nameBaselineTab[position] = computator.getContentRectMinusAxesMargins().bottom + axisMargin + labelTextAscentTab[position];
            }
            else
            {
                labelBaselineTab[position] = computator.getContentRectMinusAxesMargins().bottom + axisMargin + labelTextAscentTab[position];
                nameBaselineTab[position] = labelBaselineTab[position] + axisMargin + labelDimensionForMarginsTab[position];
            }
            separationLineTab[position] = computator.getContentRectMinusAllMargins().bottom;
        } else if (TOP == position) {
            if (axis.isInside()) {
                labelBaselineTab[position] = computator.getContentRectMinusAllMargins().top + axisMargin
                        + labelTextAscentTab[position];
                nameBaselineTab[position] = computator.getContentRectMinusAxesMargins().top - axisMargin
                        - labelTextDescentTab[position];
            } else {
                labelBaselineTab[position] = computator.getContentRectMinusAxesMargins().top - axisMargin
                        - labelTextDescentTab[position];
                nameBaselineTab[position] = labelBaselineTab[position] - axisMargin -
                        labelDimensionForMarginsTab[position];
            }
            separationLineTab[position] = computator.getContentRectMinusAllMargins().top;
        } else {
            throw new IllegalArgumentException("Invalid axis position: " + position);
        }
    }

    /**
     * Prepare axes coordinates and draw axes lines(if enabled) in the background.
     *
     * @param canvas
     */
    //绘制AXIS坐标轴
    public void drawInBackground(Canvas canvas) {
        Axis axis = chart.getChartData().getAxisYLeft();
        if (null != axis) {
            prepareAxisToDraw(axis, LEFT);
            drawAxisLines(canvas, axis, LEFT);
        }

        axis = chart.getChartData().getAxisYRight();
        if (null != axis) {
            prepareAxisToDraw(axis, RIGHT);
            drawAxisLines(canvas, axis, RIGHT);
        }

        axis = chart.getChartData().getAxisXBottom();
        if (null != axis) {
            prepareAxisToDraw(axis, BOTTOM);
            drawAxisLines(canvas, axis, BOTTOM);
        }

        axis = chart.getChartData().getAxisXTop();
        if (null != axis) {
            prepareAxisToDraw(axis, TOP);
            drawAxisLines(canvas, axis, TOP);
        }
    }
    //绘制坐标轴之前的准备
    private void prepareAxisToDraw(Axis axis, int position) {

        if (axis.isAutoGenerated())//是否自动处理坐标
        {
            Log.i("test","prepareAutoGeneratedAxis");
            prepareAutoGeneratedAxis(axis, position);
        }
        else
        {
            prepareCustomAxis(axis, position);
        }
    }

    /**
     * Draw axes labels and names in the foreground.
     *
     * @param canvas
     */
    public void drawInForeground(Canvas canvas) {
        Axis axis = chart.getChartData().getAxisYLeft();
        if (null != axis) {
            drawAxisLabelsAndName(canvas, axis, LEFT);
        }

        axis = chart.getChartData().getAxisYRight();
        if (null != axis) {
            drawAxisLabelsAndName(canvas, axis, RIGHT);
        }

        axis = chart.getChartData().getAxisXBottom();
        if (null != axis) {
            drawAxisLabelsAndName(canvas, axis, BOTTOM);
        }

        axis = chart.getChartData().getAxisXTop();
        if (null != axis) {
            drawAxisLabelsAndName(canvas, axis, TOP);
        }
    }

    private int needShowLabelNumber(Axis axis, float viewportMin,float viewportMax)
    {
        float value;
        int num=0;
        for (AxisValue axisValue : axis.getValues()) {
            value = axisValue.getValue();
            if(value>=viewportMin&&value<=viewportMax)
            {
                num++;
            }
        }
        return num;
    }

    private void prepareCustomAxis(Axis axis, int position) {
    final Viewport maxViewport = computator.getMaximumViewport();
    final Viewport visibleViewport = computator.getVisibleViewport();
    final Rect contentRect = computator.getContentRectMinusAllMargins();
    boolean isAxisVertical = isAxisVertical(position);
    float viewportMin, viewportMax;
    float scale = 1;
    if (isAxisVertical) {
        if (maxViewport.height() > 0 && visibleViewport.height() > 0) {
            scale = contentRect.height() * (maxViewport.height() / visibleViewport.height());
        }
        viewportMin = visibleViewport.bottom;
        viewportMax = visibleViewport.top;
    } else {
        if (maxViewport.width() > 0 && visibleViewport.width() > 0) {
            scale = contentRect.width() * (maxViewport.width() / visibleViewport.width());
        }
        viewportMin = visibleViewport.left;
        viewportMax = visibleViewport.right;
    }
    if (scale == 0) {
        scale = 1;
    }
    int needShowLabelNumber=needShowLabelNumber(axis,viewportMin,viewportMax);
    //int module = (int) Math.max(1, Math.ceil((axis.getValues().size() * labelDimensionForStepsTab[position]) / scale));
    int module = (int) Math.max(1, Math.ceil((needShowLabelNumber* labelDimensionForStepsTab[position]) / scale));

    //Reinitialize tab to hold lines coordinates.
    if (axis.hasLines() && (linesDrawBufferTab[position].length < axis.getValues().size() * 4)) {
        linesDrawBufferTab[position] = new float[axis.getValues().size() * 4];
    }
    //Reinitialize tabs to hold all raw values to draw.
    if (rawValuesTab[position].length < axis.getValues().size()) {
        rawValuesTab[position] = new float[axis.getValues().size()];
    }
    //Reinitialize tabs to hold all raw values to draw.
    if (valuesToDrawTab[position].length < axis.getValues().size()) {
        valuesToDrawTab[position] = new AxisValue[axis.getValues().size()];
    }

    float rawValue;
    int valueIndex = 0;
    int valueToDrawIndex = 0;
    for (AxisValue axisValue : axis.getValues()) {
        // Draw axis values that are within visible viewport.
        final float value = axisValue.getValue();
        if (value >= viewportMin && value <= viewportMax) {
            // Draw axis values that have 0 module value, this will hide some labels if there is no place for them.
            if (0 == valueIndex % module) {
                if (isAxisVertical) {
                    rawValue = computator.computeRawY(value);
                } else {
                    rawValue = computator.computeRawX(value);
                }
                if (checkRawValue(contentRect, rawValue, axis.isInside(), position, isAxisVertical)) {

                    rawValuesTab[position][valueToDrawIndex] = rawValue;
                    valuesToDrawTab[position][valueToDrawIndex] = axisValue;
                    ++valueToDrawIndex;
                }
            }
            // If within viewport - increment valueIndex;
            ++valueIndex;
        }
    }
    valuesToDrawNumTab[position] = valueToDrawIndex;
}

    private void prepareAutoGeneratedAxis(Axis axis, int position) {
        final Viewport visibleViewport = computator.getVisibleViewport();
        final Rect contentRect = computator.getContentRectMinusAllMargins();//矩形框
        boolean isAxisVertical = isAxisVertical(position);//左右为true
        float start, stop;
        int contentRectDimension;
        if (isAxisVertical)
        {
            start = visibleViewport.bottom;//起点
            stop = visibleViewport.top;
            contentRectDimension = contentRect.height();
        }
        else
        {
            start = visibleViewport.left;
            stop = visibleViewport.right;
            contentRectDimension = contentRect.width();
        }
        FloatUtils.computeAutoGeneratedAxisValues(start, stop, contentRectDimension /
                labelDimensionForStepsTab[position] / 2, autoValuesBufferTab[position]);//准备要显示的数据 存放于autoValuesBufferTab[position] AxisAutoValues
        //Reinitialize tab to hold lines coordinates.
        if (axis.hasLines() && (linesDrawBufferTab[position].length < autoValuesBufferTab[position].valuesNumber * 4))//是否显示分割线
        {
            linesDrawBufferTab[position] = new float[autoValuesBufferTab[position].valuesNumber * 4];
        }
        //Reinitialize tabs to hold all raw and auto values.
        if (rawValuesTab[position].length < autoValuesBufferTab[position].valuesNumber) {
            rawValuesTab[position] = new float[autoValuesBufferTab[position].valuesNumber];
        }
        if (autoValuesToDrawTab[position].length < autoValuesBufferTab[position].valuesNumber) {
            autoValuesToDrawTab[position] = new float[autoValuesBufferTab[position].valuesNumber];
        }

        float rawValue;
        int valueToDrawIndex = 0;
        for (int i = 0; i < autoValuesBufferTab[position].valuesNumber; ++i)
        {
            if (isAxisVertical)//水平
            {
                rawValue = computator.computeRawY(autoValuesBufferTab[position].values[i]);
            }
            else {
                rawValue = computator.computeRawX(autoValuesBufferTab[position].values[i]);
            }
            if (checkRawValue(contentRect, rawValue, axis.isInside(), position, isAxisVertical))
            {
                rawValuesTab[position][valueToDrawIndex] = rawValue;
                autoValuesToDrawTab[position][valueToDrawIndex] = autoValuesBufferTab[position].values[i];
                ++valueToDrawIndex;
            }
        }
        valuesToDrawNumTab[position] = valueToDrawIndex;
    }

    private boolean checkRawValue(Rect rect, float rawValue, boolean axisInside, int position, boolean isVertical) {
        if (axisInside)
        {
            if (isVertical)
            {
                float marginBottom = labelTextAscentTab[BOTTOM] + axisMargin;
                float marginTop = labelTextAscentTab[TOP] + axisMargin;
                if (rawValue <= rect.bottom - marginBottom && rawValue >= rect.top + marginTop) {
                    return true;
                } else {
                    return false;
                }
            }
            else
            {
                //float margin = labelWidthTab[position] / 2;
                float margin =labelDimensionForStepsTab[position] / 2;//
                //
                if (rawValue >= rect.left + margin && rawValue <= rect.right - margin) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    //画线
    private void drawAxisLines(Canvas canvas, Axis axis, int position) {
        final Rect contentRectMargins = computator.getContentRectMinusAxesMargins();
        float separationX1, separationY1, separationX2, separationY2;
        separationX1 = separationY1 = separationX2 = separationY2 = 0;
        float lineX1, lineY1, lineX2, lineY2;
        lineX1 = lineY1 = lineX2 = lineY2 = 0;
        boolean isAxisVertical = isAxisVertical(position);
        if (LEFT == position || RIGHT == position) {
            separationX1 = separationX2 = separationLineTab[position];
            separationY1 = contentRectMargins.bottom;
            separationY2 = contentRectMargins.top;
            lineX1 = contentRectMargins.left;
            lineX2 = contentRectMargins.right;
        } else if (TOP == position || BOTTOM == position) {
            separationX1 = contentRectMargins.left;
            separationX2 = contentRectMargins.right;
            separationY1 = separationY2 = separationLineTab[position];
            lineY1 = contentRectMargins.top;
            lineY2 = contentRectMargins.bottom;
        }
        // Draw separation line with the same color as axis labels and name.
        //坐标线
        if (axis.hasSeparationLine()) {
            canvas.drawLine(separationX1, separationY1, separationX2, separationY2, labelPaintTab[position]);
        }

        if (axis.hasLines()) {
            int valueToDrawIndex = 0;
            for (; valueToDrawIndex < valuesToDrawNumTab[position]; ++valueToDrawIndex)
            {
                if (isAxisVertical)
                {
                    lineY1 = lineY2 = rawValuesTab[position][valueToDrawIndex];
                } else {
                    lineX1 = lineX2 = rawValuesTab[position][valueToDrawIndex];
                }
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 0] = lineX1;
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 1] = lineY1;
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 2] = lineX2;
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 3] = lineY2;
            }
            canvas.drawLines(linesDrawBufferTab[position], 0, valueToDrawIndex * 4, linePaintTab[position]);
        }
    }

    private void drawAxisLabelsAndName(Canvas canvas, Axis axis, int position) {
        float labelX, labelY;
        labelX = labelY = 0;
        boolean isAxisVertical = isAxisVertical(position);
        if (LEFT == position || RIGHT == position) {
            labelX = labelBaselineTab[position];
        } else if (TOP == position || BOTTOM == position) {
            labelY = labelBaselineTab[position];
        }

        for (int valueToDrawIndex = 0; valueToDrawIndex < valuesToDrawNumTab[position]; ++valueToDrawIndex)
        {
            int charsNumber = 0;
            if (axis.isAutoGenerated()) {
                final float value = autoValuesToDrawTab[position][valueToDrawIndex];
                charsNumber = axis.getFormatter().formatValueForAutoGeneratedAxis(labelBuffer, value,
                        autoValuesBufferTab[position].decimals);
            } else {
                AxisValue axisValue = valuesToDrawTab[position][valueToDrawIndex];
                charsNumber = axis.getFormatter().formatValueForManualAxis(labelBuffer, axisValue);
            }

            if (isAxisVertical) {
                labelY = rawValuesTab[position][valueToDrawIndex];
            } else {
                labelX = rawValuesTab[position][valueToDrawIndex];
            }

            if (axis.hasTiltedLabels()) {
                canvas.save();
                //canvas.translate(tiltedLabelXTranslation[position], tiltedLabelYTranslation[position]);
                canvas.rotate(-45, labelX, labelY);
                canvas.drawText(labelBuffer, labelBuffer.length - charsNumber, charsNumber, labelX, labelY,
                        labelPaintTab[position]);
                canvas.restore();
            } else {
                canvas.drawText(labelBuffer, labelBuffer.length - charsNumber, charsNumber, labelX, labelY,
                        labelPaintTab[position]);
            }
        }

        // Drawing axis Label name
        final Rect contentRectMargins = computator.getContentRectMinusAxesMargins();
        if (!TextUtils.isEmpty(axis.getName())) {
            if (isAxisVertical) {
                canvas.save();
                canvas.rotate(-90, contentRectMargins.centerY(), contentRectMargins.centerY());
                canvas.drawText(axis.getName(), contentRectMargins.centerY(), nameBaselineTab[position],
                        namePaintTab[position]);
                canvas.restore();
            } else {
                canvas.drawText(axis.getName(), contentRectMargins.centerX(), nameBaselineTab[position],
                        namePaintTab[position]);
            }
        }
    }

    private boolean isAxisVertical(int position) {
        if (LEFT == position || RIGHT == position) {
            return true;
        } else if (TOP == position || BOTTOM == position) {
            return false;
        } else {
            throw new IllegalArgumentException("Invalid axis position " + position);
        }
    }
/*
    private void prepareCustomAxis(Axis axis, int position) {
        final Viewport maxViewport = computator.getMaximumViewport();//m_lineChartView.setMaximumViewport(maxV);
        final Viewport visibleViewport = computator.getVisibleViewport();//m_lineChartView.setCurrentViewport(v);
        final Rect contentRect = computator.getContentRectMinusAllMargins();
        boolean isAxisVertical = isAxisVertical(position);
        float viewportMin, viewportMax;
        float Alldp;
        float scale = 1;
        String TAG="yy";
        if (isAxisVertical)
        {
            if (maxViewport.height() > 0 && visibleViewport.height() > 0)
            {
                scale = contentRect.height() * (maxViewport.height() / visibleViewport.height());
            }
            Alldp=contentRect.height();
            viewportMin = visibleViewport.bottom;
            viewportMax = visibleViewport.top;
        }
        else
        {
            if (maxViewport.width() > 0 && visibleViewport.width() > 0) {
                scale = contentRect.width() * (maxViewport.width() / visibleViewport.width());
            }
            Alldp=contentRect.width();
            viewportMin = visibleViewport.left;
            viewportMax = visibleViewport.right;

            TAG="ppp";
        }
        if (scale == 0)
        {
            scale = 1;
        }
        double needShowLabelNumber=needShowLabelNumber(axis,viewportMin,viewportMax);
        int canShowLabelNumber=(int)(Alldp/labelDimensionForStepsTab[position]);
        boolean flag1=false;
        double module=1;
        if( (needShowLabelNumber>0)&&(needShowLabelNumber<=canShowLabelNumber))
        {
            flag1=true;
        }
        else
        {
            //module=Math.ceil(needShowLabelNumber/canShowLabelNumber );
            //module = (int) Math.max(1, Math.ceil((axis.getValues().size() * labelDimensionForStepsTab[position] * 1.0f) / scale));//fly 修改 1.5f
            module=needShowLabelNumber/canShowLabelNumber;
        }
        Log.i(TAG, "needShowLabelNumber:"+needShowLabelNumber);
        Log.i(TAG, "canShowLabelNumber:"+canShowLabelNumber);
        Log.i(TAG, "module:"+module);
        //Reinitialize tab to hold lines coordinates.
        if (axis.hasLines() && (linesDrawBufferTab[position].length < axis.getValues().size() * 4))
        {
            linesDrawBufferTab[position] = new float[axis.getValues().size() * 4];//axis.getValues().size() 每条线2个点4个坐标值 x1,y1,x2,y2
        }
        //Reinitialize tabs to hold all raw values to draw.
        if (rawValuesTab[position].length < axis.getValues().size()) {
            rawValuesTab[position] = new float[axis.getValues().size()];//存储每条间隔线的坐标
        }
        //Reinitialize tabs to hold all raw values to draw.
        if (valuesToDrawTab[position].length < axis.getValues().size()) {
            valuesToDrawTab[position] = new AxisValue[axis.getValues().size()];//存储Label值
        }

        float rawValue;
        int valueIndex = 0;
        int valueToDrawIndex = 0;

        for (AxisValue axisValue : axis.getValues())
        {
            // Draw axis values that are within visible viewport.
            final float value = axisValue.getValue();
            if (value >= viewportMin && value <= viewportMax)
            {
                // Draw axis values that have 0 module value, this will hide some labels if there is no place for them.
                if ( flag1||
                        (0 == valueIndex % module)
                        )
                {
                    if (isAxisVertical)
                    {
                        rawValue = computator.computeRawY(value);
                    }
                    else
                    {
                        rawValue = computator.computeRawX(value);
                    }
                    //rawValue 对应屏幕像素坐标 把XY坐标于像素位置对应
                    if (checkRawValue(contentRect, rawValue, axis.isInside(), position, isAxisVertical))
                    {
                        rawValuesTab[position][valueToDrawIndex] = rawValue;
                        valuesToDrawTab[position][valueToDrawIndex] = axisValue;
                        ++valueToDrawIndex;
                    }
                }
                // If within viewport - increment valueIndex;
                ++valueIndex;
            }
        }
        valuesToDrawNumTab[position] = valueToDrawIndex;
    }
*/
}
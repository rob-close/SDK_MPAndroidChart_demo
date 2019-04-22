package com.company.visual.mdui;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.company.visual.R;
import com.company.visual.app.ErrorMessage;
import com.company.visual.app.SAPWizardApplication;
import com.company.visual.data.DataContentUtilities;
import com.company.visual.data.connector.EntityValueUIConnector;
import com.company.visual.data.connector.StockUIConnector;
import com.company.visual.data.operation.OnODataOperation;
import com.company.visual.data.operation.OperationResult;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sap.cloud.android.odata.espmcontainer.Stock;

import java.util.ArrayList;
import java.util.List;

public class LinechartActivity extends AppCompatActivity implements OnODataOperation {

    LineChart lineChart;
    List<EntityValueUIConnector> mValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linechart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        EntitySetListActivity.EntitySetName eSetName = EntitySetListActivity.EntitySetName.Stock;

        DataContentUtilities.getInstance().init(eSetName);
        String title = getResources().getString(eSetName.titleId());
        setTitle(title);

        DataContentUtilities dUtil = DataContentUtilities.getInstance();
        dUtil.init(eSetName);
        dUtil.download(this);


        lineChart = (LineChart) findViewById(R.id.lineChart);
    }

    @Override
    public void onOperation(OperationResult result) {
        if (result.getError() != null) {
            // handle error
            OperationResult.Operation op = result.getOperation();
            Exception ex = result.getError();
            ErrorMessage eMsg = null;
            switch (op) {
                case UPDATE:
                    eMsg = new ErrorMessage(getResources().getString(R.string.update_failed), getResources().getString(R.string.update_failed_detail), ex, false);
                    break;
                case DELETE:
                    eMsg = new ErrorMessage(getResources().getString(R.string.delete_failed), getResources().getString(R.string.delete_failed_detail), ex, false);
                    break;
                case CREATE:
                    eMsg = new ErrorMessage(getResources().getString(R.string.create_failed), getResources().getString(R.string.create_failed_detail), ex, false);
                    break;
                case READ:
                    eMsg = new ErrorMessage(getResources().getString(R.string.read_failed), getResources().getString(R.string.read_failed_detail), ex, false);
                    break;
            }
            SAPWizardApplication.getErrorHandler().sendErrorMessage(eMsg);
        } else {
            mValues = DataContentUtilities.getInstance().getItems();

            List<Entry> entries = new ArrayList<Entry>();

            for (int i = 0, j = 0, size = mValues.size(); i < size; i++) {
                StockUIConnector stockEntry = (StockUIConnector)mValues.get(i);
                Stock stock = (Stock)stockEntry.getConnectedObject();

                float value = stock.getQuantity().floatValue();
                entries.add(new BarEntry(j++, value));
            }
            LineDataSet dataSet = new LineDataSet(entries, "");
            dataSet.setDrawValues(true);
            dataSet.setDrawIcons(false);

            // set the line to be drawn like this "- - - - - -"
            dataSet.enableDashedLine(10f, 5f, 0f);
            dataSet.enableDashedHighlightLine(10f, 5f, 0f);
            dataSet.setColor(Color.BLACK);
            dataSet.setCircleColor(Color.BLACK);
            dataSet.setLineWidth(1f);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(9f);
            dataSet.setDrawFilled(true);
            dataSet.setFormLineWidth(1f);
            dataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            dataSet.setFormSize(15.f);

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
            dataSet.setFillDrawable(drawable);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(dataSet); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            lineChart.setData(data);

            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
            leftAxis.setAxisMaximum(260f);
            leftAxis.setAxisMinimum(0f);
            //leftAxis.setYOffset(20f);
            leftAxis.enableGridDashedLine(10f, 10f, 0f);
            leftAxis.setDrawZeroLine(false);

            // limit lines are drawn behind data (and not on top)
            leftAxis.setDrawLimitLinesBehindData(true);

            lineChart.invalidate();
        }
    }

    private class LabelFormatter implements IAxisValueFormatter {
        String[] labels;

        LabelFormatter(String[] labels) {
            this.labels = labels;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if (value > 0 && value < labels.length) {
                return labels[(int) value];
            } else {
                return "";
            }
        }
    }
}

package com.company.visual.mdui;

import android.graphics.Color;
import android.os.Bundle;
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
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.sap.cloud.android.odata.espmcontainer.Stock;

import java.util.ArrayList;
import java.util.List;

public class CombinedchartActivity extends AppCompatActivity implements OnODataOperation {

    CombinedChart chart;
    List<EntityValueUIConnector> mValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combinedchart);
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


        chart = (CombinedChart) findViewById(R.id.combinedChart);
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

            List<Entry> lineEntries = new ArrayList<Entry>();
            List<BarEntry> barEntries = new ArrayList<BarEntry>();

            for (int i = 0, size = mValues.size(); i < size; i++) {
                StockUIConnector stockEntry = (StockUIConnector)mValues.get(i);
                Stock stock = (Stock)stockEntry.getConnectedObject();

                float value = stock.getQuantity().floatValue();
                lineEntries.add(new Entry(i, value));
                barEntries.add(new BarEntry(i, value));
            }
            BarDataSet barDataSet = new BarDataSet(barEntries, "");
            barDataSet.setColor(Color.rgb(60, 220, 78));
            barDataSet.setValueTextColor(Color.rgb(60, 220, 78));
            barDataSet.setValueTextSize(10f);
            barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            BarData barData = new BarData(barDataSet);

            LineDataSet lineDataSet = new LineDataSet(lineEntries, "");
            lineDataSet.setColor(Color.rgb(240, 238, 70));
            lineDataSet.setLineWidth(2.5f);
            lineDataSet.setCircleColor(Color.rgb(240, 238, 70));
            lineDataSet.setCircleRadius(5f);
            lineDataSet.setFillColor(Color.rgb(240, 238, 70));
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSet.setDrawValues(true);
            lineDataSet.setValueTextSize(10f);
            lineDataSet.setValueTextColor(Color.rgb(240, 238, 70));

            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineData lineData = new LineData();
            lineData.addDataSet(lineDataSet);

            // create a data object with the datasets
            CombinedData data = new CombinedData();
            data.setData(lineData);
            data.setData(barData);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setDrawGridLines(false);
            rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
            xAxis.setAxisMinimum(0f);
            xAxis.setGranularity(1f);

            xAxis.setAxisMaximum(data.getXMax() + 0.25f);

            // set data
            chart.setData(data);

            // draw bars behind lines
            CombinedChart.DrawOrder[] drawOrder = new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE};
            chart.setDrawOrder(drawOrder);

            chart.invalidate();
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

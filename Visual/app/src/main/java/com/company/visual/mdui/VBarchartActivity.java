package com.company.visual.mdui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.sap.cloud.android.odata.espmcontainer.Stock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class VBarchartActivity extends AppCompatActivity implements OnODataOperation {

    BarChart barChart;
    List<EntityValueUIConnector> mValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vbarchar);
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


        barChart = (BarChart) findViewById(R.id.barChart);
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

            List<BarEntry> entries = new ArrayList<BarEntry>();
            List<String> labels = new ArrayList<String>();
            List<Integer> colors = new ArrayList<Integer>();

            for (int i = 0, size = mValues.size(); i < size; i++) {
                StockUIConnector stockEntry = (StockUIConnector)mValues.get(i);
                Stock stock = (Stock)stockEntry.getConnectedObject();

                float value = stock.getQuantity().floatValue();
                entries.add(new BarEntry(i, value));
                labels.add(stock.getProductID());
                if (value < 50) {
                    colors.add(Color.RED);
                } else if (value < 100) {
                    colors.add(Color.YELLOW);
                } else {
                    colors.add(Color.GREEN);
                }
            }
            BarDataSet barDataSet = new BarDataSet(entries, "ABC");
            barDataSet.setDrawValues(true);
            barDataSet.setColors(colors);
            String[] labelArray = new String[labels.size()];
            labels.toArray(labelArray);

            IAxisValueFormatter xAxisFormatter = new LabelFormatter(labelArray);
            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setLabelRotationAngle(90f);
            xAxis.setTextColor(Color.BLACK);
            xAxis.setAxisMinimum(0f);
            xAxis.setCenterAxisLabels(false);
            xAxis.setDrawLabels(true);
            xAxis.setLabelCount(entries.size(), false);
            xAxis.setValueFormatter(xAxisFormatter);

            //YAxis leftAxis = lineChart.getAxisLeft();
//            XAxis leftAxis = barChart.getXAxis();
//            leftAxis.setTypeface(Typeface.create("f72", 0));

            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.9f);
            barChart.setData(barData);
            barChart.setFitBars(true);
            Description description = new Description();
            description.setText("Some Description");
            barChart.setFitBars(false);
            barChart.setDescription(description);
            barChart.getLegend().setEnabled(true);
            barChart.invalidate();

;
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

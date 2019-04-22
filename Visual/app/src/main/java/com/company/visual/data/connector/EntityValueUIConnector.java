package com.company.visual.data.connector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.company.visual.R;
import com.sap.cloud.mobile.odata.EntityValue;

import java.util.List;
import java.util.Map;

/**
 * This class, more preciselly, its children encapsulate the entity set data for the UI. Its
 * getters are used to fill the screens with entity set specific data. It provides a dedicated
 * method to access the wrapped data object, as well.
 */
public abstract class EntityValueUIConnector {

    /**
     * Returns the names of the key properties.
     * @return
     */
    public abstract List<String> getKeyPropertyNames();

    /**
     * Returns the name of the master property (first non-key property).
     * @return
     */
    public abstract String getMasterPropertyName();

    /**
     * Returns the names of all the properties.
     * @return
     */
    public abstract List<String> getPropertyNames();

    /**
     * Returns the all the properties with their values.
     * @return
     */
    public abstract Map<String, String> getPropertiesWithValues();

    /**
     * Returns the connected ODATA object.
     * @return
     */
    public abstract EntityValue getConnectedObject();

    /**
    * Returns a view element which represents the corresponding property on the detail view. If an
    * entity set needs a special look-and-feel for one of its properties, then this implementation should
    * be overridden in the entity set specific child class.
    */
    public View getPropertyListItem(int position, View convertView, ViewGroup parent, Context ctx) {

        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.detail_list_element, parent, false);
        }

        TextView mProperty = convertView.findViewById(R.id.textView1);
        TextView mValue = convertView.findViewById(R.id.textView2);

        List<String> pNames = getPropertyNames();
        Map<String, String> valueMap = getPropertiesWithValues();
        String p = pNames.get(position);
        String value = valueMap.get(p);

        mProperty.setText(p);

        if (value != null) {
            mValue.setText(value);
        } else {
            mValue.setText("");
        }
        return convertView;
    }
}

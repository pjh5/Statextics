package com.pbj.statextics;

/**
 * Created by phillippan on 2/1/15.
 */
public class ListData {
    public final String categoryName;
    public final double value;

    public ListData(String name, Double stat){
        categoryName = name;
        value = stat;
    }


    public String toString() {
        return this.categoryName + " " + "[" +  Math.floor(this.value * 100)/100 + "]";
    }
}
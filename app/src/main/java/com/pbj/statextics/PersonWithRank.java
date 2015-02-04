package com.pbj.statextics;

/**
 * Created by phillippan on 2/1/15.
 */
public class PersonWithRank {
    public final Person p;
    public double value;

    public PersonWithRank(Person p, double value) {
        this.p = p;

        this.value = value;

    }


    public String toString() {
        String display = p.getName();
        if (display == "" || display == null){
            display = p.getNumber();
        }

        return display + "  " + Math.floor(value * 100)/100;
    }
}

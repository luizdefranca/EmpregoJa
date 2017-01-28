package com.mafiagames.empregoja;

import java.util.ArrayList;

/**
 * Created by victor.almeida on 27/01/2017.
 */

public class JsonWrapper {

    public String query;
    public String location;
    public int totalResults;
    public int start;
    public int end;
    public ArrayList<Emprego> results;

    public JsonWrapper() {

    }
}

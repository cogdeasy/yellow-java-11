package com.libertymutual.customer.dto;

import java.util.List;

public class ListResponse<T> {

    private List<T> data;
    private int total;

    public ListResponse() {
    }

    public ListResponse(List<T> data) {
        this.data = data;
        this.total = data.size();
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}

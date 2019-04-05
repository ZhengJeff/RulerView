package com.jeff.ruler;

public class RulerData<T> {
    private T data;
    private String text;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

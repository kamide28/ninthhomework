package com.example.ninthhomework.controller;

public class UpdateForm {

    private String name;

    private Integer age;

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public UpdateForm(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}

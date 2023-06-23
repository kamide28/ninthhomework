package com.example.ninthhomework.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateForm {
    private int id;
    @NotBlank
    private String name;
    @NotNull
    private Integer age;
    
    public CreateForm(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }
}

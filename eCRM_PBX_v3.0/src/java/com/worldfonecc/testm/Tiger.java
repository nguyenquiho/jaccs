/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.worldfonecc.testm;

/**
 *
 * @author honguyen
 */
public class Tiger {
    private String name;
    private int age;
    private String color;

     // Constructor mặc định
    public Tiger() {
        // Bạn có thể khởi tạo các thuộc tính với giá trị mặc định ở đây
        this.name = "Không rõ tên";
        this.age = 0;
        this.color = "Cam";
    }
    
    // Constructor
    public Tiger(String name, int age, String color) {
        this.name = name;
        this.age = age;
        this.color = color;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }

}

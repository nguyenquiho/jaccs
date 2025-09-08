///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.worldfonecc.testc;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.servlet.ModelAndView;
//import com.worldfonecc.utils.helpers.Common;
//import com.worldfonecc.testm.Tiger;
//import com.worldfonecc.testm.Tiger;
//import java.util.HashMap;
//import java.util.Map;
//import org.springframework.http.ResponseEntity;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//
//
////@Controller
//public class JavaSpringTestController {
//
//    @RequestMapping("/tiger")
//    public ModelAndView handleTigerRequest() {
//        ModelAndView modelAndView = new ModelAndView("tiger");
//        modelAndView.addObject("message", "Hello, Tiger!");
//        modelAndView.addObject("ipAddress", Common.getIpAddress()); // Assuming Common.getIpAddress() returns an IP address
//        Tiger tiger = new Tiger();
//        tiger.setName("Hoàng hổ");
//        tiger.setAge(5);
//        tiger.setColor("Vàng cam");
//        modelAndView.addObject("tiger", tiger);
//        return modelAndView;
//    }
//    
//    @RequestMapping("/tiger/child")
//    public ModelAndView handleChildTigerRequest() {
//        ModelAndView modelAndView = new ModelAndView("tiger");
//        modelAndView.addObject("message", "Hello, Child Tiger!");
//        modelAndView.addObject("ipAddress", Common.getIpAddress()); // Assuming Common.getIpAddress() returns an IP address
//        Tiger tiger = new Tiger();
//        tiger.setName("Bach Ho");
//        tiger.setAge(5);
//        tiger.setColor("Trang");
//        modelAndView.addObject("tiger", tiger);
//        return modelAndView;
//    }
//    
////    @RequestMapping("/tiger/show_json")
////    public ResponseEntity<String> getJsonData() {
//////        // Tạo dữ liệu cần trả về dưới dạng một đối tượng Java
//////        Map<String, Object> data = new HashMap<>();
//////        data.put("name", "John Doe");
//////        data.put("age", 30);
//////
//////        // Chuyển đổi đối tượng thành JSON
//////        ObjectMapper objectMapper = new ObjectMapper();
//////        String jsonData = objectMapper.writeValueAsString(data);
//////
//////        // Tạo ResponseEntity để trả về
//////        return ResponseEntity.ok()
//////                .contentType(MediaType.APPLICATION_JSON)
//////                .body(jsonData);
////
////        Tiger tiger = new Tiger();
////        tiger.setName("Hoàng hổ");
////        tiger.setAge(5);
////        tiger.setColor("Vàng cam");
////
////        ObjectMapper objectMapper = new ObjectMapper();
////        String tigerJson = objectMapper.writeValueAsString(tiger);
////
////        return ResponseEntity.ok()
////                .contentType(MediaType.APPLICATION_JSON)
////                .body(tigerJson);
////    }
//}

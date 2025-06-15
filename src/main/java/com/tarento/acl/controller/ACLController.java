package com.tarento.acl.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/access")
public class ACLController {

    @PostMapping(value = "/check/{courseId}")
    public Boolean lookup(@RequestBody Map<String, Object> userProfile, @PathVariable String courseId) throws Exception {
        System.out.println("CourseID=" + courseId);
        System.out.println("userProfile=" + userProfile);
        return true;
    }


}

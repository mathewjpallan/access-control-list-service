package com.tarento.acl.controller;

import com.tarento.acl.common.AccessControlManager;
import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ACLController {

    @PostMapping(value = "/access/check/{courseId}")
    @Timed(value = "access.check", description = "Time taken to check access")
    public Boolean checkAccess(@RequestBody Map<String, Object> userProfile, @PathVariable String courseId) throws Exception {
        Map<String, Integer> userProfileToBitPositions = AccessControlManager.mapUserProfileToBitPositions(userProfile);
        return AccessControlManager.canUserAccessCourse(courseId, userProfileToBitPositions);
    }

    @PostMapping(value = "/access/check/courses")
    @Timed(value = "access.check.multiplecourses", description = "Time taken to check access")
    public List<Map> checkAccessForCourses(@RequestBody Map<String, Object> request) throws Exception {
        Map<String, Integer> userProfileToBitPositions = AccessControlManager.mapUserProfileToBitPositions((Map) request.get("userProfile"));
        List<Map> resultList = new ArrayList<>();
        List courses = (List) request.get("courses");
        for (Object course : courses) {
            Map<String, Boolean> result = new HashMap<>();
            result.put(course.toString(), AccessControlManager.canUserAccessCourse(course.toString(), userProfileToBitPositions));
            resultList.add(result);
        }
        return resultList;
    }


    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public String health() {
        return "service is up";
    }

}

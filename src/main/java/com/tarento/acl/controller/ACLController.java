package com.tarento.acl.controller;

import com.tarento.acl.common.AccessControlManager;
import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ACLController {

    @PostMapping(value = "/access/check/{courseId}")
    @Timed(value = "access.check", description = "Time taken to check access")
    public Boolean checkAccess(@RequestBody Map<String, Object> userProfile, @PathVariable String courseId) throws Exception {
        Map<String, Integer> userProfileToBitPositions = AccessControlManager.mapUserProfileToBitPositions(userProfile);
        return AccessControlManager.canUserAccessCourse(courseId, userProfileToBitPositions);
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public String health() {
        return "service is up";
    }

}

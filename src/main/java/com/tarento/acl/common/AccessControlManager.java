package com.tarento.acl.common;

import java.io.*;
import java.util.*;

public class AccessControlManager {

    private static Map<String, Map<String, Integer>> masterData = new HashMap<>();

    private static Map<String, List<Map<String, BitSet>>> accessControlList = new HashMap<>();

    public static void init() throws Exception {
        long time = System.currentTimeMillis();
        loadAllMasterData(masterData);
        loadAccessControlRules("course_user_groups.csv", accessControlList);
        System.out.println("Loading from file & init of bitsets took " + (System.currentTimeMillis() - time) + " milliseconds");
    }

    public static boolean canUserAccessCourse(String courseID, Map<String, Integer> userProfileToBitPositions) {
        List<Map<String, BitSet>> aclList = accessControlList.get(courseID);

        if (aclList == null) {
            //Implies course does not have an acl and anyone can access
            return true;
        }

        //Iterate through the acl list and see if the user's profile attributes match that of the user groups
        for (Map<String, BitSet> acl : aclList) {
            boolean matchesAllAttributes = true;

            //each ACL refers to a user group that contains filter values
            for (Map.Entry<String, BitSet> aclEntry : acl.entrySet()) {
                String aclKey = aclEntry.getKey();
                BitSet aclBitset = aclEntry.getValue();

                // Check if the user's bit position for this attribute is set in the ACL's BitSet
                Integer userBitPosition = userProfileToBitPositions.get(aclKey);
                if (userBitPosition == null || !aclBitset.get(userBitPosition)) {
                    matchesAllAttributes = false;
                    break;
                }
            }

            // If all attributes matched for this ACL, the user has access
            if (matchesAllAttributes) {
                return true;
            }
        }

        // If no ACL matched completely, deny access
        return false;
    }

    public static Map<String, Integer> mapUserProfileToBitPositions(Map<String, Object> userProfile) {
        Map<String, Integer> userProfileBitPositions = new HashMap<>();
        for (Map.Entry<String, Object> profileEntry : userProfile.entrySet()) {
            String key = profileEntry.getKey();
            String value = profileEntry.getValue().toString();
            userProfileBitPositions.put(key, masterData.get(key).get(value));
        }
        return userProfileBitPositions;
    }

    private static void loadAllMasterData(Map<String, Map<String, Integer>> masterData) throws IOException {
        loadMasterData("designation");
        loadMasterData("dept");
        loadMasterData("group");
        loadMasterData("cadre");
        loadMasterData("service");
        loadMasterData("batch");
        loadMasterData("profile_status");
    }

    private static void loadMasterData(String fileNamePrefix) throws IOException {
        Map<String, Integer> map = new HashMap<>();
        try (InputStream inputStream = AccessControlManager.class.getClassLoader().getResourceAsStream(fileNamePrefix + "_master.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                throw new FileNotFoundException("File not found in classpath: " + fileNamePrefix + "_master.csv");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        map.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid number: " + parts[1]);
                    }
                }
            }
            masterData.put(fileNamePrefix, map);
        }
    }

    private static void loadAccessControlRules(String fileName, Map<String, List<Map<String, BitSet>>> accessControlList) throws IOException {
        try (InputStream inputStream = AccessControlManager.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                throw new FileNotFoundException("File not found in classpath: " + fileName);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    List<Map<String, BitSet>> acl = accessControlList.get(parts[0].trim());
                    if (acl == null) {
                        acl = new ArrayList();
                        accessControlList.put(parts[0].trim(), acl);
                    }
                    acl.add(parseAccessControlList(parts[1].trim()));
                }
            }
        }
    }

    private static Map<String, BitSet> parseAccessControlList(String acl) {
        Map<String, BitSet> map = new HashMap<>();

        String[] parts = acl.split("\\|");

        map.put("designation", createBitSetForAttribute("designation", parts[0]));
        map.put("dept", createBitSetForAttribute("dept", parts[1]));
        map.put("group", createBitSetForAttribute("group", parts[2]));
        map.put("cadre", createBitSetForAttribute("cadre", parts[3]));
        map.put("service", createBitSetForAttribute("service", parts[4]));
        map.put("batch", createBitSetForAttribute("batch", parts[5]));
        map.put("profile_status", createBitSetForAttribute("profile_status", parts[6]));

        return map;
    }

    private static BitSet createBitSetForAttribute(String masterDataKey, String items) {
        Map<String, Integer> masterDataMap = masterData.get(masterDataKey);
        String[] parts = items.split("/");
        BitSet bitSet = new BitSet();
        for (String part : parts) {
            try {
                bitSet.set(masterDataMap.get(part));
            } catch (Exception ex) {
                System.out.println("Ex=" + part);
                throw ex;
            }

        }
        return bitSet;
    }


}

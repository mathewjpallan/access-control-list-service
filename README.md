# access-control-list-service
An API that implements an efficient ACL leveraging Bitsets

# **Problem Statement:**
There are a list of courses that need to be access controlled based on user's attributes like designation, dept, batch etc.

# **Approach:**
* This solution uses bitsets to efficiently store data for lookups. Bitsets are efficient at storing sparse sets.
* All the attribute values (list of designations, depts etc) that need to be used in the access control lists need to be part of master data and each one should have a unique index (bitset position). All this data is loaded into a hashmap in memory with the following structure. This allows us to quickly lookup the index (bitset) positions for any designation/dept/service value etc.
```json
{
    "designation": {
        "designation abc":1,
        "designation xyz":2,
    },
    "dept": {
        "dept abc":1,
        "dept xyz":2,
    },
}
```

* The course ACLs are represented in the below structure. For the course to be accessible to a user, the user's profile attributes should match any one (or condition across criteria) of the criteria. Within a criteria, the user's attributes should match any one of the aclValues for each of the keys ie. designation of the user has to be any one from the list AND (AND condition within a criteria) user's dept has to also be any one from the list.

```json
{
"courseID": "do_1234567890987654321_334",
"accessControlList": {
    "criteria 1": [
        {
            "aclKey": "designation",
            "aclValue": [
                "designation334",
                "designation32996",
                "designation69063",
                "designation42248",
                "designation20626",
                "designation25012"
            ]
        },
        {
            "aclKey": "dept",
            "aclValue": [
                "department of test334",
                "department of test63655",
                "department of test86362",
                "department of test30165",
                "department of test2492"
            ]
        }
        // and so on for other attributes
    ],
    "criteria 2" : [
        //similar to criteria 1
    ]

}
}
```
* The ACLs are loaded into memory and stored in the following map structure

```json
{
  "courseID": "do_1234567890987654321_334",
  "accessControlList": [
    {
      "designation" : Bitset for the aclValues,
      "dept": Bitset for the aclValues
    },
    {
      "designation" : Bitset for the aclValues,
      "dept": Bitset for the aclValues
    }
  ]
}
```

* The following steps are being done to see if a user matches a criteria
    * Find the indexes (bitset positions) for the user's attributes that are used in the ACL
    * Iterate through the accessControlList and do a bitset lookup from the ACL structure to verify if the user attributes match that of the ACL. Bitset lookups are O(1) operations and so this would be performant.
    * Within a criteria, break at the 1st mismatch as it is an AND condition
    * Within the list of criteria, break at the 1st match as it is an OR condition


# **Testing the code**

### Data Setup

Assuming access control attributes are Dept, designation, group, cadre, service, batch, profile status

**Run the below commands from the CLI in the src/main/resources folder as the code is reading these files from the classpath**

The below commands help to create sample master data files (for testing, use a DB for master data in production) with attribute values and their index (bit position). In the below samples, we are creating files with 1 lakh designations & depts, 10K cadres, service & batches and around 100 statuses and groups.

seq 0 100000 | awk '{print "designation" $1 "," $1}' > designation_master.csv
seq 0 100000 | awk '{print "department of test" $1 "," $1}' > dept_master.csv
seq 0 100 | awk '{print "Group of test" $1 "," $1}' > group_master.csv
seq 0 10000 | awk '{print "Cadre of test" $1 "," $1}' > cadre_master.csv
seq 0 10000 | awk '{print "Service of test" $1 "," $1}' > service_master.csv
seq 0 10000 | awk '{print "Batch of test" $1 "," $1}' > batch_master.csv
seq 0 100 | awk '{print "ProfileStatus of test" $1 "," $1}' > profile_status_master.csv

Assume 20K courses are there and around 10% of them have access controls with 5 criteria for each course. Increase in %age of courses having access controls would take more memory and not incrase the cpu usage.

seq 1 2000 | awk 'BEGIN{srand(42)} {
r = int(rand()*10000)
for(j=1; j<=5; j++) {
print "do_1234567890987654321_" r ",designation" r "/designation" int(rand()*100000) "/designation" int(rand()*100000) "/designation" int(rand()*100000) "/designation" int(rand()*100000) "/designation" int(rand()*100000) "|department of test" r "/department of test" int(rand()*100000) "/department of test" int(rand()*100000) "/department of test" int(rand()*100000) "/department of test" int(rand()*100000) "|Group of test" int(rand()*100) "/Group of test" int(rand()*100) "/Group of test" int(rand()*100) "/Group of test" int(rand()*100) "/Group of test" int(rand()*100) "|Cadre of test" r "/Cadre of test" int(rand()*10000) "/Cadre of test" int(rand()*10000) "/Cadre of test" int(rand()*10000) "/Cadre of test" int(rand()*10000) "|Service of test" r "/Service of test" int(rand()*10000) "/Service of test" int(rand()*10000) "/Service of test" int(rand()*10000) "/Service of test" int(rand()*10000) "/Service of test" int(rand()*10000) "|Batch of test" r "/Batch of test" int(rand()*10000) "/Batch of test" int(rand()*10000) "/Batch of test" int(rand()*10000) "/Batch of test" int(rand()*10000) "/Batch of test" int(rand()*10000) "|ProfileStatus of test55"
}
}' > course_user_groups.csv

The above command produces a file in the following structure and the AccessControlManager loads this into the map structure described above. / is used as a separator for acl values and | is used to separate the conditions within a criteria. There is a hardcoded assumption here (loader also assumes this) that the list of designations is followed by dept and so on.
The course ACLs (eg of 2 criteria for one course) are represented on file (just a temp solution for testing, actual impl should store these in DB) as follows
do_1234567890987654321_334,designation334/designation32996/designation69063/designation42248/designation20626/designation25012|department of test334/department of test63655/department of test86362/department of test30165/department of test2492|Group of test36/Group of test76/Group of test31/Group of test13/Group of test10|Cadre of test334/Cadre of test7606/Cadre of test816/Cadre of test5509/Cadre of test5646|Service of test334/Service of test7432/Service of test9817/Service of test2188/Service of test4543/Service of test5186|Batch of test334/Batch of test5738/Batch of test5552/Batch of test7285/Batch of test6288/Batch of test7961|ProfileStatus of test55
do_1234567890987654321_334,designation334/designation58371/designation27476/designation82959/designation91368/designation96540|department of test334/department of test25208/department of test11994/department of test21553/department of test88864|Group of test98/Group of test51/Group of test91/Group of test34/Group of test28|Cadre of test334/Cadre of test2314/Cadre of test4842/Cadre of test3893/Cadre of test9920|Service of test334/Service of test5659/Service of test9402/Service of test5567/Service of test3092/Service of test9220|Batch of test334/Batch of test7756/Batch of test7636/Batch of test4406/Batch of test3494/Batch of test3188|ProfileStatus of test55

### Running the code
* Run mvn clean install to build the project after cloning the project
* Execute the devops/build.sh to generate a docker image after the build is successful
* docker run -p 8080:8080 acl-service:0.0.1 --memory 2g --cpus 2
* If you want to see metrics, you can run docker-compose up from the devops folder. This will bring up prometheus and grafana. prometheus.yml targets will need to be updated based on the docker networking used. After grafana comes up, a datasource has to be configured and the url used(localhost:9090) will also need to change depending on docker networking. On podman desktop, i was using 'host.containers.internal:8080' as the target in prometheus and http://prometheus:9090 as the promtheus url within grafana.

### Load testing
The jmx file is setup to load test the API for access checks.
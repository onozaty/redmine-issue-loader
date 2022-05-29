# redmine-issue-loader

It load the issue information from the CSV file and registers or  updates it as a issue to Redmine.

It has the following characteristics.

* Multiple tickets can be created or updated at once.
* By setting the mapping information in the config file, contents of the CSV file can be converted and registered to Redmine.
* It is possible to update the issue using not only the issue ID but also the custom field as a key. However, the value of that custom field must be unique throughout the system.

## Usage

Java (JDK8 or higher) is required for execution.

Download the latest jar file (`redmine-issue-loader-x.x.x-all.jar`) from below.

* https://github.com/onozaty/redmine-issue-loader/releases/latest

Execute the application with the following command.

```
java -jar redmine-issue-loader-2.4.1-all.jar config.json issues.csv
```

The first argument is the configuration file. The second argument will be the CSV file with the Issue information.

When executed, information on the loaded issue is output as shown below.

```
Processing start...
#1 is created.
#2 is created.
#3 is created.
Processing is completed. 3 issues were loaded.
```

## Configuration file

In the configuration file, you will list the connection information of Redmine, the mapping information of the fields on the CSV file and Redmine.

### Ex: create issue

An example of a configuration file when creating a new issue.

```json
{
  "mode": "CREATE",
  "readmineUrl": "http://192.168.33.10",
  "apiKey": "8fba5d86e1d310d13860ba7ddd96be1b69743e7f",
  "csvEncoding": "UTF-8",
  "fields": [
    {
      "headerName": "Project",
      "type": "PROJECT_ID",
      "mappings" : {
        "Project A" : 1,
        "Project B" : 2
      }
    },
    {
      "headerName": "Tracker",
      "type": "TRACKER_ID",
      "mappings" : {
        "Bug" : 1,
        "Feature" : 2,
        "Support" : 3
      }
    },
    {
      "headerName": "Subject",
      "type": "SUBJECT"
    },
    {
      "headerName": "Description",
      "type": "DESCRIPTION"
    },
    {
      "headerName": "Field1",
      "type": "CUSTOM_FIELD",
      "customFieldId": 1
    },
    {
      "headerName": "Field2",
      "type": "CUSTOM_FIELD",
      "customFieldId": 2,
      "multipleItemSeparator": ";"
    },
    {
      "headerName": "Watchers",
      "type": "WATCHER_USER_IDS",
      "multipleItemSeparator": ";",
      "mappings": {
        "User A": 5,
        "User B": 6
      }
    }
  ]
}
```

An example of a CSV file corresponding to the above configuration file.

```csv
Project,Tracker,Subject,Description,Field1,Field2,Watchers
Project A,Bug,xxxx,yyyy,A,1;2,User A;User B
Project B,Feature,aaaa,bbbb,,,
Project B,Bug,zzzz,zzzz,1,2,User B
```

### Ex: update issue

An example of a configuration file when updating an issue.

```json
{
  "mode": "UPDATE",
  "readmineUrl": "http://192.168.33.10",
  "apiKey": "8fba5d86e1d310d13860ba7ddd96be1b69743e7f",
  "csvEncoding": "UTF-8",
  "fields": [
    {
      "headerName": "#",
      "type": "ISSUE_ID",
      "primaryKey": true
    },
    {
      "headerName": "Status Id",
      "type": "STATUS_ID",
      "primaryKey": false
    },
    {
      "headerName": "Field1",
      "type": "CUSTOM_FIELD",
      "customFieldId": 1,
      "primaryKey": false
    }
  ]
}
```

An example of a configuration file when updating an issue.

```csv
#,Subject,Status Id,Field1
1,xxxx,1,A
2,yyyy,2,B
3,zzzz,3,C
```

### Contents of each item

The contents of each item are as follows.

* `mode` : processing mode. `CREATE` is newly created, `UPDATE` is updated.
* `readmineUrl` : Redmine's URL.
* `apiKey` : Redmine API access key.
* `basicAuth` : Basic authentication. You must specify either API access key or Basic authentication.
  * `username` : User name used for basic authentication.
  * `password` : password used for basic authentication.
* `timeout` : Timeout seconds when making a request to Redmine. If not set, it will be `10`.
* `csvEncoding` : CSV file encoding.
* `replaceString` : String replacement settings.
  * `pattern` : Regular expression of the character to be replaced.
  * `replacement` : The replacement character.
* `fields` : CSV field information. It is not necessary to write all the CSV fields. Write what you use as the key and the field to register.
    * `headerName` : Header name in CSV.
    * `type` : Type. What can be specified as a type is described later.
    * `customFieldId` : ID of the custom field. Set if the type is `CUSTOM_FIELD`.
    * `multipleItemSeparator` : The character to separate the values. Set if the type is `WATCHER_USER_IDS` or `CUSTOM_FIELD` and multiple selection.
    * `primaryKey` : Primary key? Search for issues to be updated using the information of the field set to `true`, and the field` false` will be updated. It is not necessary to specify when mode is `CREATE`.
    * `mappings` : By describing the mapping between the value on CSV and the value on Redmine, contents of CSV can be converted and registered. For example, to convert a project name to a project ID.

Items that can be specified as a type of field are as follows.

|Type|mode: `CREATE`|mode: `UPDATE`|Contents|ID confirmation URL|
|------|-------|----|----|----|
|`ISSUE_ID`|×|○|Issue ID. It can only be specified as primary key for update.|-|
|`PROJECT_ID`|○|○|Project ID. It is required item when new issue created.|`/projects.xml`|
|`TRACKER_ID`|○|○|Tracker ID.|`/trackers.xml`|
|`STATUS_ID`|○|○|Status ID.|`/issue_statuses.xml`|
|`PRIORITY_ID`|○|○|Priority ID.|`/enumerations/issue_priorities.xml`|
|`ASSIGNED_TO_ID`|○|○|Assignee ID.|`/users.xml`|
|`CATEGORY_ID`|○|○|Category ID.|`/projects/:project_id/issue_categories.xml`<br>`:project_id` part specifies the ID of the target project.|
|`FIXED_VERSION_ID`|○|○|Target version ID.|`/projects/:project_id/versions.xml`<br>`:project_id` part specifies the ID of the target project.|
|`PARENT_ISSUE_ID`|○|○|Parent issue ID.|-|
|`SUBJECT`|○|○|Subject. It is required item when new issue created.|-|
|`DESCRIPTION`|○|○|Description.|-|
|`START_DATE`|○|○|Start date. The format is `YYYY-MM-DD` or `YYYY/MM/DD`.|-|
|`DUE_DATE`|○|○|Due date. The format is `YYYY-MM-DD` or `YYYY/MM/DD`.|-|
|`DONE_RATIO`|○|○|Done rate.|-|
|`IS_PRIVATE`|○|○|Private. `true` or `false`.|-|
|`ESTIMATED_HOURS`|○|○|Estimated time.|-|
|`CUSTOM_FIELD`|○|○|Custom field. It can also be used as a primary key for updating.<br>When specifying this type, you need to specify the ID of the corresponding custom field as `customFieldId`.|`/custom_fields.xml`|
|`WATCHER_USER_IDS`|○|×|Watcher ID. It does not support update mode.|`/users.xml`|

Items specified as ID can be confirmed with the ID confirmation URL in the table above.

For example, in an environment where the URL of Redmine is `http://localhost`, if you want to check the ID of the project, you can confirm it by `http://localhost/projects.xml`.

```xml
<projects total_count="2" offset="0" limit="25" type="array">
  <project>
    <id>1</id>
    <name>Project A</name>
    <identifier>a</identifier>
    <description/>
    <status>1</status>
    <is_public>true</is_public>
    <created_on>2019-01-05T12:46:56Z</created_on>
    <updated_on>2019-01-05T12:46:56Z</updated_on>
  </project>
  <project>
    <id>2</id>
    <name>Project B</name>
    <identifier>b</identifier>
    <description/>
    <status>1</status>
    <is_public>true</is_public>
    <created_on>2019-01-05T12:47:07Z</created_on>
    <updated_on>2019-01-05T12:47:07Z</updated_on>
  </project>
</projects>
```

If written in project name on CSV, you can convert to ID by specifying `mappings` based on the above contents.
```json
    {
      "headerName": "Project",
      "type": "PROJECT_ID",
      "mappings" : {
        "Project A" : 1,
        "Project B" : 2
      }
    }
```

Samples of the configuration file and CSV file are located under the `sample` folder, so please refer to that sample as a reference.

## Notes

* It will use Redmine's REST API, so the REST API must be enabled.
* When using a custom field as a key, "Used as a filter" must be ON as the target custom field setting.

## How to build

When building from the source code, build the application with the following command in the environment where Java (JDK 8 or higher) is installed.

```
gradlew shadowJar
```

`build/libs/redmine-issue-loader-x.x.x-all.jar` will be created. (`x.x.x` is version number)

## License

MIT

## Author

[onozaty](https://github.com/onozaty)

I am looking for people who are willing to become [sponsors](https://github.com/sponsors/onozaty) and contribute to maintaining this project.

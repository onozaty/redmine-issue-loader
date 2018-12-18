# redmine-issue-updater

It is a tool to update Redmine issues. Read the CSV file and update the issue.

Currently only custom fields are targeted.

It is possible to update the issue using not only the issue ID but also the custom field as a key. However, the value of that custom field must be unique throughout the system.

## Usage

In the environment where Java (over JDK8) is installed, build the application with the following command.

```
gradlew shadowJar
```

`build/libs/redmine-issue-updater-all.jar` will be created.

Execute the application with the following command.

```
java -jar build/libs/redmine-issue-updater-1.0.0-all.jar config.json issues.csv
```

The first argument is the configuration file. The second argument will be the CSV file with the Issue to be updated.

When executed, information on the updated issue is output as shown below.

```
Processing start...
#1 is updated.
#2 is updated.
#3 is updated.
Processing is completed. 3 issues were updated.
```

The following is an example of a configuration file.

```json
{
  "readmineUrl": "http://localhost",
  "apyKey": "20d0779f947c3c9a7248332a078ff458644ed73d",
  "csvEncoding": "UTF-8",
  "fields": [
    {
      "headerName": "#",
      "type": "ISSUE_ID",
      "primaryKey": true
    },
    {
      "headerName": "Field1",
      "type": "CUSTOM_FIELD",
      "customFieldId": 1,
      "primaryKey": false
    },
    {
      "headerName": "Field2",
      "type": "CUSTOM_FIELD",
      "customFieldId": 2,
      "primaryKey": false
    }
  ]
}
```

The contents of each item are as follows.

* `readmineUrl` : Redmine's URL.
* `apyKey` : Redmine API access key.
* `csvEncoding` : CSV file encoding.
* `fields` : CSV field information. It is not necessary to write all the CSV fields. Write what you use as the key and the field to update.
    * `headerName` : Header name in CSV.
    * `type` : Type. (`ISSUE_ID` or `CUSTOM_FIELD`)
    * `customFieldId`
    * `primaryKey` : Primary key? Search for issues to be updated using the information of the field set to `true`, and the field` false` will be updated.

The ID of the custom field can be confirmed by the URL of the custom field setting screen.
In the following cases, the ID of the custom field is `1`.

* http://localhost/custom_fields/1/edit

Or you can check the ID assigned to the custom field input field on the issue creation / editing screen. In the following cases, the ID of the custom field is `2`.

```html
<input type="text" name="issue[custom_field_values][2]" id="issue_custom_field_values_2" value="A" class="string_cf">
```

The following is an example of a CSV file.

```csv
#,Subject,Field1,Field2,Field3
1,xxxx,A,a,C
2,yyyy,B,b,B
3,zzzz,C,c,A
```

Samples of the configuration file and CSV file are located under the `sample` folder.

## Notes

* It will use Redmine's REST API, so the REST API must be enabled.
* When using a custom field as a key, "Used as a filter" must be ON as the target custom field setting.

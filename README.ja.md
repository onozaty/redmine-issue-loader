# redmine-issue-loader

CSVファイルに記載された情報を読み込んで、Redmineにチケットとして登録、更新するツールです。

下記のような特徴があります。

* 複数のチケットをまとめて作成、更新できる。
* 設定ファイルにマッピング情報を記載することによって、CSVファイルの内容を変換してRedmineへ登録することができる。
* チケットIDだけでなく、カスタムフィールドをキーとしてチケットを更新することができる。
ただし、そのカスタムフィールドの値がシステム全体で一意である必要あり。

## 利用方法

実行にはJava(JDK8以上)が必要となります。

下記から最新の実行ファイル(`redmine-issue-loader-x.x.x-all.jar`)を入手します。

* https://github.com/onozaty/redmine-issue-loader/releases/latest

入手したjarファイルを指定してアプリケーションを実行します。

```
java -jar redmine-issue-loader-2.2.0-all.jar config.json issues.csv
```

第1引数が設定ファイル、第2引数がチケットの情報が書かれたCSVファイルとなります。

実行すると、下記のように処理されたチケットの情報が出力されます。

```
Processing start...
#1 is created.
#2 is created.
#3 is created.
Processing is completed. 3 issues were loaded.
```

## 設定ファイル

設定ファイルには、Redmineの接続情報や、CSVファイルとRedmine上のフィールドのマッピング情報を記載します。

### 例: 新規作成時

新規作成時の設定ファイルの例です。

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
      "customFieldId": 2
    }
  ]
}
```

上記に対応するCSVファイルの例です。

```csv
Project,Tracker,Subject,Description,Field1,Field2,Field3
Project A,Bug,xxxx,yyyy,A,B,C
Project B,Feature,aaaa,bbbb,,,
Project B,Bug,zzzz,zzzz,1,2,3
```

### 例: 更新時

更新時の設定ファイルの例です。

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

上記に対応するCSVファイルの例です。

```csv
#,Subject,Status Id,Field1
1,xxxx,1,A
2,yyyy,2,B
3,zzzz,3,C
```

### 各項目の内容

各項目の内容は下記の通りです。

* `mode` : 処理モード。`CREATE`が新規作成、`UPDATE`が更新。
* `readmineUrl` : Redmineの接続先URL。
* `apiKey` : RedmineのAPIアクセスキー。
* `timeout` : Redmineへリクエスト時のタイムアウト秒。未設定の場合は`10`となる。
* `basicAuth` : RedmineのBasic認証で利用する情報。(APIアクセスキーまたはBasic認証のどちらかを指定する必要があります)
  * `username` : Basic認証で利用するユーザ名。
  * `password` : Basic認証で利用するパスワード。
* `csvEncoding` : CSVファイルのエンコーディング。
* `fields` : CSVの各フィールド情報。CSV内の全てのフィールドを記載するのではなく、キーとして使用するものと、Redmineに登録するフィールドを記載する。
    * `headerName` : CSV内のヘッダ名。
    * `type` : 種別。種別として指定可能なものは後述。
    * `customFieldId` : カスタムフィールドのID。種別が`CUSTOM_FIELD`の場合に設定する。
    * `primaryKey` : プライマリーキーか。更新時のみ有効な項目であり、`true`となっているフィールドの情報を使って更新対象のチケットを検索し、`false`となっているフィールドが更新されることとなる。
    * `mappings` : CSV上の値とRedmine上での値のマッピングを記載することによって、CSVの内容を変換して登録できる。たとえば、プロジェクト名をプロジェクトIDに変換する場合など。

フィールドの種別として指定可能なものは、下記となります。

|種別名|新規作成|更新|内容|ID確認URL|
|------|-------|----|----|----|
|`ISSUE_ID`|×|○|チケットのID。更新時のプライマリーキーとしてのみ指定可能。|-|
|`PROJECT_ID`|○|○|プロジェクトのID。新規作成時は必須項目となる。|`/projects.xml`|
|`TRACKER_ID`|○|○|トラッカーのID。|`/trackers.xml`|
|`STATUS_ID`|○|○|ステータスのID。|`/issue_statuses.xml`|
|`PRIORITY_ID`|○|○|優先度のID。|`/enumerations/issue_priorities.xml`|
|`ASSIGNED_TO_ID`|○|○|担当者のID。|`/users.xml`|
|`CATEGORY_ID`|○|○|カテゴリのID。|`/projects/:project_id/issue_categories.xml`<br>`:project_id`の部分は、対象プロジェクトのIDを指定。|
|`FIXED_VERSION_ID`|○|○|対象バージョンのID。|`/projects/:project_id/versions.xml`<br>`:project_id`の部分は、対象プロジェクトのIDを指定。|
|`PARENT_ISSUE_ID`|○|○|親チケットのID。|-|
|`SUBJECT`|○|○|題名。新規作成時は必須項目となる。|-|
|`DESCRIPTION`|○|○|説明。|-|
|`START_DATE`|○|○|開始日。`YYYY-MM-DD`または`YYYY/MM/DD`形式にて。|-|
|`DUE_DATE`|○|○|期日。`YYYY-MM-DD`または`YYYY/MM/DD`形式にて。|-|
|`DONE_RATIO`|○|○|進捗率。|-|
|`IS_PRIVATE`|○|○|プライベートか。`true`または`false`を指定。|-|
|`ESTIMATED_HOURS`|○|○|予定工数。|-|
|`CUSTOM_FIELD`|○|○|カスタムフィールド。更新時のプライマリーキーとしても利用できる。<br>この種別を指定する際には、`customFieldId`として対応するカスタムフィールドのIDを指定する必要がある。|`/custom_fields.xml`|

IDとして指定するものは、上記表のID確認URLでIDを確認することができます。

たとえば、RedmineのURLが`http://localhost`となっている環境で、プロジェクトのIDを確認したい場合、`http://localhost/projects.xml`でアクセスすることによって、下記のような形式でプロジェクトのIDが確認できます。

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

CSV上でプロジェクト名で書かれている場合、上記の内容をもとに`mappings`を指定することによって、IDへの変換を行うことができます。
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

設定ファイルとCSVファイルのサンプルは、`sample`フォルダ配下にありますので、そちらを参考にカスタマイズしてみてください。

## 注意事項

* Redmine の REST API を利用しますので、REST APIが有効になっている必要があります。
* カスタムフィールドをキーとする場合、対象のカスタムフィールドの設定として「フィルタとして使用」がONとなっている必要があります。

## ビルド方法

ソースコードからビルドして利用する場合、Java(JDK8以上)がインストールされた環境で、下記コマンドでアプリケーションをビルドします。

```
gradlew shadowJar
```

`build/libs/redmine-issue-loader-x.x.x-all.jar`という実行ファイルが出来上がります。(`x.x.x`はバージョン番号)

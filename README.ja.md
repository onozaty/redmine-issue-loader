# redmine-issue-updater

Redmineのチケットを更新するツールです。CSVファイルを読み込んで、チケットを更新します。

現在更新対象としているのは、カスタムフィールドのみとなります。

チケットIDだけでなく、カスタムフィールドをキーとしてチケットを更新することが可能です。ただし、そのカスタムフィールドの値が、システム全体で一意である必要があります。

## 利用方法

Java(JDK8以上)がインストールされた環境で、下記コマンドでアプリケーションをビルドします。

```
gradlew shadowJar
```

`build/libs/redmine-issue-updater-all.jar`というファイルが出来上がります。

下記のコマンドで、アプリケーションを実行します。

```
java -jar build/libs/redmine-issue-updater-1.0.0-all.jar config.json issues.csv
```

第1引数が設定ファイル、第2引数が更新する情報が書かれたCSVファイルとなります。

実行すると、下記のように更新されたチケットの情報が出力されます。

```
Processing start...
#1 is updated.
#2 is updated.
#3 is updated.
Processing is completed. 3 issues were updated.
```

以下は設定ファイルの例です。

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

各項目の内容は下記の通りです。

* `readmineUrl` : Redmineの接続先URL。
* `apyKey` : RedmineのAPIアクセスキー。
* `csvEncoding` : CSVファイルのエンコーディング。
* `fields` : CSVの各フィールド情報。CSV内の全てのフィールドを記載するのではなく、キーとして使用するものと、更新対象のフィールドを記載する。
    * `headerName` : CSV内のヘッダ名。
    * `type` : 種別。(`ISSUE_ID` または `CUSTOM_FIELD`)
    * `customFieldId` : カスタムフィールドのID。種別が`CUSTOM_FIELD`の場合に設定する。
    * `primaryKey` : プライマリーキーか。`true`となっているフィールドの情報を使って更新対象のチケットを検索し、`false`となっているフィールドが更新されることとなる。

カスタムフィールドのIDは、管理者画面のカスタムフィールドの設定画面で、対象のカスタムフィールドを選択した際のURLで確認できます。
以下のような場合、カスタムフィールドのIDは`1`となります。

* http://localhost/custom_fields/1/edit

または、チケット作成、編集画面でカスタムフィールドの入力欄に振られたIDでも確認できます。以下のような場合、カスタムフィールドのIDは`2`となります。

```html
<input type="text" name="issue[custom_field_values][2]" id="issue_custom_field_values_2" value="A" class="string_cf">
```

以下はCSVファイルの例です。

```csv
#,Subject,Field1,Field2,Field3
1,xxxx,A,a,C
2,yyyy,B,b,B
3,zzzz,C,c,A
```

設定ファイルとCSVファイルのサンプルは、`sample`フォルダ配下にあります。

## 注意事項

* Redmine の REST API を利用しますので、REST APIが有効になっている必要があります。
* カスタムフィールドをキーとする場合、対象のカスタムフィールドの設定として「フィルタとして使用」がONとなっている必要があります。

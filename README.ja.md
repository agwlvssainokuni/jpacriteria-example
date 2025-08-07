# JPA Criteria API サンプル

このプロジェクトは、JPA Criteria APIとHibernate拡張機能の包括的な使用パターンを実践的な例で示しています。

## 概要

JPA Criteria API サンプルプロジェクトは、標準JPA機能とHibernate固有の拡張機能を含む、JPA Criteria APIの効果的な使用方法を示す豊富なコードサンプルを提供します。このプロジェクトは、Javaアプリケーションで動的クエリを扱う開発者にとって包括的なリファレンスとして機能します。

## 機能

### JPA Criteria API の例
- **基本的な使用法**: エンティティ取得、Tupleクエリ、fetch/join操作、カーソル処理
- **SELECT句**: カラム選択、定数、計算、関数、CASE式、集約、スカラサブクエリ
- **FROM句**: 単一/複数テーブルクエリ、内部/外部結合、fetch戦略、関連処理
- **WHERE句**: 単純/複合条件、LIKE/IN/NULL/BETWEEN述語、EXISTS/NOT EXISTSサブクエリ
- **その他の句**: GROUP BY、HAVING、ORDER BY、SELECT FOR UPDATE（悲観的ロック）

### Hibernate拡張機能
- **WITH句（CTE）**: 連番生成のためのWITH RECURSIVEを使用した共通テーブル式
- **FROM句拡張**: 関連が定義されていないエンティティ間のJOIN操作、サブクエリによる派生テーブル
- **拡張関数**: JPA標準を超えた高度な数値、文字列、日時関数

## 技術スタック

- **Java**: 21
- **Spring Boot**: 3.5.4
- **Hibernate**: 6.6.22.Final（Spring Boot経由）
- **データベース**: H2（デフォルト）、MySQL（オプション）
- **ビルドツール**: Gradle 8.14.3
- **Lombok**: 1.18.38

## プロジェクト構成

```
src/
├── entity/java/com/example/db/entity/     # JPAエンティティ（別ソースセット）
├── main/java/com/example/
│   ├── jpa/                              # 標準JPA Criteriaの例
│   │   ├── JpaBasicUsageExample.java
│   │   ├── JpaSelectClauseExample.java
│   │   ├── JpaFromClauseExample.java
│   │   ├── JpaWhereClauseExample.java
│   │   └── JpaOtherUsageExample.java
│   ├── hibernate/                        # Hibernate固有の拡張機能
│   │   ├── HibernateWithClauseExample.java
│   │   ├── HibernateFromClauseExample.java
│   │   └── HibernateFunctionExample.java
│   ├── JpaConfiguration.java             # JPA設定
│   ├── Main.java                         # アプリケーションエントリーポイント
│   ├── PrepareExample.java               # サンプルデータ設定
│   └── Runner.java                       # 例の実行コーディネーター
└── main/resources/
    ├── application.properties            # デフォルト設定（H2）
    └── application-mysql.properties      # MySQL設定
```

## はじめに

### 前提条件

- Java 21以上
- Docker（MySQL用、オプション）

### H2での実行（デフォルト）

```bash
# プロジェクトをビルド
./gradlew build

# H2インメモリデータベースで実行
./gradlew bootRun
```

### MySQLでの実行

1. Docker Composeを使用してMySQLを開始：
```bash
docker-compose up -d mysql
```

2. MySQLプロファイルでアプリケーションを実行：
```bash
./gradlew bootRun --args='--spring.profiles.active=mysql'
```

## アーキテクチャのハイライト

### マルチソースセット設定

このプロジェクトは、JPAメタモデル生成を処理するためのユニークなGradleソースセット設定を使用しています：

- **`src/entity/`**: 別のソースセットにJPAエンティティを含む
- **`src/main/`**: エンティティを使用するメインアプリケーションコード
- **生成されたメタモデル**: `build/generated/sources/annotationProcessor/java/entity`に自動作成

この分離により、メタモデル生成でエンティティを最初にコンパイルする必要がある場合のコンパイル問題を防ぎ、循環依存の問題を回避します。

#### Gradle設定の詳細

build.gradleには3つのソースセットが含まれています：

```gradle
sourceSets {
    entity                    // JPAエンティティ
    entitymodel {             // 生成されたJPAメタモデル（IDE サポート用）
        java {
            srcDir file('build/generated/sources/annotationProcessor/java/entity')
        }
    }
}
```

重要なポイント：
- **エンティティコンパイル**: `hibernate-jpamodelgen`によってメタモデルを生成するために、エンティティを最初にコンパイルする必要があります
- **IDE互換性**: `entitymodel`ソースセットにより、IntelliJ IDEAが生成されたメタモデルクラスを認識できます
- **ビルドプロセス**: Gradleは`entitymodel`ソースセットなしでも正常にビルドできますが、IDEでエラーのない開発には必須です
- **メタモデル生成**: アノテーションプロセッサがエンティティコンパイル時に自動的にメタモデルクラス（例：`Customer_.java`）を生成します

### 例の実行フロー

1. **PrepareExample**: サンプルデータ（Customer、Product、SalesOrderなど）を設定
2. **JPA Examples**: 標準JPA Criteria APIパターンを実証
3. **Hibernate Examples**: Hibernate固有の拡張機能と高度な機能を表示

## 主な学習ポイント

### N+1問題の解決策
- 関連エンティティのイーガーロードに`fetch()`を使用
- 異なるユースケースでのfetch vs join戦略の比較

### 複雑なクエリパターン
- 計算フィールドのスカラサブクエリ
- 効率的な存在チェックのためのEXISTS/NOT EXISTS
- NOT EXISTSを使用した履歴テーブルからの最新レコード抽出

### Hibernate高度機能
- 再帰制限を迂回する連番生成のためのWITH RECURSIVE
- ON句を使用した関連定義のないエンティティ間のクロスJOIN
- FROM句でのサブクエリによる派生テーブル

## 開発

### ビルドコマンド
```bash
# クリーンビルド
./gradlew clean build

# テスト実行
./gradlew test

# ドキュメント生成
./gradlew javadoc
```

### データベーススキーマ
アプリケーションは以下のエンティティで自動生成されたスキーマを使用します：
- Customer（顧客）
- Product（商品）
- SalesOrder（ステータス付き販売注文）
- SalesOrderItem（注文明細）
- SalesOrderHistory（注文変更履歴）

## 設定

### H2設定
- コンソールアクセス付きインメモリデータベース
- 自動DDL生成が有効
- 学習目的でSQLログが有効

### MySQL設定
- Docker Compose経由の接続
- データベース: `jpacriteria`
- ユーザー: `jpacriteria` / パスワード: `jpacriteria`

## ライセンス

Apache License, Version 2.0でライセンスされています。詳細はLICENSEファイルを参照してください。

## 貢献

このプロジェクトは教育リソースとして機能します。新しいJPA/Hibernateパターンを追加したり、既存の例を改善したりする貢献を歓迎します。

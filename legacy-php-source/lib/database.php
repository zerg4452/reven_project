<?php

declare(strict_types=1);

require_once __DIR__ . '/../config.php';

function db_pdo(): PDO
{
    static $pdo = null;

    if ($pdo instanceof PDO) {
        return $pdo;
    }

    $dsn = sprintf('mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4', DB_HOST, DB_PORT, DB_NAME);
    $pdo = new PDO($dsn, DB_USER, DB_PASSWORD, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);

    return $pdo;
}

function db_execute(string $sql, array $params = []): PDOStatement
{
    $statement = db_pdo()->prepare($sql);
    $statement->execute($params);
    return $statement;
}

function db_fetch_all(string $sql, array $params = []): array
{
    return db_execute($sql, $params)->fetchAll();
}

function db_fetch_one(string $sql, array $params = []): ?array
{
    $row = db_execute($sql, $params)->fetch();
    return is_array($row) ? $row : null;
}

function db_column_exists(string $table, string $column): bool
{
    $table = str_replace('`', '``', $table);
    try {
        foreach (db_fetch_all(sprintf('SHOW COLUMNS FROM `%s`', $table)) as $row) {
            if (($row['Field'] ?? '') === $column) {
                return true;
            }
        }
    } catch (Throwable $e) {
        return false;
    }
    return false;
}

function db_table_exists(string $table): bool
{
    $table = str_replace('`', '``', $table);
    try {
        $row = db_fetch_one(sprintf("SHOW TABLES LIKE '%s'", $table));
        return is_array($row) && !empty($row);
    } catch (Throwable $e) {
        return false;
    }
}

function audit_columns_sql(): string
{
    return <<<SQL
idx INT NOT NULL AUTO_INCREMENT,
reg_dtm DATETIME NOT NULL,
reg_id VARCHAR(100) NOT NULL,
mod_dtm DATETIME NOT NULL,
mod_id VARCHAR(100) NOT NULL,
PRIMARY KEY (idx)
SQL;
}

1. О проекте

Небольшая база данных на Java: таблицы, SQL-подобные запросы, хранение в файлах, индексы.
Предметная область - университет: таблица faculties (факультеты) и таблица students (студенты). В students.faculty_id лежит id из faculties. Таблицы создаёт сам пользователь командой CREATE TABLE

2. Что умее
- Типы данных: INT, DOUBLE, STRING, DATE, BOOLEAN
- INSERT одной или нескольких строк. Повтор первичного ключа — ошибка, ничего не добавляется
- SELECT, UPDATE, DELETE с условием WHERE столбец знак значение. Знаки: =, !=, <>, <, <=, >, >=
- CREATE TABLE и DROP TABLE
- CREATE INDEX имя ON таблица (столбец): индекс сохраняется на диск, загружается при запуске, ускоряет SELECT
- Данные сохраняются в файлы после каждого изменения и загружаются при запуске
- Ошибки ввода не роняют программу, выводится "Error: ..."

3. Ограничения

- Один первичный ключ в таблице, менять его через UPDATE нельзя
- В WHERE одно условие (без AND и OR)
- Связь faculty_id с faculties программа не проверяет

4. Как запустить

- Сборка mkdir -p out, затем javac -d out src/minidb/*.java
- Запуск java -cp out minidb.Main или java -cp out minidb.Main -d папка
- Замеры скорости java -cp out minidb.Benchmark
- Javadoc javadoc -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 -d docs -sourcepath src -subpackages minidb
Каждый запрос заканчивается знаком ;. Выход — EXIT.

5. Примеры запросов

CREATE TABLE faculties (id INT PRIMARY KEY, name STRING, budget DOUBLE, founded DATE, has_dormitory BOOLEAN);
CREATE TABLE students (id INT PRIMARY KEY, name STRING, course INT, gpa DOUBLE,
                       birth_date DATE, scholarship BOOLEAN, faculty_id INT);
INSERT INTO faculties VALUES (1, 'Informatics', 1500000.5, '1998-09-01', TRUE);
INSERT INTO students VALUES (1, 'Anna', 2, 4.7, '2005-04-12', TRUE, 1);
INSERT INTO students (id, name) VALUES (2, 'Ivan');
INSERT INTO students VALUES (3, 'Maria', 3, 3.9, '2004-08-30', FALSE, 1), (4, 'Oleg', 1, 4.2, '2006-01-20', TRUE, 1);
SELECT * FROM students;
SELECT name, gpa FROM students WHERE gpa > 4;
CREATE INDEX idx_students_course ON students (course);
SELECT * FROM students WHERE course = 2;
UPDATE students SET gpa = 4.9 WHERE id = 1;
DELETE FROM students WHERE name = 'Ivan';
DROP TABLE students;

Правила даты и строки — в одинарных кавычках; логические значения — TRUE и FALSE; столбцы, которых нет в INSERT, остаются пустыми (NULL).

6. Архитектура

- Main, TablePrinter — консоль: читают запросы, печатают результат таблицей
- Parser — превращает текст запроса в объект-запрос
- Statement и 7 классов ...Statement — каждый выполняет свою команду 
- Database — набор таблиц, их изменение и сохранение 
- TableStorage, FileTableStorage — запись и чтение файлов 
- Table, Column, Row, DataType, Condition, Op, Index — данные, типы, условие, индекс
- Result, DbException, Benchmark — результат запроса, ошибки, замеры

Связи между классами 

- Main использует Parser и TablePrinter.
- Parser создаёт объекты Statement.
- Statement — интерфейс. Его реализуют: CreateTableStatement, DropTableStatement, CreateIndexStatement, InsertStatement, SelectStatement, UpdateStatement, DeleteStatement.
- Statement работает с Database и возвращает Result.
- Database содержит Table и использует TableStorage.
- TableStorage — интерфейс, его реализует FileTableStorage.
- Table состоит из Column и Row, может иметь Index и использует Condition.

9. Замеры скорости

Таблица students: 200 000 строк. Каждый запрос выполнен 100 раз, показано среднее время одного запроса. 
- Запрос WHERE name = 'name_777': найдено 40 строк; без индекса 2.612 мс, с индексом 0.050 мс - быстрее в 52 раза.
- Запрос WHERE tuition > 99900: найдено 206 строк; без индекса 2.042 мс, с индексом 0.079 мс - быстрее в 26 раз.


10. Кто что делал

- Замятина Елена — разбор запросов и консоль: Parser, Statement, CreateTableStatement, DropTableStatement, CreateIndexStatement, InsertStatement, SelectStatement, UpdateStatement, DeleteStatement, DataType, Result, TablePrinter, Benchmark, Main. 
- Стрежнева София — данные, индексы и хранение: Table, Column, Row, Index, Condition, Op, Database, TableStorage, FileTableStorage, DbException. 
- Вместе: README и проверка кода друг друга (pull request) и финальная проверка сборки и запуска.

11. Выводы
- Сделана простая СУБД: пять типов данных, SQL-подобные запросы, хранение в файлах, индексы.
- Таблицы и типы данных: создание и удаление таблиц (CREATE TABLE, DROP TABLE), пять типов столбцов (INT, DOUBLE, STRING, DATE, BOOLEAN), пустые значения (NULL)
- SQL-подобные запросы: INSERT (одной или нескольких строк, с указанием столбцов или без), SELECT (все или выбранные столбцы), UPDATE и DELETE. Условие WHERE поддерживает знаки =, !=, <>, <, <=, >, >=.
- Свой разбор запросов: Parser превращает текст запроса в объект-команду (паттерн Command). Запросы можно писать в несколько строк, знак ; внутри кавычек не считается концом запроса.
- Индексы: CREATE INDEX строит индекс на TreeMap. Он ускоряет SELECT с условиями =, <, <=, >, >=, обновляется при INSERT, UPDATE и DELETE и хранится на диске вместе с таблицей. На 200 000 строк запросы стали быстрее в 26–52 раза

12. Источники
https://docs.oracle.com/javase/8/docs/api/java/io/Console.html
https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html
https://habr.com/en/articles/494316
https://habr.com/post/330544/

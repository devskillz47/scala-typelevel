CREATE TABLE jobRepo(
     id UUID DEFAULT gen_random_uuid(),
     postDate bigint NOT NULL,
     ownerEmail text NOT NULL,
     company text NOT NULL,
     image text,
     title text NOT NULL,
     description text NOT NULL,
     remote boolean NOT NULL,
     externalUrl text NOT NULL,
     salaryHigh integer,
     salaryLow integer,
     currency text,
     location text,
     country text,
     tags text[],
     seniority text,
     other text,
     active boolean NOT NULL DEFAULT false
);

ALTER TABLE jobRepo
ADD CONSTRAINT pk_jobRepo PRIMARY KEY (id);

INSERT INTO
  "jobrepo" ("id", "postdate", "owneremail", "company", "image", "title",  "description", "remote", "externalurl",
    "salaryhigh", "salarylow", "currency", "location", "country", "tags", "seniority", "other", "active")
VALUES
  ('843df718-ec6e-4d49-9289-f799c0f40064', 1659186086, 'daniel@rockthejvm.com', 'Awesome Company', NULL,
   'An awesome job in Berlin', 'Tech Lead', false, 'https://rockthejvm.com/awesomejob',
   2000, 3000, 'EUR', 'Berlin', 'Germany', ARRAY ['scala', 'scala-3', 'cats'], 'Senior', NULL, false);

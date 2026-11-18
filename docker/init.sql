CREATE DATABASE typelevel;
\c typelevel;

    CREATE TABLE jobRepo(
         id UUID DEFAULT gen_random_uuid(),
         postDate bigint NOT NULL,
         ownerEmail text NOT NULL,
         company text NOT NULL,
         image text NOT NULL,
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
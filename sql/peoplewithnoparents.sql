SELECT p.personId, p.name
FROM people p
WHERE p.personId NOT IN (
  SELECT personId FRom births
);
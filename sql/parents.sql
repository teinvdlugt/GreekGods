-- The example here used Zeus (id 64)
SELECT p.name
FROM kcv.people p
WHERE (p.personId IN (
  SELECT r.personId1
  FROM kcv.relations r
  WHERE r.relatiod_id = (
    SELECT b.relationId
    FROM kcv.births b
    WHERE b.personId = 64
  )) OR (p.personId IN (
    SELECT r.personId2
    FROM kcv.relations r
    WHERE r.relatiod_id = (
      SELECT b.relationId
      FROM kcv.births b
      WHERE b.personId = 64
    )
  ))
)
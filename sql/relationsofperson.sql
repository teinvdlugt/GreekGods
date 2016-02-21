SELECT
  p.personId,
  p.name,
  r.relatiod_id
FROM kcv.people p, kcv.relations r
WHERE (r.personId1 = 64 AND p.personId = r.personId2)
      OR (r.personId2 = 64 AND p.personId = r.personId1)
      OR (((r.personId1 IS NULL AND r.personId2 = 64)
           OR r.personId2 IS NULL AND r.personId1 = 64) AND p.personId = 64)
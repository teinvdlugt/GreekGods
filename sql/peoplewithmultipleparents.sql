SELECT p.personId, p.name
FROM kcv.people p
WHERE (
  SELECT COUNT(b.relationId)
  FROM kcv.births b
  WHERE b.personId = p.personId
) > 1;
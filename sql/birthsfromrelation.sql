SELECT p.personId, p.name
FROM kcv.people p, kcv.births b
WHERE b.relationId = 14 AND p.personId = b.personId
SELECT *
FROM kcv.people p
WHERE p.personId IN (
  SELECT b.personId
  FROM kcv.births b, kcv.book_mentions_birth bmb
  WHERE bmb.birth_id = b.birth_id AND bmb.book_id = 1
) AND p.personId IN (
  SELECT b2.personId
  FROM kcv.births b2, kcv.book_mentions_birth bmb2
  WHERE bmb2.birth_id = b2.birth_id AND bmb2.book_id = 2
)

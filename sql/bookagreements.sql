SELECT *
FROM kcv.people
WHERE people.personId IN (
  SELECT b.personId
  FROM kcv.births b
  WHERE 1 IN (
    SELECT book_mentions_birth.book_id
    FROM kcv.book_mentions_birth
    WHERE book_mentions_birth.birth_id = b.birth_id
  ) AND 2 IN (
    SELECT book_mentions_birth.book_id
    FROM kcv.book_mentions_birth
    WHERE book_mentions_birth.birth_id = b.birth_id
  )
)
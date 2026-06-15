# CHILDREN_COUNT
update icd10.ICD10_CLASS as c1,
(
Select SUPER_CLASS as s, count(d1.code3) as cnt
From icd10.ICD10_CLASS as c2
	JOIN (
		Select CODE as code3
		From icd10.ICD10_CLASS as c3
	) as d1
	ON d1.code3 = c2.SUPER_CLASS group by SUPER_CLASS
) as d2
Set c1.CHILDREN_COUNT = d2.cnt
where c1.CODE = d2.s;

# PATH
update icd10.ICD10_CLASS as c1,
(
	Select CODE as code1, PATH as p1, d1.code2 as d1c2, d1.p2 as d1p2
	From icd10.ICD10_CLASS as c2
		JOIN (
			Select CODE as code2, SUPER_CLASS as s, PATH  as p2
			From icd10.ICD10_CLASS as c3
			WHERE PATH = ""
		) as d1
		ON c2.CODE = d1.s group by d1.code2
	) as d2
Set c1.PATH = CONCAT(d2.p1, "~", d2.code1)
where c1.CODE = d2.d1c2;

# DESCENDANT_COUNT
update icd10.ICD10_CLASS as c,
(
SELECT TRIM(TRAILING '%' FROM SUBSTRING_INDEX(g.p,'~',-1)) as s, count(*) as cnt
FROM icd10.ICD10_CLASS AS c1
        INNER JOIN
        (
                SELECT CONCAT(PATH, '~', CODE, '%') AS p
                FROM icd10.ICD10_CLASS as c2
		) AS g
WHERE c1.PATH LIKE g.p GROUP BY g.p
) as i
SET c.DESCENDANT_COUNT = i.cnt
WHERE c.CODE = i.s;

#DESCENDANT_COUNT for chapter
update icd10.ICD10_CLASS as c,
(
	SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(g.p,'~',-2), '~', 1) as s, count(*) as cnt
FROM icd10.ICD10_CLASS AS c1
        INNER JOIN
        (
                SELECT CONCAT(PATH, '~', CODE, '~' ,'%') AS p
                FROM icd10.ICD10_CLASS as c2
                WHERE c2.CLASS_KIND='chapter'
		) AS g
        ON c1.PATH LIKE g.p 
GROUP BY g.p
) as i
SET c.DESCENDANT_COUNT = i.cnt
WHERE c.CODE = i.s;

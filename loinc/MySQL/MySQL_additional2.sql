USE loinc;

\! echo 'additional2';

\! echo '- Initializing HIERARCHY_LG Table...';
INSERT INTO loinc.HIERARCHY_LG (CODE, PATH, PARENT, TYPE, SEQUENCE, NAME, PREFERRED_NAME)
VALUES
#("CLASS", "", "", 0, 1, "LOINCCLASSTYPES", "Class Types"),
#("PARTS", "", "", 0, 2, "LOINCPARTS","Parts"),
#("GROUP", "", "", 0, 3, "LOINCGROUP","Group"),
("Document groups", "GROUP", "GROUP", 1, 0, "Document groups", "Document groups"),
("Drugs of abuse", "GROUP", "GROUP", 1, 0, "Drugs of abuse", "Drugs of abuse"),
("Exercise", "GROUP", "GROUP", 1, 0, "Exercise", "Exercise"),
("Eye microbiology", "GROUP", "GROUP", 1, 0, "Eye microbiology", "Eye microbiology"),
("Flowsheet - laboratory", "GROUP", "GROUP", 1, 0, "Flowsheet - laboratory", "Flowsheet - laboratory"),
("Flowsheet - Vital signs", "GROUP", "GROUP", 1, 0, "Flowsheet - Vital signs", "Flowsheet - Vital signs"),
("Flowsheet - weight, height, and head circumference", "GROUP", "GROUP", 1, 0, "Flowsheet - weight, height, and head circumference", "Flowsheet - weight, height, and head circumference"),
("Genitourinary microbiology", "GROUP", "GROUP", 1, 0, "Genitourinary microbiology", "Genitourinary microbiology"),
("Mass-Molar conversion", "GROUP", "GROUP", 1, 0, "Mass-Molar conversion", "Mass-Molar conversion"),
("Obstetrics", "GROUP", "GROUP", 1, 0, "Obstetrics", "Obstetrics"),
("Radiology", "GROUP", "GROUP", 1, 0, "Radiology", "Radiology"),
("Reportable microbiology", "GROUP", "GROUP", 1, 0, "Reportable microbiology", "Reportable microbiology"),
("Respiratory microbiology", "GROUP", "GROUP", 1, 0, "Respiratory microbiology", "Respiratory microbiology"),
("Smoking - biochemical markers", "GROUP", "GROUP", 1, 0, "Smoking - biochemical markers", "Smoking - biochemical markers"),
("Smoking - history", "GROUP", "GROUP", 1, 0, "Smoking - history", "Smoking - history"),
("Social determinants of health", "GROUP", "GROUP", 1, 0, "Social determinants of health", "Social determinants of health");

\! echo '- INSERT LG_TERMS...';
# LOINC_TERMS를 HIERARCHY_LG로 insert
#(LG_TERMS)
#CODE : 24134-9, 
#PATH : Flowsheet - laboratory, 
#LG8749-6, 3, 1,
#Deprecated Tree Allergen Mix 6 (Boxelder+Silver birch+American beech+White oak+California walnut) IgE Ab [Presence] in Serum by Multiple allergens
INSERT INTO loinc.HIERARCHY_LG
  (CODE, PATH, PARENT, TYPE, SEQUENCE, NAME)
  SELECT t.LOINC_NUMBER, CONCAT('GROUP', '~', t.CATEGORY), LG_ID, 3, 1, LONG_COMMON_NAME
  FROM loinc.LG_TERMS as t;

\! echo '- INSERT PARENT_LG...';
# PARENT_LG INSERT
INSERT INTO loinc.HIERARCHY_LG
  (CODE, PATH, PARENT, TYPE, SEQUENCE, NAME, PREFERRED_NAME)
  SELECT DISTINCT p.PARENT_LG_ID, CONCAT('GROUP', '~', j2.f2), j2.f2, 3, 1, p.PARENT_LG, p.PARENT_LG
  FROM loinc.PARENT_LG as p
  JOIN (
    SELECT DISTINCT f1, CONCAT(t.CATEGORY) as f2, j.f3, 3, 1, j.f6
	FROM loinc.LG_TERMS as t
	  JOIN (   
	    SELECT lg.LG_ID as f1, lg.PARENT_LG_ID as f2, lg.PARENT_LG_ID as f3, lg.LG as f6 
		FROM loinc.LG as lg  WHERE lg.LG_ID in (
	          SELECT DISTINCT h.PARENT  FROM loinc.HIERARCHY_LG as h 
		) and lg.STATUS = 'Active'
	  ) as j
	  ON t.LG_ID = j.f1
   ) as j2
   ON p.STATUS = 'ACTIVE' and p.PARENT_LG_ID = j2.f3;

\! echo '- UPDATE LOINC_CODE by LG...'
# "LG의 LG_ID"가 "HIERARCHY_LG의 PARENT"와 같으면,
# HIERARCHY_LG의 PATH에 LG의 PARENT_LG_ID를 추가UPDATE
UPDATE loinc.HIERARCHY_LG as h, loinc.LG as lg
SET h.PATH = concat(h.PATH, '~', lg.PARENT_LG_ID, '~', lg.LG_ID)
where lg.LG_ID = h.PARENT;

\! echo '- INSERT GROUP by LG...'
# "LG의 LG_ID"가 "HIERARCHY_LG의 PARENT"와 같으면,
# HIERARCHY_LG에 LG의 LG_ID를 INSERT
INSERT into loinc.HIERARCHY_LG 
  (CODE, PATH, PARENT, TYPE, SEQUENCE, NAME, PREFERRED_NAME)
  SELECT DISTINCT f1, CONCAT('GROUP', '~', t.CATEGORY, '~', j.f2), j.f3, 3, 1, j.f6, j.f6
  FROM loinc.LG_TERMS as t
    JOIN (   
      SELECT lg.LG_ID as f1, lg.PARENT_LG_ID as f2, lg.PARENT_LG_ID as f3, lg.LG as f6 
      FROM loinc.LG as lg  WHERE lg.LG_ID in (
	    SELECT DISTINCT h.PARENT  FROM loinc.HIERARCHY_LG as h 
  	  ) and lg.STATUS = 'Active'
    ) as j
    ON t.LG_ID = j.f1; 

# CHILDREN_COUNT
\! echo '- CHILDREN_COUNT...'
update loinc.HIERARCHY_LG as h1,
(
  Select h1.PARENT as p, count(j1.c) as cnt
  From loinc.HIERARCHY_LG as h1
    JOIN (
	  Select CODE as c
	    From loinc.HIERARCHY_LG as h
	) as j1
	ON j1.c = h1.PARENT group by h1.PARENT
) as j2
Set h1.CHILDREN_COUNT = j2.cnt
where h1.CODE = j2.p;

# DESCENDANT_COUNT
\! echo '- DESCENDANT_COUNT...'
update loinc.HIERARCHY_LG as h3,
(
  SELECT TRIM(TRAILING '%' FROM SUBSTRING_INDEX(j.p,'~',-1)) as s, count(*) as cnt
  FROM loinc.HIERARCHY_LG AS h2
    INNER JOIN
      (
        SELECT CONCAT(h.PATH, '~', h.CODE, '%') AS p
        FROM loinc.HIERARCHY_LG as h
      ) AS j
  WHERE h2.PATH LIKE j.p GROUP BY j.p
) as j2
SET h3.DESCENDANT_COUNT = j2.cnt
WHERE h3.CODE = j2.s;

\! echo '- UPDATE PREFERRED_TERM...'
UPDATE loinc.HIERARCHY_LG as h2,
(
  SELECT * 
  FROM loinc.LOINC as l2
    JOIN
    (
      SELECT h.CODE as c 
      FROM loinc.HIERARCHY_LG as h 
      WHERE h.CODE IN (
  	    SELECT l.CODE 
        FROM loinc.LOINC as l
      )
	) as j
  ON j.c = l2.CODE 
) as j2
SET h2.PREFERRED_NAME = CONCAT(j2.COMPONENT,":", j2.PROPERTY,":", j2.TIME_ASPECT,":", j2.SYSTEM,":", j2.SCALE_TYPE, ":", j2.METHOD_TYPE)
WHERE h2.CODE = j2.CODE;

# add HIERARCHY_LG to HIERARCHY
\! echo '- Transfer HIERARCHY_LG to HIERARCHY...';
INSERT INTO HIERARCHY (CHILDREN_COUNT, CODE, DESCENDANT_COUNT, PATH, PARENT, TYPE, SEQUENCE, NAME, PREFERRED_NAME)
SELECT lg.CHILDREN_COUNT, lg.CODE, lg.DESCENDANT_COUNT, lg.PATH, lg.PARENT, lg.TYPE, lg.SEQUENCE, lg.NAME, lg.PREFERRED_NAME
FROM  HIERARCHY_LG as lg;

#UPDATE : LG_TERMS.LG_ID_NAME = LG.LG_NAME
UPDATE loinc.LG_TERMS as t, loinc.LG as lg
SET t.LG_ID_NAME = lg.LG
where t.LG_ID = lg.LG_ID;

#UPDATE : LG_TERMS.LG_NAME = LG.LG_NAME
UPDATE loinc.LINGUISTIC_VARIANT as lv, loinc.LINGUISTIC_VARIANTS as lvs
SET lv.LANG = lvs.LANG
where lv.ISO_COUNTRY = lvs.ISO_COUNTRY and lv.ISO_LANG = lvs.ISO_LANG;

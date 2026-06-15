"""
Insert KCD-9 extended codes into icd10.ICD10_CLASS.
These are codes (e.g. A08.30) that exist in KCD-9 but not in WHO ICD-10.
"""
import csv, re, psycopg2

DB = dict(host='localhost', port=5432, dbname='term', user='postgres', password='julab123!')

def parent_of(code):
    """Derive parent code: A08.30->A08.3, A08.3->A08, A15.00->A15.0"""
    if re.match(r'^[A-Z]\d{2}\.\d{2,}', code):
        return code[:-1]
    if re.match(r'^[A-Z]\d{2}\.\d$', code):
        return code.split('.')[0]
    return None

# Read kcd9_main.tsv
all_codes = {}
with open('kcd9_main.tsv', encoding='utf-8') as f:
    reader = csv.DictReader(f, delimiter='\t')
    for row in reader:
        all_codes[row['CODE']] = row

ext = {k: v for k, v in all_codes.items() if v['IS_KCD_EXT'] == '1'}
print(f"Extended codes to insert: {len(ext)}")

conn = psycopg2.connect(**DB)
cur = conn.cursor()

# Fetch existing codes and their paths from DB
cur.execute("SELECT code, path FROM icd10.ICD10_CLASS")
existing = {row[0]: row[1] for row in cur.fetchall()}
print(f"Existing codes in DB: {len(existing)}")

# We'll insert in topological order (parents before children)
# Sort by code length (shorter = higher level)
sorted_ext = sorted(ext.keys(), key=lambda c: (len(c), c))

inserted = 0
skipped = 0
path_cache = dict(existing)  # code -> path

for code in sorted_ext:
    if code in existing:
        skipped += 1
        continue

    row = ext[code]
    p = parent_of(code)

    # Determine path
    parent_path = path_cache.get(p, '') if p else ''
    path = (parent_path + '~' + p) if parent_path and p else p or ''
    path_cache[code] = path

    # Determine class_kind (codes with children are 'block', leaves are 'category')
    has_children = any(parent_of(c) == code for c in ext)
    class_kind = 'block' if has_children else 'category'

    cur.execute(
        """INSERT INTO icd10.ICD10_CLASS
           (code, version, class_kind, usage_kind, super_class, label, ref,
            children_count, descendant_count, path, korean_label, is_kcd_ext)
           VALUES (%s, %s, %s, NULL, %s, %s, NULL, 0, 0, %s, %s, TRUE)
           ON CONFLICT (code) DO NOTHING""",
        (code, 'KCD-9', class_kind, p, row['ENGLISH_LABEL'], path, row['KOREAN_LABEL'])
    )
    inserted += 1
    if inserted % 500 == 0:
        conn.commit()
        print(f"  Inserted: {inserted}")

conn.commit()
print(f"Inserted: {inserted}, Skipped (already exist): {skipped}")

# Update children_count for all affected parents
print("Updating children_count for parent codes...")
cur.execute("""
    UPDATE icd10.ICD10_CLASS p
    SET children_count = (
        SELECT COUNT(*) FROM icd10.ICD10_CLASS c WHERE c.super_class = p.code
    )
    WHERE p.code IN (
        SELECT DISTINCT super_class FROM icd10.ICD10_CLASS WHERE is_kcd_ext = TRUE AND super_class IS NOT NULL
    )
""")
print(f"Updated {cur.rowcount} parent rows")
conn.commit()

cur.close()
conn.close()
print("Done.")

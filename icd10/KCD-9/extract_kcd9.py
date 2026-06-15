"""
Extract KCD-9 Excel masterfile into two TSV files for Java loader.
Output:
  kcd9_main.tsv  - code, korean_label, english_label, is_kcd_ext (sheet 2)
  kcd9_morph.tsv - code, korean_label, english_label (sheet 3)
"""
import openpyxl, csv, sys, os

EXCEL = os.path.join(os.path.dirname(__file__),
    '제9차 한국표준질병ㆍ사인분류 DB masterfile_251223_20251223031826.xlsx')
OUT_MAIN  = os.path.join(os.path.dirname(__file__), 'kcd9_main.tsv')
OUT_MORPH = os.path.join(os.path.dirname(__file__), 'kcd9_morph.tsv')

wb = openpyxl.load_workbook(EXCEL, read_only=True, data_only=True)

# ── Sheet 2: KCD-9 DB Masterfile ────────────────────────────────────────────
# Columns (0-based): 0=표제어, 1=분류기준, 2=코드, 3=검별, 4=주석,
#                     5=한글명칭, 6=영문명칭, 7=최하위코드, 8=국내세분화코드
ws2 = wb['KCD-9 DB Masterfile']
seen = set()
rows_main = []
for row in ws2.iter_rows(min_row=4, values_only=True):
    code = str(row[2]).strip() if row[2] is not None else ''
    if not code or code == 'None':
        continue
    korean = str(row[5]).strip() if row[5] is not None else ''
    english = str(row[6]).strip() if row[6] is not None else ''
    is_ext = 1 if (row[8] is not None and str(row[8]).strip() not in ('', '0', 'None')) else 0
    if not korean and not english:
        continue
    if code not in seen:
        seen.add(code)
        rows_main.append((code, korean, english, is_ext))

with open(OUT_MAIN, 'w', encoding='utf-8', newline='') as f:
    w = csv.writer(f, delimiter='\t')
    w.writerow(['CODE', 'KOREAN_LABEL', 'ENGLISH_LABEL', 'IS_KCD_EXT'])
    w.writerows(rows_main)
print(f"kcd9_main.tsv: {len(rows_main)} rows")

# ── Sheet 3: 신생물의 형태분류 ───────────────────────────────────────────────
# Columns: 0=코드, 1=한글명칭, 2=영문명칭
ws3 = wb['4편 신생물의 형태분류']
rows_morph = []
for row in ws3.iter_rows(min_row=4, values_only=True):
    code = str(row[0]).strip() if row[0] is not None else ''
    if not code or code == 'None':
        continue
    korean = str(row[1]).strip() if row[1] is not None else ''
    english = str(row[2]).strip() if row[2] is not None else ''
    rows_morph.append((code, korean, english))

with open(OUT_MORPH, 'w', encoding='utf-8', newline='') as f:
    w = csv.writer(f, delimiter='\t')
    w.writerow(['CODE', 'KOREAN_LABEL', 'ENGLISH_LABEL'])
    w.writerows(rows_morph)
print(f"kcd9_morph.tsv: {len(rows_morph)} rows")

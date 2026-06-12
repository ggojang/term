# 2021 SM.DB : synonym in UMLS (LEX folder) https://lhncbc.nlm.nih.gov/LSG/Projects/lexicon/current/web/release/index.html
# input : SM.DB -> SMDB.bak
# output : syn_ph.txt for phrase
#          syn.txt
sort -u SM.DB > SMDB.bak
gawk -f syn.awk -F '|' SMDB.bak
cat add_syn.txt >> syn.txt
cp *.txt ~/services/elasticsearch/elasticsearch-2.1.0/config/synonym

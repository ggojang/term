BEGIN {
	INDEX_TERM=""
	FOURTH_TERM=""
	printf("\n") > "syn_ph.txt"
	printf("\n") > "syn.txt"
}

{
  if (INDEX_TERM != $1) {
	  INDEX_TERM = $1
	  FOURTH_TERM = $4
	  printf("\n") > "syn.txt"
  		if ( index(INDEX_TERM, " ") != 0) {
			tmp = gensub(" ", "_", "g", $1)
			print INDEX_TERM, "=>", tmp > "syn_ph.txt"
			printf("%s", tmp) > "syn.txt"
		} else {
			printf("%s", INDEX_TERM) > "syn.txt"
		}

  		if (INDEX_TERM != $2) {
	  		check_field($2)
		}

		if ( INDEX_TERM != $4) {
			check_field($4)
		}

  } else {
  	if ( (INDEX_TERM != $4) && (FOURTH_TERM != $4)) {
  		check_field($4)
	}
  }
}

function check_field (str) {
	if ( index(str, " ") != 0) {
		tmp = gensub(" ", "_", "g", str)
  		print str, "=>", tmp > "syn_ph.txt"
  		printf(",%s", tmp) > "syn.txt"
 	} else {
 		printf(",%s", str) > "syn.txt"
 	}
}

